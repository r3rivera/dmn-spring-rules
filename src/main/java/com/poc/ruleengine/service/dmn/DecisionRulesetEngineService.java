package com.poc.ruleengine.service.dmn;

import com.poc.ruleengine.domain.DecisionResult;
import com.poc.ruleengine.domain.EvaluationPayload;
import com.poc.ruleengine.domain.EvaluationRequest;
import com.poc.ruleengine.domain.EvaluationResponse;
import com.poc.ruleengine.domain.dmn.DMNEntryRecord;
import com.poc.ruleengine.service.database.RulesetCachingService;
import com.poc.ruleengine.service.database.RulesetStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieContainer;
import org.kie.dmn.api.core.*;
import org.kie.dmn.api.core.event.AfterEvaluateDecisionTableEvent;
import org.kie.dmn.api.core.event.DMNRuntimeEventListener;
import org.kie.internal.utils.KieHelper;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DecisionRulesetEngineService {

    private final RulesetStorageService storageService;
    private final RulesetCachingService cachingService;

    public DMNEntryRecord loadRulesetByAppCode(String appCode){
        final KieHelper kieHelper = new KieHelper();
        final String xmlContent = storageService.fetchDecisionRules(appCode);
        kieHelper.addContent(xmlContent, ResourceType.DMN);

        final KieContainer kieContainer = kieHelper.getKieContainer();
        final DMNRuntime runtime = kieContainer.newKieSession().getKieRuntime(DMNRuntime.class);
        if (runtime.getModels().isEmpty()) {
           log.error("No DMN models found for {}", appCode);
           throw new RuntimeException("No DMN models found for " + appCode);
        }
        final DMNModel model = runtime.getModels().getFirst();
        final DMNEntryRecord dmnEntryRecord = new DMNEntryRecord(runtime, model);
        cachingService.storeDecisionRules(appCode, dmnEntryRecord);
        return dmnEntryRecord;
    }


    public EvaluationResponse evaluateRequest(EvaluationPayload payload) {
        DMNEntryRecord entry = cachingService.fetchDecisionRules(payload.getApplicationCode());
        if (entry == null) {
            entry = loadRulesetByAppCode(payload.getApplicationCode());
        }
        final Map<String, List<Integer>> ruleMatches = new HashMap<>();
        entry.runtime().addListener(new DMNRuntimeEventListener() {
            @Override
            public void afterEvaluateDecisionTable(AfterEvaluateDecisionTableEvent event) {
                ruleMatches.put(event.getDecisionTableName(), event.getMatches());
            }
        });

        final DMNContext ctx = entry.runtime().newContext();
        ctx.set("evaluatedRequest", payload);
        DMNResult result = entry.runtime().evaluateAll(entry.model(), ctx);

        // collect any evaluation messages
        if (result.hasErrors()) {
            log.warn("DMN evaluation messages for '{}': {}",
                    payload.getApplicationCode(), result.getMessages());
        }

        // flatten decision results into a simple map
        Map<String, Object> decisions = new HashMap<>();
        for (DMNDecisionResult dr : result.getDecisionResults()) {
            final DecisionResult decisionResult = new DecisionResult();
            decisionResult.setDecisionResult(dr.getResult());
            decisionResult.setRules(ruleMatches.get(dr.getDecisionName()));
            decisions.put(dr.getDecisionName(), decisionResult);
        }

        return new EvaluationResponse(payload.getApplicationCode(), decisions);
    }

    public boolean isLoaded(String appCode) {
        return (cachingService.fetchDecisionRules(appCode) != null);
    }
}
