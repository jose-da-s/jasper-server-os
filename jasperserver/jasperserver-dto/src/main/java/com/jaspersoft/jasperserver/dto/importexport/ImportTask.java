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
import java.util.List;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.checkNotNull;
import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.copyOf;

/**
 * Import Task Dto
 * @author askorodumov
 * @version $Id: ImportTaskDto.java 58382 2015-10-07 11:36:07Z vzavadsk $
 */
@XmlRootElement(name = "import")
public class ImportTask implements DeepCloneable<ImportTask> {

    private String organization;
    private String brokenDependencies;
    private List<String> parameters;
    private String keyAlias;
    private String secretKey;
    private String secretUri;

    public ImportTask() {
    }

    public ImportTask(ImportTask other) {
        checkNotNull(other);

        this.organization = other.getOrganization();
        this.brokenDependencies = other.getBrokenDependencies();
        this.parameters = copyOf(other.getParameters());
        this.keyAlias = other.getKeyAlias();
        this.secretKey = other.getSecretKey();
        this.secretUri = other.getSecretUri();
    }

    @XmlElement(name = "organization")
    public String getOrganization() {
        return organization;
    }

    public ImportTask setOrganization(String organization) {
        this.organization = organization;
        return this;
    }

    @XmlElement(name = "brokenDependencies")
    public String getBrokenDependencies() {
        return brokenDependencies;
    }

    public ImportTask setBrokenDependencies(String brokenDependencies) {
        this.brokenDependencies = brokenDependencies;
        return this;
    }

    @XmlElementWrapper(name = "parameters")
    @XmlElement(name = "parameter")
    public List<String> getParameters() {
        return parameters;
    }

    public ImportTask setParameters(List<String> parameters) {
        this.parameters = parameters;
        return this;
    }

    @XmlElement(name = "keyAlias")
    public String getKeyAlias() {
        return keyAlias;
    }

    public ImportTask setKeyAlias(String keyAlias) {
        this.keyAlias = keyAlias;
        return this;
    }

    @XmlElement(name = "secretKey")
    public String getSecretKey() {
        return secretKey;
    }

    public ImportTask setSecretKey(String secretKey) {
        this.secretKey = secretKey;
        return this;
    }

    @XmlElement(name = "secretUri")
    public String getSecretUri() {
        return secretUri;
    }

    public ImportTask setSecretUri(String secretUri) {
        this.secretUri = secretUri;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ImportTask)) return false;

        ImportTask that = (ImportTask) o;

        if (getOrganization() != null ? !getOrganization().equals(that.getOrganization()) : that.getOrganization() != null)
            return false;
        if (keyAlias != null ? !keyAlias.equals(that.getKeyAlias()) : that.getKeyAlias() != null)
            return false;
        if (secretKey != null ? !secretKey.equals(that.getSecretKey()) : that.getSecretKey() != null)
            return false;
        if (secretUri != null ? !secretUri.equals(that.getSecretUri()) : that.getSecretUri() != null)
            return false;
        if (getBrokenDependencies() != null ? !getBrokenDependencies().equals(that.getBrokenDependencies()) : that.getBrokenDependencies() != null)
            return false;
        return !(getParameters() != null ? !getParameters().equals(that.getParameters()) : that.getParameters() != null);

    }

    @Override
    public int hashCode() {
        int result = getOrganization() != null ? getOrganization().hashCode() : 0;
        result = 31 * result + (getBrokenDependencies() != null ? getBrokenDependencies().hashCode() : 0);
        result = 31 * result + (getParameters() != null ? getParameters().hashCode() : 0);
        result = 31 * result + (keyAlias != null ? keyAlias.hashCode() : 0);
        result = 31 * result + (secretKey != null ? secretKey.hashCode() : 0);
        result = 31 * result + (secretUri != null ? secretUri.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ImportTask{" +
                "organization='" + organization + '\'' +
                ", brokenDependencies='" + brokenDependencies + '\'' +
                ", parameters=" + parameters +
                ", keyAlias='" + keyAlias + '\'' +
                ", secretUri='" + secretUri + '\'' +
                '}';
    }

    /*
     * DeepCloneable
     */

    @Override
    public ImportTask deepClone() {
        return new ImportTask(this);
    }
}
