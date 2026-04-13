package com.poc.ruleengine.model.rules;

import lombok.Data;

@Data
public class DecisionRulesetRequest {

    private String applicationCode;
    private String eventName;

    private UserAttributeRuleset userInfoRules;

    private String expectOutcome;
    private String defaultOutcome;
}
