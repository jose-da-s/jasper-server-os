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

package com.jaspersoft.jasperserver.remote.services.impl;

import com.jaspersoft.jasperserver.api.engine.jasperreports.domain.impl.ReportUnitResult;
import com.jaspersoft.jasperserver.dto.executions.ExecutionStatus;
import com.jaspersoft.jasperserver.remote.services.ReportExecution;
import com.jaspersoft.jasperserver.remote.services.impl.RunReportServiceCacheFactoryBean.RunReportCacheRemoveEventListener;
import net.sf.jasperreports.engine.JRVirtualizer;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;

import java.util.List;
import java.util.UUID;

import javax.cache.Cache;
import javax.cache.event.CacheEntryEvent;
import javax.cache.event.EventType;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

public class RunReportServiceCacheFactoryBeanTest {
    private final RunReportCacheRemoveEventListener listener = new RunReportCacheRemoveEventListener();

    private final Cache<String, Pair<String, ReportExecution>> cache = mock(Cache.class);

    @Test
    public void notifyElementEvicted_statusReady_evicted() {
    	Cache.Entry<String, Pair<String, ReportExecution>> element = newReportExecution("superuser", "/public/Samples/report", ExecutionStatus.ready);
        listener.onRemoved(List.of(createRemoveEvent(cache, element)));
        ReportExecution reportExecution = element.getValue().getRight();

        ReportUnitResult reportUnitResult = reportExecution.getReportUnitResult();
        verify(reportUnitResult.getVirtualizer()).cleanup();
        verify(reportUnitResult).setJasperPrintAccessor(null);
    }

    @Test
    public void notifyElementEvicted_statusCanceled_exception() {
    	Cache.Entry<String, Pair<String, ReportExecution>> element = newReportExecution("superuser", "/public/Samples/report", ExecutionStatus.cancelled);
    	listener.onRemoved(List.of(createRemoveEvent(cache, element)));
        ReportExecution reportExecution = element.getValue().getRight();

        verifyNoInteractions(reportExecution.getReportUnitResult());
    }

    private Cache.Entry<String, Pair<String, ReportExecution>> newReportExecution(String username, String resourceUri, ExecutionStatus status) {
        JRVirtualizer virtualizer = mock(JRVirtualizer.class);
        ReportUnitResult result = mock(ReportUnitResult.class);

        doReturn(resourceUri).when(result).getReportUnitURI();
        doReturn(virtualizer).when(result).getVirtualizer();

        ReportExecution reportExecution = new ReportExecution();
        reportExecution.setReportUnitResult(result);
        reportExecution.setStatus(status);

        // Username to ReportExecution
        Pair<String, ReportExecution> pair = Pair.of(username, reportExecution);
        return new Cache.Entry<String, Pair<String, ReportExecution>>(){
        	public String getKey() { return UUID.randomUUID().toString(); }
        	public Pair<String, ReportExecution> getValue() { return pair; }
        	public <T> T unwrap(Class<T> clazz) { throw new IllegalArgumentException("anonymous interface"); }
        };
    }

    private <K, V> CacheEntryEvent<K, V> createRemoveEvent(Cache<K, V> source, Cache.Entry<K, V> entry) {
    	return new CacheEntryEvent<K, V>(source, EventType.REMOVED) {
        	public K getKey() { return entry.getKey(); }
    		public V getValue() { return entry.getValue(); }
    		public V getOldValue() { return entry.getValue(); }
    		public boolean isOldValueAvailable() { return true; }
        	public <T> T unwrap(Class<T> clazz) { return entry.unwrap(clazz); }
    	};
    }
}