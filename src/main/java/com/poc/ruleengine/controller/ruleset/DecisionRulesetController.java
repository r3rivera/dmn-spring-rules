package com.poc.ruleengine.controller.ruleset;

import com.poc.ruleengine.model.rules.DecisionRulesetRequest;
import com.poc.ruleengine.service.dmn.DecisionRulesetBuilderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/rules/decision")
@RequiredArgsConstructor
public class DecisionRulesetController {

    private final DecisionRulesetBuilderService builderService;

    /**
     * Build a DMN file from the request, store it, and load it into the KIE session.
     *
     * @return the generated DMN XML for inspection / debugging.
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> createRuleset(@RequestBody DecisionRulesetRequest request) throws Exception {
        final String xml = builderService.buildDecisionRules(request);
        return ResponseEntity.ok(xml);
    }
}
