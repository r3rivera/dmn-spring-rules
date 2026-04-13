package com.poc.ruleengine.model;

import lombok.Data;

@Data
public class Rule {
    private String fieldName;
    private String fieldType;
    private String condition;
    private String expectedValue;
}
