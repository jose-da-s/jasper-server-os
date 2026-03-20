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

import com.jaspersoft.jasperserver.api.SessionAttribMissingException;
import com.jaspersoft.jasperserver.api.logging.audit.context.AuditContext;
import com.jaspersoft.jasperserver.api.logging.audit.domain.AuditEvent;
import com.jaspersoft.jasperserver.api.metadata.user.domain.Role;
import com.jaspersoft.jasperserver.api.metadata.user.domain.TenantQualified;
import com.jaspersoft.jasperserver.api.metadata.user.domain.impl.client.MetadataUserDetails;
import com.jaspersoft.jasperserver.api.metadata.user.service.TenantService;
import com.jaspersoft.jasperserver.core.util.CipherUtil;
import com.jaspersoft.jasperserver.war.helper.JSONHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.MessageSource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.webflow.action.FormAction;
import org.springframework.webflow.core.collection.ParameterMap;
import org.springframework.webflow.execution.RequestContext;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.*;

/**
 * Base form action class.
 *
 * @author Yuriy Plakosh
 */
public abstract class BaseFormAction extends FormAction {
    // Request attributes.
    protected static final String AJAX_RESPONSE_MODEL = "ajaxResponseModel";

    // Attributes.
    private final String ATTRIBUTE_STATE = getClass().getName() + ":state";

    // Log.
    protected final Log log = LogFactory.getLog(this.getClass());

    protected TenantService tenantService;
    protected MessageSource messages;
    protected JSONHelper jsonHelper;
    protected AuditContext auditContext;

    public void setTenantService(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    public void setMessages(MessageSource messages) {
        this.messages = messages;
    }

    public void setJsonHelper(JSONHelper jsonHelper) {
        this.jsonHelper = jsonHelper;
    }

    public void setAuditContext(AuditContext auditContext) {
        this.auditContext = auditContext;
    }

    protected void putState(RequestContext context, Object state) {
        ((HttpServletRequest)context.getExternalContext().getNativeRequest()).getSession().setAttribute(ATTRIBUTE_STATE, state);
    }

    protected Object getState(RequestContext context) {
        HttpSession session = ((HttpServletRequest)context.getExternalContext().getNativeRequest()).getSession();
        Object state = session.getAttribute(ATTRIBUTE_STATE);
        if (state == null) {
            initState(context);
        }
        return session.getAttribute(ATTRIBUTE_STATE);
    }

    protected abstract void initState(RequestContext context);

    protected String getParameter(RequestContext context, String name) {
        ParameterMap map = context.getRequestParameters();

        if (map.contains(name)) {
            return map.get(name);
        } else {
            return null;
        }
    }

    public String getDecodedRequestParameter(RequestContext context, String name) {
        return CipherUtil.uriDecode(getParameter(context, name));
    }

    protected List getEntitiesAndUpdateState(EntitiesListState state, int maxResults, EntitiesListManager manager) {
        if (state.getResultIndex() == 0) {
            state.updateResultState(0, manager.getResultsCount());
        }

        List entities = new ArrayList(maxResults);
        do {
            List accessibleResults = manager.getResults(state.getResultIndex(), maxResults);

            for (Object entity : accessibleResults) {
                entities.add(entity);
            }

            state.updateResultState(state.getResultIndex() + maxResults, state.getResultsCount());
        } while (entities.size() < maxResults && state.getResultIndex() < state.getResultsCount());

        return entities;
    }

    protected Set<String> getSubTenantIdsSet(String tenantId) {
        if (tenantId == null) {
            return null;
        }

        Set<String> tenantIdSet = new HashSet<String>();

        if (!tenantId.equals(TenantService.ORGANIZATIONS)) {
            tenantIdSet.add(tenantId);

            List<String> allSubTenantIds = tenantService.getAllSubTenantIdList(null, tenantId);

            if (allSubTenantIds != null) {
                tenantIdSet.addAll(allSubTenantIds);
            }
        }

        return  tenantIdSet;
    }

    protected String getCurrentTenantId() {
        Authentication authenticationToken = SecurityContextHolder.getContext().getAuthentication();

        TenantQualified tenantQualified = (TenantQualified) authenticationToken.getPrincipal();
        if (authenticationToken.getPrincipal() instanceof TenantQualified) {
            return tenantQualified.getTenantId();
        }

        return null;
    }

    @SuppressWarnings({"unchecked"})
    protected List<Role> getCurrentUserRoles() {
        List<Role> roleList = new ArrayList<Role>();

        Authentication authenticationToken = SecurityContextHolder.getContext().getAuthentication();

        if (authenticationToken.getPrincipal() instanceof MetadataUserDetails) {
            MetadataUserDetails userDetails = (MetadataUserDetails) authenticationToken.getPrincipal();

            Set<Role> roles = userDetails.getRoles();
            if (roles != null) {
                roleList.addAll(roles);
            }
        }

        return roleList;
    }

    protected String getCurrentUsername() {
        Authentication authenticationToken = SecurityContextHolder.getContext().getAuthentication();

        return authenticationToken.getName();
    }

    protected void createAuditEvent(final String auditEventName) {
        auditContext.doInAuditContext(new AuditContext.AuditContextCallback() {
            public void execute() {
                auditContext.createAuditEvent(auditEventName);
            }
        });
    }

    protected void closeAuditEvent(String auditEventName) {
        auditContext.doInAuditContext(auditEventName, new AuditContext.AuditContextCallbackWithEvent() {
            public void execute(AuditEvent auditEvent) {
                auditContext.closeAuditEvent(auditEvent);
            }
        });
    }
}

