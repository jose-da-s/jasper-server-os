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

import net.sf.jasperreports.engine.PrintPart;
import net.sf.jasperreports.engine.PrintParts;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Narcis Marcu (nmarcu@tibco.com)
 */
public class PartsInfo {
    private String id;
    private String type;
    private List<Part> parts;

    public PartsInfo() {}

    public PartsInfo(PrintParts printParts, String jasperPrintName) {
        this.id = "parts_" + (printParts.hashCode() & 0x7FFFFFFF);
        this.type = "reportparts";
        this.parts = calculateParts(printParts, jasperPrintName);
    }

    @XmlElement
    public String getId() {
        return id;
    }

    @XmlElement
    public String getType() {
        return type;
    }

    @XmlElementWrapper(name = "parts")
    @XmlElement(name = "part")
    public List<Part> getParts() {
        return parts;
    }

    private List<Part> calculateParts(PrintParts printParts, String jasperPrintName) {
        List<Part> parts = new LinkedList<>();

        if (!printParts.startsAtZero()) {
            parts.add(new Part(0, jasperPrintName));
        }

        Iterator<Map.Entry<Integer, PrintPart>> it = printParts.partsIterator();
        while (it.hasNext()) {
            Map.Entry<Integer, PrintPart> partsEntry = it.next();
            int idx = partsEntry.getKey();
            PrintPart printPart = partsEntry.getValue();
            parts.add(new Part(idx, printPart.getName()));
        }

        return parts;
    }
}
