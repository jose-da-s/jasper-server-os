/*
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
package com.jaspersoft.jasperserver.dto.resources.domain;

import com.jaspersoft.jasperserver.dto.adhoc.query.el.ClientExpressionContainer;
import com.jaspersoft.jasperserver.dto.common.DeepCloneable;
import com.jaspersoft.jasperserver.dto.resources.domain.validation.ValidEnumValueOrProfileAttribute;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.checkNotNull;
import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.copyOf;

/**
 * <p></p>
 *
 * @author Yaroslav.Kovalchyk
 * @version $Id$
 */
public class Join implements DeepCloneable<Join>, Serializable {
    public static final String ERROR_CODE_DOMAIN_SCHEMA_JOIN_TYPE_INVALID = "domain.schema.resources.join.type";
    @NotNull
    private String left;
    @NotNull
    private String right;
    @Valid
    private ClientExpressionContainer expression;
    private Integer weight;
    private String type;

    public Join(){}

    public Join(Join source){
        checkNotNull(source);

        left = source.getLeft();
        right = source.getRight();
        expression = copyOf(source.getExpression());
        weight = source.getWeight();
        type = source.getType();
    }

    public String getLeft() {
        return left;
    }

    public Join setLeft(String left) {
        this.left = left;
        return this;
    }

    public String getRight() {
        return right;
    }

    public Join setRight(String right) {
        this.right = right;
        return this;
    }

    public ClientExpressionContainer getExpression() {
        return expression;
    }

    public Join setExpression(ClientExpressionContainer expression) {
        this.expression = expression;
        return this;
    }

    public Integer getWeight() {
        return weight;
    }

    public Join setWeight(Integer weight) {
        this.weight = weight;
        return this;
    }
    @ValidEnumValueOrProfileAttribute(enumClass = JoinType.class, message = ERROR_CODE_DOMAIN_SCHEMA_JOIN_TYPE_INVALID)
    public String getType() {
        return type;
    }
    /**
     * Setter to set the type
     * @param type - one of [inner, leftOuter, rightOuter, fullOuter] or profile attribute placeholder
     * @return
     */
    public Join setType(String type) {
        this.type = type;
        return this;
    }

    @Override
    public Join deepClone() {
        return new Join(this);
    }

    public enum JoinType{inner, leftOuter, rightOuter, fullOuter}


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Join)) return false;

        Join join = (Join) o;

        if (expression != null ? !expression.equals(join.expression) : join.expression != null) return false;
        if (left != null ? !left.equals(join.left) : join.left != null) return false;
        if (right != null ? !right.equals(join.right) : join.right != null) return false;
        if (type != null ? !type.equals(join.type) : join.type != null) return false;
        if (weight != null ? !weight.equals(join.weight) : join.weight != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = left != null ? left.hashCode() : 0;
        result = 31 * result + (right != null ? right.hashCode() : 0);
        result = 31 * result + (expression != null ? expression.hashCode() : 0);
        result = 31 * result + (weight != null ? weight.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Join{" +
                "left='" + left + '\'' +
                ", right='" + right + '\'' +
                ", expression='" + expression + '\'' +
                ", weight=" + weight +
                ", type=" + type +
                '}';
    }
}
