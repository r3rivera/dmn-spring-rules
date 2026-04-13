package com.poc.ruleengine.domain;

import lombok.Data;

import java.util.Map;

/**
 POJO that represent thet request payload that will
 be evaluation by the rule engine
 */
@Data
public class EvaluationPayload {
    private String applicationCode;
    private String eventName;
    private Map<String, Object> evaluatedUser;
}
