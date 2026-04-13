package com.poc.ruleengine.model;

import lombok.Data;

@Data
public class CommunicationRequest {
    private String eventName;
    private UserEventInfoRequest clientInfo;
    private ChannelDetail channelData;
}
