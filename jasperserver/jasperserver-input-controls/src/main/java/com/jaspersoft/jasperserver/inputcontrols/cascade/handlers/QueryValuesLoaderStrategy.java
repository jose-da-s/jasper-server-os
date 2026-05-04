package com.jaspersoft.jasperserver.inputcontrols.cascade.handlers;

import com.jaspersoft.jasperserver.api.JSException;
import com.jaspersoft.jasperserver.api.JSMissingDataSourceFieldsException;
import com.jaspersoft.jasperserver.api.common.domain.impl.ExecutionContextImpl;
import com.jaspersoft.jasperserver.api.engine.jasperreports.service.impl.EhcacheEngineService;
import com.jaspersoft.jasperserver.api.metadata.common.domain.InputControl;
import com.jaspersoft.jasperserver.api.metadata.common.domain.Query;
import com.jaspersoft.jasperserver.api.metadata.common.domain.ResourceReference;
import com.jaspersoft.jasperserver.api.metadata.common.domain.client.QueryImpl;
import com.jaspersoft.jasperserver.inputcontrols.cascade.CachedEngineService;
import com.jaspersoft.jasperserver.inputcontrols.cascade.CachedRepositoryService;
import com.jaspersoft.jasperserver.inputcontrols.cascade.CascadeResourceNotFoundException;
import com.jaspersoft.jasperserver.inputcontrols.cascade.token.FilterResolver;
import com.jaspersoft.jasperserver.inputcontrols.cascade.token.ParameterTypeLookup;
import org.apache.commons.collections.OrderedMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * <p></p>
 *
 * @author Vlad Zavadskyi
 */
@Service
public class QueryValuesLoaderStrategy implements ValuesLoaderStrategy {
    private static final Log log = LogFactory.getLog(QueryValuesLoaderStrategy.class);

    @Resource
    private CachedEngineService cachedEngineService;
    @Resource
    private InputControlDataSourceResolver inputControlDataSourceResolver;
    @Resource
    protected CachedRepositoryService cachedRepositoryService;
    @Resource
    protected FilterResolver filterResolver;
    @Resource
    protected ParameterTypeLookup parameterTypeCompositeLookup;

    @Override
    public ResultsOrderedMap resultsOrderedMap(InputControl inputControl,
                                               ResourceReference dataSource,
                                               String criteria,
                                               Map<String, Object> parameters,
                                               Map<String, Class<?>> parameterTypes) throws CascadeResourceNotFoundException {
        ResourceReference dataSourceForQuery = inputControlDataSourceResolver.resolveDatasource(inputControl, dataSource);

        final ResultsOrderedMap.Builder builder = new ResultsOrderedMap.Builder();
        final Query query = cachedRepositoryService.getResource(Query.class, inputControl.getQuery());

        Map<String, Object> domainSchemaParameters = inputControlDataSourceResolver.prepareDomainDataSourceParameters(dataSourceForQuery);
        ResourceReference queryResourceReference = inputControl.getQuery();

        HashMap<String, Object> parametersWithSchema = new HashMap<>();
        parametersWithSchema.putAll(parameters);
        parametersWithSchema.putAll(domainSchemaParameters);


        // if criteria is not null, construct new query with criteria
        // if fails to update query with new criteria info, roll back to search criteria in memory
        try {
            String newSQL = filterResolver.updateQueryWithNewSearchCriteria(query.getSql(), parametersWithSchema, inputControl, criteria);

            if (newSQL != null) {
                // update new query with criteria filter
                if (inputControl.getQuery().getLocalResource() instanceof QueryImpl) {
                    queryResourceReference = new ResourceReference();
                    queryResourceReference.setLocalResource(createNewQueryImplWithNewSQL(((QueryImpl) inputControl.getQuery().getLocalResource()), newSQL));
                }
                // no longer needs to search criteria in memory, skip this criteria
                builder.setSkipCriteriaSearch(true);
            }
        } catch (JSException ex) {
            log.debug(ex.getMessage() + " Roll back to search criteria in memory instead.");
        }

        Map<String, Object> executionParameters = filterAndFillMissingQueryParameters(query, parameters, domainSchemaParameters);
        Map<String, Class<?>> executionParameterTypes = filterParameterTypes(executionParameters.keySet(), parameterTypes);
        Map<String, Class<?>> missingParameterTypes = findMissingParameterTypes(dataSource, executionParameters, executionParameterTypes);

        executionParameterTypes.putAll(missingParameterTypes);

        if (parameters != null && parameters.containsKey(EhcacheEngineService.IC_REFRESH_KEY)) {
            executionParameters.put(EhcacheEngineService.IC_REFRESH_KEY, "true");
        }
        if (parameters != null && parameters.containsKey(EhcacheEngineService.DIAGNOSTIC_REPORT_URI)) {
            executionParameters.put(EhcacheEngineService.DIAGNOSTIC_REPORT_URI, parameters.get(EhcacheEngineService.DIAGNOSTIC_REPORT_URI));
        }
        if (parameters != null && parameters.containsKey(EhcacheEngineService.DIAGNOSTIC_STATE)) {
            executionParameters.put(EhcacheEngineService.DIAGNOSTIC_STATE, parameters.get(EhcacheEngineService.DIAGNOSTIC_STATE));
        }

        try {
            OrderedMap orderedMap = cachedEngineService.executeQuery(
                    ExecutionContextImpl.getRuntimeExecutionContext(), queryResourceReference,
                    inputControl.getQueryValueColumn(), inputControl.getQueryVisibleColumns(),
                    dataSourceForQuery, executionParameters, executionParameterTypes, inputControl.getName());
            builder.setOrderedMap(orderedMap);
        } catch (JSMissingDataSourceFieldsException | IllegalArgumentException e) {
            // This occurs when a field previously found in the domain is missing
            // we ignore here as we do not need these values, and an error is rendered on the report canvas
            log.debug(e.getMessage(), e);
        }

        return builder.build();
    }

    /**
     * Create new Map with specified parameters, add only those which are used in query, find missing parameters and assign value Null to them.
     * This is done because we indicate Nothing selection in single select control as absence of parameter in map,
     * but QueryManipulator sets query to empty string and Executor throws exception if parameter is absent,
     * so we set Null as most suitable value as yet.
     *
     * @param query      Query
     * @param parameters Map&lt;String, Object&gt;
     * @return Map&lt;String, Object&gt; copy of map, where missing parameters filled with Null values.
     */
    Map<String, Object> filterAndFillMissingQueryParameters(Query query, Map<String, Object> parameters, Map<String, Object> domainSchemaParameters) {
        HashMap<String, Object> parametersWithSchema = new HashMap<>();
        parametersWithSchema.putAll(parameters);
        parametersWithSchema.putAll(domainSchemaParameters);
        Set<String> queryParameterNames = filterResolver.getParameterNames(query.getSql(), parametersWithSchema);
        HashMap<String, Object> resolvedParameters = new HashMap<>();
        for (String queryParameterName : queryParameterNames) {
            // If parameter is missing, set Null.
            resolvedParameters.put(queryParameterName, parameters.get(queryParameterName));
        }
        resolvedParameters.putAll(domainSchemaParameters);
        return resolvedParameters;
    }

    /**
     * Filter only specified parameter types
     *
     * @param parameters     Parameter names
     * @param parameterTypes Map of parameter types
     * @return Filtered map of parameter types
     */
    Map<String, Class<?>> filterParameterTypes(Set<String> parameters, Map<String, Class<?>> parameterTypes) {
        Map<String, Class<?>> filteredParameterTypes = new HashMap<>(parameters.size());
        for (String parameterName : parameters) {
            if (parameterTypes.containsKey(parameterName)) {
                filteredParameterTypes.put(parameterName, parameterTypes.get(parameterName));
            }
        }
        return filteredParameterTypes;
    }

    /**
     * Retrieves additional parameter types from dataSource (if it has any)
     *
     * @param dataSource     a resource that might have parameters
     * @param parameters     a map of all parameters
     * @param parameterTypes types that were find out earlier
     * @return a map with parameter name and type
     * @throws CascadeResourceNotFoundException
     */
    Map<String, Class<?>> findMissingParameterTypes(ResourceReference dataSource,
                                                    Map<String, Object> parameters,
                                                    Map<String, Class<?>> parameterTypes) throws CascadeResourceNotFoundException {
        Set<String> missingParameterTypes = new HashSet<>(parameters.keySet());
        missingParameterTypes.removeAll(parameterTypes.keySet());
        if (!missingParameterTypes.isEmpty()) {
            return parameterTypeCompositeLookup.getParameterTypes(ExecutionContextImpl.getRuntimeExecutionContext(), dataSource, missingParameterTypes);
        } else {
            return Collections.emptyMap();
        }
    }

    QueryImpl createNewQueryImplWithNewSQL(QueryImpl queryImpl, String sql) {
        QueryImpl newQueryImpl = new QueryImpl();
        newQueryImpl.setDataSource(queryImpl.getDataSource());
        newQueryImpl.setLanguage(queryImpl.getLanguage());
        newQueryImpl.setSql(sql);
        newQueryImpl.setParameters(queryImpl.getParameters());
        newQueryImpl.setName(queryImpl.getName());
        newQueryImpl.setLabel(queryImpl.getLabel());
        newQueryImpl.setAttributes(queryImpl.getAttributes());
        newQueryImpl.setParentFolder(queryImpl.getParentFolder());
        return newQueryImpl;
    }
}
