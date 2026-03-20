package com.jaspersoft.jasperserver.api.security.csrf;

import org.junit.Test;
import org.springframework.web.cors.CorsConfigurationSource;

import jakarta.servlet.http.HttpServletRequest;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

/**
 * <p></p>
 *
 * @author Vlad Zavadskyi
 */
public class PreAuthCorsFilterTest {
    private final PreAuthCorsFilter filter = new PreAuthCorsFilter(mock(CorsConfigurationSource.class));

    private final HttpServletRequest request = mock(HttpServletRequest.class);

    @Test
    public void shouldNotFilter_loginEndpoint_false() {
        doReturn("/login").when(request).getPathInfo();
        assertFalse(filter.shouldNotFilter(request));
    }

    @Test
    public void shouldNotFilter_nonLoginEndpoint_true() {
        doReturn("/resources").when(request).getPathInfo();
        assertTrue(filter.shouldNotFilter(request));
    }

    @Test
    public void shouldNotFilter_nullEndpoint_true() {
        doReturn(null).when(request).getPathInfo();
        assertTrue(filter.shouldNotFilter(request));
    }
}