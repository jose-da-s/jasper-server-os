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
package com.jaspersoft.jasperserver.war.httpheaders;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Multimaps;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

import static com.google.common.collect.Iterables.isEmpty;

/**
 * The filter adds HTTP Response headers to the response if requested resource URL matches to any rule
 * This filter designed to be configured using spring and
 * registered in web.xml through {@see org.springframework.web.filter.DelegatingFilterProxy}
 * <p/>
 * Most specific rules should be first. Only headers from first matching rule will be applied to the response.
 *
 * @author Stas Chubar
 */
public class ResourceHTTPHeadersFilter implements Filter {

    private static final Log log = LogFactory.getLog(ResourceHTTPHeadersFilter.class);

    private ImmutableListMultimap<String, HeadersRule> configuredHeadersRulesByHttpMethod;

    private List<HeadersRule> headersRules;

    public void setHeadersRules(List<HeadersRule> headersRules) {
        this.headersRules = headersRules;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.configuredHeadersRulesByHttpMethod = Multimaps.index(headersRules, new Function<HeadersRule, String>() {
            @Override
            public String apply(HeadersRule headersRule) {
                return headersRule.getMethod();
            }
        });
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        ServletResponse newResponse = response;

        Optional<HeadersRule> headersRule = findFirstMatchingRule(request);
        if (headersRule.isPresent()) {
            doFilterAndApplyHeadersIfNotSet(request, response, chain, headersRule.get());
        } else {
            chain.doFilter(request, newResponse);
        }
    }

    private Optional<HeadersRule> findFirstMatchingRule(ServletRequest request) {
        if (this.configuredHeadersRulesByHttpMethod == null || !(request instanceof HttpServletRequest)) {
            return Optional.absent();
        }

        HttpServletRequest httpRequest = (HttpServletRequest) request;

        List<HeadersRule> headersRules =
                this.configuredHeadersRulesByHttpMethod.get(httpRequest.getMethod());

        if (!isEmpty(headersRules)) {
            for (HeadersRule rule : headersRules) {

                if (rule.matches(httpRequest)) {
                    return Optional.of(rule);
                }
            }
        }

        return Optional.absent();

    }

    private void doFilterAndApplyHeadersIfNotSet(ServletRequest request, ServletResponse response, FilterChain chain,
                                      HeadersRule rule) throws IOException, ServletException {
        HttpResponseHeadersAccumulator httpResponse =
                new HttpResponseHeadersAccumulator((HttpServletResponse) response, rule.getHeaders());

        chain.doFilter(request, httpResponse);

        if (!httpResponse.isHeadersApplied()) {
            httpResponse.applyHeadersOnce();
        }
    }

    @Override
    public void destroy() {
    }

    ImmutableListMultimap<String, HeadersRule> getConfiguredHeadersRulesByHttpMethod() {
        return configuredHeadersRulesByHttpMethod;
    }

}

