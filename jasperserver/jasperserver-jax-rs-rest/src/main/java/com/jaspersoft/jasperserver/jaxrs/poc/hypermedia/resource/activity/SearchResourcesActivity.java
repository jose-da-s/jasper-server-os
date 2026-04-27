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

package com.jaspersoft.jasperserver.jaxrs.poc.hypermedia.resource.activity;

import com.jaspersoft.jasperserver.dto.resources.ClientResourceLookup;
import com.jaspersoft.jasperserver.dto.resources.ResourceMediaType;
import com.jaspersoft.jasperserver.jaxrs.poc.hypermedia.common.MediaTypes;
import com.jaspersoft.jasperserver.jaxrs.poc.hypermedia.common.Relation;
import com.jaspersoft.jasperserver.jaxrs.poc.hypermedia.common.activity.Activity;
import com.jaspersoft.jasperserver.jaxrs.poc.hypermedia.common.activity.GenericRequest;
import com.jaspersoft.jasperserver.jaxrs.poc.hypermedia.common.activity.AbstractActivity;
import com.jaspersoft.jasperserver.jaxrs.poc.hypermedia.common.representation.embedded.EmbeddedElement;
import com.jaspersoft.jasperserver.jaxrs.poc.hypermedia.common.representation.Link;
import com.jaspersoft.jasperserver.jaxrs.poc.hypermedia.common.representation.embedded.PluralEmbeddedElement;
import com.jaspersoft.jasperserver.jaxrs.poc.hypermedia.common.provider.RequestInfoProvider;
import com.jaspersoft.jasperserver.jaxrs.poc.hypermedia.resource.representation.ResourceLookupCollectionRepresentation;
import com.jaspersoft.jasperserver.remote.exception.IllegalParameterValueException;
import com.jaspersoft.jasperserver.remote.exception.ResourceNotFoundException;
import com.jaspersoft.jasperserver.remote.services.BatchRepositoryService;
import com.jaspersoft.jasperserver.search.service.impl.RepositorySearchAccumulator;
import com.jaspersoft.jasperserver.search.mode.SearchMode;
import com.jaspersoft.jasperserver.search.service.RepositorySearchCriteria;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jakarta.annotation.Resource;
import jakarta.ws.rs.core.MediaType;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * @author: Igor.Nesterenko
 * @version: ${Id}
 */
public class SearchResourcesActivity extends AbstractActivity<ResourceLookupCollectionRepresentation, List<ClientResourceLookup>> {

    private static final Log log = LogFactory.getLog(SearchResourcesActivity.class);

    @Resource
    protected BatchRepositoryService batchRepositoryService;

    protected RepositorySearchCriteria criteria;

    //Took from RepositorySearchAction
    private static final String PARAMETER_FILTER_ID = "filterId";
    private static final String PARAMETER_FILTER_OPTION = "filterOption";


    @Resource
    protected RequestInfoProvider requestInfoProvider;

    public SearchResourcesActivity() {
        super();
    }

    public SearchResourcesActivity(Map<Relation, Activity> relations, List<Relation> linkRelations) {
        super(relations, linkRelations);
    }

    @Override
    public List<ClientResourceLookup> findData(GenericRequest request) {

        com.jaspersoft.jasperserver.search.service.RepositorySearchResult<ClientResourceLookup> result = RepositorySearchAccumulator.EMPTY_RESULT;

        if (criteria != null){
            try {
                result = batchRepositoryService.getResources(criteria);
            } catch (IllegalParameterValueException e) {
                log.debug(e);
            } catch (ResourceNotFoundException e) {
                log.debug(e);
            }
        }

        return result.getItems();
    }

    @Override
    public ResourceLookupCollectionRepresentation buildRepresentation() {
        return new ResourceLookupCollectionRepresentation(data);
    }

    @Override
    public Relation getOwnRelation() {
        return  criteria.getSearchMode().equals(SearchMode.SEARCH) ? Relation.resources : Relation.folder;
    }

    public RepositorySearchCriteria getCriteria(){
        return criteria;
    }

    public SearchResourcesActivity setCriteria(RepositorySearchCriteria criteria){
        this.criteria = criteria;
        return this;
    }

    @Override
    public EmbeddedElement buildLink() {

        Link link = null;

        String restUrl = null;
        String webFlowUrl = null;
        PluralEmbeddedElement embeddedElements = null;

        if (criteria != null){

            Boolean isSearch = getOwnRelation() == Relation.resources;

            List<String> resourceTypes = criteria.getResourceTypes();

            webFlowUrl = buildWebflowUrl(isSearch, resourceTypes);
            restUrl = buildRestUrl();

            if (webFlowUrl != null){

                embeddedElements =  new PluralEmbeddedElement(getOwnRelation());

                String message = isSearch ? getMessage(getMessageType(resourceTypes)) : getMessage("view.repository");

                embeddedElements.add(new Link()
                        .setHref(webFlowUrl)
                        .setTitle(message)
                        .setType(MediaType.TEXT_HTML)
                        .setProfile("GET")
                        .setRelation(getOwnRelation())
                );

                embeddedElements.add(new Link()
                        .setHref(restUrl)
                        .setTitle(message)
                        .setType(MediaTypes.APPLICATION_HAL_JSON)
                        .setProfile("GET")
                        .setRelation(getOwnRelation())
                );
            }

        }else{
            throw new IllegalStateException("Search criteria isn't initialized, wrong state of activity");
        }

        return embeddedElements;
    }

    protected String buildRestUrl() {

        String url = requestInfoProvider.getBaseUrl() + "rest_v2/hypermedia/resources";

        List<String> params = toListOfParameters(criteria);

        for(int index = 0; index < params.size(); index++){
            String param = params.get(index);
            if (index == 0){
                url += "?" + param;
            }else{
                url += "&" + param;
            }
        }

        return url;
    }

    private List<String> toListOfParameters(RepositorySearchCriteria criteria) {
        List<String> result = new ArrayList<String>();

        if (criteria.getMaxCount() > 0){
            result.add("limit="+criteria.getMaxCount());
        }

        if (criteria.getSortBy() != null){
            result.add("sortBy="+criteria.getSortBy());
        }

        if (criteria.isFavorites() == true){
            result.add("favorites="+true);
        }

        if (criteria.getAccessType() != null){
            result.add("accessType="+criteria.getAccessType().name());
        }

        List<String> resourceTypes = getCriteria().getResourceTypes();
        if (resourceTypes != null && resourceTypes.size() > 0)  {
            for (String resourceType : resourceTypes) {
                result.add("type="+ resourceType);
            }
        }

        List<String> urisToExclude = getCriteria().getExcludeRelativePaths();
        if (urisToExclude != null && urisToExclude.size() > 0)  {
            for (String uri : urisToExclude) {
                result.add("relativeUriToExclude="+ uri);
            }
        }

        return result;
    }

    private String buildWebflowUrl(Boolean search, List<String> resourceTypes) {
        String webFlowUrl;
        String resourceType = resourceTypes != null && resourceTypes.size() > 0 ? resourceTypes.get(0) : null;

        if (search){
            webFlowUrl = buildSearchUrl(resourceType);
        }else{
            webFlowUrl = buildRepositoryUrl();
        }
        return webFlowUrl;
    }


    private String buildResourceTypeFilterParams(String resourceType){

        String result = "";

        if (ResourceMediaType.ADHOC_DATA_VIEW_CLIENT_TYPE.equals(resourceType)){
            resourceType = "resourceTypeFilter-adhocView";
        }else if(ResourceMediaType.REPORT_UNIT_CLIENT_TYPE.equals(resourceType)){
            resourceType = "resourceTypeFilter-reports";
        }else if(ResourceMediaType.SEMANTIC_LAYER_DATA_SOURCE_CLIENT_TYPE.equals(resourceType)){
            resourceType = "resourceTypeFilter-domains";
        }else if(ResourceMediaType.DASHBOARD_CLIENT_TYPE.equals(resourceType)){
            resourceType = "resourceTypeFilter-dashboards";
        }else if(ResourceMediaType.DATASOURCE_TYPES.contains(resourceType)){
            resourceType = "resourceTypeFilter-dataSources";
        }

        if (resourceType != null){
            result = PARAMETER_FILTER_ID+"="+"resourceTypeFilter&"
                    +PARAMETER_FILTER_OPTION+"="+resourceType;
        }
        return result;
    }

    protected String getMessageType(List<String> resourceTypes) {
        // check whether it is all data source resources
        if (resourceTypes.size() > 1 && resourceTypes.stream().allMatch( s -> ResourceMediaType.DATASOURCE_TYPES.contains(s))) {
            return "view.list.datasource";
        }
        // check whether it is mix resources.  If so, use generic message
        if (resourceTypes.stream().distinct().count() != 1) {
            return "view.list";
        }
        Map<String, String> map = Stream.of(new String[][] {
                {ResourceMediaType.ADHOC_DATA_VIEW_CLIENT_TYPE, "view.list.adhocview"},
                {ResourceMediaType.REPORT_UNIT_CLIENT_TYPE, "view.list.report"},
                {ResourceMediaType.SEMANTIC_LAYER_DATA_SOURCE_CLIENT_TYPE, "view.list.domain"},
                {ResourceMediaType.DASHBOARD_CLIENT_TYPE, "view.list.dashboard"},
        }).collect(Collectors.toMap(m -> m[0], m -> m[1]));
        // try to use specific message if possible
        return map.getOrDefault(resourceTypes.get(0), "view.list");
    }


    protected String buildRepositoryUrl(){
        return MessageFormat.format(
                "{0}flow.html?_flowId=searchFlow",
                requestInfoProvider.getBaseUrl()
        );

    }

    protected String buildSearchUrl(String resourceType){
        return MessageFormat.format(
                "{0}flow.html?_flowId=searchFlow&mode=search&{1}",
                requestInfoProvider.getBaseUrl(), buildResourceTypeFilterParams(resourceType)
        );

    }

}

