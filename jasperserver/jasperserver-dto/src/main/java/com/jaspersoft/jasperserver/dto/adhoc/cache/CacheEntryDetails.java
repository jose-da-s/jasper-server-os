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
package com.jaspersoft.jasperserver.dto.adhoc.cache;

import com.jaspersoft.jasperserver.dto.common.DeepCloneable;

import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.*;

import static com.jaspersoft.jasperserver.dto.utils.ValueObjectUtils.checkNotNull;

/**
 * @author Stanislav Chubar (schubar@tibco.com)
 * @version $Id $
 */
@XmlRootElement
public class CacheEntryDetails implements DeepCloneable<CacheEntryDetails> {
    private static final String NO_QUERY = "(empty)";
    private static final String PARAM_PARAMS = "params";
    private static final String PARAM_MEM_USED = "memUsed";
    private static final String PARAM_SORT = "sort";

    private String id;
    private long createdTime;
    private long fetchTime;
    private long queryTime;
    private long lastUsedTime;
    private long age;
    private long idle;
    private long timesUsed;
    private String query;
    private String datasourceUri;
    private String connectionUrl;
    private long rows;
    private long memUsed;
    private State status;
    private Map<String, String> params;
    private Set<String> usedBy;

    public enum State {
        New,
        WaitingForResults,
        FetchingResults,
        Complete,
        Canceled,
        Error,
        Expired;
    }

    public CacheEntryDetails() {
    }

    public CacheEntryDetails(CacheEntryDetails source) {
        checkNotNull(source);

        id = source.getId();
        createdTime = source.getCreatedTime();
        fetchTime = source.getFetchTime();
        queryTime = source.getQueryTime();
        lastUsedTime = source.getLastUsedTime();
        age = source.getAge();
        idle = source.getIdle();
        timesUsed = source.getTimesUsed();
        query = source.getQuery();
        datasourceUri = source.getDatasourceUri();
        connectionUrl = source.getConnectionUrl();
        rows = source.getRows();
        memUsed = source.getMemUsed();
        status = source.getStatus();
        params = source.getParams() != null ? new HashMap<>(source.getParams()) : null;
        usedBy = source.getUsedBy() != null ? new HashSet<>(source.getUsedBy()) : null;
    }

    public String getId() {
        return id;
    }

    public CacheEntryDetails setId(String id) {
        this.id = id;
        return this;
    }

    public long getCreatedTime() {
        return createdTime;
    }

    public CacheEntryDetails setCreatedTime(long createdTime) {
        this.createdTime = createdTime;
        return this;
    }

    public long getFetchTime() {
        return fetchTime;
    }

    public CacheEntryDetails setFetchTime(long fetchTime) {
        this.fetchTime = fetchTime;
        return this;
    }

    public long getQueryTime() {
        return queryTime;
    }

    public CacheEntryDetails setQueryTime(long queryTime) {
        this.queryTime = queryTime;
        return this;
    }

    public long getLastUsedTime() {
        return lastUsedTime;
    }

    public CacheEntryDetails setLastUsedTime(long lastUsedTime) {
        this.lastUsedTime = lastUsedTime;
        return this;
    }

    public long getAge() {
        return age;
    }

    public CacheEntryDetails setAge(long age) {
        this.age = age;
        return this;
    }

    public long getIdle() {
        return idle;
    }

    public CacheEntryDetails setIdle(long idle) {
        this.idle = idle;
        return this;
    }

    public long getTimesUsed() {
        return timesUsed;
    }

    public CacheEntryDetails setTimesUsed(long timesUsed) {
        this.timesUsed = timesUsed;
        return this;
    }

    public String getQuery() {
        return query;
    }

    public CacheEntryDetails setQuery(String query) {
        this.query = query;
        return this;
    }

    public String getDatasourceUri() {
        return datasourceUri;
    }

    public CacheEntryDetails setDatasourceUri(String datasourceUri) {
        this.datasourceUri = datasourceUri;
        return this;
    }

    public String getConnectionUrl() {
        return connectionUrl;
    }

    public CacheEntryDetails setConnectionUrl(String connectionUrl) {
        this.connectionUrl = connectionUrl;
        return this;
    }

    public long getRows() {
        return rows;
    }

    public CacheEntryDetails setRows(long rows) {
        this.rows = rows;
        return this;
    }

    public long getMemUsed() {
        return memUsed;
    }

    public CacheEntryDetails setMemUsed(long memUsed) {
        this.memUsed = memUsed;
        return this;
    }

    public State getStatus() {
        return status;
    }

    public CacheEntryDetails setStatus(State status) {
        this.status = status;
        return this;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public CacheEntryDetails setParams(Map<String, String> params) {
        this.params = params;
        return this;
    }

    public Set<String> getUsedBy() {
        return usedBy;
    }

    public CacheEntryDetails setUsedBy(Set<String> usedBy) {
        this.usedBy = usedBy;
        return this;
    }

    @Override
    public CacheEntryDetails deepClone() {
        return new CacheEntryDetails(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CacheEntryDetails)) return false;
        CacheEntryDetails that = (CacheEntryDetails) o;
        return createdTime == that.createdTime && fetchTime == that.fetchTime && queryTime == that.queryTime && lastUsedTime == that.lastUsedTime && age == that.age && idle == that.idle && timesUsed == that.timesUsed && rows == that.rows && Double.compare(that.memUsed, memUsed) == 0 && Objects.equals(id, that.id) && Objects.equals(query, that.query) && Objects.equals(datasourceUri, that.datasourceUri) && Objects.equals(connectionUrl, that.connectionUrl) && status == that.status && Objects.equals(params, that.params) && Objects.equals(usedBy, that.usedBy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, createdTime, fetchTime, queryTime, lastUsedTime, age, idle, timesUsed, query, datasourceUri, connectionUrl, rows, memUsed, status, params, usedBy);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", CacheEntryDetails.class.getSimpleName() + "{", "}")
                .add("id='" + id + "'")
                .add("createdTime=" + createdTime)
                .add("fetchTime=" + fetchTime)
                .add("queryTime=" + queryTime)
                .add("lastUsedTime=" + lastUsedTime)
                .add("age=" + age)
                .add("idle=" + idle)
                .add("timesUsed=" + timesUsed)
                .add("query='" + query + "'")
                .add("datasourceUri='" + datasourceUri + "'")
                .add("connectionUrl='" + connectionUrl + "'")
                .add("rows=" + rows)
                .add("memUsed=" + memUsed)
                .add("status=" + status)
                .add("params=" + params)
                .add("usedBy=" + usedBy)
                .toString();
    }
}
