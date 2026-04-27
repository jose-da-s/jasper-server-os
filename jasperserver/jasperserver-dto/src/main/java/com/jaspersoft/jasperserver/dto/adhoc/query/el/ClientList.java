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
package com.jaspersoft.jasperserver.dto.adhoc.query.el;

import com.jaspersoft.jasperserver.dto.adhoc.query.el.ast.ClientELVisitor;
import com.jaspersoft.jasperserver.dto.adhoc.query.el.literal.ClientBoolean;
import com.jaspersoft.jasperserver.dto.adhoc.query.el.literal.ClientDate;
import com.jaspersoft.jasperserver.dto.adhoc.query.el.literal.ClientNull;
import com.jaspersoft.jasperserver.dto.adhoc.query.el.literal.ClientNumber;
import com.jaspersoft.jasperserver.dto.adhoc.query.el.literal.ClientRelativeDateRange;
import com.jaspersoft.jasperserver.dto.adhoc.query.el.literal.ClientRelativeTimestampRange;
import com.jaspersoft.jasperserver.dto.adhoc.query.el.literal.ClientString;
import com.jaspersoft.jasperserver.dto.adhoc.query.el.literal.ClientTime;
import com.jaspersoft.jasperserver.dto.adhoc.query.el.literal.ClientTimestamp;
import com.jaspersoft.jasperserver.dto.adhoc.query.el.operator.ClientFunction;
import com.jaspersoft.jasperserver.dto.adhoc.query.el.operator.arithmetic.ClientAdd;
import com.jaspersoft.jasperserver.dto.adhoc.query.el.operator.arithmetic.ClientDivide;
import com.jaspersoft.jasperserver.dto.adhoc.query.el.operator.arithmetic.ClientMultiply;
import com.jaspersoft.jasperserver.dto.adhoc.query.el.operator.arithmetic.ClientPercentRatio;
import com.jaspersoft.jasperserver.dto.adhoc.query.el.operator.arithmetic.ClientSubtract;
import com.jaspersoft.jasperserver.dto.adhoc.query.el.operator.comparison.ClientEquals;
import com.jaspersoft.jasperserver.dto.adhoc.query.el.operator.comparison.ClientGreater;
import com.jaspersoft.jasperserver.dto.adhoc.query.el.operator.comparison.ClientGreaterOrEqual;
import com.jaspersoft.jasperserver.dto.adhoc.query.el.operator.comparison.ClientLess;
import com.jaspersoft.jasperserver.dto.adhoc.query.el.operator.comparison.ClientLessOrEqual;
import com.jaspersoft.jasperserver.dto.adhoc.query.el.operator.comparison.ClientNotEqual;
import com.jaspersoft.jasperserver.dto.adhoc.query.el.operator.logical.ClientAnd;
import com.jaspersoft.jasperserver.dto.adhoc.query.el.operator.logical.ClientNot;
import com.jaspersoft.jasperserver.dto.adhoc.query.el.operator.logical.ClientOr;
import com.jaspersoft.jasperserver.dto.adhoc.query.el.operator.membership.ClientIn;
import com.jaspersoft.jasperserver.dto.adhoc.query.el.range.ClientRange;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlElements;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.checkNotNull;
import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.copyOf;

/**
 * @author Stas Chubar <schubar@tibco.com>
 * @author Grant Bacon <gbacon@tibco.com>
 * @version $Id$
 */
@XmlRootElement(name = ClientList.EXPRESSION_ID)
@XmlAccessorType(XmlAccessType.PROPERTY)
public class ClientList implements ClientExpression<ClientList> {
    public static final String EXPRESSION_ID = "list";
    public List<ClientExpression> items;

    public ClientList() {
        this.items = new ArrayList<ClientExpression>();
    }

    public ClientList(List<ClientExpression> expressions) {
        this();
        if (expressions != null) {
            this.items = copyOf(expressions);
        }
    }

    public ClientList (ClientList source) {
        checkNotNull(source);
        this.items = copyOf(source.getItems());
    }

    public ClientList addItem(ClientExpression expr) {
        this.items.add(expr);
        return this;
    }

    public ClientList deleteItem(ClientExpression expr) {
        this.items.remove(expr);
        return this;
    }

    @Override
    public void accept(ClientELVisitor visitor) {
        if (items != null) {
            for (ClientExpression expr : items) {
                expr.accept(visitor);
            }
        }
        visitor.visit(this);
    }

    @XmlElementWrapper(name = "items")
    @XmlElements(value = {
            @XmlElement(name = ClientNull.EXPRESSION_ID,
                    type = ClientNull.class),
            @XmlElement(name = ClientBoolean.EXPRESSION_ID,
                    type = ClientBoolean.class),
            @XmlElement(name = ClientNumber.EXPRESSION_ID,
                    type = ClientNumber.class),
            @XmlElement(name = ClientRelativeDateRange.EXPRESSION_ID,
                    type = ClientRelativeDateRange.class),
            @XmlElement(name = ClientRelativeTimestampRange.EXPRESSION_ID,
                    type = ClientRelativeTimestampRange.class),
            @XmlElement(name = ClientString.EXPRESSION_ID,
                    type = ClientString.class),
            @XmlElement(name = ClientDate.EXPRESSION_ID,
                    type = ClientDate.class),
            @XmlElement(name = ClientTime.EXPRESSION_ID,
                    type = ClientTime.class),
            @XmlElement(name = ClientTimestamp.EXPRESSION_ID,
                    type = ClientTimestamp.class),
            @XmlElement(name = ClientVariable.EXPRESSION_ID,
                    type = ClientVariable.class),
            @XmlElement(name = ClientNot.EXPRESSION_ID,
                    type = ClientNot.class),
            @XmlElement(name = ClientAnd.EXPRESSION_ID,
                    type = ClientAnd.class),
            @XmlElement(name = ClientOr.EXPRESSION_ID,
                    type = ClientOr.class),
            @XmlElement(name = ClientGreater.EXPRESSION_ID,
                    type = ClientGreater.class),
            @XmlElement(name = ClientGreaterOrEqual.EXPRESSION_ID,
                    type = ClientGreaterOrEqual.class),
            @XmlElement(name = ClientLess.EXPRESSION_ID,
                    type = ClientLess.class),
            @XmlElement(name = ClientLessOrEqual.EXPRESSION_ID,
                    type = ClientLessOrEqual.class),
            @XmlElement(name = ClientNotEqual.EXPRESSION_ID,
                    type = ClientNotEqual.class),
            @XmlElement(name = ClientEquals.EXPRESSION_ID,
                    type = ClientEquals.class),
            @XmlElement(name = ClientFunction.EXPRESSION_ID,
                    type = ClientFunction.class),
            @XmlElement(name = ClientIn.EXPRESSION_ID,
                    type = ClientIn.class),
            @XmlElement(name = ClientRange.EXPRESSION_ID,
                    type = ClientRange.class),
            @XmlElement(name = ClientAdd.EXPRESSION_ID,
                    type = ClientAdd.class),
            @XmlElement(name = ClientSubtract.EXPRESSION_ID,
                    type = ClientSubtract.class),
            @XmlElement(name = ClientMultiply.EXPRESSION_ID,
                    type = ClientMultiply.class),
            @XmlElement(name = ClientDivide.EXPRESSION_ID,
                    type = ClientDivide.class),
            @XmlElement(name = ClientPercentRatio.EXPRESSION_ID,
                    type = ClientPercentRatio.class),
            @XmlElement(name = ClientList.EXPRESSION_ID,
                    type = ClientList.class)
    })
    public List<ClientExpression> getItems() {
        return items;
    }

    public ClientList setItems(List<ClientExpression> items) {
        this.items = items;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClientList that = (ClientList) o;

        return items != null ? items.equals(that.items) : that.items == null;
    }

    @Override
    public int hashCode() {
        return items != null ? items.hashCode() : 0;
    }

    @Override
    public String toString() {

        if (this.items == null || this.items.isEmpty()) {
            return "()";
        }

        StringBuilder sb = new StringBuilder("(");
        boolean isFirst = true;
        for (ClientExpression expression : this.items) {
            if(isFirst){
                isFirst = false;
            } else {
                sb.append(", ");
            }
            sb.append(expression.toString());
        }
        sb.append(")");
        return sb.toString();
    }

    @Override
    public ClientList deepClone() {
        return new ClientList(this);
    }
}
