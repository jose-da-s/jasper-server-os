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
package com.jaspersoft.jasperserver.dto.discovery;

import com.jaspersoft.jasperserver.dto.common.DeepCloneable;

import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.List;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.checkNotNull;
import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.copyOf;

/**
 * <p></p>
 *
 * @author Zakhar Tomchenko
 * @version $Id: $
 */
@XmlRootElement(name = "visualizationMetadata")
public class VisualizationMetadata implements DeepCloneable<VisualizationMetadata> {
    private List<Parameter> parameters;
    private List<Parameter> outputParameters;
    private String repositoryType;
    private String uri;

    public VisualizationMetadata() {}

    public VisualizationMetadata(VisualizationMetadata other) {
        checkNotNull(other);

        parameters = copyOf(other.getParameters());
        outputParameters = copyOf(other.getOutputParameters());
        repositoryType = other.repositoryType;
        uri = other.uri;
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    public VisualizationMetadata setParameters(List<Parameter> parameters) {
        this.parameters = parameters;
        return this;
    }

    public List<Parameter> getOutputParameters() {
        return outputParameters;
    }

    public VisualizationMetadata setOutputParameters(List<Parameter> outputParameters) {
        this.outputParameters = outputParameters;
        return this;
    }

    public String getRepositoryType() {
        return repositoryType;
    }

    public VisualizationMetadata setRepositoryType(String repositoryType) {
        this.repositoryType = repositoryType;
        return this;
    }

    public String getUri() {
        return uri;
    }

    public VisualizationMetadata setUri(String uri) {
        this.uri = uri;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VisualizationMetadata that = (VisualizationMetadata) o;

        if (outputParameters != null ? !outputParameters.equals(that.outputParameters) : that.outputParameters != null)
            return false;
        if (parameters != null ? !parameters.equals(that.parameters) : that.parameters != null) return false;
        if (repositoryType != null ? !repositoryType.equals(that.repositoryType) : that.repositoryType != null)
            return false;
        return uri != null ? uri.equals(that.uri) : that.uri == null;
    }

    @Override
    public int hashCode() {
        int result = parameters != null ? parameters.hashCode() : 0;
        result = 31 * result + (outputParameters != null ? outputParameters.hashCode() : 0);
        result = 31 * result + (repositoryType != null ? repositoryType.hashCode() : 0);
        result = 31 * result + (uri != null ? uri.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "VisualizationMetadata{" +
                "parameters=" + parameters +
                ", outputParameters=" + outputParameters +
                ", repositoryType='" + repositoryType + '\'' +
                ", uri='" + uri + '\'' +
                '}';
    }

    /*
     * DeepCloneable
     */

    @Override
    public VisualizationMetadata deepClone() {
        return new VisualizationMetadata(this);
    }
}

