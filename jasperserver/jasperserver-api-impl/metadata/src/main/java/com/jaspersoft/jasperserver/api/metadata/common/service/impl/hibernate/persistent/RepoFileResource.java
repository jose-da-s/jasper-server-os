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
package com.jaspersoft.jasperserver.api.metadata.common.service.impl.hibernate.persistent;

import com.jaspersoft.jasperserver.api.JSException;
import com.jaspersoft.jasperserver.api.JSExceptionWrapper;
import com.jaspersoft.jasperserver.api.metadata.common.domain.FileResource;
import com.jaspersoft.jasperserver.api.metadata.common.domain.FileResourceBase;
import com.jaspersoft.jasperserver.api.metadata.common.domain.FileResourceData;
import com.jaspersoft.jasperserver.api.metadata.common.domain.Resource;
import com.jaspersoft.jasperserver.api.metadata.common.service.ResourceFactory;
import com.jaspersoft.jasperserver.api.metadata.common.service.impl.hibernate.ReferenceResolver;
import com.jaspersoft.jasperserver.core.util.XMLUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.SAXParseException;

import javax.persistence.Entity;
import javax.sql.rowset.serial.SerialBlob;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Lucian Chirita (lucianc@users.sourceforge.net)
 * @version $Id$
 * 
 * @hibernate.joined-subclass table="file_resource"
 * @hibernate.joined-subclass-key column="id"
 */
@Entity
public class RepoFileResource extends RepoResource implements FileResourceBase {

	private static final Log log = LogFactory.getLog(RepoFileResource.class);
	private static final Set<String> XML_BASED_FORMATS = new HashSet<String>(Arrays.asList(new String[] {
			FileResourceBase.TYPE_XML,
			FileResourceBase.TYPE_JRXML,
			FileResourceBase.TYPE_STYLE_TEMPLATE,
			FileResourceBase.TYPE_ACCESS_GRANT_SCHEMA
	}));

	
	private String fileType;
	private Blob data;
	private RepoFileResource reference;
	private Set<RepoFileResource> references;

	public RepoFileResource() {
		super();
		getResourceType();
	}

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
	public String getFileType() {
		return fileType;
	}

	public void setFileType(String type) {
		this.fileType = type;
	}
	
	public String getFinalFileType() {
		RepoFileResource res = this;
		while (res.isFileReference()) {
			res = res.getReference();
		}
		return res.getFileType();
	}

	public boolean isFileReference() {
		return getReference() != null;
	}
	
	/**
	 * @hibernate.many-to-one column="reference"
	 */
	public RepoFileResource getReference() {
		return reference;
	}

	public void setReference(RepoFileResource reference) {
		this.reference = reference;
	}

	protected Class getClientItf() {
		return FileResource.class;
	}

	protected void copyTo(Resource clientRes, ResourceFactory resourceFactory) {
		super.copyTo(clientRes, resourceFactory);

		FileResource fileRes = (FileResource) clientRes;

		if (isFileReference()) {
			RepoFileResource ref = getReference();
			fileRes.setFileType(null);
			fileRes.setData(null);
			fileRes.setReferenceURI(ref.getResourceURI());
		} else {
			fileRes.setFileType(getFileType());
			if (hasClientOption(CLIENT_OPTION_FULL_DATA)) {
				copyDataTo(fileRes);
			} else {
				fileRes.setData(null);
			}
			fileRes.setReferenceURI(null);
		}
	}

	protected void copyDataTo(FileResource fileRes) {
		Blob blob = getData();
		if (blob == null) {
			fileRes.setData(null);
		} else {
			try {
				// check for XXE vulnerability
				if (XML_BASED_FORMATS.contains(fileRes.getFileType())) {
					fileRes.readData(XMLUtil.checkForXXE(blob.getBinaryStream()));
				} else {
					fileRes.readData(blob.getBinaryStream());
				}
			} catch (SQLException e) {
				log.error("Error while reading data blob of \"" + getResourceURI() + "\"", e);
				throw new JSExceptionWrapper(e);
			} catch (SAXParseException ex) {
				log.error("Insecure XML resource!", ex);
			} catch (Exception ex) {
				log.error(ex);
			}
		}
	}

	public FileResourceData copyData() {
		try {
			if (isFileReference()) {
				String quotedResourceURI = "\"" + getResourceURI() + "\"";
				throw new JSException("jsexception.file.resource.is.reference", new Object[] {quotedResourceURI});
			}
			
			FileResourceData resData;

			Blob blob = getData();
			if (blob == null) {
				resData = new FileResourceData((byte[]) null);
			} else {
				resData = new FileResourceData(blob.getBinaryStream());
			}

			return resData;
		} catch (SQLException e) {
			log.error("Error while reading data blob of \"" + getResourceURI() + "\"", e);
			throw new JSExceptionWrapper(e);
		}
	}

	protected void copyDataFrom(FileResource dataRes) {
		if (dataRes.isReference()) {
			setData(null);
		} else {
			//only update when the client has set some data
			if (dataRes.hasData()) {
				byte[] clientData = dataRes.getData();
				// check for XXE vulnerability
				try {
					if (XML_BASED_FORMATS.contains(dataRes.getFileType())) {
                        XMLUtil.checkForXXE(clientData);
                    }
				} catch (Exception e) {
					log.error(e);
					throw new JSException(e);
				}
				SerialBlob blob = null;
				try {
					blob = new SerialBlob(clientData);
					setData(blob);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}

    protected void copyFrom(Resource clientRes,
                            ReferenceResolver referenceResolver) {
        super.copyFrom(clientRes, referenceResolver);

        FileResource dataRes = (FileResource) clientRes;
        if (dataRes.isReference()) {
            setFileType(null);
            setData(null);

            RepoFileResource externalReference = (RepoFileResource) referenceResolver
                    .getExternalReference(dataRes.getReferenceURI(), RepoFileResource.class);

            if (dataRes.getFileType() != null
                    && !dataRes.getFileType().equals(externalReference.getFinalFileType())) {
                throw new JSException("jsexception.file.resource.no.match.type",
                        new Object[] {dataRes.getFileType(), externalReference.getFileType()}
                );
            }
            setReference(externalReference);
        } else {
            setFileType(dataRes.getFileType());
            copyDataFrom(dataRes);
            setReference(null);
        }
    }

	protected Class getImplementingItf() {
		return FileResourceBase.class;
	}

	public Set<RepoFileResource> getReferences() {
		return references;
	}

	public void setReferences(Set<RepoFileResource> references) {
		this.references = references;
	}


}
