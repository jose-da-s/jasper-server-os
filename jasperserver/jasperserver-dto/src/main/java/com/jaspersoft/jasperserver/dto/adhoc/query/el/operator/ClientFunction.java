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

import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Stas Chubar <schubar@tibco.com>
 * @author Grant Bacon <gbacon@tibco.com>
 * @version $Id$
 */
@XmlRootElement(name = ClientFunction.EXPRESSION_ID)
public class ClientFunction extends ClientOperator<ClientFunction> {

    public static final String EXPRESSION_ID = "function";
    protected String functionName;

    public ClientFunction() {
        super(ClientOperation.FUNCTION, new ArrayList<ClientExpression>());
    }

    public ClientFunction(String functionName, List<ClientExpression> operands) {
        super(ClientOperation.FUNCTION, operands);
        this.functionName = functionName;
    }

    public ClientFunction(ClientFunction source){
        super(source);
        this.functionName = source.getFunctionName();
    }

    public String getFunctionName() {
        return functionName;
    }

    public ClientFunction(String functionName)
    {
        this();
        this.functionName = functionName;
    }

    public ClientFunction setFunctionName(String functionName) {
        this.functionName = functionName;
        return this;
    }

    public ClientFunction addArgument(ClientExpression expr) {
        this.operands.add(expr);
        return this;
    }

    @Override
    public String toString() {
        final String functionNameString = (functionName != null) ? functionName : ClientExpressions.MISSING_REPRESENTATION;
        return functionNameString + "(" + operandsToString(getOperands(), ", ") + ")";
    }

    @Override
    protected String addStringOperand(ClientExpression operand) {
        return operand.toString();
    }

    @Override
    public void accept(ClientELVisitor visitor) {
        super.accept(visitor);
        visitor.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClientFunction)) return false;
        if (!super.equals(o)) return false;

        ClientFunction that = (ClientFunction) o;

        return functionName != null ? functionName.equals(that.functionName) : that.functionName == null;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (functionName != null ? functionName.hashCode() : 0);
        return result;
    }

    @Override
    public ClientFunction deepClone() {
        return new ClientFunction(this);
    }


    public int getPrecedence() {
        return 6;
    }
    /*
     * Operands for ClientFunctions never need to be surrounded by parentheses
     */
    @Override
    protected boolean operandNeedsParens(ClientOperator operand) {
        return false;
    }
}
