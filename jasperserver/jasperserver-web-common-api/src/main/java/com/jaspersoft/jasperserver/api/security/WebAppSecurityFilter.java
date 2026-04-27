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

package com.jaspersoft.jasperserver.api.security;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.servlet.LocaleResolver;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;

import static com.jaspersoft.jasperserver.api.security.validators.Validator.validateRequestParams;

/**
 * Filter level input validation for parameter/values in the request.
 */
public class WebAppSecurityFilter implements Filter, InitializingBean {
    private static final Logger logger = LogManager.getLogger(WebAppSecurityFilter.class);

    private static final Logger LOG = LogManager.getLogger(WebAppSecurityFilter.class);
    private static final String AJAX_REQUEST_HEADER_FLAG = "x-requested-with";
    private static final String ERR_MSG_INPUT_VALIDATION = "message.validation.input";
    private static final String AJAX_RESPONSE_HEADER = "JasperServerError";
    private static final String AJAX_RESPONSE_HEADER_STATUS = "true";
    private static final String FRAME_ANCESTORS_CSP_HEADER_VALUE = "frame-ancestors";

    private MessageSource messages;
    private String redirectUrl;
    private LocaleResolver localeResolver;
    private boolean showStacktraceMessage = true;


    // HSTS
    private static final String HSTS_HEADER_NAME = "Strict-Transport-Security";
    private boolean hstsEnabled = true;
    private int hstsMaxAgeSeconds = 0;
    private boolean hstsIncludeSubDomains = false;
    private boolean hstsPreload = false;
    private String hstsHeaderValue;

    // Click-jacking protection
    private static final String ANTI_CLICK_JACKING_HEADER_NAME = "X-Frame-Options";
    private static final String CSP_HEADER_NAME = "Content-Security-Policy";
    private boolean antiClickJackingEnabled = true;
    private XFrameOption antiClickJackingOption = XFrameOption.DENY;
    private String antiClickJackingUri_XFrameOption;
    private String antiClickJackingUriList_CSP;
    private String antiClickJackingHeaderValue;
    private String antiClickJackingCSPHeaderValue;

    // Block content sniffing
    private static final String BLOCK_CONTENT_TYPE_SNIFFING_HEADER_NAME = "X-Content-Type-Options";
    private static final String BLOCK_CONTENT_TYPE_SNIFFING_HEADER_VALUE = "nosniff";
    private boolean blockContentTypeSniffingEnabled = true;

    // Cross-site scripting filter protection
    private static final String XSS_PROTECTION_HEADER_NAME = "X-XSS-Protection";
    private static final String XSS_PROTECTION_HEADER_VALUE = "1; mode=block";
    private boolean xssProtectionEnabled = true;


    @Override
    public void afterPropertiesSet() throws Exception {
        // Build HSTS header value
        StringBuilder hstsValue = new StringBuilder("max-age=");
        hstsValue.append(hstsMaxAgeSeconds);
        if (hstsIncludeSubDomains) {
            hstsValue.append(";includeSubDomains");
        }
        if (hstsPreload) {
            hstsValue.append(";preload");
        }
        hstsHeaderValue = hstsValue.toString();

        // Anti click-jacking
        StringBuilder cjValue = new StringBuilder(antiClickJackingOption.headerValue);
        if (antiClickJackingOption == XFrameOption.ALLOW_FROM) {
            cjValue.append(' ');
            cjValue.append(antiClickJackingUri_XFrameOption);

            // X-Frame-Options: ALLOW-FROM <url> is not supported in Chrome.
            // We need to set a CSP header for Chrome browser as follows:
            // Content-Security-Policy: frame-ancestors <source>;
            StringBuilder cjCSPValue = new StringBuilder(FRAME_ANCESTORS_CSP_HEADER_VALUE);
            cjCSPValue.append(' ');
            cjCSPValue.append(antiClickJackingUriList_CSP);
            antiClickJackingCSPHeaderValue = cjCSPValue.toString();
        }
        antiClickJackingHeaderValue = cjValue.toString();
    }

    public void init(FilterConfig filterConfig) throws ServletException {
        //NOOP
    }

    public void destroy() {
        //NOOP
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest)request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        if (response.isCommitted()) {
            throw new ServletException("httpHeaderSecurityFilter.committed");
        }

        // HSTS
        if (hstsEnabled && request.isSecure()) {
            httpResponse.setHeader(HSTS_HEADER_NAME, hstsHeaderValue);
        }

        // anti click-jacking
        if (antiClickJackingEnabled) {
            httpResponse.setHeader(ANTI_CLICK_JACKING_HEADER_NAME, antiClickJackingHeaderValue);
            if (antiClickJackingCSPHeaderValue != null && antiClickJackingCSPHeaderValue.startsWith(FRAME_ANCESTORS_CSP_HEADER_VALUE))
                httpResponse.setHeader(CSP_HEADER_NAME, antiClickJackingCSPHeaderValue);
        }

        // Block content type sniffing
        if (blockContentTypeSniffingEnabled) {
            httpResponse.setHeader(BLOCK_CONTENT_TYPE_SNIFFING_HEADER_NAME,
                    BLOCK_CONTENT_TYPE_SNIFFING_HEADER_VALUE);
        }

        // cross-site scripting filter protection
        if (xssProtectionEnabled) {
            httpResponse.setHeader(XSS_PROTECTION_HEADER_NAME, XSS_PROTECTION_HEADER_VALUE);
        }

        LocaleContextHolder.setLocale(localeResolver.resolveLocale(httpRequest));
        if (!validateRequestParams(httpRequest))
            processInvalidRequest(httpRequest, httpResponse);
        else
            filterChain.doFilter(httpRequest, response);
    }

    private void processInvalidRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        LOG.warn("Request " + request.getRequestURL() + " was deemed invalid.");

        if (isTextOnlyResponse(request))
            sendTextOnly(response);
        else
            response.sendRedirect(response.encodeRedirectURL(request.getContextPath() + redirectUrl));
    }

    /*
     * the header is set in core.ajax.js
     * see doPost and doGet
     */
    protected boolean isTextOnlyResponse(HttpServletRequest request)
    {
        // It's enough just to check whether this header is present, and do not rely on it's value,
        // because jQuery sets it's own value for this header by default.
        return request.getHeader(AJAX_REQUEST_HEADER_FLAG) != null;
    }

    protected void sendTextOnly(HttpServletResponse response)
    {
        response.setHeader(AJAX_RESPONSE_HEADER, AJAX_RESPONSE_HEADER_STATUS);
        response.setCharacterEncoding("UTF-8");

        PrintWriter out = null;
        try
        {
            out = response.getWriter();
            out.write( messages.getMessage(ERR_MSG_INPUT_VALIDATION,new Object[]{}, LocaleContextHolder.getLocale()) );
            out.close();
        }
        catch (IOException ioe)
        {
            LOG.error(ioe);
        }
    }

    public MessageSource getMessages() {
        return messages;
    }

    public void setMessages(MessageSource messages) {
        this.messages = messages;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }
    public LocaleResolver getLocaleResolver() {
        return localeResolver;
    }
    public void setLocaleResolver(LocaleResolver localeResolver) {
        this.localeResolver = localeResolver;
    }

    public boolean isShowStacktraceMessage() {
        return showStacktraceMessage;
    }

    public void setShowStacktraceMessage(boolean showStacktraceMessage) {
        this.showStacktraceMessage = showStacktraceMessage;
    }


    public boolean isHstsEnabled() {
        return hstsEnabled;
    }


    public void setHstsEnabled(boolean hstsEnabled) {
        this.hstsEnabled = hstsEnabled;
    }


    public int getHstsMaxAgeSeconds() {
        return hstsMaxAgeSeconds;
    }


    public void setHstsMaxAgeSeconds(int hstsMaxAgeSeconds) {
        if (hstsMaxAgeSeconds < 0) {
            this.hstsMaxAgeSeconds = 0;
        } else {
            this.hstsMaxAgeSeconds = hstsMaxAgeSeconds;
        }
    }


    public boolean isHstsIncludeSubDomains() {
        return hstsIncludeSubDomains;
    }


    public void setHstsIncludeSubDomains(boolean hstsIncludeSubDomains) {
        this.hstsIncludeSubDomains = hstsIncludeSubDomains;
    }

    public boolean isHstsPreload() {
        return hstsPreload;
    }


    public void setHstsPreload(boolean hstsPreload) {
        this.hstsPreload = hstsPreload;
    }

    public boolean isAntiClickJackingEnabled() {
        return antiClickJackingEnabled;
    }

    public void setAntiClickJackingEnabled(boolean antiClickJackingEnabled) {
        this.antiClickJackingEnabled = antiClickJackingEnabled;
    }

    public String getAntiClickJackingOption() {
        return antiClickJackingOption.toString();
    }

    public void setAntiClickJackingOption(String antiClickJackingOption) {
        for (XFrameOption option : XFrameOption.values()) {
            if (option.getHeaderValue().equalsIgnoreCase(antiClickJackingOption)) {
                this.antiClickJackingOption = option;
                return;
            }
        }
        throw new IllegalArgumentException("httpHeaderSecurityFilter.clickjack.invalid: " +
                antiClickJackingOption);
    }

    public String getAntiClickJackingUri() {
        return antiClickJackingUri_XFrameOption.toString();
    }

    public boolean isBlockContentTypeSniffingEnabled() {
        return blockContentTypeSniffingEnabled;
    }

    public void setBlockContentTypeSniffingEnabled(
            boolean blockContentTypeSniffingEnabled) {
        this.blockContentTypeSniffingEnabled = blockContentTypeSniffingEnabled;
    }

    public void setAntiClickJackingUri(String antiClickJackingUri) {
        String[] antiClickJackingUriArr = antiClickJackingUri.split("\\s+");
        try {
            //validate URI's syntax
            for (String uriStr : antiClickJackingUriArr) {
                new URI(uriStr);
            }
        } catch (URISyntaxException e) {
            logger.error(e);
            throw new IllegalArgumentException(e);
        }
        this.antiClickJackingUri_XFrameOption = antiClickJackingUriArr[0];
        this.antiClickJackingUriList_CSP = antiClickJackingUri;
    }

    public boolean isXssProtectionEnabled() {
        return xssProtectionEnabled;
    }

    public void setXssProtectionEnabled(boolean xssProtectionEnabled) {
        this.xssProtectionEnabled = xssProtectionEnabled;
    }

    private static enum XFrameOption {
        DENY("DENY"),
        SAME_ORIGIN("SAMEORIGIN"),
        ALLOW_FROM("ALLOW-FROM");


        private final String headerValue;

        private XFrameOption(String headerValue) {
            this.headerValue = headerValue;
        }

        public String getHeaderValue() {
            return headerValue;
        }
    }
}

