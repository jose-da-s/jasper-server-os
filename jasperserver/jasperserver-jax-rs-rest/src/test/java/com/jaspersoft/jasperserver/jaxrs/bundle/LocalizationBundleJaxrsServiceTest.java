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
package com.jaspersoft.jasperserver.jaxrs.bundle;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Providers;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.when;
import static org.testng.Assert.*;

/**
 * <p></p>
 *
 * @author yaroslav.kovalchyk
 * @version $Id$
 */
public class LocalizationBundleJaxrsServiceTest {
    private static final String BUNDLE1_NAME = "bundle1";
    private static final String BUNDLE2_NAME = "bundle2";
    private static final String BUNDLE3_NAME = "bundle3";
    private static final String BUNDLE4_NAME = "bundle4";
    @InjectMocks
    private LocalizationBundleJaxrsService service = new LocalizationBundleJaxrsService();
    @Mock
    private ExposedResourceBundleMessageSource messageSource;
    @Mock
    private HttpHeaders httpHeaders;
    @Mock
    private Request request;
    @Mock
    HttpServletRequest httpServletRequest;
    @Mock
    private Providers providers;
    private Locale locale = Locale.US;
    private Map<String, String> bundle1 = new HashMap<String, String>() {{
        put(BUNDLE1_NAME + ".key1", BUNDLE1_NAME + ".message1");
        put(BUNDLE1_NAME + ".key2", BUNDLE1_NAME + ".message2");
        put(BUNDLE1_NAME + ".key3", BUNDLE1_NAME + ".message3");
        put(BUNDLE1_NAME + ".key4", BUNDLE1_NAME + ".message4");
    }};
    private Map<String, String> bundle2 = new HashMap<String, String>() {{
        put(BUNDLE2_NAME + ".key1", BUNDLE2_NAME + ".message1");
        put(BUNDLE2_NAME + ".key2", BUNDLE2_NAME + ".message2");
        put(BUNDLE2_NAME + ".key3", BUNDLE2_NAME + ".message3");
        put(BUNDLE2_NAME + ".key4", BUNDLE2_NAME + ".message4");
    }};
    private Map<String, String> bundle3 = new HashMap<String, String>() {{
        put(BUNDLE3_NAME + ".key1", BUNDLE3_NAME + ".message1");
        put(BUNDLE3_NAME + ".key2", BUNDLE3_NAME + ".message2");
        put(BUNDLE3_NAME + ".key3", BUNDLE3_NAME + ".message3");
        put(BUNDLE3_NAME + ".key4", BUNDLE3_NAME + ".message4");
    }};
    private Map<String, String> bundle4 = new HashMap<String, String>() {{
        put(BUNDLE4_NAME + ".key1", BUNDLE4_NAME + ".message1");
        put(BUNDLE4_NAME + ".key2", BUNDLE4_NAME + ".message2");
        put(BUNDLE4_NAME + ".key3", BUNDLE4_NAME + ".message3");
        put(BUNDLE4_NAME + ".key4", BUNDLE4_NAME + ".message4");
    }};

    @BeforeClass
    public void init() {
        MockitoAnnotations.initMocks(this);
        final ArrayList<Locale> locales = new ArrayList<Locale>();
        locales.add(locale);
        when(httpHeaders.getAcceptableLanguages()).thenReturn(locales);
        when(messageSource.getAllMessagesForBaseName(any(String.class), same(locale))).then(new Answer<Map<String, String>>() {
            @Override
            public Map<String, String> answer(InvocationOnMock invocation) throws Throwable {
                final Object bundleName = invocation.getArguments()[0];
                if (BUNDLE1_NAME.equals(bundleName)) return bundle1;
                if (BUNDLE2_NAME.equals(bundleName)) return bundle2;
                if (BUNDLE3_NAME.equals(bundleName)) return bundle3;
                if (BUNDLE4_NAME.equals(bundleName)) return bundle4;
                return null;
            }
        });
    }

    @Test
    public void setBundleNames() {
        final List<String> bundlePathsList = new ArrayList<String>();
        bundlePathsList.add("/bundles/" + BUNDLE1_NAME);
        bundlePathsList.add("/bundles/" + BUNDLE2_NAME);
        bundlePathsList.add("/bundles/" + BUNDLE3_NAME);
        bundlePathsList.add("/bundles/" + BUNDLE4_NAME);
        service.setBundleNames(bundlePathsList);
        final List<String> bundleNames = service.bundleNames;
        assertNotNull(bundleNames);
        assertEquals(bundleNames.size(), bundlePathsList.size());
        assertTrue(bundleNames.contains(BUNDLE1_NAME));
        assertTrue(bundleNames.contains(BUNDLE2_NAME));
        assertTrue(bundleNames.contains(BUNDLE3_NAME));
        assertTrue(bundleNames.contains(BUNDLE4_NAME));
    }

    @Test(dependsOnMethods = "setBundleNames")
    public void getBundles_expandedFalse() {
        final Response response = service.getBundles(false, httpHeaders, request, httpServletRequest);
        assertNotNull(response);
        final Object entity = response.getEntity();
        assertSame(entity, service.bundleNames);
    }

    @Test(dependsOnMethods = "setBundleNames")
    public void getBundles_expandedTrue() {
        final Response response = service.getBundles(true, httpHeaders, request, httpServletRequest);
        assertNotNull(response);
        assertTrue(response.getEntity() instanceof ObjectNode);
        ObjectNode json = (ObjectNode) response.getEntity();
        assertEquals(json.size(), 4);
        assertBundle((ObjectNode) json.get(BUNDLE1_NAME), bundle1);
        assertBundle((ObjectNode) json.get(BUNDLE2_NAME), bundle2);
        assertBundle((ObjectNode) json.get(BUNDLE3_NAME), bundle3);
        assertBundle((ObjectNode) json.get(BUNDLE4_NAME), bundle4);
    }

    @Test(dependsOnMethods = "setBundleNames")
    public void getBundle() {
        assertSame(service.getBundle(BUNDLE1_NAME, httpHeaders, request).getEntity(), bundle1);
        assertSame(service.getBundle(BUNDLE2_NAME, httpHeaders, request).getEntity(), bundle2);
        assertSame(service.getBundle(BUNDLE3_NAME, httpHeaders, request).getEntity(), bundle3);
        assertSame(service.getBundle(BUNDLE4_NAME, httpHeaders, request).getEntity(), bundle4);
    }

    private void assertBundle(ObjectNode json, Map<String, String> bundle) {
        assertNotNull(json);
        assertEquals(json.size(), bundle.size());
        for(String currentKey : bundle.keySet()){
            assertEquals(json.get(currentKey).textValue(), bundle.get(currentKey));
        }
    }
}

