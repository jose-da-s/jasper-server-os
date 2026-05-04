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

package com.jaspersoft.jasperserver.search.filter;

import com.jaspersoft.jasperserver.api.common.domain.ExecutionContext;
import com.jaspersoft.jasperserver.api.logging.access.domain.AccessEvent;
import com.jaspersoft.jasperserver.api.logging.access.service.impl.AccessService;
import com.jaspersoft.jasperserver.api.metadata.common.domain.Folder;
import com.jaspersoft.jasperserver.api.metadata.common.service.ResourceFactory;
import com.jaspersoft.jasperserver.api.metadata.user.domain.User;
import com.jaspersoft.jasperserver.api.search.SearchCriteria;
import com.jaspersoft.jasperserver.api.search.SortByAccessTimeTransformer;
import com.jaspersoft.jasperserver.search.common.SearchAttributes;
import com.jaspersoft.jasperserver.search.mode.AccessType;
import com.jaspersoft.jasperserver.search.mode.SearchMode;
import com.jaspersoft.jasperserver.search.service.RepositorySearchCriteria;
import org.hibernate.criterion.*;

import jakarta.annotation.Resource;
import java.io.Serializable;
import java.util.List;

/**
 * Access type filter.
 * 
 * @author Stas Chubar
 * @version $Id$
 */
public class AccessTypeFilter extends BaseSearchFilter implements Serializable {
    public static final String ACCESS_TYPE_FILTER_NAME = "accessTypeFilter";
    public static final String ACCESS_TYPE_FILTER_ALL_OPTION = "accessTypeFilter-all";
    public static final String ACCESS_TYPE_FILTER_CHANGED_BY_ME_OPTION = "accessTypeFilter-changedByMe";

    @Resource(name = "accessService")
    AccessService accessService;

    @Resource(name = "persistentMappings")
    ResourceFactory persistentClassFactory;

    public void applyRestrictions(String type, ExecutionContext context, SearchCriteria criteria) {
        RepositorySearchCriteria searchCriteria = getTypedAttribute(context, RepositorySearchCriteria.class);
        DetachedCriteria accessFilterCriteria = DetachedCriteria.forClass(persistentClassFactory.getImplementationClass(AccessEvent.class));

        if (searchCriteria == null) {
            SearchAttributes searchAttributes = getSearchAttributes(context);

            User user = null;

            if (context.getAttributes() != null) {
                for (Object o : context.getAttributes()) {
                    if (o instanceof User) {
                        user = (User) o;
                    }
                }
            }

            if (user != null && searchAttributes != null && searchAttributes.getState() != null) {
                String accessFilter = searchAttributes.getState().getCustomFiltersMap().get(ACCESS_TYPE_FILTER_NAME);

                if (accessFilter != null && !accessFilter.equals(ACCESS_TYPE_FILTER_ALL_OPTION)) {
                    processCriteria(searchCriteria, criteria, accessFilterCriteria, user, accessFilter.equals(ACCESS_TYPE_FILTER_CHANGED_BY_ME_OPTION));
                }
            }
        } else {
            if (!searchCriteria.getAccessType().equals(AccessType.ALL)){
                processCriteria(searchCriteria, criteria, accessFilterCriteria, searchCriteria.getUser(), AccessType.MODIFIED.equals(searchCriteria.getAccessType()));
            }

        }
    }

    private void processCriteria(RepositorySearchCriteria searchCriteria, SearchCriteria criteria, DetachedCriteria accessFilterCriteria, User user, boolean modified) {
        boolean isMultiColumn = false;
        int max = 0;
        accessFilterCriteria.add(Restrictions.eq("updating", modified));
        accessFilterCriteria.add(Restrictions.eq("userId", user.getUsername()+
                                        (user.getTenantId()==null?"":"|"+user.getTenantId())));
        if(searchCriteria != null
                && SearchMode.SEARCH.equals(searchCriteria.getSearchMode())
                && AccessType.VIEWED.equals(searchCriteria.getAccessType())
                && "accessTime".equalsIgnoreCase(searchCriteria.getSortBy())){
            accessFilterCriteria.add(Restrictions.in("resourceType", searchCriteria.getResourceTypes()));
            accessFilterCriteria.add(Restrictions.eq("hidden", false));
            accessFilterCriteria.setProjection(Projections.projectionList().
                    add(Projections.groupProperty("resourceUri")).
                    add(Projections.max("eventDate"), "aed"));
            accessFilterCriteria.addOrder(Order.desc("aed"));
            isMultiColumn = true;
            max = searchCriteria.getMaxCount();
        } else {
            accessFilterCriteria.setProjection(Projections.distinct(Projections.property("resourceUri")));
        }

        List results = accessService.getResourceURIs(accessFilterCriteria, max);

        if(results != null && !results.isEmpty()) {
            if(isMultiColumn){
                results = new SortByAccessTimeTransformer().transformToURIList(results);
            }
            String alias = criteria.getAlias("parent", "p");
            Disjunction or = Restrictions.disjunction();
            for(Object resultUri: results){
                String uri = (String) resultUri;
                int sep = uri.lastIndexOf(Folder.SEPARATOR);
                String name = uri.substring(sep + Folder.SEPARATOR_LENGTH);
                //To Handle root uri
                if(sep == 0){
                    sep = sep + Folder.SEPARATOR_LENGTH;
                }
                String folderName = uri.substring(0, sep);
                Conjunction and = Restrictions.conjunction();
                and.add(Restrictions.eq("name", name));
                and.add(Restrictions.eq(alias +".URI", folderName));
                or.add(and);
            }
            criteria.add(or);
        } else {
            //To make the condition fail if the resourceIds is empty
            criteria.add(Restrictions.idEq(-1L));
        }
    }
}

