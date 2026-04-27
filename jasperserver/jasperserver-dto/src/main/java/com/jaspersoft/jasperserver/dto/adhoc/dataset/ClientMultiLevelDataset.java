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
package com.jaspersoft.jasperserver.dto.adhoc.dataset;

import com.jaspersoft.jasperserver.dto.common.DeepCloneable;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlElements;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.List;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.checkNotNull;
import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.copyOf;

/**
 * @author Volodya Sabadosh
 * @version $Id$
 */
@XmlRootElement(name = "multiLevelDataset")
public class ClientMultiLevelDataset implements DeepCloneable<ClientMultiLevelDataset>, Serializable {
    private Integer counts;
    private List<AbstractClientDatasetLevel> levels;
    private List<AbstractClientDatasetLevelNode> levelDataNodes;

    public ClientMultiLevelDataset() {
    }

    public ClientMultiLevelDataset(ClientMultiLevelDataset other) {
        checkNotNull(other);

        counts = other.getCounts();
        levels = copyOf(other.getLevels());
        levelDataNodes = copyOf(other.getLevelDataNodes());
    }

    @XmlElement(name = "counts")
    public Integer getCounts() {
        return counts;
    }

    public ClientMultiLevelDataset setCounts(Integer counts) {
        this.counts = counts;
        return this;
    }

    @XmlElementWrapper(name = "levels")
    @XmlElements({
            @XmlElement(name = "all", type = ClientDatasetAllLevel.class),
            @XmlElement(name = "group", type = ClientDatasetGroupLevel.class),
            @XmlElement(name = "detail", type = ClientDatasetDetailLevel.class)})
    public List<AbstractClientDatasetLevel> getLevels() {
        return levels;
    }

    public ClientMultiLevelDataset setLevels(List<AbstractClientDatasetLevel> levels) {
        this.levels = levels;
        return this;
    }

    @XmlElementWrapper(name = "levelDataNodes")
    @XmlElements({
            @XmlElement(name = "all", type = ClientDatasetAllLevelNode.class),
            @XmlElement(name = "group", type = ClientDatasetGroupLevelNode.class),
            @XmlElement(name = "detail", type = ClientDatasetDetailLevelNode.class)})
    public List<AbstractClientDatasetLevelNode> getLevelDataNodes() {
        return levelDataNodes;
    }

    public ClientMultiLevelDataset setLevelDataNodes(List<AbstractClientDatasetLevelNode> levelDataNodes) {
        this.levelDataNodes = levelDataNodes;
        return this;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClientMultiLevelDataset that = (ClientMultiLevelDataset) o;

        if (counts != null ? !counts.equals(that.counts) : that.counts != null) return false;
        if (levelDataNodes != null ? !levelDataNodes.equals(that.levelDataNodes) : that.levelDataNodes != null)
            return false;
        if (levels != null ? !levels.equals(that.levels) : that.levels != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = counts != null ? counts.hashCode() : 0;
        result = 31 * result + (levels != null ? levels.hashCode() : 0);
        result = 31 * result + (levelDataNodes != null ? levelDataNodes.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ClientMultiLevelDataset{" +
                "counts=" + counts +
                ", levels=" + levels +
                ", levelDataNodes=" + levelDataNodes +
                '}';
    }

    @Override
    public ClientMultiLevelDataset deepClone() {
        return new ClientMultiLevelDataset(this);
    }
}
