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
package com.jaspersoft.jasperserver.api.metadata.common.service.impl.hibernate.persistent;

import com.jaspersoft.jasperserver.api.metadata.common.domain.Folder;
import com.jaspersoft.jasperserver.api.metadata.common.domain.Resource;
import com.jaspersoft.jasperserver.api.metadata.common.domain.ResourceLookup;
import com.jaspersoft.jasperserver.api.metadata.common.domain.client.ResourceLookupImpl;
import com.jaspersoft.jasperserver.api.metadata.common.domain.impl.IdedRepoObject;
import com.jaspersoft.jasperserver.api.metadata.common.service.JSResourceVersionNotMatchException;
import com.jaspersoft.jasperserver.api.metadata.common.service.ResourceFactory;
import com.jaspersoft.jasperserver.api.metadata.common.service.impl.hibernate.PersistentObjectResolver;
import com.jaspersoft.jasperserver.api.metadata.common.service.impl.hibernate.RepoManager;
import com.jaspersoft.jasperserver.api.metadata.common.service.impl.hibernate.util.UpdateDatesIndicator;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.impl.datasource.RepoReportDataSource;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.MappedSuperclass;
import java.util.Date;
import java.util.Set;


/**
 * @author Lucian Chirita (lucianc@users.sourceforge.net)
 * @version $Id$
 */
@MappedSuperclass
@Access(AccessType.PROPERTY)
public abstract class RepoResourceBase implements IdedRepoObject, RepoReportDataSource {
	
	protected long id;
	
	protected int version;
	
	protected Date creationDate;

    protected Date updateDate;

	protected String name = null;
	protected String label = null;
	protected String description = null;

   // protected Set<AccessEvent> accessEvents = null;
    protected Set<RepoReportThumbnail> thumbnails = null;
	protected Set<RepoFavoriteResource> favorites = null;

	public Set<RepoFavoriteResource> getFavorites() {
		return favorites;
	}

	public void setFavorites(Set<RepoFavoriteResource> favorites) {
		this.favorites = favorites;
	}

	protected RepoFolder parent;

    private String resourceType;

	protected RepoResourceBase() {
		version = Resource.VERSION_NEW;
	}
	
	/**
	 * @return
	 * @hibernate.id generator-class="identity"
	 */
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	/**
	 * @hibernate.version column="version" unsaved-value="negative"
	 */
	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	/**
	 * @hibernate.property
	 * 		column="name" type="string" length="100" not-null="true"
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * 
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * @hibernate.property
	 * 		column="label" type="string" length="100" not-null="true"
	 * 
	 */
	public String getLabel()
	{
		return label;
	}

	/**
	 * 
	 */
	public void setLabel(String label)
	{
		this.label = label;
	}

	/**
	 * @hibernate.property
	 * 		column="description" type="string" length="100"
	 * 
	 */
	public String getDescription()
	{
		return description;
	}

	/**
	 * 
	 */
	public void setDescription(String description)
	{
		this.description = description;
	}
	

	/**
	 * @hibernate.many-to-one
	 * 		column="parent_folder"
	 */
	public RepoFolder getParent() {
		return parent;
	}

	public void setParent(RepoFolder parent) {
		this.parent = parent;
	}

	protected abstract Class getClientItf();
	
	public final Class getClientType() {
		return getClientItf();
	}

	public Object toClient(ResourceFactory resourceFactory) {
		Class clientItf = getClientItf();
		//TODO context?
		Resource clientRes = resourceFactory.newResource(null, clientItf);
		return clientRes;
	}
	
	public void copyFromClient(Object objIdent, PersistentObjectResolver resolver){
		copyFrom((Resource) objIdent);
	}

	protected void copyFrom(Resource clientRes)
	{
		if (!isNew() && getVersion() != clientRes.getVersion()) {
			throw new JSResourceVersionNotMatchException("jsexception.resource.no.match.versions", new Object[] {getResourceURI(), new Integer(clientRes.getVersion()), new Integer(getVersion())});
		}

		setName(clientRes.getName());
		setLabel(clientRes.getLabel());
		setDescription(clientRes.getDescription());

        if (UpdateDatesIndicator.shouldUpdate()) {
            setCreationDate(UpdateDatesIndicator.getOperationalDate());

            if (UpdateDatesIndicator.useOperationalForUpdateDate() || clientRes.getUpdateDate() == null) {
                setUpdateDate(UpdateDatesIndicator.getOperationalDate());
            } else {
                setUpdateDate(clientRes.getUpdateDate());
            }
        }
	}

	protected void copyTo(Resource clientRes)
	{
		clientRes.setVersion(getVersion());
		clientRes.setCreationDate(getCreationDate());
		clientRes.setUpdateDate(getUpdateDate());
		clientRes.setName(getName());
		clientRes.setLabel(getLabel());
		clientRes.setDescription(getDescription());
		
		RepoFolder parentFolder = getParent();
		if (parentFolder != null) {
			clientRes.setParentFolder(parentFolder.getURI());
		}
	}

	public String getResourceURI() {
		RepoFolder parentFolder = getParent();
		String uri;
		if (parentFolder == null || parentFolder.isRoot()) {
			uri = Folder.SEPARATOR + getName();
		} else {
			uri = parentFolder.getURI() + Folder.SEPARATOR + getName();
		}
		return uri;
	}

    public ResourceLookup toClientLookup() {
        Class clientItf = getClientItf();
        //TODO context?
        ResourceLookup clientRes = new ResourceLookupImpl();
        clientRes.setResourceType(clientItf.getName());
        copyTo(clientRes);
        return clientRes;
    }
	
	public boolean isNew() {
		return getVersion() == Resource.VERSION_NEW;
	}

	
	/**
	 * @hibernate.property
	 * 		column="creation_date" type="timestamp" not-null="true"
	 */
	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

    /**
     * @hibernate.property
     * 		column="update_date" type="timestamp" not-null="true"
     */
    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

/*    public Set<AccessEvent> getAccessEvents() {
        return accessEvents;
    }

    public void setAccessEvents(Set<AccessEvent> accessEvents) {
        this.accessEvents = accessEvents;
    }*/

    public Set<RepoReportThumbnail> getThumbnails() {
        return thumbnails;
    }

    public void setThumbnails(Set<RepoReportThumbnail> thumbnails) {
        this.thumbnails = thumbnails;
    }

    public String getResourceType() {
        return resourceType != null ? resourceType : (resourceType = getClientType().getName());
    }

    public void setResourceType(String resourceType) {
        // No-op.
    }

    public abstract void moveTo(RepoFolder parent, RepoManager repoManager);

	protected abstract void moved(String oldBaseURI, String newBaseURI, RepoManager repoManager);
	
}
