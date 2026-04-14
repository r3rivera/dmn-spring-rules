package com.poc.ruleengine.service.database;

import com.poc.ruleengine.domain.dmn.DMNEntryRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieContainer;
import org.kie.dmn.api.core.DMNModel;
import org.kie.dmn.api.core.DMNRuntime;
import org.kie.internal.utils.KieHelper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class RulesetCachingService {

    private final StringRedisTemplate redisTemplate;

    @Value("${dmn.cache.key-prefix:dmn:ruleset:}")
    private String keyPrefix;

    @Value("${dmn.cache.ttl-seconds:3600}")
    private long ttlSeconds;

    public void storeDecisionRules(String appCode, String xmlContent) {
        String key = keyPrefix + appCode;
        redisTemplate.opsForValue().set(key, xmlContent, ttlSeconds, TimeUnit.SECONDS);
        log.info("Stored DMN ruleset for '{}' in Valkey cache (TTL={}s)", appCode, ttlSeconds);
    }

    public DMNEntryRecord fetchDecisionRules(String appCode) {
        String key = keyPrefix + appCode;
        String xmlContent = redisTemplate.opsForValue().get(key);
        if (xmlContent == null) {
            log.debug("Cache miss for appCode '{}'", appCode);
            return null;
        }
        log.debug("Cache hit for appCode '{}'", appCode);
        return buildEntryRecord(appCode, xmlContent);
    }

    private DMNEntryRecord buildEntryRecord(String appCode, String xmlContent) {
        KieHelper kieHelper = new KieHelper();
        kieHelper.addContent(xmlContent, ResourceType.DMN);
        KieContainer kieContainer = kieHelper.getKieContainer();
        DMNRuntime runtime = kieContainer.newKieSession().getKieRuntime(DMNRuntime.class);
        if (runtime.getModels().isEmpty()) {
            throw new RuntimeException("No DMN models found for appCode: " + appCode);
        }
        DMNModel model = runtime.getModels().getFirst();
        return new DMNEntryRecord(runtime, model);
    }
}
