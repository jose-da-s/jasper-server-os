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
package com.jaspersoft.jasperserver.remote.handlers;

import com.jaspersoft.jasperserver.api.metadata.common.domain.ContentResource;
import com.jaspersoft.jasperserver.api.metadata.common.domain.FileResourceData;
import com.jaspersoft.jasperserver.api.metadata.common.domain.Resource;
import com.jaspersoft.jasperserver.api.metadata.xml.domain.impl.Argument;
import com.jaspersoft.jasperserver.api.metadata.xml.domain.impl.ResourceDescriptor;
import com.jaspersoft.jasperserver.remote.ResourceDataSource;
import com.jaspersoft.jasperserver.remote.ServiceException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import jakarta.activation.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author Lucian Chirita (lucianc@users.sourceforge.net)
 * @version $Id$
 */
@Service
public class ContentResourceHandler extends RepositoryResourceHandler {

    private static final Log log = LogFactory.getLog(ContentResourceHandler.class);

    public Class getResourceType() {
        return ContentResource.class;
    }

    protected void doGet(Resource resource, ResourceDescriptor descriptor, Map options) throws ServiceException {
        ContentResource contentRes = (ContentResource) resource;
        descriptor.setWsType(ResourceDescriptor.TYPE_CONTENT_RESOURCE);
        descriptor.setResourceProperty(ResourceDescriptor.PROP_CONTENT_RESOURCE_TYPE, contentRes.getFileType());
        if(options == null || !options.containsKey(Argument.NO_RESOURCE_DATA_ATTACHMENT)){
            addFileDataAttachment(descriptor);
        }

        ResourceDescriptor childRD;
        List l = ((ContentResource) resource).getResources();
        for (int i=0 ; i<l.size() ; i++) {
            childRD = getResourcesManagementRemoteService().createResourceDescriptor((Resource) l.get(i));
            descriptor.getChildren().add(childRD);
        }
    }

    @Override
    protected void updateResource(Resource resource, ResourceDescriptor descriptor, Map options) throws ServiceException {

        super.updateResource(resource, descriptor, options);

        ContentResource contentRes = (ContentResource) resource;

        String fileType = descriptor.getResourcePropertyValue(ResourceDescriptor.PROP_CONTENT_RESOURCE_TYPE);
        if (fileType != null) {
            contentRes.setFileType(fileType);
        }

        // TODO: Support attachments...

        if (descriptor.getHasData()) {
                
                DataSource ds = locateDataAttachment(descriptor);
                readData(contentRes, ds);
        }

        List children = descriptor.getChildren();
        List subresources = new ArrayList(children == null ? 0 : children.size());
        if (children != null) {
            for (Iterator it = children.iterator(); it.hasNext();) {
                ResourceDescriptor subdescriptor = (ResourceDescriptor) it.next();
                ContentResource subresource = (ContentResource) createChildResource(subdescriptor);
                subresources.add(subresource);
            }
        }
        contentRes.setResources(subresources);
    }

    protected DataSource locateDataAttachment(ResourceDescriptor resourceDescriptor) throws ServiceException {

        String contentName = resourceDescriptor.getResourcePropertyValue(ResourceDescriptor.PROP_DATA_ATTACHMENT_ID);
        if (contentName == null) {
            contentName = resourceDescriptor.getName();
        }

        DataSource ds = getRunReportService().getInputAttachments().get(contentName);

        if (ds == null) {
            throw new ServiceException(ServiceException.GENERAL_ERROR2,
                    getMessageSource().getMessage("webservices.error.content.resource.data.not.present",
                            new Object[]{contentName}, LocaleContextHolder.getLocale()));
        }

        return ds;
    }

    protected void readData(ContentResource resource, DataSource dataSource) throws ServiceException {

        boolean close = true;
        InputStream dataStream = null;
        try {
            dataStream = dataSource.getInputStream();
            resource.readData(dataStream);

            close = false;
            dataStream.close();
        } catch (IOException e) {
            throw new ServiceException(e);
        } finally {
            if (close && dataStream != null) {
                try {
                    dataStream.close();
                } catch (IOException e) {
                    log.error("Error closing attachment stream", e);
                }
            }
        }
    }


    protected void addFileDataAttachment(ResourceDescriptor descriptor)
    {
        FileResourceData fileData = getRepository().getContentResourceData(null, descriptor.getUriString());
        ResourceDataSource attachmentSource = new ResourceDataSource(descriptor.getName(), fileData);

        final String referenceUri = descriptor.getUriString();
        getRunReportService().getReportAttachments(referenceUri).put(referenceUri, attachmentSource);
        descriptor.setResourceProperty(ResourceDescriptor.PROP_DATA_ATTACHMENT_ID, referenceUri);
    }

}
