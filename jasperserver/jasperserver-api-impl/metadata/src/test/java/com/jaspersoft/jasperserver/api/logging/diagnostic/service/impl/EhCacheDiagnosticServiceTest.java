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
import com.jaspersoft.jasperserver.api.logging.diagnostic.domain.DiagnosticAttributeImpl;
import com.jaspersoft.jasperserver.api.logging.diagnostic.helper.DiagnosticAttributeBuilder;
import com.jaspersoft.jasperserver.api.logging.diagnostic.service.DiagnosticCallback;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.cache.Cache;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;


/**
 * Tests for {@link EhCacheDiagnosticService}
 *
 * @author vsabadosh
 */
@RunWith(MockitoJUnitRunner.class)
public class EhCacheDiagnosticServiceTest {
    @Mock
    private Cache springCache;

    private EhCacheDiagnosticService ehCacheDiagnosticService;

    private Long objectCount = 6L;
    private Double cacheHitPercentage = 7.0;
    private Long cacheHits = 8L;
    private Double cacheMissPercentage = 9.0;
    private Long cacheMisses = 10L;
    private Long diskStoreObjectCount = 11L;
    private Double onDiskHitPercentage = 12.0;
    private Long onDiskHits = 13L;
    private Long onDiskMisses = 14L;
    private Long memoryStoreObjectCount = 15L;
    private Double inMemoryHitPercentage = 16.0;
    private Long inMemoryHits = 17L;
    private Long inMemoryMisses = 18L;
    private Long offHeapStoreObjectCount = 19L;
    private Double OffHeapHitPercentage = 20.0;
    private Long OffHeapHits = 21L;
    private Long offHeapMisses = 22L;
    private Integer writeMaxQueueSize = 23;
    private Long writeQueueLength = 24L;

    private Integer conf_DiskSpoolBufferSizeMB = 25;
    private Long conf_DiskExpiryThreadIntervalSeconds = 26L;
    private Boolean conf_LoggingEnabled = false;
    private Long conf_MaxBytesLocalDisk = 27L;
    private Long conf_MaxBytesLocalHeap = 28L;
    private Long conf_MaxBytesLocalOffHeap = 29L;
    private Integer conf_MaxElementsOnDisk = 30;
    private Long conf_TimeToIdleSeconds = 32L;
    private Long conf_TimeToLiveSeconds = 33L;
    private Long conf_MaxEntriesLocalDisk = 34L;
    private Long conf_MaxEntriesLocalHeap = 35L;
    private Boolean diskPersistent = true;
    private Boolean eternal = false;
    private Boolean overflowToDisk = true;
    private Boolean overflowToOffHeap = false;

    @Before
    public void setUp() {
        // Create mock EhCache configuration
        net.sf.ehcache.config.CacheConfiguration cacheConfig = Mockito.mock(net.sf.ehcache.config.CacheConfiguration.class);

        when(cacheConfig.getStatistics()).thenReturn(true);
        when(cacheConfig.getDiskSpoolBufferSizeMB()).thenReturn(conf_DiskSpoolBufferSizeMB);
        when(cacheConfig.getDiskExpiryThreadIntervalSeconds()).thenReturn(conf_DiskExpiryThreadIntervalSeconds);
        when(cacheConfig.getLogging()).thenReturn(conf_LoggingEnabled);
        when(cacheConfig.getMaxBytesLocalDisk()).thenReturn(conf_MaxBytesLocalDisk);
        when(cacheConfig.getMaxBytesLocalHeap()).thenReturn(conf_MaxBytesLocalHeap);
        when(cacheConfig.getMaxBytesLocalOffHeap()).thenReturn(conf_MaxBytesLocalOffHeap);
        when(cacheConfig.getMaxElementsOnDisk()).thenReturn(conf_MaxElementsOnDisk);
        when(cacheConfig.getMaxEntriesLocalHeap()).thenReturn(conf_MaxEntriesLocalHeap);
        when(cacheConfig.getTimeToIdleSeconds()).thenReturn(conf_TimeToIdleSeconds);
        when(cacheConfig.getTimeToLiveSeconds()).thenReturn(conf_TimeToLiveSeconds);
        when(cacheConfig.getMaxEntriesLocalDisk()).thenReturn(conf_MaxEntriesLocalDisk);
        when(cacheConfig.getMaxEntriesLocalHeap()).thenReturn(conf_MaxEntriesLocalHeap);
        when(cacheConfig.isDiskPersistent()).thenReturn(diskPersistent);
        when(cacheConfig.isEternal()).thenReturn(eternal);
        when(cacheConfig.isOverflowToDisk()).thenReturn(overflowToDisk);
        when(cacheConfig.isOverflowToOffHeap()).thenReturn(overflowToOffHeap);
        when(cacheConfig.getMemoryStoreEvictionPolicy()).thenReturn(net.sf.ehcache.store.MemoryStoreEvictionPolicy.LRU);

        // Create mock native EhCache
        net.sf.ehcache.Ehcache ehCache = Mockito.mock(net.sf.ehcache.Ehcache.class);
        when(ehCache.getCacheConfiguration()).thenReturn(cacheConfig);
        when(ehCache.getSize()).thenReturn(objectCount.intValue());

        // Mock statistics that will be created inside getDiagnosticData
        net.sf.ehcache.management.CacheStatistics mockStats = Mockito.mock(net.sf.ehcache.management.CacheStatistics.class);
        when(mockStats.getObjectCount()).thenReturn(objectCount);
        when(mockStats.getCacheHitPercentage()).thenReturn(cacheHitPercentage);
        when(mockStats.getCacheHits()).thenReturn(cacheHits);
        when(mockStats.getCacheMissPercentage()).thenReturn(cacheMissPercentage);
        when(mockStats.getCacheMisses()).thenReturn(cacheMisses);
        when(mockStats.getDiskStoreObjectCount()).thenReturn(diskStoreObjectCount);
        when(mockStats.getOnDiskHitPercentage()).thenReturn(onDiskHitPercentage);
        when(mockStats.getOnDiskHits()).thenReturn(onDiskHits);
        when(mockStats.getOnDiskMisses()).thenReturn(onDiskMisses);
        when(mockStats.getMemoryStoreObjectCount()).thenReturn(memoryStoreObjectCount);
        when(mockStats.getInMemoryHitPercentage()).thenReturn(inMemoryHitPercentage);
        when(mockStats.getInMemoryHits()).thenReturn(inMemoryHits);
        when(mockStats.getInMemoryMisses()).thenReturn(inMemoryMisses);
        when(mockStats.getOffHeapStoreObjectCount()).thenReturn(offHeapStoreObjectCount);
        when(mockStats.getOffHeapHitPercentage()).thenReturn(OffHeapHitPercentage);
        when(mockStats.getOffHeapHits()).thenReturn(OffHeapHits);
        when(mockStats.getOffHeapMisses()).thenReturn(offHeapMisses);
        when(mockStats.getWriterMaxQueueSize()).thenReturn(writeMaxQueueSize);
        when(mockStats.getWriterQueueLength()).thenReturn(writeQueueLength);

        // Mock Spring Cache to return native EhCache
        when(springCache.getNativeCache()).thenReturn(ehCache);

        // Create service and inject the Spring Cache
        ehCacheDiagnosticService = new EhCacheDiagnosticService();
        ehCacheDiagnosticService.setCache(springCache);
    }

    @Test
    public void getDiagnosticDataTest() {
        Map<DiagnosticAttribute, DiagnosticCallback> resultDiagnosticData = ehCacheDiagnosticService.getDiagnosticData();

        //Test total size of diagnostic attributes collected from SessionRegistryDiagnosticService
        assertEquals(37, resultDiagnosticData.size());

        assertEquals(objectCount, resultDiagnosticData.get(new DiagnosticAttributeImpl(
                DiagnosticAttributeBuilder.EHCACHE_STAT_OBJECTCOUNT, null, null)).getDiagnosticAttributeValue());
        assertEquals(cacheHitPercentage, resultDiagnosticData.get(new DiagnosticAttributeImpl(
                DiagnosticAttributeBuilder.EHCACHE_STAT_CACHEHIT_PERCENTAGE, null, null)).getDiagnosticAttributeValue());
        assertEquals(cacheHits, resultDiagnosticData.get(new DiagnosticAttributeImpl(
                DiagnosticAttributeBuilder.EHCACHE_STAT_CACHEHITS, null, null)).getDiagnosticAttributeValue());
        assertEquals(cacheMissPercentage, resultDiagnosticData.get(new DiagnosticAttributeImpl(
                DiagnosticAttributeBuilder.EHCACHE_STAT_CACHEMISS_PERCENTAGE, null, null)).getDiagnosticAttributeValue());
        assertEquals(cacheMisses, resultDiagnosticData.get(new DiagnosticAttributeImpl(
                DiagnosticAttributeBuilder.EHCACHE_STAT_CACHEMISSES, null, null)).getDiagnosticAttributeValue());
        assertEquals(diskStoreObjectCount, resultDiagnosticData.get(new DiagnosticAttributeImpl(
                DiagnosticAttributeBuilder.EHCACHE_STAT_DISKSTORECOUNT, null, null)).getDiagnosticAttributeValue());
        assertEquals(onDiskHitPercentage, resultDiagnosticData.get(new DiagnosticAttributeImpl(
                DiagnosticAttributeBuilder.EHCACHE_STAT_DISKHITT_PERCENTAGE, null, null)).getDiagnosticAttributeValue());
        assertEquals(onDiskHits, resultDiagnosticData.get(new DiagnosticAttributeImpl(
                DiagnosticAttributeBuilder.EHCACHE_STAT_DISKHITS, null, null)).getDiagnosticAttributeValue());
        assertEquals(onDiskMisses, resultDiagnosticData.get(new DiagnosticAttributeImpl(
                DiagnosticAttributeBuilder.EHCACHE_STAT_DISKMISSES, null, null)).getDiagnosticAttributeValue());
        assertEquals(memoryStoreObjectCount, resultDiagnosticData.get(new DiagnosticAttributeImpl(
                DiagnosticAttributeBuilder.EHCACHE_STAT_MEMORYSTORECOUNT, null, null)).getDiagnosticAttributeValue());
        assertEquals(inMemoryHitPercentage, resultDiagnosticData.get(new DiagnosticAttributeImpl(
                DiagnosticAttributeBuilder.EHCACHE_STAT_MEMORYHITT_PERCENTAGE, null, null)).getDiagnosticAttributeValue());
        assertEquals(inMemoryHits, resultDiagnosticData.get(new DiagnosticAttributeImpl(
                DiagnosticAttributeBuilder.EHCACHE_STAT_MEMORYHITS, null, null)).getDiagnosticAttributeValue());
        assertEquals(inMemoryMisses, resultDiagnosticData.get(new DiagnosticAttributeImpl(
                DiagnosticAttributeBuilder.EHCACHE_STAT_MEMORYMISSES, null, null)).getDiagnosticAttributeValue());
        assertEquals(offHeapStoreObjectCount, resultDiagnosticData.get(new DiagnosticAttributeImpl(
                DiagnosticAttributeBuilder.EHCACHE_STAT_OFFHEAPSTORECOUNT, null, null)).getDiagnosticAttributeValue());
        assertEquals(OffHeapHitPercentage, resultDiagnosticData.get(new DiagnosticAttributeImpl(
                DiagnosticAttributeBuilder.EHCACHE_STAT_OFFHEAPHITT_PERCENTAGE, null, null)).getDiagnosticAttributeValue());
        assertEquals(OffHeapHits, resultDiagnosticData.get(new DiagnosticAttributeImpl(
                DiagnosticAttributeBuilder.EHCACHE_STAT_OFFHEAPHITS, null, null)).getDiagnosticAttributeValue());
        assertEquals(offHeapMisses, resultDiagnosticData.get(new DiagnosticAttributeImpl(
                DiagnosticAttributeBuilder.EHCACHE_STAT_OFFHEAPMISSES, null, null)).getDiagnosticAttributeValue());
        assertEquals(writeMaxQueueSize, resultDiagnosticData.get(new DiagnosticAttributeImpl(
                DiagnosticAttributeBuilder.EHCACHE_STAT_WRITEMAXQUEUE, null, null)).getDiagnosticAttributeValue());
        assertEquals(writeQueueLength, resultDiagnosticData.get(new DiagnosticAttributeImpl(
                DiagnosticAttributeBuilder.EHCACHE_STAT_WRITEQUEUELENGTH, null, null)).getDiagnosticAttributeValue());

        assertEquals(conf_DiskSpoolBufferSizeMB, resultDiagnosticData.get(new DiagnosticAttributeImpl(
                DiagnosticAttributeBuilder.EHCACHE_CONF_DISKSPOOL, null, null)).getDiagnosticAttributeValue());
        assertEquals(conf_DiskExpiryThreadIntervalSeconds, resultDiagnosticData.get(new DiagnosticAttributeImpl(
                DiagnosticAttributeBuilder.EHCACHE_CONF_DISKEXPIRYTHREAD, null, null)).getDiagnosticAttributeValue());
        assertEquals(conf_LoggingEnabled, resultDiagnosticData.get(new DiagnosticAttributeImpl(
                DiagnosticAttributeBuilder.EHCACHE_CONF_LOGGING, null, null)).getDiagnosticAttributeValue());
        assertEquals(conf_MaxBytesLocalDisk, resultDiagnosticData.get(new DiagnosticAttributeImpl(
                DiagnosticAttributeBuilder.EHCACHE_CONF_MBYTE_LOCALDISK, null, null)).getDiagnosticAttributeValue());
        assertEquals(conf_MaxBytesLocalHeap, resultDiagnosticData.get(new DiagnosticAttributeImpl(
                DiagnosticAttributeBuilder.EHCACHE_CONF_MBYTE_LOCALHEAP, null, null)).getDiagnosticAttributeValue());
        assertEquals(conf_MaxBytesLocalOffHeap, resultDiagnosticData.get(new DiagnosticAttributeImpl(
                DiagnosticAttributeBuilder.EHCACHE_CONF_MBYTE_LOCALOFFHEAP, null, null)).getDiagnosticAttributeValue());
        assertEquals(conf_MaxElementsOnDisk, resultDiagnosticData.get(new DiagnosticAttributeImpl(
                DiagnosticAttributeBuilder.EHCACHE_CONF_MELEMENTS_LOCALDISK, null, null)).getDiagnosticAttributeValue());
        assertEquals(conf_MaxEntriesLocalHeap, resultDiagnosticData.get(new DiagnosticAttributeImpl(
                DiagnosticAttributeBuilder.EHCACHE_CONF_MELEMENTS_MEMORY, null, null)).getDiagnosticAttributeValue());
        assertEquals(conf_TimeToIdleSeconds, resultDiagnosticData.get(new DiagnosticAttributeImpl(
                DiagnosticAttributeBuilder.EHCACHE_CONF_TIME_IDLE, null, null)).getDiagnosticAttributeValue());
        assertEquals(conf_TimeToLiveSeconds, resultDiagnosticData.get(new DiagnosticAttributeImpl(
                DiagnosticAttributeBuilder.EHCACHE_CONF_TIME_LIVE, null, null)).getDiagnosticAttributeValue());
        assertEquals(conf_MaxEntriesLocalDisk, resultDiagnosticData.get(new DiagnosticAttributeImpl(
                DiagnosticAttributeBuilder.EHCACHE_CONF_MENTRYES_MEMORY, null, null)).getDiagnosticAttributeValue());
        assertEquals(conf_MaxEntriesLocalHeap, resultDiagnosticData.get(new DiagnosticAttributeImpl(
                DiagnosticAttributeBuilder.EHCACHE_CONF_MENTRYES_LOCALHEAP, null, null)).getDiagnosticAttributeValue());
        assertEquals(diskPersistent, resultDiagnosticData.get(new DiagnosticAttributeImpl(
                DiagnosticAttributeBuilder.EHCACHE_CONF_DISKEPERSISTENT, null, null)).getDiagnosticAttributeValue());
        assertEquals(eternal, resultDiagnosticData.get(new DiagnosticAttributeImpl(
                DiagnosticAttributeBuilder.EHCACHE_CONF_ETERNAL, null, null)).getDiagnosticAttributeValue());
        assertEquals(overflowToDisk, resultDiagnosticData.get(new DiagnosticAttributeImpl(
                DiagnosticAttributeBuilder.EHCACHE_CONF_OVERFLOW_DISK, null, null)).getDiagnosticAttributeValue());
        assertEquals(overflowToOffHeap, resultDiagnosticData.get(new DiagnosticAttributeImpl(
                DiagnosticAttributeBuilder.EHCACHE_CONF_OVERFLOW_OFFHEAP, null, null)).getDiagnosticAttributeValue());
    }
}
