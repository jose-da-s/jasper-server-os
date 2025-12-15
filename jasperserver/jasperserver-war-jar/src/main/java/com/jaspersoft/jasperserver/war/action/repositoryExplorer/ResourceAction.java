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

package com.jaspersoft.jasperserver.war.action.repositoryExplorer;

import com.jaspersoft.jasperserver.api.JSDuplicateResourceException;
import com.jaspersoft.jasperserver.api.JSException;
import com.jaspersoft.jasperserver.api.common.domain.ExecutionContext;
import com.jaspersoft.jasperserver.api.common.util.StaticExecutionContextProvider;
import com.jaspersoft.jasperserver.api.engine.common.service.SecurityContextProvider;
import com.jaspersoft.jasperserver.api.engine.scheduling.service.ReportSchedulingService;
import com.jaspersoft.jasperserver.api.metadata.common.domain.Folder;
import com.jaspersoft.jasperserver.api.metadata.common.domain.RepositoryConfiguration;
import com.jaspersoft.jasperserver.api.metadata.common.domain.Resource;
import com.jaspersoft.jasperserver.api.metadata.common.domain.client.FolderImpl;
import com.jaspersoft.jasperserver.api.metadata.common.domain.client.ResourceLookupImpl;
import com.jaspersoft.jasperserver.api.metadata.common.service.JSResourceNotFoundException;
import com.jaspersoft.jasperserver.api.metadata.common.service.RepositoryService;
import com.jaspersoft.jasperserver.api.metadata.common.service.impl.RepositorySecurityChecker;
import com.jaspersoft.jasperserver.api.metadata.common.util.RepositoryLabelIDHelper;
import com.jaspersoft.jasperserver.api.metadata.view.domain.FilterCriteria;
import com.jaspersoft.jasperserver.war.action.reportManager.ResourceListModel;
import com.jaspersoft.jasperserver.war.action.reportManager.ResourceRowModel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.webflow.action.FormAction;
import org.springframework.webflow.core.collection.ParameterMap;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;


/**
 * @author achan
 */
//public class FolderAction extends MultiAction implements Serializable {
public class ResourceAction extends FormAction {

    public static final String AJAX_REPORT_MODEL = "ajaxResponseModel";


    private RepositoryService repositoryService;
    private RepositorySecurityChecker repositoryServiceSecurityChecker;
    private MessageSource messageSource;
    private ReportSchedulingService schedulingService;
    private int pagination;
    private String showMoveCopyConfirmation;
    private SecurityContextProvider securityContextProvider;
    private RepositoryConfiguration configuration;

    protected final Log log = LogFactory.getLog(this.getClass());

    public SecurityContextProvider getSecurityContextProvider() {
        return securityContextProvider;
    }

    public void setSecurityContextProvider(SecurityContextProvider securityContextProvider) {
        this.securityContextProvider = securityContextProvider;
    }

    public RepositoryConfiguration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(RepositoryConfiguration configuration) {
        this.configuration = configuration;
    }

    // Actions

    public Event initEvent(RequestContext context) {
        String tenantId = securityContextProvider.getContextUser().getTenantId();

        context.getRequestScope().put("organizationId", tenantId);
        context.getRequestScope().put("publicFolderUri", configuration.getPublicFolderUri());

        return success();
    }

    public Event createFolder(RequestContext context) {

        try {
            String parentFolderUri = (String) context.getRequestParameters().get("FolderUri");
            String folderName = URLDecoder.decode((String) context.getRequestParameters().get("FolderName"), "UTF-8");
            String folderDescription = URLDecoder.decode((String) context.getRequestParameters().get("FolderDescription"), "UTF-8");
            Folder folder = new FolderImpl();
            folder.setParentFolder(parentFolderUri);
            String generatedId = RepositoryLabelIDHelper.generateIdBasedOnLabel(repositoryService, parentFolderUri, folderName);
            folder.setName(generatedId);
            folder.setLabel(folderName);
            folder.setDescription(folderDescription);
            try {
                repositoryService.saveFolder(null, folder);
            } catch (JSDuplicateResourceException e) {
                context.getRequestScope().put(AJAX_REPORT_MODEL, e.toString());
                return error();
            } catch (JSResourceNotFoundException f) {
                context.getRequestScope().put(AJAX_REPORT_MODEL, f.toString());
                return error();
            }
            context.getRequestScope().put(AJAX_REPORT_MODEL, generatedId);
        } catch (UnsupportedEncodingException e) {

        }
        return success();
    }

    public Event doesFolderExist(RequestContext context) {

        try {
            String parentFolderUri = URLDecoder.decode((String) context.getRequestParameters().get("FolderUri"), "UTF-8");
            String folderName = URLDecoder.decode((String) context.getRequestParameters().get("FolderName"), "UTF-8");
            if (folderName == null) {
                folderName = "";
            }

            try {
                List repoFolderList = repositoryService.getSubFolders(null, parentFolderUri);

                FilterCriteria criteria = FilterCriteria.createFilter();
                criteria.addFilterElement(FilterCriteria.createParentFolderFilter(parentFolderUri));

                List resources = repositoryService.loadResourcesList(exContext(), criteria);
                repoFolderList.addAll(resources);

                for (int i = 0; i < repoFolderList.size(); i++) {
                    if (repoFolderList.get(i) instanceof FolderImpl) {
                        FolderImpl repoFolder = (FolderImpl) repoFolderList.get(i);
                        if (folderName.equalsIgnoreCase(repoFolder.getLabel())) {
                            context.getRequestScope().put(AJAX_REPORT_MODEL, "DUPLICATE_DISPLAY_NAME");
                            return success();
                        }
                    } else if (repoFolderList.get(i) instanceof ResourceLookupImpl) {
                        ResourceLookupImpl res = (ResourceLookupImpl) repoFolderList.get(i);
                        if (folderName.equalsIgnoreCase(res.getLabel())) {
                            context.getRequestScope().put(AJAX_REPORT_MODEL, "DUPLICATE_DISPLAY_NAME");
                            return success();
                        }
                    }
                }
            } catch (Exception e) {
                context.getRequestScope().put(AJAX_REPORT_MODEL, e.toString());
                return success();
            }

            context.getRequestScope().put(AJAX_REPORT_MODEL, "OK");
        } catch (UnsupportedEncodingException e) {

        }
        return success();
    }

    public Event deleteFolder(RequestContext context) {

        try {
            String parentFolderUri = URLDecoder.decode((String) context.getRequestParameters().get("FolderUri"), "UTF-8");

            try {
                repositoryService.deleteFolder(null, parentFolderUri);
            } catch (Exception e) {
                context.getRequestScope().put(AJAX_REPORT_MODEL, "ERROR:MISSING_RESOURCE");
                return success();
            }

            context.getRequestScope().put(AJAX_REPORT_MODEL, "OK");
        } catch (UnsupportedEncodingException e) {
        }
        return success();
    }


    public Event deleteResources(RequestContext context) {

        String errorUri = "";
        try {
            String parentFolderUri = URLDecoder.decode((String) context.getRequestParameters().get("ResourceList"), "UTF-8");

            // /reports/hello,/reports/world
            // parse the resource list delimited by ,
            StringTokenizer str = new StringTokenizer(parentFolderUri, ",");
            String currentResource = "";


            while (str.hasMoreTokens()) {
                try {
                    currentResource = (String) str.nextToken();
                    repositoryService.deleteResource(null, currentResource);
                } catch (ConstraintViolationException f) {
                    if (!errorUri.equals("")) {
                        errorUri = errorUri + "," + currentResource;
                    } else {
                        errorUri = currentResource;
                    }
                } catch (JSResourceNotFoundException g) {
                    // don't do anything.
                } catch (Exception e) {
                    if (!errorUri.equals("")) {
                        errorUri = errorUri + "," + currentResource;
                    } else {
                        errorUri = currentResource;
                    }
                }
            }
        } catch (UnsupportedEncodingException e) {
        }

        if (errorUri.equals("")) {
            context.getRequestScope().put(AJAX_REPORT_MODEL, "OK");
            return success();
        } else {
            context.getRequestScope().put(AJAX_REPORT_MODEL, "ERROR," + errorUri);
            return success();
        }
    }


    public Event getResources(RequestContext context) {

        try {
            String folderUri = URLDecoder.decode((String) context.getRequestParameters().get("FolderUri"), "UTF-8");
            String pageNumberString = (String) context.getRequestParameters().get("PageNumber");
            int pageNumber = 0;
            if (pageNumberString != null) {
                try {
                    pageNumber = Integer.parseInt(pageNumberString);
                } catch (NumberFormatException e) {
                    pageNumber = 0;
                }
            }
            if (pageNumber < 0) {
                pageNumber = 0;
            }
            // given a folder, get all the resources,
            FilterCriteria criteria = FilterCriteria.createFilter();
            criteria.addFilterElement(FilterCriteria.createParentFolderFilter(folderUri));

            List resources = repositoryService.loadResourcesList(exContext(), criteria);
            ResourceListModel model = returnModel(resources, context, pageNumber);
            model.setParentUri(folderUri);
            context.getRequestScope().put(AJAX_REPORT_MODEL, model);
        } catch (UnsupportedEncodingException e) {
        }
        return success();
    }

    public ResourceListModel returnModel(List resources, RequestContext context, int pageNumber) {

        ResourceListModel listModel = new ResourceListModel();
        ArrayList listOfRows = new ArrayList();
        listModel.setSize(resources.size());
        int nResources = resources.size();
        listModel.setTotalPages(((nResources - 1) / pagination) + 1);
        listModel.setCurrentPageNumber(pageNumber + 1);
        int upperLimit = (((pagination * pageNumber) + pagination) > nResources ? nResources : ((pagination * pageNumber) + pagination));
        for (int i = (pagination * pageNumber); i < upperLimit; i++) {
            ResourceLookupImpl res = (ResourceLookupImpl) resources.get(i);
            ResourceRowModel row = new ResourceRowModel();
            row.setName(res.getLabel());
            row.setHiddenName(res.getLabel().replaceAll("'", "\\\\'"));
            row.setId(res.getURIString());
            row.setResourceUrl("");
            String description = res.getDescription();
            if ((description == null) || "".equals(description)) {
                description = "&nbsp;";
            }
            row.setDescription((description == null) ? "" : description);
            row.setType(messageSource.getMessage("resource." + res.getResourceType() + ".label", null, LocaleContextHolder.getLocale()));
            row.setResourceType(res.getResourceType());
            row.setCreationDate(res.getCreationDate());
            row.setUpdateDate(res.getUpdateDate());
            row.setScheduled(false);
            row.setSelected(false);
            row.setHasSavedOptions(false);
            row.setWritable(repositoryServiceSecurityChecker.isEditable(res));
            row.setDeletable(repositoryServiceSecurityChecker.isRemovable(res));
            row.setAdministrable(repositoryServiceSecurityChecker.isAdministrable(res));
            List jobs = schedulingService.getScheduledJobSummaries(exContext(), res.getURIString());
            if (jobs.size() > 0) {
                row.setScheduled(true);
            } else {
                row.setScheduled(false);
            }
            String resourceType = res.getResourceType();

            if (resourceType.endsWith("AdhocReportUnit") || resourceType.endsWith("ReportUnit")) {
                //row.setResourceUrl("javascript:document.frm.resource.value='"+ res.getURIString()+"';document.frm.viewReport.click();");
                row.setResourceUrl("flow.html?_flowId=viewReportFlow&reportUnit=" + res.getURIString() + "&standAlone=true&ParentFolderUri=" + res.getParentURI().substring(5));
            } else if (resourceType.endsWith("ContentResource")) {
                row.setResourceUrl("fileview/fileview" + res.getURIString());
                row.setContentType(true);
            } else if (resourceType.endsWith("ReportOptions")) {
                row.setResourceUrl("flow.html?_flowId=viewReportFlow&reportOptionsURI=" + res.getURIString() + "&standAlone=true&ParentFolderUri=" + res.getParentURI().substring(5));
            }

            listOfRows.add(row);
        }
        listModel.setRows(listOfRows);
        // enable/disable navigaiton buttons
        listModel.setFirst(pageNumber > 0);
        listModel.setPrevious(pageNumber > 0);
        listModel.setNext(((pagination * pageNumber) + pagination) < nResources);
        listModel.setLast(((pagination * pageNumber) + pagination) < nResources);
        return listModel;
    }

    public Event updateResourceProperties(RequestContext context) {

        try {
            String labelProperty = URLDecoder.decode((String) context.getRequestParameters().get("label"), "UTF-8");
            String descProperty = URLDecoder.decode((String) context.getRequestParameters().get("desc"), "UTF-8");
            String idProperty = URLDecoder.decode((String) context.getRequestParameters().get("id"), "UTF-8");
            String folderUri = URLDecoder.decode((String) context.getRequestParameters().get("folderUri"), "UTF-8");



            String returnStatus = "OK";
            // validate to make sure no more than one label in the same directory
            FilterCriteria criteria = FilterCriteria.createFilter();
            //criteria.addFilterElement(FilterCriteria.createPropertyEqualsFilter("label", labelProperty));
            criteria.addFilterElement(FilterCriteria.createParentFolderFilter(folderUri));
            List resources = repositoryService.loadClientResources(criteria);
            List folders = repositoryService.getSubFolders(exContext(), folderUri);
            // join the lists
            resources.addAll(folders);
            Resource matchingResource = null;

            for (int i = 0; i < resources.size(); i++) {
                String label = ((Resource) resources.get(i)).getLabel();
                String id = ((Resource) resources.get(i)).getName();

                if (label.equalsIgnoreCase(labelProperty)) {
                    if (!id.equalsIgnoreCase(idProperty)) {
                        returnStatus = "DUPLICATE_LABEL_ERROR";
                        context.getRequestScope().put(AJAX_REPORT_MODEL, returnStatus);
                        return success();
                    }
                }
                if (id.equalsIgnoreCase(idProperty)) {
                    matchingResource = (Resource) resources.get(i);
                }

            }


            // the resource is no longer available
            if (matchingResource == null) {
                context.getRequestScope().put(AJAX_REPORT_MODEL, "ERROR:MISSING_RESOURCE");
                return success();

            }

            // update the name and description
            if ((!labelProperty.equalsIgnoreCase(matchingResource.getLabel())) || (!descProperty.equalsIgnoreCase(matchingResource.getDescription()))) {
                matchingResource.setLabel(labelProperty);
                matchingResource.setDescription(descProperty);

                if (matchingResource.getResourceType().toLowerCase().contains("folder")) {
                    repositoryService.saveFolder(exContext(), (Folder) matchingResource);
                } else {
                    repositoryService.saveResource(exContext(), matchingResource);
                }
            }
            context.getRequestScope().put(AJAX_REPORT_MODEL, returnStatus);
        } catch (UnsupportedEncodingException e) {
        }
        return success();
    }

    public Event getResourceProperties(RequestContext context) {

        try {
            boolean isFolder = false;
            String resourceUri = URLDecoder.decode((String) context.getRequestParameters().get("resourceUri"), "UTF-8");

            Resource resource = null;
            try {
                resource = repositoryService.getResource(exContext(), resourceUri);
            } catch (JSException e) {
                context.getRequestScope().put(AJAX_REPORT_MODEL, "ERROR:PATH_NOT_VISIBLE_IN_ORGANIZATION_CONTEXT");
                return success();
            }
            // if it's null, then a folder
            if (resource == null) {
                resource = repositoryService.getFolder(exContext(), resourceUri);
                isFolder = true;
            }

            // the resource or folder has been deleted
            if (resource == null) {
                context.getRequestScope().put(AJAX_REPORT_MODEL, "ERROR:MISSING_RESOURCE");
                return success();
            }


            boolean isWritable = repositoryServiceSecurityChecker.isEditable(resource);
            boolean isDeletable = repositoryServiceSecurityChecker.isRemovable(resource);
            boolean isAdministrable = repositoryServiceSecurityChecker.isAdministrable(resource);

            // store in json
            StringBuffer sb = new StringBuffer();
            sb.append('{');

            if (resource.getDescription() == null) {
                resource.setDescription("");
            }
            SimpleDateFormat df = new SimpleDateFormat("MM-dd-yyyy HH:mm");
            sb.append("\"name\":\"").append(resource.getName()).append("\",")
                    .append("\"label\":\"").append(escape(resource.getLabel())).append("\",")
                    .append("\"date\":\"").append(df.format(resource.getCreationDate())).append("\",")
                    .append("\"updateDate\":\"").append(df.format(resource.getUpdateDate())).append("\",")
                    .append("\"writable\":\"").append(isWritable ? "true" : "false").append("\",")
                    .append("\"deletable\":\"").append(isDeletable ? "true" : "false").append("\",")
                    .append("\"administrable\":\"").append(isAdministrable ? "true" : "false").append("\",")
                    .append("\"description\":\"").append(escape(resource.getDescription().replace("\r", " ").replace("\n", " "))).append("\",");

            if (!isFolder) {
                sb.append("\"type\":\"").append(messageSource.getMessage("resource." + resource.getResourceType() + ".label", null, LocaleContextHolder.getLocale())).append("\"");
            } else {
                sb.append("\"type\":\"").append(messageSource.getMessage("label.folder", null, LocaleContextHolder.getLocale())).append("\"");
            }
            sb.append('}');
            context.getRequestScope().put(AJAX_REPORT_MODEL, sb.toString());
        } catch (UnsupportedEncodingException e) {
        }

        return success();

    }

    public Event getBreadCrumb(RequestContext context) {

        try {
            String folderUri = URLDecoder.decode((String) context.getRequestParameters().get("FolderUri"), "UTF-8");
            int fromIndex = 1;
            StringBuffer sb = new StringBuffer();
            sb.append('{');

            Resource rootFolder = repositoryService.getFolder(null, "/");
            sb.append("\"/\":").append("\"" + rootFolder.getLabel() + "\"");

            if (folderUri.length() > 1) {
                while ((fromIndex = folderUri.indexOf('/', fromIndex)) != -1) {
                    String currentUri = folderUri.substring(0, folderUri.indexOf('/', fromIndex));
                    Folder folder = repositoryService.getFolder(exContext(), currentUri);

                    if (folder == null) {
                        break;
                    }

                    String displayLabel = folder.getLabel();
                    fromIndex++;
                    sb.append(",\"" + escape(currentUri) + "\":\"").append(escape(displayLabel)).append("\"");
                }
            }
            Resource res = null;
            try {
                res = repositoryService.getFolder(exContext(), folderUri);
            } catch (Exception e) {
            }
            String displayLabel = res != null ? res.getLabel() : "";
            sb.append(",\"" + escape(folderUri) + "\":\"").append(escape(displayLabel)).append("\"");

            sb.append('}');
            context.getRequestScope().put(AJAX_REPORT_MODEL, sb.toString());
        } catch (Exception e) {
            log.error("ERROR: getting folder failed", e);
        }
        return success();
    }


    public Event getUriDisplayLabelList(RequestContext context) {

        try {
            String uriList = URLDecoder.decode((String) context.getRequestParameters().get("uriList"), "UTF-8");
            String type = (String) context.getRequestParameters().get("type");

            StringTokenizer str = new StringTokenizer(uriList, ",");
            StringBuffer sb = new StringBuffer();
            sb.append('{');
            int counter = 0;
            int fromIndex = 1;
            Resource rootFolder = repositoryService.getFolder(null, "/");
            while (str.hasMoreElements()) {
                String currentString = (String) str.nextElement();
                StringBuffer currentDisplayLabel = new StringBuffer("");
                if (currentString.equals("/")) {
                    sb.append("\"" + counter + "\":\"" + "/" + rootFolder.getLabel().replaceAll("<", "&lt;") + "\"");
                    counter++;
                    continue;
                } else {
                    currentDisplayLabel.append("/" + rootFolder.getLabel().replaceAll("<", "&lt;"));
                }
                if (currentString.length() > 1) {
                    int lastIndex = currentString.lastIndexOf("/");
                    while ((fromIndex = currentString.indexOf('/', fromIndex)) != -1) {
                        String currentUri = currentString.substring(0, currentString
                                .indexOf('/', fromIndex));

                        try {
                            currentDisplayLabel.append("/").append(repositoryService.getFolder(
                                    exContext(), currentUri).getLabel().replaceAll("<", "&lt;"));
                        } catch (Exception e) {
                            currentDisplayLabel.append("/").append(repositoryService.getResource(
                                    exContext(), currentUri).getLabel().replaceAll("<", "&lt;"));
                        }

                        if (lastIndex == fromIndex) {
                            break;
                        }
                        fromIndex++;
                    }
                    if ((type.equals("resource")) && (counter > 0)) {
                        currentDisplayLabel.append("/").append(repositoryService.getResource(exContext(), currentString).getLabel().replaceAll("<", "&lt;"));
                    } else if (type.equals("folder") || ((type.equals("resource")) && (counter == 0))) {
                        currentDisplayLabel.append("/").append(repositoryService.getFolder(
                                exContext(), currentString).getLabel().replaceAll("<", "&lt;"));
                    }
                    // put each full display label into json
                    if (counter == 0) {
                        sb.append("\"" + counter + "\":\"" + currentDisplayLabel.toString() + "\"");
                    } else {
                        sb.append(",\"" + counter + "\":\"" + currentDisplayLabel.toString() + "\"");
                    }
                }
                counter++;
                fromIndex = 1;
            }

            sb.append('}');
            context.getRequestScope().put(AJAX_REPORT_MODEL, sb.toString());
        } catch (UnsupportedEncodingException e) {
        }

        return success();
    }

    public Event getNewFolderName(RequestContext context) {

        try {
            String folderUri = URLDecoder.decode((String) context.getRequestParameters().get("FolderUri"), "UTF-8");
            int largestNumber = 1;
            boolean newFolderExist = false;
            String sb = messageSource.getMessage("RM_CREATE_FOLDER_POP_UP_HEADER", null, LocaleContextHolder.getLocale());
            List repoFolderList = repositoryService.getSubFolders(null, folderUri);

            for (int i = 0; i < repoFolderList.size(); i++) {
                String curFolderName = (String) ((Folder) repoFolderList.get(i)).getLabel();
                if (curFolderName.equals(sb)) {
                    newFolderExist = true;
                    if (largestNumber == 1) {
                        largestNumber++;
                    }
                    continue;
                }

                if (curFolderName.startsWith(sb)) {
                    int startParenthsisIndex = curFolderName.indexOf('(');
                    int endParenthsisIndex = curFolderName.indexOf(')');

                    if ((startParenthsisIndex != -1) && (endParenthsisIndex != -1) && (startParenthsisIndex < endParenthsisIndex) && (startParenthsisIndex == (sb.length() + 1))) {
                        int curNumber = -1;
                        try {
                            curNumber = Integer.parseInt(curFolderName.substring(startParenthsisIndex + 1, endParenthsisIndex));
                        } catch (NumberFormatException e) {
                        }
                        if (curNumber != -1) {
                            if (curNumber >= largestNumber) {
                                largestNumber = curNumber + 1;
                            }
                        }
                    }
                }
            }
            if ((largestNumber == 1) || (!newFolderExist)) {
                context.getRequestScope().put(AJAX_REPORT_MODEL, sb);
            } else {
                context.getRequestScope().put(AJAX_REPORT_MODEL, sb + " (" + largestNumber + ")");
            }
        } catch (UnsupportedEncodingException e) {
        }

        return success();
    }

    public Event copyFolder(RequestContext context) {

        try {
            String sourceUri = URLDecoder.decode((String) context.getRequestParameters().get("sourceUri"), "UTF-8");
            String destUri = URLDecoder.decode((String) context.getRequestParameters().get("destUri"), "UTF-8");
            Folder destinationFolder = null;


            int lastIndex = sourceUri.lastIndexOf("/");
            if (lastIndex != -1) {
                destUri = destUri + "/" + sourceUri.substring(lastIndex + 1);
            }

            StringBuffer sb = new StringBuffer();
            sb.append('{');

            // get sourceUri label
            String sourceLabel = repositoryService.getFolder(null, sourceUri).getLabel();
            // check if the label already exist in the destination folder
            String trimDestUri = destUri.substring(0, destUri.lastIndexOf('/'));
            if (doesObjectLabelExist(trimDestUri, sourceLabel)) {
                sb.append("\"status\":\"FAILED\"");
                sb.append('}');
                context.getRequestScope().put(AJAX_REPORT_MODEL, sb.toString());
                return no();
            }




            try {
                destinationFolder = repositoryService.copyFolder(null, sourceUri, destUri);
            } catch (Exception e) {
                e.printStackTrace();
                sb.append("\"status\":\"FAILED\"");
                sb.append('}');
                context.getRequestScope().put(AJAX_REPORT_MODEL, sb.toString());
                return no();
            }
            sb.append("\"status\":\"SUCCESS\",");
            sb.append("\"id\":\"" + destinationFolder.getURIString() + "\"");
            sb.append('}');
            context.getRequestScope().put(AJAX_REPORT_MODEL, sb.toString());
        } catch (UnsupportedEncodingException e) {
        }
        return success();
    }

    public Event moveFolder(RequestContext context) {
        try {
            String sourceUri = URLDecoder.decode((String) context.getRequestParameters().get("sourceUri"), "UTF-8");
            String destUri = URLDecoder.decode((String) context.getRequestParameters().get("destUri"), "UTF-8");

            StringBuffer sb = new StringBuffer();
            sb.append('{');
            // get sourceUri label
            String sourceLabel = repositoryService.getFolder(null, sourceUri).getLabel();
            // check if the label already exist in the destination folder
            if (doesObjectLabelExist(destUri, sourceLabel)) {
                sb.append("\"status\":\"FAILED\"");
                sb.append('}');
                context.getRequestScope().put(AJAX_REPORT_MODEL, sb.toString());
                return no();
            }
            try {
                repositoryService.moveFolder(null, sourceUri, destUri);
            } catch (Exception e) {
                e.printStackTrace();
                sb.append("\"status\":\"FAILED\"");
                sb.append('}');
                context.getRequestScope().put(AJAX_REPORT_MODEL, sb.toString());
                return no();
            }
            sb.append("\"status\":\"SUCCESS\",");
            if ("/".equals(destUri)) {
                sb.append("\"id\":\"" + sourceUri.substring(sourceUri.lastIndexOf("/")) + "\"");
            } else {
                sb.append("\"id\":\"" + destUri + sourceUri.substring(sourceUri.lastIndexOf("/")) + "\"");
            }
            sb.append('}');
            context.getRequestScope().put(AJAX_REPORT_MODEL, sb.toString());
        } catch (UnsupportedEncodingException e) {
        }
        return success();
    }


    public Event copyResource(RequestContext context) {
        try {
            String sourceUri = URLDecoder.decode((String) context.getRequestParameters().get("sourceUri"), "UTF-8");
            String destUri = URLDecoder.decode((String) context.getRequestParameters().get("destUri"), "UTF-8");
            StringBuffer sb = new StringBuffer();
            sb.append('{');

            StringTokenizer str = new StringTokenizer(sourceUri, ",");
            List resourceURIs = new ArrayList();
            while (str.hasMoreElements()) {
                String currentResource = (String) str.nextElement();

                // get sourceUri label
                String sourceLabel = repositoryService.getResource(null, currentResource).getLabel();
                // check if the label already exist in the destination folder
                if (doesObjectLabelExist(destUri, sourceLabel)) {
                    sb.append("\"status\":\"FAILED\"");
                    sb.append('}');
                    context.getRequestScope().put(AJAX_REPORT_MODEL, sb.toString());
                    return no();
                }

                resourceURIs.add(currentResource);
            }

            try {
                repositoryService.copyResources(null,
                        (String[]) resourceURIs.toArray(new String[resourceURIs.size()]),
                        destUri);
            } catch (Exception e) {
                log.error("Error copying resources", e);
                sb.append("\"status\":\"FAILED\"");
                sb.append('}');
                context.getRequestScope().put(AJAX_REPORT_MODEL, sb.toString());
                return no();
            }

            sb.append("\"status\":\"SUCCESS\"");
            sb.append('}');
            context.getRequestScope().put(AJAX_REPORT_MODEL, sb.toString());
        } catch (UnsupportedEncodingException e) {
        }
        return success();
    }

    public Event moveResource(RequestContext context) {
        try {
            String sourceUri = URLDecoder.decode((String) context.getRequestParameters().get("sourceUri"), "UTF-8");
            String destUri = URLDecoder.decode((String) context.getRequestParameters().get("destUri"), "UTF-8");

            StringTokenizer str = new StringTokenizer(sourceUri, ",");
            StringBuffer sb = new StringBuffer();
            sb.append('{');

            while (str.hasMoreElements()) {
                String currentResource = (String) str.nextElement();
                // get sourceUri label
                String sourceLabel = repositoryService.getResource(null, currentResource).getLabel();
                // check if the label already exist in the destination folder
                if (doesObjectLabelExist(destUri, sourceLabel)) {
                    sb.append("\"status\":\"FAILED\"");
                    sb.append('}');
                    context.getRequestScope().put(AJAX_REPORT_MODEL, sb.toString());
                    return no();
                }
            }


            str = new StringTokenizer(sourceUri, ",");
            while (str.hasMoreElements()) {
                String currentResource = (String) str.nextElement();
                try {
                    repositoryService.moveResource(null, currentResource, destUri);
                } catch (Exception e) {
                    e.printStackTrace();
                    sb.append("\"status\":\"FAILED\"");
                    sb.append('}');
                    context.getRequestScope().put(AJAX_REPORT_MODEL, sb.toString());
                    return no();
                }
            }
            sb.append("\"status\":\"SUCCESS\"");
            sb.append('}');
            context.getRequestScope().put(AJAX_REPORT_MODEL, sb.toString());
        } catch (UnsupportedEncodingException e) {
        }
        return success();
    }

    private boolean doesObjectLabelExist(String parentFolderUri, String objectLabel) {

        if (objectLabel == null) {
            objectLabel = "";
        }

        try {
            List repoFolderList = repositoryService.getSubFolders(null, parentFolderUri);
            FilterCriteria criteria = FilterCriteria.createFilter();
            criteria.addFilterElement(FilterCriteria.createParentFolderFilter(parentFolderUri));

            List resources = repositoryService.loadResourcesList(null, criteria);
            repoFolderList.addAll(resources);

            for (int i = 0; i < repoFolderList.size(); i++) {
                if (repoFolderList.get(i) instanceof FolderImpl) {
                    FolderImpl repoFolder = (FolderImpl) repoFolderList.get(i);
                    if (objectLabel.equalsIgnoreCase(repoFolder.getLabel())) {
                        return true;
                    }
                } else if (repoFolderList.get(i) instanceof ResourceLookupImpl) {
                    ResourceLookupImpl res = (ResourceLookupImpl) repoFolderList.get(i);
                    if (objectLabel.equalsIgnoreCase(res.getLabel())) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return false;

    }


    protected String escape(String str) {
        return (str == null) ? null : str.replace("\"", "\\\"").replace("<", "&lt;").replace(">", "&gt;").replace("&", "&amp;");
    }


    // Getters and Setters


    public ExecutionContext exContext() {
        return StaticExecutionContextProvider.getExecutionContext();
    }


    public RepositoryService getRepositoryService() {
        return repositoryService;
    }


    public void setRepositoryService(RepositoryService repositoryService) {
        this.repositoryService = repositoryService;
    }


    public RepositorySecurityChecker getRepositoryServiceSecurityChecker() {
        return repositoryServiceSecurityChecker;
    }


    public void setRepositoryServiceSecurityChecker(
            RepositorySecurityChecker repositoryServiceSecurityChecker) {
        this.repositoryServiceSecurityChecker = repositoryServiceSecurityChecker;
    }

    public MessageSource getMessageSource() {
        return messageSource;
    }

    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public ReportSchedulingService getSchedulingService() {
        return schedulingService;
    }

    public void setSchedulingService(ReportSchedulingService schedulingService) {
        this.schedulingService = schedulingService;
    }

    public int getPagination() {
        return pagination;
    }

    public void setPagination(int pagination) {
        this.pagination = pagination;
    }

    public String getShowMoveCopyConfirmation() {
        return showMoveCopyConfirmation;
    }

    public void setShowMoveCopyConfirmation(String showMoveCopyConfirmation) {
        this.showMoveCopyConfirmation = showMoveCopyConfirmation;
    }

    public Event getConfirmationOption(RequestContext context) {

        context.getRequestScope().put(AJAX_REPORT_MODEL, getShowMoveCopyConfirmation());

        return success();
    }


    public Event generateResourceName(RequestContext context) {
        ParameterMap parameters = context.getRequestParameters();
        String parentFolderUri = parameters.get("ParentFolderUri");
        String newId = parameters.get("resourceLabel").replaceAll(configuration.getResourceIdNotSupportedSymbols(), "_").toLowerCase();
        Resource resource = repositoryService.getResource(null, parentFolderUri + "/" + newId);
        if (resource != null) {
            int counter = 1;
            while (repositoryService.getResource(null, parentFolderUri + "/" + newId + "_" + counter) != null) {
                counter++;
            }
            newId += "_" + counter;
        }

        context.getRequestScope().put(AJAX_REPORT_MODEL, "{\"newId\":\"" + newId + "\"}");
        return success();
    }

}
