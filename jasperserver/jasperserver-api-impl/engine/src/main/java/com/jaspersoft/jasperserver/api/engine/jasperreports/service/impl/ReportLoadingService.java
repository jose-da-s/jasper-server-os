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
package com.jaspersoft.jasperserver.api.engine.jasperreports.service.impl;


import com.jaspersoft.jasperserver.api.JSExceptionWrapper;
import com.jaspersoft.jasperserver.api.common.domain.ExecutionContext;
import com.jaspersoft.jasperserver.api.common.domain.impl.ExecutionContextImpl;
import com.jaspersoft.jasperserver.api.engine.common.service.BuiltInParameterProvider;
import com.jaspersoft.jasperserver.api.metadata.common.domain.*;
import com.jaspersoft.jasperserver.api.metadata.common.domain.util.MarkAllInputControlsResolved;
import com.jaspersoft.jasperserver.api.metadata.common.domain.util.SrcSets;
import com.jaspersoft.jasperserver.api.metadata.common.service.RepositoryCache;
import com.jaspersoft.jasperserver.api.metadata.common.service.RepositoryCacheableItem;
import com.jaspersoft.jasperserver.api.metadata.common.service.RepositoryService;
import com.jaspersoft.jasperserver.api.metadata.common.service.impl.hibernate.util.RepositoryUtils;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.extension.annotations.WithSpan;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.util.JRLoader;
import com.jaspersoft.jasperserver.api.engine.jasperreports.util.CustomJRXmlLoader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.InputStream;
import java.io.InvalidClassException;
import java.util.*;
import java.util.function.Consumer;

public class ReportLoadingService {
    private static final Log log = LogFactory.getLog(ReportLoadingService.class);

    protected RepositoryService repository;
    protected RepositoryCache compiledReportsCache;
    protected RepositoryCacheableItem cacheableCompiledReports;
    protected RepositoryContextManager repositoryContextManager;
    protected List builtInParameterProviders = new ArrayList();
    protected String reportParameterLabelKeyPrefix;

    public void setRepositoryService(RepositoryService repository) {
        this.repository = repository;
    }

    public void setCompiledReportsCache(RepositoryCache compiledReportsCache) {
        this.compiledReportsCache = compiledReportsCache;
    }

    public void setCacheableCompiledReports(RepositoryCacheableItem cacheableCompiledReports) {
        this.cacheableCompiledReports = cacheableCompiledReports;
    }

    public void setRepositoryContextManager(
            RepositoryContextManager repositoryContextManager) {
        this.repositoryContextManager = repositoryContextManager;
    }

    public void setBuiltInParameterProviders(List builtInParameterProviders) {
        this.builtInParameterProviders = builtInParameterProviders;
    }

    public void setReportParameterLabelKeyPrefix(
            String reportParameterLabelKeyPrefix) {
        this.reportParameterLabelKeyPrefix = reportParameterLabelKeyPrefix;
    }

    public RepositoryService getRepositoryService() {
        return repository;
    }

    public List getBuiltInParameterProviders() {
        return builtInParameterProviders;
    }

    public String getReportParameterLabelKeyPrefix() {
        return reportParameterLabelKeyPrefix;
    }


    public JasperReport getJasperReport(ExecutionContext context, FileResource reportRes, boolean inMemoryUnit) {
        JasperReport report;
        try {
            if (inMemoryUnit) {
                InputStream fileResourceData = getFileResourceDataStream(context, reportRes);
                report = compileReport(fileResourceData);
            } else {
                //TODO use an in-memory cache of compiled reports
                InputStream compiledReport = getCompiledReport(context, reportRes);
                try {
                    report = (JasperReport) JRLoader.loadObject(compiledReport);
                } catch (JRException e) {
                    Throwable cause = e.getCause();
                    if (cause == null || !(cause instanceof InvalidClassException)) {
                        throw e;
                    }

                    if (log.isInfoEnabled()) {
                        log.info("InvalidClassException caught while loading compiled report, clearing the compiled report cache");
                    }
                    clearCompiledReportCache();

                    //recompiling the report
                    compiledReport = getCompiledReport(context, reportRes);
                    report = (JasperReport) JRLoader.loadObject(compiledReport);
                }
            }
            return report;
        } catch (JRException e) {
            log.error(e, e);
            throw new JSExceptionWrapper(e);
        }
    }

    public InputStream getFileResourceDataStream(ExecutionContext context, FileResource fileResource) {
        InputStream data;
        if (fileResource.hasData()) {
            data = fileResource.getDataStream();
        } else {
            FileResourceData resourceData = repository.getResourceData(context, fileResource.getURIString());
            data = resourceData.getDataStream();
        }
        return data;
    }

    private InputStream getCompiledReport(ExecutionContext context, FileResource jrxml) {
        return compiledReportsCache.cache(context, jrxml, cacheableCompiledReports);
    }

    private void clearCompiledReportCache() {
        compiledReportsCache.clearCache(cacheableCompiledReports);
    }

    public JasperReport compileReport(InputStream jrxmlData) {
        try {
            JasperDesign design = CustomJRXmlLoader.load(jrxmlData);
            JasperReport report = JasperCompileManager.compileReport(design);
            return report;
        } catch (JRException e) {
            log.error(e, e);
            throw new JSExceptionWrapper(e);
        }
    }

    public RepositoryContextHandle setThreadRepositoryContext(ExecutionContext context, ResourceContainer reportUnit, String reportUnitURI)
    {
        // resources loading in the repository context should only need execute permissions,
        // so make sure that the execution context has the magic attribute to signal that perms change
        context = EngineServiceImpl.getRuntimeExecutionContext(context);
        return repositoryContextManager.setRepositoryContext(context, reportUnitURI, reportUnit);
    }

    public void resetThreadRepositoryContext(RepositoryContextHandle repositoryContextHandle)
    {
    	if (repositoryContextHandle != null)
    	{
    		repositoryContextHandle.unset();
    	}
    }

    public Map getReportParameters(ExecutionContext context, JasperReport report,
                                      Map requestParameters) {
        Map reportParameters = new HashMap();

        if (context != null && context.getLocale() != null
                && reportParameters.get(JRParameter.REPORT_LOCALE) == null) {
            reportParameters.put(JRParameter.REPORT_LOCALE, context.getLocale());
        }

        if (context != null && context.getTimeZone() != null) {
            reportParameters.put(JRParameter.REPORT_TIME_ZONE, context.getTimeZone());
        }

        if (requestParameters != null) {
            reportParameters.putAll(requestParameters);
        }

        setBuiltinParameters(context, true, report.getParameters(),
                reportParameters, null);

        return reportParameters;
    }


    public void setBuiltinParameters(ExecutionContext context, boolean onlyExistingParameters, JRParameter[] existingJRParameters,
                                     Map parametersMap, List additionalParameters) {
        for (Object o : getBuiltInParameterProviders() ) {
            BuiltInParameterProvider pProvider = (BuiltInParameterProvider) o;

            // set standard parameters that will always be available

            List<Object[]> results = pProvider.getParameters(context, additionalParameters, parametersMap);

            for (Object[] aResult : results) {
                setBuiltInParameter(context, onlyExistingParameters, existingJRParameters, parametersMap, additionalParameters, aResult);
            }

            // add parameters that are built in and requested
            // This is only needed when we are running a report.
            // Parameters in stand alone queries (engineService.executeQuery) are managed by FilterCore (cascading engine)
            if (onlyExistingParameters) {
                for (JRParameter jrParameter : existingJRParameters) {
                    Object parameterMapValue = parametersMap.get(jrParameter.getName());
                    if (parameterMapValue == null) {
                        Object[] aResult = pProvider.getParameter(context, additionalParameters, parametersMap, jrParameter.getName());
                        if (aResult != null) {
                            setBuiltInParameter(context, onlyExistingParameters, existingJRParameters, parametersMap, additionalParameters, aResult);
                        }
                    }
                }
            }
        }
    }

    public void setBuiltInParameter(ExecutionContext context, boolean onlyExistingParameters, JRParameter[] existingJRParameters,
                                    Map parametersMap, List additionalParameters, Object[] aResult) {

        JRParameter jrParameter = (JRParameter) aResult[0];
        Object aValue = aResult[1];
        boolean set = false;

        Object parameterFromMap = parametersMap.get(jrParameter.getName());

        JRParameter existingJRParameter = null;

        if (onlyExistingParameters) {
            for (JRParameter existingJRParameterFromArray : existingJRParameters) {
                if (existingJRParameterFromArray.getName().equals(jrParameter.getName())) {
                    existingJRParameter = existingJRParameterFromArray;
                    break;
                }
            }
        }

        if (onlyExistingParameters) {
            if (existingJRParameter == null) {
                // don't add: leave set = false
            } else if (existingJRParameter.getDefaultValueExpression() != null) {
                //do not set if parameter has default value expression
                if (log.isDebugEnabled()) {
                    log.debug("Report parameter " + jrParameter.getName() + " has a default value expression, not setting value");
                }
            } else if (!existingJRParameter.getValueClass().isAssignableFrom(jrParameter.getValueClass())) {
                //do not set if parameter has type incompatible with parameter
                if (log.isDebugEnabled()) {
                    log.debug("Report parameter " + jrParameter.getName() + " type " + jrParameter.getValueClassName() + " not compatible with java.lang.String, not setting value");
                }
            } else {
                //set the value
                set = true;
            }
        } else if (parameterFromMap == null) {
            //set the value anyway, it will be accessible through the parametersMap map
            set = true;
        }

        if (set) {
            if (log.isDebugEnabled()) {
                log.debug("Setting report parameter " + jrParameter.getName() + " to " + aValue);
            }

            if (!onlyExistingParameters) {
                additionalParameters.add(jrParameter);
            }

            parametersMap.put(jrParameter.getName(), aValue);
        }

    }


//    public ResourceBundle loadResourceBundle(ExecutionContext context, JasperReport report) {
//        ResourceBundle bundle = null;
//        String baseName = report.getResourceBundle();
//        if (baseName != null) {
//            Locale locale = context == null ? null : context.getLocale();
//            if (locale == null) {
//                locale = Locale.getDefault();
//            }
//
//            bundle = JRResourcesUtil.loadResourceBundle(baseName, locale);
//        }
//        return bundle;
//    }

    public <T extends Resource> T getFinalResource(ExecutionContext context, T resource) {
        return resource;
    }

    public <T extends Resource> T getFinalResource(ExecutionContext context, ResourceReference res, Class<T> type) {
        T finalRes;
        if (res == null) {
            return null;
        }
        if (res.isLocal()) {
            finalRes = (T) res.getLocalResource();
        } else {
            finalRes = getRepositoryResource(context, res.getReferenceURI(), type);
        }
        return finalRes;
    }

    private <T extends Resource> T getRepositoryResource(ExecutionContext context, String uri, Class<T> type) {
        return (T) getRepositoryService().getResource(context, uri, type);
    }

    /**
     * Get list of resource references of input controls from specified container and it's data source,
     * input controls with the same names get replaced, ones from container have priority over ones from the data source.
     *
     * @param context ExecutionContext
     * @param container InputControlsContainer
     * @return List of ResourceReferences
     */
    public List<ResourceReference> getInputControlReferences(ExecutionContext context, InputControlsContainer container) {
        List<ResourceReference> inputControlRefs = new ArrayList<ResourceReference>();

        container = getFinalResource(context, container);

        if (container instanceof ReferenceNormalizer) {
            try {
                ((ReferenceNormalizer) container).checkReferences();
            } catch (Exception e) {
                // We are going to fix this error here
                ((ReferenceNormalizer) container).normalizeReferences(new MarkAllInputControlsResolved());
            }
        }

        // Get input control references of the container
        if (container.getInputControls() != null) {
            inputControlRefs.addAll(container.getInputControls());
        }

        // Get input control references of the report data source
        Resource dataSource = getFinalResource(ExecutionContextImpl.getRuntimeExecutionContext(context), container.getDataSource(), null);
        if (dataSource instanceof InputControlsContainer && ((InputControlsContainer) dataSource).getInputControls() != null) {

            if (container.getDataSource() != dataSource && dataSource instanceof ReferenceNormalizer) {
                try {
                    ((ReferenceNormalizer) dataSource).checkReferences();
                } catch (Exception e) {
                    // We are going to fix this error here
                    ((ReferenceNormalizer) container).normalizeReferences(new MarkAllInputControlsResolved());
                }
            }

            List<String> containerInputControlNames = new ArrayList<String>(inputControlRefs.size());
            for (ResourceReference ref : inputControlRefs) {
                containerInputControlNames.add(getInputControlParameterName(ref));
            }
            for (ResourceReference dataSourceControlRef : ((InputControlsContainer) dataSource).getInputControls()) {
                String dataSourceControlName = getInputControlParameterName(dataSourceControlRef);
                if (!containerInputControlNames.contains(dataSourceControlName)) {
                    inputControlRefs.add(dataSourceControlRef);
                }
            }
        }

        return inputControlRefs;
    }

    public boolean isDummyReport(ExecutionContext executionContext, ResourceReference reportUri){
        return false;
    }

    protected String getInputControlParameterName(ResourceReference inputControlRef) {
		// we only need the input control name
		String name = null;
		if (inputControlRef.isLocal()) {
			name = inputControlRef.getLocalResource().getName();
		} else {
			String referenceURI = inputControlRef.getReferenceURI();
			if (referenceURI != null) {
				name = RepositoryUtils.getName(referenceURI);
			}
		}
		return name;
	}

    protected List<InputControl> getInputControls(ExecutionContext context, InputControlsContainer container, Consumer<InputControl> process) {
        List<ResourceReference> inputControlRefs = getInputControlReferences(context, container);
        List<InputControl> inputControls = new ArrayList<InputControl>(inputControlRefs.size());
        for (ResourceReference ref : inputControlRefs) {
            final InputControl inputControl = getFinalResource(context, ref, InputControl.class);

            if (process != null && inputControl !=null) process.accept(inputControl);

            inputControls.add(inputControl);
        }
        return inputControls;
    }
    /**
     * Get list of input controls from specified container and it's data source,
     * input controls with the same names get replaced, ones from container have priority over ones from the data source.
     *
     * @param context ExecutionContext
     * @param container InputControlsContainer
     * @return List of InputControl
     */
    @WithSpan
    public List<InputControl> getInputControls(ExecutionContext context, InputControlsContainer container) {
        Optional.ofNullable(container).ifPresent(c -> Span.current().setAttribute("resourceUri", c.getURIString()));
        return getInputControls(context, container, (inputControl)->{
            if (SrcSets.hasUnresolved(inputControl.getSources())) {
                Query query = getFinalResource(context, inputControl.getQuery(), Query.class);
                resolveInputControlSources(context, inputControl, query, container);
            }
        });
    }

    /**
     * Resolving input control sources. Can be overridden in PRO java classes
     * @param context Execution Context
     * @param inputControl Input Control
     * @param query {@link Query} for query-based input control; {@code null} otherwise
     */
    protected void resolveInputControlSources(ExecutionContext context, InputControl inputControl, Query query, InputControlsContainer container) {
        if (query == null || !Query.DOMAIN_LANGUAGE.equals(query.getLanguage())) {
            SrcSets.resolveAll(inputControl.getSources());
        }
    }
}
