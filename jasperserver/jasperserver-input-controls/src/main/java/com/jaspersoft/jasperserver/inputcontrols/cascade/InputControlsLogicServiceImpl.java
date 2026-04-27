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
package com.jaspersoft.jasperserver.inputcontrols.cascade;

import com.jaspersoft.jasperserver.api.common.domain.ValidationErrors;
import com.jaspersoft.jasperserver.api.common.domain.impl.ValidationErrorsImpl;
import com.jaspersoft.jasperserver.api.engine.common.service.SecurityContextProvider;
import com.jaspersoft.jasperserver.api.engine.jasperreports.service.impl.EhcacheEngineService;
import com.jaspersoft.jasperserver.api.metadata.common.domain.InputControlsContainer;
import com.jaspersoft.jasperserver.api.metadata.common.domain.ResourceReference;
import com.jaspersoft.jasperserver.dto.reports.inputcontrols.InputControlState;
import com.jaspersoft.jasperserver.dto.reports.inputcontrols.ReportInputControl;
import com.jaspersoft.jasperserver.dto.reports.inputcontrols.SelectedValuesListWrapper;
import com.jaspersoft.jasperserver.inputcontrols.cascade.cache.ControlLogicCacheManager;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.extension.annotations.SpanAttribute;
import io.opentelemetry.extension.annotations.WithSpan;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @author Yaroslav.Kovalchyk
 * @version $Id: InputControlsLogicServiceImpl.java 22695 2012-03-21 13:55:32Z ykovalchyk $
 */
@Service("inputControlsLogicService")
public class InputControlsLogicServiceImpl implements InputControlsLogicService {
    @Resource
    private HashMap<String, ControlLogic<InputControlsContainer>> controlLogicRegestry;
    @Resource
    private HashMap<String, String> controlLogicUriMapping;
    @Resource
    private HashMap<String, String> controlLogicReportTypeMapping;
    @Resource
    private String defaultControlLogicName;
    @Resource
    private ControlLogicCacheManager controlLogicCacheManager;
    @Resource
    private CachedRepositoryService cachedRepositoryService;
    @Resource(name = "concreteSecurityContextProvider")
    protected SecurityContextProvider securityContextProvider;
    @Resource
    protected EvaluationEventsHandler defaultEvaluationEventsHandler;

    public Map<String, Object> getTypedParameters(
            String containerUri, final Map<String, String[]> requestParameters, final boolean skipValidation)
            throws CascadeResourceNotFoundException, InputControlsValidationException {

        return callControlLogic(containerUri, new ControLogicCaller<Map<String, Object>>() {
            @Override
            public Map<String, Object> callByContainerUri(ControlLogic<InputControlsContainer> controlLogic, String containerUri) throws CascadeResourceNotFoundException, InputControlsValidationException {
                return controlLogic.getTypedParameters(containerUri, requestParameters, skipValidation);
            }

            @Override
            public Map<String, Object> callByContainer(ControlLogic<InputControlsContainer> controlLogic, InputControlsContainer container) throws CascadeResourceNotFoundException, InputControlsValidationException {
                return controlLogic.getTypedParameters(container, requestParameters, skipValidation);
            }
        });
    }

    public Map<String, Object> getTypedParameters(
            String containerUri, final Map<String, String[]> requestParameters)
            throws CascadeResourceNotFoundException, InputControlsValidationException {

        return getTypedParameters(containerUri, requestParameters, false);
    }

    public Map<String, String[]> formatTypedParameters(
            String containerUri, final Map<String, Object> requestParameters) throws CascadeResourceNotFoundException, InputControlsValidationException {

        return callControlLogic(containerUri, new ControLogicCaller<Map<String, String[]>>() {
            @Override
            public Map<String, String[]> callByContainerUri(ControlLogic<InputControlsContainer> controlLogic, String containerUri) throws CascadeResourceNotFoundException, InputControlsValidationException {
                return controlLogic.formatTypedParameters(containerUri, requestParameters);
            }

            @Override
            public Map<String, String[]> callByContainer(ControlLogic<InputControlsContainer> controlLogic, InputControlsContainer container) throws CascadeResourceNotFoundException, InputControlsValidationException {
                return controlLogic.formatTypedParameters(container, requestParameters);
            }
        });
    }

    @WithSpan
    public List<ReportInputControl> getInputControlsStructure(@SpanAttribute("resourceUri") String containerUri, final Set<String> inputControlIds) throws CascadeResourceNotFoundException {
        controlLogicCacheManager.clearCache();
        try {
            return callControlLogic(containerUri, new ControLogicCaller<List<ReportInputControl>>() {
                @Override
                public List<ReportInputControl> callByContainerUri(ControlLogic<InputControlsContainer> controlLogic, String containerUri) throws CascadeResourceNotFoundException, InputControlsValidationException {
                    return controlLogic.getInputControlsStructure(containerUri, inputControlIds);
                }

                @Override
                public List<ReportInputControl> callByContainer(ControlLogic<InputControlsContainer> controlLogic, InputControlsContainer container) throws CascadeResourceNotFoundException, InputControlsValidationException {
                    return controlLogic.getInputControlsStructure(container, inputControlIds);
                }
            });
        } catch (InputControlsValidationException e) {
            // shouldn't happen
            throw new RuntimeException("Input controls validation failed", e);
        }
    }

    @WithSpan
    public SelectedValuesListWrapper getInputControlsSelectedValues(@SpanAttribute("resourceUri") String reportUnitUri, @SpanAttribute("freshData")boolean freshData, boolean withLabel) throws CascadeResourceNotFoundException {
        final Map<String, String[]> parameters = new HashMap<>();

        setCacheParameter(freshData, parameters);
        parameters.put("withLabel", new String[]{String.valueOf(withLabel)});
        try {
        return callControlLogic(reportUnitUri, new ControLogicCaller<SelectedValuesListWrapper>() {
            @Override
            public SelectedValuesListWrapper callByContainerUri(ControlLogic<InputControlsContainer> controlLogic, String containerUri) throws CascadeResourceNotFoundException, InputControlsValidationException {
                return controlLogic.getSelectedValues(containerUri, parameters);
            }
            @Override
            public SelectedValuesListWrapper callByContainer(ControlLogic<InputControlsContainer> controlLogic, InputControlsContainer container) throws CascadeResourceNotFoundException, InputControlsValidationException {
                return controlLogic.getSelectedValues(container, parameters);
            }
            });
        } catch (InputControlsValidationException e) {
            //Should not happen
            throw new RuntimeException(e);
        }
    }

    @WithSpan
    public List<ReportInputControl> getInputControlsWithValues(@SpanAttribute("resourceUri") String reportUnitUri, Set<String> inputControlIds, Map<String, String[]> rawParameters) throws CascadeResourceNotFoundException {
        List<ReportInputControl> result = getInputControlsStructure(reportUnitUri, inputControlIds);
        if (result != null && !result.isEmpty()) {
            final List<InputControlState> states = getValuesForInputControls(reportUnitUri, inputControlIds, rawParameters, false);
            if (states != null && !states.isEmpty()) {
                Map<String, InputControlState> statesMap = new HashMap<String, InputControlState>();
                for (InputControlState currentState : states)
                    statesMap.put(currentState.getId(), currentState);
                for (ReportInputControl currentInputControl : result)
                    currentInputControl.setState(statesMap.get(currentInputControl.getId()));
            }
        }
        return result;
    }

    @WithSpan
    public synchronized List<ReportInputControl> reorderInputControls(@SpanAttribute("resourceUri") String containerUri, List<String> newOrder) throws CascadeResourceNotFoundException, InputControlsValidationException {
        List<ReportInputControl> existing = getInputControlsStructure(containerUri, null), reordered = new ArrayList<ReportInputControl>(existing.size());
        reordered.addAll(existing);
        if (existing.size() != newOrder.size()){
            ValidationErrors errors = new ValidationErrorsImpl();
            errors.add(new InputControlValidationError("size.not.equal",null,"", null, null));
            throw new InputControlsValidationException(errors);
        }

        int[] reorderingMapping = new int[newOrder.size()];
        Set<String> matchedIds = new HashSet<String>();
        for (int newIndex  = 0; newIndex < newOrder.size(); newIndex++){
            for (int oldIndex  = 0; oldIndex < newOrder.size(); oldIndex++){
                if (existing.get(oldIndex).getId().equals(newOrder.get(newIndex))){
                    if (oldIndex != newIndex){
                        ReportInputControl t = reordered.remove(oldIndex);
                        if (newIndex == existing.size()){
                            reordered.add(t);
                        } else {
                            reordered.add(newIndex, t);
                        }
                    }
                    reorderingMapping[newIndex] = oldIndex;
                    matchedIds.add(newOrder.get(newIndex));
                }
            }
        }

        if (matchedIds.size() != existing.size()){
            // it is not reordering
            ValidationErrors errors = new ValidationErrorsImpl();
            errors.add(new InputControlValidationError("ids.not.match",null,null,"",null));
            throw new InputControlsValidationException(errors);
        }

        reorderControlsInternal(containerUri, reorderingMapping);

        return reordered;
    }
    @WithSpan
    public List<InputControlState> getValuesForInputControls(@SpanAttribute("resourceUri") String containerUri, final Set<String> inputControlIds,
            Map<String, String[]> originalParameters, @SpanAttribute("freshData") boolean freshData) throws CascadeResourceNotFoundException {
        final Map<String, String[]> parameters = originalParameters != null ? new HashMap<String, String[]>(originalParameters) : new HashMap<String, String[]>();
        List<InputControlState> states;
        setCacheParameter(freshData, parameters);
        defaultEvaluationEventsHandler.beforeEvaluation(containerUri, parameters, securityContextProvider.getContextUser());
        try {
            states = callControlLogic(containerUri, new ControLogicCaller<List<InputControlState>>() {
                @Override
                public List<InputControlState> callByContainerUri(ControlLogic<InputControlsContainer> controlLogic, String containerUri) throws CascadeResourceNotFoundException, InputControlsValidationException {
                    return controlLogic.getValuesForInputControlsFromRawData(containerUri, inputControlIds, parameters);
                }

                @Override
                public List<InputControlState> callByContainer(ControlLogic<InputControlsContainer> controlLogic, InputControlsContainer container) throws CascadeResourceNotFoundException, InputControlsValidationException {
                    return controlLogic.getValuesForInputControlsFromRawData(container, inputControlIds, parameters);
                }
            });
        } catch (InputControlsValidationException e) {
            //Should not happen
            throw new RuntimeException(e);
        }
        defaultEvaluationEventsHandler.afterEvaluation(containerUri, parameters, states, securityContextProvider.getContextUser());
        return states;
    }

    // similar to getValuesForInputControls() function, but it won't load icContainer from URI
    // use icContainer in memory instead
    public List<InputControlState> getValuesForInputControlsFromICContainer(InputControlsContainer icContainer, final Set<String> inputControlIds,
                                                                            Map<String, String[]> originalParameters, boolean freshData) throws CascadeResourceNotFoundException {
        final Map<String, String[]> parameters = originalParameters != null ? new HashMap<String, String[]>(originalParameters) : new HashMap<String, String[]>();
        setCacheParameter(freshData, parameters);
        List<InputControlState> states;
        defaultEvaluationEventsHandler.beforeEvaluation(icContainer.getURI(), parameters, securityContextProvider.getContextUser());
        try {
            states = getControlLogic(icContainer).getValuesForInputControlsFromRawData(icContainer, inputControlIds, parameters);
        } catch (InputControlsValidationException e) {
            //Should not happen
            throw new RuntimeException(e);
        }
        defaultEvaluationEventsHandler.afterEvaluation(icContainer.getURI(), parameters, states, securityContextProvider.getContextUser());
        return states;
    }

    public void setCacheParameter(boolean freshData, Map<String, String[]> parameters) {
        if (freshData) {
            controlLogicCacheManager.clearCache();
            parameters.put(EhcacheEngineService.IC_REFRESH_KEY, new String[]{"true"});
        }
    }

    protected void reorderControlsInternal(String containerUri, int[] reorderMapping) throws CascadeResourceNotFoundException {
        InputControlsContainer container = cachedRepositoryService.getResource(InputControlsContainer.class, containerUri);
        List<ResourceReference> inputControls = container.getInputControls(), reordered = new LinkedList<ResourceReference>();

        if (inputControls.size() == reorderMapping.length) {
            for (int i = 0; i < reorderMapping.length; i++) {
                reordered.add(inputControls.get(reorderMapping[i]));
            }

            container.setInputControls(reordered);
            cachedRepositoryService.updateResource(container);
        }
    }

    protected String getControlLogicAliasByContainerUri(String containerUri) {
        String controlLogicAlias = null;
        if (!controlLogicUriMapping.isEmpty()) {
            // try to find control logic alias in URI mapping
            for (String currentyUriMappingExpression : controlLogicUriMapping.keySet())
                if (Pattern.matches(currentyUriMappingExpression, containerUri)) {
                    controlLogicAlias = controlLogicUriMapping.get(currentyUriMappingExpression);
                    break;
                }
        }
        return controlLogicAlias;
    }

    protected <T> T callControlLogic(String containerUri, ControLogicCaller<T> caller) throws CascadeResourceNotFoundException, InputControlsValidationException {
        T result;
        String controlLogicAlias = getControlLogicAliasByContainerUri(containerUri);
        if (controlLogicAlias != null)
            result = caller.callByContainerUri(getControlLogicByAlias(controlLogicAlias), containerUri);
        else {
            final InputControlsContainer container = cachedRepositoryService.getResource(InputControlsContainer.class, containerUri);
            controlLogicAlias = controlLogicReportTypeMapping.get(container.getResourceType());
            if (controlLogicAlias == null)
                controlLogicAlias = defaultControlLogicName;
            result = caller.callByContainer(getControlLogicByAlias(controlLogicAlias), container);
        }
        return result;
    }

    protected ControlLogic<InputControlsContainer> getControlLogic(InputControlsContainer icContainer) throws CascadeResourceNotFoundException, InputControlsValidationException {
        String controlLogicAlias = getControlLogicAliasByContainerUri(icContainer.getURI());
        if (controlLogicAlias != null) {
            return getControlLogicByAlias(controlLogicAlias);
        } else {
            controlLogicAlias = controlLogicReportTypeMapping.get(icContainer.getResourceType());
            if (controlLogicAlias == null)
                controlLogicAlias = defaultControlLogicName;
            return getControlLogicByAlias(controlLogicAlias);
        }
    }


    protected ControlLogic<InputControlsContainer> getControlLogicByAlias(String controlLogicAlias) {
        final ControlLogic<InputControlsContainer> controlLogic = controlLogicRegestry.get(controlLogicAlias);
        if (controlLogic == null)
            throw new IllegalStateException("Control logic for alias '" + controlLogicAlias + "' not found");
        return controlLogic;
    }

    protected interface ControLogicCaller<T> {
        T callByContainerUri(ControlLogic<InputControlsContainer> controlLogic, String containerUri) throws CascadeResourceNotFoundException, InputControlsValidationException;

        T callByContainer(ControlLogic<InputControlsContainer> controlLogic, InputControlsContainer container) throws CascadeResourceNotFoundException, InputControlsValidationException;
    }


    public void setControlLogicCacheManager(ControlLogicCacheManager controlLogicCacheManager) {
        this.controlLogicCacheManager = controlLogicCacheManager;
    }
}

