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
package com.jaspersoft.jasperserver.inputcontrols.cascade.handlers;

import com.jaspersoft.jasperserver.api.engine.common.service.ReportInputControlInformation;
import com.jaspersoft.jasperserver.api.logging.audit.context.AuditContext;
import com.jaspersoft.jasperserver.api.logging.audit.domain.AuditEvent;
import com.jaspersoft.jasperserver.api.logging.audit.domain.AuditEventType;
import com.jaspersoft.jasperserver.api.metadata.common.domain.InputControl;
import com.jaspersoft.jasperserver.api.metadata.common.domain.ListOfValuesItem;
import com.jaspersoft.jasperserver.api.metadata.common.domain.Query;
import com.jaspersoft.jasperserver.api.metadata.common.domain.ResourceReference;
import com.jaspersoft.jasperserver.api.metadata.common.domain.client.ListOfValuesItemImpl;
import com.jaspersoft.jasperserver.inputcontrols.cascade.CachedRepositoryService;
import com.jaspersoft.jasperserver.inputcontrols.cascade.CascadeResourceNotFoundException;
import com.jaspersoft.jasperserver.inputcontrols.cascade.handlers.converters.DataConverterService;
import com.jaspersoft.jasperserver.inputcontrols.cascade.token.FilterResolver;
import org.apache.commons.collections.OrderedMap;
import org.apache.commons.collections.OrderedMapIterator;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.jaspersoft.jasperserver.inputcontrols.cascade.handlers.InputControlHandler.NOTHING_SUBSTITUTION_LABEL;
import static com.jaspersoft.jasperserver.inputcontrols.cascade.handlers.InputControlHandler.NOTHING_SUBSTITUTION_VALUE;

/**
 * @author Yaroslav.Kovalchyk
 */
@Service
public class QueryValuesLoader implements ValuesLoader {
    public static final String COLUMN_VALUE_SEPARATOR = " | ";

    @jakarta.annotation.Resource
    protected FilterResolver filterResolver;
    @jakarta.annotation.Resource
    protected CachedRepositoryService cachedRepositoryService;
    @jakarta.annotation.Resource
    private AuditContext concreteAuditContext;
    @jakarta.annotation.Resource
    private DataConverterService dataConverterService;
    @jakarta.annotation.Resource
    private InputControlDataSourceResolver inputControlDataSourceResolver;
    @jakarta.annotation.Resource
    private ValuesLoaderStrategy queryValuesLoaderStrategy;
    @jakarta.annotation.Resource
    private ValuesLoaderStrategy parametersValuesLoaderStrategy;

    @Override
    public List<ListOfValuesItem> loadValues(InputControl inputControl, ResourceReference dataSource, Map<String, Object> parameters, Map<String, Class<?>> parameterTypes, ReportInputControlInformation info, boolean isSingleSelect) throws CascadeResourceNotFoundException {
        createInputControlsAuditEvent(inputControl.getURIString(), parameters);

        List<ListOfValuesItem> listOfValuesItems = null;

        /* Typed results are returned */
        String criteria = getCriteria(inputControl, parameters);
        ResultsOrderedMap results = getLoaderStrategy(parameters).resultsOrderedMap(inputControl, dataSource, criteria, parameters, parameterTypes);
        OrderedMap orderedMap = results.getOrderedMap();
        addNothingLabelToResults(orderedMap, isSingleSelect, inputControl);

        if (orderedMap != null) {
            listOfValuesItems = getListOfValuesItems(inputControl, info,
                    results.isSkipCriteriaSearch() ? null : criteria, orderedMap);
            addTotalCountToParameters(parameters, listOfValuesItems.size());
        }

        closeInputControlsAuditEvent();

        return listOfValuesItems;
    }

    protected List<ListOfValuesItem> getListOfValuesItems(InputControl inputControl,
                                                          ReportInputControlInformation info,
                                                          String criteria,
                                                          OrderedMap results) throws CascadeResourceNotFoundException {
        List<ListOfValuesItem> result = new ArrayList<>(results.size());

        if (results.containsKey(NOTHING_SUBSTITUTION_VALUE)) {
            createAndAddItem(criteria, result, NOTHING_SUBSTITUTION_VALUE, NOTHING_SUBSTITUTION_LABEL);
        }

        OrderedMapIterator it = results.orderedMapIterator();

        while (it.hasNext()) {
            Object valueColumn = it.next();
            if (NOTHING_SUBSTITUTION_VALUE.equals(valueColumn)) continue;

            Object[] visibleColumns = (Object[]) it.getValue();
            String label = extractLabelFromResults(inputControl, info, visibleColumns);
            createAndAddItem(criteria, result, valueColumn, label);
        }

        return result;
    }

    private void createAndAddItem(String criteria, List<ListOfValuesItem> result, Object valueColumn, String label) {
        ListOfValuesItem item = new ListOfValuesItemImpl();
        item.setLabel(label);

        valueColumn = formatValueColumn(valueColumn);
        item.setValue(valueColumn);

        /**
         * Filter the results based on the totalLimit and by search criteria
         * when provided and then add item to result.
         */
        checkCriteriaAndAddItem(criteria, result, item);
    }

    private void addNothingLabelToResults(OrderedMap results, boolean isSingleSelect, InputControl inputControl) {
        if (results != null && isSingleSelect && !inputControl.isMandatory()) {
            results.put(NOTHING_SUBSTITUTION_VALUE, new Object[]{NOTHING_SUBSTITUTION_LABEL});
        }
    }

    protected String extractLabelFromResults(InputControl inputControl, ReportInputControlInformation info, Object[] visibleColumns) throws CascadeResourceNotFoundException {
        StringBuilder label = new StringBuilder();

        for (int i = 0; i < visibleColumns.length; i++) {
            Object visibleColumn = visibleColumns[i];
            String visibleColumnName = inputControl.getQueryVisibleColumns()[i];
            boolean isVisibleColumnMatchesValueColumn = inputControl.getQueryValueColumn().equals(visibleColumnName);

            checkLabelLength(label);

            String formattedValue = formatValueToString(visibleColumn, isVisibleColumnMatchesValueColumn, inputControl, info);
            label.append(visibleColumn != null ? formattedValue : InputControlHandler.NULL_SUBSTITUTION_LABEL);
        }

        return label.toString();
    }

    private void checkLabelLength(StringBuilder label) {
        if (label.length() > 0) {
            label.append(COLUMN_VALUE_SEPARATOR);
        }
    }

    private Object formatValueColumn(Object valueColumn) {
        if (valueColumn instanceof BigDecimal) {
            valueColumn = ((BigDecimal) valueColumn).stripTrailingZeros();
        }
        return valueColumn;
    }

    private String formatValueToString(Object visibleColumn, boolean isVisibleColumnMatchesValueColumn,
                                       InputControl inputControl, ReportInputControlInformation info)
            throws CascadeResourceNotFoundException {

        if (isVisibleColumnMatchesValueColumn) {
            return dataConverterService.formatSingleValue(visibleColumn, inputControl, info);
        } else {
            return dataConverterService.formatSingleValue(visibleColumn, (InputControl) null, null);
        }
    }

    @Override
    public Set<String> getMasterDependencies(InputControl inputControl, ResourceReference dataSource) throws CascadeResourceNotFoundException {
        ResourceReference dataSourceForQuery = inputControlDataSourceResolver.resolveDatasource(inputControl, dataSource);
        Map<String, Object> filterParameters = inputControlDataSourceResolver.prepareDomainDataSourceParameters(dataSourceForQuery);
        Query query = cachedRepositoryService.getResource(Query.class, inputControl.getQuery());
        String querySQL = query.getSql();
        return filterResolver.getParameterNames(querySQL, filterParameters);
    }

    protected void createInputControlsAuditEvent(final String resourceUri, final Map<String, Object> parameters) {
        concreteAuditContext.doInAuditContext(new AuditContext.AuditContextCallback() {
            public void execute() {
                AuditEvent event = concreteAuditContext.createAuditEvent(AuditEventType.INPUT_CONTROLS_QUERY.toString());
                if (event.getResourceUri() == null) {
                    event.setResourceUri(resourceUri);
                }

                for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                    if (entry.getKey() != null) {
                        concreteAuditContext.addPropertyToAuditEvent("inputControlParam", entry, event);
                    }
                }

            }
        });
    }

    protected void closeInputControlsAuditEvent() {
        concreteAuditContext.doInAuditContext(AuditEventType.INPUT_CONTROLS_QUERY.toString(), new AuditContext.AuditContextCallbackWithEvent() {
            public void execute(AuditEvent auditEvent) {
                concreteAuditContext.closeAuditEvent(auditEvent);
            }
        });
    }

    /**
     * This method determines whether we need to load values from a database, or from parameters (came from topic's XML)
     *
     * @param parameters parameters that contains selected Input Controls values
     * @return a concrete strategy
     */
    ValuesLoaderStrategy getLoaderStrategy(Map<String, Object> parameters) {
        // Fix for https://jira.tibco.com/browse/JS-62353
        return parameters != null && Boolean.TRUE.toString().equals(parameters.get(SKIP_FETCHING_IC_VALUES_FROM_DB))
                ? parametersValuesLoaderStrategy
                : queryValuesLoaderStrategy;
    }
}

