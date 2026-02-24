/*
 * Copyright (C) 2025 the Jasper Server OS Authors
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

package com.jaspersoft.jasperserver.jaxrs.poc.hypermedia.workflow.visitor;

import com.jaspersoft.jasperserver.dto.resources.ClientResourceLookup;
import com.jaspersoft.jasperserver.dto.resources.ResourceMediaType;
import com.jaspersoft.jasperserver.jaxrs.poc.hypermedia.admin.ReadAdminActivity;
import com.jaspersoft.jasperserver.jaxrs.poc.hypermedia.common.Relation;
import com.jaspersoft.jasperserver.jaxrs.poc.hypermedia.common.activity.GenericRequest;
import com.jaspersoft.jasperserver.jaxrs.poc.hypermedia.common.provider.ApplicationInfoProvider;
import com.jaspersoft.jasperserver.jaxrs.poc.hypermedia.common.provider.AuthenticationInfoProvider;
import com.jaspersoft.jasperserver.jaxrs.poc.hypermedia.common.provider.RequestInfoProvider;
import com.jaspersoft.jasperserver.jaxrs.poc.hypermedia.common.representation.HypermediaRepresentation;
import com.jaspersoft.jasperserver.jaxrs.poc.hypermedia.common.visitor.RelationsVisitor;
import com.jaspersoft.jasperserver.jaxrs.poc.hypermedia.content.activity.ReadContentReferenceActivity;
import com.jaspersoft.jasperserver.jaxrs.poc.hypermedia.content.dto.ContentReference;
import com.jaspersoft.jasperserver.jaxrs.poc.hypermedia.resource.activity.BrowseResourcesActivity;
import com.jaspersoft.jasperserver.jaxrs.poc.hypermedia.resource.activity.CreateResourceActivity;
import com.jaspersoft.jasperserver.jaxrs.poc.hypermedia.resource.activity.SearchResourcesActivity;
import com.jaspersoft.jasperserver.jaxrs.poc.hypermedia.workflow.activity.ReadUserWorkflowCollectionActivity;
import com.jaspersoft.jasperserver.jaxrs.poc.hypermedia.workflow.data.UserWorkflowTypes;
import com.jaspersoft.jasperserver.jaxrs.poc.hypermedia.workflow.dto.UserWorkflow;
import com.jaspersoft.jasperserver.jaxrs.poc.hypermedia.workflow.representation.UserWorkflowRepresentation;
import com.jaspersoft.jasperserver.remote.services.BatchRepositoryService;
import com.jaspersoft.jasperserver.search.service.RepositorySearchCriteria;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author: Igor.Nesterenko
 * @version: ${Id}
 */
public class UserWorkflowRelationVisitor extends RelationsVisitor<UserWorkflowRepresentation> {


    @Resource
    private ApplicationInfoProvider appInfoProvider;

    @Resource
    private AuthenticationInfoProvider authInfoProvider;

    @Resource
    private RequestInfoProvider requestInfoProvider;

    @Resource
    protected BatchRepositoryService batchRepositoryService;

    public static String convertWorkflowNameToResourceType(String workflowName){
        String resourceType = null;

        if (UserWorkflowTypes.DATA_SOURCE.equals(workflowName)){
            resourceType = ResourceMediaType.ANY_DATASOURCE_TYPE;
        }else if (UserWorkflowTypes.ADHOC_VIEW.equals(workflowName)){
            resourceType = ResourceMediaType.ADHOC_DATA_VIEW_CLIENT_TYPE;
        }else if (UserWorkflowTypes.DOMAIN.equals(workflowName)){
            resourceType = ResourceMediaType.SEMANTIC_LAYER_DATA_SOURCE_CLIENT_TYPE;
        }else if (UserWorkflowTypes.DASHBOARD.equals(workflowName)){
            resourceType = ResourceMediaType.DASHBOARD_CLIENT_TYPE;
        }else if (UserWorkflowTypes.REPORT.equals(workflowName)){
            resourceType = ResourceMediaType.REPORT_UNIT_CLIENT_TYPE;
        }

        return  resourceType;
    }

    private Boolean isAtLeastOneResourceByTypes(List<String> types) {
        return batchRepositoryService.getResourcesCountByTypes(types) > 0;
    }

    public void resolve(ReadUserWorkflowCollectionActivity workflowsActivity, Relation relation, Boolean isLink){

        String workflowName = representation.getName();

        if (UserWorkflowTypes.ADMIN.equals(workflowName)
                && !authInfoProvider.checkAuthenticationRoles("ROLE_ADMINISTRATOR")){
            return;
        }

        workflowsActivity.setGenericRequest(new GenericRequest()
                .setOptional(true)
                .setExpanded(true)
                .addParam("parentName", workflowName)
        );

        if (!isLink){
            HypermediaRepresentation childRepresentation = (HypermediaRepresentation) workflowsActivity.proceed();
            representation.addEmbedded(relation, childRepresentation);
        }else{
            representation.addLink(workflowsActivity.buildLink());
        }

    }

    public void resolve(BrowseResourcesActivity browseRepositoryActivity, Relation relation, Boolean isLink){

        String workflowName = representation.getName();

        if (workflowName.equals(UserWorkflowTypes.REPOSITORY)){
            if (isLink) {
                representation.addLink(browseRepositoryActivity.buildLink());
            }
        }

    }

    public void resolve(CreateResourceActivity child, Relation relation, Boolean isLink) {

        String workflowName = representation.getName();

        ClientResourceLookup resource = new ClientResourceLookup();

        String resourceType = convertWorkflowNameToResourceType(workflowName);

        List<String> dataSourcesTypes = Arrays.asList(
                ResourceMediaType.SEMANTIC_LAYER_DATA_SOURCE_CLIENT_TYPE,
                ResourceMediaType.ANY_DATASOURCE_TYPE
        );

        Boolean isAdmin = authInfoProvider.checkAuthenticationRoles("ROLE_ADMINISTRATOR");
        Boolean isAvailableProFeature = appInfoProvider.isAvailableProFeature("AHD");
        Boolean isSupportedDevice = requestInfoProvider.isSupportedDevice();

        if (dataSourcesTypes.contains(resourceType)) {
            resourceType = (isAdmin && isAvailableProFeature && isSupportedDevice && isAtLeastOneResourceByTypes(dataSourcesTypes)) ? resourceType : null;
        } else if (ResourceMediaType.ADHOC_DATA_VIEW_CLIENT_TYPE.equals(resourceType)){

            List<String> adhocSourceTypes = Arrays.asList(
                    ResourceMediaType.SEMANTIC_LAYER_DATA_SOURCE_CLIENT_TYPE
            );

            resourceType = isAvailableProFeature && isAtLeastOneResourceByTypes(adhocSourceTypes) ? resourceType : null;

        } else if (ResourceMediaType.DASHBOARD_CLIENT_TYPE.equals(resourceType)){
            resourceType = isSupportedDevice && appInfoProvider.isAvailableProFeature("DB") ? resourceType : null;
        } else if (ResourceMediaType.REPORT_UNIT_CLIENT_TYPE.equals(resourceType)){
            resourceType = isAtLeastOneResourceByTypes(Arrays.asList(ResourceMediaType.REPORT_UNIT_CLIENT_TYPE)) ? resourceType : null;
        }

        if (resourceType != null){
            resource.setResourceType(resourceType);
            if (isLink) {
                child.setData(resource);
                representation.addLink(child.buildLink());
            }
        }

    }

    public void resolve(SearchResourcesActivity child, Relation relation, Boolean isLink) {


        String workflowName = representation.getName();
        String resourceType = convertWorkflowNameToResourceType(workflowName);

        RepositorySearchCriteria searchCriteria = child.getCriteria();

        List<String> resourceTypes = new ArrayList<String>();

        if (resourceType != null){
            if (resourceType.equals(ResourceMediaType.ANY_DATASOURCE_TYPE)){
                resourceTypes.addAll(ResourceMediaType.DATASOURCE_TYPES);
            } else{
                resourceTypes.add(resourceType);
            }

            if(isAtLeastOneResourceByTypes(resourceTypes)){

                searchCriteria.setResourceTypes(resourceTypes);
                searchCriteria.setStartIndex(0);
                searchCriteria.setExcludeFolders(true);

                if (isLink){
                    child.setCriteria(searchCriteria);
                    representation.addLink(child.buildLink());
                }

            }

        }

    }

    public void resolve(ReadAdminActivity adminActivity, Relation relation, Boolean isLink) {

        String section = null;

        Boolean isAdmin = authInfoProvider.checkAuthenticationRoles("ROLE_ADMINISTRATOR");

        if (isAdmin){

            String workflowName = representation.getName();
            if (UserWorkflowTypes.USERS.equals(workflowName)){
                section = "users";
            }else if (UserWorkflowTypes.ROLES.equals(workflowName)){
                section = "roles";
            }else if (UserWorkflowTypes.SERVER_SETTINGS.equals(workflowName)){

                Boolean isAvailableForCE = appInfoProvider.isCEVersion()
                        && authInfoProvider.checkAuthenticationRoles("ROLE_ADMINISTRATOR");
                Boolean isAvailableForPRO = appInfoProvider.isProVersion()
                        && authInfoProvider.checkAuthenticationRoles("ROLE_SUPERUSER");

                section = (isAvailableForCE || isAvailableForPRO) ? "logging" : null;
            }

            if (isLink && section != null) {
                adminActivity.setSection(section);
                representation.addLink(adminActivity.buildLink());
            }
        }
    }

    public void resolve(ReadContentReferenceActivity referenceActivity, Relation relation, Boolean isLink) {

        UserWorkflow workflow = representation.getBody();

        String contentReferenceId = workflow.getContentReferenceId();

        if (!isLink){

            ContentReference data = referenceActivity.findData(new GenericRequest().addParam("id", contentReferenceId));

            if (data != null){
                referenceActivity.setData(data);
                representation.addEmbedded(relation, (HypermediaRepresentation)referenceActivity.proceed());
            }
        }

    }

}

