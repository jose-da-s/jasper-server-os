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

import jakarta.validation.Valid;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.copyOf;

/**
 * <p></p>
 *
 * @author Yaroslav.Kovalchyk
 * @version $Id$
 */
public abstract class AbstractResourceSingleElement<T extends AbstractResourceSingleElement<T>> extends ResourceElement<T> {
    private String type;
    @Valid
    private ClientExpressionContainer expression;

    public AbstractResourceSingleElement() {
    }

    public AbstractResourceSingleElement(AbstractResourceSingleElement source) {
        super(source);
        type = source.getType();
        expression = copyOf(source.getExpression());
    }

    public String getType() {
        return type;
    }

    public T setType(String type) {
        this.type = type;
        return (T) this;
    }
    public ClientExpressionContainer getExpression() {
        return expression;
    }

    public T setExpression(ClientExpressionContainer expression) {
        this.expression = expression;
        return (T) this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractResourceSingleElement)) return false;
        if (!super.equals(o)) return false;

        AbstractResourceSingleElement that = (AbstractResourceSingleElement) o;

        if (expression != null ? !expression.equals(that.expression) : that.expression != null) return false;
        if (type != null ? !type.equals(that.type) : that.type != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (expression != null ? expression.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "AbstractResourceSingleElement{" +
                "type='" + type + '\'' +
                ", expression='" + expression + '\'' +
                "} " + super.toString();
    }
}
