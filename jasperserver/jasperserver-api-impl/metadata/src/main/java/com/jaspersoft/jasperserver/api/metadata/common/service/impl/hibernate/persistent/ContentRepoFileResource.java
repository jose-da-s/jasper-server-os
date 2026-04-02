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

import com.jaspersoft.jasperserver.api.JSException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.jaspersoft.jasperserver.api.JSExceptionWrapper;
import com.jaspersoft.jasperserver.api.metadata.common.domain.ContentResource;
import com.jaspersoft.jasperserver.api.metadata.common.domain.DataContainer;
import com.jaspersoft.jasperserver.api.metadata.common.domain.FileBufferedDataContainer;
import com.jaspersoft.jasperserver.api.metadata.common.domain.FileResourceData;
import com.jaspersoft.jasperserver.api.metadata.common.domain.Resource;

import com.jaspersoft.jasperserver.api.metadata.common.domain.util.DataContainerStreamUtil;
import com.jaspersoft.jasperserver.api.metadata.common.service.ResourceFactory;
import com.jaspersoft.jasperserver.api.metadata.common.service.impl.hibernate.ReferenceResolver;

import jakarta.persistence.Entity;
import javax.sql.rowset.serial.SerialBlob;
import javax.sql.rowset.serial.SerialException;


/**
* @hibernate.joined-subclass table="content_file"
* @hibernate.joined-subclass-key column="id"
*/
@Entity
public class ContentRepoFileResource extends RepoResource
{
	private static final Log log = LogFactory.getLog(ContentRepoFileResource.class);

		private String contentFileType;

		private Blob data;


		/**
		 * @hibernate.property column="data" type="blob"
		 */
		public Blob getData() {
			return data;
		}

		public void setData(Blob data) {
			this.data = data;
		}

	/**
	 * @hibernate.property column="file_type" type="string" length="20"
	 */

	public String getContentFileType() {
		return contentFileType;
	}

	public void setContentFileType(String contentFileType) {
		this.contentFileType = contentFileType;
	}


	protected void copyDataFrom(ContentResource dataRes) {
			if (dataRes.isReference()) {
				setData(null);
			} else {
				//only update when the client has set some data
				if (dataRes.hasData()) {
										SerialBlob blob = null;
                                        /* ContentResourceImpl uses a dataContainer that can either have content
                                         * completely in memory or have part of the content stored on disk.
                                         */ 
                                        try {
                                            blob = new SerialBlob(readFully(dataRes.getDataStream()));
                                        } catch (IOException ex) {
                                            throw new JSException(ex);
                                        } catch (SerialException e) {
											e.printStackTrace();
										} catch (SQLException e) {
											e.printStackTrace();
										}
					setData(blob);
				}
			}
		}

//	public void set(ContentResource file)
//	{
//		copyDataFrom(file);
//		setDescription(file.getDescription());
//		setFileType(file.getFileType());
//		setLabel(file.getLabel());
//		setName(file.getName());
//	}

	public FileResourceData copyData() {
		FileResourceData resData;

		Blob blob = getData();
		if (blob == null) {
			resData = new FileResourceData((byte[]) null);
		} else {
			DataContainer dataContainer = createDataContainer(blob);
			resData = new FileResourceData(dataContainer);
		}

		return resData;
	}

	protected void copyTo(Resource clientRes, ResourceFactory resourceFactory) {
		super.copyTo(clientRes, resourceFactory);

		ContentResource resource = (ContentResource) clientRes;

		resource.setFileType(getContentFileType());
		if (hasClientOption(CLIENT_OPTION_FULL_DATA)) {
			copyDataTo(resource);
		} else {
			resource.setDataContainer(null);
		}
		resource.setReferenceURI(null);

		RepoFolder childrenFolder = getChildrenFolder();

		if (childrenFolder != null) {
			Set resList = childrenFolder.getChildren();
			for (Iterator it = resList.iterator(); it.hasNext();) {
				ContentRepoFileResource fileRes = (ContentRepoFileResource) it.next();
				resource.addChildResource((ContentResource) fileRes.toClient(resourceFactory));
			}
		}

	}

	protected void copyDataTo(ContentResource resource) {
		Blob blob = getData();
		if (blob == null) {
			resource.setDataContainer(null);
		} else {
			DataContainer dataContainer = createDataContainer(blob);
			resource.setDataContainer(dataContainer);
		}
	}

	protected DataContainer createDataContainer(Blob blob) {
		try {
			InputStream dataStream = blob.getBinaryStream();
			DataContainer dataContainer = new FileBufferedDataContainer();
			// copy the raw data, sniffing for gzip
			dataContainer = DataContainerStreamUtil.pipeGzipSniffedData(dataStream, dataContainer);
			return dataContainer;
		} catch (SQLException e) {
			log.error("Error while reading data blob of \"" + getResourceURI() + "\"", e);
			throw new JSExceptionWrapper(e);
		}
	}

	protected void copyFrom(Resource clientRes,
			ReferenceResolver referenceResolver) {
		super.copyFrom(clientRes, referenceResolver);

		ContentResource dataRes = (ContentResource) clientRes;
		setContentFileType(dataRes.getFileType());
		copyDataFrom(dataRes);
		copyResources(referenceResolver, (ContentResource) clientRes);
	}


	private void copyResources(ReferenceResolver referenceResolver, ContentResource resource) {
		List clientResources = resource.getResources();
		if (clientResources != null && !clientResources.isEmpty()) {
			for (Iterator it = clientResources.iterator(); it.hasNext();) {
				ContentResource childResource = (ContentResource) it.next();
				getReference(childResource, RepoFileResource.class, referenceResolver);
			}
		}
	}


	protected Class getClientItf()
	{
		return ContentResource.class;
	}
	
	public static byte[] readFully(InputStream input) throws IOException
	{
	    byte[] buffer = new byte[8192];
	    int bytesRead;
	    ByteArrayOutputStream output = new ByteArrayOutputStream();
	    while ((bytesRead = input.read(buffer)) != -1)
	    {
	        output.write(buffer, 0, bytesRead);
	    }
	    return output.toByteArray();
	}
}
