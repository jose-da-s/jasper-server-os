package com.jaspersoft.jasperserver.war.control;

import com.jaspersoft.jasperserver.api.common.properties.Log4jSettingsService;
import com.jaspersoft.jasperserver.api.common.properties.PropertiesManagementService;
import org.apache.logging.log4j.Level;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import static com.jaspersoft.jasperserver.war.control.LogSettingsController.LEVEL_PARAMETER;
import static com.jaspersoft.jasperserver.war.control.LogSettingsController.LOGGER_PARAMETER;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LogSettingsControllerTest {

    private final LogSettingsController logSettingsController = new LogSettingsController();

    private final PropertiesManagementService propertiesManagementService = mock(PropertiesManagementService.class);

    private final Log4jSettingsService log4jSettingsService = mock(Log4jSettingsService.class);

    @Before
    public void setUp() {
        logSettingsController.setPropertiesManagementService(propertiesManagementService);
        logSettingsController.setLog4jSettingsService(log4jSettingsService);
    }

    @Test
    public void handleRequest_invalidLevel_error() throws Exception {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse res = mock(HttpServletResponse.class);
        when(req.getParameter("level")).thenReturn("ABC");
        when(req.getMethod()).thenReturn(HttpMethod.POST.name());

        logSettingsController.handleRequest(req, res);

        verify(res).sendError(HttpStatus.BAD_REQUEST.value(), "Invalid level for logging: [ABC]");
    }

    @Test
    public void handleRequest_noLevel_success() throws Exception {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse res = mock(HttpServletResponse.class);

        logSettingsController.handleRequest(req, res);

        verify(res, never()).sendError(eq(HttpStatus.BAD_REQUEST.value()), anyString());
    }

    @Test
    public void handleRequest_getRequestWithLoggerAndLevelParams_ignoreUpdatingLevel() throws Exception {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse res = mock(HttpServletResponse.class);
        String levelName = Level.ERROR.name();
        when(req.getParameter(LOGGER_PARAMETER)).thenReturn("ABC");
        when(req.getParameter(LEVEL_PARAMETER)).thenReturn(levelName);
        when(req.getMethod()).thenReturn(HttpMethod.GET.name());

        logSettingsController.handleRequest(req, res);

        verify(propertiesManagementService, never()).setProperty("log4j.ABC", levelName);
    }

    @Test
    public void handleRequest_postRequestWithLoggerAndLevelParams_updatedLevel() throws Exception {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse res = mock(HttpServletResponse.class);
        String levelName = Level.ERROR.name();
        when(req.getParameter(LOGGER_PARAMETER)).thenReturn("ABC");
        when(req.getParameter(LEVEL_PARAMETER)).thenReturn(levelName);
        when(req.getMethod()).thenReturn(HttpMethod.POST.name());

        logSettingsController.handleRequest(req, res);

        verify(propertiesManagementService).setProperty("log4j.ABC", levelName);
    }

}
