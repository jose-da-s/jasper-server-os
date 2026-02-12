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
package com.jaspersoft.jasperserver.remote.connection.storage;

import com.jaspersoft.jasperserver.remote.exception.ResourceNotFoundException;
import org.springframework.cache.Cache;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.UUID;

/**
 * <p></p>
 *
 * @author yaroslav.kovalchyk
 * @version $Id$
 */
@Service
public class ContextDataStorage {
    @Resource(name = "contextsCache")
    private Cache cache;

    public UUID save(ContextDataPair item){
        final UUID uuid = UUID.randomUUID();
        cache.put(uuid, item);
        return uuid;
    }

    public ContextDataPair get(UUID uuid, boolean throwExceptionIfNotFound){
        final Cache.ValueWrapper wrapper = cache.get(uuid);
        if (wrapper == null) {
            if (throwExceptionIfNotFound) {
                throw new ResourceNotFoundException(uuid.toString());
            } else {
                // return null if resource not found.  ContextsManager will handle this NULL case
                return null;
            }
        }
        return (ContextDataPair) wrapper.get();
    }

    public ContextDataPair get(UUID uuid){
        return get(uuid, true);
    }

    public void delete(UUID uuid){
        cache.evict(uuid);
    }

    public void update(UUID uuid, ContextDataPair item){
        cache.put(uuid, item);
    }

}
