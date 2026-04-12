package com.poc.ruleengine.domain;

import lombok.Data;

import java.util.List;

@Data
public class DecisionResult {
    private Object decisionResult;
    private List<Integer> rules;
}
