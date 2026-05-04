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
package com.jaspersoft.jasperserver.dto.adhoc.query.el.operator;

import com.jaspersoft.jasperserver.dto.adhoc.query.el.ClientExpression;
import com.jaspersoft.jasperserver.dto.adhoc.query.el.ClientExpressions;
import com.jaspersoft.jasperserver.dto.adhoc.query.el.ClientOperator;
import com.jaspersoft.jasperserver.dto.adhoc.query.el.ast.ClientELVisitor;
import com.jaspersoft.jasperserver.dto.adhoc.query.el.operator.comparison.ClientEquals;
import com.jaspersoft.jasperserver.dto.adhoc.query.el.operator.comparison.ClientGreater;
import com.jaspersoft.jasperserver.dto.adhoc.query.el.operator.comparison.ClientGreaterOrEqual;
import com.jaspersoft.jasperserver.dto.adhoc.query.el.operator.comparison.ClientLess;
import com.jaspersoft.jasperserver.dto.adhoc.query.el.operator.comparison.ClientLessOrEqual;
import com.jaspersoft.jasperserver.dto.adhoc.query.el.operator.comparison.ClientNotEqual;
import com.jaspersoft.jasperserver.dto.adhoc.query.validation.CheckExpressionOperandsSize;

import jakarta.xml.bind.annotation.XmlTransient;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

/**
 * @author Stas Chubar <schubar@tibco.com>
 * @author Grant Bacon <gbacon@tibco.com>
 * @version $Id $
 */
public abstract class ClientComparison<F extends ClientComparison<F>> extends ClientOperator<F> {

    public ClientComparison() {
        super(ClientOperation.UNDEFINED);
    }

    protected ClientComparison(ClientOperation operation) {
        super(operation);
    }

    protected ClientComparison(ClientOperation operation, List<? extends ClientExpression> operands) {
        super(operation, operands);
    }

    protected ClientComparison(ClientOperation operation, List<? extends ClientExpression> operands, Boolean paren) {
        super(operation, operands, paren);
        setParen(paren);
    }

    protected ClientComparison(ClientComparison source) {
        super(source);
    }

    public static ClientComparison eq(ClientExpression lhs, ClientExpression rhs) {
        return new ClientEquals().setOperands(asList(lhs, rhs));
    }

    public static ClientGreaterOrEqual gtOrEq(ClientExpression lhs, ClientExpression rhs) {
        return new ClientGreaterOrEqual().setOperands(asList(lhs, rhs));
    }

    public static ClientLessOrEqual ltOrEq(ClientExpression lhs, ClientExpression rhs) {
        return new ClientLessOrEqual().setOperands(asList(lhs, rhs));
    }

    public static ClientGreater gt(ClientExpression lhs, ClientExpression rhs) {
        return new ClientGreater().setOperands(asList(lhs, rhs));
    }

    public static ClientComparison lt(ClientExpression lhs, ClientExpression rhs) {
        return new ClientLess().setOperands(asList(lhs, rhs));
    }

    public static ClientComparison notEq(ClientExpression lhs, ClientExpression rhs) {
        return new ClientNotEqual().setOperands(asList(lhs, rhs));
    }

    public static ClientComparison createComparison(String name, List<ClientExpression> operands) {

        ClientOperation operation = ClientOperation.fromString(name);
        if (operation != null) {
            ClientComparison comparison = null;

            switch (operation) {
                case GREATER:
                    comparison = new ClientGreater(operands);
                    break;
                case GREATER_OR_EQUAL:
                    comparison = new ClientGreaterOrEqual(operands);
                    break;
                case LESS:
                    comparison = new ClientLess(operands);
                    break;
                case LESS_OR_EQUAL:
                    comparison = new ClientLessOrEqual(operands);
                    break;
                case EQUALS:
                    comparison = new ClientEquals(operands);
                    break;
                case NOT_EQUAL:
                    comparison = new ClientNotEqual(operands);
                    break;
            }

            return comparison;
        }
        return null;
    }

    @CheckExpressionOperandsSize(min = 2, max = 2)
    @Override
    public List<ClientExpression> getOperands() {
        return super.getOperands();
    }

    @Override
    public F setOperands(List<ClientExpression> operands) {
        super.setOperands(operands);
        return (F) this;
    }

    /*
     * We have to calculate LHS and RHS on the fly because XMLMapper may use getOperands().add() to add new operands
     */
    @XmlTransient
    public ClientExpression getLhs() {
        return operands.size() > 0 ? operands.get(0) : null;
    }

    @XmlTransient
    public ClientExpression getRhs() {
        return operands.size() > 1 ? operands.get(1) : null;
    }

    @Override
    public void accept(ClientELVisitor visitor) {
        final List<ClientExpression> operands = getOperands();
        if (operands != null && !operands.isEmpty()) {
            operands.get(0).accept(visitor);
        }
        if (operands != null && operands.size() > 1) {
            operands.get(1).accept(visitor);
        }

    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        final List<ClientExpression> operands = getOperands();
        String lhsString = operands != null && !operands.isEmpty() ? operands.get(0).toString() : ClientExpressions.MISSING_REPRESENTATION;
        String rhsString = operands != null && operands.size() > 1 ? operands.get(1).toString() : ClientExpressions.MISSING_REPRESENTATION;

        if (hasParen()) {
            sb.append("(");
        }

        sb
                .append(lhsString)
                .append(" ")
                .append(getOperator().getDomelOperator())
                .append(" ")
                .append(rhsString);

        if (hasParen()) {
            sb.append(")");
        }
        return sb.toString();
    }


    public int getPrecedence() {
        return 3;
    }

}
