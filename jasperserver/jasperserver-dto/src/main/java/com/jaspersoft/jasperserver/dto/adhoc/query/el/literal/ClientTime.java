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
package com.jaspersoft.jasperserver.dto.adhoc.query.el.literal;

import com.jaspersoft.jasperserver.dto.adhoc.query.adapter.ELTimeAdapter;
import com.jaspersoft.jasperserver.dto.adhoc.query.el.ClientExpressions;
import com.jaspersoft.jasperserver.dto.adhoc.query.el.ClientLiteral;
import com.jaspersoft.jasperserver.dto.adhoc.query.el.adapters.DomELCommonSimpleDateFormats;
import com.jaspersoft.jasperserver.dto.adhoc.query.el.ast.ClientELVisitor;

import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.sql.Time;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.regex.Pattern;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.checkNotNull;
import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.copyOf;

/**
 * @author Grant Bacon <gbacon@tibco.com>
 * @author Stas Chubar <schubar@tibco.com>
 * @version $Id$
 */
@XmlRootElement(name = ClientTime.EXPRESSION_ID)
public class ClientTime extends ClientLiteral<Time, ClientTime> {
    public static final String EXPRESSION_ID = "time";
    public static final DateFormat FORMATTER = DomELCommonSimpleDateFormats.timeFormat();

    public ClientTime() {
    }

    public ClientTime(Time time) {
        super(time);
    }

    public ClientTime(ClientTime source){
        checkNotNull(source);

        setValue(copyOf(source.getValue()));
    }

    @Override
    @XmlJavaTypeAdapter(ELTimeAdapter.class)
    public Time getValue() {
        return value;
    }

    @Override
    public ClientTime setValue(Time value) {
        this.value = value;
        return this;
    }

    public static ClientTime valueOf(String string){
        try {
            return new ClientTime().setValue(new Time(DomELCommonSimpleDateFormats.timeFormat()
                    .parse(string).getTime()));
        } catch (ParseException e) {
            throw new IllegalArgumentException("Unable to parse given value for time: " + string);
        }
    }

    @Override
    public ClientTime deepClone() {
        return new ClientTime(this);
    }

    @Override
    public String toString() {
        String valueString;
        Time value = getValue();
        try {
            valueString = (value != null) ? FORMATTER.format(value) : ClientExpressions.MISSING_REPRESENTATION;
        } catch (IllegalArgumentException e) {
            valueString = ClientExpressions.MISSING_REPRESENTATION;
        }
        // DomEL format removes milliseconds from time if they are all zero
        String zeroMilliseconds = "(\\.0+)$";
        if (valueString.contains(".") && Pattern.matches("[\\d\\s:]*" + zeroMilliseconds, valueString)) {
            valueString = valueString.substring(0, valueString.indexOf("."));
        }
        return "t'" + valueString + "'";
    }

    @Override
    public void accept(ClientELVisitor visitor) {
        visitor.visit(this);
    }

}
