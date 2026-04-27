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
import net.sf.jasperreports.engine.PrintParts;

import jakarta.xml.bind.annotation.*;
import java.util.List;

/**
 * @author Narcis Marcu (nmarcu@tibco.com)
 */
@XmlRootElement
@XmlAccessorOrder(XmlAccessOrder.ALPHABETICAL)
public class ReportInfo {
    private BookmarksInfo bookmarks;
    private PartsInfo parts;

    @XmlElement
    public BookmarksInfo getBookmarks() {
        return bookmarks;
    }

    public void setBookmarks(List<PrintBookmark> bookmarks) {
        this.bookmarks = new BookmarksInfo(bookmarks);
    }

    @XmlElement
    public PartsInfo getParts() {
        return parts;
    }

    public void setParts(PrintParts printParts, String jasperPrintName) {
        this.parts = new PartsInfo(printParts, jasperPrintName);
    }
}
