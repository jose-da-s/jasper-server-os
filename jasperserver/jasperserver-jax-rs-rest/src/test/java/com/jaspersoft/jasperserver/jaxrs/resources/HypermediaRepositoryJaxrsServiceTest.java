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
package com.jaspersoft.jasperserver.jaxrs.resources;

import com.jaspersoft.jasperserver.api.metadata.common.service.impl.hibernate.persistent.RepoFolder;
import com.jaspersoft.jasperserver.api.metadata.common.service.impl.hibernate.persistent.RepoResourceItem;
import com.jaspersoft.jasperserver.api.metadata.user.domain.User;
import com.jaspersoft.jasperserver.api.metadata.user.domain.client.UserImpl;
import com.jaspersoft.jasperserver.dto.resources.ClientResourceLookup;
import com.jaspersoft.jasperserver.jaxrs.poc.hypermedia.common.provider.RequestInfoProvider;
import com.jaspersoft.jasperserver.remote.resources.converters.ResourceConverterProvider;
import com.jaspersoft.jasperserver.remote.services.BatchRepositoryService;
import com.jaspersoft.jasperserver.remote.services.SingleRepositoryService;
import com.jaspersoft.jasperserver.search.mode.AccessType;
import com.jaspersoft.jasperserver.search.service.ItemProcessor;
import com.jaspersoft.jasperserver.search.service.RepositorySearchResult;
import com.jaspersoft.jasperserver.search.service.impl.RepositorySearchAccumulator;
import com.jaspersoft.jasperserver.war.common.ConfigurationBean;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;

import jakarta.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static java.util.Collections.EMPTY_LIST;
import static org.mockito.Mockito.*;

/**
 * @author Grant Bacon <gbacon@tibco.com>
 * @since 7.1.0
 * @see HypermediaRepositoryJaxrsService
 */
public class HypermediaRepositoryJaxrsServiceTest {

    @InjectMocks
    private HypermediaRepositoryJaxrsService hypermediaRepositoryJaxrsService = new HypermediaRepositoryJaxrsService();

    @Mock
    protected ResourceConverterProvider resourceConverterProvider;
    @Mock
    protected SingleRepositoryService singleRepositoryService;
    @Mock
    protected BatchRepositoryService batchRepositoryService;
    @Mock
    protected RequestInfoProvider requestInfoProvider;
    @Mock
    private Map<String, String> contentTypeMapping;
    @Mock
    private ConfigurationBean configurationBean;
    @Mock
    private HttpHeaders httpHeaders;
    @Mock
    private Request request;
    @Mock
    HttpServletRequest httpServletRequest;

    private User fakeUser = new UserImpl();

    @Mock
    private SecurityContextHolder securityContextHolder;

    @Before
    public void setUpTest()
    {
        MockitoAnnotations.initMocks(this);
        SecurityContext securityContext = mock(SecurityContextImpl.class);
        Authentication auth = mock (AnonymousAuthenticationToken.class);

        fakeUser.setUsername("fakeUser");

        when(auth.getPrincipal()).thenReturn(fakeUser);
        when(securityContext.getAuthentication()).thenReturn(auth);
        when(configurationBean.getOrganizationsFolderUri()).thenReturn("/organizations");
        when(configurationBean.getPublicFolderUri()).thenReturn("/public");
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    public void appendSearchResultsUsingContainerTypeTest() throws Exception
    {
        RepositorySearchResult<ClientResourceLookup> searchResult = new RepositorySearchResult<ClientResourceLookup>() {
            @Override
            public boolean isFull() {
                return false;
            }

            @Override
            public int size() {
                return 5;
            }

            @Override
            public int getClientLimit() {
                return 0;
            }

            @Override
            public int getTotalCount() {
                return 0;
            }

            @Override
            public int getClientOffset() {
                return 0;
            }

            @Override
            public int getNextOffset() {
                return 0;
            }

            @Override
            public int getNextLimit() {
                return 0;
            }

            @Override
            public List<ClientResourceLookup> getItems() {
                List<ClientResourceLookup> result = new ArrayList<ClientResourceLookup>(5);
                for (int i=0; i<5; i++) {
                    result.add(new ClientResourceLookup());
                }
                return result;
            }

            @Override
            public <U> RepositorySearchResult<U> transform(ItemProcessor<ClientResourceLookup, U> transformer) {
                return null;
            }

            @Override
            public void append(RepositorySearchResult<ClientResourceLookup> r) {

            }
        };


        when(batchRepositoryService.getResourcesForLookupClass(RepoFolder.class.getName(),
                "", "/", Arrays.asList("folder"), null, null, new ArrayList<String>(),
                0, 1000,
                false, false, null,
                AccessType.ALL, false, fakeUser, true)).thenReturn(searchResult);


        final Response response = hypermediaRepositoryJaxrsService.getResources("",
                "/", Arrays.asList("dataSource", "domain"),
               null, new ArrayList<String>(),
                Arrays.asList("folder"), "accessType",false,
                0, 1000,
                false, false,
                false, null,
                false, true,
                "application/json", httpServletRequest);

        // First call only has type folder since it is the container type
        verify(batchRepositoryService).getResourcesForLookupClass(RepoFolder.class.getName(),
                "", "/", Arrays.asList("folder"), null, null,
                EMPTY_LIST, 0,
                1000, false,
                false, null,
                AccessType.ALL, false, fakeUser,
                true);

        // Second call does not contain "folder"
        // Also, the limit will shift by 5 since that's the size that is returned by the mock searchResult
        verify(batchRepositoryService).getResourcesForLookupClass(RepoResourceItem.class.getName(),
                "", "/", Arrays.asList("dataSource", "domain"), null, null,
                EMPTY_LIST, 0,
                995, false,
                false, null,
                AccessType.ALL, false, fakeUser,
                true);

    }

}

