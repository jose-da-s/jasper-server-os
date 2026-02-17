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
package com.jaspersoft.jasperserver.api.engine.jasperreports.service.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.jaspersoft.jasperserver.api.engine.jasperreports.util.RepositoryCacheMap;
import com.jaspersoft.jasperserver.api.metadata.common.domain.util.DataContainerStreamUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.cache.Cache;

import org.apache.commons.collections.OrderedMap;

import com.jaspersoft.jasperserver.api.common.domain.ExecutionContext;
import com.jaspersoft.jasperserver.api.engine.common.service.EngineService;
import com.jaspersoft.jasperserver.api.metadata.common.domain.ResourceReference;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EhcacheEngineServiceImpl extends EngineBaseDecorator implements EhcacheEngineService {

	private static final Log log = LogFactory.getLog(EhcacheEngineServiceImpl.class);

	private static class DiagnosticCacheKey implements Serializable {

		private static final long serialVersionUID = 20150210130500L;

		private final String uri;
		private final Serializable key;
		private final DiagnosticItemType type;

		public DiagnosticCacheKey(Serializable key, String uri, DiagnosticItemType type) {
			if (key == null) throw new IllegalArgumentException("key is null");
			if (StringUtils.isBlank(uri)) throw new IllegalArgumentException("uri is blank");
			if (type == null) throw new IllegalArgumentException("type is null");

			this.key = key;
			this.uri = uri;
			this.type = type;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (!(o instanceof DiagnosticCacheKey)) return false;

			DiagnosticCacheKey that = (DiagnosticCacheKey) o;

			if (!key.equals(that.key)) return false;
			if (type != that.type) return false;
			if (!uri.equals(that.uri)) return false;

			return true;
		}

		@Override
		public int hashCode() {
			int result = uri.hashCode();
			result = 31 * result + key.hashCode();
			result = 31 * result + type.hashCode();
			return result;
		}

		public Serializable getKey() {
			return key;
		}

		public String getUri() {
			return uri;
		}

		public DiagnosticItemType getType() {
			return type;
		}
	}


	private Cache cache;

	private Cache diagnosticCache;

	public Cache getCache() {
		return cache;
	}

	public void setCache(Cache cache) {
		this.cache = cache;
	}

	public Cache getDiagnosticCache() {
		return diagnosticCache;
	}

	public void setDiagnosticCache(Cache diagnosticCache) {
		this.diagnosticCache = diagnosticCache;
	}

	public void setEngineService(EngineService engine) {
		setDecoratedEngine(engine);
	}

	public EngineService getEngineService() {
		return getDecoratedEngine();
	}
		
    public OrderedMap executeQuery(ExecutionContext context,
		ResourceReference queryReference, String keyColumn, String[] resultColumns,
		ResourceReference defaultDataSourceReference,
		Map parameterValues, Map<String, Class<?>> parameterTypes, boolean formatValueColumns) 
    {
		String key = (String)parameterValues.get(IC_CACHE_KEY);
		OrderedMap value = null;
        boolean refresh = parameterValues!=null&&parameterValues.containsKey(IC_REFRESH_KEY);

		String diagnosticReportURI = null;
		boolean diagnostic = false;
		if (parameterValues!=null) {
			Object obj = parameterValues.get(DIAGNOSTIC_REPORT_URI);
			if (obj instanceof String && StringUtils.isNotBlank(diagnosticReportURI = (String) obj)) {
				diagnostic = true;
			}
		}

		String diagnosticKey = key;
		if (diagnostic && key != null) {
			int pos = Math.min(key.lastIndexOf(';'), key.lastIndexOf(' '));
			if (pos > 0) {
				diagnosticKey = key.substring(0, pos);
			}
		}

		if (key!=null) {
			if (refresh) {
				cache.evict(key);
				if (diagnostic) {
					removeFromDiagnosticCache(diagnosticKey);
				}
			} else {
				Cache.ValueWrapper wrapper = cache.get(key);
	    		if (wrapper != null) {
	    			value = (OrderedMap)(wrapper.get());
	    			if (value!=null) return value;
	    		}
				if (diagnostic) {
					value = (OrderedMap) getFromDiagnosticCache(diagnosticKey);
					if (value != null) return value;
				}
			}
		}
		value=getDecoratedEngine().executeQuery(context, queryReference, keyColumn, resultColumns, defaultDataSourceReference, parameterValues, parameterTypes, formatValueColumns);
		if (key!=null&&value!=null) {
			cache.put(key, value);
			if (diagnostic) {
				putToDiagnocsticCache(diagnosticKey, value);
				saveKeyToDiagnosticCache(diagnosticReportURI, diagnosticKey, DiagnosticItemType.INPUT_CONTROL_CACHE);
			}
		}
		return value;
    }
   
    public void clear() {
    	cache.clear();
    }

	public synchronized void removeFromDiagnosticCache(Serializable key) {
		if (key != null) {
			// Spring Cache doesn't expose status checks - assume cache is available
			diagnosticCache.evict(key);
		}
	}

	public synchronized void removeDiagnosticKeys(String uri) {
		if (StringUtils.isBlank(uri)) {
			return;
		}

		// Spring Cache abstraction doesn't provide key iteration
		// Fall back to native Ehcache cache if available
		try {
			Object nativeCache = diagnosticCache.getNativeCache();
			if (nativeCache instanceof net.sf.ehcache.Ehcache) {
				net.sf.ehcache.Ehcache ehcache = (net.sf.ehcache.Ehcache) nativeCache;
				List keys = Collections.unmodifiableList(ehcache.getKeys());
				for (Object key : keys) {
					if (key instanceof DiagnosticCacheKey) {
						DiagnosticCacheKey cacheKey = (DiagnosticCacheKey) key;
						if (cacheKey.getUri().equals(uri)) {
							diagnosticCache.evict(cacheKey);
						}
					}
				}
			}
		} catch (Exception e) {
			log.error("Key iteration not supported by cache provider: {}", e);
		}
	}

	public synchronized Serializable getFromDiagnosticCache(Serializable key) {
		if (key != null) {
			Cache.ValueWrapper wrapper = diagnosticCache.get(key);
			if (wrapper != null) {
				return (Serializable) wrapper.get();
			}
		}
		return null;
	}

	public synchronized void putToDiagnocsticCache(Serializable key, Object value) {
		if (key != null && value != null) {
			diagnosticCache.put(key, value);
		}
	}

	public synchronized void saveKeyToDiagnosticCache(String uri, Serializable key, DiagnosticItemType type) {
		if (type == null) throw new IllegalArgumentException("type is null");

		if (key != null && StringUtils.isNotBlank(uri)) {
			diagnosticCache.put(new DiagnosticCacheKey(key, uri, type), "");
		}
	}

	public synchronized Set<Serializable> getDiagnosticKeys(String uri, DiagnosticItemType type) {
		Set<Serializable> result = new HashSet<Serializable>();
		if (StringUtils.isBlank(uri)) {
			return result;
		}

		// Spring Cache abstraction doesn't provide key iteration
		// Fall back to native Ehcache cache if available
		try {
			Object nativeCache = diagnosticCache.getNativeCache();
			if (nativeCache instanceof net.sf.ehcache.Ehcache) {
				net.sf.ehcache.Ehcache ehcache = (net.sf.ehcache.Ehcache) nativeCache;
				List keys = ehcache.getKeys();
				for (Object key : keys) {
					if (key instanceof DiagnosticCacheKey) {
						DiagnosticCacheKey cacheKey = (DiagnosticCacheKey) key;
						if (cacheKey.getUri().equals(uri) && cacheKey.getType().equals(type)) {
							result.add(cacheKey.getKey());
						}
					}
				}
			}
		} catch (Exception e) {
			// Key iteration not supported by this cache provider
			// Log warning and return empty result
		}

		return result;
	}
}
