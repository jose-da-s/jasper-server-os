/*
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
package com.jaspersoft.jasperserver.dto.adhoc.query.order;

import com.jaspersoft.jasperserver.dto.common.DeepCloneable;

import jakarta.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlElement;

/**
 * @author Andriy Godovanets
 */
public interface ClientOrder extends DeepCloneable {

    /**
     * @return Is item sorted ascending? If false - then item is sorted descending.
     */
    @NotNull
    @XmlElement(name = "ascending", required = false, defaultValue = "true")
    Boolean isAscending();

    /**
     * {@inheritDoc}
     */
    @Override
    ClientOrder deepClone();
}
