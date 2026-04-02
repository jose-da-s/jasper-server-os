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

import com.jaspersoft.jasperserver.dto.adhoc.query.el.ast.ClientELVisitor;
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
import com.jaspersoft.jasperserver.dto.adhoc.query.el.operator.ClientOperation;
import com.jaspersoft.jasperserver.dto.adhoc.query.el.operator.arithmetic.ClientAdd;
import com.jaspersoft.jasperserver.dto.adhoc.query.el.operator.arithmetic.ClientDivide;
import com.jaspersoft.jasperserver.dto.adhoc.query.el.operator.arithmetic.ClientMultiply;
import com.jaspersoft.jasperserver.dto.adhoc.query.el.operator.arithmetic.ClientPercentRatio;
import com.jaspersoft.jasperserver.dto.adhoc.query.el.operator.arithmetic.ClientSubtract;
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
import com.jaspersoft.jasperserver.dto.adhoc.query.el.range.ClientRange;
import com.jaspersoft.jasperserver.dto.adhoc.query.validation.CheckExpressionOperandsSize;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlElements;
import jakarta.xml.bind.annotation.XmlTransient;
import java.util.ArrayList;
import java.util.List;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.checkNotNull;
import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.copyOf;

/**
 * @author Stas Chubar <schubar@tibco.com>
 * @author Grant Bacon <gbacon@tibco.com>
 * @version $Id $
 */
public abstract class ClientOperator<F extends ClientOperator<F>> implements ClientExpression<F> {

    public static final String DOMEL_INCORRECT_OPERANDS_COUNT = "domel.incorrect.operands.count";
    private ClientOperation operator;

    @NotNull
    @Valid
    protected List<ClientExpression> operands = new ArrayList<ClientExpression>();
    protected Boolean paren;

    public ClientOperator() {
        this.operands = new ArrayList<ClientExpression>();
        this.operator = ClientOperation.UNDEFINED;
    }

    public ClientOperator(ClientOperation operator) {
        this.operator = operator;
    }

    public ClientOperator(ClientOperation operator, List<? extends ClientExpression> operands) {
        this(operator);
        this.operands = operands == null ? new ArrayList<ClientExpression>() : new ArrayList<ClientExpression>(operands);
    }

    /*
        This is specifically for use by copy constructors!
     */

    public ClientOperator(ClientOperation operator, Boolean paren) {
        this();
        this.operator = operator;
        this.paren = paren;
    }

    protected ClientOperator(ClientOperation operator, List<? extends ClientExpression> operands, Boolean paren) {
        this(operator);
        this.operands = copyOf(new ArrayList<ClientExpression>(operands));
        this.paren = paren;
    }

    @XmlTransient
    public String getOperatorName() {
        return operator.getDomelOperator();
    }

    /*
     * Copy constructor
     */
    protected ClientOperator(ClientOperator source) {
        checkNotNull(source);

        this.operator = source.operator;
        this.paren = source.paren;
        this.operands = copyOf(source.operands);
    }

    @XmlTransient
    public ClientOperation getOperator() {
        return operator;
    }

    @XmlElementWrapper(name = "operands")
    @XmlElements(value = {
            @XmlElement(name = ClientNull.EXPRESSION_ID,
                    type = ClientNull.class),
            @XmlElement(name = ClientBoolean.EXPRESSION_ID,
                    type = ClientBoolean.class),
            @XmlElement(name = ClientNumber.EXPRESSION_ID,
                    type = ClientNumber.class),
            @XmlElement(name = ClientRelativeDateRange.EXPRESSION_ID,
                    type = ClientRelativeDateRange.class),
            @XmlElement(name = ClientRelativeTimestampRange.EXPRESSION_ID,
                    type = ClientRelativeTimestampRange.class),
            @XmlElement(name = ClientString.EXPRESSION_ID,
                    type = ClientString.class),
            @XmlElement(name = ClientDate.EXPRESSION_ID,
                    type = ClientDate.class),
            @XmlElement(name = ClientTime.EXPRESSION_ID,
                    type = ClientTime.class),
            @XmlElement(name = ClientTimestamp.EXPRESSION_ID,
                    type = ClientTimestamp.class),
            @XmlElement(name = ClientVariable.EXPRESSION_ID,
                    type = ClientVariable.class),
            @XmlElement(name = ClientNot.EXPRESSION_ID,
                    type = ClientNot.class),
            @XmlElement(name = ClientAnd.EXPRESSION_ID,
                    type = ClientAnd.class),
            @XmlElement(name = ClientOr.EXPRESSION_ID,
                    type = ClientOr.class),
            @XmlElement(name = ClientGreater.EXPRESSION_ID,
                    type = ClientGreater.class),
            @XmlElement(name = ClientGreaterOrEqual.EXPRESSION_ID,
                    type = ClientGreaterOrEqual.class),
            @XmlElement(name = ClientLess.EXPRESSION_ID,
                    type = ClientLess.class),
            @XmlElement(name = ClientLessOrEqual.EXPRESSION_ID,
                    type = ClientLessOrEqual.class),
            @XmlElement(name = ClientNotEqual.EXPRESSION_ID,
                    type = ClientNotEqual.class),
            @XmlElement(name = ClientEquals.EXPRESSION_ID,
                    type = ClientEquals.class),
            @XmlElement(name = ClientFunction.EXPRESSION_ID,
                    type = ClientFunction.class),
            @XmlElement(name = ClientIn.EXPRESSION_ID,
                    type = ClientIn.class),
            @XmlElement(name = ClientRange.EXPRESSION_ID,
                    type = ClientRange.class),
            @XmlElement(name = ClientAdd.EXPRESSION_ID,
                    type = ClientAdd.class),
            @XmlElement(name = ClientSubtract.EXPRESSION_ID,
                    type = ClientSubtract.class),
            @XmlElement(name = ClientMultiply.EXPRESSION_ID,
                    type = ClientMultiply.class),
            @XmlElement(name = ClientDivide.EXPRESSION_ID,
                    type = ClientDivide.class),
            @XmlElement(name = ClientPercentRatio.EXPRESSION_ID,
                    type = ClientPercentRatio.class),
            @XmlElement(name = ClientList.EXPRESSION_ID,
                    type = ClientList.class)
    })

    public List<ClientExpression> getOperands() {
        return operands;
    }

    public F setOperands(List<ClientExpression> operands) {
        if (operands != null) {
            this.operands = new ArrayList<ClientExpression>();
            for (ClientExpression op : operands) {
                addOperand(op);
            }
        } else {
            this.operands = null;
        }
        return (F) this;
    }

    /**
     * When setting an operand, add parentheses if needed to maintain the correct structure of the expression
     * when converting it to a string.
     * @see Operator for an explanation of precedence
     * @param operand
     * @return
     */
    private ClientExpression setOperandParensIfNeeded(ClientExpression operand) {
        // only operators need parens
        if (! (operand instanceof ClientOperator)) {
            return operand;
        }
        ClientOperator op = (ClientOperator) operand;
        // already have parens, so you don't need to add them
        if (op.isParen() != null && op.isParen()) {
            return op;
        }
        // If precedence of operand is lower than this operator, you need parentheses.
        // Clone the operand first, then set parens.
        if (operandNeedsParens(op)) {
            op = (ClientOperator)op.deepClone();
            op.setParen();
        }
        return op;
    }

    protected boolean operandNeedsParens(ClientOperator operand)
    {
        return operand.getPrecedence() < getPrecedence();
    }

    public F addOperand(ClientExpression operand) {
        if(this.operands == null){
            this.operands = new ArrayList<ClientExpression>();
        }
        this.operands.add(setOperandParensIfNeeded(operand));
        return (F) this;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClientOperator<?> that = (ClientOperator<?>) o;

        if (operator != null ? !operator.equals(that.operator) : that.operator != null) return false;
        if (operands != null ? !operands.equals(that.operands) : that.operands != null) return false;
        return paren != null ? paren.equals(that.paren) : that.paren == null;
    }

    @Override
    public int hashCode() {
        int result = operator != null ? operator.hashCode() : 0;
        result = 31 * result + (operands != null ? operands.hashCode() : 0);
        result = 31 * result + (paren != null ? paren.hashCode() : 0);
        return result;
    }

    @Override
    public void accept(ClientELVisitor visitor) {
        visitor.visit(this);

        if (getOperands() != null) {
            for (ClientExpression expr : getOperands()) {
                if (expr != null) {
                    expr.accept(visitor);
                }
            }
        }
    }

    public boolean hasParen() {
        return paren != null && paren;
    }

    @XmlElement(name = "paren")
    public Boolean isParen() {
        return paren;
    }

    public F setParen(Boolean paren) {
        if(paren != null && paren)
            this.paren = paren;
        else
            this.unsetParen(); //set paren to null when paren is false, to avoid serialization.
        return (F) this;
    }

    public F setParen() {
        this.paren = Boolean.TRUE;
        return (F) this;
    }

    /**
     * We set paren to null, to avoid serialization.
     *
     * @return
     */
    public F unsetParen() {
        paren = null;
        return (F) this;
    }

    protected String addStringOperand(ClientExpression operand) {
        StringBuilder result = new StringBuilder();
        result.append(operand == null ? ClientExpressions.MISSING_REPRESENTATION : operand.toString());
        return result.toString();
    }


    protected String operandsToString(List<ClientExpression> operands, String separator) {
        StringBuilder sb = new StringBuilder();
        for (ClientExpression operand : operands) {
            sb.append(addStringOperand(operand));
            sb.append(separator);
        }
        int indexOfLastSeparator = sb.lastIndexOf(separator);
        if (indexOfLastSeparator != -1) {
            sb.replace(indexOfLastSeparator, indexOfLastSeparator + separator.length(), "");
        }
        return sb.toString();
    }
    @XmlTransient
    public abstract int getPrecedence();
}
