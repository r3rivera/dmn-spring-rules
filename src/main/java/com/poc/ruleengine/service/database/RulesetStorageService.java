package com.poc.ruleengine.service.database;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 *  Acts as database store for now
 */
@Slf4j
@Service
public class RulesetStorageService {

    private final ConcurrentMap<String, String> dmnStorageRegistry = new ConcurrentHashMap<>();

    public void storeDecisionRules(String appCode, String  xmlString){
        dmnStorageRegistry.put(appCode, xmlString);
    }

    public String fetchDecisionRules(String appCode){
        return dmnStorageRegistry.get(appCode);
    }

}
