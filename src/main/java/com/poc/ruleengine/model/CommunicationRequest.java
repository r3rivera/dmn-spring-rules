package com.poc.ruleengine.model;

import lombok.Data;

import java.util.Map;

@Data
public class CommunicationRequest {
    private String eventName;
    private Map<String, Object> clientInfo;
    private ChannelDetail channelData;
}
