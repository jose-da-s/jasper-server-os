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

package com.jaspersoft.jasperserver.api.metadata.user.service.impl;

import com.jaspersoft.jasperserver.api.metadata.user.domain.ProfileAttribute;
import com.jaspersoft.jasperserver.api.metadata.user.service.ProfileAttributeCache;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.cache.Cache;


/**
 * Created with IntelliJ IDEA.
 * User: nthapa
 * Date: 3/12/15
 * Time: 2:35 PM
 * To change this template use File | Settings | File Templates.
 */
public class ProfileAttributeCacheImpl implements ProfileAttributeCache {

    static final Log log = LogFactory.getLog(ProfileAttributeCacheImpl.class);

    private Cache attributeCache;

    public Cache getAttributeCache() {
        return attributeCache;
    }

    public void setAttributeCache(Cache attributeCache) {
        this.attributeCache = attributeCache;
    }

    @Override
    public void addItem(Object key, Object value) {
        if (attributeCache != null) {
            attributeCache.put(key, value);
        }
    }

    @Override
    public Object getItem(Object key) {
        if (attributeCache != null) {
            Cache.ValueWrapper wrapper = attributeCache.get(key);
            if (wrapper != null) {
                if (log.isDebugEnabled()) {
                    log.debug("element found for key:" + key.toString());
                }
                return wrapper.get();
            }
        }
        return null;
    }

    @Override
    public void removeItem(Object key) {
        if (attributeCache != null) {
            attributeCache.evict(key);
        }
    }

    @Override
    public void clearAll() {
        if (attributeCache != null) {
            attributeCache.clear();
        }
    }

    public void shutdown() {
        log.warn(" -- JasperServer:  ProfileAttributeCacheImpl shutdown called.  This normal shutdown operation. ");
        if (attributeCache != null) {
            attributeCache.clear();
        }
    }
}
