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
package com.jaspersoft.jasperserver.dto.adhoc.query.el.literal;

import com.jaspersoft.jasperserver.dto.adhoc.query.el.ClientLiteral;
import com.jaspersoft.jasperserver.dto.adhoc.query.el.ast.ClientELVisitor;

import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * @author Grant Bacon <gbacon@tibco.com>
 * @author Stas Chubar <schubar@tibco.com>
 * @version $Id$
 */
@XmlRootElement(name = ClientBoolean.EXPRESSION_ID)
public class ClientBoolean extends ClientLiteral<Boolean, ClientBoolean> {
    // literal type differs from java alias in case of numeric types. So, let's use own constats to avoid confusions
    public static final String EXPRESSION_ID = "boolean";

    public ClientBoolean() {
    }

    public ClientBoolean(Boolean booleanValue) {
        super(booleanValue);
    }

    public ClientBoolean(ClientBoolean literal) {
        super(literal);
    }


    @Override
    public ClientBoolean setValue(Boolean value) {
        this.value = value;
        return this;
    }

    @Override
    public Boolean getValue() {
        return value;
    }

    public static ClientBoolean valueOf(String string){
        return new ClientBoolean(Boolean.valueOf(string));
    }

    @Override
    public void accept(ClientELVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public ClientBoolean deepClone() {
        return new ClientBoolean(this);
    }
}
