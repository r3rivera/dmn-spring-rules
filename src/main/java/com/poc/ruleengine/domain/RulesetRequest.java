package com.poc.ruleengine.domain;

import lombok.Data;

import java.util.List;

/**
 * Top-level request to build a complete DMN ruleset.
 */
@Data
public class RulesetRequest {

    private String rulesetName;
    private String namespace;
    private List<InputColumnDef> inputColumns;
    private List<OutputColumnDef> outputColumns;
    private String hitPolicy;            // UNIQUE, FIRST, COLLECT, etc.
    private List<DecisionTableDef> decisionTables;

    // --- nested types ---
    @Data
    public static class InputColumnDef {
        private String name;
        private String label;
        private String typeRef;          // string, number, boolean
        private String expression;       // FEEL expression, e.g. "eventRequest.applicationCode"

    }

    @Data
    public static class OutputColumnDef {
        private String name;
        private String label;
        private String typeRef;

    }

    @Data
    public static class DecisionTableDef {
        private String decisionName;
        private String hitPolicy;
        private List<InputColumnDef> inputs;
        private List<OutputColumnDef> outputs;
        private List<RuleDef> rules;
        private List<String> requiredInputs;      // IDs of inputData nodes
        private List<String> requiredDecisions;    // IDs of upstream decisions

    }

    @Data
    public static class RuleDef {
        private String description;
        private List<String> inputEntries;   // FEEL literal for each input column ("-" = any)
        private List<String> outputEntries;  // FEEL literal for each output column

    }

    // --- top-level getters / setters ---

}
