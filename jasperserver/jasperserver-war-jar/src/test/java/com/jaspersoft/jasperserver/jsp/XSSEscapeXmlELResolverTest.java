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

package com.jaspersoft.jasperserver.jsp;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.web.util.JavaScriptUtils;

import jakarta.el.ELContext;
import jakarta.el.ELResolver;
import jakarta.el.MapELResolver;
import jakarta.servlet.Servlet;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.jsp.JspContext;
import jakarta.servlet.jsp.JspWriter;
import jakarta.servlet.jsp.PageContext;
import jakarta.servlet.jsp.el.ExpressionEvaluator;
import jakarta.servlet.jsp.el.VariableResolver;

import org.springframework.binding.expression.el.DefaultELContext;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author dlitvak
 * @version $Id$
 */
public class XSSEscapeXmlELResolverTest {

	private static final String EL_PROPERTY = "elProperty";
	private static final String EL_NON_ESCAPED_PROPERTY = "bodyContent";
	private static final String EL_PROPERTY_AJAX_RESPONSE_MODEL = "ajaxResponseModel";
	private static final String SKIP_XSS_ESCAPE_REQ_ATTRIB = "SKIP_XSS_ESCAPE";

	private PageContext pageContext;
	private ELResolver xssElResolver;
	private ELContext elContext;
	private Map<String, String> elBaseMap;


	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);

		xssElResolver = new XSSEscapeXmlELResolver();

		ELResolver baseElResolver = new MapELResolver();
		elContext = new DefaultELContext(baseElResolver,null,null);
		pageContext = new SimplePageContext(elContext);
		elContext.putContext(JspContext.class, pageContext);
		elBaseMap = new HashMap<String, String>();
	}

	/**
	 * Test that < and > are properly escaped as &lt; and &gt;
	 */
    @Test
    public void testGetValue() {
		elBaseMap.put(EL_PROPERTY, "<script>");
		Object jspVal = xssElResolver.getValue(elContext, elBaseMap, EL_PROPERTY);
		String escVal = EscapeXssScript.escape(elBaseMap.get(EL_PROPERTY));

		Assert.assertNotNull("xssElResolver.getValue is null", jspVal);
		Assert.assertNotNull("EscapeXssScript.escape is null", escVal);
		Assert.assertEquals("Result returned by XSS resolver is not equal that from XSS Escaper.", jspVal.toString(), escVal);
    }

/**
	 * Test that < and > are NOT escaped when the resolved EL attribute is part of the exception list
	 * in XSSEscapeXmlELResolver.properties AND base object is null.
	 * Usually, it means that this attribute is part of JSP Tiles.  We assume there is no base object in Tiles.
	 */
    @Test
    public void testGetValueNotEscaping() {
		Object jspVal = xssElResolver.getValue(elContext, null, EL_NON_ESCAPED_PROPERTY);

		Assert.assertNull("testGetValueNotEscaping: xssElResolver.getValue should be null", jspVal);
    }

/**
	 * Test that < and > are escaped when the resolved EL attribute is part of the exception list
	 * in XSSEscapeXmlELResolver.properties AND base object is NOT null.
	 * It means that this attribute is NOT a part of JSP Tiles.  We assume there is no base object in Tiles.
	 */
	@Test
	public void testGetValueEscapingWithNonNullBaseObj() {
		elBaseMap.put(EL_NON_ESCAPED_PROPERTY, "<script>");
		Object jspVal = xssElResolver.getValue(elContext, elBaseMap, EL_NON_ESCAPED_PROPERTY);
		String escVal = EscapeXssScript.escape(elBaseMap.get(EL_NON_ESCAPED_PROPERTY));

		Assert.assertNotNull("testGetValueEscapingWithNonNullBaseObj: xssElResolver.getValue should NOT be null", jspVal);
		Assert.assertEquals("Result returned by XSS resolver is not equal that from XSS Escaper.", jspVal.toString(), escVal);
	}

	/**
	 * Test that < and > are NOT escaped when EL is inside <js:out escapeScript=false>${elProperty}</js:out>.
	 */
	@Test
	public void testGetValueWithEscapeScriptFalse() {
		pageContext.setAttribute(XSSEscapeXmlELResolver.ESCAPE_XSS_SCRIPT, false);

		elBaseMap.put(EL_PROPERTY, "<script>");
		Object jspVal = xssElResolver.getValue(elContext, elBaseMap, EL_PROPERTY);

		Assert.assertNull("testGetValueWithEscapeScriptFalse: xssElResolver.getValue should be null", jspVal);
	}

/**
	 * Test that < and > are UTF-8 escaped when EL is inside <js:out javaScriptEscape=true>${elProperty}</js:out>.
	 */
	@Test
	public void testGetValueScriptWithJavaScriptEscapeTrue() {
		pageContext.setAttribute(XSSEscapeXmlELResolver.UTF8_ESCAPE_XSS_SCRIPT, true);

		elBaseMap.put(EL_PROPERTY, "<script>");
		Object jspVal = xssElResolver.getValue(elContext, elBaseMap, EL_PROPERTY);
		String escVal = JavaScriptUtils.javaScriptEscape(elBaseMap.get(EL_PROPERTY));

		Assert.assertNotNull("xssElResolver.getValue is null", jspVal);
		Assert.assertNotNull("JavaScriptUtils.javaScriptEscape is null", escVal);
		Assert.assertEquals("Result returned by XSS resolver should be equal that from JavaScriptUtils.javaScriptEscape.", jspVal.toString(), escVal);
	}

	/**
	 * Test that there is no escaping when SKIP_XSS_ESCAPE Request Attrib is set.
	 */
	@Test
	public void testGetValueWith_SKIP_XSS_ESCAPE_RequestAttrib() {
		try {
			pageContext.setAttribute(SKIP_XSS_ESCAPE_REQ_ATTRIB, true);

			final String elValue = "{\"hello\": 'JSON'}";
			elBaseMap.put(EL_PROPERTY_AJAX_RESPONSE_MODEL, elValue);
			Object jspVal = xssElResolver.getValue(elContext, elBaseMap, EL_PROPERTY_AJAX_RESPONSE_MODEL);

			Assert.assertNull("xssElResolver.getValue should return null when SKIP_XSS_ESCAPE Request Attrib is set.", jspVal);
		}
		finally {
			pageContext.setAttribute(SKIP_XSS_ESCAPE_REQ_ATTRIB, false);
		}
	}


	private static class SimplePageContext extends PageContext  {

		private ELContext elContext;
		private final HashMap<String, Object> applicationScope = new HashMap<>();
		private final HashMap<String, Object> sessionScope = new HashMap<>();
		private final HashMap<String, Object> requestScope = new HashMap<>();
		private final HashMap<String, Object> pageScope = new HashMap<>();

		public SimplePageContext(ELContext elContext) {
			this.elContext = elContext;
		}

		@Override
		public void setAttribute(String name, Object value) {
			setAttribute(name, value, PAGE_SCOPE);
		}

		@Override
		public void setAttribute(String name, Object value, int scope) {
			switch (scope) {
				case APPLICATION_SCOPE:
					applicationScope.put(name, value);
					break;
				case SESSION_SCOPE:
					sessionScope.put(name, value);
					break;
				case REQUEST_SCOPE:
					requestScope.put(name, value);
					break;
				case PAGE_SCOPE:
					pageScope.put(name, value);
					break;
			}
		}

		@Override
		public Object getAttribute(String name) {
			return getAttribute(name, PAGE_SCOPE);
		}

		@Override
		public Object getAttribute(String name, int scope) {
			switch (scope) {
				case APPLICATION_SCOPE:
					return applicationScope.get(name);
				case SESSION_SCOPE:
					return sessionScope.get(name);
				case REQUEST_SCOPE:
					return requestScope.get(name);
				case PAGE_SCOPE:
					return pageScope.get(name);
				default:
					return null;
			}
		}

		@Override
		public Object findAttribute(String name) {
			if (pageScope.containsKey(name)) {
				return pageScope.get(name);
			} else if (requestScope.containsKey(name)) {
				return requestScope.get(name);
			} else if (sessionScope.containsKey(name)) {
				return sessionScope.get(name);
			} else {
				return applicationScope.get(name);
			}
		}

		@Override
		public void removeAttribute(String name) {
			removeAttribute(name, PAGE_SCOPE);
		}

		@Override
		public void removeAttribute(String name, int scope) {
			switch (scope) {
				case APPLICATION_SCOPE:
					applicationScope.remove(name);
					break;
				case SESSION_SCOPE:
					sessionScope.remove(name);
					break;
				case REQUEST_SCOPE:
					requestScope.remove(name);
					break;
				case PAGE_SCOPE:
					pageScope.remove(name);
					break;
			}
		}

		@Override
		public int getAttributesScope(String name) {
			if (name == null) {
				throw new NullPointerException("name");
			}
			if (pageScope.get(name) != null) {
				return PAGE_SCOPE;
			} else if (requestScope.get(name) != null) {
				return REQUEST_SCOPE;
			} else if (sessionScope.get(name) != null) {
				return APPLICATION_SCOPE;
			} else if (applicationScope.get(name) != null) {
				return APPLICATION_SCOPE;
			} else {
				return 0;
			}
		}

		@Override
		public Enumeration<String> getAttributeNamesInScope(int scope) {
			HashMap<String, Object> scopeMap;
			switch (scope) {
				case APPLICATION_SCOPE:
					scopeMap = applicationScope;
					break;
				case SESSION_SCOPE:
					scopeMap = sessionScope;
					break;
				case REQUEST_SCOPE:
					scopeMap = requestScope;
					break;
				case PAGE_SCOPE:
					scopeMap = pageScope;
					break;
				default:
					throw new IllegalArgumentException("unknown scope constant: " + scope);
			}
			return Collections.enumeration(scopeMap.keySet());
		}

		@Override
		public ELContext getELContext() {
			return elContext;
		}

		@Override
		public void initialize(Servlet servlet, ServletRequest request, ServletResponse response,
				String errorPageURL, boolean needsSession, int bufferSize, boolean autoFlush)
				throws IOException, IllegalStateException, IllegalArgumentException {
		}

		@Override
		public void release() {
		}

		@Override
		public HttpSession getSession() {
			throw new UnsupportedOperationException("getSession()");
		}

		@Override
		public Object getPage() {
			throw new UnsupportedOperationException("getPage()");
		}

		@Override
		public ServletRequest getRequest() {
			throw new UnsupportedOperationException("getRequest()");
		}

		@Override
		public ServletResponse getResponse() {
			throw new UnsupportedOperationException("getResponse()");
		}

		@Override
		public Exception getException() {
			throw new UnsupportedOperationException("getException()");
		}

		@Override
		public ServletConfig getServletConfig() {
			throw new UnsupportedOperationException("getServletConfig()");
		}

		@Override
		public ServletContext getServletContext() {
			throw new UnsupportedOperationException("getServletContext()");
		}

		@Override
		public void forward(String relativeUrlPath) throws ServletException, IOException {
			throw new UnsupportedOperationException("forward(String relativeUrlPath)");
		}

		@Override
		public void include(String relativeUrlPath) throws ServletException, IOException {
			throw new UnsupportedOperationException("include(String relativeUrlPath)");
		}

		@Override
		public void include(String relativeUrlPath, boolean flush) throws ServletException, IOException {
			throw new UnsupportedOperationException("include(String relativeUrlPath, boolean flush)");
		}

		@Override
		public void handlePageException(Exception e) throws ServletException, IOException {
			throw new UnsupportedOperationException("include(String relativeUrlPath, boolean flush)");
		}

		@Override
		public void handlePageException(Throwable t) throws ServletException, IOException {
			throw new UnsupportedOperationException("handlePageException(Throwable t)");
		}

		@Override
		public JspWriter getOut() {
			throw new UnsupportedOperationException("getOut()");
		}

		@Override
		public ExpressionEvaluator getExpressionEvaluator() {
			throw new UnsupportedOperationException("getExpressionEvaluator()");
		}

		@Override
		public VariableResolver getVariableResolver() {
			throw new UnsupportedOperationException("getVariableResolver()");
		}
	};
}