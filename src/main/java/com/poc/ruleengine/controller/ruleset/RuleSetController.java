package com.poc.ruleengine.controller.ruleset;


import com.poc.ruleengine.domain.RulesetRequest;
import com.poc.ruleengine.service.dmn.DMNBuilderService;
import com.poc.ruleengine.service.dmn.DMNKIESessionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.util.Map;

/**
 * <b>POST /api/rulesets</b> — accepts a {@link RulesetRequest}, generates the
 * DMN XML, persists it, and loads it into the KIE runtime so it is immediately
 * available for evaluation.
 */
@Slf4j
@RestController
@RequestMapping("/api/rulesets")
public class RuleSetController {

    private final DMNBuilderService builderService;
    private final DMNKIESessionService kieSessionService;

    public RuleSetController(DMNBuilderService builderService,
                             DMNKIESessionService kieSessionService) {
        this.builderService = builderService;
        this.kieSessionService = kieSessionService;
    }

    /**
     * Build a DMN file from the request, store it, and load it into the KIE session.
     *
     * @return the generated DMN XML for inspection / debugging.
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> createRuleset(@RequestBody RulesetRequest request) {

        String xml = builderService.buildAndStore(request);

        Path dmnPath = builderService.getPath(request.getRulesetName());
        kieSessionService.loadDmn(request.getRulesetName(), dmnPath);

        return ResponseEntity.ok(xml);
    }

    /**
     * List all registered rulesets.
     */
    @GetMapping
    public ResponseEntity<Map<String, String>> listRulesets() {
        var map = new java.util.LinkedHashMap<String, String>();
        builderService.getRegistry().forEach((name, path) ->
                map.put(name, path.toAbsolutePath().toString()));
        return ResponseEntity.ok(map);
    }
}
