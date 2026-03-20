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
package com.jaspersoft.jasperserver.remote.resources.converters;

import com.jaspersoft.jasperserver.api.ErrorDescriptorException;
import com.jaspersoft.jasperserver.api.ExceptionListWrapper;
import com.jaspersoft.jasperserver.api.common.domain.ExecutionContext;
import com.jaspersoft.jasperserver.api.common.util.TimeZoneContextHolder;
import com.jaspersoft.jasperserver.api.engine.jasperreports.util.CalendarFormatProvider;
import com.jaspersoft.jasperserver.api.metadata.common.domain.Resource;
import com.jaspersoft.jasperserver.api.metadata.common.domain.ResourceReference;
import com.jaspersoft.jasperserver.api.metadata.common.domain.util.ToClientConversionOptions;
import com.jaspersoft.jasperserver.api.metadata.common.service.ResourceFactory;
import com.jaspersoft.jasperserver.core.util.type.GenericTypeProcessorRegistry;
import com.jaspersoft.jasperserver.dto.resources.ClientResource;
import com.jaspersoft.jasperserver.remote.exception.IllegalParameterValueException;
import com.jaspersoft.jasperserver.remote.exception.MandatoryParameterNotFoundException;
import com.jaspersoft.jasperserver.remote.exception.ResourceGroupProfileAttributeErrorDescriptor;
import com.jaspersoft.jasperserver.remote.exception.ResourceValidationException;
import com.jaspersoft.jasperserver.remote.resources.ClientTypeHelper;
import com.jaspersoft.jasperserver.remote.resources.attachments.AttachmentsProcessor;
import com.jaspersoft.jasperserver.remote.resources.validation.ResourceValidator;
import com.jaspersoft.jasperserver.remote.services.PermissionsService;
import com.jaspersoft.jasperserver.remote.validation.ClientValidator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <p></p>
 *
 * @author Yaroslav.Kovalchyk
 * @version $Id$
 */
public abstract class ResourceConverterImpl<ResourceType extends Resource, ClientType extends ClientResource<ClientType>> implements ResourceConverter<ResourceType, ClientType> {

    private static final Log log = LogFactory.getLog(ResourceConverterImpl.class);

    @jakarta.annotation.Resource(name = "mappingResourceFactory")
    protected ResourceFactory objectFactory;

    @jakarta.annotation.Resource(name = "isoCalendarFormatProvider")
    protected CalendarFormatProvider calendarFormatProvider;

    @jakarta.annotation.Resource(name = "concretePermissionsService")
    private PermissionsService permissionsService;

    @jakarta.annotation.Resource
    private GenericTypeProcessorRegistry genericTypeProcessorRegistry;

    private ServerResourceTypeExtractor serverResourceTypeExtractor;

    private ClientTypeHelper<ClientType> clientTypeHelper;

    @jakarta.annotation.Resource(name = "basicResourceValidator")
    private ResourceValidator defaultValidator;
    @jakarta.annotation.Resource(name = "beanValidator")
    private Validator validator;

    public ResourceConverterImpl() {
        clientTypeHelper = new ClientTypeHelper(this.getClass());
        serverResourceTypeExtractor = new ServerResourceTypeExtractor(this.getClass());
    }

    // object factory returns correct type of resource. So, cast below is safe
    @SuppressWarnings("unchecked")
    protected ResourceType getNewResourceInstance() {
        return (ResourceType) objectFactory.newResource(null, getServerResourceType());
    }

    protected DateFormat getDateTimeFormat() {
        return calendarFormatProvider.getDatetimeFormat();
    }

    public String getServerResourceType() {
        return serverResourceTypeExtractor.getServerResourceType();
    }

    @Override
    public ResourceType toServer(ExecutionContext ctx, ClientType clientObject, ToServerConversionOptions options) throws IllegalParameterValueException, MandatoryParameterNotFoundException {
        return toServer(ctx, clientObject, null, options);
    }

    @Override
    public ResourceType toServer(ExecutionContext ctx, ClientType clientObject, ResourceType resultToUpdate, ToServerConversionOptions options) throws IllegalParameterValueException, MandatoryParameterNotFoundException {
        List<Exception> exceptions = new ArrayList<Exception>();
        ClientValidator clientValidator = genericTypeProcessorRegistry.getTypeProcessor(clientObject.getClass(), ClientValidator.class, false);
        if (clientValidator != null) {
            final List<Exception> list = (List<Exception>)clientValidator.validate(ctx, clientObject);
            addValidExceptions(exceptions, list, options);
        }
        Class<?>[] validationGroups = (options == null)? new Class[0] : options.getValidationGroups();
        try {
            Set<ConstraintViolation<ClientType>> constraintViolations = validator.validate(clientObject, validationGroups);
            if (!constraintViolations.isEmpty()) {
                exceptions.add(new ConstraintViolationException("Resource validation failed.", constraintViolations));
            }
        } catch (java.util.ConcurrentModificationException ex) {
            // JRS-17051
            // hibernate throws conncurrentModificationException when trying to validate clientDomain object
            // currently, we cannot get around this issue until we upgrade our hibernate
            // temporay solution until this hibernate problem is fixed:
            // we can catch the hibernate error and throw 400 with Resource validation failed. However, we can't tell client which element is failing in this case.
            exceptions.add(new ResourceValidationException("Resource validation failed."));
        }

        ResourceType resource;
        try {
            resource = genericFieldsToServer(ctx, clientObject, resultToUpdate, options);
            resource = resourceSpecificFieldsToServer(ctx, clientObject, resource, exceptions, options);
        } catch (RuntimeException e) {
            if (exceptions.isEmpty()) {
                log.warn("Domain converting failed. ", e);
                throw e;
            } else if (e instanceof ErrorDescriptorException) {
                exceptions.add(e);
            }
            throw new ExceptionListWrapper(exceptions);
        }
        if (options != null && options.getAttachments() != null) {
            // impossible to build
            @SuppressWarnings("unchecked")
            AttachmentsProcessor<ResourceType> attachmentsProcessor = genericTypeProcessorRegistry.getTypeProcessor(getServerResourceType(), AttachmentsProcessor.class, false);
            if (attachmentsProcessor != null) {
                resource = attachmentsProcessor.processAttachments(resource, options.getAttachments());
            }
        }
        if (options == null || !options.isSuppressValidation()) {
            boolean skipRepoFieldsValidation = false;
            Map<String, String[]> additionalProperties = new HashMap<String, String[]>();
            if(options != null){
                skipRepoFieldsValidation = options.isSkipRepoFieldsValidation();
                if (options.getAdditionalProperties() != null) additionalProperties = options.getAdditionalProperties();
            }
            exceptions.addAll(validateResource(ctx, resource, skipRepoFieldsValidation, additionalProperties));
        }
        if (!exceptions.isEmpty()) throw new ExceptionListWrapper(exceptions);
        return resource;
    }

    protected void addValidExceptions(List<Exception> includedExceptions, List<Exception> allFoundExceptions,  ToServerConversionOptions options) {
        boolean isAddAllExceptions = true;
        if (options.getAdditionalProperties() != null) {
            // JRS-19513
            // when skipDatabaseMetadataCheck is set to true, we should ignore exception from profile attribute schemas also
            String[] values = options.getAdditionalProperties().get(ToServerConversionOptions.SKIP_DATA_BASE_METADATA_CHECK);
            if ((values != null) && (values.length > 0) && "true".equalsIgnoreCase(values[0])) {
                isAddAllExceptions = false;
                for (Exception ex : allFoundExceptions) {
                    if (ex instanceof ErrorDescriptorException) {
                        if (!((ErrorDescriptorException) ex).getErrorDescriptor().getErrorCode().equals(ResourceGroupProfileAttributeErrorDescriptor.ERROR_CODE)) {
                            includedExceptions.add(ex);
                        }
                    }
                }
            }
        }
        if (isAddAllExceptions) {
            includedExceptions.addAll(allFoundExceptions);
        }
    }


    protected List<Exception> validateResource(ExecutionContext ctx, ResourceType resource, boolean skipRepoFieldsValidation,
                                               Map<String, String[]> additionalParameters) {
        ResourceValidator<ResourceType> validator = genericTypeProcessorRegistry
                .getTypeProcessor(resource.getResourceType(), ResourceValidator.class, false);
        List<Exception> result;
        if(validator != null){

        }
        return  (validator != null ? validator : defaultValidator).validate(ctx, resource,
                skipRepoFieldsValidation, additionalParameters);
    }

    protected List<Exception> validateResource(ExecutionContext ctx, ResourceType resource, boolean skipRepoFieldsValidation) {
        return validateResource(ctx, resource, skipRepoFieldsValidation, Collections.<String, String[]>emptyMap());
    }

    protected ResourceType genericFieldsToServer(ExecutionContext ctx, ClientType clientObject, ResourceType resultToUpdate, ToServerConversionOptions options) throws IllegalParameterValueException, MandatoryParameterNotFoundException {
        if (resultToUpdate == null) {
            resultToUpdate = getNewResourceInstance();
            resultToUpdate.setVersion(Resource.VERSION_NEW);
        } else {
            resultToUpdate.setVersion(clientObject.getVersion() == null || (options != null && options.isResetVersion()) ?
                    Resource.VERSION_NEW : clientObject.getVersion());
        }
        resultToUpdate.setURIString(clientObject.getUri());
        resultToUpdate.setDescription(clientObject.getDescription());
        resultToUpdate.setLabel(clientObject.getLabel());
        return resultToUpdate;
    }

    protected abstract ResourceType resourceSpecificFieldsToServer(ExecutionContext ctx, ClientType clientObject, ResourceType resultToUpdate, List<Exception> exceptions, ToServerConversionOptions options) throws IllegalParameterValueException, MandatoryParameterNotFoundException;

    @Override
    public ClientType toClient(ResourceType serverObject, ToClientConversionOptions options) {
        final ClientType client = genericFieldsToClient(getNewClientObjectInstance(), serverObject, options);
        final ClientType resultClient = resourceSpecificFieldsToClient(client, serverObject, options);
        if (options != null && options.isAllowSecureDataConversation()) {
            resourceSecureFieldsToClient(resultClient, serverObject, options);
        }
        return resultClient;
    }

    protected ClientType genericFieldsToClient(ClientType client, ResourceType serverObject, ToClientConversionOptions options) {
        final DateFormat dateTimeFormatter = getDateTimeFormat();
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        dateTimeFormatter.setTimeZone(TimeZoneContextHolder.getTimeZone());
        if (serverObject.getCreationDate() != null) {
            client.setCreationDate(dateTimeFormatter.format(serverObject.getCreationDate()));
        }
        client.setDescription(serverObject.getDescription());
        client.setLabel(serverObject.getLabel());
        if (serverObject.getUpdateDate() != null) {
            client.setUpdateDate(dateTimeFormatter.format(serverObject.getUpdateDate()));
        }
        client.setUri(serverObject.getURIString());

        if (options != null && options.isInMemoryResource()) {
            client.setVersion(null);
            client.setPermissionMask(null);
        } else {
            client.setVersion(serverObject.getVersion());
            client.setPermissionMask(permissionsService.getEffectivePermission(serverObject, authentication).getPermissionMask());
        }

        return client;
    }

    protected void resourceSecureFieldsToClient(ClientType client, ResourceType serverObject, ToClientConversionOptions options) {
    }

    protected abstract ClientType resourceSpecificFieldsToClient(ClientType client, ResourceType serverObject, ToClientConversionOptions options);

    protected ClientType getNewClientObjectInstance() {
        return clientTypeHelper.getNewClientObjectInstance();
    }

    // Client object class is extracted from real implementation class by reflection. So, cast is safe.
    @SuppressWarnings("unchecked")
    public Class<ClientType> getClientTypeClass() {
        return clientTypeHelper.getClientClass();
    }

    protected final ResourceReference findReference(List<ResourceReference> references, String uri) {
        ResourceReference result = null;
        if (references != null) {
            for (ResourceReference reference : references) {
                if (reference.getTargetURI().equals(uri)) {
                    result = reference;
                }
            }
        }
        return result;
    }

    public String getClientResourceType() {
        return clientTypeHelper.getClientResourceType();
    }
}

