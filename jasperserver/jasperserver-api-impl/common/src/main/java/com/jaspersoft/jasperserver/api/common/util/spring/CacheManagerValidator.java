/*
 * Copyright (C) 2025-2026 the Jasper Server OS Authors
 * SPDX-License-Identifier: AGPL-3.0-or-later
 * Copyright (C) 2005-2023. Cloud Software Group, Inc. All Rights Reserved.
 * http://www.jaspersoft.com.
 *
 * Unless you have purchased a commercial license agreement from Jaspersoft,
 * the following license terms apply:
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.jaspersoft.jasperserver.api.common.util.spring;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.cache.CacheManager;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Validates that all required caches are properly configured in the Spring CacheManager.
 * <p>
 * This validator runs during application startup to fail-fast if any required cache
 * is missing from the configuration (ehcache.xml). This prevents cryptic runtime errors
 * when attempting to use unconfigured caches.
 * </p>
 * <p>
 * The validator can be configured with:
 * <ul>
 *   <li>{@code requiredCacheNames} - list of cache names that must exist</li>
 *   <li>{@code failOnMissingCache} - whether to throw exception or just log warning (default: true)</li>
 * </ul>
 * </p>
 *
 * @author Jasper Server OS Authors
 */
public class CacheManagerValidator {

    private static final Logger log = LogManager.getLogger(CacheManagerValidator.class);

    private CacheManager cacheManager;
    private Collection<String> requiredCacheNames;
    private boolean failOnMissingCache = true;

    /**
     * Default required cache names for the main cache manager.
     * These caches are essential for JasperServer operation.
     */
    private static final String[] DEFAULT_REQUIRED_CACHES = {
            "aclCache",
            "attributeCache",
            "inputControlCache",
            "hibernate_repository_ehcache",
            "systemUserStorageCache",
            "RRSCache",
            "connection.descriptions",
            "teiidResultsetEhCache",
            "teiidResultsetReplEhCache",
            "teiidPreparedPlanEhCache",
            "dashboardTasks",
            "dashboardResults",
            "dashboardProcesses",
            "dashboardIDToUsers"
    };

    public CacheManagerValidator() {
        this.requiredCacheNames = Arrays.asList(DEFAULT_REQUIRED_CACHES);
    }

    /**
     * Validates that all required caches exist in the CacheManager.
     * This method is called automatically after bean initialization.
     *
     * @throws IllegalStateException if any required cache is missing and failOnMissingCache is true
     */
    @PostConstruct
    public void validateCaches() {
        if (!validateCacheManagerPresent()) {
            return;
        }

        log.info("Validating Spring Cache configuration...");

        List<String> missingCaches = findMissingCaches();

        if (!missingCaches.isEmpty()) {
            handleMissingCaches(missingCaches);
        } else {
            log.info("Cache configuration validated successfully. {} cache(s) verified.",
                    requiredCacheNames.size());
        }
    }

    private boolean validateCacheManagerPresent() {
        if (cacheManager == null) {
            String message = "CacheManager is not configured. Cannot validate cache configuration.";
            if (failOnMissingCache) {
                throw new IllegalStateException(message);
            } else {
                log.warn(message);
                return false;
            }
        }
        return true;
    }

    private List<String> findMissingCaches() {
        List<String> missingCaches = new ArrayList<>();
        for (String cacheName : requiredCacheNames) {
            if (cacheManager.getCache(cacheName) == null) {
                missingCaches.add(cacheName);
                log.error("Required cache not found: '{}'", cacheName);
            } else if (log.isDebugEnabled()) {
                log.debug("Cache validated: '{}'", cacheName);
            }
        }
        return missingCaches;
    }

    private void handleMissingCaches(List<String> missingCaches) {
        String message = String.format(
                "Cache configuration validation failed. Missing %d required cache(s): %s. " +
                "Please ensure these caches are defined in ehcache.xml.",
                missingCaches.size(),
                missingCaches
        );

        if (failOnMissingCache) {
            throw new IllegalStateException(message);
        } else {
            log.warn(message);
        }
    }

    /**
     * Sets the Spring CacheManager to validate.
     *
     * @param cacheManager the cache manager (typically springCacheManager)
     */
    public void setCacheManager(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    /**
     * Sets the list of required cache names to validate.
     * If not set, uses the default list of essential JasperServer caches.
     *
     * @param requiredCacheNames collection of cache names that must exist
     */
    public void setRequiredCacheNames(Collection<String> requiredCacheNames) {
        this.requiredCacheNames = requiredCacheNames;
    }

    /**
     * Sets whether to throw an exception when a required cache is missing.
     * If false, only logs a warning.
     *
     * @param failOnMissingCache true to fail fast (default), false to just warn
     */
    public void setFailOnMissingCache(boolean failOnMissingCache) {
        this.failOnMissingCache = failOnMissingCache;
    }

    public CacheManager getCacheManager() {
        return cacheManager;
    }

    public Collection<String> getRequiredCacheNames() {
        return requiredCacheNames;
    }

    public boolean isFailOnMissingCache() {
        return failOnMissingCache;
    }
}