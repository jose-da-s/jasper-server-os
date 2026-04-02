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
package com.jaspersoft.jasperserver.dto.reports.inputcontrols;

import com.jaspersoft.jasperserver.dto.common.DeepCloneable;
import com.jaspersoft.jasperserver.dto.common.validations.DateTimeFormatValidationRule;
import com.jaspersoft.jasperserver.dto.common.validations.MandatoryValidationRule;
import com.jaspersoft.jasperserver.dto.common.validations.RangeValidationRule;
import com.jaspersoft.jasperserver.dto.common.validations.RegexpValidationRule;
import com.jaspersoft.jasperserver.dto.common.validations.ValidationRule;
import com.jaspersoft.jasperserver.dto.resources.ClientDataType;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlElements;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.checkNotNull;
import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.copyOf;

/**
 * @author akasych
 * @version $Id$
 */
@XmlRootElement
public class ReportInputControl implements DeepCloneable<ReportInputControl> {
    private String id;
    private String description;
    private String type;
    private String uri;
    private String label;
    private Boolean mandatory;
    private Boolean readOnly;
    private Boolean  visible;
    private List<String> masterDependencies = new ArrayList<String>();
    private List<String> slaveDependencies = new ArrayList<String>();
    private List<ValidationRule> validationRules;
    private InputControlState state;
    private ClientDataType dataType;

    public ReportInputControl(ReportInputControl other) {
        checkNotNull(other);

        this.id = other.getId();
        this.description = other.getDescription();
        this.type = other.getType();
        this.uri = other.getUri();
        this.label = other.getLabel();
        this.mandatory = other.getMandatory();
        this.readOnly = other.getReadOnly();
        this.visible = other.getVisible();
        this.masterDependencies = copyOf(other.getMasterDependencies());
        this.slaveDependencies = copyOf(other.getSlaveDependencies());
        this.validationRules = copyOf(other.getValidationRules());
        this.state = copyOf(other.getState());
        this.dataType = copyOf(other.getDataType());
    }

    public ReportInputControl() {
    }

    public ClientDataType getDataType() {
        return dataType;
    }

    public ReportInputControl setDataType(ClientDataType dataType) {
        // reset generic resource fields
        this.dataType = dataType == null ? null : new ClientDataType(dataType).setCreationDate(null)
                .setDescription(null).setLabel(null).setPermissionMask(null).setUpdateDate(null).setUri(null)
                .setVersion(null);
        return this;
    }

    public InputControlState getState() {
        return state;
    }

    public ReportInputControl setState(InputControlState state) {
        this.state = state;
        return this;
    }

    public ReportInputControl setMasterDependencies(List<String> masterDependencies) {
        this.masterDependencies = masterDependencies == null ? new ArrayList<String>() : masterDependencies;
        return this;
    }

    public ReportInputControl setSlaveDependencies(List<String> slaveDependencies) {
        this.slaveDependencies = slaveDependencies == null ? new ArrayList<String>() : slaveDependencies;
        return this;
    }

    @XmlElementWrapper(name = "validationRules")
    @XmlElements({
            @XmlElement(name = "rangeValidationRule", type = RangeValidationRule.class),
            @XmlElement(name = "regexpValidationRule", type = RegexpValidationRule.class),
            @XmlElement(name = "dateTimeFormatValidationRule", type = DateTimeFormatValidationRule.class),
            @XmlElement(name = "mandatoryValidationRule", type = MandatoryValidationRule.class)
    })
    public List<ValidationRule> getValidationRules() {
        return validationRules;
    }

    public ReportInputControl setValidationRules(List<ValidationRule> validationRules) {
        this.validationRules = validationRules;
        return this;
    }

    public String getType() {
        return type;
    }

    public ReportInputControl setType(String type) {
        this.type = type;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public ReportInputControl setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getUri() {
        return uri;
    }

    public ReportInputControl setUri(String uri) {
        this.uri = uri;
        return this;
    }

    public String getLabel() {
        return label;
    }

    public ReportInputControl setLabel(String label) {
        this.label = label;
        return this;
    }

    public Boolean getMandatory() {
        return mandatory;
    }

    public ReportInputControl setMandatory(Boolean mandatory) {
        this.mandatory = mandatory;
        return this;
    }

    public Boolean getReadOnly() {
        return readOnly;
    }

    public ReportInputControl setReadOnly(Boolean readOnly) {
        this.readOnly = readOnly;
        return this;
    }

    public Boolean getVisible() {
        return visible;
    }

    public ReportInputControl setVisible(Boolean visible) {
        this.visible = visible;
        return this;
    }

    @XmlElementWrapper(name = "masterDependencies")
    @XmlElements(@XmlElement(name = "controlId"))
    public List<String> getMasterDependencies() {
        return masterDependencies;
    }

    @XmlElementWrapper(name = "slaveDependencies")
    @XmlElements(@XmlElement(name = "controlId"))
    public List<String> getSlaveDependencies() {
        return slaveDependencies;
    }

    public String getId() {
        return id;
    }

    public ReportInputControl setId(String id) {
        this.id = id;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ReportInputControl)) return false;

        ReportInputControl that = (ReportInputControl) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (description != null ? !description.equals(that.description) : that.description != null) return false;
        if (type != null ? !type.equals(that.type) : that.type != null) return false;
        if (uri != null ? !uri.equals(that.uri) : that.uri != null) return false;
        if (label != null ? !label.equals(that.label) : that.label != null) return false;
        if (mandatory != null ? !mandatory.equals(that.mandatory) : that.mandatory != null) return false;
        if (readOnly != null ? !readOnly.equals(that.readOnly) : that.readOnly != null) return false;
        if (visible != null ? !visible.equals(that.visible) : that.visible != null) return false;
        if (!masterDependencies.equals(that.masterDependencies))
            return false;
        if (!slaveDependencies.equals(that.slaveDependencies))
            return false;
        if (validationRules != null ? !validationRules.equals(that.validationRules) : that.validationRules != null)
            return false;
        if (state != null ? !state.equals(that.state) : that.state != null) return false;
        return dataType != null ? dataType.equals(that.dataType) : that.dataType == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (uri != null ? uri.hashCode() : 0);
        result = 31 * result + (label != null ? label.hashCode() : 0);
        result = 31 * result + (mandatory != null ? mandatory.hashCode() : 0);
        result = 31 * result + (readOnly != null ? readOnly.hashCode() : 0);
        result = 31 * result + (visible != null ? visible.hashCode() : 0);
        result = 31 * result + (masterDependencies.hashCode());
        result = 31 * result + (slaveDependencies.hashCode());
        result = 31 * result + (validationRules != null ? validationRules.hashCode() : 0);
        result = 31 * result + (state != null ? state.hashCode() : 0);
        result = 31 * result + (dataType != null ? dataType.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ReportInputControl{" +
                "id='" + id + '\'' +
                ", description='" + description + '\'' +
                ", type='" + type + '\'' +
                ", uri='" + uri + '\'' +
                ", label='" + label + '\'' +
                ", mandatory=" + mandatory +
                ", readOnly=" + readOnly +
                ", visible=" + visible +
                ", masterDependencies=" + masterDependencies +
                ", slaveDependencies=" + slaveDependencies +
                ", validationRules=" + validationRules +
                ", state=" + state +
                ", dataType=" + dataType +
                '}';
    }

    @Override
    public ReportInputControl deepClone() {
        return new ReportInputControl(this);
    }
}
