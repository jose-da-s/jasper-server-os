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
import com.jaspersoft.jasperserver.api.common.crypto.PasswordCipherer;
import com.jaspersoft.jasperserver.api.engine.common.service.SecurityContextProvider;
import com.jaspersoft.jasperserver.api.metadata.common.domain.*;
import com.jaspersoft.jasperserver.api.metadata.common.domain.client.ContentResourceImpl;
import com.jaspersoft.jasperserver.api.metadata.common.domain.client.FileResourceImpl;
import com.jaspersoft.jasperserver.api.metadata.common.service.RepositoryService;
import com.jaspersoft.jasperserver.api.metadata.xml.domain.impl.ResourceDescriptor;
import com.jaspersoft.jasperserver.war.common.JasperServerConst;
import com.jaspersoft.jasperserver.war.common.JasperServerConstImpl;
import com.jaspersoft.jasperserver.war.dto.BaseDTO;
import com.jaspersoft.jasperserver.war.dto.FileResourceWrapper;
import com.jaspersoft.jasperserver.war.model.impl.TypedTreeDataProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.validation.DataBinder;
import org.springframework.web.multipart.support.ByteArrayMultipartFileEditor;
import org.springframework.webflow.action.FormAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.ScopeType;

import java.util.ArrayList;
import java.util.List;

public class FileResourceAction extends FormAction {

	protected final Log log = LogFactory.getLog(this.getClass());

	protected static final String FORM_OBJECT_KEY = "fileResource";

    protected static final String FILE_RESOURCE_COPY = "fileResourceCopy";

	private static final String FILERES_URI_PARAM = "resource";

	protected static final String PARENT_FOLDER_ATTR = "parentFolder";

	protected static final String CONSTANTS_KEY = "constants";

    private static final String ATTRIBUTE_ORGANIZATION_ID = "organizationId";
    private static final String ATTRIBUTE_PUBLIC_FOLDER_URI = "publicFolderUri";
    private static final String PARAM_ATTR_OPEN_IN_EDITOR = "openInEditor";
    private static final String ATTRIBUTE_ALL_FILE_TYPES = "allTypes";
    private static final String ATTRIBUTE_RESOURCE_ID_NOT_SUPPORTED_SYMBOLS = "resourceIdNotSupportedSymbols";

    private static final String FILE_RESOURCE_TREE_DATA_PROVIDER = "fileResourceTreeDataProvider";

    private String expectedFileTypeParameter = "expectedFileType";// default value
    private String expectedFileTypeAttribute = "expectedFileType";// default value

	protected RepositoryService repository;
	private RepositoryConfiguration configuration;
    protected MessageSource messages;

    private SecurityContextProvider securityContextProvider;
    private TypedTreeDataProvider typedTreeDataProvider;

    public FileResourceAction() {
		setFormObjectClass(FileResourceWrapper.class);
		setFormObjectName(FORM_OBJECT_KEY);
		setFormObjectScope(ScopeType.FLOW);
	}

    public void setSecurityContextProvider(SecurityContextProvider securityContextProvider) {
        this.securityContextProvider = securityContextProvider;
    }

    public void setMessages(MessageSource messages) {
        this.messages = messages;
    }

    public Event initAction(RequestContext context) throws Exception {
		FileResourceWrapper wrapper = (FileResourceWrapper) getFormObject(context);

		if (wrapper.isSubflowMode()) {
//			getAllFolders(wrapper); // TODO get this from main flow
			String folderURI = (String) context.getFlowScope().get(PARENT_FOLDER_ATTR);
			if (folderURI == null) {
				folderURI = (String)context.getRequestParameters().get("ParentFolderUri");
				context.getFlowScope().put(PARENT_FOLDER_ATTR, folderURI);
			}
			if (folderURI == null)
			{
				folderURI = "/";
			}
			if (!isMasterFlowReportUnit(context)) {
				wrapper.getFileResource().setParentFolder( // TODO put parent folder in flow scope in main flow
						folderURI);
			}
		}
		// set default source

		if (wrapper.getSource() == null) {
			if (wrapper.isNewMode()) {
				wrapper.setSource(JasperServerConst.FIELD_CHOICE_FILE_SYSTEM);
			}
			else if (wrapper.getFileResource() != null) {
				wrapper.setSource(JasperServerConst.FIELD_CHOICE_CONT_REPO);
				wrapper.setNewUri(wrapper.getFileResource().getReferenceURI());
			}
		}
		context.getFlowScope().put(FORM_OBJECT_KEY, wrapper);
		context.getFlowScope().put(CONSTANTS_KEY, new JasperServerConstImpl());

		// set expected file type on flow scope
		String expectedFileType = context.getRequestParameters().get(expectedFileTypeParameter);
        if (expectedFileType == null && wrapper.getFileResource().getFileType() != null) {
            expectedFileType = wrapper.getFileResource().getFileType();
        }
		context.getFlowScope().put(expectedFileTypeAttribute, expectedFileType);

		// set openInEditor on flow scope
		Boolean openInEditor = context.getRequestParameters().getBoolean(PARAM_ATTR_OPEN_IN_EDITOR);
		context.getFlowScope().put(PARAM_ATTR_OPEN_IN_EDITOR, openInEditor != null ? openInEditor.booleanValue() : false);

        context.getFlowScope().put(FILE_RESOURCE_COPY, new FileResourceImpl((FileResourceImpl) wrapper.getFileResource()));

        context.getFlowScope().put(ATTRIBUTE_ORGANIZATION_ID,
                securityContextProvider.getContextUser().getTenantId());
        context.getFlowScope().put(ATTRIBUTE_PUBLIC_FOLDER_URI, configuration.getPublicFolderUri());
        context.getFlowScope().put(ATTRIBUTE_RESOURCE_ID_NOT_SUPPORTED_SYMBOLS,
                configuration.getResourceIdNotSupportedSymbols());
        context.getFlowScope().put(ATTRIBUTE_ALL_FILE_TYPES, configuration.getAllFileResourceTypes());

        context.getExternalContext().getSessionMap().put(FILE_RESOURCE_TREE_DATA_PROVIDER, typedTreeDataProvider);        
		return success();
	}

	@Deprecated
	protected void getAllFolders(FileResourceWrapper wrapper)
	{
		List allFolders = repository.getAllFolders(null);
		wrapper.setAllFolders(new ArrayList());
		for (int i = 0; i < allFolders.size(); i++) {
			String folderUri = ((Folder) allFolders.get(i)).getURIString();
			wrapper.getAllFolders().add(folderUri);
		}
	}

	public Event determineType(RequestContext context) throws Exception {
	
		FileResourceWrapper wrapper = (FileResourceWrapper) getFormObject(context);
		if (wrapper.getFileResource().getFileType() == null) {
			String fileExtension = context.getExternalContext().getRequestMap()
					.getString(JasperServerConst.UPLOADED_FILE_EXT);
			if (fileExtension != null && fileExtension.trim().length() != 0) {
				wrapper.getFileResource().setFileType(
						getTypeForExtension(context, fileExtension));
			}
		}

		if (wrapper.getSource() != null
			&& wrapper.getSource().equals(
					JasperServerConst.FIELD_CHOICE_CONT_REPO)) {		
			// User opted for a lookup URI
			String newUri = wrapper.getNewUri();
			if (newUri != null && newUri.trim().length() != 0) {
				Resource resource = repository.getResource(null, newUri);
				if (FileResource.class.isAssignableFrom(resource.getClass())) {
					FileResource fileR = (FileResource) resource;
					wrapper.getFileResource().setFileType(fileR.getFileType());
				}
			}
			wrapper.setLocated(true);
		}
		// allow file resource optional
		if (wrapper.getSource() != null) {
		    if (wrapper.getSource().equals(JasperServerConst.FIELD_CHOICE_FILE_SYSTEM)) {
			// clean up previsous selection
			wrapper.setNewUri(null);
			wrapper.setLocated(true);
		    }
		    else if (wrapper.getSource().equals(JasperServerConst.FIELD_CHOICE_NONE)) {
			// signal a remove file resource
			return no();
		    }
		}
		return success();
	}

	protected String getTypeForExtension(RequestContext context, String extension) {
		String type = null;
		if (extension != null) {
			if (extension.equalsIgnoreCase(FileResource.TYPE_JRXML))
				type = FileResource.TYPE_JRXML;
			else if (extension.equalsIgnoreCase("ttf"))
				type = FileResource.TYPE_FONT;
			else if (extension.equalsIgnoreCase("xml")) {
				type = FileResource.TYPE_XML;
			}
			else if (extension.equalsIgnoreCase("agxml"))
				type = ResourceDescriptor.TYPE_ACCESS_GRANT_SCHEMA; // pro-only
			else if (extension.equalsIgnoreCase(FileResource.TYPE_JAR)) {
				type = FileResource.TYPE_JAR;
			} else if (extension.indexOf(FileResource.TYPE_RESOURCE_BUNDLE) != -1) {
				type = FileResource.TYPE_RESOURCE_BUNDLE;
			} else if (extension.equalsIgnoreCase(FileResource.TYPE_STYLE_TEMPLATE)) {
				type = FileResource.TYPE_STYLE_TEMPLATE;
			} else {
				String[] imageTypes = { "jpg", "jpeg", "gif", "bmp" };
				for (int i = 0; i < imageTypes.length; i++) {
					if (extension.equalsIgnoreCase(imageTypes[i])) {
						type = FileResource.TYPE_IMAGE;
					}
				}
			}
		}
		return type;
	}

    protected ContentResource fileToContentResource(FileResource fileResource) {
        ContentResource result = new ContentResourceImpl();
        result.setData(fileResource.getData());
        result.setReferenceURI(fileResource.getReferenceURI());
        result.setAttributes(fileResource.getAttributes());
        result.setCreationDate(fileResource.getCreationDate());
        result.setDescription(fileResource.getDescription());
        result.setLabel(fileResource.getLabel());
        result.setName(fileResource.getName());
        result.setParentFolder(fileResource.getParentFolder());
        result.setUpdateDate(fileResource.getUpdateDate());
        result.setURIString(fileResource.getURIString());
        result.setFileType(ContentResource.TYPE_UNSPECIFIED);
        return result;
    }

	protected boolean isMasterFlowReportUnit(RequestContext context)
	{
		String masterFlow = context.getFlowScope().getString("masterFlow");
		return "reportUnit".equals(masterFlow);
	}

	public Event setupNamingForm(RequestContext context) throws Exception
	{
		context.getRequestScope().put("allTypes", configuration.getAllFileResourceTypes());
		return success();
	}

    /**
     * Event that restores File Resource to state in which it has been before editing.
     *
     * @param context Request context.
     * @return Event
     * @throws Exception
     */
    public Event cancelFileResourceChanges(RequestContext context) throws Exception {
        FileResourceWrapper wrapper = (FileResourceWrapper) getFormObject(context);
        FileResourceImpl file = (FileResourceImpl) context.getFlowScope().get(FILE_RESOURCE_COPY);
        wrapper.setFileResource(new FileResourceImpl(file));
        wrapper.setLocated(false);
        return success();
    }

	/**
	 * Saves the changes made in alone mode back to repository
	 * 
	 * @param context
	 * @return
	 * @throws Exception
	 */
	public Event saveResource(RequestContext context) throws Exception {
		log("In saveresource");
		FileResourceWrapper wrapper = (FileResourceWrapper) getFormObject(context);
		FileResource fr = wrapper.getFileResource();
		Resource toSave = fr;

		if (fr.getParentFolder() == null) {
			fr.setParentFolder("/");
		}
		if (fr.getFileType().equals(ContentResource.TYPE_UNSPECIFIED)){
            toSave = fileToContentResource(fr);
        }
        if (fr.getFileType().equals(FileResource.TYPE_SECURE_FILE) && fr.getData()!= null){
			fr.setData(PasswordCipherer.getInstance().encodePassword(new String(fr.getData())).getBytes());
        }

		if (wrapper.isStandAloneMode()) {
			try {
				repository.saveResource(null, toSave);
			}
			catch (JSDuplicateResourceException e) {
				getFormErrors(context).rejectValue("fileResource.name", "FileResourceValidator.error.duplicate");
				return error();
			}
            if (!wrapper.isEditMode()) {
                context.getExternalContext().getSessionMap().put("repositorySystemConfirm",
                        messages.getMessage("resource.file.fileAdded",
                                new String[] {wrapper.getFileResource().getName(),
                                wrapper.getFileResource().getParentFolder()},
                                LocaleContextHolder.getLocale()));
            }
			return yes();
		}
		return success();
	}

    public Object createFormObject(RequestContext context) {
		FileResourceWrapper formObject = null;
		String resourceUri = context.getRequestParameters().get(
				FILERES_URI_PARAM);			
		if (resourceUri == null) {
			resourceUri = context.getRequestParameters().get(
					"selectedResource");
		}		
		if (resourceUri != null && resourceUri.trim().length() != 0) {
			Resource resource = (Resource) repository.getResource(null,
					resourceUri);			
			
			if (resource == null) {
				throw new JSException("jsexception.could.not.find.resource.with.uri", new Object[] {resourceUri});
			}	
			log("Found resource with uri=" + resourceUri);
			formObject = new FileResourceWrapper();
			formObject.setFileResource((FileResource) resource);
			formObject.setMode(BaseDTO.MODE_STAND_ALONE_EDIT);
			formObject.setLocated(true);
		}
		if (formObject == null) {
			formObject = new FileResourceWrapper();
			formObject.setMode(BaseDTO.MODE_STAND_ALONE_NEW);
			// set default option for datasource type
			String parentFolder = context.getRequestParameters().get(
					PARENT_FOLDER_ATTR);
			if (parentFolder == null) {
				parentFolder = (String)context.getRequestParameters().get("ParentFolderUri");
				context.getFlowScope().put(PARENT_FOLDER_ATTR, parentFolder);
			}
			FileResource fileResource = (FileResource) repository.newResource(
					null, FileResource.class);
			fileResource.setParentFolder(parentFolder);
			fileResource.setVersion(FileResource.VERSION_NEW);
			formObject.setFileResource(fileResource);
		}
		return formObject;
	}

	/**
	 * Gets the repository service instance
	 * 
	 * @return
	 */
	public RepositoryService getRepository() {
		return repository;
	}

	/**
	 * Sets the Repository service instace, necessary to allow Spring inject the
	 * instance of Repository service
	 * 
	 * @param repository
	 */
	public void setRepository(RepositoryService repository) {
		this.repository = repository;
	}

	public RepositoryConfiguration getConfiguration()
	{
		return configuration;
	}

	public void setConfiguration(RepositoryConfiguration configuration)
	{
		this.configuration = configuration;
	}

	public String getExpectedFileTypeParameter() {
		return expectedFileTypeParameter;
	}

	public void setExpectedFileTypeParameter(String expectedFileTypeParameter) {
		this.expectedFileTypeParameter = expectedFileTypeParameter;
	}

	public String getExpectedFileTypeAttribute() {
		return expectedFileTypeAttribute;
	}

	public void setExpectedFileTypeAttribute(String expectedFileTypeAttribute) {
		this.expectedFileTypeAttribute = expectedFileTypeAttribute;
	}

	/**
	 * Registers a byte array editor to allow spring handle File uploads as byte
	 * arrays
	 */
	protected void initBinder(RequestContext context, DataBinder binder) {
		binder.registerCustomEditor(byte[].class,
				new ByteArrayMultipartFileEditor());
	}

	protected void log(String text) {
		log.debug(text);
	}

	public static String getFORM_OBJECT_KEY() {
		return FORM_OBJECT_KEY;
	}

	protected void doBind(RequestContext context, DataBinder binder) throws Exception {
        super.doBind(context, binder);
        FileResourceWrapper res = (FileResourceWrapper) binder.getTarget();
		res.afterBind();
	}

    public void setFileResourceTreeDataProvider(TypedTreeDataProvider typedTreeDataProvider) {
        this.typedTreeDataProvider = typedTreeDataProvider;
    }
}
