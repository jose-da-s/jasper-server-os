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
package com.jaspersoft.jasperserver.dto.reports;

import com.jaspersoft.jasperserver.dto.common.DeepCloneable;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.checkNotNull;
import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.copyOf;

/**
 * <p></p>
 *
 * @author Yaroslav.Kovalchyk
 * @version $Id$
 */
@XmlRootElement
public class ReportParameter implements DeepCloneable<ReportParameter>, Serializable {
    private String name;
    private String limit;
    private String offset;
    private String select;
    private String criteria;

    private List<String> values = new LinkedList<String>();

    public ReportParameter(ReportParameter other) {
        checkNotNull(other);

        this.name = other.getName();
        this.limit = other.getLimit();
        this.offset = other.getOffset();
        this.select = other.getSelect();
        this.criteria = other.getCriteria();
        this.values = copyOf(other.getValues());
    }

    public ReportParameter() {
    }

    @XmlAttribute(required = true)
	@Schema(
			description = "The parameter name.", 
			required=true,
			example = "someParameterName"
			)
    public String getName() {
        return name;
    }

    public ReportParameter setName(String name) {
        this.name = name;
        return this;
    }

    public String getSelect() {
        return select;
    }

    public ReportParameter setSelect(String select) {
        this.select = select;
        return this;
    }



    public String getLimit() {
        return limit;
    }

    public ReportParameter setLimit(String limit) {
        this.limit = limit;
        return this;
    }



    public String getOffset() {
        return offset;
    }

    public ReportParameter setOffset(String offset) {
        this.offset = offset;
        return this;
    }



    public String getCriteria() {
        return criteria;
    }

    public ReportParameter setCriteria(String criteria) {
        this.criteria = criteria;
        return this;
    }


    @XmlElement(name = "value")
    @ArraySchema(schema = @Schema(
    		name = "value",
    		description = "Specifies the list of values for this parameter.", 
    		example = "someParameterValue"))    
    public List<String> getValues() {
        return values;
    }

    public ReportParameter setValues(List<String> values) {
        this.values = values == null ? new LinkedList<String>() : values;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ReportParameter that = (ReportParameter) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (limit != null ? !limit.equals(that.limit) : that.limit != null) return false;
        if (select != null ? !select.equals(that.select) : that.select != null) return false;
        if (offset != null ? !offset.equals(that.offset) : that.offset != null) return false;
        if (criteria != null ? !criteria.equals(that.criteria) : that.criteria != null) return false;
        if (!new HashSet(values).equals(new HashSet(that.values))) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (limit != null ? limit.hashCode() : 0);
        result = 31 * result + (select != null ? select.hashCode() : 0);
        result = 31 * result + (offset != null ? offset.hashCode() : 0);
        result = 31 * result + (criteria != null ? criteria.hashCode() : 0);
        result = 31 * result + new HashSet(values).hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "ReportParameter{" +
                "name='" + name + '\'' +
                "limit='" + limit + '\'' +
                "offset='" + offset + '\'' +
                "select='" + select + '\'' +
                "criteria='" + criteria + '\'' +
                ", values=" + values +
                '}';
    }

    /*
     * DeepCloneable
     */

    @Override
    public ReportParameter deepClone() {
        return new ReportParameter(this);
    }
}
