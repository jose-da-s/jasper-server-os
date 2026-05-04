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
package com.jaspersoft.jasperserver.remote.connection;

import com.jaspersoft.jasperserver.api.ExceptionListWrapper;
import com.jaspersoft.jasperserver.api.JSExceptionWrapper;
import com.jaspersoft.jasperserver.api.common.domain.ExecutionContext;
import com.jaspersoft.jasperserver.api.common.domain.impl.ExecutionContextImpl;
import com.jaspersoft.jasperserver.api.metadata.user.service.ProfileAttributesResolver;
import com.jaspersoft.jasperserver.core.util.type.GenericParametersHelper;
import com.jaspersoft.jasperserver.core.util.type.GenericTypeProcessorRegistry;
import com.jaspersoft.jasperserver.dto.common.ClientTypeUtility;
import com.jaspersoft.jasperserver.dto.common.validations.SupportsValidation;
import com.jaspersoft.jasperserver.remote.common.JrsBeanValidator;
import com.jaspersoft.jasperserver.remote.connection.storage.ContextDataPair;
import com.jaspersoft.jasperserver.remote.connection.storage.ContextDataStorage;
import com.jaspersoft.jasperserver.remote.exception.IllegalParameterValueException;
import com.jaspersoft.jasperserver.remote.exception.MandatoryParameterNotFoundException;
import com.jaspersoft.jasperserver.remote.exception.OperationCancelledException;
import com.jaspersoft.jasperserver.remote.exception.ResourceNotFoundException;
import com.jaspersoft.jasperserver.remote.exception.UnsupportedOperationErrorDescriptorException;
import com.jaspersoft.jasperserver.remote.resources.converters.ToServerConverter;
import com.jaspersoft.jasperserver.remote.validation.ClientValidator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * <p></p>
 *
 * @author Yaroslav.Kovalchyk
 * @version $Id$
 */
@Service
public class ContextsManager {
    private static final Logger log = LogManager.getLogger(ContextsManager.class);

    @Resource
    private List<ContextQueryExecutor> queryExecutors;
    private Map<String, Class<?>> queryTypeToClassMapping;
    private Map<Class<?>, List<ContextQueryExecutor>> queryClassToQueryExecutorsMapping;
    @Resource
    private List<ContextParametrizedMetadataBuilder> parametrizedMetadataBuilders;
    private Map<String, Class<?>> metadataParamsTypeToClassMapping;
    private Map<String, Class<?>> typeToClassMapping;
    @Resource
    private GenericTypeProcessorRegistry genericTypeProcessorRegistry;
    @Resource
    private ProfileAttributesResolver profileAttributesResolver;
    @Resource
    private ContextDataStorage contextDataStorage;
    @Resource
    private JrsBeanValidator jrsBeanValidator;
    @Resource
    private ContextExecutorService contextExecutorService;

    public Class<?> getContextDescriptionClass(String contextType) {
        return typeToClassMapping.get(contextType != null ? contextType.toLowerCase() : null);
    }

    public Class<?> getQueryClass(String queryType) {
        return queryTypeToClassMapping.get(queryType != null ? queryType.toLowerCase() : null);
    }

    public Class<?> getMetadataParamsClass(String paramsType) {
        return metadataParamsTypeToClassMapping.get(paramsType != null ? paramsType.toLowerCase() : null);
    }

    public UUID createContext(Object contextDescription) throws IllegalParameterValueException {
        if (contextDescription == null) {
            throw new MandatoryParameterNotFoundException("body");
        }
        final ExecutionContext ctx = ExecutionContextImpl.getRuntimeExecutionContext();

        final ContextManagementStrategy<Object, Object> strategy = (ContextManagementStrategy<Object, Object>) getStrategy(contextDescription.getClass());
        if (strategy.getClass().getAnnotation(SupportsValidation.class) == null) {
            jrsBeanValidator.validate(contextDescription);
            // generic type processor registry assures safety of unchecked assignment
            ClientValidator clientValidator = genericTypeProcessorRegistry.getTypeProcessor(contextDescription.getClass(), ClientValidator.class, false);
            if (clientValidator != null) {
                final List<Exception> exceptions = clientValidator.validate(ctx, contextDescription);
                if (!exceptions.isEmpty()) {
                    throw new ExceptionListWrapper(exceptions);
                }
            }
        }
        final Map<String, Object> data = new HashMap<String, Object>();
        final Object context = strategy.createContext(ctx, contextDescription, data);
        ContextDataPair item = new ContextDataPair(context, data).setExternalContextClass(contextDescription.getClass());
        UUID uuid = contextDataStorage.save(item);
        log.debug("Created new context: {}", uuid);
        return uuid;
    }


    public UUID createRawContext(Object contextDescription) throws IllegalParameterValueException {
        if (contextDescription == null) {
            throw new MandatoryParameterNotFoundException("body");
        }
        final Map<String, Object> data = new HashMap<String, Object>();
        ContextDataPair item = new ContextDataPair(contextDescription, data).setExternalContextClass(contextDescription.getClass());
        UUID uuid = contextDataStorage.save(item);
        log.debug("Created raw context: {}", uuid);
        return uuid;
    }

    public void updateRawContext(UUID uuid, Object contextDescription) throws IllegalParameterValueException {
        if (contextDescription == null) {
            throw new MandatoryParameterNotFoundException("body");
        }
        final Map<String, Object> data = new HashMap<String, Object>();
        ContextDataPair item = new ContextDataPair(contextDescription, data).setExternalContextClass(contextDescription.getClass());
        contextDataStorage.update(uuid, item);
        log.debug("Updated raw context: {}", uuid);
    }


    public boolean isMetadataSupported(ExecutionContext ctx, Object contextDescription, String metadataClientType) {
        final ContextMetadataBuilder<?> metadataBuilder = getMetadataBuilder(contextDescription);
        boolean result = false;
        if (metadataBuilder instanceof GenericTypeMetadataBuilder) {
            // some context strategies serves data sources of multiple types, described by generic model.
            final GenericTypeMetadataBuilder genericTypeMetadataBuilder = (GenericTypeMetadataBuilder) metadataBuilder;
            try {
                result = genericTypeMetadataBuilder.isMetadataSupported(ctx, contextDescription, metadataClientType);
            } catch (ClassCastException e) {
                final ToServerConverter typeProcessor = genericTypeProcessorRegistry
                        .getTypeProcessor(contextDescription.getClass(), ToServerConverter.class, false);
                if (typeProcessor != null) {
                    // it's a case with internal representation of resource. Let's try to convert it and check
                    // if metadata is supported
                    try {
                        result = genericTypeMetadataBuilder.isMetadataSupported(ctx,
                                typeProcessor.toServer(ctx, contextDescription, null), metadataClientType);
                    } catch (Exception ex) {
                        // do nothing. Don't support this case
                    }
                }
            }
        } else if(metadataBuilder != null) {
            result = extractMetadataClientResourceType(metadataBuilder, contextDescription)
                    .equalsIgnoreCase(metadataClientType);
        }
        return result;
    }

    protected ContextMetadataBuilder<?> getMetadataBuilder(Object contextDescription) {
        final Class<?> contextDescriptionClass = contextDescription.getClass();
        final ContextManagementStrategy contextManagementStrategy = genericTypeProcessorRegistry.getTypeProcessor(contextDescriptionClass, ContextManagementStrategy.class);
        final Class<?> internalContextType = GenericParametersHelper
                .getGenericTypeArgument(contextManagementStrategy.getClass(), ContextManagementStrategy.class, 1);
        ContextMetadataBuilder metadataBuilder = genericTypeProcessorRegistry.getTypeProcessor(internalContextType,
                ContextMetadataBuilder.class, false);
        if (metadataBuilder == null && contextDescriptionClass != internalContextType) {
            if (internalContextType.isAssignableFrom(contextDescriptionClass)) {
                // real context class is a subtype of internal context type. It's possible if strategy handles multiple types.
                // Let's try to find metadata builder for context class
                metadataBuilder = genericTypeProcessorRegistry.getTypeProcessor(contextDescriptionClass,
                        ContextMetadataBuilder.class, false);
            } else if (contextManagementStrategy instanceof GenericTypeContextStrategy) {
                // context strategy converts external type to internal. Let's ask it for concrete internal type
                final Class concreteInternalType = ((GenericTypeContextStrategy) contextManagementStrategy)
                        .getConcreteInternalType(contextDescription);
                metadataBuilder = genericTypeProcessorRegistry.getTypeProcessor(concreteInternalType,
                        ContextMetadataBuilder.class, false);
            }
        }
        return metadataBuilder;
    }

    public String getMetadataClientResourceType(Object contextDescription) {
        final ContextMetadataBuilder<?> metadataBuilder = getMetadataBuilder(contextDescription);
        String result = null;
        if (metadataBuilder instanceof GenericTypeMetadataBuilder) {
            // some context strategies serves data sources of multiple types, described by generic model.
            final GenericTypeMetadataBuilder genericTypeMetadataBuilder = (GenericTypeMetadataBuilder) metadataBuilder;
            try {
                result = genericTypeMetadataBuilder.getMetadataClientResourceType(contextDescription);
            } catch (ClassCastException e) {
                final ToServerConverter typeProcessor = genericTypeProcessorRegistry
                        .getTypeProcessor(contextDescription.getClass(), ToServerConverter.class, false);
                if (typeProcessor != null) {
                    // it's a case with internal representation of resource. Let's try to convert it
                    // and get metadata client type
                    try {
                        result = genericTypeMetadataBuilder.getMetadataClientResourceType(typeProcessor.toServer(ExecutionContextImpl.getRuntimeExecutionContext(), contextDescription, null));
                    } catch (Exception ex) {
                        // do nothing. Don't support this case
                    }
                }
            }
        }
        return result != null ? result : extractMetadataClientResourceType(metadataBuilder, contextDescription);
    }

    protected String extractMetadataClientResourceType(ContextMetadataBuilder<?> metadataBuilder,
            Object contextDescription) {
        String result = null;
        final ClientResourceType clientResourceTypeAnnotation;
        try {
            clientResourceTypeAnnotation = metadataBuilder.getClass().getMethod("build",
                    Object.class, Map.class, Map.class)
                    .getAnnotation(ClientResourceType.class);
        } catch (NoSuchMethodException e) {
            // may not happen
            throw new IllegalStateException("no build method in ContextMetadataBuilder.");
        }
        if (clientResourceTypeAnnotation != null) {
            result = clientResourceTypeAnnotation.value();
        }
        return result != null ? result :
                "repository." + ClientTypeUtility.extractClientType(contextDescription.getClass()) + ".metadata";
    }

    public Object getContext(UUID uuid, Map<String, String[]> additionalProperties) throws ResourceNotFoundException {
        log.debug("Get context: {}", uuid);
        final ContextDataPair pair = contextDataStorage.get(uuid);
        return pair != null ? getStrategy(pair)
                .getContextForClient(pair.getContext(), pair.getData(), additionalProperties) : null;
    }

    public Object getRawContext(UUID uuid) {
        log.debug("Get raw context: {}", uuid);
        final ContextDataPair pair = contextDataStorage.get(uuid);
        return (pair != null ? pair.getContext() : null);
    }

    public void removeContext(UUID uuid) throws ResourceNotFoundException {
        log.debug("Remove context: {}", uuid);
        final ContextDataPair pair = contextDataStorage.get(uuid, false);
        if (pair != null) {
            ContextManagementStrategy<Object, Object> strategy = getStrategy(pair);
            if (strategy != null) {
                strategy.deleteContext(pair.getContext(), pair.getData());
            }
        }
        contextDataStorage.delete(uuid);
        contextExecutorService.cancelContext(uuid);
    }

    // generic processor registry assures safety of call
    @SuppressWarnings("unchecked")
    public Object getContextMetadata(UUID uuid, final Map<String, String[]> options) throws ResourceNotFoundException, UnsupportedOperationErrorDescriptorException {
        log.debug("Get context metadata: {}", uuid);
        final ContextDataPair pair = contextDataStorage.get(uuid);
        final Object context = pair.getContext();
        ContextMetadataBuilder typeProcessor = genericTypeProcessorRegistry.getTypeProcessor(context.getClass(), ContextMetadataBuilder.class, false);
        if (typeProcessor == null && context instanceof com.jaspersoft.jasperserver.api.metadata.common.domain.Resource) {
            typeProcessor = genericTypeProcessorRegistry
                    .getTypeProcessor(
                            ((com.jaspersoft.jasperserver.api.metadata.common.domain.Resource) context)
                                    .getResourceType(),
                            ContextMetadataBuilder.class, false);
        }
        if (typeProcessor == null) {
            throw new UnsupportedOperationErrorDescriptorException(ClientTypeUtility.extractClientType(context.getClass()) + "/metadata");
        }
        resolveAttributes(options);
        final Object mergedContext = profileAttributesResolver.mergeObject(context, uuid.toString());
        final ContextMetadataBuilder typeProcessorClosure = typeProcessor;
        final Map<String, Object> data = pair.getData();
        return callAndGet(uuid, () -> {
            log.debug("Starting building of metadata for context {}", context);
            final Object result = typeProcessorClosure.build(mergedContext, options, data);
            log.debug("Building of metadata completed. Context {}", context);
            return result;
        });
    }

    protected <T> T callAndGet(UUID contextUuid, Callable<T> callable){
        final Future<T> future = contextExecutorService.runWithContext(contextUuid, callable);
        try {
            return future.get();
        } catch (CancellationException e){
            throw new OperationCancelledException(e);
        } catch (ExecutionException e){
            final Throwable cause = e.getCause();
            if(cause instanceof RuntimeException){
                throw (RuntimeException)cause;
            } else {
                throw new JSExceptionWrapper((Exception) cause);
            }
        } catch (Exception e) {
            throw new JSExceptionWrapper(e);
        }
    }

    public Object getContextMetadata(UUID uuid, Object metadataParams) {
        log.debug("Get context metadata: {}", uuid);
        final ContextDataPair pair = contextDataStorage.get(uuid);
        final Object context = pair.getContext();
        final ContextParametrizedMetadataBuilder typeProcessor = genericTypeProcessorRegistry
                .getTypeProcessor(context.getClass(), ContextParametrizedMetadataBuilder.class, false);
        if (typeProcessor == null) {
            throw new UnsupportedOperationErrorDescriptorException(ClientTypeUtility.extractClientType(context.getClass()) + "/metadata");
        }
        final Object mergedContext = profileAttributesResolver.mergeObject(context, uuid.toString());
        final Object mergedMetadataParams = profileAttributesResolver.mergeObject(metadataParams, uuid.toString());
        final Map<String, Object> data = pair.getData();
        return callAndGet(uuid, () -> {
            log.debug("Starting building of metadata for context {}", context);
            final Object result = typeProcessor.build(mergedContext, mergedMetadataParams, data);
            log.debug("Building of metadata completed. Context {}", context);
            return result;
        });
    }

    protected void resolveAttributes(Map<String, String[]> options) {
        if (options != null && options.size() > 0) {
            for (String[] option : options.values()) {
                if (option != null && option.length > 0) {
                    for (int i = 0; i < option.length; i++) {
                        String optionValue = option[i];
                        if (profileAttributesResolver.containsAttribute(optionValue)) {
                            option[i] = profileAttributesResolver.merge(optionValue, null);
                        }
                    }
                }
            }
        }
    }

    public Object executeQuery(UUID uuid, final Object query, final Map<String, String[]> queryParameters) {
        jrsBeanValidator.validate(query);
        final ContextDataPair contextDataPair = contextDataStorage.get(uuid);
        final Object context = contextDataPair.getContext();
        final ContextQueryExecutor<Object, Object> queryExecutor = getQueryExecutor(query, context);
        return callAndGet(uuid, () -> {
            log.debug("Starting of query execution for context {}", context);
            final Object result = queryExecutor.executeQuery(query, context, queryParameters, contextDataPair.getData());
            log.debug("Query execution is completed. Context {}", context);
            return result;
        });
    }

    public Object executeQueryForMetadata(UUID uuid, final Object query) {
        jrsBeanValidator.validate(query);
        final ContextDataPair contextDataPair = contextDataStorage.get(uuid);
        final Object context = contextDataPair.getContext();
        final ContextQueryExecutor<Object, Object> queryExecutor = getQueryExecutor(query, context);
        return callAndGet(uuid, () -> {
            log.debug("Starting of query execution for metadata. Context {}", context);
            final Object result = queryExecutor.executeQueryForMetadata(query, context, contextDataPair.getData());
            log.debug("Query execution for metadata is completed. Context {}", context);
            return result;
        });
    }

    protected ContextQueryExecutor<Object, Object> getQueryExecutor(Object query, Object context) {
        ContextQueryExecutor<Object, Object> queryExecutor = null;
        final ContextManagementStrategy<Object, Object> strategy = (ContextManagementStrategy<Object, Object>) getStrategy(context.getClass());
        if (strategy instanceof ContextQueryExecutor) {
            queryExecutor = (ContextQueryExecutor<Object, Object>) strategy;
        } else {
            final List<ContextQueryExecutor> queryExecutorList = queryClassToQueryExecutorsMapping.get(query.getClass());
            if (queryExecutorList != null) {
                for (ContextQueryExecutor currentQueryExecutor : queryExecutorList) {
                    final Class<?> contextClass = GenericParametersHelper
                            .getGenericTypeArgument(currentQueryExecutor.getClass(), ContextQueryExecutor.class, 1);
                    if (contextClass.isAssignableFrom(context.getClass())) {
                        queryExecutor = currentQueryExecutor;
                        break;
                    }
                }
            }
        }
        if (queryExecutor == null) {
            throw new UnsupportedOperationErrorDescriptorException(ClientTypeUtility.extractClientType(context.getClass()));
        }
        return queryExecutor;
    }

    // initialization code in afterPropertiesSet() ensures cast safety in this case.
    @SuppressWarnings("unchecked")
    protected <T> ContextManagementStrategy<T, Object> getStrategy(Class<T> objectClass) {
        return (ContextManagementStrategy<T, Object>) genericTypeProcessorRegistry.getTypeProcessor(objectClass,
                ContextManagementStrategy.class, false);
    }

    // initialization code in afterPropertiesSet() ensures cast safety in this case.
    @SuppressWarnings("unchecked")
    protected <T> ContextManagementStrategy<T, Object> getStrategy(ContextDataPair contextDataPair) {
        Class internalContextClass = contextDataPair.getContext().getClass();
        Class externalContextClass = contextDataPair.getExternalContextClass();
        return (ContextManagementStrategy<T, Object>) genericTypeProcessorRegistry.getTypeProcessor((externalContextClass != null) ? externalContextClass : internalContextClass,
                ContextManagementStrategy.class, false);
    }

    @PostConstruct
    public void afterPropertiesSet() throws Exception {
        Map<String, Class<?>> typeToClassMap = new HashMap<String, Class<?>>();
        final Set<Class<?>> contextDescriptionClasses = genericTypeProcessorRegistry
                .getProcessorMapping(ContextManagementStrategy.class).keySet();
        for (Class<?> contextDescriptionClass : contextDescriptionClasses) {
            typeToClassMap.put(ClientTypeUtility.extractClientType(contextDescriptionClass).toLowerCase(), contextDescriptionClass);
        }
        typeToClassMapping = Collections.unmodifiableMap(typeToClassMap);
        if (queryExecutors != null) {
            Map<String, Class<?>> queryTypeToClassMap = new HashMap<String, Class<?>>();
            Map<Class<?>, List<ContextQueryExecutor>> queryClassToQueryExecutorMap = new HashMap<Class<?>, List<ContextQueryExecutor>>();
            for (ContextQueryExecutor queryExecutor : queryExecutors) {
                final Class<?> queryClass = GenericParametersHelper.getGenericTypeArgument(queryExecutor.getClass(),
                        ContextQueryExecutor.class, 0);
                final String queryType = ClientTypeUtility.extractClientType(queryClass).toLowerCase();
                queryTypeToClassMap.put(queryType, queryClass);
                List<ContextQueryExecutor> executorsForClass = queryClassToQueryExecutorMap.get(queryClass);
                if (executorsForClass == null) {
                    executorsForClass = new ArrayList<ContextQueryExecutor>();
                    queryClassToQueryExecutorMap.put(queryClass, executorsForClass);
                }
                executorsForClass.add(queryExecutor);
            }
            queryTypeToClassMapping = Collections.unmodifiableMap(queryTypeToClassMap);
            for (Class<?> clazz : queryClassToQueryExecutorMap.keySet()) {
                queryClassToQueryExecutorMap.put(clazz, Collections.unmodifiableList(queryClassToQueryExecutorMap.get(clazz)));
            }
            queryClassToQueryExecutorsMapping = Collections.unmodifiableMap(queryClassToQueryExecutorMap);
        }
    }

    @PostConstruct
    public void initMetadataParamsTypes() {
        Map<String, Class<?>> metadataParamsMapping = new HashMap<String, Class<?>>();
        if (parametrizedMetadataBuilders != null) {
            for (ContextParametrizedMetadataBuilder parametrizedMetadataBuilder : parametrizedMetadataBuilders) {
                final Class<?> metadataOptionsClass = GenericParametersHelper
                        .getGenericTypeArgument(parametrizedMetadataBuilder.getClass(),
                                ContextParametrizedMetadataBuilder.class, 1);
                metadataParamsMapping
                        .put(ClientTypeUtility.extractClientType(metadataOptionsClass).toLowerCase(), metadataOptionsClass);
            }
        }
        metadataParamsTypeToClassMapping = Collections.unmodifiableMap(metadataParamsMapping);
    }
}

