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

package com.jaspersoft.jasperserver.api.logging.diagnostic.service.impl;

import com.jaspersoft.jasperserver.api.logging.diagnostic.domain.DiagnosticAttribute;
import com.jaspersoft.jasperserver.api.logging.diagnostic.helper.DiagnosticAttributeBuilder;
import com.jaspersoft.jasperserver.api.logging.diagnostic.service.DiagnosticCallback;
import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.Configuration;
import javax.cache.spi.CachingProvider;

import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.jsr107.Eh107Configuration;
import org.ehcache.jsr107.EhcacheCachingProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.cache.ehcache.EhCacheCache;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


/**
 * Tests for {@link EhCacheDiagnosticService}
 * <p>
 * This test uses a real EhCache instance to test the diagnostic service,
 * as the service creates CacheStatistics internally which cannot be mocked.
 *
 * @author vsabadosh
 */
public class EhCacheDiagnosticServiceTest {

    private CacheManager cacheManager;
    private EhCacheDiagnosticService ehCacheDiagnosticService;
    private static final String TEST_CACHE_NAME = "testDiagnosticCache";

    @Before
    public void setUp() {
    	CacheManager cacheManager = Caching.getCachingProvider(EhcacheCachingProvider.class.getName()).getCacheManager();

    	Configuration<String, String> jcacheConfig = Eh107Configuration.fromEhcacheCacheConfiguration(CacheConfigurationBuilder.newCacheConfigurationBuilder(
    	            String.class, // Key type
    	            String.class, // Value type
    	            ResourcePoolsBuilder.heap(100) // Heap size
    	        ));

    	Cache<String, String> testCache = cacheManager.createCache(TEST_CACHE_NAME, jcacheConfig);
    	Cache<String, String> ehcache = cacheManager.getCache(TEST_CACHE_NAME, String.class, String.class);

        ehCacheDiagnosticService.setCache(new org.springframework.cache.jcache.JCacheCache((Cache<Object,Object>) (Object) testCache));
    }

    @After
    public void tearDown() {
        if (cacheManager != null) {
            cacheManager.destroyCache(TEST_CACHE_NAME);
            cacheManager.close();
        }
    }

    @Test
    public void getDiagnosticDataTest() {
        Map<DiagnosticAttribute, DiagnosticCallback> resultDiagnosticData = ehCacheDiagnosticService.getDiagnosticData();

        // Test total size of diagnostic attributes collected from EhCacheDiagnosticService
        assertEquals(25, resultDiagnosticData.size());

        // Verify configuration values are present and not null
        assertNotNull(resultDiagnosticData);

        // Check that all expected diagnostic attributes are present
        // We can't verify exact values since we're using a real cache, but we can verify the structure
        assertTrue(resultDiagnosticData.keySet().stream()
                .anyMatch(attr -> DiagnosticAttributeBuilder.JCACHE_STAT_CACHEHITPERCENTAGE.equals(attr.getAttributeName())));
        assertTrue(resultDiagnosticData.keySet().stream()
                .anyMatch(attr -> DiagnosticAttributeBuilder.JCACHE_CONF_STOREBYVALUE.equals(attr.getAttributeName())));
        assertTrue(resultDiagnosticData.keySet().stream()
                .anyMatch(attr -> DiagnosticAttributeBuilder.EHCACHE_CONF_HEAPSIZE.equals(attr.getAttributeName())));
    }

    @Test
    public void getDiagnosticData_nullCache_returnsEmptyMap() {
        EhCacheDiagnosticService service = new EhCacheDiagnosticService();
        // Don't set a cache - it should be null

        Map<DiagnosticAttribute, DiagnosticCallback> result = service.getDiagnosticData();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
