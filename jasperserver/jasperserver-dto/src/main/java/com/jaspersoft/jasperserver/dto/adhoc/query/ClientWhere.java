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
package com.jaspersoft.jasperserver.dto.adhoc.query;

import com.jaspersoft.jasperserver.dto.adhoc.query.ast.ClientQueryClause;
import com.jaspersoft.jasperserver.dto.adhoc.query.ast.ClientQueryVisitor;
import com.jaspersoft.jasperserver.dto.adhoc.query.el.ClientExpression;
import com.jaspersoft.jasperserver.dto.adhoc.query.el.ClientExpressionContainer;
import com.jaspersoft.jasperserver.dto.adhoc.query.el.ClientList;
import com.jaspersoft.jasperserver.dto.adhoc.query.el.ParameterExpressionsMapXmlAdapter;
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
import com.jaspersoft.jasperserver.dto.adhoc.query.validation.CheckExpressionType;
import com.jaspersoft.jasperserver.dto.adhoc.query.validation.CheckParametersExpressionContainer;
import com.jaspersoft.jasperserver.dto.adhoc.query.validation.ParameterMap;
import com.jaspersoft.jasperserver.dto.common.DeepCloneable;

import jakarta.validation.Valid;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.Serializable;
import java.util.Map;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.checkNotNull;
import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.copyOf;

/**
 * Created by stas on 4/8/15.
 */
@XmlRootElement(name = "where")
public class ClientWhere implements ClientQueryClause, DeepCloneable<ClientWhere>, Serializable {

    @CheckExpressionType(
            value = {
                    ClientNot.class,
                    ClientAnd.class,
                    ClientOr.class,
                    ClientGreater.class,
                    ClientGreaterOrEqual.class,
                    ClientLess.class,
                    ClientLessOrEqual.class,
                    ClientNotEqual.class,
                    ClientEquals.class,
                    ClientFunction.class,
                    ClientIn.class,
                    ClientList.class
    })
    @Valid
    private ClientExpressionContainer filterExpression;

    @ParameterMap
    @CheckParametersExpressionContainer(
            value = {
                    ClientNull.class,
                    ClientBoolean.class,
                    ClientNumber.class,
                    ClientString.class,
                    ClientDate.class,
                    ClientTime.class,
                    ClientTimestamp.class,
                    ClientList.class,
                    ClientRelativeDateRange.class,
                    ClientRelativeTimestampRange.class})
    @Valid
    private Map<String, ClientExpressionContainer> parameters;

    public ClientWhere() {

    }

    public ClientWhere(ClientWhere where) {
        checkNotNull(where);

        filterExpression = copyOf(where.getFilterExpression());
        parameters = copyOf(where.getParameters());
    }

    public ClientWhere(ClientExpressionContainer container) {
        this.filterExpression = container;
    }

    public ClientWhere(ClientExpression filters) {
        if (filters != null) {
            this.filterExpression = new ClientExpressionContainer().setObject(filters);
        }
    }

    public ClientWhere(String expression) {
        if (expression != null) {
            this.filterExpression = new ClientExpressionContainer().setString(expression);
        }
    }

    public ClientWhere(Map<String, ClientExpressionContainer> parameters) {
        this.parameters = parameters;
    }

    public ClientWhere(ClientExpression filters, Map<String, ClientExpressionContainer> parameters) {
        if (filters != null) {
            this.filterExpression = new ClientExpressionContainer().setObject(filters);
        }
        this.parameters = parameters;
    }

    public ClientExpressionContainer getFilterExpression() {
        return filterExpression;
    }

    public ClientWhere setFilterExpression(ClientExpressionContainer filterExpression) {
        this.filterExpression = filterExpression;
        return this;
    }

    @XmlJavaTypeAdapter(ParameterExpressionsMapXmlAdapter.class)
    public Map<String, ClientExpressionContainer> getParameters() {
        return parameters;
    }

    @ParameterMap
    public ClientWhere setParameters(Map<String, ClientExpressionContainer> parameters) {
        this.parameters = parameters;
        return this;
    }

    @Override
    public void accept(ClientQueryVisitor visitor) {
        if (this.filterExpression != null && this.filterExpression.getObject() != null) {
            this.filterExpression.getObject().accept(visitor);
        }
        visitor.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClientWhere that = (ClientWhere) o;

        if (filterExpression != null ? !filterExpression.equals(that.filterExpression) : that.filterExpression != null)
            return false;
        return !(parameters != null ? !parameters.equals(that.parameters) : that.parameters != null);

    }

    @Override
    public int hashCode() {
        int result = filterExpression != null ? filterExpression.hashCode() : 0;
        result = 31 * result + (parameters != null ? parameters.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ClientWhere{" +
                "filterExpression=" + filterExpression +
                ", parameters=" + parameters +
                '}';
    }

    @Override
    public ClientWhere deepClone() {
        return new ClientWhere(this);
    }
}
