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

import net.sf.jasperreports.engine.PrintBookmark;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Narcis Marcu (nmarcu@tibco.com)
 */
public class BookmarksInfo {
    private String id;
    private String type;
    private List<Bookmark> bookmarks;

    public BookmarksInfo() {}

    public BookmarksInfo(List<PrintBookmark> printBookmarks) {
        this.id = "bkmrk_" + (printBookmarks.hashCode() & 0x7FFFFFFF);
        this.type = "bookmarks";
        this.bookmarks = calculateBookmarks(printBookmarks);
    }

    @XmlElement
    public String getId() {
        return id;
    }

    @XmlElement
    public String getType() {
        return type;
    }

    @XmlElementWrapper(name = "bookmarks")
    @XmlElement(name = "bookmark")
    public List<Bookmark> getBookmarks() {
        return bookmarks;
    }

    private List<Bookmark> calculateBookmarks(List<PrintBookmark> printBookmarks) {
        List<Bookmark> bookmarks = new LinkedList<>();

        for (PrintBookmark printBookmark: printBookmarks) {
            Bookmark bkmrk = new Bookmark(printBookmark.getLabel(),
                    printBookmark.getPageIndex(), printBookmark.getElementAddress());

            if (printBookmark.getBookmarks() != null && printBookmark.getBookmarks().size() > 0) {
                bkmrk.setBookmarks(calculateBookmarks(printBookmark.getBookmarks()));
            }

            bookmarks.add(bkmrk);
        }

        return bookmarks;
    }
}
