package com.jaspersoft.jasperserver.api.metadata.user.service.impl;

import com.jaspersoft.jasperserver.api.metadata.user.service.impl.CreateExecutionApplicationEvent.ExecutionType;
import com.jaspersoft.jasperserver.api.common.util.spring.StaticApplicationContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.cache.Cache;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Component
@SessionScope(proxyMode = ScopedProxyMode.TARGET_CLASS)
public class ExecutionsCleanupOnSessionLogout implements ApplicationListener<CreateExecutionApplicationEvent>, Externalizable {
    private static final Logger log = LogManager.getLogger(ExecutionsCleanupOnSessionLogout.class);

    // Run Report Service cache
    @Resource(name = "runReportServiceCacheFactoryBean")
    private Cache sharedReportExecutionsCache;

    // Dashboard caches
    @Resource(name = "dashboardTasks")
    private Cache sharedDashboardTasks;

    @Resource(name = "dashboardResults")
    private Cache sharedDashboardResults;

    @Resource(name = "dashboardProcesses")
    private Cache sharedDashboardProcesses;

    @Resource(name = "dashboardIDToUsers")
    private Cache sharedDashboardIDToUsers;

    private Map<ExecutionType, Set<String>> sessionExecutionsCache;

    @Override
    public void onApplicationEvent(CreateExecutionApplicationEvent event) {
        if (sessionExecutionsCache == null) sessionExecutionsCache = new HashMap<>();

        log.debug("Received {} execution with id {} from source {}",
                event.getType(), event.getExecutionID(), event.getSource());

        getSessionCacheForType(event.getType()).add(event.getExecutionID());

        if (log.isTraceEnabled()) {
            for (ExecutionType type : ExecutionType.values()) {
                Set<String> cache = getSessionCacheForType(type);
                log.trace("Total {} executions for user: {}\nExecution id's: {}", type, cache.size(), cache);
            }
        }
    }

    @PreDestroy
    public void destroy() {
        log.debug("Clean up user executions on session logout");
        if (sessionExecutionsCache == null) return;
        for (ExecutionType type : ExecutionType.values()) {
            log.debug("Clean up {} executions", type);
            Set<String> cache = sessionExecutionsCache.get(type);
            if (cache == null || cache.isEmpty()) {
                log.debug("No executions were found, skipping");
                continue;
            }
            Cache[] sharedCaches = getSharedCachesForType(type);
            for (Cache sharedCache : sharedCaches) {
                // Spring Cache doesn't support removeAll with collection, so we need to use native cache
                Object nativeCache = sharedCache.getNativeCache();
                if (nativeCache instanceof net.sf.ehcache.Ehcache) {
                    net.sf.ehcache.Ehcache ehcache = (net.sf.ehcache.Ehcache) nativeCache;
                    ehcache.removeAll(cache);
                    if (log.isDebugEnabled()) {
                        log.debug("Removed {} {} executions from the {} cache. Total number of executions left: {}",
                                cache.size(), type, sharedCache.getName(), ehcache.getSize());
                    }
                } else {
                    // Fallback: evict one by one
                    for (String executionId : cache) {
                        sharedCache.evict(executionId);
                    }
                    if (log.isDebugEnabled()) {
                        log.debug("Removed {} {} executions from the {} cache",
                                cache.size(), type, sharedCache.getName());
                    }
                }
            }
            cache.clear();
        }
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(sessionExecutionsCache);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        sharedReportExecutionsCache = (Cache) StaticApplicationContext.getApplicationContext()
                .getBean("runReportServiceCacheFactoryBean");
        sessionExecutionsCache = (Map<ExecutionType, Set<String>>) in.readObject();
    }

    private Set<String> getSessionCacheForType(ExecutionType type) {
        return sessionExecutionsCache.computeIfAbsent(type, executionType -> new HashSet<>());
    }

    private Cache[] getSharedCachesForType(ExecutionType type) {
        switch (type) {
            case REPORT:
                return new Cache[]{sharedReportExecutionsCache};
            case DASHBOARD:
                return new Cache[]{sharedDashboardTasks, sharedDashboardResults,
                        sharedDashboardProcesses, sharedDashboardIDToUsers};
            default:
                return new Cache[0];
        }
    }
}
