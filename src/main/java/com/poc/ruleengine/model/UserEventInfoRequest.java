package com.poc.ruleengine.model;

import lombok.Data;

@Data
public class UserEventInfoRequest {
    private String clientId;
    private String name;
    private boolean brokerageAccount;
    private String clientType;
}
