package com.poc.ruleengine.service;

import com.poc.ruleengine.model.Rule;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Sorts rules by ascending priority so that lower priority numbers fire first
 * (priority 1 = highest precedence). The ordered list drives the row order in
 * the generated DMN decision table, which uses hitPolicy="FIRST".
 */
@Service
public class RuleOrderingService {

    public List<Rule> orderByPriority(List<Rule> rules) {
        return rules.stream()
                .sorted(Comparator.comparingInt(Rule::getPriority))
                .collect(Collectors.toList());
    }
}
