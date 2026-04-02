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
 * @author Vlad Zavadskii <vzavadsk@tibco.com>
 * @version $Id$
 */
@XmlRootElement(name = ClientRelativeTimestampRange.EXPRESSION_ID)
public class ClientRelativeTimestampRange extends ClientLiteral<String, ClientRelativeTimestampRange> {

    public static final String EXPRESSION_ID = "relativeTimestampRange";

    public ClientRelativeTimestampRange() {
    }

    public ClientRelativeTimestampRange(String relativeRange) {
        setValue(relativeRange);
    }


    public ClientRelativeTimestampRange(ClientRelativeTimestampRange d) {
        super(d);
    }


    @Override
    public String getValue() {
        return value;
    }

    @Pattern(regexp = DomELCommonSimpleDateFormats.DATE_RANGE_PATTERN_STRING, message = "domel.relative.timestamp.range.value.invalid")
    public ClientRelativeTimestampRange setValue(String expression) {
        if (expression == null)
            throw new IllegalArgumentException("domel.relative.timestamp.range.value.invalid");
        this.value = expression;
        return this;
    }

    @Override
    public void accept(ClientELVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public ClientRelativeTimestampRange deepClone() {
        return new ClientRelativeTimestampRange(this);
    }
}
