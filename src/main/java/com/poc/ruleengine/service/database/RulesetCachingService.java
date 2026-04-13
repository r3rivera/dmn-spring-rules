package com.poc.ruleengine.service.database;

import com.poc.ruleengine.domain.dmn.DMNEntryRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@Service
public class RulesetCachingService {

    private final ConcurrentMap<String, DMNEntryRecord> dmnCacheRegistry = new ConcurrentHashMap<>();

    public void storeDecisionRules(String appCode, DMNEntryRecord entry){
        dmnCacheRegistry.put(appCode, entry);
    }

    public DMNEntryRecord fetchDecisionRules(String appCode){
        return dmnCacheRegistry.get(appCode);
    }
}
