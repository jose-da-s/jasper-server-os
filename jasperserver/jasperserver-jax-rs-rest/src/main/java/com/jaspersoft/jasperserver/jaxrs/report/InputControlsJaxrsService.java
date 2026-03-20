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
package com.jaspersoft.jasperserver.jaxrs.report;

import com.jaspersoft.jasperserver.api.metadata.common.domain.Folder;
import com.jaspersoft.jasperserver.dto.reports.ReportParameters;
import com.jaspersoft.jasperserver.dto.reports.inputcontrols.InputControlState;
import com.jaspersoft.jasperserver.dto.reports.inputcontrols.ReportInputControl;
import com.jaspersoft.jasperserver.dto.reports.inputcontrols.ReportInputControlsListWrapper;
import com.jaspersoft.jasperserver.dto.reports.inputcontrols.SelectedValuesListWrapper;
import com.jaspersoft.jasperserver.remote.common.CallTemplate;
import com.jaspersoft.jasperserver.remote.common.RemoteServiceWrapper;
import com.jaspersoft.jasperserver.remote.exception.ModificationNotAllowedException;
import com.jaspersoft.jasperserver.api.ErrorDescriptorException;
import com.jaspersoft.jasperserver.remote.exception.ResourceNotFoundException;
import com.jaspersoft.jasperserver.dto.common.ErrorDescriptor;
import com.jaspersoft.jasperserver.inputcontrols.cascade.CascadeResourceNotFoundException;
import com.jaspersoft.jasperserver.inputcontrols.cascade.InputControlsLogicService;
import com.jaspersoft.jasperserver.inputcontrols.cascade.InputControlsValidationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Response;
import java.util.*;

import static com.jaspersoft.jasperserver.inputcontrols.cascade.handlers.InputControlHandler.WITH_LABEL;

/**
 * <p></p>
 *
 * @author Yaroslav.Kovalchyk
 * @version $Id: InputControlsJaxrsService.java 26672 2012-12-11 13:47:44Z ztomchenco $
 */
@Service
@Path("/reports/{reportUnitURI: .+}/inputControls")
@CallTemplate(ReportsServiceCallTemplate.class)
public class InputControlsJaxrsService extends RemoteServiceWrapper<InputControlsLogicService> {
    public static final String EXCLUDE_PARAMETER = "exclude";
    public static final String STATE_VALUE = "state";
    public static final String INCLUDE_TOTAL_COUNT = "includeTotalCount";
    public static final String FRESH_DATA = "freshData";

    public static final String SELECTED_ONLY_PARAMETER = "selectedOnly";
    // Using a different parameter name to avoid possible collision with Input Control having exactly the same name
    public static final String SELECTED_ONLY_INTERNAL = "&selectedOnly";

    @Resource(name = "inputControlsLogicService")
    public void setRemoteService(InputControlsLogicService remoteService) {
        this.remoteService = remoteService;
    }


    @GET
    @Path("/selectedValues")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getSelectedValues(@PathParam("reportUnitURI") final String reportUnitURI, @QueryParam("freshData") @DefaultValue("false") Boolean freshData, @QueryParam(WITH_LABEL) @DefaultValue("true") Boolean withLabel,
    		@Context final HttpServletRequest request) {
        return internalGetSelectedValues(reportUnitURI, freshData, withLabel);
    }

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getReportInputParameters(@PathParam("reportUnitURI") final String reportUnitURI, @Context final HttpServletRequest request) {
        // parameters map can be safely cast to Map<String, String[]>
        @SuppressWarnings("unchecked")
        final Map<String, String[]> rawParameters = request.getParameterMap();
        return internalGetReportInputParameters(reportUnitURI, null, rawParameters);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getReportInputParametersViaPost(@PathParam("reportUnitURI") String reportUnitURI, Map<String, String[]> jsonParameters) {
        return internalGetReportInputParameters(reportUnitURI, null, jsonParameters);
    }

    @POST
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    public Response getReportInputParametersViaPost(@PathParam("reportUnitURI") String reportUnitURI, ReportParameters parameters) {
        return internalGetReportInputParameters(reportUnitURI, null, parameters.getRawParameters());
    }

    protected Response internalGetSelectedValues(final String reportUnitUri, boolean freshData, boolean withLabel) {
        return callRemoteService(new ConcreteCaller<Response>() {
            @Override
            public Response call(InputControlsLogicService remoteService) throws ErrorDescriptorException {
                SelectedValuesListWrapper selectedValuesListWrapper;
                try {
                    selectedValuesListWrapper = remoteService.getInputControlsSelectedValues(Folder.SEPARATOR + reportUnitUri, freshData, withLabel);
                } catch (CascadeResourceNotFoundException e) {
                    throw new ResourceNotFoundException("URI:" + e.getResourceUri() + " Type:" + e.getResourceType());
                }
                if (selectedValuesListWrapper != null) {
                    return Response.ok(selectedValuesListWrapper).build();
                } else {
                    return Response.status(Response.Status.NO_CONTENT).build();
                }
            }
        });
    }

    protected Response internalGetReportInputParameters(final String reportUnitUri, final Set<String> inputControlIds, final Map<String, String[]> rawParameters) {
        return callRemoteService(new ConcreteCaller<Response>() {
            @Override
            public Response call(InputControlsLogicService remoteService) throws ErrorDescriptorException {
                List<ReportInputControl> inputControlsForReport;
                try {
                    if (rawParameters.containsKey(EXCLUDE_PARAMETER) && Arrays.asList(rawParameters.get(EXCLUDE_PARAMETER)).contains(STATE_VALUE)){
                        inputControlsForReport = remoteService.getInputControlsStructure(Folder.SEPARATOR + reportUnitUri, inputControlIds);
                    } else {
                        inputControlsForReport = remoteService.getInputControlsWithValues(Folder.SEPARATOR + reportUnitUri, inputControlIds, rawParameters);
                    }
                } catch (CascadeResourceNotFoundException e) {
                    throw new ResourceNotFoundException("URI:" + e.getResourceUri() + " Type:" + e.getResourceType());
                }
                if (inputControlsForReport != null && !inputControlsForReport.isEmpty())
                    return Response.ok(new ReportInputControlsListWrapper(inputControlsForReport)).build();
                else
                    return Response.status(Response.Status.NO_CONTENT).build();
            }
        });
    }

    @PUT
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response reorderInputControls(@PathParam("reportUnitURI") final String reportUnitURI, final ReportInputControlsListWrapper toUpdateWrapper) {
        return callRemoteService(new ConcreteCaller<Response>() {
            @Override
            public Response call(InputControlsLogicService remoteService) throws ErrorDescriptorException {
                final List<ReportInputControl> toUpdate = toUpdateWrapper.getInputParameters();
                List<String> newList = new ArrayList<String>(toUpdate.size());
                for (ReportInputControl control : toUpdate) {
                    newList.add(control.getId());
                }
                final List<ReportInputControl> updated;
                try {
                    updated = remoteService.reorderInputControls("/" + reportUnitURI, newList);
                } catch (CascadeResourceNotFoundException e) {
                    throw new ResourceNotFoundException("URI:" + e.getResourceUri() + " Type:" + e.getResourceType());
                } catch (AccessDeniedException spe) {
                    throw new ModificationNotAllowedException("");
                } catch (InputControlsValidationException e) {
                    throw new ModificationNotAllowedException(new ErrorDescriptor()
                            .setMessage("Malformed data")
                            .addParameters(e.getErrors()));
                }
                return Response.ok(new ReportInputControlsListWrapper(updated)).build();
            }
        });
    }

    @GET
    @Path("/{inputControlIds: [^;/]+(;[^;/]+)*}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getReportInputParametersForSpecifiedInputControls(
            @PathParam("reportUnitURI") String reportUnitURI,
            @PathParam("inputControlIds") PathSegment inputControlIds,
            @Context HttpServletRequest request) {
        // parameters map can be safely cast to Map<String, String[]>
        @SuppressWarnings("unchecked")
        final Map<String, String[]> parameterMap = request.getParameterMap();
        return internalGetReportInputParameters(reportUnitURI, getInputControlIdsFromPathSegment(inputControlIds), parameterMap);
    }

    @POST
    @Path("/{inputControlIds: [^;/]+(;[^;/]+)*}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getReportInputParametersForSpecifiedInputControlsViaPost(@PathParam("reportUnitURI") String reportUnitURI, @PathParam("inputControlIds") PathSegment inputControlIds, Map<String, String[]> jsonParameters) {
        return internalGetReportInputParameters(reportUnitURI, getInputControlIdsFromPathSegment(inputControlIds), jsonParameters);
    }

    @POST
    @Path("/{inputControlIds: [^;/]+(;[^;/]+)*}")
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    public Response getReportInputParametersForSpecifiedInputControlsViaPost(@PathParam("reportUnitURI") String reportUnitURI, @PathParam("inputControlIds") PathSegment inputControlIds, ReportParameters parameters) {
        return internalGetReportInputParameters(reportUnitURI, getInputControlIdsFromPathSegment(inputControlIds), parameters.getRawParameters());
    }

    @GET
    @Path("/values")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getInputControlsInitialValues(@PathParam("reportUnitURI") final String reportUnitURI,
            @Context final HttpServletRequest request, @QueryParam(FRESH_DATA) @DefaultValue("false") Boolean freshData) {
        // parameters map can be safely cast to Map<String, String[]>
        @SuppressWarnings("unchecked")
        final Map<String, String[]> parameterMap = request.getParameterMap();
        return internalGetInputControlsInitialValues(reportUnitURI, parameterMap, freshData);
    }

    @POST
    @Path("/values")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getInputControlsInitialValuesViaPost(@PathParam("reportUnitURI") String reportUnitURI,
                                                         Map<String, String[]> jsonParameters,
                                                         @QueryParam(FRESH_DATA) @DefaultValue("false") Boolean freshData,
                                                         @QueryParam(SELECTED_ONLY_PARAMETER) @DefaultValue("false") Boolean selectedOnly) {
        setSelectedOnlyParameter(jsonParameters, selectedOnly);
        return internalGetInputControlsInitialValues(reportUnitURI, jsonParameters, freshData);
    }

    @POST
    @Path("/values/pagination")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getInputControlsPaginationValues(@PathParam("reportUnitURI") String reportUnitURI,
                                                         ReportParameters parameters, @QueryParam(FRESH_DATA) @DefaultValue("false") Boolean freshData,
                                                     @QueryParam(INCLUDE_TOTAL_COUNT) @DefaultValue("true") Boolean includeTotalCount) {
        Map<String, String[]> parametersMap = getParametersWithTotalCount(parameters, includeTotalCount);
        return internalGetInputControlsInitialValues(reportUnitURI, parametersMap, freshData);
    }

    @POST
    @Path("/{inputControlIds: [^;/]+(;[^;/]+)*}/values/pagination")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getReportInputControlValuesViaPost(
            @PathParam("reportUnitURI") final String reportUnitUri,
            @PathParam("inputControlIds") final PathSegment inputControlIds,
            ReportParameters parameters,
            @QueryParam(FRESH_DATA) @DefaultValue("false") Boolean freshData,
            @QueryParam(INCLUDE_TOTAL_COUNT) @DefaultValue("true") Boolean includeTotalCount) {

        Map<String, String[]> parametersMap = getParametersWithTotalCount(parameters, includeTotalCount);
        return internalGetInputControlValues(Folder.SEPARATOR + reportUnitUri, inputControlIds, parametersMap, freshData);
    }

    @POST
    @Path("/values")
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    public Response getInputControlsInitialValuesViaPost(@PathParam("reportUnitURI") String reportUnitURI,
            ReportParameters parameters, @QueryParam(FRESH_DATA) @DefaultValue("false") Boolean freshData) {
        return internalGetInputControlsInitialValues(reportUnitURI, parameters.getRawParameters(), freshData);
    }

    protected Response internalGetInputControlsInitialValues(final String reportUnitURI, final Map<String, String[]> parameters, final boolean freshData) {
        return callRemoteService(new ConcreteCaller<Response>() {
            public Response call(InputControlsLogicService remoteService) throws ErrorDescriptorException {
                final String completeInputControlUri = Folder.SEPARATOR + reportUnitURI;
                List<InputControlState> values;
                try {
                    values = remoteService.getValuesForInputControls(completeInputControlUri, null, parameters, freshData);
                } catch (CascadeResourceNotFoundException e) {
                    throw new ResourceNotFoundException("URI:" + e.getResourceUri() + " Type:" + e.getResourceType());
                }
                if (values != null && !values.isEmpty())
                    return Response.ok(new InputControlStateListWrapper(values)).build();
                else
                    return Response.status(Response.Status.NO_CONTENT).entity("No input controls values found for the report /" + reportUnitURI).build();
            }
        });
    }

    @GET
    @Path("/{inputControlIds: [^;/]+(;[^;/]+)*}/values")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getReportInputControlValues(
            @PathParam("reportUnitURI") final String reportUnitUri,
            @PathParam("inputControlIds") final PathSegment inputControlIds,
            @Context final HttpServletRequest request,
            @QueryParam(FRESH_DATA) @DefaultValue("false") Boolean freshData) {
        // parameters map can be safely cast to Map<String, String[]>
        @SuppressWarnings("unchecked")
        final Map<String, String[]> parameterMap = request.getParameterMap();
        return internalGetInputControlValues(Folder.SEPARATOR + reportUnitUri, inputControlIds, parameterMap, freshData);
    }

    @POST
    @Path("/{inputControlIds: [^;/]+(;[^;/]+)*}/values")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getReportInputControlValuesViaPost(
            @PathParam("reportUnitURI") final String reportUnitUri,
            @PathParam("inputControlIds") final PathSegment inputControlIds,
            Map<String, String[]> jsonParameters,
            @QueryParam(FRESH_DATA) @DefaultValue("false") Boolean freshData,
            @QueryParam(SELECTED_ONLY_PARAMETER) @DefaultValue("false") Boolean selectedOnly) {
        setSelectedOnlyParameter(jsonParameters, selectedOnly);
        return internalGetInputControlValues(Folder.SEPARATOR + reportUnitUri, inputControlIds,
                jsonParameters, freshData);
    }

    @POST
    @Path("/{inputControlIds: [^;/]+(;[^;/]+)*}/values")
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    public Response getReportInputControlValuesViaPost(
            @PathParam("reportUnitURI") final String reportUnitUri,
            @PathParam("inputControlIds") final PathSegment inputControlIds,
            ReportParameters parameters,
            @QueryParam(FRESH_DATA) @DefaultValue("false") Boolean freshData) {
        return internalGetInputControlValues(Folder.SEPARATOR + reportUnitUri, inputControlIds,
                parameters.getRawParameters(), freshData);
    }

    protected Response internalGetInputControlValues(
            final String reportUnitUri,
            final PathSegment inputControlIdsSegment,
            final Map<String, String[]> parameters,
            final boolean freshData) {
        return callRemoteService(new ConcreteCaller<Response>() {
            public Response call(InputControlsLogicService remoteService) throws ErrorDescriptorException {
                List<InputControlState> values;
                try {
                    values = remoteService.getValuesForInputControls(reportUnitUri, getInputControlIdsFromPathSegment(inputControlIdsSegment), parameters, freshData);
                } catch (CascadeResourceNotFoundException e) {
                    throw new ResourceNotFoundException("URI:" + e.getResourceUri() + " Type:" + e.getResourceType());
                }
                if (values != null && !values.isEmpty())
                    return Response.ok(new InputControlStateListWrapper(values)).build();
                else
                    return Response.status(Response.Status.NO_CONTENT).entity("No input controls values found for the report /" + reportUnitUri).build();
            }
        });
    }

    protected Set<String> getInputControlIdsFromPathSegment(PathSegment inputControlIdsSegment) {
        Set<String> inputControlIds = new HashSet<String>();
        inputControlIds.add(inputControlIdsSegment.getPath());
        for (String currentId : inputControlIdsSegment.getMatrixParameters().keySet()) {
            inputControlIds.add(currentId);
        }
        return inputControlIds;
    }

    protected Map<String, String[]> getParametersWithTotalCount(ReportParameters parameters, @QueryParam(INCLUDE_TOTAL_COUNT) @DefaultValue("true") Boolean includeTotalCount) {
        Map<String, String[]> parametersMap = new HashMap<>();
        parametersMap.put(INCLUDE_TOTAL_COUNT, new String[] {includeTotalCount.toString()});

        if(parameters != null) {
            parametersMap.putAll(parameters.getRawParameters());
        }
        return parametersMap;
    }

    private void setSelectedOnlyParameter(Map<String, String[]> jsonParameters, boolean selectedOnly) {
        if (selectedOnly) {
            jsonParameters.put(SELECTED_ONLY_INTERNAL, new String[] { Boolean.TRUE.toString() });
        }
    }
}

