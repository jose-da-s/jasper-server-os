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
import com.jaspersoft.jasperserver.api.metadata.common.domain.InputControl;
import com.jaspersoft.jasperserver.api.metadata.common.domain.ListOfValues;
import com.jaspersoft.jasperserver.api.metadata.common.domain.ListOfValuesItem;
import com.jaspersoft.jasperserver.api.metadata.common.domain.ResourceReference;
import com.jaspersoft.jasperserver.api.metadata.common.domain.client.ListOfValuesItemImpl;
import com.jaspersoft.jasperserver.inputcontrols.cascade.CachedRepositoryService;
import com.jaspersoft.jasperserver.inputcontrols.cascade.CascadeResourceNotFoundException;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.jaspersoft.jasperserver.inputcontrols.cascade.handlers.InputControlHandler.NOTHING_SUBSTITUTION_LABEL;
import static com.jaspersoft.jasperserver.inputcontrols.cascade.handlers.InputControlHandler.NOTHING_SUBSTITUTION_VALUE;


/**
 * @author Yaroslav.Kovalchyk
 * @version $Id$
 */
@Service
public class ListOvValuesLoader implements ValuesLoader {
    @Resource
    private CachedRepositoryService cachedRepositoryService;

    @Override
    public List<ListOfValuesItem> loadValues(InputControl inputControl, ResourceReference dataSource, Map<String, Object> parameters, Map<String, Class<?>> parameterTypes, ReportInputControlInformation info, boolean isSingleSelect) throws CascadeResourceNotFoundException {
        final ListOfValues listOfValues = cachedRepositoryService.getResource(ListOfValues.class, inputControl.getListOfValues());

        if (listOfValues != null) {
            List<ListOfValuesItem> listOfValuesItems = createAndAddListOfValuesItems(listOfValues, isSingleSelect, inputControl);
            return filterListOfValuesItems(listOfValuesItems, parameters, inputControl);
        } else {
            return null;
        }
    }

    protected List<ListOfValuesItem> filterListOfValuesItems(List<ListOfValuesItem> listOfValuesItems, Map<String, Object> parameters, InputControl inputControl) {
        List<ListOfValuesItem> results = new ArrayList<>();
        String criteria = getCriteria(inputControl, parameters);

        /**
         * Filter the results based on the totalLimit and by search criteria
         * when provided and then add item to results.
         */
        listOfValuesItems.forEach(item -> checkCriteriaAndAddItem(criteria, results, item));

        addTotalCountToParameters(parameters, results.size());

        return results;
    }

    @Override
    public Set<String> getMasterDependencies(InputControl inputControl, ResourceReference dataSource) {
        // no master dependencies for this type
        return null;
    }

    private List<ListOfValuesItem> createAndAddListOfValuesItems(ListOfValues listOfValues, boolean isSingleSelect, InputControl inputControl) {
        List<ListOfValuesItem> listOfValuesItems;

        if (isSingleSelect && !inputControl.isMandatory()) {
            listOfValuesItems = new ArrayList<>();
            ListOfValuesItem item = new ListOfValuesItemImpl();
            item.setValue(NOTHING_SUBSTITUTION_VALUE);
            item.setLabel(NOTHING_SUBSTITUTION_LABEL);

            listOfValuesItems.add(item);
            listOfValuesItems.addAll(Arrays.asList(listOfValues.getValues()));
        } else {
            listOfValuesItems = Arrays.asList(listOfValues.getValues());
        }

        return listOfValuesItems;
    }

}

