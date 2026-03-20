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


import com.jaspersoft.jasperserver.api.common.domain.impl.ExecutionContextImpl;
import com.jaspersoft.jasperserver.api.metadata.common.domain.ResourceReference;
import com.jaspersoft.jasperserver.api.metadata.common.service.RepositoryService;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.CustomReportDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.validation.Errors;

import jakarta.annotation.Resource;
import java.io.File;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: ichan
 * Date: 2/12/14
 * Time: 2:00 PM
 * To change this template use File | Settings | File Templates.
 */
public class JsonDataSourceValidator implements CustomDataSourceValidator {


    private final static Log log = LogFactory.getLog(XlsDataSourceValidator.class);

    @Resource(name = "concreteRepository")
    private RepositoryService repositoryService;

    public void validatePropertyValues(CustomReportDataSource ds, Errors errors) {
        String filePath = null;
        String selectExpression = null;
        Map props = ds.getPropertyMap();

        if (props != null) {
            selectExpression = (String) ds.getPropertyMap().get("selectExpression");
        }

        if (selectExpression == null || selectExpression.length() == 0) {
            reject(errors, "selectExpression", "Please enter query");
        }

        final ResourceReference dataFileReference = ds.getResources() != null
                ? ds.getResources().get(TextDataSourceDefinition.DATA_FILE_RESOURCE_ALIAS) : null;
        if(dataFileReference != null){
            final String targetURI = dataFileReference.getTargetURI();
            if(repositoryService.getResource(
                    ExecutionContextImpl.getRuntimeExecutionContext(), targetURI) == null){
                reject(errors, "fileName", "Data file isn't found by URI " + targetURI);
            }
        } else {
            filePath = (String) ds.getPropertyMap().get("fileName");
            if (filePath == null || filePath.length() == 0) {
                // file path can be null if data file resources is linked to the data source
                reject(errors, "fileName", "Please enter file path");
            } else {
                if (filePath.toLowerCase().startsWith("ftp://") || filePath.toLowerCase().startsWith("ftps://")) {
                    int idx1 = filePath.indexOf(":", 6);
                    int idx2 = filePath.indexOf("@", 6);
                    int idx3 = filePath.indexOf("/", idx1);
                    if ((idx1 <= 0) || (idx2 <= 0) || (idx3 <= 0) || (idx3 == (idx2 + 1))) {
                        reject(errors, "fileName", "Please follow FTP syntax for FTP path: ftp://[USERNAME]:[PASSWORD]@[HOST]:[PORT]/[PATH]/[FILENAME]");
                    }
                } else if (filePath.toLowerCase().startsWith("repo:")) {
                } else if (filePath.toLowerCase().startsWith("http:")) {
                } else if (filePath.toLowerCase().startsWith("https:")) {
                } else {
                    try {
                        File f = new File(filePath);
                        if (!(f.exists() && !f.isDirectory())) {
                            reject(errors, "fileName", "Invalid Server file system path");
                        }
                    } catch (Exception ex) {
                        reject(errors, "fileName", "Invalid Server file system path");
                    }
                }
            }
        }
    }

    // first arg is the path of the property which has the error
    // for custom DS's this will always be in the form "reportDataSource.propertyMap[yourPropName]"
    protected void reject(Errors errors, String name, String reason) {
        if (errors != null) errors.rejectValue("jsonDataSource.propertyMap[" + name + "]",  reason);
        else log.debug("jsonDataSource.propertyMap[" + name + "] - " + reason);
    }


}

