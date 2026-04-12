package com.poc.ruleengine.model;

import lombok.Data;

@Data
public class EventRequest {
    private String eventName;
    private String applicationCode;
    private UserProfile userProfile;
}
