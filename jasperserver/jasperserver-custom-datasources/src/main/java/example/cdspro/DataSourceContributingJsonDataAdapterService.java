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

package example.cdspro;

import com.jaspersoft.jasperserver.api.metadata.common.domain.ContentResource;
import com.jaspersoft.jasperserver.api.metadata.common.domain.FileResource;
import com.jaspersoft.jasperserver.api.metadata.common.domain.FileResourceData;
import com.jaspersoft.jasperserver.api.metadata.common.service.RepositoryService;
import net.sf.jasperreports.data.json.JsonDataAdapter;
import net.sf.jasperreports.data.json.JsonDataAdapterService;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperReportsContext;
import net.sf.jasperreports.engine.ParameterContributorContext;
import net.sf.jasperreports.json.data.JsonQLDataSource;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;

/**
 * Downloads remote resource, parses it, creates a datasource and contributes it to the datasource parameters.
 */
public class DataSourceContributingJsonDataAdapterService extends JsonDataAdapterService {

    RepositoryService repositoryService;

    public DataSourceContributingJsonDataAdapterService(JasperReportsContext jasperReportsContext, JsonDataAdapter jsonDataAdapter, RepositoryService repositoryService) {
        super(new ParameterContributorContext(jasperReportsContext, null, null), jsonDataAdapter);
        this.repositoryService = repositoryService;
    }

    @Override
    public void contributeParameters(Map<String, Object> parameters) throws JRException
    {

        String fileName = getJsonDataAdapter().getFileName();
        JsonQLDataSource ds = null;
        if (fileName.toLowerCase().startsWith("repo:")) {
            InputStream in = getResourceInputStream(fileName.substring(5), repositoryService);
            ds = new DescriptionEnsuringJsonQLDataSourceWrapper(getJsonDataAdapter(), in);
        } else if (fileName.toLowerCase().startsWith("http:") || fileName.toLowerCase().startsWith("https:")) {
            try {
                InputStream in = (new URL(fileName)).openStream();
                ds = new DescriptionEnsuringJsonQLDataSourceWrapper(getJsonDataAdapter(), in);
                in.close();
            } catch (Exception ex) {
                throw new JRException(ex.getMessage());
            }
        } else {
            ds = new DescriptionEnsuringJsonQLDataSourceWrapper(getJsonDataAdapter(), fileName);
        }
        parameters.put(JRParameter.REPORT_DATA_SOURCE, ds);

    }

    private static InputStream getResourceInputStream(String repoPath, RepositoryService repositoryService) {
        InputStream is = null;
        FileResource fileResource;
            fileResource = (FileResource) repositoryService.getResource(null, repoPath, FileResource.class);
        if (fileResource != null) {
            if (fileResource.hasData()) {
                is = fileResource.getDataStream();
            } else {
                FileResourceData resourceData = repositoryService.getResourceData(null, fileResource.getURIString());
                is = resourceData.getDataStream();
            }
        } else {
            ContentResource contentResource = (ContentResource) repositoryService.getResource(null, repoPath, ContentResource.class);
            if (contentResource.hasData()) {
                is = contentResource.getDataStream();
            } else {
                FileResourceData resourceData = repositoryService.getContentResourceData(null, contentResource.getURIString());
                is = resourceData.getDataStream();
            }
        }
        return is;
    }


    /**
     * A wrapper which ensures that a field description is present before {@link #getFieldValue(net.sf.jasperreports.engine.JRField)} call.
     */
    private static final class DescriptionEnsuringJsonQLDataSourceWrapper extends JsonQLDataSource {

        public DescriptionEnsuringJsonQLDataSourceWrapper(JsonDataAdapter dataAdapter, String uri) throws JRException {
            super(new File(uri), dataAdapter.getSelectExpression());
            setDataAdapterProperties(dataAdapter);
        }

        public DescriptionEnsuringJsonQLDataSourceWrapper(JsonDataAdapter dataAdapter, InputStream in) throws JRException {
            super(in, dataAdapter.getSelectExpression());
            setDataAdapterProperties(dataAdapter);
        }

        void setDataAdapterProperties(JsonDataAdapter dataAdapter) {
            setDatePattern(dataAdapter.getDatePattern());
            setLocale(dataAdapter.getLocale());
            setNumberPattern(dataAdapter.getNumberPattern());
            setTimeZone(dataAdapter.getTimeZone());
        }


        @Override
        public Object getFieldValue(JRField jrField) throws JRException {
            // we do this hack because description gets lost sometimes during domain creation (bug)
            String originalDescriptoin = jrField.getDescription();
            boolean descriptionPresent = !StringUtils.isBlank(originalDescriptoin);
            if (!descriptionPresent) {
                jrField.setDescription(jrField.getName());
            }
            try {
                return super.getFieldValue(jrField);
            } finally {
                // clean up
                if (!descriptionPresent) {
                    jrField.setDescription(originalDescriptoin);
                }
            }
        }
    }
}
