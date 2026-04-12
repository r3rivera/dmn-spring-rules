package com.poc.ruleengine.model.rules;

import com.poc.ruleengine.model.Rule;
import lombok.Data;

import java.util.List;

@Data
public class DecisionRulesetRequest {

    private String applicationCode;
    private String eventName;

    private List<Rule> ruleList;
    private String expectOutcome;
    private String defaultOutcome;
}
