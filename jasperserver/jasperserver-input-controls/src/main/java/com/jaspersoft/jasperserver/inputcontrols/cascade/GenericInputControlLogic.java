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

import com.google.common.collect.FluentIterable;
import com.jaspersoft.jasperserver.api.JSException;
import com.jaspersoft.jasperserver.api.common.domain.ExecutionContext;
import com.jaspersoft.jasperserver.api.common.domain.ValidationErrors;
import com.jaspersoft.jasperserver.api.common.domain.impl.ExecutionContextImpl;
import com.jaspersoft.jasperserver.api.common.domain.impl.ValidationErrorsImpl;
import com.jaspersoft.jasperserver.api.common.util.ImportRunMonitor;
import com.jaspersoft.jasperserver.api.common.util.TimeZoneContextHolder;
import com.jaspersoft.jasperserver.api.common.util.diagnostic.DiagnosticSnapshotPropertyHelper;
import com.jaspersoft.jasperserver.api.engine.common.service.ReportInputControlInformation;
import com.jaspersoft.jasperserver.api.engine.common.service.ReportInputControlValueInformation;
import com.jaspersoft.jasperserver.api.engine.common.service.ReportInputControlValuesInformation;
import com.jaspersoft.jasperserver.api.engine.common.service.ReportInputControlsInformation;
import com.jaspersoft.jasperserver.api.engine.jasperreports.service.impl.EhcacheEngineService;
import com.jaspersoft.jasperserver.api.metadata.common.domain.DataType;
import com.jaspersoft.jasperserver.api.metadata.common.domain.InputControl;
import com.jaspersoft.jasperserver.api.metadata.common.domain.InputControlsContainer;
import com.jaspersoft.jasperserver.api.metadata.common.domain.ListOfValues;
import com.jaspersoft.jasperserver.api.metadata.common.domain.ListOfValuesItem;
import com.jaspersoft.jasperserver.api.metadata.common.domain.Query;
import com.jaspersoft.jasperserver.api.metadata.common.domain.QueryParameterDescriptor;
import com.jaspersoft.jasperserver.api.metadata.common.domain.ResourceReference;
import com.jaspersoft.jasperserver.api.metadata.common.domain.client.InputControlImpl;
import com.jaspersoft.jasperserver.api.metadata.common.domain.client.ListOfValuesImpl;
import com.jaspersoft.jasperserver.api.metadata.common.domain.client.ListOfValuesItemImpl;
import com.jaspersoft.jasperserver.api.metadata.common.domain.util.RefSets;
import com.jaspersoft.jasperserver.api.metadata.user.service.ObjectPermissionService;
import com.jaspersoft.jasperserver.api.security.internalAuth.InternalAuthenticationTokenImpl;
import com.jaspersoft.jasperserver.dto.reports.inputcontrols.InputControlOption;
import com.jaspersoft.jasperserver.dto.reports.inputcontrols.InputControlState;
import com.jaspersoft.jasperserver.dto.reports.inputcontrols.ReportInputControl;
import com.jaspersoft.jasperserver.dto.reports.inputcontrols.SelectedValue;
import com.jaspersoft.jasperserver.dto.reports.inputcontrols.SelectedValuesListWrapper;
import com.jaspersoft.jasperserver.inputcontrols.cascade.cache.ControlLogicCacheManager;
import com.jaspersoft.jasperserver.inputcontrols.cascade.handlers.InputControlHandler;
import com.jaspersoft.jasperserver.inputcontrols.cascade.handlers.ValueFormattingUtils;
import com.jaspersoft.jasperserver.inputcontrols.cascade.handlers.ValuesLoader;
import com.jaspersoft.jasperserver.inputcontrols.cascade.handlers.converters.InputControlValueClassResolver;
import com.jaspersoft.jasperserver.inputcontrols.cascade.token.FilterResolver;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.extension.annotations.WithSpan;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.jaspersoft.jasperserver.api.metadata.user.service.ProfileAttributesResolver.SKIP_PROFILE_ATTRIBUTES_RESOLVING;
import static com.jaspersoft.jasperserver.inputcontrols.cascade.handlers.InputControlHandler.WITH_LABEL;
import static com.jaspersoft.jasperserver.inputcontrols.cascade.handlers.InputControlHandler.WITH_NO_LABEL;


/**
 * GenericInputControlLogic
 *
 * @author akasych
 * @version $Id: GenericInputControlLogic.java 23019 2012-04-05 20:18:46Z carbiv $
 *          Singleton spring service to evaluate sets of controls.
 */
@Service
public class GenericInputControlLogic<T extends InputControlsContainer> implements ControlLogic<T> {

    public static final String INPUT_CONTROL_CONFIGURATION_KEY_HANDLER = "handler";
    public static final String INPUT_CONTROL_CONFIGURATION_KEY_UI_TYPE = "uiType";

    private final static Log log = LogFactory.getLog(GenericInputControlLogic.class);

    @jakarta.annotation.Resource
    protected ControlLogicCacheManager controlLogicCacheManager;
    @jakarta.annotation.Resource
    protected CachedRepositoryService cachedRepositoryService;
    @jakarta.annotation.Resource
    protected CachedEngineService cachedEngineService;
    @jakarta.annotation.Resource
    protected Map<String, Map<String, Object>> inputControlTypeConfiguration;
    @jakarta.annotation.Resource
    protected FilterResolver filterResolver;
    @jakarta.annotation.Resource
    protected boolean allowExtraReportParameters = false;
    @jakarta.annotation.Resource
    protected ValueFormattingUtils valueFormattingUtils;

    /**
     * This is the MAIN public interface method for getting controls structure in JasperServer CE
     */
    @WithSpan
    public final List<ReportInputControl> getInputControlsStructure(T container, Set<String> inputControlIds) throws CascadeResourceNotFoundException {
        Optional.ofNullable(container).ifPresent(c -> Span.current().setAttribute("resourceUri", c.getURIString()));
        ReportInputControlsInformation reportInputControlsInformation = getReportInputControlsInformation(container);
        final List<InputControl> allInputControls = getAllInputControls(container);
        List<InputControl> localizedInputControls = localizeInputControls(allInputControls, reportInputControlsInformation);
        final ResourceReference dataSource = getMainDataSource(container);
        return getInputControlsStructure(localizedInputControls, inputControlIds, dataSource);
    }

    /**
     * Replace labels and values of input controls in given list with localized
     *
     * @param inputControls - list of input controls
     * @param infos         - report information about input controls
     * @return list of localized  controls
     */

    protected List<InputControl> localizeInputControls(List<InputControl> inputControls, ReportInputControlsInformation infos) {
        for (InputControl inputControl : inputControls) {
            ReportInputControlInformation controlInfo = infos.getInputControlInformation(inputControl.getName());
            if (controlInfo != null) // in cases where there is IC that does not have a matching IC in the jrxml
            {
                //Technically inputControl already contains correct label and description
                //but they could be not i18n-ized.
                //since we need to use Report bundles for this it should be done during
                //creation of ReportInputControlInformation object.
                //currently the flow is the following:
                //inputControl.label -> ReportInputControlInformation.label -> i18n -> inputControl.label
                //latest step is done right here.
                inputControl.setLabel(controlInfo.getPromptLabel());
                inputControl.setDescription(controlInfo.getDescription());

                ReportInputControlValuesInformation controlValueInfos = controlInfo.getReportInputControlValuesInformation();

                if (controlValueInfos != null && CollectionUtils.isNotEmpty(controlValueInfos.getControlValuesNames())) {
                    //create new list of values with localized labels to replace existing
                    ListOfValues localizedListOfValues = new ListOfValuesImpl();
                    Set<String> controlValuesNames = controlValueInfos.getControlValuesNames();

                    for (String controlValueName : controlValuesNames) {
                        ReportInputControlValueInformation inputControlValueInformation =
                                controlValueInfos.getInputControlValueInformation(controlValueName);
                        //create same list value item but with localized label
                        ListOfValuesItem valuesItem = new ListOfValuesItemImpl();
                        valuesItem.setLabel(inputControlValueInformation.getPromptLabel());
                        valuesItem.setValue(inputControlValueInformation.getDefaultValue());
                        localizedListOfValues.addValue(valuesItem);
                    }
                    inputControl.setListOfValues(localizedListOfValues);
                }
            }
        }
        return inputControls;
    }

    // the final method for resolving controls structure
    protected List<ReportInputControl> getInputControlsStructure(List<InputControl> inputControls, Set<String> inputControlIds, ResourceReference dataSource) throws CascadeResourceNotFoundException {

        List<ReportInputControl> result = new ArrayList<ReportInputControl>();
        Map<String, ReportInputControl> controlsMap = new HashMap<String, ReportInputControl>();

        for (InputControl inputControl : inputControls) {
            if (inputControlIds == null || inputControlIds.isEmpty() || inputControlIds.contains(inputControl.getName())) {

                final Map<String, Object> typeConfiguration = inputControlTypeConfiguration.get(String.valueOf(inputControl.getInputControlType()));
                final InputControlHandler handler = getHandlerForInputControl(inputControl, typeConfiguration);

                String uiType = (String) typeConfiguration.get(INPUT_CONTROL_CONFIGURATION_KEY_UI_TYPE);

                ReportInputControl control = handler.buildReportInputControl(inputControl, uiType, dataSource);

                result.add(control);
                controlsMap.put(control.getId(), control);
            }
        }
        // here we resolve the slave dependencies for all controls
        resolveDependencies(controlsMap);

        return result;
    }

    private void resolveDependencies(Map<String, ReportInputControl> controlsMap) {
        for (ReportInputControl control : controlsMap.values()) {
            List<String> currentDependencies = control.getMasterDependencies();
            List<String> masterDependenciesToRemove = new ArrayList<String>();
            if (!currentDependencies.isEmpty()) {
                for (String parameter : currentDependencies) {
                    ReportInputControl masterControl = controlsMap.get(parameter);
                    if (masterControl != null) {
                        masterControl.getSlaveDependencies().add(control.getId());
                    } else {
                        masterDependenciesToRemove.add(parameter);
                    }
                }
                if(!masterDependenciesToRemove.isEmpty()){
                    currentDependencies.removeAll(masterDependenciesToRemove);
                }
            }
        }
    }

    protected ReportInputControlsInformation getReportInputControlsInformation(T container) throws CascadeResourceNotFoundException {
        Map<String, Object> initialParameters = getDefaultValues(container);
        ExecutionContext originalContext = Optional
                .ofNullable(ExecutionContextImpl.getThreadLocal().get())
                .orElseGet(() -> ExecutionContextImpl.create(LocaleContextHolder.getLocale(), TimeZoneContextHolder.getTimeZone()));
        ExecutionContext context = ExecutionContextImpl.getRuntimeExecutionContext(originalContext);
        return cachedEngineService.getReportInputControlsInformation(context, container, initialParameters);
    }

    protected Map<String, Object> getDefaultValues(T container) throws CascadeResourceNotFoundException {
        return new HashMap<String, Object>();
    }

    protected ResourceReference getMainDataSource(T container) throws CascadeResourceNotFoundException {
        return container.getDataSource();
    }

    protected List<InputControl> getAllInputControls(T container) throws CascadeResourceNotFoundException {
        return cachedEngineService.getInputControls(container);
    }

    // the final method for resolving values. All execution parameters should be already cast and validated. Default values are only used to set the selection for query controls
    protected List<InputControlState> getValuesForInputControls(
            T container, List<InputControl> controls, ResourceReference dataSource, Map<String, Object> executionParameters,
            Map<String, Class<?>> parameterTypes, ReportInputControlsInformation infos) throws CascadeResourceNotFoundException {

        List<InputControlState> values = new ArrayList<InputControlState>();
        for (InputControl inputControl : controls) {
            InputControlHandler icHandler = getHandlerForInputControl(inputControl);
            ReportInputControlInformation info = infos.getInputControlInformation(inputControl.getName());
            final InputControlState value = icHandler.getState(inputControl, dataSource, executionParameters, parameterTypes, info);
            if (value != null) {
                values.add(value);
            }
        }
        return values;
    }

    protected DataType getDataType(InputControl inputControl) throws CascadeResourceNotFoundException {
        return (inputControl != null && inputControl.getDataType() != null)
                ? cachedRepositoryService.getResource(DataType.class, inputControl.getDataType()) : null;
    }

    public SelectedValuesListWrapper getSelectedValues(String containerUri, Map<String, String[]> requestParameters) throws CascadeResourceNotFoundException {
       return getSelectedValues(getContainer(containerUri), requestParameters);
    }

    public SelectedValuesListWrapper getSelectedValues(final T container, Map<String, String[]> requestParameters) throws CascadeResourceNotFoundException {
        StateParameters stateParameters = new StateParameters(container, null, requestParameters).invoke();

        List<Map<String,List<InputControlOption>>> values = getSelectedValuesFromInputControls(container, stateParameters.getControls(),
                stateParameters.getDataSource(), stateParameters.getTypedParameter(), stateParameters.getParameterTypes(), stateParameters.getInfos());

        return getSelectedValuesListWrapper(values);
    }

    protected List<Map<String,List<InputControlOption>>> getSelectedValuesFromInputControls(T container, List<InputControl> controls, ResourceReference dataSource, Map<String, Object> executionParameters,
            Map<String, Class<?>> parameterTypes, ReportInputControlsInformation infos) throws CascadeResourceNotFoundException {

        List<Map<String,List<InputControlOption>>> values = new ArrayList<>();
        for (InputControl inputControl : controls) {
            InputControlHandler icHandler = getHandlerForInputControl(inputControl);
            ReportInputControlInformation info = infos.getInputControlInformation(inputControl.getName());

            final Map<String,List<InputControlOption>> value = icHandler.filterSelectedValues(inputControl, dataSource, executionParameters, parameterTypes, info);
            if (value != null) {
                values.add(value);
            }
        }
        return values;
    }

    protected SelectedValuesListWrapper getSelectedValuesListWrapper(List<Map<String,List<InputControlOption>>> values) {
        SelectedValuesListWrapper selectedValuesListWrapper = null;
        List<SelectedValue> SelectedValueList = new ArrayList<>();
        SelectedValue selectedValue;
        for (Map<String,List<InputControlOption>> value: values) {
            for(Map.Entry<String,List<InputControlOption>> defaultValueMap: value.entrySet()) {
                selectedValue = createSelectedValueInstance();
                selectedValue.setId(defaultValueMap.getKey());
                selectedValue.setOptions(defaultValueMap.getValue());
                SelectedValueList.add(selectedValue);
            }
        }

        if(!SelectedValueList.isEmpty()) {
            selectedValuesListWrapper = createSelectedValuesListWrapper();
            selectedValuesListWrapper.setSelectedValues(SelectedValueList);
        }

        return selectedValuesListWrapper;
    }

    private SelectedValuesListWrapper createSelectedValuesListWrapper() {
        return new SelectedValuesListWrapper();
    }

    /**
     * This is the MAIN public interface method for getting values in JasperServer CE
     * Resolves the Input Controls values based on given parameters
     */
    @WithSpan
    @Override
    public final List<InputControlState> getValuesForInputControlsFromRawData(final T container, Set<String> inputControlIds, Map<String, String[]> requestParameters)
            throws CascadeResourceNotFoundException {
        Optional.ofNullable(container).ifPresent(c -> Span.current().setAttribute("resourceUri", c.getURIString()));
        StateParameters stateParameters = new StateParameters(container, inputControlIds, requestParameters).invoke();

        List<InputControlState> states = getValuesForInputControls(container, stateParameters.getControls(),
                stateParameters.getDataSource(), stateParameters.getTypedParameter(), stateParameters.getParameterTypes(), stateParameters.getInfos());
        addValidationErrorsToInputControlStates(states, stateParameters.getValidationErrors(), stateParameters.getAllControls());

        return states;
    }

    private void ensureQueryParametersInfos(List<InputControl> allControls, ReportInputControlsInformation infos) throws CascadeResourceNotFoundException {
        for (InputControl control : allControls) {
            if (control.getQuery() != null) {
                Query query = cachedRepositoryService.getResource(Query.class, control.getQuery());

                if (query.getParameters() == null) {
                    ReportInputControlInformation information = infos.getInputControlInformation(control.getName());
                    Set<String> queryParams = getHandlerForQueryControl(information).getMasterDependencies(control, query.getDataSource());

                    if (queryParams != null) {
                        List<QueryParameterDescriptor> descriptors = new ArrayList<QueryParameterDescriptor>();
                        for (String param : queryParams) {
                            QueryParameterDescriptor descriptor = new QueryParameterDescriptor();
                            ReportInputControlInformation paramInformation = infos.getInputControlInformation(param);

                            if (paramInformation == null) {
                                throw new JSException("Query parameters not initialized yet");
                            }

                            if (Collection.class.isAssignableFrom(paramInformation.getValueType())) {
                                descriptor.setCollection(true);
                                if (paramInformation.getNestedType() != null) {
                                    descriptor.setParameterType(paramInformation.getNestedType().getName());
                                }
                            } else {
                                descriptor.setParameterType(paramInformation.getValueType().getName());
                            }
                            descriptor.setParameterName(paramInformation.getParameterName());

                            descriptors.add(descriptor);
                        }

                        query.setParameters(descriptors);
                        updateQuery(query);
                    }
                }
            }
        }
    }

    private void updateQuery(Query query){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        try{
            // Authentication override will be bound for current thread only, for others threads existing authorities will be used.
            Collection<? extends GrantedAuthority> basicAuthority = authentication.getAuthorities();

            List<GrantedAuthority> newAut = new ArrayList<GrantedAuthority>(basicAuthority);
            newAut.add(new SimpleGrantedAuthority(ObjectPermissionService.PRIVILEGED_OPERATION));

            Authentication newAuth = new InternalAuthenticationTokenImpl(authentication.getPrincipal(),
                    authentication.getCredentials(), newAut);

            SecurityContextHolder.getContext().setAuthentication(newAuth);

            ImportRunMonitor.start();
            cachedRepositoryService.updateResource(query);
        } finally {
            ImportRunMonitor.stop();
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
    }

    protected InputControlHandler getHandlerForQueryControl(ReportInputControlInformation information) throws CascadeResourceNotFoundException {
        InputControl dummyControl = new InputControlImpl();
        if (information.getValueType() != null && Collection.class.isAssignableFrom(information.getValueType())) {
            dummyControl.setInputControlType(InputControl.TYPE_MULTI_SELECT_QUERY);
        } else {
            dummyControl.setInputControlType(InputControl.TYPE_SINGLE_SELECT_QUERY);
        }

        return getHandlerForInputControl(dummyControl);
    }

    private void addValidationErrorsToInputControlStates(List<InputControlState> states, ValidationErrors validationErrors, List<InputControl> allControls) {
        if (!validationErrors.getErrors().isEmpty()) {
            for (Object errorObject : validationErrors.getErrors()) {
                InputControlValidationError error = (InputControlValidationError) errorObject;
                boolean stateFound = false;
                for (InputControlState state : states) {
                    if (state.getUri().equals(error.getInputControlUri())) {
                        state.setError(error.getDefaultMessage());
                        state.setValue(error.getInvalidValue());
                        stateFound = true;
                        break;
                    }
                }

                //TODO looks like this code is redundant since we don't refresh validation message when cascade works (only when Apply/Ok is clicked).
                //there is a validation exception for control which state is not present in states list.
                //we need to add new {@link InputControlState} in order to pass validation exception to client
                if (!stateFound) {
                    for (InputControl control : allControls) {
                        if (control.getURIString().equals(error.getInputControlUri())) {
                            InputControlState state = new InputControlState();

                            state.setUri(control.getURIString());
                            state.setId(control.getName());
                            state.setError(error.getDefaultMessage());
                            state.setValue(error.getInvalidValue());

                            states.add(state);
                            break;
                        }
                    }
                }
            }
        }
    }

    protected List<InputControl> filterSelectedInputControls(Set<String> inputControlIds, List<InputControl> allControls) {
        List<InputControl> selectedControls = new ArrayList<InputControl>();
        if (inputControlIds != null && !inputControlIds.isEmpty()) {
            for (InputControl control : allControls) {
                if (inputControlIds.contains(control.getName())) {
                    selectedControls.add(control);
                }
            }
        }
        else {
            selectedControls.addAll(allControls);
        }
        return selectedControls;
    }

    protected void resolveCascadingOrder(List<InputControl> inputControls, ResourceReference dataSource) throws CascadeResourceNotFoundException {
        Map<String, InputControl> inputControlIndexMap = new LinkedHashMap<String, InputControl>();
        Map<String, Set<String>> masterDependencies = new LinkedHashMap<String, Set<String>>();
        for (InputControl inputControl : inputControls) {
            inputControlIndexMap.put(inputControl.getName(), inputControl);
        }
        for (Map.Entry<String, InputControl> entry : inputControlIndexMap.entrySet()) {
            masterDependencies.put(entry.getKey(), getCheckedMastedDependenciesForInputControl(entry.getValue(), inputControlIndexMap.keySet(), dataSource));
        }

        LinkedHashSet<String> orderedInputControlNames = filterResolver.resolveCascadingOrder(masterDependencies);

        List<InputControl> tempOrderedList = new ArrayList<InputControl>();
        for (String inputControlName : orderedInputControlNames) {
            tempOrderedList.add(inputControlIndexMap.get(inputControlName));
        }

        inputControls.clear();
        inputControls.addAll(tempOrderedList);
    }

    private Set<String> getCheckedMastedDependenciesForInputControl(InputControl inputControl, Set<String> allInputControlNames, ResourceReference dataSource) throws CascadeResourceNotFoundException {
        InputControlHandler icHandler = getHandlerForInputControl(inputControl);
        Set<String> rawMasterDependencies = icHandler.getMasterDependencies(inputControl, dataSource);
        Set<String> checkedMasterDependencies = new HashSet<String>();

        if (rawMasterDependencies != null && !rawMasterDependencies.isEmpty()) {
            for (String masterId : rawMasterDependencies) {
                if (allInputControlNames.contains(masterId)) {
                    checkedMasterDependencies.add(masterId);
                }
            }
        }
        return checkedMasterDependencies;
    }

    @WithSpan
    @Override
    public final Map<String, Object> getTypedParameters(
            T container, Map<String, String[]> requestParameters, boolean skipValidation)
                throws CascadeResourceNotFoundException, InputControlsValidationException {
        Optional.ofNullable(container).ifPresent(c -> Span.current().setAttribute("resourceUri", c.getURIString()));

        List<InputControl> controls = getAllInputControls(container);
        ReportInputControlsInformation infos = getReportInputControlsInformation(container);
        ValidationErrors validationErrors = new ValidationErrorsImpl();
        Map<String, Object> typedParameters = getTypedParameters(controls, requestParameters, infos, validationErrors);

        for (InputControl control: controls) {
            if (!typedParameters.containsKey(control.getName())) {
                restoreAbsentParameter(typedParameters, control.getName());
            }
        }

        if (!validationErrors.getErrors().isEmpty() && !skipValidation) {
            throw new InputControlsValidationException(validationErrors);
        }

        return typedParameters;
    }

    protected void restoreAbsentParameter(Map<String, Object> typedParameters, String absentControlName) {
        typedParameters.put(absentControlName, null);
    }

    @WithSpan
    @Override
    public Map<String, String[]> formatTypedParameters(T container, Map<String, Object> typedParameters) throws CascadeResourceNotFoundException {
        Optional.ofNullable(container).ifPresent(c -> Span.current().setAttribute("resourceUri", c.getURIString()));
        List<InputControl> controls = getAllInputControls(container);
        ReportInputControlsInformation infos = getReportInputControlsInformation(container);
        Map<String, String[]> formattedValues = new HashMap<String, String[]>(controls.size());
        // fix for JS-34780
        if (allowExtraReportParameters) {
            // add all parameters as strings (even unknown), and modify values if needed based on IC logic in next block
            formattedValues = valueFormattingUtils.formatTypedParameters(typedParameters);
        }

        for (InputControl control : controls) {
            InputControlHandler icHandler = getHandlerForInputControl(control);
            final String controlName = control.getName();
            formattedValues.put(
                    controlName,
                    icHandler.formatValue(typedParameters.get(controlName), control, infos.getInputControlInformation(controlName))
            );
        }
        return formattedValues;
    }

    /**
     * Get map populated with parameter values which are merged from values sent through request and default values
     * and convert it to proper type.
     *
     * @param inputControls     - list of all input controls of current input controls container
     * @param requestParameters - input controls raw values
     * @param infos             - report input controls information
     * @param validationErrors  - instance of {@link ValidationErrors} where all validation errors will be collected
     * @return map of typed parameters converted from raw data
     * @throws CascadeResourceNotFoundException
     *          if any resource required for parameters conversion isn't found
     */
    protected Map<String, Object> getTypedParameters(
            List<InputControl> inputControls, Map<String, String[]> requestParameters,
            ReportInputControlsInformation infos, ValidationErrors validationErrors)
            throws CascadeResourceNotFoundException {

        //Need to preserve order in result map
        Map<String, Object> result = new LinkedHashMap<>();
        // fix for JS-34780
        if (allowExtraReportParameters) {
            // add all parameters (even unknown), and correct / remove them later on
            result.putAll(requestParameters);
        }
        if (inputControls != null) {
            if (requestParameters == null) requestParameters = Collections.emptyMap();
            for (InputControl inputControl : inputControls) {
                final String inputControlName = inputControl.getName();
                String[] valuesFromRequest = requestParameters.get(inputControlName);
                InputControlHandler icHandler = getHandlerForInputControl(inputControl);
                if (icHandler.isNothingSelected(valuesFromRequest)) {
                    icHandler.applyNothingSelected(inputControlName, result);
                } else {
                    try {
                        result.put(inputControlName, icHandler.convertParameterValueFromRawData(
                                valuesFromRequest, inputControl, infos.getInputControlInformation(inputControlName)));
                    } catch (InputControlValidationException e) {
                        InputControlValidationError error = e.getValidationError();
                        // Process error only in case if invalid value is present, which is true only for single value control
                        if (error.getInvalidValue() != null) {
                            error.setInputControlUri(inputControl.getURIString());
                            validationErrors.add(error);
                        }

                        // Error results in state when nothing is selected in control
                        icHandler.applyNothingSelected(inputControlName, result);
                    }
                }

                // Add parameters such as limit, offset, criteria and select
                setInputControlParameters(requestParameters, result, inputControlName);
            }
            setAdditionalParameters(requestParameters, result);
        }
        return result;
    }

    private void setAdditionalParameters(Map<String, String[]> requestParameters, Map<String, Object> result) {
        for (String parameter : ValuesLoader.REQUIRED_IC_PARAMETERS) {
            if (requestParameters.containsKey(parameter)) {
                result.put(parameter, requestParameters.get(parameter)[0]);
            }
        }
    }

    private void setInputControlParameters(Map<String, String[]> requestParameters, Map<String, Object> result, String inputControlName) {
        if(requestParameters.get(inputControlName+"_limit") != null) {
            result.put(inputControlName+"_limit", requestParameters.get(inputControlName+"_limit")[0]);
        }
        if(requestParameters.get(inputControlName+"_select") != null) {
            result.put(inputControlName+"_select", requestParameters.get(inputControlName+"_select")[0]);
        }
        if(requestParameters.get(inputControlName+"_offset") != null) {
            result.put(inputControlName+"_offset", requestParameters.get(inputControlName+"_offset")[0]);
        }
        if(requestParameters.get(inputControlName+"_criteria") != null) {
            result.put(inputControlName+"_criteria", requestParameters.get(inputControlName+"_criteria")[0]);
        }
    }

    protected InputControlHandler getHandlerForInputControl(InputControl inputControl) {
        final Map<String, Object> typeConfiguration = inputControlTypeConfiguration.get(String.valueOf(inputControl.getInputControlType()));
        return getHandlerForInputControl(inputControl, typeConfiguration);
    }

    // if value in the map isn't of type InputControlHandler, then exception is thrown before of cast. Therefore cast is safe
    @SuppressWarnings("unchecked")
    protected InputControlHandler getHandlerForInputControl(InputControl inputControl, Map<String, Object> typeConfiguration) {
        InputControlHandler result = null;
        if (inputControl != null) {
            if (typeConfiguration == null) {
                throw new IllegalArgumentException("Input control type '" + inputControl.getInputControlType() + "' isn't configured");
            } else if (typeConfiguration.get(INPUT_CONTROL_CONFIGURATION_KEY_HANDLER) == null) {
                throw new IllegalStateException("Handler for input control type '" + inputControl.getInputControlType() + "' isn't configured");
            } else if (!(typeConfiguration.get(INPUT_CONTROL_CONFIGURATION_KEY_HANDLER) instanceof InputControlHandler)) {
                throw new IllegalStateException("Handler for input control type '" + inputControl.getInputControlType() + "' must be of type " + InputControlHandler.class.getName());
            }
            result = (InputControlHandler) typeConfiguration.get(INPUT_CONTROL_CONFIGURATION_KEY_HANDLER);
        }
        return result;
    }

    protected T getContainer(String containerUri) throws CascadeResourceNotFoundException {
        return (T) cachedRepositoryService.getResource(InputControlsContainer.class, containerUri);
    }

    @Override
    public List<ReportInputControl> getInputControlsStructure(String containerUri, Set<String> inputControlIds) throws CascadeResourceNotFoundException {
        return getInputControlsStructure(getContainer(containerUri), inputControlIds);
    }

    @Override
    public List<InputControlState> getValuesForInputControlsFromRawData(
            String containerUri, Set<String> inputControlIds, Map<String, String[]> requestParameters)
            throws CascadeResourceNotFoundException {
        return getValuesForInputControlsFromRawData(getContainer(containerUri), inputControlIds, requestParameters);
    }

    @Override
    public Map<String, Object> getTypedParameters(
            String containerUri, Map<String, String[]> requestParameters, boolean skipValidation)
                throws CascadeResourceNotFoundException, InputControlsValidationException {

        return getTypedParameters(getContainer(containerUri), requestParameters, skipValidation);
    }

    @Override
    public Map<String, String[]> formatTypedParameters(String containerUri, Map<String, Object> typedParameters) throws CascadeResourceNotFoundException {
        return formatTypedParameters(getContainer(containerUri), typedParameters);
    }

    private SelectedValue createSelectedValueInstance() {
        return new SelectedValue();
    }

    private class StateParameters {
        private final T container;
        private final Set<String> inputControlIds;
        private Map<String, String[]> requestParameters;
        private List<InputControl> allControls;
        private ResourceReference dataSource;
        private ReportInputControlsInformation infos;
        private ValidationErrors validationErrors;
        private Map<String, Object> typedParameter;
        private Map<String, Class<?>> parameterTypes;
        private List<InputControl> controls;

        public StateParameters(T container, Set<String> inputControlIds, Map<String, String[]> requestParameters) {
            this.container = container;
            this.inputControlIds = inputControlIds;
            this.requestParameters = requestParameters;
        }

        public List<InputControl> getAllControls() {
            return allControls;
        }

        public ResourceReference getDataSource() {
            return dataSource;
        }

        public ReportInputControlsInformation getInfos() {
            return infos;
        }

        public ValidationErrors getValidationErrors() {
            return validationErrors;
        }

        public Map<String, Object> getTypedParameter() {
            return typedParameter;
        }

        public Map<String, Class<?>> getParameterTypes() {
            return parameterTypes;
        }

        public List<InputControl> getControls() {
            return controls;
        }

        public StateParameters invoke() throws CascadeResourceNotFoundException {
            if (requestParameters == null) {
                requestParameters = Collections.emptyMap();
            }

            if (requestParameters.containsKey(SKIP_PROFILE_ATTRIBUTES_RESOLVING)) {
                List<?> attributes = new ArrayList<Object>() {{
                    if (container.getAttributes() != null) addAll(container.getAttributes());
                    add(SKIP_PROFILE_ATTRIBUTES_RESOLVING);
                }};
                container.setAttributes(attributes);
            }

            // resolve all input controls
            allControls = getAllInputControls(container);
            dataSource = getMainDataSource(container);
            resolveCascadingOrder(allControls, dataSource);

            // cast the request parameters to required types and merge them with default values
            infos = getReportInputControlsInformation(container);
            validationErrors = new ValidationErrorsImpl();
            typedParameter = getTypedParameters(allControls, requestParameters, infos, validationErrors);

            parameterTypes = new HashMap<String, Class<?>>(allControls.size());
            for (InputControl inputControl : allControls) {
                DataType dataType = getDataType(inputControl);
                parameterTypes.put(inputControl.getName(), InputControlValueClassResolver.getValueClass(dataType, infos.getInputControlInformation(inputControl.getName()), false));
            }

            if (requestParameters.containsKey(EhcacheEngineService.IC_REFRESH_KEY)) {
                typedParameter.put(EhcacheEngineService.IC_REFRESH_KEY,"true");
            }


            if (requestParameters.containsKey(WITH_LABEL) && requestParameters.get(WITH_LABEL)[0].equals("false")) {
                typedParameter.put(WITH_NO_LABEL,"true");
            }

            Boolean isDiagnostic = infos.getDiagnosticProperty();
            if (isDiagnostic) {
                typedParameter.put(
                        DiagnosticSnapshotPropertyHelper.ATTRIBUTE_IS_DIAG_SNAPSHOT,
                        String.valueOf(isDiagnostic));
            }

            controls = filterSelectedInputControls(inputControlIds, allControls);
            controls = FluentIterable.from(controls).filter(RefSets.RESOLVED_PREDICATE).toList();
            return this;
        }
    }
}

