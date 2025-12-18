/*
 * Copyright (C) 2025 the Jasper Server OS Authors
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

package com.jaspersoft.jasperserver.remote.resources.validation;

import com.jaspersoft.jasperserver.api.common.domain.ExecutionContext;
import com.jaspersoft.jasperserver.api.common.domain.impl.ExecutionContextImpl;
import com.jaspersoft.jasperserver.api.metadata.common.domain.Resource;
import com.jaspersoft.jasperserver.api.metadata.common.domain.ResourceLookup;
import com.jaspersoft.jasperserver.api.metadata.common.domain.ResourceReference;
import com.jaspersoft.jasperserver.api.metadata.common.domain.client.InputControlImpl;
import com.jaspersoft.jasperserver.api.metadata.common.domain.client.ResourceLookupImpl;
import com.jaspersoft.jasperserver.api.metadata.common.service.RepositoryService;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.VirtualReportDataSource;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.client.JdbcReportDataSourceImpl;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.client.VirtualReportDataSourceImpl;
import com.jaspersoft.jasperserver.api.metadata.user.service.ProfileAttributesResolver;
import com.jaspersoft.jasperserver.api.search.SearchCriteriaFactory;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.access.AccessDeniedException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;

/**
 * <p></p>
 *
 * @author Zakhar.Tomchenco
 * @version $Id$
 */
public class VirtualDataSourceResourceValidatorTest {
    @InjectMocks
    private final VirtualDataSourceResourceValidator validator = new VirtualDataSourceResourceValidator();
    @Mock
    private RepositoryService service;
    @Mock
    private ProfileAttributesResolver profileAttributesResolver;
    @Mock
    private SearchCriteriaFactory searchCriteriaFactory;

    private VirtualReportDataSource dataSource;
    private List<ResourceLookup> dependent = new LinkedList<ResourceLookup>();
    ExecutionContext ctx = ExecutionContextImpl.getRuntimeExecutionContext();

    @BeforeClass
    public void init(){
        MockitoAnnotations.initMocks(this);
    }

    @BeforeMethod
    public void setUp() throws Exception {
        reset(service);
        reset(profileAttributesResolver);

        dataSource = new VirtualReportDataSourceImpl();
        dataSource.setLabel("tets");
        dataSource.setURIString("/a/uri");
        dataSource.setVersion(100);

        Map<String, ResourceReference> dataSourceUris = new HashMap<String, ResourceReference>();
        dataSourceUris.put("a", new ResourceReference("/a"));
        dataSourceUris.put("b", new ResourceReference("/b"));
        dataSourceUris.put("z", new ResourceReference("/z"));
        dataSource.setDataSourceUriMap(dataSourceUris);

        ResourceLookup lookup1 = new ResourceLookupImpl();
        lookup1.setURIString("/a/lookup1");
        dependent.add(lookup1);

        ResourceLookup lookup2 = new ResourceLookupImpl();
        lookup2.setURIString("/a/lookup2");
        dependent.add(lookup2);

        when(service.getResource(nullable(ExecutionContext.class), eq("/a"))).thenReturn(new JdbcReportDataSourceImpl());
        when(service.getResource(nullable(ExecutionContext.class), eq("/b"))).thenReturn(new JdbcReportDataSourceImpl());
        when(service.getResource(nullable(ExecutionContext.class), eq("/d"))).thenReturn(new JdbcReportDataSourceImpl());
        when(service.getResource(nullable(ExecutionContext.class), eq("/z"))).thenReturn(new JdbcReportDataSourceImpl());
        when(profileAttributesResolver.containsAttribute(anyString())).thenReturn(false);
    }

    @Test
    public void testValidate() throws Exception {
        validator.validate(ctx, dataSource);
    }

    @Test
    public void testValidate_one_datasource() throws Exception {
        dataSource.getDataSourceUriMap().remove(dataSource.getDataSourceUriMap().keySet().iterator().next());
        dataSource.getDataSourceUriMap().remove(dataSource.getDataSourceUriMap().keySet().iterator().next());

        validator.validate(ctx, dataSource);
    }

    @Test
    public void testValidate_empty() throws Exception {
        dataSource.getDataSourceUriMap().clear();

        final List<Exception> exceptions = validator.validate(ctx, dataSource);

        assertNotNull(exceptions);
        assertFalse(exceptions.isEmpty());
    }

    @Test
    public void testValidate_special() throws Exception {
        dataSource.getDataSourceUriMap().put("[]", new ResourceReference("/d"));

        final List<Exception> exceptions = validator.validate(ctx, dataSource);

        assertNotNull(exceptions);
        assertFalse(exceptions.isEmpty());
    }

    @Test
    public void testValidate_Id() throws Exception {
        dataSource.getDataSourceUriMap().put("Hey'", new ResourceReference("/d"));

        final List<Exception> exceptions = validator.validate(ctx, dataSource);

        assertNotNull(exceptions);
        assertFalse(exceptions.isEmpty());
    }

    @Test
    public void testValidate_specialInAttribute() {
        String attributePlaceholder = "{attribute('[]!', '(,.:')";
        when(profileAttributesResolver.containsAttribute(attributePlaceholder)).thenReturn(true);
        dataSource.getDataSourceUriMap().put(attributePlaceholder, new ResourceReference("/d"));

        final List<Exception> exceptions = validator.validate(ctx, dataSource);
        assertTrue(exceptions.isEmpty());
    }

    @Test
    public void testValidate_duplicate_uri() throws Exception {
        dataSource.getDataSourceUriMap().put("c", new ResourceReference("/a"));

        final List<Exception> exceptions = validator.validate(ctx, dataSource);

        assertNotNull(exceptions);
        assertFalse(exceptions.isEmpty());
    }

    @Test
    public void testValidate_relative_uri() throws Exception {
        dataSource.getDataSourceUriMap().put("c", new ResourceReference("a"));

        final List<Exception> exceptions = validator.validate(ctx, dataSource);

        assertNotNull(exceptions);
        assertFalse(exceptions.isEmpty());
    }

    @Test
    public void testValidate_resourceNotExist() throws Exception {
        dataSource.getDataSourceUriMap().put("c", new ResourceReference("/c"));

        final List<Exception> exceptions = validator.validate(ctx, dataSource);

        assertNotNull(exceptions);
        assertFalse(exceptions.isEmpty());
    }

    @Test
    public void testValidate_resourceNotADatasource() throws Exception {
        when(service.getResource(nullable(ExecutionContext.class), eq("/c"))).thenReturn(new InputControlImpl());

        dataSource.getDataSourceUriMap().put("c", new ResourceReference("/c"));

        final List<Exception> exceptions = validator.validate(ctx, dataSource);

        assertNotNull(exceptions);
        assertFalse(exceptions.isEmpty());
    }

    @Test
    public void testValidate_noUsageValidationOnCreate() throws Exception {
        dataSource.setURIString(null);
        dataSource.setVersion(Resource.VERSION_NEW);

        validator.validate(ctx, dataSource);

        verify(service,never()).getDependentResources(nullable(ExecutionContext.class), anyString(), same(searchCriteriaFactory), anyInt(), anyInt());
    }

    @Test
    public void testValidate_noUsageValidationOnOverwriteSomeOtherTypeOfResource() throws Exception {
        when(service.getResource(nullable(ExecutionContext.class), eq(dataSource.getURIString()))).thenReturn(new InputControlImpl());

        validator.validate(ctx, dataSource);

        verify(service,never()).getDependentResources(nullable(ExecutionContext.class), anyString(), same(searchCriteriaFactory), anyInt(), anyInt());
    }

    @Test
    public void testValidate_validateUsage() throws Exception {
        when(service.getResource(nullable(ExecutionContext.class), eq(dataSource.getURIString()))).thenReturn(dataSource);

        validator.validate(ctx, dataSource);

        verify(service,atLeastOnce()).getDependentResources(nullable(ExecutionContext.class), anyString(), same(searchCriteriaFactory), anyInt(), anyInt());
    }

    @Test
    public void testValidate_validateUsage_noDependent() throws Exception {
        when(service.getResource(nullable(ExecutionContext.class), eq(dataSource.getURIString()))).thenReturn(dataSource);
        when(service.getDependentResources(nullable(ExecutionContext.class), anyString(), same(searchCriteriaFactory), anyInt(), anyInt())).thenReturn(Collections.EMPTY_LIST);

        validator.validate(ctx, dataSource);

        verify(service,atLeastOnce()).getDependentResources(nullable(ExecutionContext.class), anyString(), same(searchCriteriaFactory), anyInt(), anyInt());
    }

    @Test
    public void testValidate_validateUsage_dependent_same() throws Exception {
        when(service.getResource(nullable(ExecutionContext.class), eq(dataSource.getURIString()))).thenReturn(dataSource);
        when(service.getDependentResources(nullable(ExecutionContext.class), anyString(), same(searchCriteriaFactory), anyInt(), anyInt())).thenReturn(dependent);

        validator.validate(ctx, dataSource);

        verify(service,atLeastOnce()).getDependentResources(nullable(ExecutionContext.class), anyString(), same(searchCriteriaFactory), anyInt(), anyInt());
    }

    @Test
    public void testValidate_validateUsage_dependent_equal() throws Exception {
        VirtualReportDataSource existing = new VirtualReportDataSourceImpl();
        existing.setDataSourceUriMap(new HashMap<String, ResourceReference>(dataSource.getDataSourceUriMap()));
        existing.setURIString(dataSource.getURIString());

        when(service.getResource(nullable(ExecutionContext.class), eq(dataSource.getURIString()))).thenReturn(existing);
        when(service.getDependentResources(nullable(ExecutionContext.class), anyString(), same(searchCriteriaFactory), anyInt(), anyInt())).thenReturn(dependent);

        validator.validate(ctx, dataSource);

        verify(service,atLeastOnce()).getDependentResources(nullable(ExecutionContext.class), anyString(), same(searchCriteriaFactory), anyInt(), anyInt());
    }

    @Test(expectedExceptions = {AccessDeniedException.class})
    public void testValidate_validateUsage_dependent_removed() throws Exception {
        VirtualReportDataSource existing = new VirtualReportDataSourceImpl();
        existing.setDataSourceUriMap(new HashMap<String, ResourceReference>(dataSource.getDataSourceUriMap()));
        existing.setURIString(dataSource.getURIString());

        dataSource.getDataSourceUriMap().remove(dataSource.getDataSourceUriMap().keySet().iterator().next());

        when(service.getResource(nullable(ExecutionContext.class), eq(dataSource.getURIString()))).thenReturn(existing);
        when(service.getDependentResources(nullable(ExecutionContext.class), anyString(), same(searchCriteriaFactory), anyInt(), anyInt())).thenReturn(dependent);

        validator.validate(ctx, dataSource);
    }

    @Test
    public void testValidate_validateUsage_noDependent_removed() throws Exception {
        VirtualReportDataSource existing = new VirtualReportDataSourceImpl();
        existing.setDataSourceUriMap(new HashMap<String, ResourceReference>(dataSource.getDataSourceUriMap()));
        existing.setURIString(dataSource.getURIString());

        dataSource.getDataSourceUriMap().remove(dataSource.getDataSourceUriMap().keySet().iterator().next());

        when(service.getResource(nullable(ExecutionContext.class), eq(dataSource.getURIString()))).thenReturn(existing);
        when(service.getDependentResources(nullable(ExecutionContext.class), anyString(), same(searchCriteriaFactory), anyInt(), anyInt())).thenReturn(Collections.EMPTY_LIST);

        validator.validate(ctx, dataSource);
    }

    @Test(expectedExceptions = {AccessDeniedException.class})
    public void testValidate_validateUsage_dependent_changed() throws Exception {
        VirtualReportDataSource existing = new VirtualReportDataSourceImpl();
        existing.setDataSourceUriMap(new HashMap<String, ResourceReference>(dataSource.getDataSourceUriMap()));
        existing.setURIString(dataSource.getURIString());

        dataSource.getDataSourceUriMap().put(dataSource.getDataSourceUriMap().keySet().iterator().next(), new ResourceReference("/a/different"));

        when(service.getResource(nullable(ExecutionContext.class), eq(dataSource.getURIString()))).thenReturn(existing);
        when(service.getResource(nullable(ExecutionContext.class), eq("/a/different"))).thenReturn(existing);
        when(service.getDependentResources(nullable(ExecutionContext.class), anyString(), same(searchCriteriaFactory), anyInt(), anyInt())).thenReturn(dependent);

        validator.validate(ctx, dataSource);
    }

    @Test
    public void testValidate_validateUsage_noDependent_changed() throws Exception {
        VirtualReportDataSource existing = new VirtualReportDataSourceImpl();
        existing.setDataSourceUriMap(new HashMap<String, ResourceReference>(dataSource.getDataSourceUriMap()));
        existing.setURIString(dataSource.getURIString());

        dataSource.getDataSourceUriMap().put(dataSource.getDataSourceUriMap().keySet().iterator().next(), new ResourceReference("/a/different"));

        when(service.getResource(nullable(ExecutionContext.class), eq(dataSource.getURIString()))).thenReturn(existing);
        when(service.getResource(nullable(ExecutionContext.class), eq("/a/different"))).thenReturn(existing);
        when(service.getDependentResources(nullable(ExecutionContext.class), anyString(), same(searchCriteriaFactory), anyInt(), anyInt())).thenReturn(Collections.EMPTY_LIST);

        validator.validate(ctx, dataSource);
    }

    @Test
    public void testValidate_validateUsage_dependent_added() throws Exception {
        VirtualReportDataSource existing = new VirtualReportDataSourceImpl();
        existing.setDataSourceUriMap(new HashMap<String, ResourceReference>(dataSource.getDataSourceUriMap()));
        existing.setURIString(dataSource.getURIString());

        dataSource.getDataSourceUriMap().put("c", new ResourceReference("/a/a"));

        when(service.getResource(nullable(ExecutionContext.class), eq("/a/a"))).thenReturn(new JdbcReportDataSourceImpl());
        when(service.getResource(nullable(ExecutionContext.class), eq(dataSource.getURIString()))).thenReturn(existing);
        when(service.getDependentResources(nullable(ExecutionContext.class), anyString(), same(searchCriteriaFactory), anyInt(), anyInt())).thenReturn(dependent);

        validator.validate(ctx, dataSource);
    }

    @Test
    public void testValidate_validateUsage_noDependent_added() throws Exception {
        VirtualReportDataSource existing = new VirtualReportDataSourceImpl();
        existing.setDataSourceUriMap(new HashMap<String, ResourceReference>(dataSource.getDataSourceUriMap()));
        existing.setURIString(dataSource.getURIString());

        dataSource.getDataSourceUriMap().put("c", new ResourceReference("/a/a"));

        when(service.getResource(nullable(ExecutionContext.class), eq("/a/a"))).thenReturn(new JdbcReportDataSourceImpl());
        when(service.getResource(nullable(ExecutionContext.class), eq(dataSource.getURIString()))).thenReturn(existing);
        when(service.getDependentResources(nullable(ExecutionContext.class), anyString(), same(searchCriteriaFactory), anyInt(), anyInt())).thenReturn(Collections.EMPTY_LIST);

        validator.validate(ctx, dataSource);
    }
}
