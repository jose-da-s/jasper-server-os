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

package com.jaspersoft.jasperserver.dto.adhoc.query.el.range;

import com.jaspersoft.jasperserver.dto.adhoc.query.el.ClientExpression;
import com.jaspersoft.jasperserver.dto.adhoc.query.el.ClientExpressions;
import com.jaspersoft.jasperserver.dto.adhoc.query.el.ClientLiteral;
import com.jaspersoft.jasperserver.dto.adhoc.query.el.ast.ClientELVisitor;

import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.checkNotNull;
import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.copyOf;

/**
 *
 * @version $Id$
 */
@XmlRootElement(name = ClientRange.EXPRESSION_ID)
@XmlType(propOrder = {"start", "end"})
public class ClientRange implements ClientExpression<ClientRange> {
    public static final String EXPRESSION_ID = "range";

    private ClientRangeBoundary start;
    private ClientRangeBoundary end;

    public ClientRange() {
    }

    public ClientRange(ClientRangeBoundary start, ClientRangeBoundary end) {
        this.start = new ClientRangeBoundary(start);
        this.end = new ClientRangeBoundary(end);
    }

    public ClientRange(ClientLiteral start, ClientLiteral end) {
        this(new ClientRangeBoundary(start), new ClientRangeBoundary(end));
    }

    public ClientRange(ClientExpression start, ClientExpression end) {
        this(new ClientRangeBoundary(start), new ClientRangeBoundary(end));
    }

    public ClientRange(ClientRange range) {
        checkNotNull(range);

        this.start = copyOf(range.getStart());
        this.end = copyOf(range.getEnd());
    }

    public ClientRangeBoundary getEnd() {
        return end;
    }

    public ClientRange setEnd(ClientRangeBoundary end) {
        this.end = end;
        return this;
    }

    public ClientRangeBoundary getStart() {
        return start;
    }

    public ClientRange setStart(ClientRangeBoundary start) {
        this.start = start;
        return this;
    }

    @Override
    public void accept(ClientELVisitor visitor) {
        if (start != null && start.getBoundary() != null) {
            start.getBoundary().accept(visitor);
        }
        if (end != null && end.getBoundary() != null) {
            end.getBoundary().accept(visitor);
        }
        visitor.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClientRange that = (ClientRange) o;

        if (start != null ? !start.equals(that.start) : that.start != null) return false;
        return end != null ? end.equals(that.end) : that.end == null;
    }

    @Override
    public int hashCode() {
        int result = start != null ? start.hashCode() : 0;
        result = 31 * result + (end != null ? end.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        String startString;
        String endString;
        if (getStart() != null && getStart().getBoundary() != null) {
            startString = getStart().getBoundary().toString();
        } else {
            startString = ClientExpressions.MISSING_REPRESENTATION;
        }
        if (getEnd() != null && getEnd().getBoundary() != null) {
            endString = getEnd().getBoundary().toString();
        } else {
            endString = ClientExpressions.MISSING_REPRESENTATION;
        }
        return "(" + startString + ":" + endString + ")";
    }

    @Override
    public ClientRange deepClone() {
        return new ClientRange(this);
    }
}
