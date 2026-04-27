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

package com.jaspersoft.jasperserver.dto.resources.hypermedia;

import com.jaspersoft.jasperserver.dto.common.DeepCloneable;

import jakarta.xml.bind.annotation.XmlRootElement;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.checkNotNull;

/**
 * Created by borys.kolesnykov on 9/22/2014.
 */
@XmlRootElement(name = "_links")
public class HypermediaResourceLookupLinks implements DeepCloneable<HypermediaResourceLookupLinks> {

    private String self;
    private String next;
    private String prev;

    public HypermediaResourceLookupLinks() {
    }

    public HypermediaResourceLookupLinks(HypermediaResourceLookupLinks other) {
        checkNotNull(other);

        this.self = other.getSelf();
        this.next = other.getNext();
        this.prev = other.getPrev();
    }

    @Override
    public HypermediaResourceLookupLinks deepClone() {
        return new HypermediaResourceLookupLinks(this);
    }

    public String getSelf() {
        return self;
    }

    public HypermediaResourceLookupLinks setSelf(String self) {
        this.self = self;
        return this;
    }

    public String getNext() {
        return next;
    }

    public HypermediaResourceLookupLinks setNext(String next) {
        this.next = next;
        return this;
    }

    public String getPrev() {
        return prev;
    }

    public HypermediaResourceLookupLinks setPrev(String prev) {
        this.prev = prev;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HypermediaResourceLookupLinks that = (HypermediaResourceLookupLinks) o;

        if (self != null ? !self.equals(that.self) : that.self != null) return false;
        if (next != null ? !next.equals(that.next) : that.next != null) return false;
        return prev != null ? prev.equals(that.prev) : that.prev == null;
    }

    @Override
    public int hashCode() {
        int result = self != null ? self.hashCode() : 0;
        result = 31 * result + (next != null ? next.hashCode() : 0);
        result = 31 * result + (prev != null ? prev.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "HypermediaResourceLookupLinks{" +
                "self='" + self + '\'' +
                ", next='" + next + '\'' +
                ", prev='" + prev + '\'' +
                '}';
    }
}
