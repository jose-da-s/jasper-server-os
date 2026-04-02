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
package com.jaspersoft.jasperserver.dto.resources;

import jakarta.validation.constraints.NotNull;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlElements;
import java.util.List;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.copyOf;

/**
 * <p></p>
 *
 * @author Yaroslav.Kovalchyk
 * @version $Id$
 */
public abstract class BaseSemanticLayerDataSource<T extends BaseSemanticLayerDataSource<T, S>, S> extends AbstractClientDataSourceHolder<T> implements ClientReferenceableDataSource, SchemaHolder<T, S> {
    public static final String SECURITY_FILE_ATTRIBUTE_NAME = "securityFile";
    private ClientReferenceableFile securityFile;
    private List<ClientBundle> bundles;

    public BaseSemanticLayerDataSource(BaseSemanticLayerDataSource other) {
        super(other);
        securityFile = copyOf(other.getSecurityFile());
        bundles = copyOf(other.getBundles());
    }

    public BaseSemanticLayerDataSource() {
    }

    @Override
    @NotNull
    public ClientReferenceableDataSource getDataSource() {
        return super.getDataSource();
    }


    @XmlElements({
            @XmlElement(name = "securityFileReference", type = ClientReference.class),
            @XmlElement(name = BaseSemanticLayerDataSource.SECURITY_FILE_ATTRIBUTE_NAME, type = ClientFile.class)
    })
    public ClientReferenceableFile getSecurityFile() {
        return securityFile;
    }

    public T setSecurityFile(ClientReferenceableFile securityFile) {
        this.securityFile = securityFile;
        return (T) this;
    }

    @XmlElementWrapper(name = "bundles")
    @XmlElement(name = "bundle")
    public List<ClientBundle> getBundles() {
        return bundles;
    }

    public T setBundles(List<ClientBundle> bundles) {
        this.bundles = bundles;
        return (T) this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BaseSemanticLayerDataSource)) return false;
        if (!super.equals(o)) return false;

        BaseSemanticLayerDataSource that = (BaseSemanticLayerDataSource) o;

        if (bundles != null ? !bundles.equals(that.bundles) : that.bundles != null) return false;
        if (securityFile != null ? !securityFile.equals(that.securityFile) : that.securityFile != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (securityFile != null ? securityFile.hashCode() : 0);
        result = 31 * result + (bundles != null ? bundles.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "AbstractSemanticLayerDataSource{" +
                "securityFile=" + securityFile +
                ", bundles=" + bundles +
                "} " + super.toString();
    }
}
