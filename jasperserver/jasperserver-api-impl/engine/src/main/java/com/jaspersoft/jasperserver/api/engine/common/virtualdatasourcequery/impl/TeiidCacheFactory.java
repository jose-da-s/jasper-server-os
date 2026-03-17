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
import org.teiid.cache.CacheFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class TeiidCacheFactory implements CacheFactory {
    Map<String, Cache> cacheFactoryMap = new HashMap<String, Cache>();


    static final Log log = LogFactory.getLog(TeiidCacheFactory.class);

    public Map<String, Cache> getCacheFactoryMap() {
        return cacheFactoryMap;
    }

    public void setCacheFactoryMap(Map<String, Cache> cacheFactoryMap) {
        this.cacheFactoryMap = cacheFactoryMap;
    }

    public <K, V> Cache<K, V> get(String var1) {

        Cache currentCache = cacheFactoryMap.get(var1);

        if (currentCache != null) {
            if (log.isDebugEnabled()) {
                log.debug("Teiid cache found for key:" + var1);
            }
            return currentCache;
        }
        return null;
    }

    public void destroy() {
        cacheFactoryMap = null;
    }



}