package com.poc.ruleengine.service.dmn;

import com.poc.ruleengine.domain.DecisionResult;
import com.poc.ruleengine.domain.EvaluationRequest;
import com.poc.ruleengine.domain.EvaluationResponse;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieModule;
import org.kie.api.builder.Message;
import org.kie.api.runtime.KieContainer;

import org.kie.dmn.api.core.*;
import org.kie.dmn.api.core.event.AfterEvaluateDecisionTableEvent;
import org.kie.dmn.api.core.event.DMNRuntimeEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class DMNKIESessionService {
    private static final Logger log = LoggerFactory.getLogger(DMNKIESessionService.class);

    private record DmnEntry(DMNRuntime runtime, DMNModel model) {}

    /** rulesetName → compiled DMN artefacts */
    private final ConcurrentMap<String, DmnEntry> cache = new ConcurrentHashMap<>();

    // ─── Loading ───────────────────────────────────────────────────────────────

    /**
     * Compile and cache a DMN file for the given ruleset name.
     */
    public void loadDmn(String rulesetName, Path dmnFile) {
        try {
            String xml = Files.readString(dmnFile);

            KieServices ks = KieServices.Factory.get();
            KieFileSystem kfs = ks.newKieFileSystem();
            kfs.write("src/main/resources/" + rulesetName + ".dmn", xml);

            KieBuilder kb = ks.newKieBuilder(kfs).buildAll();
            if (kb.getResults().hasMessages(Message.Level.ERROR)) {
                throw new RuntimeException(
                        "KIE build errors: " + kb.getResults().getMessages(Message.Level.ERROR));
            }

            KieModule km = kb.getKieModule();
            KieContainer kc = ks.newKieContainer(km.getReleaseId());
            DMNRuntime runtime = kc.newKieSession().getKieRuntime(DMNRuntime.class);

            if (runtime.getModels().isEmpty()) {
                throw new RuntimeException("No DMN models found in " + dmnFile);
            }

            DMNModel model = runtime.getModels().get(0);
            cache.put(rulesetName, new DmnEntry(runtime, model));

            log.info("DMN model '{}' loaded – namespace={}, decisions={}",
                    rulesetName, model.getNamespace(), model.getDecisions().size());
        } catch (Exception e) {
            throw new RuntimeException("Failed to load DMN for " + rulesetName, e);
        }
    }

    // ─── Evaluation ────────────────────────────────────────────────────────────

    /**
     * Evaluate a request against a previously loaded DMN model.
     *
     * @return an {@link EvaluationResponse} containing every decision result.
     */
    public EvaluationResponse evaluate(EvaluationRequest request) {

        final DmnEntry entry = cache.get(request.getRulesetName());
        if (entry == null) {
            throw new IllegalArgumentException(
                    "No DMN model loaded for ruleset: " + request.getRulesetName());
        }

        final Map<String, List<Integer>> ruleMatches = new HashMap<>();
        entry.runtime.addListener(new DMNRuntimeEventListener() {
            @Override
            public void afterEvaluateDecisionTable(AfterEvaluateDecisionTableEvent event) {
                ruleMatches.put(event.getDecisionTableName(), event.getMatches());
            }
        });

        final DMNContext ctx = entry.runtime.newContext();
        ctx.set("eventRequest", request.getRequestContext());
        DMNResult result = entry.runtime.evaluateAll(entry.model, ctx);

        // collect any evaluation messages
        if (result.hasErrors()) {
            log.warn("DMN evaluation messages for '{}': {}",
                    request.getRulesetName(), result.getMessages());
        }

        // flatten decision results into a simple map
        Map<String, Object> decisions = new HashMap<>();
        for (DMNDecisionResult dr : result.getDecisionResults()) {
            final DecisionResult decisionResult = new DecisionResult();
            decisionResult.setDecisionResult(dr.getResult());
            decisionResult.setRules(ruleMatches.get(dr.getDecisionName()));
            decisions.put(dr.getDecisionName(), decisionResult);
        }

        return new EvaluationResponse(request.getRulesetName(), decisions);
    }

    /**
     * Check whether a ruleset has been loaded.
     */
    public boolean isLoaded(String rulesetName) {
        return cache.containsKey(rulesetName);
    }


}
