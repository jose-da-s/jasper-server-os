/*
 * Copyright (C) 2025 the Jasper Server OS Authors
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

package com.jaspersoft.jasperserver.remote.handlers;

import com.jaspersoft.jasperserver.api.common.domain.impl.ExecutionContextImpl;
import com.jaspersoft.jasperserver.api.metadata.common.domain.FileResource;
import com.jaspersoft.jasperserver.api.metadata.common.domain.InputControl;
import com.jaspersoft.jasperserver.api.metadata.common.domain.Query;
import com.jaspersoft.jasperserver.api.metadata.common.domain.Resource;
import com.jaspersoft.jasperserver.api.metadata.common.domain.ResourceReference;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.ReportDataSource;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.ReportUnit;
import com.jaspersoft.jasperserver.api.metadata.xml.domain.impl.ResourceDescriptor;
import com.jaspersoft.jasperserver.remote.ResourceHandler;
import com.jaspersoft.jasperserver.remote.ServiceException;
import com.jaspersoft.jasperserver.remote.services.ResourcesManagementRemoteService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author gtoffoli
 * @version $Id$
 */
@Service("reportUnitHandler")
public class ReportUnitHandler extends RepositoryResourceHandler implements ResourceContainer{

    private static final Log log = LogFactory.getLog(ReportUnitHandler.class);
    public static final String OPTION_REPORT_UNIT_CONTENTS = "ReportUnitContents";

    public Class getResourceType() {
        return ReportUnit.class;
    }

    protected void doGet(Resource resource, ResourceDescriptor descriptor, Map options) throws ServiceException {
        ReportUnit reportUnit = (ReportUnit) resource;
        descriptor.setWsType(ResourceDescriptor.TYPE_REPORTUNIT);

        descriptor.setResourceProperty(ResourceDescriptor.PROP_RU_ALWAYS_PROPMT_CONTROLS,
                reportUnit.isAlwaysPromptControls());
        descriptor.setResourceProperty(ResourceDescriptor.PROP_RU_CONTROLS_LAYOUT,
                reportUnit.getControlsLayout());

        if (reportUnit.getInputControlRenderingView() != null) {
            descriptor.setResourceProperty(
                    ResourceDescriptor.PROP_RU_INPUTCONTROL_RENDERING_VIEW,
                    reportUnit.getInputControlRenderingView());
        }

        if (reportUnit.getReportRenderingView() != null) {
            descriptor.setResourceProperty(
                    ResourceDescriptor.PROP_RU_REPORT_RENDERING_VIEW,
                    reportUnit.getReportRenderingView());
        }

        Boolean reportUnitContent = options == null ? null
                : (Boolean) options.get(OPTION_REPORT_UNIT_CONTENTS);


        if (reportUnitContent != null && reportUnitContent.booleanValue()) {
            // Get datasource...
            ResourceReference ruDataSource = reportUnit.getDataSource();
            ResourcesManagementRemoteService resourcesManagementRemoteService = getResourcesManagementRemoteService();
            if (ruDataSource != null) {
                if (ruDataSource.isLocal()) {
                    ResourceDescriptor dsDescriptor =
                            resourcesManagementRemoteService.createResourceDescriptor(ruDataSource.getLocalResource());
                    descriptor.getChildren().add(dsDescriptor);
                } else {
                    ResourceDescriptor rdDs = new ResourceDescriptor();
                    rdDs.setWsType(ResourceDescriptor.TYPE_DATASOURCE);
                    rdDs.setReferenceUri(ruDataSource.getReferenceURI());
                    rdDs.setIsReference(true);
                    descriptor.getChildren().add(rdDs);
                }
            }

            ResourceReference ruQuery = reportUnit.getQuery();
            if (ruQuery != null) {
                if (ruQuery.isLocal()) {
                    ResourceDescriptor queryDescriptor =
                            resourcesManagementRemoteService.createResourceDescriptor(ruQuery.getLocalResource());
                    descriptor.getChildren().add(ruQuery);
                } else {
                    ResourceDescriptor rdDs = new ResourceDescriptor();
                    rdDs.setWsType(ResourceDescriptor.TYPE_QUERY);
                    rdDs.setReferenceUri(ruDataSource.getReferenceURI());
                    rdDs.setIsReference(true);
                    descriptor.getChildren().add(rdDs);
                }
            }

            // Get the main jrxml...
            ResourceReference ruMainReportResourceResourceReference = reportUnit.getMainReport();

            ResourceDescriptor ruMainReportResourceDescriptor = null;

            if (ruMainReportResourceResourceReference != null) {
                if (ruMainReportResourceResourceReference.isLocal()) {
                    ruMainReportResourceDescriptor =
                            resourcesManagementRemoteService.createResourceDescriptor(ruMainReportResourceResourceReference.getLocalResource());
                    // ruMainReportResourceDescriptor.setLocal(true);
                } else {
                    ruMainReportResourceDescriptor =
                            resourcesManagementRemoteService.createResourceDescriptor(resourcesManagementRemoteService.locateResource(ruMainReportResourceResourceReference.getReferenceURI()));
                    // This is a trick to say that this is really a reference,
                    // and the resource is not local!
                    ruMainReportResourceDescriptor.setReferenceUri(ruMainReportResourceResourceReference.getReferenceURI());
                    ruMainReportResourceDescriptor.setIsReference(true);
                    // ruMainReportResourceDescriptor.setLocal(false);
                }

                ruMainReportResourceDescriptor.setMainReport(true);
                descriptor.getChildren().add(ruMainReportResourceDescriptor);
            }

            List inputControls = reportUnit.getInputControls();
            if (inputControls != null && !inputControls.isEmpty()) {
                for (Iterator it = inputControls.iterator(); it.hasNext();) {

                    ResourceReference resRef = (ResourceReference) it.next();
                    ResourceDescriptor ruResourceDescriptor = null;
                    if (resRef.isLocal()) {
                        ruResourceDescriptor =
                                resourcesManagementRemoteService.createResourceDescriptor(resRef.getLocalResource());
                        // ruMainReportResourceDescriptor.setLocal(true);
                    } else {
                        ruResourceDescriptor =
                                resourcesManagementRemoteService.createResourceDescriptor(resourcesManagementRemoteService.locateResource(resRef.getReferenceURI(), ExecutionContextImpl.getRuntimeExecutionContext()));
                        // ruMainReportResourceDescriptor.setLocal(false);
                    }

                    if (ruResourceDescriptor != null) {
                        descriptor.getChildren().add(ruResourceDescriptor);
                    }
                }
            }

            List resources = reportUnit.getResources();
            if (resources != null && !resources.isEmpty()) {
                for (Iterator it = resources.iterator(); it.hasNext();) {

                    ResourceReference resRef = (ResourceReference) it.next();
                    ResourceDescriptor ruResourceDescriptor = null;
                    if (resRef.isLocal()) {
                        ruResourceDescriptor =
                                resourcesManagementRemoteService.createResourceDescriptor(resRef.getLocalResource());
                        // ruMainReportResourceDescriptor.setLocal(true);
                    } else {
                        ruResourceDescriptor =
                                resourcesManagementRemoteService.createResourceDescriptor(resourcesManagementRemoteService.locateResource(resRef.getReferenceURI()));
                        // ruMainReportResourceDescriptor.setLocal(false);
                    }

                    if (ruResourceDescriptor != null
                            && !ruMainReportResourceDescriptor.getUriString().equals(ruResourceDescriptor.getUriString())) {
                        descriptor.getChildren().add(ruResourceDescriptor);
                    }
                }
            }
        }
    }

    /**
     *
     * The updateResource method for the report unit just set basic options of the report unit and check for a datasource,
     * the main jrxml and if available the main query.
     *
     *
     * @param resource
     * @param descriptor
     * @param options
     * @throws ServiceException
     */
    @Override
    public void updateResource(Resource resource, ResourceDescriptor descriptor, Map options) throws ServiceException {

        super.updateResource(resource, descriptor, options);

        ReportUnit reportUnit = (ReportUnit) resource;

        Boolean alwaysPrompt = descriptor.getResourcePropertyValueAsBoolean(ResourceDescriptor.PROP_RU_ALWAYS_PROPMT_CONTROLS);
        if (alwaysPrompt != null) {
            reportUnit.setAlwaysPromptControls(alwaysPrompt.booleanValue());
        }

        Integer controlsLayout = descriptor.getResourcePropertyValueAsInteger(ResourceDescriptor.PROP_RU_CONTROLS_LAYOUT);
        if (controlsLayout != null) {
            reportUnit.setControlsLayout(controlsLayout.byteValue());
        }

        String controlsView = descriptor.getResourcePropertyValue(ResourceDescriptor.PROP_RU_INPUTCONTROL_RENDERING_VIEW);
        if (controlsView != null) {
            reportUnit.setInputControlRenderingView(controlsView);
        }

        String renderingView = descriptor.getResourcePropertyValue(ResourceDescriptor.PROP_RU_REPORT_RENDERING_VIEW);
        if (renderingView != null) {
            reportUnit.setReportRenderingView(renderingView);
        }


        List children = descriptor.getChildren();

        for (int i = 0; i < children.size() ; ++i) {
            ResourceDescriptor childResource = (ResourceDescriptor) children.get(i);

            if (isDataSource(childResource))
            {
                if (childResource.getIsReference()) {
                    reportUnit.setDataSourceReference(childResource.getReferenceUri());
                }
                else {

                    ReportDataSource dataSource = (ReportDataSource)createChildResource(childResource);
                    reportUnit.setDataSource(dataSource);
                }
            }

            else if (ResourceDescriptor.isFileType(childResource.getWsType()) || childResource.getWsType().equals(ResourceDescriptor.TYPE_REFERENCE))
            {
                if( childResource.isMainReport() )
                {
                    String referenceURI = childResource.getReferenceUri();
                    if (referenceURI != null && referenceURI.trim().length() > 0)
                    {
                        reportUnit.setMainReportReference(referenceURI);
                    }
                    else {
                        FileResource fileResource = (FileResource) createChildResource(childResource);
                        fileResource.setFileType(childResource.getWsType());

                        reportUnit.setMainReport(fileResource);
                    }
                }
                else {
                    FileResource fileResource = (FileResource) createChildResource(childResource);
                    reportUnit.addResource(fileResource);
                }
            }

            else if (childResource.getWsType().equals(ResourceDescriptor.TYPE_INPUT_CONTROL))
            {
                String referenceURI = childResource.getReferenceUri();
                if (referenceURI != null && referenceURI.trim().length() > 0)
                {
                    reportUnit.addInputControlReference(referenceURI);
                }
                else {
                    InputControl ic = (InputControl) createChildResource(childResource);
                    reportUnit.addInputControl(ic);
                }
            }


        }
    }

    /**
     * Create or Modify a sub resource of this report unit.
     * As resources, a report unit accepts only resources for which is provided an AbstractResourceHandler.
     *
     * @param parent
     * @param descriptor
     * @return
     * @throws ServiceException
     */
    public Resource addSubResource(Resource parent, ResourceDescriptor descriptor)  throws ServiceException {

        ReportUnit reportUnit = (ReportUnit) parent;

        Resource subResource = null;
        if (descriptor.getIsNew()) {

            String wsType = descriptor.getWsType();
            
            if (isDataSource(descriptor)) { // We assume here that the user will add this resource as primary locally defined data source.
                 
                 ReportDataSource dataSource = (ReportDataSource)createChildResource(descriptor);
                 reportUnit.setDataSource(dataSource);
                 subResource = dataSource;
                
            } else if (wsType.equals(ResourceDescriptor.TYPE_QUERY)) { // We assume here that the user will add this resource as primary locally defined query.

                Query queryResource = (Query) createChildResource(descriptor);
                reportUnit.setQuery(new ResourceReference(queryResource));
                subResource = queryResource;

            } else if (wsType.equals(ResourceDescriptor.TYPE_REFERENCE)
                    || wsType.equals(ResourceDescriptor.TYPE_IMAGE)
                    || wsType.equals(ResourceDescriptor.TYPE_FONT)
                    || wsType.equals(ResourceDescriptor.TYPE_CLASS_JAR)
                    || wsType.equals(ResourceDescriptor.TYPE_JRXML)
                    || wsType.equals(ResourceDescriptor.TYPE_RESOURCE_BUNDLE)
                    || wsType.equals(ResourceDescriptor.TYPE_STYLE_TEMPLATE)) {
                FileResource fileResource = (FileResource) createChildResource(descriptor);
                reportUnit.addResource(fileResource);

                subResource = fileResource;
            } else if (wsType.equals(ResourceDescriptor.TYPE_INPUT_CONTROL)) {
                if (descriptor.getIsReference()) {
                    String referenceUri = descriptor.getReferenceUri();
                    log.info("Adding control to RU: " + referenceUri);

                    InputControl inputControl = (InputControl)getRepository().getResource(null,
                            referenceUri, InputControl.class);
                    if (inputControl == null) {
                        throw new ServiceException(
                                ServiceException.RESOURCE_NOT_FOUND,
                                getMessageSource().getMessage(
                                        "webservices.error.resourceNotFoundOrInvalid",
                                        new Object[]{referenceUri}, LocaleContextHolder.getLocale()));
                    }

                    reportUnit.addInputControlReference(referenceUri);

                    subResource = inputControl;
                } else {
                    InputControl inputControl = (InputControl)createChildResource(descriptor);
                    reportUnit.addInputControl(inputControl);

                    subResource = inputControl;
                }
            } else {
                throw new ServiceException(
                        ServiceException.GENERAL_REQUEST_ERROR,
                        getMessageSource().getMessage(
                                "webservices.error.generic.not.supported",
                                new Object[]{}, LocaleContextHolder.getLocale()));
            }
        } else {
            // We assume the resource is a FileResource...
            Resource resource = getReportUnitResource(descriptor, reportUnit);

            if (resource == null) {
                throw new ServiceException(ServiceException.GENERAL_ERROR2,
                        "webservices.error.resourceNotFound");
            }

            ResourceHandler handler = getResourcesManagementRemoteService().getHandler(resource);
            if (handler instanceof AbstractResourceHandler)
            {
                ((AbstractResourceHandler)handler).updateResource(resource, descriptor, null);
            }

            // If this is the main report, set it correctly....
            if (descriptor.isMainReport() && reportUnit.getMainReport().isLocal()) {
                reportUnit.setMainReport((FileResource)resource);
            }

            subResource = resource;
        }

        return subResource;
    }

    /**
     * Look for a resource pointed by the ResourceDescriptor in a report unit.
     *
     * @param descriptor
     * @param reportUnit
     * @return
     */
    private Resource getReportUnitResource(ResourceDescriptor descriptor, ReportUnit reportUnit) {
        Resource resource = null;
        if (descriptor.isMainReport()) {
            ResourceReference resRef = reportUnit.getMainReport();
            Resource jrxmlResource = null;
            if (resRef.isLocal()) {
                jrxmlResource = resRef.getLocalResource();
            } else {
                jrxmlResource = getRepository().getResource(
                        null, resRef.getReferenceURI());
            }

            resource = jrxmlResource;
        } else {
            List resources = reportUnit.getResources();
            for (int i = 0; i < resources.size(); ++i) {
                ResourceReference resRef = (ResourceReference) resources.get(i);
                Resource genericResource = null;
                if (resRef.isLocal()) {
                    genericResource = resRef.getLocalResource();
                } else {
                    genericResource = getRepository().getResource(null, resRef.getReferenceURI());
                }
                if (genericResource.getURIString().equals(
                        descriptor.getUriString())) {
                    resource = genericResource;
                    break;
                }
            }
        }

        return resource;
    }

    public void deleteSubResource(Resource parent, ResourceDescriptor childDescriptor) throws ServiceException {
        ReportUnit reportUnit = (ReportUnit) parent;

        if (childDescriptor.isMainReport()) {
            throw new ServiceException(ServiceException.GENERAL_ERROR2, getMessageSource().getMessage("webservices.error.deletingMainJrxml", new Object[]{}, LocaleContextHolder.getLocale()));
        }

        // if this is the main query, remove it.
        if (isMainQuery(childDescriptor)) {

            if (log.isDebugEnabled()) {
                log.debug("Delete: removed ReportUnit query");
            }

            reportUnit.setQuery((ResourceReference)null);
        }

        // If this is the main datasource, remove it
        if (isDataSource(childDescriptor)) {

            if (reportUnit.getDataSource().getTargetURI() != null &&
                reportUnit.getDataSource().getTargetURI().equals(childDescriptor.getUriString()))
            {
                if (log.isDebugEnabled()) {
                    log.debug("Delete: removed ReportUnit datasource");
                }
                reportUnit.setDataSource((ResourceReference)null);
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("Delete: resource in reportUnit: " + childDescriptor.getWsType());
        }

        if (childDescriptor.getWsType().equals(
                ResourceDescriptor.TYPE_INPUT_CONTROL)) {

            List resources = reportUnit.getInputControls();
            for (int i = 0; i < resources.size(); ++i) {
                ResourceReference resRef = (ResourceReference) resources.get(i);
                if (resRef.isLocal()
                        && childDescriptor.getName().equals(
                        resRef.getLocalResource().getName())) {
                    reportUnit.removeInputControl(i);
                    break;
                } else if (!resRef.isLocal()
                        && childDescriptor.getUriString().equals(resRef.getReferenceURI())) {
                    reportUnit.removeInputControl(i);
                    break;
                }
            }
        } else {
            List resources = reportUnit.getResources();
            for (int i = 0; i < resources.size(); ++i) {
                ResourceReference resRef = (ResourceReference) resources.get(i);
                if (resRef.isLocal()
                        && childDescriptor.getName().equals(
                        resRef.getLocalResource().getName())) {
                    reportUnit.removeResourceLocal(resRef.getLocalResource().getName());
                    break;
                } else if (!resRef.isLocal()
                        && childDescriptor.getUriString().equals(
                        resRef.getReferenceURI())) {
                    reportUnit.removeResourceReference(childDescriptor.getUriString());
                    break;
                }
            }
        }

        //serviceContext.getRepository().saveResource(null, reportUnit);
    }



    /**
     * TODO: this method should go in the ResourceDescriptor class.
     *
     *
     * @param descriptor
     * @return
     */
    public boolean isMainQuery(ResourceDescriptor descriptor) {

            // TODO: put the PROP_RU_IS_MAIN_QUERY in a static field.
            String s = descriptor.getResourcePropertyValue( "PROP_RU_IS_MAIN_QUERY" );
            if (s != null) return s.equals("true");
            return false;
    }
}
