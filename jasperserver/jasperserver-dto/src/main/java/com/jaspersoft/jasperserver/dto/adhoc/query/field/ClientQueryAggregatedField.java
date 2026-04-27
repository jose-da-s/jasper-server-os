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
package com.jaspersoft.jasperserver.dto.adhoc.query.field;

import com.jaspersoft.jasperserver.dto.adhoc.datasource.ClientDataSourceField;
import com.jaspersoft.jasperserver.dto.adhoc.query.*;
import com.jaspersoft.jasperserver.dto.adhoc.query.ast.ClientQueryExpression;
import com.jaspersoft.jasperserver.dto.adhoc.query.ast.ClientQueryVisitor;
import com.jaspersoft.jasperserver.dto.adhoc.query.el.ClientExpressionContainer;
import com.jaspersoft.jasperserver.dto.adhoc.query.el.ClientVariable;
import com.jaspersoft.jasperserver.dto.adhoc.query.el.literal.*;
import com.jaspersoft.jasperserver.dto.adhoc.query.el.operator.ClientFunction;
import com.jaspersoft.jasperserver.dto.adhoc.query.el.operator.arithmetic.ClientAdd;
import com.jaspersoft.jasperserver.dto.adhoc.query.el.operator.arithmetic.ClientDivide;
import com.jaspersoft.jasperserver.dto.adhoc.query.el.operator.arithmetic.ClientMultiply;
import com.jaspersoft.jasperserver.dto.adhoc.query.el.operator.arithmetic.ClientSubtract;
import com.jaspersoft.jasperserver.dto.adhoc.query.el.operator.comparison.*;
import com.jaspersoft.jasperserver.dto.adhoc.query.el.operator.logical.ClientAnd;
import com.jaspersoft.jasperserver.dto.adhoc.query.el.operator.logical.ClientNot;
import com.jaspersoft.jasperserver.dto.adhoc.query.el.operator.logical.ClientOr;
import com.jaspersoft.jasperserver.dto.adhoc.query.el.operator.membership.ClientIn;
import com.jaspersoft.jasperserver.dto.adhoc.query.el.range.ClientRange;
import com.jaspersoft.jasperserver.dto.adhoc.query.validation.CheckAggregateDefinition;
import com.jaspersoft.jasperserver.dto.adhoc.query.validation.CheckExpressionType;
import com.jaspersoft.jasperserver.dto.adhoc.query.validation.NotEmpty;
import com.jaspersoft.jasperserver.dto.common.DeepCloneable;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;

import java.io.Serializable;

import static com.jaspersoft.jasperserver.dto.executions.QueryExecutionsErrorCode.Codes.QUERY_AGGREGATION_EXPRESSION_NOT_VALID;
import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.checkNotNull;
import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.copyOf;


/**
 * @author Andriy Godovanets
 */
@XmlRootElement
@CheckAggregateDefinition
public class ClientQueryAggregatedField implements ClientField, ClientAggregate, ClientFieldReference, ClientIdentifiable<String>,
        ClientQueryExpression, IExpressionContainer, DeepCloneable<ClientQueryAggregatedField>, Serializable {
    private String id;
    private String aggregateFunction;
    private String fieldExpression;
    private String aggregateFirstLevelFunction;


    @CheckExpressionType(message = QUERY_AGGREGATION_EXPRESSION_NOT_VALID,
            value = {
                    ClientAdd.class,
                    ClientAnd.class,
                    ClientBoolean.class,
                    ClientDate.class,
                    ClientNumber.class,
                    ClientDivide.class,
                    ClientEquals.class,
                    ClientFunction.class,
                    ClientGreater.class,
                    ClientGreaterOrEqual.class,
                    ClientIn.class,
                    ClientLess.class,
                    ClientLessOrEqual.class,
                    ClientMultiply.class,
                    ClientNot.class,
                    ClientNotEqual.class,
                    ClientNull.class,
                    ClientOr.class,
                    ClientRange.class,
                    ClientString.class,
                    ClientSubtract.class,
                    ClientTime.class,
                    ClientTimestamp.class,
                    ClientVariable.class
            })
    private ClientExpressionContainer expressionContainer;

    private String aggregateType;

    //TODO-Andriy Remove
    private String aggregateArg;

    @NotEmpty
    private String fieldRef;

    public ClientQueryAggregatedField() {
    }

    public ClientQueryAggregatedField(String field) {
        QueryPatternsUtil.NameAliasExpression nameAliasExpression = QueryPatternsUtil.parseNameAliasExpression(field);
        this.fieldRef = nameAliasExpression.name;
        this.id = nameAliasExpression.alias;
        if (nameAliasExpression.expression != null) {
            this.expressionContainer = new ClientExpressionContainer(nameAliasExpression.expression);
        }
    }

    public ClientQueryAggregatedField(ClientQueryAggregatedField field) {
        checkNotNull(field);

        id = field.getId();
        aggregateFunction = field.getAggregateFunction();
        aggregateFirstLevelFunction = field.getAggregateFirstLevelFunction();
        expressionContainer = copyOf(field.getExpressionContainer());
        aggregateType = field.getAggregateType();
        aggregateArg = field.getAggregateArg();
        fieldRef = field.getFieldReference();
    }

    @Override
    public String getId() {
        return id;
    }

    public ClientQueryAggregatedField setId(String id) {
        this.id = id;
        return this;
    }

    @Override
    @XmlTransient
    public String getName() {
        return fieldRef;
    }

    @Override
    @XmlElement(name = "type")
    public String getType() {
        return getAggregateType();
    }

    public ClientQueryAggregatedField setType(String type) {
        setAggregateType(type);
        return this;
    }

    @Override
    @XmlElement(name = "fieldRef")
    public String getFieldReference() {
        return fieldRef;
    }

    public ClientQueryAggregatedField setFieldReference(String fieldRef) {
        this.fieldRef = fieldRef;
        return this;
    }

    @Override
    @XmlElement(name = "functionName")
    public String getAggregateFunction() {
        return aggregateFunction;
    }

    public ClientQueryAggregatedField setAggregateFunction(String aggregateFunction) {
        this.aggregateFunction = aggregateFunction;
        return this;
    }

    @Override
    @XmlElement(name = "timeBalanceFunctionName")
    public String getAggregateFirstLevelFunction() {
        return aggregateFirstLevelFunction;
    }

    public ClientQueryAggregatedField setAggregateFirstLevelFunction(String aggregateFirstLevelFunction) {
        this.aggregateFirstLevelFunction = aggregateFirstLevelFunction;
        return this;
    }

    @Override
    @XmlTransient
    public String getAggregateExpression() {
        if (expressionContainer == null) return null;
        return expressionContainer.getString();
    }

    @Override
    public String getFieldExpression() {
        return fieldExpression;
    }

    @Override
    @XmlElement(name = "expression")
    public ClientExpressionContainer getExpressionContainer() {
        return expressionContainer;
    }

    @Override
    public ClientQueryAggregatedField setExpressionContainer(ClientExpressionContainer expressionContainer) {
        this.expressionContainer = expressionContainer;
        return this;
    }

    /**
     * {@link #setAggregateArg(String)}
     *
     * @return
     */
    @Deprecated
    @Override
    @XmlElement(name = "arg")
    public String getAggregateArg() {
        return aggregateArg;
    }

    /**
     * TODO-Andriy Bob suggested to remove it.
     * TODO-Andriy Because this field is used in case of function is WeightedAverage. We propose to make WeightedAverage available only if it's was defined in metadata as default function.
     * That means we cant specify it in the aggregate function but we will be able to use it as default or in custom expression
     *
     * @return
     */
    @Deprecated
    public ClientQueryAggregatedField setAggregateArg(String aggregateArg) {
        this.aggregateArg = aggregateArg;
        return this;
    }

    @Override
    @XmlTransient
    public String getAggregateType() {
        return aggregateType;
    }

    public ClientQueryAggregatedField setAggregateType(String aggregateType) {
        this.aggregateType = aggregateType;
        return this;
    }

    public ClientQueryAggregatedField setDataSourceField(ClientDataSourceField field) {
        setFieldReference(field.getName());
        return this;
    }

    /**
     * {@link #setAggregateArg(String)}
     *
     * @return
     */
    @Deprecated
    public String getSecondAggregateField() {
        return getAggregateArg();
    }

    @Override
    public void accept(ClientQueryVisitor visitor) {
        if (this.expressionContainer != null) {
            this.expressionContainer.getObject().accept(visitor);
        }
        visitor.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClientQueryAggregatedField that = (ClientQueryAggregatedField) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (aggregateFunction != null ? !aggregateFunction.equals(that.aggregateFunction) : that.aggregateFunction != null)
            return false;
        if (aggregateFirstLevelFunction != null ? !aggregateFirstLevelFunction.equals(that.aggregateFirstLevelFunction) : that.aggregateFirstLevelFunction != null) return false;
        if (expressionContainer != null ? !expressionContainer.equals(that.expressionContainer) : that.expressionContainer != null) {
            return false;
        }
        if (aggregateType != null ? !aggregateType.equals(that.aggregateType) : that.aggregateType != null) {
            return false;
        }
        if (aggregateArg != null ? !aggregateArg.equals(that.aggregateArg) : that.aggregateArg != null) return false;
        return !(fieldRef != null ? !fieldRef.equals(that.fieldRef) : that.fieldRef != null);

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (aggregateFunction != null ? aggregateFunction.hashCode() : 0);
        result = 31 * result + (aggregateFirstLevelFunction != null ? aggregateFirstLevelFunction.hashCode() : 0);
        result = 31 * result + (expressionContainer != null ? expressionContainer.hashCode() : 0);
        result = 31 * result + (aggregateType != null ? aggregateType.hashCode() : 0);
        result = 31 * result + (aggregateArg != null ? aggregateArg.hashCode() : 0);
        result = 31 * result + (fieldRef != null ? fieldRef.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ClientQueryAggregatedField{" +
                "id='" + id + '\'' +
                ", aggregateFunction='" + aggregateFunction + '\'' +
                ", aggregateFirstLevelFunction='" + aggregateFirstLevelFunction + '\'' +
                ", expression=" + expressionContainer +
                ", aggregateType='" + aggregateType + '\'' +
                ", aggregateArg='" + aggregateArg + '\'' +
                ", name='" + getName() + '\'' +
                '}';
    }

    @Override
    public ClientQueryAggregatedField deepClone() {
        return new ClientQueryAggregatedField(this);
    }
}
