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
import com.jaspersoft.jasperserver.dto.adhoc.query.el.adapters.DomELCommonSimpleDateFormats;
import com.jaspersoft.jasperserver.dto.adhoc.query.el.ast.ClientELVisitor;

import jakarta.validation.constraints.Pattern;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * @author Grant Bacon <gbacon@tibco.com>
 * @author Stas Chubar <schubar@tibco.com>
 * @version $Id$
 */
@XmlRootElement(name = ClientRelativeDateRange.EXPRESSION_ID)
public class ClientRelativeDateRange extends ClientLiteral<String, ClientRelativeDateRange> {
    public static final String EXPRESSION_ID = "relativeDateRange";

    public ClientRelativeDateRange() {
    }

    public ClientRelativeDateRange(String relativeRange) {
        setValue(relativeRange);
    }


    public ClientRelativeDateRange(ClientRelativeDateRange d) {
        super(d);
    }


    @Override
    public String getValue() {
        return value;
    }

    @Pattern(regexp = DomELCommonSimpleDateFormats.DATE_RANGE_PATTERN_STRING, message = "domel.relative.date.range.value.invalid")
    public ClientRelativeDateRange setValue(String expression) {
        this.value = expression;
        return this;
    }

    @Override
    public void accept(ClientELVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public ClientRelativeDateRange deepClone() {
        return new ClientRelativeDateRange(this);
    }
}
