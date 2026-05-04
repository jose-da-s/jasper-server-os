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
package com.jaspersoft.jasperserver.jaxrs.jdbcdrivers;

import com.jaspersoft.jasperserver.api.common.properties.PropertiesManagementService;
import com.jaspersoft.jasperserver.api.common.service.JdbcDriverService;
import com.jaspersoft.jasperserver.api.metadata.user.domain.Role;
import com.jaspersoft.jasperserver.api.metadata.user.domain.User;
import com.jaspersoft.jasperserver.dto.jdbcdrivers.JdbcDriverInfo;
import com.jaspersoft.jasperserver.dto.resources.ClientProperty;
import com.jaspersoft.jasperserver.jaxrs.poc.hypermedia.common.Relation;
import com.jaspersoft.jasperserver.jaxrs.poc.hypermedia.common.representation.HypermediaRepresentation;
import com.jaspersoft.jasperserver.jaxrs.poc.hypermedia.common.representation.Link;
import com.jaspersoft.jasperserver.remote.exception.IllegalParameterValueException;
import com.jaspersoft.jasperserver.api.ErrorDescriptorException;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.*;

/**
 * <p></p>
 *
 * @author yaroslav.kovalchyk
 * @version $Id$
 */
@Service
@Path("/jdbcDrivers")
@Scope("prototype")
public class JdbcDriversJaxrsService {
    @Resource
    private JdbcDriverService jdbcDriverService;

    @Value("${isProVersion:false}")
    private boolean isProVersion;
    @Resource
    protected Map<String, Map<String, Object>> jdbcConnectionMap;
    @Resource
    protected List<String> configurationAllowedRoles;
    @Resource(name="propertiesManagementService")
    protected PropertiesManagementService propertiesManagementService;

    private final static String ALLOW_JAR_FILE_UPLOAD = "allowJarFileUpload";

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public List<JdbcDriverInfo> getJdbcDrivers() {
        List<JdbcDriverInfo> jdbcDriverInfos = new ArrayList<JdbcDriverInfo>(jdbcConnectionMap.size());
        final Set<String> registeredDriverClassNames = new HashSet<String>(jdbcDriverService.getRegisteredDriverClassNames());

        for (Map.Entry<String, Map<String, Object>> entry : jdbcConnectionMap.entrySet()) {
            JdbcDriverInfo info = new JdbcDriverInfo().setName(entry.getKey());
            jdbcDriverInfos.add(info);
            final Map<String, Object> value = entry.getValue();
            final String jdbcDriverClass = (String) value.get("jdbcDriverClass");
            final String isDefault = (String) value.get("default");
            info.setAvailable(jdbcDriverService.isRegistered(jdbcDriverClass))
                    .setJdbcDriverClass(jdbcDriverClass)
                    .setDefault(isDefault != null ? Boolean.valueOf(isDefault) : null)
                    .setJdbcUrl((String) value.get("jdbcUrl")).setLabel((String) value.get("label"))
                    .setAllowSpacesInDbName("true".equals(value.get("allowSpacesInDbName")));
            // XML configuration assures cast safety
            @SuppressWarnings("unchecked")
            final Map<String, String> defaultValues = (Map<String, String>) value.get("defaultValues");
            if (defaultValues != null) {
                List<ClientProperty> defaultProperties = new ArrayList<ClientProperty>(defaultValues.size());
                info.setDefaultValues(defaultProperties);
                for (String currentProperty : defaultValues.keySet()) {
                    defaultProperties.add(new ClientProperty(currentProperty, defaultValues.get(currentProperty)));
                }
            }
            registeredDriverClassNames.remove(jdbcDriverClass);
        }
        if (!registeredDriverClassNames.isEmpty()) {
            for (String currentClass : registeredDriverClassNames) {
                jdbcDriverInfos.add(new JdbcDriverInfo().setName(currentClass).setJdbcDriverClass(currentClass).setLabel(currentClass));
            }
        }
        return jdbcDriverInfos;
    }

    @GET
    @Produces("application/hal+json")
    public HypermediaRepresentation getJdbcDriversHipermediaRepresentation() {
        final List<JdbcDriverInfo> jdbcDrivers = getJdbcDrivers();
        final HypermediaRepresentation hypermediaRepresentation = new HypermediaRepresentation() {
            public List<JdbcDriverInfo> getJdbcDrivers() {
                return jdbcDrivers;
            }
        };
        Authentication existingAuth = SecurityContextHolder.getContext().getAuthentication();
        Set<Role> roles = ((User) existingAuth.getPrincipal()).getRoles();
        for (Role currentRole : roles) {
            String generalSettingProperty = propertiesManagementService.getProperty("general." + ALLOW_JAR_FILE_UPLOAD);
            Boolean allowJARUpload = Optional.ofNullable(generalSettingProperty).map(Boolean::valueOf).orElse(!isProVersion);
            if (configurationAllowedRoles.contains(currentRole.getRoleName()) && allowJARUpload ) {
                hypermediaRepresentation.addLink(new Link()
                        .setRelation(Relation.create)
                        .setProfile("POST"));
                hypermediaRepresentation.addLink(new Link()
                        .setRelation(Relation.edit)
                        .setProfile("PUT"));
            }
        }
        return hypermediaRepresentation;
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public JdbcDriverInfo uploadDriver(@FormDataParam("className") String className, FormDataMultiPart multiPart) {
        Map<String, byte[]> driverFilesData = new HashMap<String, byte[]>();
        for (String currentKey : multiPart.getFields().keySet()) {
            if ("className".equals(currentKey)) continue;
            List<FormDataBodyPart> currentPartsList = multiPart.getFields(currentKey);
            if (currentPartsList != null && !currentPartsList.isEmpty()) {
                final FormDataBodyPart formDataBodyPart = currentPartsList.get(0);
                final String fileName = formDataBodyPart.getContentDisposition().getFileName();
                final byte[] jarContent = formDataBodyPart.getEntityAs(byte[].class);
                if (fileName != null && !"".equals(fileName) && jarContent != null && jarContent.length > 0) {
                    driverFilesData.put(fileName, jarContent);
                }
            }
        }

        try {
            jdbcDriverService.setDriver(className, driverFilesData);
        } catch (NoClassDefFoundError e) {
            throw new IllegalParameterValueException("className", className);
        } catch (ClassNotFoundException e) {
            throw new IllegalParameterValueException("className", className);
        } catch (Exception e) {
            throw new ErrorDescriptorException(e.getMessage(), e);
        }
        List<JdbcDriverInfo> infos = getJdbcDrivers();
        JdbcDriverInfo result = null;
        for (JdbcDriverInfo info : infos) {
            if (className.equals(info.getJdbcDriverClass())) {
                result = info;
                break;
            }
        }

        if (result == null) {
            throw new IllegalStateException("JdbcDriverInfo for class " + className + " isn't found");
        }

        return result;
    }
}

