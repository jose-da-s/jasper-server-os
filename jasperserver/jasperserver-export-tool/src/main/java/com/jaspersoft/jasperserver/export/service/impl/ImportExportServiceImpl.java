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

package com.jaspersoft.jasperserver.export.service.impl;


import com.jaspersoft.jasperserver.api.JSException;
import com.jaspersoft.jasperserver.api.common.crypto.DiagnosticDataCipherer;
import com.jaspersoft.jasperserver.api.common.crypto.Hexer;
import com.jaspersoft.jasperserver.api.common.domain.impl.ExecutionContextImpl;
import com.jaspersoft.jasperserver.api.metadata.user.domain.Role;
import com.jaspersoft.jasperserver.api.metadata.user.domain.User;
import com.jaspersoft.jasperserver.api.metadata.user.service.ObjectPermissionService;
import com.jaspersoft.jasperserver.api.metadata.user.service.TenantService;
import com.jaspersoft.jasperserver.api.security.encryption.PlainCipher;
import com.jaspersoft.jasperserver.crypto.JrsKeystore;
import com.jaspersoft.jasperserver.crypto.KeyProperties;
import com.jaspersoft.jasperserver.crypto.KeystoreManager;
import com.jaspersoft.jasperserver.dto.common.BrokenDependenciesStrategy;
import com.jaspersoft.jasperserver.dto.common.WarningDescriptor;
import com.jaspersoft.jasperserver.export.ExportTaskImpl;
import com.jaspersoft.jasperserver.export.Exporter;
import com.jaspersoft.jasperserver.export.ImportInputMetadata;
import com.jaspersoft.jasperserver.export.ImportTaskImpl;
import com.jaspersoft.jasperserver.export.Importer;
import com.jaspersoft.jasperserver.export.Parameters;
import com.jaspersoft.jasperserver.export.ParametersImpl;
import com.jaspersoft.jasperserver.export.io.PathProcessor;
import com.jaspersoft.jasperserver.export.io.PathProcessorFactory;
import com.jaspersoft.jasperserver.export.io.ZipFileInput;
import com.jaspersoft.jasperserver.export.io.ZipFileInputManager;
import com.jaspersoft.jasperserver.export.io.ZipStreamOutput;
import com.jaspersoft.jasperserver.export.service.ExportFailedException;
import com.jaspersoft.jasperserver.export.service.ImportExportService;
import com.jaspersoft.jasperserver.export.service.ImportFailedException;
import com.jaspersoft.jasperserver.export.util.CommandOut;
import com.jaspersoft.jasperserver.export.util.EncryptionParams;
import org.apache.commons.lang3.StringUtils;
import org.owasp.esapi.codecs.Hex;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.MessageSource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import jakarta.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Key;
import java.text.MessageFormat;
import java.util.*;
import java.util.zip.Deflater;
import java.util.zip.ZipException;

import static java.lang.String.format;

/**
 * Implementation of facade on top of import/export tool
 *
 * @author ztomchenco
 */
public abstract class ImportExportServiceImpl implements ImportExportService, ZipFileInputManager, ApplicationContextAware {

    public final static String ROLES_PARAMETER = "roles";
    public final static String USERS_PARAMETER = "users";
    public final static String ORGANIZATION = "organization";
    public final static String RESOURCE_TYPES = "resource-types";
    public final static String SKIP_DEPENDENT_RESOURCES = "skip-dependent-resources";
    public final static String SKIP_FAVORITE_RESOURCES = "skip-favorite-resources";
    public static final String SKIP_SUBORGANIZATIONS = "skip-suborganizations";
    public final static String URIS_PARAMETER = "uris";
    public final static String URIS_OF_SCHEDULED_PARAMETER = "report-jobs";

    protected final static CommandOut log = CommandOut.getInstance();

    @Resource(name = "messageSource")
    private MessageSource messageSource;

    @Autowired
    private KeystoreManager keystoreManager;

    private  JrsKeystore keystore;
    /**
     * @see ImportExportServiceImpl#setDiagnosticDataCipherer(PlainCipher)
     */
    private DiagnosticDataCipherer diagnosticDataCipherer;

    private PathProcessorFactory pathProcessorFactory;
    private String propertyPathProcessorId = "pathProcessorId"; // default value
    private ApplicationContext applicationContext;

    @Resource(name = "importExportPrivilegeRoles")
    protected Set<String> importExportPrivilegeRoles;

    @Resource(name = "exportPrivilegeOptions")
    private Set<String> exportPrivilegeOptions;

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Resource(name = "diagnosticDataCipherer")
    public void setDiagnosticDataCipherer(PlainCipher cipherer) {
        if (cipherer instanceof DiagnosticDataCipherer) {
            this.diagnosticDataCipherer = (DiagnosticDataCipherer) cipherer;
        } else {
            throw new BeanCreationException(
                    format("Invalid cipher implementation was provided. Expecting %"
                            , DiagnosticDataCipherer.class.getCanonicalName()));
        }
    }

    @Override
    //TODO: Thinking about having Map<String, String>
    public void doImport(File input, Map<String, String> importParams, String organizationId,
                         String brokenDependenciesStrategy, Locale locale, List<WarningDescriptor> warnings) throws ImportFailedException{
        Importer importer = this.getImporter();

        String processorId = pathProcessorFactory.getDefaultInputProcessor();
        PathProcessor processor = pathProcessorFactory.getProcessor(processorId);

        ImportTaskImpl task = new ImportTaskImpl();
        task.setWarnings(warnings);
        task.setExecutionContext(makeExecutionContext(locale));
        task.setParameters(createImportParameters(importParams)
                .setParameterValue(ORGANIZATION, organizationId)
                .setParameterValue(BROKEN_DEPENDENCIES, brokenDependenciesStrategy));
        Boolean isEncrypted = importParams != null ? Boolean.parseBoolean(importParams.get("isEncrypted")) : null;
        if (isEncrypted != null && isEncrypted) {
            // Decrypt the file and put it into Importer
            // input = decrypted zip
            File decryptedInput = null;
            try {
                String key = null;
                if (importParams.containsKey(SECRET_KEY)) {
                    key = importParams.get(SECRET_KEY);

                } else if(importParams.containsKey(EncryptionParams.KEY_ALIAS_PARAMETER) ) {
                    final String alias = importParams.get(EncryptionParams.KEY_ALIAS_PARAMETER);
                    if (keystore == null) {
                        keystore = keystoreManager.getKeystore(null);
                    }
                    final KeyProperties props = keystore.getKeyProperties(alias);
                    if (props.getKeyAlias() != null) {
                        final Key sKey = keystore.getKey(new KeyProperties(props.getKeyAlias(), props.getKeyPasswd() != null ? props.getKeyPasswd() : ""));
                        if (sKey != null) {
                            key = Hexer.stringify(sKey.getEncoded());
                        }
                    }
                }

                decryptedInput = diagnosticDataCipherer.decryptExportZip(input, key);
            } catch (Exception e) {
                log.error("Error while decrypting export.zip", e);
                throw new ImportFailedException(e.getMessage());
            }
            task.setInput(new ZipFileInput(decryptedInput, processor, this));
        } else {
            task.setInput(new ZipFileInput(input, processor, this));
        }
        task.setApplicationContext(getApplicationContext());

        startImport(importer, task);
    }

    @Override
    public void doImport(InputStream input, Map<String, String> importParams, String organizationId,
                         String brokenDependenciesStrategy, Locale locale, List<WarningDescriptor> warnings)
                         throws ImportFailedException {
        File tempFile = null;
        try {
            tempFile = createTmpFile(input);
            log.debug("Upload finished. Temp file created at " + tempFile.getAbsolutePath());
            doImport(tempFile, importParams, organizationId, brokenDependenciesStrategy, locale, warnings);
        }catch (IOException  e){
            throw new ImportFailedException(e.getMessage());
        } finally {
            if (tempFile != null){
                if (tempFile.delete()){
                    log.debug("Uploaded file was deleted from: "+ tempFile.getAbsolutePath());
                }else{
                    log.debug("Can't delete temp dir from:"+ tempFile.getAbsolutePath());
                }
            }
        }
    }

    @Override
    public void doExport(OutputStream output, Map<String, String> exportParams, List<String> urisOfResources,
                         List<String> urisOfScheduledJobs, List<String> rolesToExport, List<String> usersToExport,
                         List<String> exportResourceTypes, String organizationId, Locale locale,
                         List<WarningDescriptor> warnings) throws ExportFailedException {

        Exporter exporter = this.getExporter();

        ExportTaskImpl task = new ExportTaskImpl();

        filterOptions(exportParams, organizationId);

        task.setExecutionContext(makeExecutionContext(locale));
        task.setParameters(createExportParameters(exportParams, urisOfResources, urisOfScheduledJobs, rolesToExport,
                usersToExport, exportResourceTypes, organizationId));
        String processorId = pathProcessorFactory.getDefaultOutputProcessor();
        PathProcessor pathProcessor = pathProcessorFactory.getProcessor(processorId);
        Properties properties = new Properties();
        properties.setProperty(propertyPathProcessorId, processorId);
        task.setOutput(new ZipStreamOutput(output, Deflater.BEST_COMPRESSION, pathProcessor, properties));
        task.setWarnings(warnings);
        startExport(exporter, task);
    }

    private void filterOptions(Map<String, String> exportParams, String orgId) {
        if (!hasCurrentUserPrivileges() && exportParams != null) {
            for (String option : exportPrivilegeOptions) {
                log.warn("Option --" + option + " is not accessible for current user");
                exportParams.remove(option);
            }
        }
    }

    @Override
    public void updateInputProperties(ZipFileInput input, Properties properties) {
        String pathProcessorId = properties.getProperty(propertyPathProcessorId);
        if (pathProcessorId != null && !pathProcessorId.equals(pathProcessorFactory.getDefaultInputProcessor())) {
            PathProcessor processor = pathProcessorFactory.getProcessor(pathProcessorId);
            input.setPathProcessor(processor);
        }
    }

    protected void startImport(Importer importer, ImportTaskImpl task) throws ImportFailedException {
        try{
            log.info("About to start import process");
            importer.setTask(task);
            checkInputFileMetadata(task);
            importer.performImport();
            log.info("Import process completed successfully");
        }catch (Exception e){
            //all exceptions in import/export tool wrapperd by JSException, decompose it
            if (e.getCause() instanceof ZipException) {
                String message = localize("exception.remote.import.failed.zip.error", task.getExecutionContext().getLocale());
                throw new ImportFailedException(message, "import.failed.zip.error", null);
            } else if (e instanceof ImportFailedException) {
                ImportFailedException exception = (ImportFailedException) e;
                throw new ImportFailedException(
                        localize(exception.getErrorCode(), task.getExecutionContext().getLocale()),
                        exception.getErrorCode(),
                        exception.getParameters()
                );
            } else if (e instanceof NullPointerException) {
                //Import export tool throws null pointer exceptions when content hasn't proper structure
                String message = localize("exception.remote.import.failed.content.error", task.getExecutionContext().getLocale());
                throw new ImportFailedException(message, "import.failed.content.error", null);
            } else {
                String message = MessageFormat.format(
                        localize("exception.remote.import.failed", task.getExecutionContext().getLocale()),e.getMessage());
                throw new ImportFailedException(message, "import.failed", new String[]{e.getMessage()});
            }
        }
    }
    //Todo: thinking about move to base import-export validator.
    private void checkInputFileMetadata(ImportTaskImpl task) throws ImportFailedException {
        ImportInputMetadata fileMetadata = task.getInputMetadata();

        //Root Tenant from import input
        String sourceTenantId = fileMetadata.getProperty(ImportExportService.ROOT_TENANT_ID);
        //Tenant id we import into
        String destinationTenantId = task.getParameters().getParameterValue(ImportExportServiceImpl.ORGANIZATION);

        if (StringUtils.isNotBlank(destinationTenantId) && !destinationTenantId.equals(TenantService.ORGANIZATIONS)) {
            if (StringUtils.isBlank(sourceTenantId) || sourceTenantId.equals(TenantService.ORGANIZATIONS)) {
                throw new ImportFailedException(
                        ImportExportService.ERROR_CODE_IMPORT_ROOT_INTO_ORGANIZATION,
                        new String[] {destinationTenantId});
            }

            // it is tenant merging // check for tenant ids
            if (!sourceTenantId.equals(destinationTenantId)) {
                if (!task.getParameters().hasParameter(ImportExportService.MERGE_ORGANIZATION)) {
                    throw new ImportFailedException(
                            format("Organization ids do not match. You are merging %s into %s",
                                    sourceTenantId, destinationTenantId),
                            ImportExportService.ERROR_CODE_IMPORT_TENANTS_NOT_MATCH,
                            new String[] {sourceTenantId}
                    );
                }
            }
        } else if (StringUtils.isNotBlank(sourceTenantId) && !sourceTenantId.equals(TenantService.ORGANIZATIONS)) {
            throw new ImportFailedException(
                    ImportExportService.ERROR_CODE_IMPORT_ORGANIZATION_INTO_ROOT,
                    new String[] {sourceTenantId});
        }

        BrokenDependenciesStrategy dependenciesStrategy =
                BrokenDependenciesStrategy.parseString(
                        task.getParameters().getParameterValue(ImportExportService.BROKEN_DEPENDENCIES));

        // check for broken dependencies
        Set<String> brokenDependencies = fileMetadata.getBrokenDependencies();
        if (brokenDependencies != null && !brokenDependencies.isEmpty()) {
            if (BrokenDependenciesStrategy.FAIL.equals(dependenciesStrategy)) {
                throw new ImportFailedException(
                        localize(ImportExportService.ERROR_CODE_IMPORT_BROKEN_DEPENDENCIES,
                                task.getExecutionContext().getLocale()),
                        ImportExportService.ERROR_CODE_IMPORT_BROKEN_DEPENDENCIES,
                        brokenDependencies.toArray(new String[brokenDependencies.size()])
                );
            }
        }
    }

    protected void startExport(Exporter exporter, ExportTaskImpl task) throws ExportFailedException {
        try{
            log.info("About to start export process");
            exporter.setTask(task);
            exporter.performExport();
            log.info("Export process completed successfully");
        }catch (JSException e){
            //convert from unchecked to checked exception
            throw new ExportFailedException(e.getLocalizedMessage());
        }
    }

    private Parameters createExportParameters(Map<String, String> exportParams, List<String> urisOfResources,
                                              List<String> urisOfScheduledJobs, List<String> rolesToExport,
                                              List<String> usersToExport, List<String> exportResourceTypes,
                                              String organizationId){
        Parameters parameters = new ParametersImpl();

        processInputList(parameters, URIS_PARAMETER, urisOfResources);
        processInputList(parameters, URIS_OF_SCHEDULED_PARAMETER, urisOfScheduledJobs);
        processInputList(parameters, ROLES_PARAMETER, rolesToExport);
        processInputList(parameters, USERS_PARAMETER, usersToExport);
        processInputList(parameters, RESOURCE_TYPES, exportResourceTypes);
        processInputMap(parameters, exportParams);
        if (organizationId != null) {
            parameters.setParameterValue(ORGANIZATION, organizationId);
        }

        return parameters;
    }

    private Parameters createImportParameters(Map<String, String> importParams){
        Parameters parameters = new ParametersImpl();

        processInputMap(parameters, importParams);

        return parameters;
    }

    private void processInputMap(Parameters parameters, Map<String, String>  map){
        if (map != null) {
            for (String param: map.keySet()) {
                final String value = map.get(param);
                if ((value != null) && value.equalsIgnoreCase("true")) {
                    parameters.addParameter(param);
                } else if ((value != null) && !value.equalsIgnoreCase("false")) {
                    parameters.setParameterValue(param, value);
                }
            }
        }
    }

    private void processInputList(Parameters parameters, String parameter, List<String> list) {
        if (list != null) {
            if (list.size() > 0) {
                parameters.addParameterValues(parameter, list.toArray(new String[list.size()]));
            } else {
                parameters.addParameter(parameter);
            }
        }
    }

    private File createTmpFile(InputStream input) throws IOException{
        File tmp = File.createTempFile("tmp"+input.hashCode(), null);

        FileOutputStream fileStream = new FileOutputStream(tmp);

        byte[] buff = new byte[512];
        int read = input.read(buff);
        while (read > 0){
            fileStream.write(buff, 0, read);
            read = input.read(buff);
        }
        fileStream.flush();
        fileStream.close();

        return tmp;
    }

    private ExecutionContextImpl makeExecutionContext(Locale locale){
        ExecutionContextImpl context = new ExecutionContextImpl();
        context.setLocale(locale);
        if (hasCurrentUserPrivileges()) {
            context.getAttributes().add(ObjectPermissionService.PRIVILEGED_OPERATION);

        }

        return context;
    }

    protected boolean hasCurrentUserPrivileges() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user;

        if (authentication != null) {
            user = (User) authentication.getPrincipal();
        } else {
            return false;
        }

        //only root users, like jasperadmin in Ce and superuser in Pro should have privilege.
        if (user.getTenantId() != null && !user.getTenantId().equals(TenantService.ORGANIZATIONS)) {
            return false;
        }

        for (Object role: user.getRoles()){
            if (role instanceof Role) {
                Role r = (Role) role;
                if (r.getTenantId() == null && importExportPrivilegeRoles.contains(r.getRoleName())) {
                    return true;
                }
            }
        }

        return false;
    }

    private String localize(String key, Locale locale){
        return messageSource.getMessage(key, null, key, locale);
    }

    public abstract Exporter getExporter();

    public abstract Importer getImporter();

    public PathProcessorFactory getPathProcessorFactory() {
        return pathProcessorFactory;
    }

    public void setPathProcessorFactory(PathProcessorFactory pathProcessorFactory) {
        this.pathProcessorFactory = pathProcessorFactory;
    }

    public String getPropertyPathProcessorId() {
        return propertyPathProcessorId;
    }

    public void setPropertyPathProcessorId(String propertyPathProcessorId) {
        this.propertyPathProcessorId = propertyPathProcessorId;
    }

    public MessageSource getMessageSource() {
        return messageSource;
    }

}

