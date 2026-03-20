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
package com.jaspersoft.jasperserver.search.common;

import com.jaspersoft.jasperserver.api.common.domain.ExecutionContext;
import com.jaspersoft.jasperserver.api.metadata.common.domain.Folder;
import com.jaspersoft.jasperserver.api.search.QueryModificationEvaluator;
import com.jaspersoft.jasperserver.api.search.SearchFilter;
import com.jaspersoft.jasperserver.search.filter.AccessTypeFilter;
import com.jaspersoft.jasperserver.search.filter.FolderFilter;
import com.jaspersoft.jasperserver.search.service.RepositorySearchCriteria;
import com.jaspersoft.jasperserver.search.service.RepositorySearchService;

import java.util.List;

/**
 * Implementation of {@link QueryModificationEvaluator}.
 *
 * @author Chaim Arbiv
 * @author Yuriy Plakosh
 * @version $Id$
 * @since 4.7
 *
 * @see com.jaspersoft.jasperserver.api.search.QueryModificationEvaluator;
 */
public class QueryModificationEvaluatorImpl implements QueryModificationEvaluator {
    @jakarta.annotation.Resource
    private FolderFilter folderFilter;

    /**
     * In case of filtering by access event we are going from the access events table to the resource table so we will
     * have duplicates. In this case we need to add distinct projection to the query.
     *
     * @param context the execution context.
     * @return <code>true</code> if distinct projection should be applied, <code>false</code> otherwise.
     */
    @Override
    public boolean useFullResource(ExecutionContext context) {
        boolean use = context != null && context.getAttributes() != null && context.getAttributes().contains(FORCE_REPO_RESOURCE);

        if (!use){
        SearchAttributes searchAttributes = getTypedAttribute(context, SearchAttributes.class);
            use = searchAttributes != null &&
                searchAttributes.getState() != null &&
                searchAttributes.getState().getCustomFiltersMap() != null &&
                searchAttributes.getState().getCustomFiltersMap().
                        get(AccessTypeFilter.ACCESS_TYPE_FILTER_NAME) != null &&
                !searchAttributes.getState().getCustomFiltersMap().get(AccessTypeFilter.ACCESS_TYPE_FILTER_NAME).equals(
                        AccessTypeFilter.ACCESS_TYPE_FILTER_ALL_OPTION);
        }

        RepositorySearchCriteria searchCriteria = getTypedAttribute(context, RepositorySearchCriteria.class);

        if (!use){
            if(searchCriteria != null){
                final List<SearchFilter> customFilters = searchCriteria.getCustomFilters();
                use = customFilters != null && (!customFilters.isEmpty()
                        && !(customFilters.size() == 1 && customFilters.contains(folderFilter)));
                if(!use){
                    use = RepositorySearchService.PARAM_SORT_BY_POPULARITY.equals(searchCriteria.getSortBy());
                }
            }
        }

        if (!use){
            use = searchCriteria != null
                    && (searchCriteria.getFileResourceTypes() != null && !searchCriteria.getFileResourceTypes().isEmpty());
            if (use && searchCriteria.getResourceTypes() != null) {
                use = !searchCriteria.getResourceTypes().contains(Folder.class.getName());
            }
            if(!use){
                use = searchCriteria != null && searchCriteria.getCustomDataSourceTypes() != null
                        && !searchCriteria.getCustomDataSourceTypes().isEmpty();
            }
        }
        return use;
    }


    protected <T> T getTypedAttribute(ExecutionContext context, Class<T> attributeClass) {
        T result = null;
        if (context != null && context.getAttributes() != null && !context.getAttributes().isEmpty())
            for (Object currentAttribute : context.getAttributes())
                if (attributeClass.isAssignableFrom(currentAttribute.getClass())) {
                    // casting safety is checked above
                    @SuppressWarnings("unchecked")
                    final T typedAttribute = (T) currentAttribute;
                    result = typedAttribute;
                    break;
                }
        return result;
    }
}

