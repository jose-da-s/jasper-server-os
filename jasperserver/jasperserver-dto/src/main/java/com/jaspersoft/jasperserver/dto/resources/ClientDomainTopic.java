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
package com.jaspersoft.jasperserver.dto.resources;

import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * <p></p>
 *
 * @author Yaroslav.Kovalchyk
 * @version $Id: ClientDomainTopic.java 32880 2013-08-09 07:09:12Z inesterenko $
 */
@XmlRootElement(name = ResourceMediaType.DOMAIN_TOPIC_CLIENT_TYPE)
public class ClientDomainTopic extends AbstractClientReportUnit<ClientDomainTopic> {

    public ClientDomainTopic() {}

    public ClientDomainTopic(ClientDomainTopic other) {
        super(other);
    }

    @Override
    public ClientDomainTopic deepClone() {
        return new ClientDomainTopic(this);
    }
}
