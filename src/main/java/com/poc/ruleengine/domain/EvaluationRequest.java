package com.poc.ruleengine.domain;

import com.poc.ruleengine.model.EventRequest;
import lombok.Data;

/**
 * Request body sent to the evaluation endpoint.
 * The context map is injected as DMN input data (keys = input data names).
 */
@Data
@Deprecated
public class EvaluationRequest {
    private String rulesetName;
    // identifies which DMN to evaluate
    private EventRequest requestContext;          // input data fed into the DMN
}
