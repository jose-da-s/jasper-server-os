package com.jaspersoft.jasperserver.war.control;

import org.junit.Test;
import org.junit.jupiter.api.AfterEach;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.switchuser.SwitchUserGrantedAuthority;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class JSCommonControllerTest {

    private final JSCommonController jsCommonController = new JSCommonController();

    @Test
    public void heartbeatInfo() throws Exception {
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getParameter("scrWidth")).thenReturn("ABC");

        HttpServletResponse res = mock(HttpServletResponse.class);
        jsCommonController.heartbeatInfo(req, res);

        verify(res).sendError(HttpStatus.BAD_REQUEST.value(), "Invalid value for scrWidth: [ABC]");
    }

    @Test
    public void test_buildRedirectURL_showPassTrue_weakFalse() {
        final String URL = "/bear";
        JSCommonController jcc = new JSCommonController();

        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getParameter("showPasswordChange")).thenReturn("true");
        when(req.getParameter("weakPassword")).thenReturn(null);

        assertEquals(URL + "?showPasswordChange=true", jcc.buildRedirectUrl(req, URL));
    }

    @Test
    public void exitUser_userSwitched_exitUserRedirectURL() {
        // Arrange
        Authentication authentication = mock(Authentication.class);
        SwitchUserGrantedAuthority switchUserGrantedAuthority = new SwitchUserGrantedAuthority("ROLE_USER", authentication);
        doReturn(singletonList(switchUserGrantedAuthority)).when(authentication).getAuthorities();
        SecurityContextHolder.getContext().setAuthentication(authentication);

        HttpServletResponse res = mock(HttpServletResponse.class);
        HttpServletRequest req = mock(HttpServletRequest.class);

        // Act
        ModelAndView result = jsCommonController.exitUser(req, res);

        // Assert
        assertEquals("redirect:/j_acegi_exit_user", result.getViewName());
        verify(req).setAttribute(eq(View.RESPONSE_STATUS_ATTRIBUTE), eq(HttpStatus.TEMPORARY_REDIRECT));
    }

    @Test
    public void buildRedirectUrl_weakPass_noReset() {
        final String URL = "el_farolito/";

        JSCommonController jcc = new JSCommonController();

        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getParameter("showPasswordChange")).thenReturn(null);
        when(req.getParameter("weakPassword")).thenReturn("true");

        String redirectUrl = jsCommonController.buildRedirectUrl(req, URL);

        assertEquals(URL + "?weakPassword=true", redirectUrl);
    }

    @Test
    public void buildRedirectURL_nullIsNoop() {
        final String URL = "testing";

        JSCommonController jcc = new JSCommonController();
        HttpServletRequest req = mock(HttpServletRequest.class);

        when(req.getParameter("showPasswordChange")).thenReturn(null);
        when(req.getParameter("weakPassword")).thenReturn(null);

        String redirectUrl = jsCommonController.buildRedirectUrl(req, URL);
        assertEquals(URL, redirectUrl);
    }

    @AfterEach
    public static void cleanUp() {
        SecurityContextHolder.getContext().setAuthentication(null);
    }
}

