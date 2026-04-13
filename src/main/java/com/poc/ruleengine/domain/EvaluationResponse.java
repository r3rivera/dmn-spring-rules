package com.poc.ruleengine.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Setter
@Getter
public class EvaluationResponse {

    private String rulesetName;
    private Map<String, Object> results;   // decision-name → result value
    public EvaluationResponse(String rulesetName, Map<String, Object> results) {
        this.rulesetName = rulesetName;
        this.results = results;
    }

}
