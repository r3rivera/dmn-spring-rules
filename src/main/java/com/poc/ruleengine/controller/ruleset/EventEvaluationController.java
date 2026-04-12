package com.poc.ruleengine.controller.ruleset;


import com.poc.ruleengine.domain.EvaluationRequest;
import com.poc.ruleengine.domain.EvaluationResponse;
import com.poc.ruleengine.service.dmn.DMNKIESessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * <b>POST /api/events/evaluate</b> — evaluates input data against a previously loaded
 * DMN model and returns the decision results.
 */

@RestController
@RequestMapping("/api/events/evaluate")
@RequiredArgsConstructor
public class EventEvaluationController {

    private final DMNKIESessionService kieSessionService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EvaluationResponse> evaluate(@RequestBody EvaluationRequest request) {

        if (!kieSessionService.isLoaded(request.getRulesetName())) {
            return ResponseEntity.badRequest().body(
                    new EvaluationResponse(request.getRulesetName(),
                            java.util.Map.of("error",
                                    "Ruleset '" + request.getRulesetName() + "' is not loaded. "
                                            + "POST /api/rulesets first.")));
        }

        EvaluationResponse response = kieSessionService.evaluate(request);
        return ResponseEntity.ok(response);
    }
}