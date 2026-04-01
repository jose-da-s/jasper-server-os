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

import jakarta.validation.constraints.Min;
import javax.xml.bind.annotation.XmlTransient;
import java.io.Serializable;
import java.util.List;

import static com.jaspersoft.jasperserver.dto.executions.QueryExecutionsErrorCode.Codes.QUERY_ORDERBY_TOP_OR_BOTTOM_LIMIT_NOT_VALID;

/**
 * @author Andriy Godovanets
 * @version $Id$
 */
public class ClientTopOrBottomNOrder extends ClientPathOrder implements Serializable {
    /**
     * Order limit
     */
    @Min(message = QUERY_ORDERBY_TOP_OR_BOTTOM_LIMIT_NOT_VALID, value = 1)
    private Integer limit;
    private boolean createOtherBucket = false;
    private boolean limitAllLevels = false;

    public ClientTopOrBottomNOrder() {
    }

    public ClientTopOrBottomNOrder(ClientTopOrBottomNOrder sorting) {
        super(sorting);

        setLimit(sorting.getLimit());
        setCreateOtherBucket(sorting.getCreateOtherBucket());
        setLimitAllLevels(sorting.getLimitAllLevels());
    }

    @Override
    public ClientTopOrBottomNOrder deepClone() {
        return new ClientTopOrBottomNOrder(this);
    }

    /**
     *
     * @return Boolean value that specify if additional bucket (with name "other") should be created
     * for aggregation applied for all values that were filtered out by this ordering filter
     */
    public boolean getCreateOtherBucket() {
        return createOtherBucket;
    }

    public ClientTopOrBottomNOrder setCreateOtherBucket(boolean createOtherBucket) {
        this.createOtherBucket = createOtherBucket;
        return this;
    }

    /**
     *
     * @return Number of Top/Bottom items to display
     */
    public Integer getLimit() {
        return limit;
    }

    public ClientTopOrBottomNOrder setLimit(Integer limit) {
        this.limit = limit;
        return this;
    }

    /**
     *
     * @return Control whether we should limit number of member in each level along with measures
     */
    public boolean getLimitAllLevels() {
        return limitAllLevels;
    }

    public ClientTopOrBottomNOrder setLimitAllLevels(boolean limitAllLevels) {
        this.limitAllLevels = limitAllLevels;
        return this;
    }

    @Override
    public ClientTopOrBottomNOrder setPath(List<String> path) {
        return (ClientTopOrBottomNOrder) super.setPath(path);
    }

    @Override
    public ClientTopOrBottomNOrder setAscending(Boolean isAscending) {
        return (ClientTopOrBottomNOrder) super.setAscending(isAscending);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        ClientTopOrBottomNOrder that = (ClientTopOrBottomNOrder) o;

        if (createOtherBucket != that.createOtherBucket) return false;
        if (limitAllLevels != that.limitAllLevels) return false;
        return limit != null ? limit.equals(that.limit) : that.limit == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (limit != null ? limit.hashCode() : 0);
        result = 31 * result + (createOtherBucket ? 1 : 0);
        result = 31 * result + (limitAllLevels ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ClientTopOrBottomNOrder{" +
                "limit=" + limit +
                ", createOtherBucket=" + createOtherBucket +
                ", limitAllLevels=" + limitAllLevels +
                "} " + super.toString();
    }

    public static class ClientTopNOrder extends ClientTopOrBottomNOrder {
        public ClientTopNOrder() {
            setAscending(false);
        }

        public ClientTopNOrder(ClientTopOrBottomNOrder sorting) {
            super(sorting);
        }

        @Override
        public ClientTopNOrder deepClone() {
            return new ClientTopNOrder(this);
        }

        @XmlTransient
        @Override
        public Boolean isAscending() {
            return super.isAscending();
        }
    }

    public static class ClientBottomNOrder extends ClientTopOrBottomNOrder {
        public ClientBottomNOrder() {
            setAscending(true);
        }

        public ClientBottomNOrder(ClientTopOrBottomNOrder sorting) {
            super(sorting);
        }

        @Override
        public ClientBottomNOrder deepClone() {
            return new ClientBottomNOrder(this);
        }

        @XmlTransient
        @Override
        public Boolean isAscending() {
            return super.isAscending();
        }

        @Override
        public String toString() {
            return "ClientBottomNOrder{" +
                    "limit=" + getLimit() +
                    ", createOtherBucket=" + getCreateOtherBucket() +
                    ", limitAllLevels=" + getLimitAllLevels() +
                    "} " + super.toString();
        }
    }
}
