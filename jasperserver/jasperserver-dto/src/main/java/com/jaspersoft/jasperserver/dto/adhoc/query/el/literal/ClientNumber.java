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
import com.jaspersoft.jasperserver.dto.adhoc.query.el.adapters.NumberAdapter;
import com.jaspersoft.jasperserver.dto.adhoc.query.el.ast.ClientELVisitor;

import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.math.BigDecimal;


/**
 * @author Yaroslav Kovalchyk
 * @version $Id$
 */
@XmlRootElement(name = ClientNumber.EXPRESSION_ID)
public class ClientNumber extends ClientLiteral<Number, ClientNumber> {
    private static final NumberAdapter NUMBER_ADAPTER = new NumberAdapter();
    public static final String EXPRESSION_ID = "number";

    public ClientNumber() {
    }

    public ClientNumber(Number number) {
        super(number);
    }

    public ClientNumber(ClientNumber literal) {
        super(literal);
    }

    @Override
    @XmlJavaTypeAdapter(NumberAdapter.class)
    public Number getValue() {
        return value;
    }

    @Override
    public ClientNumber setValue(Number value) {
        this.value = value;
        return this;
    }
    @Override
    public void accept(ClientELVisitor visitor) {
        visitor.visit(this);
        super.accept(visitor);
    }

    @Override
    public ClientNumber deepClone() {
        return new ClientNumber(this);
    }

    @Override
    public String toString() {
        try {
            return new NumberAdapter().marshal(getValue());
        } catch (Exception e) {
            // should not happen
            throw new IllegalStateException(e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClientLiteral)) return false;

        ClientLiteral<?, ?> that = (ClientLiteral<?, ?>) o;

        final Object thatValue = that.getValue();
        try {
            if (this.value != null ?
                    !new BigDecimal(NUMBER_ADAPTER.marshal(getValue())).equals(thatValue != null ?
                            new BigDecimal(NUMBER_ADAPTER.marshal((Number) that.getValue())) : null) :
                    thatValue != null) return false;
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return value != null ? value.hashCode() : 0;
    }
}
