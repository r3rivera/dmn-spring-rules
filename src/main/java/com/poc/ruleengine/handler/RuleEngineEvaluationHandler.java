package com.poc.ruleengine.handler;

import com.poc.ruleengine.domain.EvaluationPayload;
import com.poc.ruleengine.domain.EvaluationResponse;
import com.poc.ruleengine.model.CommunicationRequest;
import com.poc.ruleengine.service.dmn.DecisionRulesetEngineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RuleEngineEvaluationHandler {

    private final DecisionRulesetEngineService rulesetEngineService;

    public EvaluationResponse processAndEvaluate(String appCode, CommunicationRequest request) {
        final EvaluationPayload evaluationPayload = new EvaluationPayload();
        evaluationPayload.setApplicationCode(appCode);
        evaluationPayload.setEventName(request.getEventName());
        evaluationPayload.setEvaluatedUser(request.getClientInfo());
        return rulesetEngineService.evaluateRequest(evaluationPayload);

    }
}
