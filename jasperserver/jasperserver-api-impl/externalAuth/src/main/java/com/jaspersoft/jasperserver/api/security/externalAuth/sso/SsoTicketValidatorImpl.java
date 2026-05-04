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

package com.jaspersoft.jasperserver.api.security.externalAuth.sso;

import com.jaspersoft.jasperserver.api.security.externalAuth.ExternalAuthProperties;
import com.jaspersoft.jasperserver.api.security.externalAuth.ExternalUserDetails;
import org.apache.http.client.utils.URIBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.xerces.parsers.DOMParser;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;


/**
 * This class validates SSO token by contacting an external SSO server
 * Extends {@link AbstractSsoTicketValidator}
 *
 * @author Chaim Arbiv
 */

public class SsoTicketValidatorImpl extends AbstractSsoTicketValidator {
	private static final Logger logger = LogManager.getLogger(SsoTicketValidatorImpl.class);
	public static final String PRINCIPAL_XML_TAG_NAME = "principalXmlTagName";

    private String service;

	@Override
    public ExternalUserDetails validate(final Object ticket) throws AuthenticationServiceException {
		if (ticket == null || "".equals(ticket.toString().trim()))
			throw new AuthenticationServiceException("No SSO token");

		final URI validationUrl = constructValidationUrl((String) ticket);
		final ClientHttpResponse serverResponse = requestTicketValidationFromSsoServer(validationUrl);

		if (serverResponse == null) {
			throw new AuthenticationServiceException("The CAS server returned no response.");
		}

		return parseResponseFromServer(serverResponse);
    }

	protected URI constructValidationUrl(final String ticket) throws AuthenticationServiceException {
		final Map<String, String> urlParameters = new HashMap<String, String>();
		try {
			logger.debug("Constructing SSO token validation URL (ticket: " + ticket + ")");

			final ExternalAuthProperties externalAuthProperties = getExternalAuthProperties();
			String ticketParamName = externalAuthProperties.getTicketParameterName();
			urlParameters.put(ticketParamName, ticket);

			String serviceParameterName = externalAuthProperties.getServiceParameterName();
			if (serviceParameterName != null && serviceParameterName.length() > 0) {

                // in case of SOAP the service does not have to match the request. in that case we will specify the service
                if (getService() != null && getService().length() > 0){
                    urlParameters.put(serviceParameterName, getService());
                }
                else {
                    HttpServletRequest req = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
                    StringBuffer requestURL = req.getRequestURL();

                    urlParameters.put(serviceParameterName, requestURL.toString());
                }
			}

			URIBuilder uriBuilder = new URIBuilder(externalAuthProperties.getSsoServerTicketValidationUrl());
			for (Map.Entry<String, String> param : urlParameters.entrySet()) {
				uriBuilder.setParameter(param.getKey(), param.getValue());
			}

			return uriBuilder.build();
		} catch (URISyntaxException e) {
			logger.error("Failed to construct the token validation URL (ticket: " + ticket + ")", e);
			throw new AuthenticationServiceException(e.getMessage(), e);
		}
	}

    protected ClientHttpResponse requestTicketValidationFromSsoServer(URI validationUrl) throws AuthenticationServiceException {
        try {
			logger.debug("Requesting SSO token validation from SSO server: " + validationUrl);
            ClientHttpRequest req = getClientHttpRequestFactory().createRequest(validationUrl, HttpMethod.GET);
            ClientHttpResponse res = req.execute();
            return res;
        } catch (IOException e) {
			logger.error("Failed to validate SSO token (" + validationUrl + ")", e);
            throw new AuthenticationServiceException(e.getMessage(), e);
        }
    }

	/**
	 * Parse token validation response  from SSO server.
	 * @param response response from SSO server
	 *
	 * @return ExternalUserDetails
	 * @throws AuthenticationServiceException
	 */
    @Override
    protected ExternalUserDetails parseResponseFromServer(ClientHttpResponse response) throws AuthenticationServiceException {
		logger.debug("Parsing response from SSO server");

		final String principalNameFromSsoResponse = getPrincipalNameFromSsoResponse(response);
        return new ExternalUserDetails(principalNameFromSsoResponse);
    }

	/**
	 * Override this method to handle a response in any format other than XML
	 *
	 * @param response
	 * @return
	 */
    protected String getPrincipalNameFromSsoResponse(ClientHttpResponse response) {
        try {
			DOMParser domParser = new DOMParser();
			domParser.parse(new InputSource(response.getBody()));
			String principalXmlTagName = getExternalAuthProperties().getCustomSsoProperty(PRINCIPAL_XML_TAG_NAME);
			NodeList nodeList = domParser.getDocument().getElementsByTagName(principalXmlTagName);
			if (nodeList == null || nodeList.getLength() == 0) {
				logger.warn("Could not find principal xml tag in the SSO server response.");
				throw new AuthenticationServiceException("SSO Authentication failed");
			}

			return nodeList.item(0).getTextContent();
        } catch (Exception e) {
			logger.error("Failed to get principal from the SSO server response", e);
            throw new AuthenticationServiceException(e.getMessage(), e);
        }
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }


}

