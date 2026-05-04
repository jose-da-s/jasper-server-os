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
package com.jaspersoft.jasperserver.dto.adhoc.query.el.operator.logical;

import com.jaspersoft.jasperserver.dto.adhoc.query.el.ClientExpression;
import com.jaspersoft.jasperserver.dto.adhoc.query.el.ClientExpressions;
import com.jaspersoft.jasperserver.dto.adhoc.query.el.ClientOperator;
import com.jaspersoft.jasperserver.dto.adhoc.query.el.ClientVariable;
import com.jaspersoft.jasperserver.dto.adhoc.query.el.ast.ClientELVisitor;
import com.jaspersoft.jasperserver.dto.adhoc.query.el.literal.ClientBoolean;
import com.jaspersoft.jasperserver.dto.adhoc.query.el.operator.ClientOperation;

import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Stas Chubar <schubar@tibco.com>
 * @author Grant Bacon <gbacon@tibco.com>
 * @version $Id$
 */
@XmlRootElement(name = ClientNot.EXPRESSION_ID)
public class ClientNot extends ClientOperator<ClientNot> {
    public static final String EXPRESSION_ID = "not";

    public ClientNot() {
        super(ClientOperation.NOT);
    }

    public ClientNot(ClientNot source) {
        super(source);
    }

    public ClientNot(ClientExpression expression, Boolean paren) {
        // Not always has parens
        super(ClientOperation.NOT, true);
        this.operands.add(expression);
    }

    public ClientNot(ClientOperation operation, List<ClientExpression> operands) {
        super(operation, operands);
    }

    public ClientNot(ClientExpression expression) {
        this();
        addOperand(expression);
    }
    @Override
    public ClientNot addOperand(ClientExpression operand) {
        if(this.operands == null){
            this.operands = new ArrayList<ClientExpression>();
        }
        /*   To match ClientExpression with the Expression we make
        sure that when not is true then its operands have the paren flag unset.*/
        if(operand instanceof ClientOperator)
            this.operands.add(((ClientOperator) operand.deepClone()).unsetParen());
        else
            this.operands.add(operand);
        return this;
    }
    public static ClientNot not(ClientExpression expression) {
        if (expression == null) {
            throw new IllegalArgumentException("Error creating 'not' filter expression. Expression is null!");
        }
        if (!(expression instanceof ClientOperator)) {
            throw new IllegalArgumentException("Error creating 'not' filter expression. Expression is not operator!");
        }
        return new ClientNot(expression);
    }

    @XmlTransient
    public ClientExpression getOperand() {
        return getOperands() != null && !getOperands().isEmpty() ? this.getOperands().get(0) : null;
    }

    @Override
    @Size(min = 1, max = 1, message = DOMEL_INCORRECT_OPERANDS_COUNT)
    public List<ClientExpression> getOperands() {
        return operands;
    }

    @Override
    public String toString() {
        String operand = (getOperand() != null) ? getOperand().toString() : ClientExpressions.MISSING_REPRESENTATION;
        if(getOperand() instanceof ClientOperator) {
            return getOperator().getDomelOperator() + " (" + operand + ")";
        }
        return "("+ getOperator().getDomelOperator() +" "+ operand + ")";
    }

    @Override
    public void accept(ClientELVisitor visitor) {
        if (getOperands() != null && !getOperands().isEmpty()) {
            getOperand().accept(visitor);
        }
        visitor.visit(this);
    }

    public ClientNot setParen(Boolean paren) {
        // Setting boolean not permitted
        return this;
    }
    @Override
    public ClientNot setParen() {
        // Setting boolean not permitted
        return this;
    }

    @Override
    public ClientNot unsetParen() {
        // Setting boolean not permitted
        return this;
    }

    @Override
    public ClientNot deepClone() {
        return new ClientNot(this);
    }

    @Override
    public int getPrecedence() {
        return 2;
    }
}
