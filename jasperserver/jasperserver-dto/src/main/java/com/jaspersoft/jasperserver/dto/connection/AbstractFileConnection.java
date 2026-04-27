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
package com.jaspersoft.jasperserver.dto.connection;

import com.jaspersoft.jasperserver.dto.common.DeepCloneable;
import com.jaspersoft.jasperserver.dto.common.ResourceLocation;
import com.jaspersoft.jasperserver.dto.resources.ClientReference;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElements;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.checkNotNull;
import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.copyOf;

/**
 * <p></p>
 *
 * @author yaroslav.kovalchyk
 * @version $Id$
 */
public class AbstractFileConnection<BuilderType extends AbstractFileConnection> implements DeepCloneable<AbstractFileConnection> {
    @XmlElement(name = "hasHeaderLine")
    private boolean hasHeaderLine;
    private ResourceLocation location;

    public AbstractFileConnection() {
    }

    public AbstractFileConnection(AbstractFileConnection source) {
        checkNotNull(source);

        hasHeaderLine = source.hasHeaderLine();
        location = copyOf(source.getLocation());
    }

    public boolean hasHeaderLine() {
        return hasHeaderLine;
    }

    // unchecked cast to BuilderType safety assured by the rule of usage BuilderType generic parameter.
    @SuppressWarnings("unchecked")
    public BuilderType setHasHeaderLine(boolean hasHeaderLine) {
        this.hasHeaderLine = hasHeaderLine;
        return (BuilderType) this;
    }

    @XmlElements({
            @XmlElement(name = "repositoryLocation", type = ClientReference.class),
            @XmlElement(name = "ftpLocation", type = FtpConnection.class),
            @XmlElement(name = "lfcLocation", type = LfsConnection.class)
    })
    public ResourceLocation getLocation() {
        return location;
    }

    // unchecked cast to BuilderType safety assured by the rule of usage BuilderType generic parameter.
    @SuppressWarnings("unchecked")
    public BuilderType setLocation(ResourceLocation location) {
        this.location = location;
        return (BuilderType) this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AbstractFileConnection that = (AbstractFileConnection) o;

        if (hasHeaderLine != that.hasHeaderLine) return false;
        if (location != null ? !location.equals(that.location) : that.location != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (hasHeaderLine ? 1 : 0);
        result = 31 * result + (location != null ? location.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "hasHeaderLine=" + hasHeaderLine +
                ", location=" + location +
                "} " + super.toString();
    }

    /*
     * DeepCloneable
     */

    @Override
    public AbstractFileConnection deepClone() {
        return new AbstractFileConnection(this);
    }
}
