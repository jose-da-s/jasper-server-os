package com.jaspersoft.jasperserver.api.metadata.user.service.impl;

import com.jaspersoft.jasperserver.api.metadata.user.service.impl.CreateExecutionApplicationEvent.ExecutionType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

import static com.jaspersoft.jasperserver.api.metadata.user.service.impl.CreateExecutionApplicationEvent.ExecutionType.DASHBOARD;
import static com.jaspersoft.jasperserver.api.metadata.user.service.impl.CreateExecutionApplicationEvent.ExecutionType.REPORT;

@Service
public class ExecutionsPublishingEventServiceImpl implements ExecutionsPublishingEventService {
    private static final Logger log = LogManager.getLogger(ExecutionsPublishingEventServiceImpl.class);

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void publishReportExecutions(String id) {
        if (hasSession()) {
            applicationEventPublisher.publishEvent(eventForType(REPORT, id));
        }
    }

    @Override
    public void publishDashboardExecutions(String id) {
        if (hasSession()) {
            applicationEventPublisher.publishEvent(eventForType(DASHBOARD, id));
        }
    }

    private ApplicationEvent eventForType(ExecutionType type, String id) {
        log.debug("{} execution with ID {} has been created", type, id);
        return new CreateExecutionApplicationEvent(this, id, type);
    }

    private boolean hasSession() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes) {
            HttpServletRequest req = ((ServletRequestAttributes) requestAttributes).getRequest();
            return req.getSession(false) != null;
        }
        return false;
    }
}
