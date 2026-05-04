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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.util.JavaScriptUtils;

import jakarta.el.ELContext;
import jakarta.el.ELResolver;
import jakarta.servlet.jsp.JspContext;
import java.beans.FeatureDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * @author  dlitvak
 * @version $Id$
 */
public class XSSEscapeXmlELResolver extends ELResolver {
	private static final Logger log = LogManager.getLogger(XSSEscapeXmlELResolver.class);

	private static final String XSSESCAPE_XML_ELRESOLVER_PROPERTIES = "com/jaspersoft/jasperserver/jsp/XSSEscapeXmlELResolver.properties";

	/** pageContext attribute name for flag to enable XML escaping */
	public static final String ESCAPE_XSS_SCRIPT = XSSEscapeXmlELResolver.class.getName() + ".escapeScript";

	public static final String UTF8_ESCAPE_XSS_SCRIPT = XSSEscapeXmlELResolver.class.getName() + ".javaScriptEscape";
	private static final String UNESCAPED_TILE_AND_EL_ATTRIBUTES_PROP = "unescapedTileandELAttributes";

	//used with ${ajaxResponseModel} and ${ajaxError} in jsp's
	private static final String SKIP_XSS_ESCAPE_REQ_ATTRIB = "SKIP_XSS_ESCAPE";

	private static Set<String> unescapedTileandELAttributes;

	public XSSEscapeXmlELResolver() {
		if (unescapedTileandELAttributes == null) {
			Properties classProperies = new Properties();
			InputStream is = null;
			try {
				is = XSSEscapeXmlELResolver.class.getClassLoader().getResourceAsStream(XSSESCAPE_XML_ELRESOLVER_PROPERTIES);
				classProperies.load(is);
				unescapedTileandELAttributes = new HashSet<String>(Arrays.asList(classProperies.getProperty(UNESCAPED_TILE_AND_EL_ATTRIBUTES_PROP).split("\\s*,\\s*")));
			}
			catch (Exception e) {
				final String errMessage = "Failed to find " + UNESCAPED_TILE_AND_EL_ATTRIBUTES_PROP + " property in " + XSSESCAPE_XML_ELRESOLVER_PROPERTIES;
				log.error(errMessage, e);
				throw new RuntimeException(errMessage, e);
			}
			finally {
				try {
					if (is != null)
						is.close();
				}
				catch (IOException ioe) {
					log.error("Failed to close " + XSSESCAPE_XML_ELRESOLVER_PROPERTIES, ioe);
				}
			}
		}
	}

	private ThreadLocal<Boolean> excludeMe = new ThreadLocal<Boolean>() {
		@Override
		protected Boolean initialValue() {
			return Boolean.FALSE;
		}
	};

	@Override
	public Object getValue(ELContext context, Object base, Object property) {
		JspContext pageContext = (JspContext) context.getContext(JspContext.class);
		Boolean escapeScript = (Boolean) pageContext.getAttribute(ESCAPE_XSS_SCRIPT);
		Boolean javaScriptEscape = (Boolean) pageContext.getAttribute(UTF8_ESCAPE_XSS_SCRIPT);

		if (escapeScript != null && !escapeScript) {
			return null;
		}

		try {
			if (excludeMe.get()) {
				return null;
			}

			Object skipEscapePageAttrib = pageContext.getAttribute(SKIP_XSS_ESCAPE_REQ_ATTRIB);
			if (property == null
					// Tiles do not seem to have 'base' object in attrib. names.  By checking base == null,
					// we avoid attribute collisions such as frame.id (EL) and id (tiles attrib).
					|| (base == null && unescapedTileandELAttributes.contains(property.toString()))
					|| skipEscapePageAttrib != null) {
				return null;
			}

			// excludeMe.get() == false means that this is 1st invocation of the resolver
			// chain by the parent CompositeResolver.  This class is part of this chain.
			//
			// context.getELResolver() returns the parent CompositeResolver, which
			// invokes the ELResolver chain again inside getValue().  2nd invocation of
			// the resolver chain finds excludeMe.set(Boolean.TRUE), which prevents the infinite loop.
			excludeMe.set(Boolean.TRUE);

			Object obj = context.getELResolver().getValue(context, base, property);
			return escapeObj(obj, javaScriptEscape);
		} finally {
		  excludeMe.remove();
		}
	}

	@Override
	public Object invoke(ELContext context, Object base, Object method, Class<?>[] paramTypes, Object[] params) {
		JspContext pageContext = (JspContext) context.getContext(JspContext.class);
		Boolean escapeScript = (Boolean) pageContext.getAttribute(ESCAPE_XSS_SCRIPT);
		Boolean javaScriptEscape = (Boolean) pageContext.getAttribute(UTF8_ESCAPE_XSS_SCRIPT);

		if (escapeScript != null && !escapeScript) {
			return null;
		}

		try {
			if (excludeMe.get()) {
				return null;
			}

			// excludeMe.get() == false means that this is 1st invocation of the resolver
			// chain by the parent CompositeResolver.  This class is part of this chain.
			//
			// context.getELResolver() returns the parent CompositeResolver, which
			// invokes the ELResolver chain again inside getValue().  2nd invocation of
			// the resolver chain finds excludeMe.set(Boolean.TRUE), which prevents the infinite loop.
			excludeMe.set(Boolean.TRUE);

			Object obj = context.getELResolver().invoke(context, base, method, paramTypes, params);
			return escapeObj(obj, javaScriptEscape);
		} finally {
			excludeMe.remove();
		}
	}

	private Object escapeObj(Object obj, Boolean javaScriptEscape) {
		if (obj != null) {
			if (obj instanceof Object[]) {
				obj = escapeXSSArray((Object[])obj, javaScriptEscape);
			}
			else
				obj = escapeXSS(obj, javaScriptEscape);
		}
		return obj;
	}

    private Object escapeXSSArray(Object[] objArr, Boolean javaScriptEscape) {
        for (int k = 0; k < objArr.length; ++k) {
            objArr[k] = escapeXSS(objArr[k], javaScriptEscape);
        }
        return objArr;
    }

    private Object escapeXSS(Object obj, Boolean javaScriptEscape) {
        if (isTypeSuitable(obj)) {
            if (javaScriptEscape != null && javaScriptEscape) {
                return JavaScriptUtils.javaScriptEscape(obj.toString());
            }
            else
            	return EscapeXssScript.escape(obj.toString());
        }
        else
            return obj;
    }

    private boolean isTypeSuitable(Object obj) {
        return obj instanceof String
                || obj instanceof StringBuffer
                || obj instanceof StringBuilder 
                || obj instanceof Locale 
                || obj instanceof JSONObject;
    }

	@Override
	public Class<?> getType(ELContext context, Object base, Object property) {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public void setValue(ELContext context, Object base, Object property, Object value) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public boolean isReadOnly(ELContext context, Object base, Object property) {
		return true;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext context, Object base) {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public Class<?> getCommonPropertyType(ELContext context, Object base) {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}
}

