package com.poc.ruleengine.controller.ruleset;

import com.poc.ruleengine.domain.EvaluationRequest;
import com.poc.ruleengine.domain.EvaluationResponse;
import com.poc.ruleengine.service.dmn.DecisionRulesetEngineService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rules/evaluate")
@RequiredArgsConstructor
public class DecisionEvaluationController {

    private final DecisionRulesetEngineService rulesetEngineService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EvaluationResponse> evaluate(@RequestBody EvaluationRequest request) {
        EvaluationResponse response = rulesetEngineService.evaluateRequest(request);
        return ResponseEntity.ok(response);
    }
}
