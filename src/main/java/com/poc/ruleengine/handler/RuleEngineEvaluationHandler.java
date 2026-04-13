package com.poc.ruleengine.handler;

import com.poc.ruleengine.domain.EvaluationPayload;
import com.poc.ruleengine.domain.EvaluationResponse;
import com.poc.ruleengine.domain.client.ClientAttribute;
import com.poc.ruleengine.model.CommunicationRequest;
import com.poc.ruleengine.model.UserEventInfoRequest;
import com.poc.ruleengine.service.dmn.DecisionRulesetEngineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RuleEngineEvaluationHandler {

    private final DecisionRulesetEngineService rulesetEngineService;


    public EvaluationResponse processAndEvaluate(String appCode, CommunicationRequest request){
        final EvaluationPayload evaluationPayload = new EvaluationPayload();
        evaluationPayload.setApplicationCode(appCode);
        evaluationPayload.setEventName(request.getEventName());

        final UserEventInfoRequest userInfo = request.getClientInfo();

        final ClientAttribute clientAttribute = new ClientAttribute();
        clientAttribute.setClientId(userInfo.getClientId());
        clientAttribute.setName(userInfo.getName());
        clientAttribute.setClientType(userInfo.getClientType());
        evaluationPayload.setEvaluatedUser(clientAttribute);
        return rulesetEngineService.evaluateRequest(evaluationPayload);

    }
}
