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

package com.jaspersoft.jasperserver.dto.job.wrappers;

import com.jaspersoft.jasperserver.dto.common.DeepCloneable;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.LinkedHashSet;
import java.util.Set;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.checkNotNull;
import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.copyOf;

/**
 * This class is used for serialization because of no ability inFolder use @XmlElementWrapper together with @XmlJavaTypeAdapter.
 * See http://java.net/jira/browse/JAXB-787
 *
 * @author Yaroslav.Kovalchyk
 * @version $Id$
 */
@XmlRootElement(name = "outputFormats")
public class ClientReportJobOutputFormatsWrapper implements DeepCloneable<ClientReportJobOutputFormatsWrapper> {

    private Set<String> formats;

    public ClientReportJobOutputFormatsWrapper() {
    }

    public ClientReportJobOutputFormatsWrapper(Set<String> formats) {
        if (formats == null) {
            return;
        }
        this.formats = new LinkedHashSet<String>(formats);
    }

    public ClientReportJobOutputFormatsWrapper(ClientReportJobOutputFormatsWrapper other) {
        checkNotNull(other);

        formats = copyOf(other.getFormats());
    }


    @XmlElement(name = "outputFormat")
    public Set<String> getFormats() {
        return formats;
    }

    public ClientReportJobOutputFormatsWrapper setFormats(Set<String> formats) {
        this.formats = formats;
        return this;
    }

    @Override
    public String toString() {
        return "ClientReportJobOutputFormatsWrapper{" +
                "formats=" + formats +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClientReportJobOutputFormatsWrapper)) return false;

        ClientReportJobOutputFormatsWrapper that = (ClientReportJobOutputFormatsWrapper) o;

        return !(getFormats() != null ? !getFormats().equals(that.getFormats()) : that.getFormats() != null);

    }

    @Override
    public int hashCode() {
        return getFormats() != null ? getFormats().hashCode() : 0;
    }

    @Override
    public ClientReportJobOutputFormatsWrapper deepClone() {
        return new ClientReportJobOutputFormatsWrapper(this);
    }
}
