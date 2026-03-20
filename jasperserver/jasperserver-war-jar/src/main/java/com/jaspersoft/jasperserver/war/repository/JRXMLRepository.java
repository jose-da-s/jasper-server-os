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

package com.jaspersoft.jasperserver.war.repository;

import com.jaspersoft.jasperserver.api.common.domain.ExecutionContext;
import com.jaspersoft.jasperserver.api.metadata.common.domain.FileResource;
import com.jaspersoft.jasperserver.api.metadata.common.domain.ResourceLookup;
import com.jaspersoft.jasperserver.api.metadata.common.service.RepositoryService;
import com.jaspersoft.jasperserver.api.metadata.view.domain.FilterCriteria;
import com.jaspersoft.jasperserver.api.search.BasicTransformerFactory;
import com.jaspersoft.jasperserver.api.search.SearchCriteria;
import com.jaspersoft.jasperserver.api.search.SearchFilter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.webflow.execution.RequestContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.jaspersoft.jasperserver.api.common.util.StaticExecutionContextProvider.getExecutionContext;
import static com.jaspersoft.jasperserver.api.metadata.common.domain.FileResource.TYPE_JRXML;
import static org.apache.commons.lang3.ArrayUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.EMPTY;

/**
 * Created by stas on 3/10/15.
 */
public class JRXMLRepository {
    public static final String FILE_TYPE_PROPERTY = "fileType";

    private final Log log = LogFactory.getLog(JRXMLRepository.class);

    @jakarta.annotation.Resource(name = "${bean.repositoryService}")
    private RepositoryService repository;

    /**
     * Get a list of all jrxml files in repo
     *
     * @param context
     * @return
     */
    public List<String> listAll(RequestContext context) {
        ResourceLookup[] lookups = repository.findResource(getExecutionContext(), jrxmlFilesCriteria());

        List<String> allJrxmls = null;
        if (isNotEmpty(lookups)) {
            log("Found JRXML lookups size = ", lookups.length);

            allJrxmls = new ArrayList<String>(lookups.length);

            for (int i = 0; i < lookups.length; i++) {
                allJrxmls.add(lookups[i].getURIString());
            }
        }

        return allJrxmls;
    }

    public static FilterCriteria jrxmlFilesCriteria() {
        FilterCriteria criteria = FilterCriteria.createFilter(FileResource.class);
        criteria.addFilterElement(FilterCriteria.createPropertyEqualsFilter(FILE_TYPE_PROPERTY, TYPE_JRXML));
        return criteria;
    }

    private void log(String s, Object arg) {
        if (log.isDebugEnabled()) {
            log.debug(s + arg.toString());
        }
    }

    /**
     * Count all jrxml files in the repo
     *
     * @return
     */
    public int count() {
        List<Class> jrxmlTypeList = new ArrayList<Class>();
        jrxmlTypeList.add(FileResource.class);

        List<SearchFilter> jrxmlFilesFilters = new ArrayList<SearchFilter>();
        jrxmlFilesFilters.add(jrxmlFilesSearchFilter());
        Map<Class, Integer> resultMap =
                repository.loadResourcesMapCount(getExecutionContext(), EMPTY, jrxmlTypeList, jrxmlFilesFilters,
                        null, null, new BasicTransformerFactory());

        return resultMap.get(FileResource.class);
    }

    public static SearchFilter jrxmlFilesSearchFilter() {
        return new SearchFilter() {
            @Override
            public void applyRestrictions(String type, ExecutionContext context, SearchCriteria criteria) {
                criteria.add(Restrictions.eq(FILE_TYPE_PROPERTY, TYPE_JRXML));
            }
        };
    }

    public boolean isAnyJrxmlExists() {
        return repository.resourceExists(getExecutionContext(), jrxmlFilesCriteria());
    }

    public void setRepository(RepositoryService repository) {
        this.repository = repository;
    }
}

