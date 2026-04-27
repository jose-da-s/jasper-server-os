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
package com.jaspersoft.hibernate.resolver;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.jpa.HibernatePersistenceProvider;

import jakarta.persistence.spi.PersistenceProvider;
import jakarta.persistence.spi.PersistenceProviderResolver;
import jakarta.persistence.spi.PersistenceProviderResolverHolder;
import java.util.Collections;
import java.util.List;

/**
 * a workaround to resolve Websphere classloading troubles
 *
 * @see https://hibernate.atlassian.net/browse/JPA-4
 */
public class HibernatePersistenceProviderResolver implements PersistenceProviderResolver {
    private static final Logger LOGGER = LogManager.getLogger(HibernatePersistenceProviderResolver.class.getName());

    private volatile PersistenceProvider persistenceProvider = new HibernatePersistenceProvider();

    @Override
    public List<PersistenceProvider> getPersistenceProviders() {
        return Collections.singletonList(persistenceProvider);
    }

    @Override
    public void clearCachedProviders() {
        persistenceProvider = new HibernatePersistenceProvider();
    }

    public static void register() {
        LOGGER.info("Registering HibernatePersistenceProviderResolver");
        PersistenceProviderResolverHolder.setPersistenceProviderResolver(new HibernatePersistenceProviderResolver());
    }
}
