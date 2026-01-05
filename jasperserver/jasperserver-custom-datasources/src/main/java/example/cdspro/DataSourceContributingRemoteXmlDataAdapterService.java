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

import net.sf.jasperreports.data.xml.RemoteXmlDataAdapter;
import net.sf.jasperreports.data.xml.RemoteXmlDataAdapterService;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperReportsContext;
import net.sf.jasperreports.engine.ParameterContributorContext;
import net.sf.jasperreports.jaxen.data.JaxenXmlDataSource;
import org.apache.commons.lang.StringUtils;

import java.util.Map;

/**
 * Downloads remote resource, parses it, creates a datasource and contributes it to the datasource parameters.
 */
public class DataSourceContributingRemoteXmlDataAdapterService extends RemoteXmlDataAdapterService {

    public DataSourceContributingRemoteXmlDataAdapterService(JasperReportsContext jasperReportsContext, RemoteXmlDataAdapter remoteXmlDataAdapter) {
        super(new ParameterContributorContext(jasperReportsContext, null, null), remoteXmlDataAdapter);
    }

    @Override
    public void contributeParameters(Map<String, Object> parameters) throws JRException
    {
        String fileName = getRemoteXmlDataAdapter().getFileName();
        if (fileName.toLowerCase().startsWith("https://") || fileName.toLowerCase().startsWith("http://")) {

            JaxenXmlDataSource ds = new DescriptionEnsuringJaxenXmlDataSourceWrapper(fileName, getRemoteXmlDataAdapter().getSelectExpression());

            parameters.put(JRParameter.REPORT_DATA_SOURCE, ds);
        }
    }

    /**
     * A wrapper which ensures that a field description is present before {@link #getFieldValue(JRField)} call.
     */
    private static final class DescriptionEnsuringJaxenXmlDataSourceWrapper extends JaxenXmlDataSource {

        public DescriptionEnsuringJaxenXmlDataSourceWrapper(String uri, String selectExpression) throws JRException {
            super(uri, selectExpression);
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
