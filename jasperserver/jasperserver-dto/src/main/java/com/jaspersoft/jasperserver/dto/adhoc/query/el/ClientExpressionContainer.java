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
package com.jaspersoft.jasperserver.dto.adhoc.query.el;

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
import com.jaspersoft.jasperserver.dto.adhoc.query.validation.ExpressionRepresentationRequired;
import com.jaspersoft.jasperserver.dto.common.DeepCloneable;

import jakarta.validation.Valid;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import java.io.Serializable;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.checkNotNull;
import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.copyOf;

/**
 * <p></p>
 *
 * @author yaroslav.kovalchyk
 * @version $Id$
 */
@ExpressionRepresentationRequired
@XmlRootElement(name = "expression")
public class ClientExpressionContainer implements DeepCloneable<ClientExpressionContainer>, Serializable {
    public static final String EXPRESSION_REPRESENTATION_PARAMETER_NAME = "expression";
    public static final String EXPRESSION_REPRESENTATION_STRING = "string";
    public static final String EXPRESSION_REPRESENTATION_OBJECT = "object";
    @Valid
    private ClientExpression object;
    private String string;

    public ClientExpressionContainer(){}

    public ClientExpressionContainer(ClientExpressionContainer source){
        checkNotNull(source);

        this.object = copyOf(source.getObject());
        this.string = source.getString();
    }

    public ClientExpressionContainer(String expression){
        this.string = expression;
    }

    public ClientExpressionContainer(ClientExpression expression){
        this.object = expression;
    }

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
    public ClientExpression getObject() {
        return object;
    }

    public ClientExpressionContainer setObject(ClientExpression expressionObject) {
        this.object = expressionObject;
        return this;
    }

    public String getString() {
        return string;
    }

    public ClientExpressionContainer setString(String expressionString) {
        this.string = expressionString;
        return this;
    }

    @XmlTransient
    public String getExpression(){
        return object != null ? object.toString() : string;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClientExpressionContainer)) return false;

        ClientExpressionContainer that = (ClientExpressionContainer) o;

        if (object != null ? !object.equals(that.object) : that.object != null)
        return false;
        return string != null ? string.equals(that.string) : that.string == null;

    }

    @Override
    public int hashCode() {
        int result = object != null ? object.hashCode() : 0;
        result = 31 * result + (string != null ? string.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ClientExpressionContainer{" +
                "object=" + object +
                ", string='" + string + '\'' +
                '}';
    }

    @Override
    public ClientExpressionContainer deepClone() {
        return new ClientExpressionContainer(this);
    }
}
