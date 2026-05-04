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
package com.jaspersoft.jasperserver.dto.importexport;

import com.jaspersoft.jasperserver.dto.common.DeepCloneable;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import java.util.List;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.checkNotNull;
import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.copyOf;

/**
 * @author: Zakhar.Tomchenco
 */
@XmlRootElement(name = "export")
public class ExportTask implements DeepCloneable<ExportTask> {

    private List<String> exportParams;
    private List<String> urisOfResources;
    private List<String> rolesToExport;
    private List<String> usersToExport;
    private List<String> resourceTypes;
    private String organization;
    private String keyAlias;

    @XmlTransient
    private List<String> urisOfScheduledJobs;

    public ExportTask() {
    }

    public ExportTask(ExportTask other) {
        checkNotNull(other);

        this.exportParams = copyOf(other.getParameters());
        this.urisOfResources = copyOf(other.getUris());
        this.urisOfScheduledJobs = copyOf(other.getScheduledJobs());
        this.rolesToExport = copyOf(other.getRoles());
        this.usersToExport = copyOf(other.getUsers());
        this.resourceTypes = copyOf(other.getResourceTypes());
        this.organization = other.getOrganization();
        this.keyAlias = other.getKeyAlias();
    }

    @XmlElementWrapper(name = "parameters")
    @XmlElement(name = "parameter")
    public List<String> getParameters() {
        return exportParams;
    }

    public ExportTask setParameters(List<String> exportParams) {
        this.exportParams = exportParams;
        return this;
    }

    @XmlElementWrapper(name = "uris")
    @XmlElement(name = "uri")
    public List<String> getUris() {
        return urisOfResources;
    }

    public ExportTask setUris(List<String> urisOfResources) {
        this.urisOfResources = urisOfResources;
        return this;
    }

    public List<String> getScheduledJobs() {
        return urisOfScheduledJobs;
    }

    public ExportTask setScheduledJobs(List<String> urisOfScheduledJobs) {
        this.urisOfScheduledJobs = urisOfScheduledJobs;
        return this;
    }

    @XmlElementWrapper(name = "roles")
    @XmlElement(name = "role")
    public List<String> getRoles() {
        return rolesToExport;
    }

    public ExportTask setRoles(List<String> rolesToExport) {
        this.rolesToExport = rolesToExport;
        return this;
    }

    @XmlElementWrapper(name = "users")
    @XmlElement(name = "user")
    public List<String> getUsers() {
        return usersToExport;
    }

    public ExportTask setUsers(List<String> usersToExport) {
        this.usersToExport = usersToExport;
        return this;
    }

    @XmlElement(name = "organization")
    public String getOrganization() {
        return organization;
    }

    public ExportTask setOrganization(String organization) {
        this.organization = organization;
        return this;
    }

    @XmlElementWrapper(name = "resourceTypes")
    @XmlElement(name = "resourceType")
    public List<String> getResourceTypes() {
        return resourceTypes;
    }

    public ExportTask setResourceTypes(List<String> resourceTypes) {
        this.resourceTypes = resourceTypes;
        return this;
    }

    @XmlElement(name = "keyAlias")
    public String getKeyAlias() {
        return keyAlias;
    }

    public ExportTask setKeyAlias(String keyAlias) {
        this.keyAlias = keyAlias;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExportTask)) return false;

        ExportTask that = (ExportTask) o;

        if (exportParams != null ? !exportParams.equals(that.exportParams) : that.exportParams != null) return false;
        if (keyAlias != null ? !keyAlias.equals(that.keyAlias) : that.keyAlias != null)
            return false;
        if (urisOfResources != null ? !urisOfResources.equals(that.urisOfResources) : that.urisOfResources != null)
            return false;
        if (urisOfScheduledJobs != null ? !urisOfScheduledJobs.equals(that.urisOfScheduledJobs) : that.urisOfScheduledJobs != null)
            return false;
        if (rolesToExport != null ? !rolesToExport.equals(that.rolesToExport) : that.rolesToExport != null)
            return false;
        if (usersToExport != null ? !usersToExport.equals(that.usersToExport) : that.usersToExport != null)
            return false;
        if (getResourceTypes() != null ? !getResourceTypes().equals(that.getResourceTypes()) : that.getResourceTypes() != null)
            return false;
        return !(getOrganization() != null ? !getOrganization().equals(that.getOrganization()) : that.getOrganization() != null);

    }

    @Override
    public int hashCode() {
        int result = exportParams != null ? exportParams.hashCode() : 0;
        result = 31 * result + (urisOfResources != null ? urisOfResources.hashCode() : 0);
        result = 31 * result + (urisOfScheduledJobs != null ? urisOfScheduledJobs.hashCode() : 0);
        result = 31 * result + (rolesToExport != null ? rolesToExport.hashCode() : 0);
        result = 31 * result + (usersToExport != null ? usersToExport.hashCode() : 0);
        result = 31 * result + (getResourceTypes() != null ? getResourceTypes().hashCode() : 0);
        result = 31 * result + (getOrganization() != null ? getOrganization().hashCode() : 0);
        result = 31 * result + (keyAlias != null ? keyAlias.hashCode() : 0);

        return result;
    }

    @Override
    public String toString() {
        return "ExportTask{" +
                "exportParams=" + exportParams +
                ", urisOfResources=" + urisOfResources +
                ", urisOfScheduledJobs=" + urisOfScheduledJobs +
                ", rolesToExport=" + rolesToExport +
                ", usersToExport=" + usersToExport +
                ", resourceTypes=" + resourceTypes +
                ", organization='" + organization + '\'' +
                ", keyAlias='" + keyAlias + '\'' +
                '}';
    }

    /*
     * DeepCloneable
     */

    @Override
    public ExportTask deepClone() {
        return new ExportTask(this);
    }
}
