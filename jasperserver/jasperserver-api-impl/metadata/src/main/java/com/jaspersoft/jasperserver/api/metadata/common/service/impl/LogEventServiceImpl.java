package com.jaspersoft.jasperserver.api.metadata.common.service.impl;

import com.jaspersoft.jasperserver.api.common.domain.LogEvent;
import com.jaspersoft.jasperserver.api.engine.common.service.LoggingService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Date;

public class LogEventServiceImpl implements LogEventService {

    private static final String LOG_EVENT_MESSAGE_CODE_NOFA_EXCEEDED = "log.error.user.account.disabled.exceeded.nofa";
    private static final String LOG_EVENT_COMPONENT = "internalDaoAuthenticationProvider";
    private static final String LOG_EVENT_NOFA_EXCEEDED_TEXT = "Number of failed attempts exceeded. Disabling user: ";
    private static final String LOG_EVENT_CLEAN_UP_COMPONENT = "externalUserLoginAttemptsCleanupService";
    private static final String LOG_EVENT_MESSAGE_CODE_RECORD_CLEANUP = "log.event.external.user.record.cleanup";
    private static final String LOG_EVENT_EXTERNAL_USER_RECORD_CLEANUP_TEXT = "Records deleted for external user login events.";

    private LoggingService loggingService;

    @Override
    public void createUserAccountLockedEvent(UserDetails userDetails) {
        LogEvent logEventInstance = getLoggingService().instantiateLogEvent();
        logEventInstance.setType(LogEvent.TYPE_ERROR);
        logEventInstance.setComponent(LogEventServiceImpl.LOG_EVENT_COMPONENT);
        logEventInstance.setMessageCode(LogEventServiceImpl.LOG_EVENT_MESSAGE_CODE_NOFA_EXCEEDED);
        logEventInstance.setText(createUserLockEventText(userDetails));
        getLoggingService().log(logEventInstance);
    }

    private String createUserLockEventText(UserDetails userDetails) {
        return LogEventServiceImpl.LOG_EVENT_NOFA_EXCEEDED_TEXT +
                userDetails.getUsername() +
                " | " +
                getRemoteIpAddress();
    }

    private String getRemoteIpAddress() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        HttpServletRequest httpServletRequest = ((ServletRequestAttributes)requestAttributes).getRequest();
        return httpServletRequest.getRemoteAddr();
    }

    public LoggingService getLoggingService() {
        return loggingService;
    }

    public void setLoggingService(LoggingService loggingService) {
        this.loggingService = loggingService;
    }

    @Override
    public void createRecordCleanUpEventForExternalUsers() {
        LogEvent logEvent = getLoggingService().instantiateLogEvent();
        logEvent.setType(LogEvent.TYPE_ERROR);
        logEvent.setComponent(LogEventServiceImpl.LOG_EVENT_CLEAN_UP_COMPONENT);
        logEvent.setMessageCode(LogEventServiceImpl.LOG_EVENT_MESSAGE_CODE_RECORD_CLEANUP);
        logEvent.setText(LOG_EVENT_EXTERNAL_USER_RECORD_CLEANUP_TEXT+" | "+new Date());
        getLoggingService().log(logEvent);
    }
}