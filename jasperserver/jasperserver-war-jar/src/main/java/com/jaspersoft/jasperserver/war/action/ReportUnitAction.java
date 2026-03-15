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

package com.jaspersoft.jasperserver.war.action;

import com.jaspersoft.jasperserver.api.JSDuplicateResourceException;
import com.jaspersoft.jasperserver.api.JSException;
import com.jaspersoft.jasperserver.api.JSExceptionWrapper;
import com.jaspersoft.jasperserver.api.JSNotImplementedException;
import com.jaspersoft.jasperserver.api.common.domain.ValidationResult;
import com.jaspersoft.jasperserver.api.common.util.StaticExecutionContextProvider;
import com.jaspersoft.jasperserver.api.engine.common.service.EngineService;
import com.jaspersoft.jasperserver.api.engine.common.service.SecurityContextProvider;
import com.jaspersoft.jasperserver.api.engine.jasperreports.service.impl.CustomReportDataSourceServiceFactory;
import com.jaspersoft.jasperserver.api.engine.jasperreports.util.CustomDataSourceDefinition;
import com.jaspersoft.jasperserver.api.metadata.common.domain.*;
import com.jaspersoft.jasperserver.api.metadata.common.domain.client.FileResourceImpl;
import com.jaspersoft.jasperserver.api.metadata.common.domain.client.InputControlImpl;
import com.jaspersoft.jasperserver.api.metadata.common.service.RepositoryService;
import com.jaspersoft.jasperserver.api.metadata.common.service.impl.RepositorySecurityChecker;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.*;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.helper.DataSourceJsonHelper;
import com.jaspersoft.jasperserver.api.metadata.user.service.ObjectPermissionService;
import com.jaspersoft.jasperserver.api.metadata.view.domain.FilterCriteria;
import com.jaspersoft.jasperserver.war.common.JasperServerConst;
import com.jaspersoft.jasperserver.war.common.JasperServerConstImpl;
import com.jaspersoft.jasperserver.war.dto.*;
import com.jaspersoft.jasperserver.war.model.impl.BaseTreeDataProvider;
import com.jaspersoft.jasperserver.war.model.impl.TypedTreeDataProvider;
import com.jaspersoft.jasperserver.war.repository.JRXMLRepository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.DataBinder;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.web.multipart.support.ByteArrayMultipartFileEditor;
import org.springframework.webflow.action.FormAction;
import org.springframework.webflow.core.collection.ParameterMap;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.ScopeType;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class ReportUnitAction extends FormAction {
    private static final String ATTRIBUTE_RESOURCE_ID_NOT_SUPPORTED_SYMBOLS = "resourceIdNotSupportedSymbols";

	private static final String DATA_SOURCE_PARENT_TYPE = "reportUnit";
	private static final String RU_URI_PARAM = "resource";

	private static final String FORM_OBJECT_KEY = "wrapper";

	private static final String REPORT_UNIT_SUFFIX = "_unit";

	private static final String REPORT_UNIT_KEY = "unit";

	private static final String CONTROL_OBJECT_KEY = "control";

	private static final String RESOURCE_OBJECT_KEY = "resource";

	private static final String DATASOURCE_OBJECT_KEY = "dataResource";

	private static final String PARENT_FOLDER_ATTR = "parentFolder";

	private static final String PARAM_FROM_PAGE = "frompage";

	private static final String PARAM_RESOURCE_NAME = "resourceName";
	
	private static final String UNPARSABLE_JRXML_ATTR = "jrxmlUnparsable";
	
	private static final String UNPARSABLE_JRXML_MESSAGE_KEY = "jsp.jrXmlUploadForm.jrxmlUnparsable";
	
	private static final String LOCAL_JRXML_SUFFIX = "_";

    private static final String JRXML_TREE_DATA_PROVIDER = "jrxmlTreeDataProvider";

	private static final String QUERY_TREE_DATA_PROVIDER = "queryTreeDataProvider";

    private static final String INPUT_CONTROL_RESOURCE_TREE_DATA_PROVIDER = "inputControlResourceTreeDataProvider";

    private static final String DATA_SOURCE_TREE_DATA_PROVIDER = "dsTreeDataProvider";

    public static final String JRXML_FILE_RESOURCE_ALREADY_UPLOADED = "jrxmlFileResourceAlreadyUploaded";

    // Request parameters.
    private static final String REQUEST_PARAM_SELECTED_RESOURCE = "selectedResource";

    protected final Log log = LogFactory.getLog(this.getClass());

	private RepositoryService repository;

	private JRXMLRepository jrxmlRepository;

    protected RepositorySecurityChecker securityChecker;

    protected ObjectPermissionService permissionService;

	private EngineService engine;

	private RepositoryConfiguration configuration;

	private JasperServerConstImpl constants = new JasperServerConstImpl();
	
	private String queryReferenceReqAttrName;
	private String queryReferenceOutpuAttrName;
	
	private String reportQueryLanguageFlowAttribute;

    private static final String ATTRIBUTE_ORGANIZATION_ID = "organizationId";
    private static final String ATTRIBUTE_PUBLIC_FOLDER_URI = "publicFolderUri";
    private SecurityContextProvider securityContextProvider;

    protected MessageSource messages;

    private BaseTreeDataProvider jrxmlTreeDataProvider;
    private TypedTreeDataProvider queryTreeDataProvider;
    private TypedTreeDataProvider inputControlResourceTreeDataProvider;
    private TypedTreeDataProvider dataSourceTreeDataProvider;
    private CustomReportDataSourceServiceFactory customDataSourceFactory;

    @Autowired
    private DataSourceJsonHelper dataSourceJsonHelper;

    public ReportUnitAction() {
		setFormObjectClass(ReportUnitWrapper.class);
		setFormObjectName(FORM_OBJECT_KEY);
		setFormObjectScope(ScopeType.FLOW);
	}

    public void setCustomDataSourceFactory(CustomReportDataSourceServiceFactory customDataSourceFactory) {
        this.customDataSourceFactory = customDataSourceFactory;
    }

    public void setMessages(MessageSource messages) {
        this.messages = messages;
    }

    public void setSecurityChecker(RepositorySecurityChecker securityChecker) {
        this.securityChecker = securityChecker;
    }

    public void setPermissionService(ObjectPermissionService permissionService) {
        this.permissionService = permissionService;
    }

    public Event checkPermissions(RequestContext context) throws Exception {
        String folderUri = getParentFolderUri(context);
        String resourceUri = context.getRequestParameters().get(REQUEST_PARAM_SELECTED_RESOURCE);

        if(folderUri != null && folderUri.length() > 0) {
            Folder folder = repository.getFolder(null, folderUri);

            Resource resource = null;
            if (resourceUri != null && resourceUri.length() > 0) {
                resource = repository.getResource(null, resourceUri);
            }

            // Case 1: folder exists, is editable and no resource selected (new report is added).
            if ((folder != null && resourceUri == null && securityChecker.isEditable(folder)) ||
                    // Case 2: folder exists, resource exists and is editable (existing resource is being edited).
                    (folder != null && resource != null && securityChecker.isEditable(resource))) {
                return yes();
            } else {
                return no();
            }
        } else {
            throw new AccessDeniedException("Access is denied");
        }
    }

	public Event initAction(RequestContext context) throws Exception {
		ReportUnitWrapper wrapper = getFormReportUnitWrapper(context);

        wrapper.setAnyJrxmlAvailable(jrxmlRepository.isAnyJrxmlExists());
		/* In new Mode get a list of all resources already present in the chosen
		 * folder, to validate report name's uniqueness */
		if (wrapper.isNewMode()) {
            /**
             * TODO(stas): Remove this block. I didn't find any usage of getExistingResources
             */
//			String folderURI = wrapper.getReportUnit().getParentFolder();
//			if (folderURI == null)
//			{
//				folderURI = "/";
//			}
//			FilterCriteria resourcesInFolder = FilterCriteria.createFilter();
//			resourcesInFolder.addFilterElement(FilterCriteria
//					.createParentFolderFilter(folderURI));
//			log("Searching for resources in the chosen folder:"+folderURI);
//			ResourceLookup[] existingResources = repository.findResource(StaticExecutionContextProvider.getExecutionContext(),
//					resourcesInFolder);
//
//			if (existingResources != null && existingResources.length != 0) {
//				log("res lookup size="+existingResources.length);
//				List allResources = new ArrayList();
//				for (int i = 0; i < existingResources.length; i++) {
//					ResourceLookup rLookup = existingResources[i];
//					allResources.add(rLookup.getName());
//					log("adding resource: "+rLookup.getName()+ " to the list");
//				}
//				wrapper.setExistingResources(allResources);
//			}
		} else {
			setJRXMLQueryLanguage(context, false);
		}
		
		context.getFlowScope().put("constants", constants);

        context.getRequestScope().put(ATTRIBUTE_ORGANIZATION_ID, securityContextProvider.getContextUser().getTenantId());
        context.getRequestScope().put(ATTRIBUTE_PUBLIC_FOLDER_URI, configuration.getPublicFolderUri());

        context.getExternalContext().getSessionMap().put(JRXML_TREE_DATA_PROVIDER, jrxmlTreeDataProvider);
        context.getExternalContext().getSessionMap().put(QUERY_TREE_DATA_PROVIDER, queryTreeDataProvider);
        context.getExternalContext().getSessionMap().put(INPUT_CONTROL_RESOURCE_TREE_DATA_PROVIDER, inputControlResourceTreeDataProvider);
        context.getExternalContext().getSessionMap().put(DATA_SOURCE_TREE_DATA_PROVIDER, dataSourceTreeDataProvider);

        context.getFlowScope().put(ATTRIBUTE_RESOURCE_ID_NOT_SUPPORTED_SYMBOLS,
                configuration.getResourceIdNotSupportedSymbols());

		return success();
	}

    public void setSecurityContextProvider(SecurityContextProvider securityContextProvider) {
        this.securityContextProvider = securityContextProvider;
    }

	protected ReportUnitWrapper getFormReportUnitWrapper(RequestContext context) {
		try {
			return (ReportUnitWrapper) getFormObject(context);
		} catch (Exception e) {
			throw new JSExceptionWrapper(e);
		}
	}

	public Event uploadJRXML(RequestContext context) throws Exception {
        //context.getRequestParameters().get("fileName");
    	ReportUnitWrapper wrapper = getFormReportUnitWrapper(context);
		String jrxmlSource = wrapper.getSource();
		// jrxmlSource should never be null with validations in place
		ReportUnit ru = wrapper.getReportUnit();
		ResourceReference jrxmlRef = ru.getMainReport();
		if (jrxmlSource != null
				&& jrxmlSource.equals(constants.getFieldChoiceFile())) {
			log("JRXML source was " + constants.getFieldChoiceFile());
			FileResource jrxml;
            String fileName = context.getRequestParameters().get("fileName");
            if (!jrxmlRef.isLocal()) {
				// The jrxml holds object instance of type FileResourceLookup
				jrxml = (FileResource) repository.newResource(null,
						FileResource.class);
				log("Replaced the JRXML lookup instance with a real object");
				jrxml.setData(wrapper.getJrxmlData());
				jrxml.setFileType(FileResource.TYPE_JRXML);
				jrxml.setName(ru.getName() + LOCAL_JRXML_SUFFIX);
				jrxmlRef = new ResourceReference(jrxml);
                context.getFlowScope().put(JRXML_FILE_RESOURCE_ALREADY_UPLOADED, fileName);
           		wrapper.setJrxmlChanged(true);
			} else {
				// jrxml hold an instance of type FileResource
				log("Jrxml instance was FileResource");
				jrxml = (FileResource) jrxmlRef.getLocalResource();
				if (wrapper.getJrxmlData() != null
						&& wrapper.getJrxmlData().length != 0) {
					// User uploaded a new jrxml file, change the data
					log("Setting the uploaded data into the jrxml");
					if (jrxml.getName() == null) {
						log("Jrxml Name was null, setting it now");
						jrxml.setName(ru.getName() + LOCAL_JRXML_SUFFIX);
					}
					jrxml.setData(wrapper.getJrxmlData());
                    context.getFlowScope().put(JRXML_FILE_RESOURCE_ALREADY_UPLOADED, fileName);
                	wrapper.setJrxmlChanged(true);
				} else {
					log("No Jrxml upload detected");
					// user didnt upload a new file, JRXML is not changed
				}
			}
			if (jrxml.getLabel() == null)
				jrxml.setLabel(ru.getLabel());
			// jrxml.setDescription(ru.getDescription());
			ru.setMainReport(jrxml);
		} else {
			if (jrxmlRef.isLocal()) {
				// The jrxml object is not a FileResourceLookup object, change
				// the object
				jrxmlRef = new ResourceReference(wrapper.getJrxmlUri());
				log("Replaced the JRXML instance with a lookup object");
				log("Jrxml uri set to " + jrxmlRef.getReferenceURI());
                context.getFlowScope().put(JRXML_FILE_RESOURCE_ALREADY_UPLOADED, "");
            	wrapper.setJrxmlChanged(true);
			} else {
				// jrxml is an instance of JRXML Lookup
				log("Setting the lookup uri onto the JRXML lookup");
				if (!jrxmlRef.getReferenceURI()
						.equals(wrapper.getJrxmlUri())) {
					// User changed the jrxml URI selection, JRXML is changed
                    context.getFlowScope().put(JRXML_FILE_RESOURCE_ALREADY_UPLOADED, "");
             		wrapper.setJrxmlChanged(true);
					wrapper.setOldJrxmlUri(jrxmlRef.getReferenceURI());
					jrxmlRef.setReference(wrapper.getJrxmlUri());
					log("Jrxml uri set to " + jrxmlRef.getReferenceURI());
				} else {
					log("JRXML URI was not changed");
					// The selected URI is same as earlier one, JRXML is not
					// changed
				}
			}
			ru.setMainReport(jrxmlRef);
		}

		// If jrxml is changed, parse jrxml to get a list of suggested resources
		if (wrapper.isJrxmlChanged()) {
			log("jrxml was changed, parsing now");

			wrapper.setHasSuggestedResources(false);
			List sugResources = wrapper.getSuggestedResources();
			if (sugResources != null && !sugResources.isEmpty()) {
				// Lose previous unlocated resources (if they were suggested
				// sometime earlier)
				for (int i = sugResources.size() - 1; i > -1; i--) {
					FileResourceWrapper frW = (FileResourceWrapper) sugResources
							.get(i);
					if (!frW.isLocated()) {
						sugResources.remove(i);
						ru.removeResourceLocal(frW.getFileResource().getName());
					}
				}
				// Clear the suggested Resources list
				sugResources = null;
			}
			wrapper.setHasSuggestedControls(false);
			List sugControls = wrapper.getSuggestedControls();
			if (sugControls != null && !sugControls.isEmpty()) {
				// Lose previous unlocated controls (if they were suggested
				// sometime earlier)
				for (int i = sugControls.size() - 1; i > -1; i--) {
					InputControlWrapper icW = (InputControlWrapper) sugControls
							.get(i);
					if (!icW.isLocated()) {
						sugControls.remove(i);
						ru.removeInputControlLocal(icW.getInputControl()
								.getName());
					}
				}
				// Clear the suggested Controls list
				sugControls = null;
			}
			Resource[] parsedResources=null;
			try{
				parsedResources = engine.getResources(jrxmlRef);
			}catch(Exception ex){
				//The JRXML file couldnt be parsed
				context.getRequestScope().put(UNPARSABLE_JRXML_ATTR,UNPARSABLE_JRXML_MESSAGE_KEY);
				wrapper.setJrxmlLocated(false);
				wrapper.setJrxmlData(null);
				return error();
			}

			if (parsedResources != null || parsedResources.length != 0) {
				log("Parsed " + parsedResources.length + " from JRXML");
				for (int i = 0; i < parsedResources.length; i++) {
					Resource resource = parsedResources[i];

					if (FileResource.class
							.isAssignableFrom(resource.getClass())) {
						// Resource is a FileResource object
						wrapper.setHasSuggestedResources(true);
						FileResource suggestedFileResource = (FileResource) resource;
						FileResourceWrapper frW = new FileResourceWrapper();
						frW.setFileResource(suggestedFileResource);
						log("Detected suggested FileResource : "
								+ suggestedFileResource.getName() + " of type "
								+ suggestedFileResource.getFileType());
						// Check if a resource by this name is already added
						FileResource addedFr = ru
								.getResourceLocal(suggestedFileResource
										.getName());
						if (addedFr != null) {
							log("Suggested FileResource is alreday added");
							// mark it added, in the wrapper
							frW.setLocated(true);
						} else {
							// The resource is not added already, dont mark it
							// located
							log("Adding Suggested FileResource to ReportUnit now");
							ru.addResource(suggestedFileResource);
						}
						// Keep a wrapped copy to track
						if (sugResources == null)
							sugResources = new ArrayList(parsedResources.length
									- i);
						sugResources.add(frW);
					} else {
						if (InputControl.class.isAssignableFrom(resource
								.getClass())) {
							// Resource object is an InputControl
							wrapper.setHasSuggestedControls(true);
							InputControl suggestedControl = (InputControl) resource;
							InputControlWrapper icW = new InputControlWrapper(
									suggestedControl);
							log("Detected suggested Control : "
									+ suggestedControl.getName());

							// Check if a resource by this name is already added
							InputControl removedIc = ru
									.getInputControl(suggestedControl.getName());
							if (removedIc != null) {
								// Mark the control as added in the wrapper
								icW.setLocated(true);
							} else {
								// Suggested Control isnt added to the
								// ReportUnit already
								ru.addInputControl(suggestedControl);
							}
							// Keep a wrapped copy to track
							if (sugControls == null)
								sugControls = new ArrayList(
										parsedResources.length - i);
							icW.setSuggested(true);
							sugResources.add(icW);
						}
					}
				}
			} else {
				log("Found no resources on parsing jrxml");
			}
			wrapper.setSuggestedResources(sugResources);
			wrapper.setSuggestedControls(sugControls);

			setJRXMLQueryLanguage(context, true);
		}
		return success();
	}

	public Event setupListResources(RequestContext context) throws Exception
	{
		context.getRequestScope().put("allTypes", configuration.getAllFileResourceTypes());
		return success();
	}


	/* Events originating from list resources page */
	public Event removeResource(RequestContext context) throws Exception {
		log("In removeResource");
		ReportUnitWrapper ruWrapper = getFormReportUnitWrapper(context);
		String resName = context.getRequestParameters()
				.get(PARAM_RESOURCE_NAME);
		log("recieved resName param=" + resName);
		if (resName != null && resName.trim().length() != 0) {
			ReportUnit ru = ruWrapper.getReportUnit();
			ru.removeResourceLocal(resName);
			List sugRes = ruWrapper.getSuggestedResources();
			List allResources = ru.getResources();
			if (ru.getResources() == null
					|| ru.getResources().isEmpty()
					|| (sugRes != null && allResources.size() - sugRes.size() == 0))
				ruWrapper.setHasNonSuggestedResources(false);
			// If the resource figures in the suggested resources list, remove

			if (sugRes != null && !sugRes.isEmpty()) {
				for (int i = 0; i < sugRes.size(); i++) {
					FileResourceWrapper fr = (FileResourceWrapper) sugRes
							.get(i);
					if (fr.getFileResource().getName().equals(resName))
						fr.setLocated(false);
				}
			}

		}
		return success();
	}

	public Event removeControl(RequestContext context) throws Exception {
		log("In removeControl");
		ReportUnitWrapper ruWrapper = getFormReportUnitWrapper(context);
		String resName = context.getRequestParameters()
				.get(PARAM_RESOURCE_NAME);
		log("recieved resName param=" + resName);
		if (resName != null && resName.trim().length() != 0) {
			ReportUnit ru = ruWrapper.getReportUnit();

			if (ru.getInputControl(resName) != null)
				ru.removeInputControlLocal(resName);
			else
				ru.removeInputControlReference(resName);
			List sugCont = ruWrapper.getSuggestedControls();
			List allControls = ru.getInputControls();
			if (allControls == null
					|| allControls.isEmpty()
					|| (sugCont != null && allControls.size() - sugCont.size() == 0))
				ruWrapper.setHasNonSuggestedControls(false);
			// If the name figures in suggested controls list mark the control
			// as Not added

			if (sugCont != null && !sugCont.isEmpty()) {
				for (int i = 0; i < sugCont.size(); i++) {
					InputControlWrapper icW = (InputControlWrapper) sugCont
							.get(i);
					if (icW.getInputControl().getName().equals(resName))
						icW.setLocated(false);
				}
			}

		}
		return success();
	}

	public Event addResource(RequestContext context) throws Exception {
		log("In addNewResource");
		ReportUnitWrapper ruWrapper = getFormReportUnitWrapper(context);
		FileResourceWrapper frW = new FileResourceWrapper();
		FileResource fileR = (FileResource) repository.newResource(null,
				FileResource.class);
		frW.setMode(BaseDTO.MODE_SUB_FLOW_NEW);
		frW.setFileResource(fileR);
		// Set current form object to validate duplicate resource names
		frW.setParentFlowObject(ruWrapper);
		// Set the FileResourceWrapper object into scope for the subflow
		context.getFlowScope()
				.put(FileResourceAction.getFORM_OBJECT_KEY(), frW);
		return success();
	}


	private List getControlsList(RequestContext context)
	{
		List existingPathsList=new ArrayList();
		FilterCriteria criteria = FilterCriteria.createFilter(InputControl.class);
		ResourceLookup[] lookups = repository.findResource(StaticExecutionContextProvider.getExecutionContext(), criteria);
		if(lookups!=null){
			for(int i=0;i<lookups.length;i++){
				existingPathsList.add(lookups[i].getURIString());
			}
		}

		return existingPathsList;
	}

	public Event addControl(RequestContext context) throws Exception {
		log("In addNewControl");
		ReportUnitWrapper ruWrapper = getFormReportUnitWrapper(context);
		InputControlWrapper icW = new InputControlWrapper();
		InputControl ic = (InputControl) repository.newResource(null,
				InputControl.class);
		icW.setMode(BaseDTO.MODE_SUB_FLOW_NEW);
		icW.setInputControl(ic);
		// Set current form object to validate duplicate resource names
		icW.setParentFlowObject(ruWrapper);
		// Set the InputControlWrapper into scope for the subflow
		context.getFlowScope().put(
				DefineInputControlsAction.getFORM_OBJECT_KEY(), icW);

		ruWrapper.setInputControlList(getControlsList(context));
		ruWrapper.setInputControlSource(JasperServerConstImpl.getFieldChoiceRepo());
		return success();
	}

	public Event editResource(RequestContext context) throws Exception {
		log("In editResource");
		ReportUnitWrapper ruWrapper = getFormReportUnitWrapper(context);
		String resName = context.getRequestParameters()
				.get(PARAM_RESOURCE_NAME);
		log("recieved resName param=" + resName);
		if (resName != null && resName.trim().length() != 0) {
			FileResource fileR = ruWrapper.getReportUnit().getResourceLocal(
					resName);
			if (fileR == null) {
				log("NO Local resource by that name");
            } else {
				log("Passed resName=" + fileR.getName() + " type="
						+ fileR.getFileType());
            }
			FileResourceWrapper frW = new FileResourceWrapper();
			frW.setMode(BaseDTO.MODE_SUB_FLOW_EDIT);
            frW.setParentFlowObject(ruWrapper);

			if (fileR != null) {
                frW.setFileResource(new FileResourceImpl((FileResourceImpl) fileR));
				if (fileR.isReference()) {
					frW.setSource(JasperServerConst.FIELD_CHOICE_CONT_REPO);
					frW.setNewUri(fileR.getReferenceURI());
				} else {
					frW.setSource(JasperServerConst.FIELD_CHOICE_FILE_SYSTEM);
					if (!fileR.isNew()) {
						frW.setLocated(true);
					}
				}
			}

			// Set the FileResourceWrapper object into scope for the subflow
			context.getFlowScope().put(FileResourceAction.getFORM_OBJECT_KEY(),
					frW);
		}
		return success();
	}

	public Event editControl(RequestContext context) throws Exception {
		log("In editControl");
		ReportUnitWrapper ruWrapper = getFormReportUnitWrapper(context);
		String resName = context.getRequestParameters()
				.get(PARAM_RESOURCE_NAME);
		if (resName != null && resName.trim().length() != 0) {
			InputControl ic = ruWrapper.getReportUnit()
					.getInputControl(resName);
			InputControlWrapper icW = new InputControlWrapper();
			icW.setMode(BaseDTO.MODE_SUB_FLOW_EDIT);
			icW.setInputControl(new InputControlImpl((InputControlImpl) ic));
			// Set the FileResourceWrapper object into scope for the subflow
			context.getFlowScope().put(
					DefineInputControlsAction.getFORM_OBJECT_KEY(), icW);
			ruWrapper.setInputControlList(getControlsList(context));
			if (ic == null) {
				ruWrapper.setInputControlPath(resName);
				ruWrapper.setInputControlSource(JasperServerConstImpl.getFieldChoiceRepo());
				icW.setOldInputControlURI(resName);
			}
			else
				ruWrapper.setInputControlSource(JasperServerConstImpl.getFieldChoiceLocal());
		}

		return success();
	}

	public Event selectControlType(RequestContext context) throws Exception
	{
		ReportUnitWrapper ruWrapper = getFormReportUnitWrapper(context);
		InputControlWrapper icW = (InputControlWrapper) context.getFlowScope().get(DefineInputControlsAction.getFORM_OBJECT_KEY());
		String source = context.getRequestParameters().get("inputControlSource");
		if (source.equals(JasperServerConstImpl.getFieldChoiceLocal())) {
			if (icW.getInputControl() == null) {
				InputControl ic = (InputControl) repository.newResource(null,
						InputControl.class);
				icW.setInputControl(ic);
				icW.setMode(BaseDTO.MODE_SUB_FLOW_NEW);
			}
			return yes();
		} else {
			String path;// = ruWrapper.getInputControlPath(); FIXME
			path = context.getRequestParameters().get("inputControlPath");
			ResourceReference inputControlURI = new ResourceReference(path);
			icW.setInputControlURI(inputControlURI);
			return no();
		}
	}

	/* End events originating from List resources page */

	public Event saveResource(RequestContext context) throws Exception {
		log("In saveResource");
		ReportUnitWrapper ruWrapper = getFormReportUnitWrapper(context);
		FileResourceWrapper frW = (FileResourceWrapper) context.getFlowScope()
				.get(FileResourceAction.getFORM_OBJECT_KEY());
		if (frW != null) {
			// FileResourceWrapper returned by a subflow, determine if its new
			// object or edited
			if (frW.isEditMode()) {
				ReportUnit ru = ruWrapper.getReportUnit();

				// If the FileResource is a suggested one, change that reference
				// also
				List sugRes = ruWrapper.getSuggestedResources();
				if (sugRes != null && !sugRes.isEmpty()) {
					for (int i = 0; i < sugRes.size(); i++) {
						FileResourceWrapper sRW = (FileResourceWrapper) sugRes
								.get(i);
						if (sRW.getFileResource().getName().equals(
								frW.getFileResource().getName())) {
							sRW.setFileResource(frW.getFileResource());
							sRW.setLocated(true);
							break;
						}
					}
				}
				ru.removeResourceLocal(frW.getFileResource().getName());
			}
			// In any case save the new/edited FileResource into ReportUnit
			ruWrapper.getReportUnit().addResource(frW.getFileResource());
		}
		return success();
	}

	public Event saveControl(RequestContext context) throws Exception {
		log("In saveControl");
		ReportUnitWrapper ruWrapper = getFormReportUnitWrapper(context);
		InputControlWrapper icW = (InputControlWrapper) context.getFlowScope()
				.get(DefineInputControlsAction.getFORM_OBJECT_KEY());
		if (icW != null) {
			// InputControlWrapper returned by a subflow
			if (icW.isEditMode()) {
				replaceInputControl(ruWrapper, icW);
				
				// If the InputControl is suggested, update it
				updateSuggestedInputControl(ruWrapper, icW);
			} else {
				addInputControl(ruWrapper, icW);
			}
			ruWrapper.setHasNonSuggestedControls(true);
		}
		return success();
	}

	protected void addInputControl(ReportUnitWrapper ruWrapper, InputControlWrapper icW) {
		ReportUnit report = ruWrapper.getReportUnit();
		String oldInputControlReference = icW.getOldInputControlURI();
		if (oldInputControlReference == null) {
			if (icW.getInputControlURI() == null) {
				report.addInputControl(icW.getInputControl());
			} else {
				report.addInputControl(icW.getInputControlURI());
			}
		} else {
			if (icW.getInputControlURI() == null) {
				report.replaceInputControlReference(oldInputControlReference, icW.getInputControl());
			} else {
				report.replaceInputControlReference(oldInputControlReference, icW.getInputControlURI());
			}
		}
	}

	protected void replaceInputControl(ReportUnitWrapper ruWrapper, InputControlWrapper icW) {
		ReportUnit report = ruWrapper.getReportUnit();
		String oldInputControlReference = icW.getOldInputControlURI();
		if (oldInputControlReference == null) {
			String oldInputControlName = icW.getInputControl().getName();
			if (icW.getInputControlURI() == null) {
				report.replaceInputControlLocal(oldInputControlName, icW.getInputControl());
			} else {
				report.replaceInputControlLocal(oldInputControlName, icW.getInputControlURI());
			}
		} else {
			if (icW.getInputControlURI() == null) {
				report.replaceInputControlReference(oldInputControlReference, icW.getInputControl());
			} else {
				report.replaceInputControlReference(oldInputControlReference, icW.getInputControlURI());
			}
		}
	}

	protected void updateSuggestedInputControl(ReportUnitWrapper ruWrapper, InputControlWrapper icW) {
		List sugControls = ruWrapper.getSuggestedControls();
		if (sugControls != null && !sugControls.isEmpty()) {
			for (int i = 0; i < sugControls.size(); i++) {
				InputControlWrapper sICW = (InputControlWrapper) sugControls
						.get(i);
				if (icW.getInputControlURI() != null) {
					if (icW.getInputControlURI().getReferenceURI().equals(sICW.getInputControlURI().getReferenceURI())) {
						sICW.setInputControlURI(icW.getInputControlURI());
						sICW.setLocated(true);
						break;
					}
				} else {
					if (icW.getInputControl().getName().equals(
							sICW.getInputControl().getName())) {
						sICW.setInputControl(icW.getInputControl());
						sICW.setLocated(true);
						break;
					}
				}
			}
		}
	}

	public Event locateDataSource(RequestContext context) throws Exception {
		log("In locate data source");
		ReportUnitWrapper ruWrapper =
                getFormReportUnitWrapper(context);
		ResourceReference dsRef =
                ruWrapper.getReportUnit().getDataSource();
		ReportDataSourceWrapper rdWrapper = new ReportDataSourceWrapper();
        rdWrapper.setMode(BaseDTO.MODE_SUB_FLOW_NEW);
        rdWrapper.setParentFlowObject(ruWrapper);
		rdWrapper.setParentType(DATA_SOURCE_PARENT_TYPE);
		if (dsRef == null || (dsRef.getReferenceURI() == null && dsRef.getLocalResource() == null)) {
			log("Found no previous ReportDataSource, creating new");
			ReportDataSource ds = (ReportDataSource) repository.newResource(
					null, JdbcReportDataSource.class);
            String parentFolder = (String)context.getFlowScope().get(PARENT_FOLDER_ATTR);
			if (parentFolder == null) {
				parentFolder = context.getRequestParameters().get("ParentFolderUri");
				context.getFlowScope().put(PARENT_FOLDER_ATTR, parentFolder);
			}
            ds.setParentFolder(parentFolder);
			rdWrapper.setSource(constants.getFieldChoiceNone());
			rdWrapper.setType(constants.getJDBCDatasourceType());
			rdWrapper.setReportDataSource(ds);
		} else {
			// if the dataSource exists decide source and type and set in
			// wrapper
			if (dsRef.isLocal()) {
				rdWrapper.setSource(constants.getFieldChoiceLocal());
				ReportDataSource ds = (ReportDataSource) dsRef.getLocalResource();
				if (ds != null)
				{
                    final String editedDataSourceJson = (String) context.getFlowScope().get(ReportDataSourceWrapper.ATTRIBUTE_DATA_SOURCE_JSON);
                    if(editedDataSourceJson != null){
                        rdWrapper.setDataSourceJson(editedDataSourceJson);
                    } else {
                        rdWrapper.setDataSourceUri(ds.getURIString());
                    }
					if (JdbcReportDataSource.class.isAssignableFrom(ds.getClass())) {
						rdWrapper.setType(constants.getJDBCDatasourceType());
					} else if (JndiJdbcReportDataSource.class.isAssignableFrom(ds.getClass())) {
						rdWrapper.setType(constants.getJNDIDatasourceType());
					} else if (VirtualReportDataSource.class.isAssignableFrom(ds.getClass())) {
						rdWrapper.setType(constants.getVirtualDatasourceType());
                    } else if (BeanReportDataSource.class.isAssignableFrom(ds.getClass())) {
						rdWrapper.setType(constants.getBeanDatasourceType());
					} else if (ds instanceof CustomReportDataSource) {
                        CustomReportDataSource cds = (CustomReportDataSource) ds;
                        // look up definition & use it to init defaults & set prop defs
                        CustomDataSourceDefinition customDef = customDataSourceFactory.getDefinition(cds);
                        rdWrapper.setType(customDef.getName());
					}
				}
                rdWrapper.setMode(BaseDTO.MODE_SUB_FLOW_EDIT);
				rdWrapper.setReportDataSource(ds);
			} else {
				// DataSource object is a lookup
                rdWrapper.setSelectedUri(dsRef.getReferenceURI());
                log("Found ReportDataSourceLookup");
                ReportDataSource ds = (ReportDataSource) repository.newResource(
					null, JdbcReportDataSource.class);
                String parentFolder = (String)context.getFlowScope().get(PARENT_FOLDER_ATTR);
                if (parentFolder == null) {
                    parentFolder = (String)context.getRequestParameters().get("ParentFolderUri");
                    context.getFlowScope().put(PARENT_FOLDER_ATTR, parentFolder);
                }
                dsRef = new ResourceReference(ds);
                ds.setParentFolder(parentFolder);
                rdWrapper.setSource(constants.getFieldChoiceRepo());
                rdWrapper.setReportDataSource(ds);
			}
		}
		// Set the object into scope with the name that the reportDataSourceFlow
		// can pickup
		context.getFlowScope().put(DataSourceAction.FORM_OBJECT_KEY,
				rdWrapper);
		return success();
	}

	public Event saveDatasource(RequestContext context) throws Exception {
		// Save the returned datasource info
		ReportDataSourceWrapper resource = (ReportDataSourceWrapper) context
				.getFlowScope().get(DATASOURCE_OBJECT_KEY);
        final ParameterMap requestParameters = context.getRequestParameters();
        final String dataSourceJson = requestParameters.get("dataSourceJson");
        final String dataSourceType = requestParameters.get("dataSourceType");
        String source = requestParameters.get("source");
        if(source == null) source = constants.getFieldChoiceNone();
        if (resource == null && (dataSourceJson == null || dataSourceType == null))
			log("Got null datatsource back from subflow");
		else
			log("type was " + (resource != null ? resource.getType() : dataSourceType));
		ReportUnitWrapper ruWrapper = getFormReportUnitWrapper(context);
		if (source.equals(constants.getFieldChoiceRepo())) {
			ruWrapper.getReportUnit().setDataSourceReference(
					resource.getSelectedUri());
		} else if (source.equals(constants.getFieldChoiceLocal())) {
            final ReportDataSource reportDataSource;
            if(dataSourceJson != null && dataSourceType != null){
                // save localDataSource JSON to send it back to the client if the data source page is opened again.
                context.getFlowScope().put(ReportDataSourceWrapper.ATTRIBUTE_DATA_SOURCE_JSON, dataSourceJson);
                reportDataSource = dataSourceJsonHelper.parse(dataSourceJson, dataSourceType);
                reportDataSource.setURIString(ruWrapper.getReportUnit().getURIString() + "_files");
            } else {
                reportDataSource = resource.getReportDataSource();
            }
            ruWrapper.getReportUnit().setDataSource(
                    reportDataSource);
		} else if (source.equals(constants.getFieldChoiceNone())) {
			ruWrapper.getReportUnit().setDataSource((ResourceReference) null);
		}
		return success();
	}

	public Event saveReport(RequestContext context) throws Exception {
		log("In saveReport");
		ReportUnitWrapper ruWrapper = getFormReportUnitWrapper(context);
		try {
            repository.saveResource(null, ruWrapper.getReportUnit());
		} catch (JSDuplicateResourceException e) {
			getFormErrors(context).rejectValue("reportUnit.name", "ReportDetailsValidator.error.duplicate");
			return error();
		}
        if (!ruWrapper.isEditMode()) {
            context.getExternalContext().getSessionMap().put("repositorySystemConfirm",
                    messages.getMessage("resource.report.reportAdded",
                            new String[] {ruWrapper.getReportUnit().getName(),
                            ruWrapper.getReportUnit().getParentFolder()},
                            LocaleContextHolder.getLocale()));
        }
		return success();
	}

	public Object createFormObject(RequestContext context) {
		ReportUnitWrapper formObject = null;
		String resourceUri = context.getRequestParameters().get(RU_URI_PARAM);
		if (resourceUri == null) {
			resourceUri = context.getRequestParameters().get(REQUEST_PARAM_SELECTED_RESOURCE);
		}
		if (resourceUri != null && resourceUri.trim().length() != 0) {
			ReportUnit ru = (ReportUnit) repository.getResource(null,
					resourceUri);
			if (ru == null)
				throw new JSException("jsexception.could.not.find.resource.with.uri", new Object[] {resourceUri});
			log("Found resource with uri=" + resourceUri);
			formObject = new ReportUnitWrapper();
			formObject.setReportUnit(ru);
			ResourceReference jrxmlRef = ru.getMainReport();
			String uri;
			if (!jrxmlRef.isLocal()) {
				// ReportUnit is a lookup object
				formObject.setSource(constants.FIELD_CHOICE_CONT_REPO);
				uri = jrxmlRef.getReferenceURI();
				formObject.setJrxmlUri(uri);
			} else {
				formObject.setSource(constants.FIELD_CHOICE_FILE_SYSTEM);
				uri = jrxmlRef.getLocalResource().getURIString();
			}
			formObject.setOldJrxmlUri(uri);
			formObject.setOriginalJrxmlUri(uri);
			formObject.setMode(BaseDTO.MODE_STAND_ALONE_EDIT);
			formObject.setDatasourceIdentified(true);
			formObject.setNamed(true);
			formObject.setJrxmlLocated(true);
			if (ru.getResources() != null && !ru.getResources().isEmpty())
				formObject.setHasNonSuggestedResources(true);
			else
				formObject.setHasNonSuggestedResources(false);
			if (ru.getInputControls() != null
					&& !ru.getInputControls().isEmpty())
				formObject.setHasNonSuggestedControls(true);
			else
				formObject.setHasNonSuggestedControls(false);
		}
		if (formObject == null) {
			log("ReportUnitAction: Stand alone new mode");
			formObject = new ReportUnitWrapper();
			ReportUnit ru = (ReportUnit) repository.newResource(null,
					ReportUnit.class);
			FileResource jrxml = (FileResource) repository.newResource(null,
					FileResource.class);
			jrxml.setFileType(FileResource.TYPE_JRXML);
			ru.setMainReport(jrxml);
			formObject.setMode(BaseDTO.MODE_STAND_ALONE_NEW);
			String parentFolder = getParentFolderUri(context);
			ru.setParentFolder(parentFolder);
			formObject.setReportUnit(ru);
			formObject.setSource(constants.FIELD_CHOICE_FILE_SYSTEM);
			// TODO set default options when creating new RU
			formObject.setHasNonSuggestedResources(false);
			formObject.setHasNonSuggestedControls(false);
		}
		return formObject;
	}

    private String getParentFolderUri(RequestContext context) {
        String parentFolder = (String) context.getFlowScope().get(PARENT_FOLDER_ATTR);
        // add to allow invoking from repository explorer
        if (parentFolder == null) {
            parentFolder = (String)context.getRequestParameters().get("ParentFolderUri");
            context.getFlowScope().put(PARENT_FOLDER_ATTR, parentFolder);
        }

        return parentFolder;
    }
	public Event validateReport(RequestContext context) throws Exception {
		
		Errors errors = getFormErrors(context);
		
		getValidator().validate(getFormObject(context), errors);
		
		List fieldErrors = errors.getFieldErrors();
		if (fieldErrors != null && !fieldErrors.isEmpty())
		{
			FieldError error = (FieldError)fieldErrors.get(0);
			String field = error.getField();
			
			if (
				"reportUnit.name".equals(field)
				|| "reportUnit.label".equals(field)
				|| "reportUnit.description".equals(field)
				)
			{
				return result("displayNameLabelDescForm");
			}
			else if (
				"source".equals(field)
				|| "jrxmlData".equals(field)
				)
			{
				return result("displayJrxmlUploadForm");
			}
			else if (
				"validationMessage".equals(field)
				|| "reportUnit.inputControlRenderingView".equals(field)
				)
				
			{
				return result("listResources");
			}
		}
		
		log("In Validate Report");
		ReportUnitWrapper ruWrapper = (ReportUnitWrapper) getFormObject(context);
		ReportUnit ru = ruWrapper.getReportUnit();
		ValidationResult result = engine.validate(null, ru);
		ruWrapper.setResult(result.getValidationState().equals(
				ValidationResult.STATE_VALID));
		return success();
	}

	/*
	 * method to get the reposervice object arguments: none returns:
	 * RepositoryService
	 */
	public RepositoryService getRepository() {
		return repository;
	}

	/*
	 * method to set the reposervice object arguments: RepositoryService
	 * returns: void
	 */
	public void setRepository(RepositoryService repository) {
		this.repository = repository;
	}

	public EngineService getEngine() {
		return engine;
	}

	public void setEngine(EngineService engine) {
		this.engine = engine;
	}

	public RepositoryConfiguration getConfiguration()
	{
		return configuration;
	}

	public void setConfiguration(RepositoryConfiguration configuration)
	{
		this.configuration = configuration;
	}

	/**
	 * Registers a byte array editor to allow spring handle File uploads as byte
	 * arrays
	 */
	protected void initBinder(RequestContext context, DataBinder binder) {
		binder.registerCustomEditor(byte[].class,
				new ByteArrayMultipartFileEditor());
	}

	private void log(String text) {
		log.debug(text);
	}
	
	public Event prepareQuery(RequestContext context) throws Exception {
		ReportUnitWrapper ruWrapper = (ReportUnitWrapper) getFormObject(context);
		ResourceReference queryRef = ruWrapper.getReportUnit().getQuery();
		ResourceReferenceDTO queryRefDTO = new ResourceReferenceDTO(queryRef);
		context.getRequestScope().put(getQueryReferenceReqAttrName(), queryRefDTO);
        context.getRequestScope().put(FORM_OBJECT_KEY, ruWrapper);
        
		return success();
	}
	
	public Event setQueryReference(RequestContext context) throws Exception {
		ResourceReferenceDTO queryRef = (ResourceReferenceDTO) context.getFlowScope().getRequired(getQueryReferenceOutpuAttrName(), ResourceReferenceDTO.class);
		ReportUnitWrapper ruWrapper = (ReportUnitWrapper) getFormObject(context);
		ruWrapper.getReportUnit().setQuery(queryRef.toResourceReference());
		return success();
	}
	
	/**
	 * @return Returns the queryReferenceReqAttrName.
	 */
	public String getQueryReferenceReqAttrName() {
		return queryReferenceReqAttrName;
	}
	
	/**
	 * @param queryReferenceReqAttrName The queryReferenceReqAttrName to set.
	 */
	public void setQueryReferenceReqAttrName(String queryReferenceReqAttrName) {
		this.queryReferenceReqAttrName = queryReferenceReqAttrName;
	}
	
	/**
	 * @return Returns the queryReferenceOutpuAttrName.
	 */
	public String getQueryReferenceOutpuAttrName() {
		return queryReferenceOutpuAttrName;
	}
	
	/**
	 * @param queryReferenceOutpuAttrName The queryReferenceOutpuAttrName to set.
	 */
	public void setQueryReferenceOutpuAttrName(
			String queryReferenceOutpuAttrName) {
		this.queryReferenceOutpuAttrName = queryReferenceOutpuAttrName;
	}

	protected void setJRXMLQueryLanguage(RequestContext context, boolean changed) {
		ResourceReference mainReport = getFormReportUnitWrapper(context).getReportUnit().getMainReport();
		String queryLanguage = engine.getQueryLanguage(StaticExecutionContextProvider.getExecutionContext(), mainReport);
		context.getFlowScope().put(getReportQueryLanguageFlowAttribute(), queryLanguage);
		
		if (changed && queryLanguage != null) {
			checkDataSource(context, queryLanguage);
		}
	}

	protected void checkDataSource(RequestContext context, String queryLanguage) {
		ReportUnitWrapper ruWrapper = getFormReportUnitWrapper(context);
		ReportUnit reportUnit = ruWrapper.getReportUnit();
		ResourceReference dsRef = reportUnit.getDataSource();
		if (dsRef != null) {
			ReportDataSource ds = null;
			if (dsRef.isLocal()) {
				ds = (ReportDataSource) dsRef.getLocalResource();
			} else {
				String uri = dsRef.getReferenceURI();
				if (uri != null) {
					ds = (ReportDataSource) repository.getResource(StaticExecutionContextProvider.getExecutionContext(), uri);
				}
			}
			
			if (ds != null) {
				Set dataSourceTypes = engine.getDataSourceTypes(StaticExecutionContextProvider.getExecutionContext(), queryLanguage);
				if (!checkDataSourceType(ds, dataSourceTypes)) {
					if (log.isDebugEnabled()) {
						log.debug("Data source of report unit " + reportUnit.getURIString() + " does not support " + queryLanguage + " queries, removing");
					}
					//FIXME inform the user about this
					reportUnit.setDataSource((ResourceReference) null);
					ruWrapper.setDatasourceIdentified(false);
				}
			}
		}
	}

	protected boolean checkDataSourceType(ReportDataSource ds, Set supportedTypes) {
		boolean valid = false;
		for (Iterator it = supportedTypes.iterator(); !valid && it.hasNext();) {
			Class type = (Class) it.next();
			if (type.isInstance(ds)) {
				valid = true;
			}
		}
		return valid;
	}
	
	public String getReportQueryLanguageFlowAttribute() {
		return reportQueryLanguageFlowAttribute;
	}

	public void setReportQueryLanguageFlowAttribute(String queryLanguageFlowAttribute) {
		this.reportQueryLanguageFlowAttribute = queryLanguageFlowAttribute;
	}

    public void setJrxmlTreeDataProvider(BaseTreeDataProvider typedTreeDataProvider) {
        this.jrxmlTreeDataProvider = typedTreeDataProvider;
    }

    public void setQueryTreeDataProvider(TypedTreeDataProvider queryTreeDataProvider) {
        this.queryTreeDataProvider = queryTreeDataProvider;
    }

    public void setInputControlResourceTreeDataProvider(TypedTreeDataProvider inputControlResourceTreeDataProvider) {
        this.inputControlResourceTreeDataProvider = inputControlResourceTreeDataProvider;
    }

    public void setDataSourceTreeDataProvider(TypedTreeDataProvider dataSourceTreeDataProvider) {
        this.dataSourceTreeDataProvider = dataSourceTreeDataProvider;
    }

    public void setJrxmlRepository(JRXMLRepository jrxmlRepository) {
        this.jrxmlRepository = jrxmlRepository;
    }
}
