package com.poc.ruleengine.controller.ruleset;

import com.poc.ruleengine.domain.EvaluationResponse;
import com.poc.ruleengine.handler.RuleEngineEvaluationHandler;
import com.poc.ruleengine.model.CommunicationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/events/evaluate")
@RequiredArgsConstructor
public class DecisionEvaluationController {

    private final RuleEngineEvaluationHandler ruleEngineEvaluationHandler;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EvaluationResponse> evaluate(@RequestHeader("appCode") String appCode,
            @RequestBody CommunicationRequest request) {

            final EvaluationResponse response = ruleEngineEvaluationHandler.processAndEvaluate(appCode, request);
            return ResponseEntity.ok(response);

    }
}
