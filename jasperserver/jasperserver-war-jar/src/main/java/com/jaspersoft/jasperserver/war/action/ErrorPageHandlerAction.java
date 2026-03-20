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
package com.jaspersoft.jasperserver.war.action;

import com.jaspersoft.jasperserver.api.JSException;
import com.jaspersoft.jasperserver.api.SessionAttribMissingException;
import com.jaspersoft.jasperserver.api.common.error.handling.ExceptionOutputManager;
import com.jaspersoft.jasperserver.api.common.error.handling.SecureExceptionHandler;
import com.jaspersoft.jasperserver.api.engine.jasperreports.common.JSReportExecutionRequestCancelledException;
import com.jaspersoft.jasperserver.dto.common.ErrorDescriptor;
import com.jaspersoft.jasperserver.war.util.JSExceptionUtils;
import net.sf.jasperreports.web.JRInteractiveRuntimeException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.Assert;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;

/**
 * @author dlitvak
 * @version $Id$
 */
public class ErrorPageHandlerAction extends AbstractAction {
	private static final Logger logger = LogManager.getLogger(ErrorPageHandlerAction.class);
	private static final String EXCEPTION_ATTRIB = "exception";
	private static final String FLOW_EXECUTION_EXCEPTION = "flowExecutionException";

	@Resource
	private SecureExceptionHandler secureExceptionHandler;

	public void setSecureExceptionHandler(SecureExceptionHandler secureExceptionHandler1) {
		this.secureExceptionHandler = secureExceptionHandler1;
	}

	@Override
	protected void initAction() throws Exception {
		Assert.notNull(secureExceptionHandler, "errorDescriptorFactory must not be null.");
	}

	@Override
	protected Event doExecute(RequestContext context) throws Exception {
		ServletExternalContext externalContext = (ServletExternalContext) context.getExternalContext();
		HttpServletRequest request = (HttpServletRequest) externalContext.getNativeRequest();
		HttpServletResponse response = (HttpServletResponse) externalContext.getNativeResponse();

		MutableAttributeMap flashScope = context.getFlashScope();
		Throwable flowException = (Throwable) flashScope.get(FLOW_EXECUTION_EXCEPTION);
		Throwable exception = flowException != null ? flowException.getCause() : null;
		if (exception == null) {
			//from controllers or swf action
			exception = (Throwable) request.getAttribute(EXCEPTION_ATTRIB);
		}

		if (JSExceptionUtils.extractCause(exception, SessionAttribMissingException.class) != null)
			request.setAttribute("sessionAttributeMissingException", true);

		prepareErrorPage(request, response, exception);

		return success();
	}

	public void prepareErrorPage(HttpServletRequest request, HttpServletResponse response, Throwable exception) {
		if (exception == null) {
			exception = new JSException(ExceptionOutputManager.GENERIC_ERROR_MESSAGE_CODE);
		}

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		Collection<? extends GrantedAuthority> roles = authentication.getAuthorities();

		response.setHeader("JasperServerError", "true");

		if (exception != null && (exception instanceof ReportCanceledException || exception instanceof JSReportExecutionRequestCancelledException)) {
			response.setHeader("SuppressError", "true");
		}

		StringWriter stackTraceWriter = new StringWriter();
		exception.printStackTrace(new PrintWriter(stackTraceWriter));

		ErrorDescriptor errorDescriptor = secureExceptionHandler.handleException(exception);

		final RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
		requestAttributes.setAttribute("errorUID", errorDescriptor.getErrorUid(), RequestAttributes.SCOPE_REQUEST);

		if (errorDescriptor.getErrorUid() != null && !errorDescriptor.getErrorUid().isEmpty() )
			logger.error("Error UID " + errorDescriptor.getErrorUid(), exception);

		final String edMessage = errorDescriptor.getMessage();
		String[] errorMessages = (exception instanceof JRInteractiveRuntimeException) ? edMessage.split("<#_#>") : new String[] {edMessage};
		requestAttributes.setAttribute("errorMessages", errorMessages, RequestAttributes.SCOPE_REQUEST);
		if (exception instanceof JSException)
			requestAttributes.setAttribute("exceptionArgs", ((JSException)exception).getArgs(), RequestAttributes.SCOPE_REQUEST);

		String[] stackTraces = errorDescriptor.getParameters();
		if (stackTraces != null && stackTraces.length > 0 && !(exception instanceof JRInteractiveRuntimeException)) {
			requestAttributes.setAttribute("stackTraces", stackTraces, RequestAttributes.SCOPE_REQUEST);
		}
	}
}

