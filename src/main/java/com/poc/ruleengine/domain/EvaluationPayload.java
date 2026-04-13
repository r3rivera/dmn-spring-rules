package com.poc.ruleengine.domain;

import com.poc.ruleengine.domain.client.ClientAttribute;
import lombok.Data;

/**
 POJO that represent thet request payload that will
 be evaluation by the rule engine
 */
@Data
public class EvaluationPayload {
    private String applicationCode;
    private String eventName;
    private ClientAttribute evaluatedUser;
}
