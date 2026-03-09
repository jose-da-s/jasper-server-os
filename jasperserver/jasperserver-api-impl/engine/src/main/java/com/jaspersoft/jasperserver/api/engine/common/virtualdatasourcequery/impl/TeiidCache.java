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

package com.jaspersoft.jasperserver.api.engine.common.virtualdatasourcequery.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.teiid.cache.Cache;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by mchan on 4/6/2017.
 */

public class TeiidCache<K, V> implements Cache<K, V> {

    org.springframework.cache.Cache springCache;
    String cacheName;
    boolean transactional;

    static final Log log = LogFactory.getLog(TeiidCacheFactory.class);

    public org.springframework.cache.Cache getSpringCache() {
        return springCache;
    }

    public void setSpringCache(org.springframework.cache.Cache springCache) {
        this.springCache = springCache;
    }

    public String getCacheName() {
        return cacheName;
    }

    public void setCacheName(String cacheName) {
        this.cacheName = cacheName;
    }

    public V get(K key) {
        org.springframework.cache.Cache.ValueWrapper wrapper = springCache.get(key);
        if (wrapper != null) {
            if (log.isDebugEnabled()) {
                log.debug("element found for key:" + key.toString());
            }
            return (V) wrapper.get();
        }
        return null;
    }

    public V put(K var1, V var2, Long var3) {
        springCache.put(var1, var2);
        return var2;
    }

    public V remove(K var1) {
        V val = get(var1);
        if (val != null) {
            springCache.evict(var1);
            return val;
        } else return null;
    }

    public int size() {
        Object nativeCache = springCache.getNativeCache();
        if (nativeCache instanceof javax.cache.Cache) {
        	int size = 0;
        	for (javax.cache.Cache.Entry<Object,Object> entry : ((javax.cache.Cache<Object,Object>) nativeCache)) 
        		size++;
			return size;
        }
        return 0;
    }

    public void clear() {
        springCache.clear();
    }

    public String getName() {
        return cacheName;
    }

    public Set<K> keySet() {
        Object nativeCache = springCache.getNativeCache();
        if (nativeCache instanceof javax.cache.Cache) {
        	Set<K> keys = new HashSet<K>();
        	for (javax.cache.Cache.Entry<Object,Object> entry : ((javax.cache.Cache<Object,Object>) nativeCache))
        		keys.add((K) entry.getKey());
        }
        return new HashSet<K>();
    }

    @Override
    public boolean isTransactional() {
        return transactional;
    }

    public void setTransactional(boolean transactional) {
        this.transactional = transactional;
    }

    public void shutdown() {
        log.warn(" -- JasperServer:  TeiidCache " + cacheName + " shutdown called.  This normal shutdown operation. ");
        springCache.clear();
    }
}

