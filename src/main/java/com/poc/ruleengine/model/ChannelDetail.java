package com.poc.ruleengine.model;

import lombok.Data;

import java.util.Map;

@Data
public class ChannelDetail {
    private String template;
    private Map<String, String> values;
}
