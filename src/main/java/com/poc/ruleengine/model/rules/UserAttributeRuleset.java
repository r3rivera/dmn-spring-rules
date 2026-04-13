package com.poc.ruleengine.model.rules;

import com.poc.ruleengine.model.Rule;
import lombok.Data;

import java.util.List;

@Data
public class UserAttributeRuleset {
    private List<Rule> rules;
    private String expectedValue;
    private String defaultValue;
}
