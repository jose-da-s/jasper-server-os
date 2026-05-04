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

package com.jaspersoft.jasperserver.dto.permissions;

import com.jaspersoft.jasperserver.dto.common.DeepCloneable;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.checkNotNull;

/**
 * <p></p>
 *
 * @author Zakhar.Tomchenco
 * @version $Id$
 */
@XmlRootElement(name = "repositoryPermission")
public class RepositoryPermission implements DeepCloneable<RepositoryPermission> {
    String uri;
    String recipient;
    Integer mask;

    public RepositoryPermission() {
    }

    public RepositoryPermission(String uri, String recipient, int mask) {
        this.uri = uri;
        this.recipient = recipient;
        this.mask = mask;
    }

    public RepositoryPermission(RepositoryPermission other) {
        checkNotNull(other);

        this.uri = other.getUri();
        this.recipient = other.getRecipient();
        this.mask = other.getMask();
    }

    @Override
    public RepositoryPermission deepClone() {
        return new RepositoryPermission(this);
    }

    @XmlElement(name = "uri")
    public String getUri() {
        return uri;
    }

    public RepositoryPermission setUri(String uri) {
        this.uri = uri;
        return this;
    }

    @XmlElement(name = "recipient")
    public String getRecipient() {
        return recipient;
    }

    public RepositoryPermission setRecipient(String recipient) {
        this.recipient = recipient;
        return this;
    }

    @XmlElement(name = "mask")
    public Integer getMask() {
        return mask;
    }

    public RepositoryPermission setMask(Integer mask) {
        this.mask = mask;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RepositoryPermission that = (RepositoryPermission) o;

        if (mask != null ? !mask.equals(that.mask) : that.mask != null) return false;
        if (recipient != null ? !recipient.equals(that.recipient) : that.recipient != null) return false;
        if (uri != null ? !uri.equals(that.uri) : that.uri != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = uri != null ? uri.hashCode() : 0;
        result = 31 * result + (recipient != null ? recipient.hashCode() : 0);
        result = 31 * result + (mask != null ? mask.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "RepositoryPermission{" +
                "uri='" + uri + '\'' +
                ", recipient='" + recipient + '\'' +
                ", mask=" + mask +
                '}';
    }
}
