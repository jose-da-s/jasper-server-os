package com.jaspersoft.jasperserver.api.security.csrf;

import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import jakarta.servlet.http.HttpServletRequest;

/**
 * <p></p>
 *
 * @author Vlad Zavadskyi
 */
public class PreAuthCorsFilter extends CorsFilter {

    private static final String LOGIN_PATH_INFO = "/login";

    public PreAuthCorsFilter(CorsConfigurationSource configSource) {
        super(configSource);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !isLoginEndpoint(request);
    }

    private boolean isLoginEndpoint(HttpServletRequest request) {
        return LOGIN_PATH_INFO.equals(request.getPathInfo());
    }
}
