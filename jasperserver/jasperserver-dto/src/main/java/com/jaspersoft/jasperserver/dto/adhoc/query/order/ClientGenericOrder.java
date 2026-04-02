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
package com.jaspersoft.jasperserver.dto.adhoc.query.order;


import com.jaspersoft.jasperserver.dto.adhoc.query.ClientField;
import com.jaspersoft.jasperserver.dto.adhoc.query.ClientFieldReference;
import com.jaspersoft.jasperserver.dto.adhoc.query.IExpressionContainer;
import com.jaspersoft.jasperserver.dto.adhoc.query.QueryPatternsUtil;
import com.jaspersoft.jasperserver.dto.adhoc.query.el.ClientExpressionContainer;
import com.jaspersoft.jasperserver.dto.adhoc.query.validation.CheckGenericOrderFieldReference;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlTransient;

import java.io.Serializable;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.checkNotNull;
import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.copyOf;

/**
 * @author Andriy Godovanets
 */
@CheckGenericOrderFieldReference
public class ClientGenericOrder implements ClientOrder, ClientFieldReference, ClientField, IExpressionContainer, Serializable {
    public static final Boolean IS_AGGREGATION_DEFAULT = false;

    private Boolean isAscending = true;
    private Boolean isAggregationLevel = IS_AGGREGATION_DEFAULT;
    private String type;
    private String fieldRef;

    private ClientExpressionContainer expressionContainer;

    public ClientGenericOrder() {
        // no op
    }

    public ClientGenericOrder(String orderExpression) {
        QueryPatternsUtil.NameExpressionOrder nameExpressionOrder = QueryPatternsUtil.parseNameExpressionOrder(orderExpression);
        this.fieldRef = nameExpressionOrder.name;

        if (nameExpressionOrder.expression != null) {
            this.expressionContainer = new ClientExpressionContainer(nameExpressionOrder.expression);

        }
        if ("DESC".equals(nameExpressionOrder.order)) {
            isAscending = false;
        }
    }

    public ClientGenericOrder(ClientGenericOrder sorting) {
        checkNotNull(sorting);

        this
                .setAscending(sorting.isAscending())
                .setFieldReference(sorting.getFieldReference())
                .setAggregation(sorting.isAggregationLevel());
        expressionContainer = copyOf(sorting.getExpressionContainer());
    }

    @Override
    public ClientGenericOrder deepClone() {
        return new ClientGenericOrder(this);
    }

    @Override
    public Boolean isAscending() {
        return isAscending;
    }

    public ClientGenericOrder setAscending(Boolean isAscending) {
        this.isAscending = isAscending;
        return this;
    }
    /**
     * @return Query Field Identifier or Metadata Field Name to order by
     */
    @XmlElement(name = "fieldRef")
    public String getFieldReference() {
        return fieldRef;
    }

    public ClientGenericOrder setFieldReference(String fieldReference) {
        this.fieldRef = fieldReference;
        return this;
    }

    /**
     * Since name of the measure level can vary, we've added separate property
     * to distinguish Measure level amongst others
     *
     * Default value: false
     *
     * @return true, if Measures level is sorted
     */
    @XmlTransient
    public Boolean isAggregationLevel() {
        return isAggregationLevel;
    }

    @XmlElement(name = "aggregation")
    public Boolean isAggregation() {
        return isAggregationLevel == IS_AGGREGATION_DEFAULT ? null : isAggregationLevel;
    }

    public ClientGenericOrder setAggregation(Boolean aggregationLevel) {
        isAggregationLevel = aggregationLevel;
        return this;
    }

    @Override
    @XmlTransient
    public String getFieldExpression() {
        if (expressionContainer == null) return null;
        return expressionContainer.getString();
    }

    @Override
    @XmlTransient
    public String getType() {
        return type;
    }

    @Override
    @XmlTransient
    public String getName() {
        return fieldRef;
    }


    @Override
    @XmlElement(name = "expression")
    public ClientExpressionContainer getExpressionContainer() {
        return expressionContainer;
    }

    @Override
    public ClientGenericOrder setExpressionContainer(ClientExpressionContainer expressionContainer) {
        this.expressionContainer = expressionContainer;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClientGenericOrder)) return false;

        ClientGenericOrder that = (ClientGenericOrder) o;

        if (isAscending != null ? !isAscending.equals(that.isAscending) : that.isAscending != null) return false;
        if (isAggregationLevel != null ? !isAggregationLevel.equals(that.isAggregationLevel) : that.isAggregationLevel != null)
            return false;
        if (expressionContainer != null ? !expressionContainer.equals(that.expressionContainer) : that.expressionContainer != null) {
            return false;
        }
        return fieldRef != null ? fieldRef.equals(that.fieldRef) : that.fieldRef == null;
    }

    @Override
    public int hashCode() {
        int result = isAscending != null ? isAscending.hashCode() : 0;
        result = 31 * result + (isAggregationLevel != null ? isAggregationLevel.hashCode() : 0);
        result = 31 * result + (fieldRef != null ? fieldRef.hashCode() : 0);
        result = 31 * result + (expressionContainer != null ? expressionContainer.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ClientGenericOrder{");
        sb.append("isAscending=").append(isAscending);
        sb.append(", isAggregationLevel=").append(isAggregationLevel);
        sb.append(", fieldReference='").append(fieldRef).append('\'');
        sb.append(", expression=").append(expressionContainer);
        sb.append('}');
        return sb.toString();
    }
}
