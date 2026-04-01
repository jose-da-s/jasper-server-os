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
package com.jaspersoft.jasperserver.dto.adhoc.query.el.operator.membership;

import com.jaspersoft.jasperserver.dto.adhoc.query.el.ClientExpression;
import com.jaspersoft.jasperserver.dto.adhoc.query.el.ClientExpressions;
import com.jaspersoft.jasperserver.dto.adhoc.query.el.ClientList;
import com.jaspersoft.jasperserver.dto.adhoc.query.el.ClientOperator;
import com.jaspersoft.jasperserver.dto.adhoc.query.el.ClientVariable;
import com.jaspersoft.jasperserver.dto.adhoc.query.el.ast.ClientELVisitor;
import com.jaspersoft.jasperserver.dto.adhoc.query.el.operator.ClientOperation;
import com.jaspersoft.jasperserver.dto.adhoc.query.el.range.ClientRange;
import com.jaspersoft.jasperserver.dto.adhoc.query.validation.InstanceOf;

import jakarta.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Grant Bacon <gbacon@tibco.com>
 * @author Stas Chubar <schubar@tibco.com>
 * @version $Id$
 */
@XmlRootElement(name = ClientIn.EXPRESSION_ID)
public class ClientIn extends ClientOperator<ClientIn> {

    public static final String EXPRESSION_ID = "in";

    public ClientIn() {
        super(ClientOperation.IN);
    }

    public ClientIn(ClientVariable variable, List<ClientExpression> list) {
        this(variable, (ClientExpression) new ClientList(list));
    }

    public ClientIn(ClientVariable variable, ClientRange range) {
        this(variable, (ClientExpression) range);
    }

    public ClientIn(ClientVariable variable, ClientList list) {
        this(variable, (ClientExpression) list);
    }

    public ClientIn(ClientVariable variable, ClientVariable inVariable) {
        this(variable, (ClientExpression) inVariable);
    }

    public ClientIn(ClientExpression lhs, ClientExpression rhs) {
        this();
        this.operands = new ArrayList<ClientExpression>(2);
        this.operands.add(lhs);
        this.operands.add(rhs);
        super.setOperands(this.operands);
    }

    public ClientIn(ClientIn source) {
        super(source);
    }

    @XmlTransient
    public ClientExpression getLhs() {
        return (operands != null && !operands.isEmpty()) ? this.operands.get(0) : null;
    }

    @InstanceOf(value = {ClientRange.class, ClientList.class, ClientVariable.class},
            message = "The second operand of an In expression may only be a 'list', 'range' or 'variable'",
            errorCode = "domel.in.rhs.type.invalid")
    @XmlTransient
    public ClientExpression getRhs() {
        if (this.operands != null && this.operands.size() > 1) {
            return this.operands.get(1);
        } else {
            return null;
        }
    }

    @Override
    @Size(min = 2, max = 2, message = DOMEL_INCORRECT_OPERANDS_COUNT)
    public List<ClientExpression> getOperands() {
        return operands;
    }






    @Override
    public String toString() {
        final boolean hasOperands = operands != null && !operands.isEmpty();
        String lhs = hasOperands && operands.get(0) != null ? operands.get(0).toString() :
                ClientExpressions.MISSING_REPRESENTATION;
        String rhs = hasOperands && operands.size() > 1 && operands.get(1) !=  null ? operands.get(1).toString() :
                ClientExpressions.MISSING_REPRESENTATION;
        if(hasParen())
            return "("+lhs + " " + this.getOperator().getDomelOperator() + " " + rhs+ ")";

        return lhs + " " + this.getOperator().getDomelOperator() + " " + rhs;
    }

    @Override
    public void accept(ClientELVisitor visitor) {
        if (getLhs() != null) {
            getLhs().accept(visitor);
        }

        if (getRhs() != null) {
            getRhs().accept(visitor);
        }
        visitor.visit(this);
    }

    @Override
    public ClientIn deepClone() {
        return new ClientIn(this);
    }

    public int getPrecedence() {
        return 3;
    }

}
