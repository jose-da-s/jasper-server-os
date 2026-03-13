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
package com.jaspersoft.jasperserver.api.common.util;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.cache.Cache;
import javax.cache.CacheManager;

import java.util.Arrays;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link CacheManagerValidator}.
 *
 * @author Jasper Server OS Authors
 */
@RunWith(MockitoJUnitRunner.class)
public class CacheManagerValidatorTest {

    @Mock
    private CacheManager cacheManager;

    @Mock
    private Cache<Object, Object> mockCache;

    private CacheManagerValidator validator;

    @Before
    public void setUp() {
        validator = new CacheManagerValidator();
        validator.setCacheManager(cacheManager);
    }

    @Test
    public void validateCaches_allCachesExist_noException() {
        // Given
        validator.setRequiredCacheNames(Arrays.asList("cache1", "cache2", "cache3"));
        when(cacheManager.getCache("cache1")).thenReturn(mockCache);
        when(cacheManager.getCache("cache2")).thenReturn(mockCache);
        when(cacheManager.getCache("cache3")).thenReturn(mockCache);

        // When/Then - should not throw
        validator.validateCaches();

        // Verify all caches were checked
        verify(cacheManager).getCache("cache1");
        verify(cacheManager).getCache("cache2");
        verify(cacheManager).getCache("cache3");
    }

    @Test(expected = IllegalStateException.class)
    public void validateCaches_missingCache_throwsException() {
        // Given
        validator.setRequiredCacheNames(Arrays.asList("cache1", "missingCache", "cache3"));
        when(cacheManager.getCache("cache1")).thenReturn(mockCache);
        when(cacheManager.getCache("missingCache")).thenReturn(null);
        when(cacheManager.getCache("cache3")).thenReturn(mockCache);

        // When - should throw IllegalStateException
        validator.validateCaches();
    }

    @Test
    public void validateCaches_missingCacheWithFailOnMissingCacheFalse_noException() {
        // Given
        validator.setRequiredCacheNames(Arrays.asList("cache1", "missingCache"));
        validator.setFailOnMissingCache(false);
        when(cacheManager.getCache("cache1")).thenReturn(mockCache);
        when(cacheManager.getCache("missingCache")).thenReturn(null);

        // When/Then - should NOT throw, only warn
        validator.validateCaches();
    }

    @Test(expected = IllegalStateException.class)
    public void validateCaches_nullCacheManager_throwsException() {
        // Given
        validator.setCacheManager(null);
        validator.setFailOnMissingCache(true);

        // When - should throw IllegalStateException
        validator.validateCaches();
    }

    @Test
    public void validateCaches_nullCacheManagerWithFailOnMissingCacheFalse_noException() {
        // Given
        validator.setCacheManager(null);
        validator.setFailOnMissingCache(false);

        // When/Then - should NOT throw, only warn
        validator.validateCaches();
    }

    @Test
    public void validateCaches_multipleMissingCaches_exceptionContainsAllNames() {
        // Given
        validator.setRequiredCacheNames(Arrays.asList("missing1", "missing2", "existing"));
        when(cacheManager.getCache("missing1")).thenReturn(null);
        when(cacheManager.getCache("missing2")).thenReturn(null);
        when(cacheManager.getCache("existing")).thenReturn(mockCache);

        // When
        IllegalStateException exception = null;
        try {
            validator.validateCaches();
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            exception = e;
        }

        // Then - exception message should contain both missing cache names
        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("missing1"));
        assertTrue(exception.getMessage().contains("missing2"));
        assertTrue(exception.getMessage().contains("2")); // count of missing caches
    }

    @Test
    public void validateCaches_emptyRequiredList_noException() {
        // Given
        validator.setRequiredCacheNames(Arrays.asList());

        // When/Then - should not throw
        validator.validateCaches();
    }

    @Test
    public void defaultRequiredCaches_containsEssentialCaches() {
        // Given - new validator with defaults
        CacheManagerValidator defaultValidator = new CacheManagerValidator();

        // Then - should have default required caches
        assertNotNull(defaultValidator.getRequiredCacheNames());
        assertTrue(defaultValidator.getRequiredCacheNames().contains("aclCache"));
        assertTrue(defaultValidator.getRequiredCacheNames().contains("inputControlCache"));
        assertTrue(defaultValidator.getRequiredCacheNames().contains("systemUserStorageCache"));
    }

    @Test
    public void gettersAndSetters_workCorrectly() {
        // Given
        CacheManagerValidator v = new CacheManagerValidator();

        // When
        v.setCacheManager(cacheManager);
        v.setRequiredCacheNames(Arrays.asList("test1", "test2"));
        v.setFailOnMissingCache(false);

        // Then
        assertEquals(cacheManager, v.getCacheManager());
        assertEquals(2, v.getRequiredCacheNames().size());
        assertTrue(v.getRequiredCacheNames().contains("test1"));
        assertFalse(v.isFailOnMissingCache());
    }
}