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

package com.jaspersoft.jasperserver.jaxrs.poc.hypermedia.workflow.activity;

import com.jaspersoft.jasperserver.jaxrs.poc.hypermedia.common.MediaTypes;
import com.jaspersoft.jasperserver.jaxrs.poc.hypermedia.common.Relation;
import com.jaspersoft.jasperserver.jaxrs.poc.hypermedia.common.activity.AbstractActivity;
import com.jaspersoft.jasperserver.jaxrs.poc.hypermedia.common.activity.Activity;
import com.jaspersoft.jasperserver.jaxrs.poc.hypermedia.common.activity.GenericRequest;
import com.jaspersoft.jasperserver.jaxrs.poc.hypermedia.common.representation.embedded.EmbeddedElement;
import com.jaspersoft.jasperserver.jaxrs.poc.hypermedia.common.representation.embedded.PluralEmbeddedElement;
import com.jaspersoft.jasperserver.jaxrs.poc.hypermedia.common.representation.Link;
import com.jaspersoft.jasperserver.jaxrs.poc.hypermedia.common.provider.RequestInfoProvider;
import com.jaspersoft.jasperserver.jaxrs.poc.hypermedia.workflow.data.UserWorkflowStorage;
import com.jaspersoft.jasperserver.jaxrs.poc.hypermedia.workflow.data.UserWorkflowTypes;
import com.jaspersoft.jasperserver.jaxrs.poc.hypermedia.workflow.dto.UserWorkflow;
import com.jaspersoft.jasperserver.jaxrs.poc.hypermedia.workflow.representation.UserWorkflowCollectionRepresentation;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author: Igor.Nesterenko
 * @version: ${Id}
 */

@Component
public class ReadUserWorkflowCollectionActivity extends AbstractActivity<UserWorkflowCollectionRepresentation, List<UserWorkflow>> {

    @Resource
    private UserWorkflowStorage userWorkflowStorage;

    @Resource
    private RequestInfoProvider requestInfoProvider;

    @Resource
    private ApplicationContext applicationContext;

    public ReadUserWorkflowCollectionActivity() {
        super();
    }

    public ReadUserWorkflowCollectionActivity(Map<Relation, Activity> relations, List<Relation> linkRelations) {
        super(relations, linkRelations);
    }

    @Override
    public EmbeddedElement proceed() {
        //workaround to resolve circular dependency with workflow collection activity
        relations.put(Relation.workflow, (Activity)applicationContext.getBean("workflowActivity"));
        return super.proceed();
    }

    @Override
    public Relation getOwnRelation() {
        return Relation.workflows;
    }

    @Override
    public List<UserWorkflow> findData(GenericRequest request){
        String parentName = (String) request.getParams().get("parentName");
        List<UserWorkflow> userWorkflows = new ArrayList<UserWorkflow>();
        if (parentName == null || parentName.equals("null")){
            userWorkflows.addAll(userWorkflowStorage.findAll());
        } else {
            userWorkflows = userWorkflowStorage.findAllByParentName(parentName);
        }
        return  userWorkflows;
    }

    @Override
    public UserWorkflowCollectionRepresentation buildRepresentation() {
        return new UserWorkflowCollectionRepresentation(data);
    }


    @Override
    public EmbeddedElement buildLink() {

        String restUrl = null;
        PluralEmbeddedElement embeddedElements = null;
        EmbeddedElement result = null;

        if (genericRequest != null){
            String parentName = (String)genericRequest.getParams().get("parentName");
            if (isAtLeastOneChild(parentName)){
                restUrl = MessageFormat.format("{0}rest_v2/hypermedia/workflows?parentName={1}", requestInfoProvider.getBaseUrl(),parentName);
                if (UserWorkflowTypes.ADMIN.equals(parentName)){
                    String webFlowUrl = requestInfoProvider.getBaseUrl() + "flow.html?_flowId=adminHomeFlow";
                    embeddedElements = new PluralEmbeddedElement(getOwnRelation());
                    embeddedElements.add(new Link()
                            .setHref(webFlowUrl)
                            .setTitle(getMessage("view.options"))
                            .setType(MediaTypes.TEXT_HTML)
                            .setRelation(getOwnRelation())
                            .setProfile("GET")
                    );
                }
            }
        }

        if (restUrl != null){
            Link restLink = new Link()
                    .setHref(restUrl)
                    .setTitle(getMessage("view.options"))
                    .setType(MediaTypes.APPLICATION_HAL_JSON)
                    .setRelation(getOwnRelation());
            if (embeddedElements == null){
                result = restLink;
            }else{
                embeddedElements.add(restLink);
                result = embeddedElements;
            }
        }

        return  result;
    }

    private Boolean isAtLeastOneChild(String parentName) {
        final List<UserWorkflow> children = userWorkflowStorage.findAllByParentName(parentName);
        return children != null && !children.isEmpty();
    }

}

