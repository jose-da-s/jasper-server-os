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
package com.jaspersoft.jasperserver.dto.authority.hypermedia;

import com.jaspersoft.jasperserver.dto.authority.ClientAttribute;
import com.jaspersoft.jasperserver.dto.common.DeepCloneable;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.checkNotNull;
import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.copyOf;

/**
 * <p></p>
 *
 * @author Volodya Sabadosh
 * @version $Id$
 */
@XmlRootElement(name = "attributes")
public class HypermediaAttributesListWrapper implements DeepCloneable<HypermediaAttributesListWrapper> {
    private List<HypermediaAttribute> profileAttributes;

    public HypermediaAttributesListWrapper() {
    }

    public HypermediaAttributesListWrapper(List<? extends ClientAttribute> attributes) {
        this.profileAttributes = new ArrayList<HypermediaAttribute>(attributes.size());
        for (ClientAttribute client : attributes) {
            if (client instanceof HypermediaAttribute) {
                profileAttributes.add((HypermediaAttribute) client);
            } else {
                profileAttributes.add(new HypermediaAttribute(client));
            }
        }
    }

    public HypermediaAttributesListWrapper(HypermediaAttributesListWrapper other) {
        checkNotNull(other);

        this.profileAttributes = copyOf(other.getProfileAttributes());
    }

    @Override
    public HypermediaAttributesListWrapper deepClone() {
        return new HypermediaAttributesListWrapper(this);
    }

    @XmlElement(name = "attribute")
    public List<HypermediaAttribute> getProfileAttributes() {
        return profileAttributes;
    }

    public HypermediaAttributesListWrapper setProfileAttributes(List<HypermediaAttribute> profileAttributes) {
        this.profileAttributes = profileAttributes;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HypermediaAttributesListWrapper that = (HypermediaAttributesListWrapper) o;

        return profileAttributes != null ? profileAttributes.equals(that.profileAttributes) : that.profileAttributes == null;
    }

    @Override
    public int hashCode() {
        return profileAttributes != null ? profileAttributes.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "HypermediaAttributesListWrapper{" +
                "profileAttributes=" + profileAttributes +
                '}';
    }
}
