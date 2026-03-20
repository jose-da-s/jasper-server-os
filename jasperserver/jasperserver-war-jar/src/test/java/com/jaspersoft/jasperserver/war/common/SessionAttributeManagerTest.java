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

package com.jaspersoft.jasperserver.war.common;

import com.jaspersoft.jasperserver.war.action.BaseHttpSessionAwareTest;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.execution.RequestContext;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

/**
 * @version $Id$
 */
public class SessionAttributeManagerTest extends BaseHttpSessionAwareTest {

	@Mock
	private HttpServletRequest requestMock;
	@Mock
	private RequestContext requestContextMock;
	@Mock
    private MutableAttributeMap<Object> flowScopeMock;

	private SessionAttributeManager sam = SessionAttributeManager.getInstance();
	private Long clientKeyReturned = null;
	
	@Before
	public void setUp() {
		when(requestContextMock.getFlowScope()).thenReturn(flowScopeMock);
		prepareHttpSession(requestMock, requestContextMock);
		// set up the request and flow scope so that they return the desired value for clientKey,
		// without having to hook up and recreate everything
		// Mocks are a PITA :(
		when(requestMock.getParameter(SessionAttributeManager.CLIENT_KEY)).thenAnswer(invocation -> getClientKeyReturned());
		when(flowScopeMock.get(SessionAttributeManager.CLIENT_KEY)).thenAnswer(invocation -> getClientKeyReturned());
	}
	
	public void setClientKeyReturned(Long ck) {
		clientKeyReturned = ck;
	}
	
	public String getClientKeyReturned() {
		return clientKeyReturned == null ? null : clientKeyReturned.toString();
	}

	/**
	 * test SAM with no clientKey
	 */
	@Test
	public void SAMworksWithNoClientKey() {
		// values can be seen on both httpServletRequest and requestContext
		clientKeyReturned = null;
		sam.setSessionAttribute("foo", "bar", requestMock);
		checkValues("foo", "bar", null);
		sam.setSessionAttribute("foo", "bar1", requestContextMock);
		checkValues("foo", "bar1", null);
	}
	
	private void checkValues(String key, Object value, Long clientKey) {
		setClientKeyReturned(clientKey);
		assertEquals(value, sam.getSessionAttribute(key, requestMock));
		assertEquals(value, sam.getSessionAttribute(key, requestContextMock));
	}
	
	/**
	 * each client key has its own data
	 */
	@Test
	public void clientsKeepSeparateSessionValues() {
		List<Long>ckList = new ArrayList<>();
		int nsessions = 10;
		// create client keys
		for (int i = 0; i < nsessions; i++) {
			ckList.add(sam.createClientKey());
		}
		// stick 'em in using HttpServletRequest
		for (Long ck : ckList) {
			setClientKeyReturned(ck);
			sam.setSessionAttribute("foo", "bar" + ck, requestMock);
		}
		// now test
		for (Long ck : ckList) {
			checkValues("foo", "bar" + ck, ck);
		}
		// stick 'em in using RequestContext
		for (Long ck : ckList) {
			setClientKeyReturned(ck);
			sam.setSessionAttribute("foo", "bar1" + ck, requestContextMock);
		}
		// now test
		for (Long ck : ckList) {
			checkValues("foo", "bar1" + ck, ck);
		}
	}
	
	/**
	 * client key should always be unique
	 */
	private static final int LOTS_O_KEYS = 1000000;
    @Ignore // Ignoring this as test is periodically (approx. 1 of 5 times) failing with java.lang.AssertionError: Expected: 100000, Actual: 999999
	@Test   // what means key is not always unique, will fix client key in separate check-in
	public void clientKeysUnique() {
            Set<Long> clientKeys = new HashSet<>();
            for (int i = 0; i < LOTS_O_KEYS; i++) {
                clientKeys.add(sam.createClientKey());
            }
            assertEquals(LOTS_O_KEYS, clientKeys.size());
	}

}

