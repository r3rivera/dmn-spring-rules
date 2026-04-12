package com.poc.ruleengine.model;

import lombok.Data;

@Data
public class Rule {
    private String name;
    private String condition;
    private String expectedValue;
}
