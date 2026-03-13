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

import java.time.Duration;

import com.jaspersoft.jasperserver.api.logging.diagnostic.domain.DiagnosticAttribute;
import com.jaspersoft.jasperserver.api.logging.diagnostic.helper.DiagnosticAttributeBuilder;
import com.jaspersoft.jasperserver.api.logging.diagnostic.service.Diagnostic;
import com.jaspersoft.jasperserver.api.logging.diagnostic.service.DiagnosticCallback;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ehcache.config.ResourceUnit;
import org.ehcache.config.SizedResourcePool;

import java.util.Map;
import java.util.Optional;

import javax.cache.Cache;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

/**
 * Implementation of the EhCache Diagnostic service (Service which collecting statistics and configuration on specified cache).
 * <p>
 *
 * @author ogavavka, vsabadosh
 */
public class EhCacheDiagnosticService implements Diagnostic {
    private final static Logger logger = LogManager.getLogger(EhCacheDiagnosticService.class);

	private MBeanServer mBeanServer;

	private Cache<?, ?> cache;

	@SuppressWarnings("rawtypes")
    public Map<DiagnosticAttribute, DiagnosticCallback> getDiagnosticData() {

        ObjectName mgmt = getManagementObjectName(cache);
        ObjectName stats = getStatisticsObjectName(cache);
        if (mgmt == null || stats == null) {
        	// Error creating object names: return empty map. The above methods already logged errors.
            return new DiagnosticAttributeBuilder().build();
        }

        DiagnosticAttributeBuilder builder = new DiagnosticAttributeBuilder()
                .addDiagnosticAttribute(DiagnosticAttributeBuilder.JCACHE_STAT_CACHEHITS, new DiagnosticCallback<Long>() {
                    public Long getDiagnosticAttributeValue() {
                    	return (Long) getMBeanAttribute(stats, DiagnosticAttributeBuilder.JCACHE_STAT_CACHEHITS);
                    }
                })
            .addDiagnosticAttribute(DiagnosticAttributeBuilder.JCACHE_STAT_CACHEHITPERCENTAGE, new DiagnosticCallback<Float>() {
                public Float getDiagnosticAttributeValue() {
                	return (Float) getMBeanAttribute(stats, DiagnosticAttributeBuilder.JCACHE_STAT_CACHEHITPERCENTAGE);
                }
            })
            .addDiagnosticAttribute(DiagnosticAttributeBuilder.JCACHE_STAT_CACHEMISSES, new DiagnosticCallback<Long>() {
                public Long getDiagnosticAttributeValue() {
                	return (Long) getMBeanAttribute(stats, DiagnosticAttributeBuilder.JCACHE_STAT_CACHEMISSES);
                }
            })
            .addDiagnosticAttribute(DiagnosticAttributeBuilder.JCACHE_STAT_CACHEMISSPERCENTAGE, new DiagnosticCallback<Float>() {
                public Float getDiagnosticAttributeValue() {
                	return (Float) getMBeanAttribute(stats, DiagnosticAttributeBuilder.JCACHE_STAT_CACHEMISSPERCENTAGE);
                }
            })
            .addDiagnosticAttribute(DiagnosticAttributeBuilder.JCACHE_STAT_CACHEGETS, new DiagnosticCallback<Long>() {
                public Long getDiagnosticAttributeValue() {
                	return (Long) getMBeanAttribute(stats, DiagnosticAttributeBuilder.JCACHE_STAT_CACHEGETS);
                }
            })
            .addDiagnosticAttribute(DiagnosticAttributeBuilder.JCACHE_STAT_CACHEPUTS, new DiagnosticCallback<Long>() {
                public Long getDiagnosticAttributeValue() {
                	return (Long) getMBeanAttribute(stats, DiagnosticAttributeBuilder.JCACHE_STAT_CACHEPUTS);
                }
            })
            .addDiagnosticAttribute(DiagnosticAttributeBuilder.JCACHE_STAT_CACHEREMOVALS, new DiagnosticCallback<Long>() {
                public Long getDiagnosticAttributeValue() {
                	return (Long) getMBeanAttribute(stats, DiagnosticAttributeBuilder.JCACHE_STAT_CACHEREMOVALS);
                }
            })
            .addDiagnosticAttribute(DiagnosticAttributeBuilder.JCACHE_STAT_CACHEEVICTIONS, new DiagnosticCallback<Long>() {
                public Long getDiagnosticAttributeValue() {
                	return (Long) getMBeanAttribute(stats, DiagnosticAttributeBuilder.JCACHE_STAT_CACHEEVICTIONS);
                }
            })
            .addDiagnosticAttribute(DiagnosticAttributeBuilder.JCACHE_STAT_AVERAGEGETTIME, new DiagnosticCallback<Float>() {
                public Float getDiagnosticAttributeValue() {
                	return (Float) getMBeanAttribute(stats, DiagnosticAttributeBuilder.JCACHE_STAT_AVERAGEGETTIME);
                }
            })
            .addDiagnosticAttribute(DiagnosticAttributeBuilder.JCACHE_STAT_AVERAGEPUTTIME, new DiagnosticCallback<Float>() {
                public Float getDiagnosticAttributeValue() {
                	return (Float) getMBeanAttribute(stats, DiagnosticAttributeBuilder.JCACHE_STAT_AVERAGEPUTTIME);
                }
            })
            .addDiagnosticAttribute(DiagnosticAttributeBuilder.JCACHE_CONF_KEYTYPE, new DiagnosticCallback<String>() {
                public String getDiagnosticAttributeValue() {
                	return (String) getMBeanAttribute(mgmt, DiagnosticAttributeBuilder.JCACHE_CONF_KEYTYPE);
                }
            })
            .addDiagnosticAttribute(DiagnosticAttributeBuilder.JCACHE_CONF_VALUETYPE, new DiagnosticCallback<String>() {
                public String getDiagnosticAttributeValue() {
                	return (String) getMBeanAttribute(mgmt, DiagnosticAttributeBuilder.JCACHE_CONF_VALUETYPE);
                }
            })
            .addDiagnosticAttribute(DiagnosticAttributeBuilder.JCACHE_CONF_READTHROUGH, new DiagnosticCallback<Boolean>() {
                public Boolean getDiagnosticAttributeValue() {
                	return (Boolean) getMBeanAttribute(mgmt, DiagnosticAttributeBuilder.JCACHE_CONF_READTHROUGH);
                }
            })
            .addDiagnosticAttribute(DiagnosticAttributeBuilder.JCACHE_CONF_WRITETHROUGH, new DiagnosticCallback<Boolean>() {
                public Boolean getDiagnosticAttributeValue() {
                	return (Boolean) getMBeanAttribute(mgmt, DiagnosticAttributeBuilder.JCACHE_CONF_WRITETHROUGH);
                }
            })
            .addDiagnosticAttribute(DiagnosticAttributeBuilder.JCACHE_CONF_STOREBYVALUE, new DiagnosticCallback<Boolean>() {
                public Boolean getDiagnosticAttributeValue() {
                	return (Boolean) getMBeanAttribute(mgmt, DiagnosticAttributeBuilder.JCACHE_CONF_STOREBYVALUE);
                }
            })
            .addDiagnosticAttribute(DiagnosticAttributeBuilder.JCACHE_CONF_STATISTICSENABLED, new DiagnosticCallback<Boolean>() {
                public Boolean getDiagnosticAttributeValue() {
                	return (Boolean) getMBeanAttribute(mgmt, DiagnosticAttributeBuilder.JCACHE_CONF_STATISTICSENABLED);
                }
            })
            .addDiagnosticAttribute(DiagnosticAttributeBuilder.JCACHE_CONF_MANAGEMENTENABLED, new DiagnosticCallback<Boolean>() {
                public Boolean getDiagnosticAttributeValue() {
                	return (Boolean) getMBeanAttribute(mgmt, DiagnosticAttributeBuilder.JCACHE_CONF_MANAGEMENTENABLED);
                }
            });
        // Ehcache-specific props if possible. Only the ones explicitly configured in ehcache-*.xml, though.
        try {
        	@SuppressWarnings("unchecked")
        	org.ehcache.jsr107.Eh107Configuration<Object, Object> eh107Configuration = 
        		    cache.getConfiguration(org.ehcache.jsr107.Eh107Configuration.class); // <3>

        	@SuppressWarnings("unchecked")
        	org.ehcache.config.CacheRuntimeConfiguration<Object, Object> runtimeConfiguration = 
        		    eh107Configuration.unwrap(org.ehcache.config.CacheRuntimeConfiguration.class);

        	org.ehcache.config.ResourcePools resourcePools = runtimeConfiguration.getResourcePools();

        	builder.addDiagnosticAttribute(DiagnosticAttributeBuilder.EHCACHE_CONF_TIME_LIVE, new DiagnosticCallback<Duration>() {
				public Duration getDiagnosticAttributeValue() {
					return runtimeConfiguration.getExpiryPolicy().getExpiryForCreation(null, null);
				}
			})
			.addDiagnosticAttribute(DiagnosticAttributeBuilder.EHCACHE_CONF_TIME_IDLE, new DiagnosticCallback<Duration>() {
				public Duration getDiagnosticAttributeValue() {
					return runtimeConfiguration.getExpiryPolicy().getExpiryForAccess(null, null);
				}
			})
			.addDiagnosticAttribute(DiagnosticAttributeBuilder.EHCACHE_CONF_HEAPSIZE, new DiagnosticCallback<Long>() {
				public Long getDiagnosticAttributeValue() {
					return Optional.ofNullable(resourcePools.getPoolForResource(org.ehcache.config.ResourceType.Core.HEAP))
							.map(SizedResourcePool::getSize)
							.orElse(0L);
				}
			})
			.addDiagnosticAttribute(DiagnosticAttributeBuilder.EHCACHE_CONF_HEAPUNIT, new DiagnosticCallback<String>() {
				public String getDiagnosticAttributeValue() {
					return Optional.ofNullable(resourcePools.getPoolForResource(org.ehcache.config.ResourceType.Core.HEAP))
							.map(SizedResourcePool::getUnit)
							.map(ResourceUnit::toString)
							.orElse("");
				}
			})
			.addDiagnosticAttribute(DiagnosticAttributeBuilder.EHCACHE_CONF_OFFHEAPSIZE, new DiagnosticCallback<Long>() {
				public Long getDiagnosticAttributeValue() {
					return Optional.ofNullable(resourcePools.getPoolForResource(org.ehcache.config.ResourceType.Core.OFFHEAP))
							.map(SizedResourcePool::getSize)
							.orElse(0L);
				}
			})
			.addDiagnosticAttribute(DiagnosticAttributeBuilder.EHCACHE_CONF_OFFHEAPUNIT, new DiagnosticCallback<String>() {
				public String getDiagnosticAttributeValue() {
					return Optional.ofNullable(resourcePools.getPoolForResource(org.ehcache.config.ResourceType.Core.OFFHEAP))
							.map(SizedResourcePool::getUnit)
							.map(ResourceUnit::toString)
							.orElse("");
				}
			})
			.addDiagnosticAttribute(DiagnosticAttributeBuilder.EHCACHE_CONF_DISKSIZE, new DiagnosticCallback<Long>() {
				public Long getDiagnosticAttributeValue() {
					return Optional.ofNullable(resourcePools.getPoolForResource(org.ehcache.config.ResourceType.Core.DISK))
							.map(SizedResourcePool::getSize)
							.orElse(0L);
				}
			})
			.addDiagnosticAttribute(DiagnosticAttributeBuilder.EHCACHE_CONF_DISKUNIT, new DiagnosticCallback<String>() {
				public String getDiagnosticAttributeValue() {
					return Optional.ofNullable(resourcePools.getPoolForResource(org.ehcache.config.ResourceType.Core.DISK))
							.map(SizedResourcePool::getUnit)
							.map(ResourceUnit::toString)
							.orElse("");
				}
			});
        } catch (IllegalArgumentException e) {
        	logger.error("Could not unwrap Ehcache configuration from JCache", e);
        }
        return builder.build();
    }

    private ObjectName getManagementObjectName(Cache<?, ?> jCache) {
        try {
        	ObjectName mgmt = new ObjectName("javax.cache:type=CacheConfiguration"
        		+ ",CacheManager=" + sanitizeMbeanProperty(jCache.getCacheManager().getURI().toString())
        		+ ",Cache=" + sanitizeMbeanProperty(jCache.getName()));
        	return mgmt;
        } catch (MalformedObjectNameException e) {
        	logger.error("error constructing ObjectName for management cache", e);
        	return null;
        }
    }

    private ObjectName getStatisticsObjectName(Cache<?, ?> jCache) {

        try {
        	ObjectName stats = new ObjectName("javax.cache:type=CacheStatistics"
        		+ ",CacheManager=" + sanitizeMbeanProperty(jCache.getCacheManager().getURI().toString())
        		+ ",Cache=" + sanitizeMbeanProperty(jCache.getName()));
        	return stats;
        } catch (MalformedObjectNameException e) {
        	logger.error("error constructing ObjectName for statistics cache", e);
        	return null;
        }
    }

    private Object getMBeanAttribute(ObjectName mBeanName, String attribute) {
    	try {
    		return mBeanServer.getAttribute(mBeanName, attribute);
    	} catch (Exception e) {
    		logger.error("failed to get attribute " + attribute + " from MBean " + mBeanName.getCanonicalName(), e);
    		return null;
    	}
    }

    public void setCache(Cache<Object, Object> cache) {
        this.cache = cache;
    }

    public void setmBeanServer(MBeanServer mBeanServer) {
		this.mBeanServer = mBeanServer;
	}

    /*
     * Copied from org.ehcache.jsr107.Eh107MXBean
     */
    private String sanitizeMbeanProperty(String string) {
        return string == null ? "" : string.replaceAll(",|:|=|\n", ".");
      }
}
