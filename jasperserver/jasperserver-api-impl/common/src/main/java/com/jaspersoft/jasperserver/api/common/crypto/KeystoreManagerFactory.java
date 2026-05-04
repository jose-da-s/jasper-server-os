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
package com.jaspersoft.jasperserver.api.common.crypto;

import com.jaspersoft.jasperserver.crypto.KeystoreManager;
import org.springframework.beans.BeanInstantiationException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

import static java.lang.System.getProperty;

/**
 * This factory tris to get instance of {@code KeystoreManager} and
 * fails application initialization if {@code KeystoreManager} is not initialized.
 */
@Lazy
@Component("keystoreManager")
public class KeystoreManagerFactory implements FactoryBean<KeystoreManager> {

    /**
     * Location of keystore properties file
     */
    private KeystoreManager keystoreManager;

    @PostConstruct
    public void init() {
        try {
            keystoreManager = KeystoreManager.getInstance();
        } catch (Exception e) {
            throw new BeanInstantiationException(KeystoreManager.class, "Please make sure that `create-keystore` was executed", e);
        }
    }

    @Override
    public KeystoreManager getObject() throws Exception {
        return keystoreManager;
    }

    @Override
    public Class<?> getObjectType() {
        return KeystoreManager.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}

