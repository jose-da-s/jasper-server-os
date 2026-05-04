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
package com.jaspersoft.jasperserver.api.common.properties;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * PropertyChanger
 *
 * This helper class is used to avoid implementation of methods that are not used
 *
 * @author scubar
 */
public abstract class PropertyChangerAdapter implements PropertyChanger {

    protected Map<String, String> defaults;

    @PostConstruct
    public void init() {
        this.defaults = new HashMap<String, String>();
        this.defaults.putAll(getProperties());
    }

    @Override
    public void setProperty(String key, String val) { }

    @Override
    public String getProperty(String key) { return null; }

    @Override
    public void removeProperty(String key, String val) {
        if (defaults.containsKey(key)) {
            setProperty(key, defaults.get(key));
        }
    }

    @Override
    public Map<String, String> getProperties() {return null;}

}

