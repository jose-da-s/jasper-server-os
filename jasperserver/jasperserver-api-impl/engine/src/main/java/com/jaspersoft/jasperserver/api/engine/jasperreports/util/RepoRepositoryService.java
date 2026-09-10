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
package com.jaspersoft.jasperserver.api.engine.jasperreports.util;

import com.jaspersoft.jasperserver.api.common.domain.ExecutionContext;
import com.jaspersoft.jasperserver.api.metadata.common.domain.ContentResource;
import com.jaspersoft.jasperserver.api.metadata.common.domain.FileResource;
import com.jaspersoft.jasperserver.api.metadata.common.domain.FileResourceData;
import com.jaspersoft.jasperserver.api.metadata.common.domain.Folder;
import com.jaspersoft.jasperserver.api.metadata.common.domain.ResourceContainer;
import com.jaspersoft.jasperserver.api.metadata.common.service.impl.hibernate.util.RepositoryUtils;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.repo.PersistenceService;
import net.sf.jasperreports.repo.Resource;
import net.sf.jasperreports.repo.StreamRepositoryService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

/**
 * @author Lucian Chirita (lucianc@users.sourceforge.net)
 * @version $Id$
 */
public class RepoRepositoryService implements StreamRepositoryService {

	private static final Log log = LogFactory.getLog(RepoRepositoryService.class);
	
	public final static String REPOSITORY_PROTOCOL = "repo";
	public final static String URL_PROTOCOL_PREFIX = REPOSITORY_PROTOCOL + ':';
	
	private Map<Class<?>, PersistenceService> persistenceServices;
			
	@Override
	public Resource getResource(String uri) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void saveResource(String uri, Resource resource) {
		throw new UnsupportedOperationException();
	}

	@Override
    public InputStream getInputStream(String uri) {
		return getInputStream(null, uri);
	}

	@Override
    public InputStream getInputStream(net.sf.jasperreports.repo.RepositoryContext context, String uri) {
        RepositoryContext repositoryContext = RepositoryUtil.getThreadRepositoryContext();
        if (repositoryContext == null || repositoryContext.getCompiledReportProvider() == null) {
            if (log.isDebugEnabled()) {
                log.debug("No repository context for resource " + uri);
            }
            return null;
        }
        
        String contextURI = null;
        if (context != null && context.getResourceContext() != null) {
        	contextURI = context.getResourceContext().getContextLocation();
        }
        if (contextURI == null) {
        	contextURI = repositoryContext.getContextURI();
        }
        
        if (contextURI == null) {
            if (log.isDebugEnabled()) {
                log.debug("No context URI for resource " + uri);
            }
            return null;
        }
        
        //inherited from RepositoryURLHandlerFactory
        uri = uri.trim();

        // filtering cases when the uri is (almost) obviously not a repository path.
        // we could use the repository resource names patters as validation but I'm not sure
        // that the validation is enforced everywhere.
        if (uri.startsWith("file:") || uri.startsWith("http:") || uri.startsWith("https:")) {
            return null;
        }

        if (uri.startsWith(URL_PROTOCOL_PREFIX)) {// handling repo: URLs
            uri = uri.substring(URL_PROTOCOL_PREFIX.length());
        }
        
        ExecutionContext executionContext = repositoryContext.getExecutionContext();
        
        if (!uri.startsWith(Folder.SEPARATOR)) {
            ResourceContainer reportUnit = repositoryContext.getReportUnit();
            if (reportUnit != null) {
            	FileResource resource = reportUnit.getResourceLocal(uri);
                if (resource != null && resource.hasData()) {
                    if (log.isDebugEnabled()) {
                        log.debug("Loading resource \"" + resource.getName()
                                + "\" from in-memory report unit");
                    }
                    InputStream data = resource.getDataStream();
                    if (resource.getFileType().equals(FileResource.TYPE_JRXML)) {
                        data = repositoryContext.getCompiledReportProvider().getCompiledReport(
                                executionContext,
                                data);//FIXME not currently used, but should do autoUpdateJRXMLResource
                    } else if (resource.getFileType().equals(FileResource.TYPE_STYLE_TEMPLATE)) {
                    	InputStream rawData = data;
                    	try {
	                    	byte[] v7TemplateData = CustomJRXmlTemplateLoader.convertLegacyToV7(data);
	                    	data = new ByteArrayInputStream(v7TemplateData);
                    	} catch (JRException e) {
                    		log.error("could not convert v6 JRTX to v7, loading raw file content", e);
                    		data = rawData;
                    	}
                    }
                    if (data != null) {
                    	return data;
                    }
                }
            }
        }

        String path = RepositoryUtils.resolveRelativePath(contextURI, uri);
        com.jaspersoft.jasperserver.api.metadata.common.service.RepositoryService repository =
                repositoryContext.getRepository();

        if (log.isDebugEnabled()) {
            log.debug("loading repository resource " + path
                    + ", context path " + contextURI
                    + ", uri " + uri);
        }

        final com.jaspersoft.jasperserver.api.metadata.common.domain.Resource resource =
                repository.getResource(executionContext, path);
        if (resource == null) {
            if (log.isDebugEnabled()) {
                log.debug("Resource \"" + path + "\" not found in the repository");
            }
            return null;
        }

        InputStream data = null;
        if (resource instanceof FileResource) {
            FileResource fileResource = (FileResource) resource;
            while (fileResource != null && fileResource.isReference()) {
                if (log.isDebugEnabled()) {
                    log.debug("loading repository resource " + fileResource.getReferenceURI());
                }

                fileResource = (FileResource) repository.getResource(executionContext,
                        fileResource.getReferenceURI(), FileResource.class);
            }
            if (fileResource != null) {
                if (fileResource.getFileType().equals(FileResource.TYPE_JRXML)) {
                    // this would conceptually belong to the persistence service, but we don't have the context there
                    data = repositoryContext.getCompiledReportProvider().getCompiledReport(executionContext, path);
                } else {
                    FileResourceData resourceData = repository.getResourceData(executionContext, path);
                    data = resourceData.getDataStream();
                }

                if (fileResource.getFileType().equals(FileResource.TYPE_STYLE_TEMPLATE)) {
                	InputStream rawData = data;
                	try {
                    	byte[] v7TemplateData = CustomJRXmlTemplateLoader.convertLegacyToV7(data);
                    	data = new ByteArrayInputStream(v7TemplateData);
                	} catch (JRException e) {
                		log.error("could not convert v6 JRTX to v7, loading raw file content", e);
                		data = rawData;
                	}
                }

                if (log.isDebugEnabled()) {
                    log.debug("loaded resource of type " + fileResource.getFileType());
                }
            }

        } else if (resource instanceof ContentResource) {
            data = repository.getContentResourceData(executionContext, path).getDataStream();
        }
        return data;
    }

	@Override
	public <K extends Resource> K getResource(String uri, Class<K> resourceType) {
		return getResource(null, uri, resourceType);
	}

	@Override
	public <K extends Resource> K getResource(net.sf.jasperreports.repo.RepositoryContext context, String uri, Class<K> resourceType) {
		PersistenceService persistenceService = persistenceServices.get(resourceType);
		if (persistenceService == null) {
			if (log.isDebugEnabled()) {
				log.debug("Unknown resource type " + resourceType.getClass().getName() + " requested");
			}
			return null;
		}
		
		@SuppressWarnings("unchecked")
		K resource = (K) persistenceService.load(context, uri, this);
		return resource;
	}

	@Override
	public OutputStream getOutputStream(String uri) {
		throw new UnsupportedOperationException();
	}

	public Map<Class<?>, PersistenceService> getPersistenceServices() {
		return persistenceServices;
	}

	public void setPersistenceServices(
			Map<Class<?>, PersistenceService> persistenceServices) {
		this.persistenceServices = persistenceServices;
	}

}
