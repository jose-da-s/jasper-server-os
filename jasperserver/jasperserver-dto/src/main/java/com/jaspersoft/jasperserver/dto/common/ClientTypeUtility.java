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

package com.jaspersoft.jasperserver.dto.common;

import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

/**
 * @author serhii.blazhyievskyi
 * @version $Id$
 */
public class ClientTypeUtility {
    public static String extractClientType(Class<?> clientObjectClass) {
        String clientResourceType = null;
        final XmlRootElement xmlRootElement = clientObjectClass.getAnnotation(XmlRootElement.class);
        if (xmlRootElement != null && !"##default".equals(xmlRootElement.name())) {
            clientResourceType = xmlRootElement.name();
        } else {
            final XmlType xmlType = clientObjectClass.getAnnotation(XmlType.class);
            if (xmlType != null && !"##default".equals(xmlType.name())) {
                clientResourceType = xmlType.name();
            }
        }
        if (clientResourceType == null) {
            final String classSimpleName = clientObjectClass.getSimpleName();
            clientResourceType = classSimpleName.replaceFirst("^.", classSimpleName.substring(0, 1).toLowerCase());
        }
        return clientResourceType;
    }

}
