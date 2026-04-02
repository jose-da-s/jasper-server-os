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

import com.jaspersoft.jasperserver.dto.common.DeepCloneable;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

import java.io.Serializable;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.checkNotNull;
import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.copyOf;

/**
 * @author Volodya Sabadosh
 * @version $Id$
 */
@XmlType(propOrder = {"name", "expression"})
public class ClientParameterExpression implements DeepCloneable<ClientParameterExpression>, Serializable {
    private String name;
    private ClientExpressionContainer expression;

    public ClientParameterExpression() {

    }

    public ClientParameterExpression(String name, ClientExpressionContainer expression) {
        this.name = name;
        this.expression = expression;
    }

    public ClientParameterExpression(ClientParameterExpression other) {
        checkNotNull(other);

        this.name = other.getName();
        this.expression = copyOf(other.getExpression());
    }

    @XmlElement(name="name")
    public String getName() {
        return name;
    }

    public ClientParameterExpression setName(String name) {
        this.name = name;
        return this;
    }

    public ClientExpressionContainer getExpression() {
        return expression;
    }

    public ClientParameterExpression setExpression(ClientExpressionContainer expression) {
        this.expression = expression;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClientParameterExpression)) return false;

        ClientParameterExpression that = (ClientParameterExpression) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        return expression != null ? expression.equals(that.expression) : that.expression == null;

    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (expression != null ? expression.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ClientParameterExpression{" +
                "name='" + name + '\'' +
                ", expression=" + expression +
                '}';
    }

    @Override
    public ClientParameterExpression deepClone() {
        return new ClientParameterExpression(this);
    }
}
