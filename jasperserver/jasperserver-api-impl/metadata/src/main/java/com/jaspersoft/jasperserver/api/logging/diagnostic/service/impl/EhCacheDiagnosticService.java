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
import com.jaspersoft.jasperserver.api.logging.diagnostic.service.Diagnostic;
import com.jaspersoft.jasperserver.api.logging.diagnostic.service.DiagnosticCallback;
import org.springframework.cache.Cache;

import java.util.Map;

/**
 * Implementation of the EhCache Diagnostic service (Service which collecting statistics and configuration on specified cache).
 * <p>
 * This service uses Spring Cache abstraction but accesses native EhCache instance for diagnostic purposes.
 *
 * @author ogavavka, vsabadosh
 */
public class EhCacheDiagnosticService implements Diagnostic {

    private Cache cache;

    public Map<DiagnosticAttribute, DiagnosticCallback> getDiagnosticData() {
        // Access native EhCache instance for statistics
        final net.sf.ehcache.Ehcache ehcache = getNativeEhcache();
        if (ehcache == null) {
            // Return empty map if cache is not EhCache
            return new DiagnosticAttributeBuilder().build();
        }

        final net.sf.ehcache.management.CacheStatistics cacheStatistics =
            new net.sf.ehcache.management.CacheStatistics(ehcache);
        final net.sf.ehcache.config.CacheConfiguration cacheConfig = ehcache.getCacheConfiguration();
        return new DiagnosticAttributeBuilder()
            .addDiagnosticAttribute(DiagnosticAttributeBuilder.EHCACHE_STAT_OBJECTCOUNT, new DiagnosticCallback<Long>() {
                public Long getDiagnosticAttributeValue() {
                    return cacheStatistics.getObjectCount();
                }
            })
            .addDiagnosticAttribute(DiagnosticAttributeBuilder.EHCACHE_STAT_CACHEHIT_PERCENTAGE, new DiagnosticCallback<Double>() {
                public Double getDiagnosticAttributeValue() {
                    return cacheStatistics.getCacheHitPercentage();
                }
            })
            .addDiagnosticAttribute(DiagnosticAttributeBuilder.EHCACHE_STAT_CACHEHITS, new DiagnosticCallback<Long>() {
                public Long getDiagnosticAttributeValue() {
                    return cacheStatistics.getCacheHits();
                }
            })
            .addDiagnosticAttribute(DiagnosticAttributeBuilder.EHCACHE_STAT_CACHEMISS_PERCENTAGE, new DiagnosticCallback<Double>() {
                public Double getDiagnosticAttributeValue() {
                    return cacheStatistics.getCacheMissPercentage();
                }
            })
            .addDiagnosticAttribute(DiagnosticAttributeBuilder.EHCACHE_STAT_CACHEMISSES, new DiagnosticCallback<Long>() {
                public Long getDiagnosticAttributeValue() {
                    return cacheStatistics.getCacheMisses();
                }
            })
            .addDiagnosticAttribute(DiagnosticAttributeBuilder.EHCACHE_STAT_DISKSTORECOUNT, new DiagnosticCallback<Long>() {
                public Long getDiagnosticAttributeValue() {
                    return cacheStatistics.getDiskStoreObjectCount();
                }
            })
            .addDiagnosticAttribute(DiagnosticAttributeBuilder.EHCACHE_STAT_DISKHITT_PERCENTAGE, new DiagnosticCallback<Double>() {
                public Double getDiagnosticAttributeValue() {
                    return cacheStatistics.getOnDiskHitPercentage();
                }
            })
            .addDiagnosticAttribute(DiagnosticAttributeBuilder.EHCACHE_STAT_DISKHITS, new DiagnosticCallback<Long>() {
                public Long getDiagnosticAttributeValue() {
                    return cacheStatistics.getOnDiskHits();
                }
            })
            .addDiagnosticAttribute(DiagnosticAttributeBuilder.EHCACHE_STAT_DISKMISSES, new DiagnosticCallback<Long>() {
                public Long getDiagnosticAttributeValue() {
                    return cacheStatistics.getOnDiskMisses();
                }
            })
            .addDiagnosticAttribute(DiagnosticAttributeBuilder.EHCACHE_STAT_DISKHITS, new DiagnosticCallback<Long>() {
                public Long getDiagnosticAttributeValue() {
                    return cacheStatistics.getOnDiskHits();
                }
            })
            .addDiagnosticAttribute(DiagnosticAttributeBuilder.EHCACHE_STAT_MEMORYSTORECOUNT, new DiagnosticCallback<Long>() {
                public Long getDiagnosticAttributeValue() {
                    return cacheStatistics.getMemoryStoreObjectCount();
                }
            })
            .addDiagnosticAttribute(DiagnosticAttributeBuilder.EHCACHE_STAT_MEMORYHITT_PERCENTAGE, new DiagnosticCallback<Double>() {
                public Double getDiagnosticAttributeValue() {
                    return cacheStatistics.getInMemoryHitPercentage();
                }
            })
            .addDiagnosticAttribute(DiagnosticAttributeBuilder.EHCACHE_STAT_MEMORYHITS, new DiagnosticCallback<Long>() {
                public Long getDiagnosticAttributeValue() {
                    return cacheStatistics.getInMemoryHits();
                }
            })
            .addDiagnosticAttribute(DiagnosticAttributeBuilder.EHCACHE_STAT_MEMORYMISSES, new DiagnosticCallback<Long>() {
                public Long getDiagnosticAttributeValue() {
                    return cacheStatistics.getInMemoryMisses();
                }
            })
            .addDiagnosticAttribute(DiagnosticAttributeBuilder.EHCACHE_STAT_OFFHEAPSTORECOUNT, new DiagnosticCallback<Long>() {
                public Long getDiagnosticAttributeValue() {
                    return cacheStatistics.getOffHeapStoreObjectCount();
                }
            })
            .addDiagnosticAttribute(DiagnosticAttributeBuilder.EHCACHE_STAT_OFFHEAPHITT_PERCENTAGE, new DiagnosticCallback<Double>() {
                public Double getDiagnosticAttributeValue() {
                    return cacheStatistics.getOffHeapHitPercentage();
                }
            })
            .addDiagnosticAttribute(DiagnosticAttributeBuilder.EHCACHE_STAT_OFFHEAPHITS, new DiagnosticCallback<Long>() {
                public Long getDiagnosticAttributeValue() {
                    return cacheStatistics.getOffHeapHits();
                }
            })
            .addDiagnosticAttribute(DiagnosticAttributeBuilder.EHCACHE_STAT_OFFHEAPMISSES, new DiagnosticCallback<Long>() {
                public Long getDiagnosticAttributeValue() {
                    return cacheStatistics.getOffHeapMisses();
                }
            })
            .addDiagnosticAttribute(DiagnosticAttributeBuilder.EHCACHE_STAT_WRITEMAXQUEUE, new DiagnosticCallback<Integer>() {
                public Integer getDiagnosticAttributeValue() {
                    return cacheStatistics.getWriterMaxQueueSize();
                }
            })
            .addDiagnosticAttribute(DiagnosticAttributeBuilder.EHCACHE_STAT_WRITEQUEUELENGTH, new DiagnosticCallback<Long>() {
                public Long getDiagnosticAttributeValue() {
                    return cacheStatistics.getWriterQueueLength();
                }
            })
            .addDiagnosticAttribute(DiagnosticAttributeBuilder.EHCACHE_CONF_STATISTICS, new DiagnosticCallback<Boolean>() {
                public Boolean getDiagnosticAttributeValue() {
                    return cacheConfig.getStatistics();
                }
            })
            .addDiagnosticAttribute(DiagnosticAttributeBuilder.EHCACHE_CONF_DISKSPOOL, new DiagnosticCallback<Integer>() {
                public Integer getDiagnosticAttributeValue() {
                    return cacheConfig.getDiskSpoolBufferSizeMB();
                }
            })
            .addDiagnosticAttribute(DiagnosticAttributeBuilder.EHCACHE_CONF_DISKEXPIRYTHREAD, new DiagnosticCallback<Long>() {
                public Long getDiagnosticAttributeValue() {
                    return cacheConfig.getDiskExpiryThreadIntervalSeconds();
                }
            })
            .addDiagnosticAttribute(DiagnosticAttributeBuilder.EHCACHE_CONF_LOGGING, new DiagnosticCallback<Boolean>() {
                public Boolean getDiagnosticAttributeValue() {
                    return cacheConfig.getLogging();
                }
            })
            .addDiagnosticAttribute(DiagnosticAttributeBuilder.EHCACHE_CONF_MBYTE_LOCALDISK, new DiagnosticCallback<Long>() {
                public Long getDiagnosticAttributeValue() {
                    return cacheConfig.getMaxBytesLocalDisk();
                }
            })
            .addDiagnosticAttribute(DiagnosticAttributeBuilder.EHCACHE_CONF_MBYTE_LOCALHEAP, new DiagnosticCallback<Long>() {
                public Long getDiagnosticAttributeValue() {
                    return cacheConfig.getMaxBytesLocalHeap();
                }
            })
            .addDiagnosticAttribute(DiagnosticAttributeBuilder.EHCACHE_CONF_MBYTE_LOCALOFFHEAP, new DiagnosticCallback<Long>() {
                public Long getDiagnosticAttributeValue() {
                    return cacheConfig.getMaxBytesLocalOffHeap();
                }
            })
            .addDiagnosticAttribute(DiagnosticAttributeBuilder.EHCACHE_CONF_MELEMENTS_LOCALDISK, new DiagnosticCallback<Integer>() {
                public Integer getDiagnosticAttributeValue() {
                    return cacheConfig.getMaxElementsOnDisk();
                }
            })
            .addDiagnosticAttribute(DiagnosticAttributeBuilder.EHCACHE_CONF_MELEMENTS_MEMORY, new DiagnosticCallback<Long>() {
                public Long getDiagnosticAttributeValue() {
                    return cacheConfig.getMaxEntriesLocalHeap();
                }
            })
            .addDiagnosticAttribute(DiagnosticAttributeBuilder.EHCACHE_CONF_TIME_IDLE, new DiagnosticCallback<Long>() {
                public Long getDiagnosticAttributeValue() {
                    return cacheConfig.getTimeToIdleSeconds();
                }
            })
            .addDiagnosticAttribute(DiagnosticAttributeBuilder.EHCACHE_CONF_TIME_LIVE, new DiagnosticCallback<Long>() {
                public Long getDiagnosticAttributeValue() {
                    return cacheConfig.getTimeToLiveSeconds();
                }
            })
            .addDiagnosticAttribute(DiagnosticAttributeBuilder.EHCACHE_CONF_MEMORYSTORE_POLICY, new DiagnosticCallback<String>() {
                public String getDiagnosticAttributeValue() {
                    return cacheConfig.getMemoryStoreEvictionPolicy().toString();
                }
            })
            .addDiagnosticAttribute(DiagnosticAttributeBuilder.EHCACHE_CONF_MENTRYES_MEMORY, new DiagnosticCallback<Long>() {
                public Long getDiagnosticAttributeValue() {
                    return cacheConfig.getMaxEntriesLocalDisk();
                }
            })
            .addDiagnosticAttribute(DiagnosticAttributeBuilder.EHCACHE_CONF_MENTRYES_LOCALHEAP, new DiagnosticCallback<Long>() {
                public Long getDiagnosticAttributeValue() {
                    return cacheConfig.getMaxEntriesLocalHeap();
                }
            })
            .addDiagnosticAttribute(DiagnosticAttributeBuilder.EHCACHE_CONF_DISKEPERSISTENT, new DiagnosticCallback<Boolean>() {
                public Boolean getDiagnosticAttributeValue() {
                    return cacheConfig.isDiskPersistent();
                }
            })
            .addDiagnosticAttribute(DiagnosticAttributeBuilder.EHCACHE_CONF_ETERNAL, new DiagnosticCallback<Boolean>() {
                public Boolean getDiagnosticAttributeValue() {
                    return cacheConfig.isEternal();
                }
            })
            .addDiagnosticAttribute(DiagnosticAttributeBuilder.EHCACHE_CONF_OVERFLOW_DISK, new DiagnosticCallback<Boolean>() {
                public Boolean getDiagnosticAttributeValue() {
                    return cacheConfig.isOverflowToDisk();
                }
            })
            .addDiagnosticAttribute(DiagnosticAttributeBuilder.EHCACHE_CONF_OVERFLOW_OFFHEAP, new DiagnosticCallback<Boolean>() {
                public Boolean getDiagnosticAttributeValue() {
                    return cacheConfig.isOverflowToOffHeap();
                }
            }).build();
    }

    /**
     * Gets the native EhCache instance from the Spring Cache wrapper.
     *
     * @return the native EhCache instance, or null if the cache is not EhCache
     */
    private net.sf.ehcache.Ehcache getNativeEhcache() {
        if (cache == null) {
            return null;
        }
        Object nativeCache = cache.getNativeCache();
        if (nativeCache instanceof net.sf.ehcache.Ehcache) {
            return (net.sf.ehcache.Ehcache) nativeCache;
        }
        return null;
    }

    public void setCache(Cache cache) {
        this.cache = cache;
    }
}
