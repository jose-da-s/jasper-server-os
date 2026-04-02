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

import com.jaspersoft.jasperserver.dto.adhoc.query.adapter.ELTimestampAdapter;
import com.jaspersoft.jasperserver.dto.adhoc.query.el.ClientExpressions;
import com.jaspersoft.jasperserver.dto.adhoc.query.el.ClientLiteral;
import com.jaspersoft.jasperserver.dto.adhoc.query.el.ast.ClientELVisitor;

import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.sql.Timestamp;
import java.util.regex.Pattern;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.checkNotNull;
import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.copyOf;

/**
 * @author Grant Bacon <gbacon@tibco.com>
 * @author Stas Chubar <schubar@tibco.com>
 * @version $Id$
 */
@XmlRootElement(name = ClientTimestamp.EXPRESSION_ID)
public class ClientTimestamp extends ClientLiteral<Timestamp, ClientTimestamp> {
    public static final String EXPRESSION_ID = "timestamp";
    private static final ELTimestampAdapter TIMESTAMP_ADAPTER = new ELTimestampAdapter();


    public ClientTimestamp() {
    }

    public ClientTimestamp(Timestamp timestamp) {
        super(timestamp);
    }

    public ClientTimestamp(ClientTimestamp t) {
        checkNotNull(t);

        setValue(copyOf(t.getValue()));
    }

    @Override
    @XmlJavaTypeAdapter(ELTimestampAdapter.class)
    public Timestamp getValue() {
        return value;
    }

    @Override
    public ClientTimestamp setValue(Timestamp value) {
        this.value = value;
        return this;
    }

    @Override
    public ClientTimestamp deepClone() {
        return new ClientTimestamp(this);
    }

    public static ClientTimestamp valueOf(String string){
        final Timestamp timestamp;
        try {
            timestamp = TIMESTAMP_ADAPTER.unmarshal(string);
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException)e;
            } else {
                throw new RuntimeException(e);
            }
        }
        return new ClientTimestamp().setValue(timestamp);
    }

    /**
     * DomEL displays timestamps in its own format. It trims milliseconds as well as time if all values
     * are zero (e.g: 00:00:00.000, or 00:00:00). This may not be an optimal approach; however, I think it is important
     * to have parity between the toString() representations for each model regardless of their implementation or
     * representation in their serialized forms.
     *
     * Thus, this toString() implementation mimics the behaviour found in TimestampLiteral and
     * TimestampWithMillisecondsLiteral (datarator-el).
     *
     * @link com.jaspersoft.commons.dataset.expr.TimestampLiteral#format(boolean)
     */
    @Override
    public String toString() {

        final String resultFormat;
        String formattedValue;
        final Timestamp value = getValue();
        try {
            formattedValue = TIMESTAMP_ADAPTER.marshal(value);
        } catch (Exception e) {
            formattedValue = ClientExpressions.MISSING_REPRESENTATION;
        }

        String preciselyMidnightRegex = "(00:?){3}(\\.0+)?$";
        // This matches any date that ends with 00:00:00 or 00:00:00.0 (or .00, .000...)
        if (Pattern.matches("[\\d-T\\s]*" + preciselyMidnightRegex, formattedValue)) {
            // If we find precise midnight, we will remove it and any leading T or space character
            // commonly found in ISO8601 or the format used by DomEL
            resultFormat = formattedValue.replaceFirst("[\\sT]" + preciselyMidnightRegex, "");

        // If the time is not 00:00:00, but we do have .000 milliseconds, lets make sure to shave them off as DomEL does
        } else if (formattedValue.contains(".") && Pattern.matches("[\\d-T\\s:]*(\\.0+)?$", formattedValue)) {
            resultFormat = formattedValue.substring(0, formattedValue.indexOf("."));
        } else {
            resultFormat = formattedValue;
        }
        return "ts'" + resultFormat + "'";
    }

    @Override
    public void accept(ClientELVisitor visitor) {
        visitor.visit(this);
    }

}
