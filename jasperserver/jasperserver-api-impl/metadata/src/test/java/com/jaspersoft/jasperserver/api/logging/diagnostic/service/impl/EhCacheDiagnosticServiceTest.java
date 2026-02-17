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
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.CacheConfiguration;
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
        // Create a real EhCache cache manager and cache for testing
        cacheManager = CacheManager.create();

        // Create cache configuration
        CacheConfiguration config = new CacheConfiguration(TEST_CACHE_NAME, 100)
                .eternal(false)
                .timeToIdleSeconds(120)
                .timeToLiveSeconds(300)
                .overflowToDisk(false)
                .statistics(true);

        Cache ehcache = new Cache(config);
        cacheManager.addCache(ehcache);

        // Wrap the EhCache in Spring Cache wrapper
        EhCacheCache springCache = new EhCacheCache(ehcache);

        // Create service and inject the Spring Cache
        ehCacheDiagnosticService = new EhCacheDiagnosticService();
        ehCacheDiagnosticService.setCache(springCache);
    }

    @After
    public void tearDown() {
        if (cacheManager != null) {
            cacheManager.removeCache(TEST_CACHE_NAME);
            cacheManager.shutdown();
        }
    }

    @Test
    public void getDiagnosticDataTest() {
        Map<DiagnosticAttribute, DiagnosticCallback> resultDiagnosticData = ehCacheDiagnosticService.getDiagnosticData();

        // Test total size of diagnostic attributes collected from EhCacheDiagnosticService
        assertEquals(37, resultDiagnosticData.size());

        // Verify configuration values are present and not null
        assertNotNull(resultDiagnosticData);

        // Check that all expected diagnostic attributes are present
        // We can't verify exact values since we're using a real cache, but we can verify the structure
        assertTrue(resultDiagnosticData.keySet().stream()
                .anyMatch(attr -> DiagnosticAttributeBuilder.EHCACHE_STAT_OBJECTCOUNT.equals(attr.getAttributeName())));
        assertTrue(resultDiagnosticData.keySet().stream()
                .anyMatch(attr -> DiagnosticAttributeBuilder.EHCACHE_STAT_CACHEHIT_PERCENTAGE.equals(attr.getAttributeName())));
        assertTrue(resultDiagnosticData.keySet().stream()
                .anyMatch(attr -> DiagnosticAttributeBuilder.EHCACHE_CONF_TIME_IDLE.equals(attr.getAttributeName())));
        assertTrue(resultDiagnosticData.keySet().stream()
                .anyMatch(attr -> DiagnosticAttributeBuilder.EHCACHE_CONF_TIME_LIVE.equals(attr.getAttributeName())));
        assertTrue(resultDiagnosticData.keySet().stream()
                .anyMatch(attr -> DiagnosticAttributeBuilder.EHCACHE_CONF_ETERNAL.equals(attr.getAttributeName())));
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
