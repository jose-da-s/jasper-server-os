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
package com.jaspersoft.jasperserver.jaxrs.cache;

import com.jaspersoft.jasperserver.api.common.util.CacheManager;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p></p>
 *
 * @author yaroslav.kovalchyk
 * @version $Id$
 */
@Service
@Path("/caches")
@Scope("prototype")
public class CachesJaxrsService implements InitializingBean {
    @Autowired
    private List<CacheManager> managers;

    private Map<String, CacheManager> managersMap;
    @DELETE
    @Path("/{cacheId}")
    public void clearCache(@PathParam("cacheId")String cacheId){
        final CacheManager cacheManager = managersMap.get(cacheId);
        if(cacheManager != null) cacheManager.clear();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        final Map<String,CacheManager> cacheManagerMap = new HashMap<String, CacheManager>();
        for(CacheManager cacheManager : managers){
            cacheManagerMap.put(cacheManager.getCacheId(), cacheManager);
        }
        managersMap = Collections.unmodifiableMap(cacheManagerMap);
    }
}
