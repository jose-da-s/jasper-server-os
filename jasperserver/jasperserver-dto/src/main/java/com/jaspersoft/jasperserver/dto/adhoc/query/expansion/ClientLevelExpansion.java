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
package com.jaspersoft.jasperserver.dto.adhoc.query.expansion;

import com.jaspersoft.jasperserver.dto.adhoc.query.ClientFieldReference;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlTransient;

import java.io.Serializable;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.checkNotNull;

/**
 * @author Andriy Godovanets
 */
public class ClientLevelExpansion implements ClientExpandable<String>, ClientFieldReference, Serializable {
    private boolean isExpanded;
    private Boolean isAggregationLevel = false;

    private String levelReference;

    public ClientLevelExpansion() {
        // no op
    }

    public ClientLevelExpansion(ClientLevelExpansion expansion) {
        checkNotNull(expansion);

        isExpanded = expansion.isExpanded();
        isAggregationLevel = expansion.isAggregationLevel();
        levelReference = expansion.getLevelReference();
    }

    @Override
    public ClientLevelExpansion deepClone() {
        return new ClientLevelExpansion(this);
    }

    /**
     * @return Expansion state: true - for expanded, false - collapsed
     */
    @Override
    public boolean isExpanded() {
        return isExpanded;
    }

    public ClientLevelExpansion setExpanded(boolean expanded) {
        this.isExpanded = expanded;
        return this;
    }

    @Override
    public String get() {
        return levelReference;
    }

    /**
     * @return Field name or query Level Id that should be expanded
     */
    @XmlElement(name = "fieldRef")
    public String getLevelReference() {
        return levelReference;
    }

    public ClientLevelExpansion setLevelReference(String levelReference) {
        this.levelReference = levelReference;
        return this;
    }

    @XmlTransient
    @Override
    public String getFieldReference() {
        return getLevelReference();
    }

    /**
     * Since name of the measure level can vary, we've added separate property
     * to distinguish Measure level amongst others
     *
     * Default value: false
     *
     * @return true, if Measures level expanded
     */
    @XmlElement(name = "aggregation")
    public Boolean isAggregationLevel() {
        return isAggregationLevel;
    }

    public ClientLevelExpansion setAggregationLevel(Boolean aggregationLevel) {
        isAggregationLevel = aggregationLevel != null ? aggregationLevel : false;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClientLevelExpansion)) return false;

        ClientLevelExpansion that = (ClientLevelExpansion) o;

        if (isExpanded() != that.isExpanded()) return false;
        if (!isAggregationLevel.equals(that.isAggregationLevel)) return false;
        return getLevelReference() != null ? getLevelReference().equals(that.getLevelReference()) : that.getLevelReference() == null;

    }

    @Override
    public int hashCode() {
        int result = (isExpanded() ? 1 : 0);
        result = 31 * result + isAggregationLevel.hashCode();
        result = 31 * result + (getLevelReference() != null ? getLevelReference().hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ClientLevelExpansion{");
        sb.append("isAggregationLevel=").append(isAggregationLevel);
        sb.append(", isExpanded=").append(isExpanded);
        sb.append(", levelReference='").append(levelReference).append('\'');
        sb.append('}');
        return sb.toString();
    }

}
