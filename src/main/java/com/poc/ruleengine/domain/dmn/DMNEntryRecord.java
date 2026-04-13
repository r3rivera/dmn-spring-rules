package com.poc.ruleengine.domain.dmn;

import org.kie.dmn.api.core.DMNModel;
import org.kie.dmn.api.core.DMNRuntime;
public record DMNEntryRecord(DMNRuntime runtime, DMNModel model){}
