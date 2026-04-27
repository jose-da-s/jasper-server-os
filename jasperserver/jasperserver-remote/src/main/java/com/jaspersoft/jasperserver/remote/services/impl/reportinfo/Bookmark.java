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
package com.jaspersoft.jasperserver.remote.services.impl.reportinfo;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import java.util.List;

/**
 * @author Narcis Marcu (nmarcu@tibco.com)
 */
public class Bookmark {
    private String label;
    private Integer pageIndex;
    private String elementAddress;
    private List<Bookmark> bookmarks;

    public Bookmark() {}

    public Bookmark(String _label, Integer _pageIndex, String _elementAddress) {
        label = _label;
        pageIndex = _pageIndex;
        elementAddress = _elementAddress;
    }

    @XmlElement
    public String getLabel() {
        return label;
    }

    @XmlElement
    public Integer getPageIndex() {
        return pageIndex;
    }

    @XmlElement
    public String getElementAddress() {
        return elementAddress;
    }

    @XmlElementWrapper(name = "bookmarks")
    @XmlElement(name = "bookmark")
    public List<Bookmark> getBookmarks() {
        return bookmarks;
    }

    public void setBookmarks(List<Bookmark> _bookmarks) {
        bookmarks = _bookmarks;
    }
}
