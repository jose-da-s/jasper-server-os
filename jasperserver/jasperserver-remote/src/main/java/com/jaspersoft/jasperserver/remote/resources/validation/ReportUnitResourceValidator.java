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

package com.jaspersoft.jasperserver.remote.resources.validation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.jaspersoft.jasperserver.api.common.domain.ExecutionContext;
import com.jaspersoft.jasperserver.api.metadata.common.domain.FileResource;
import com.jaspersoft.jasperserver.api.metadata.common.domain.ResourceReference;
import com.jaspersoft.jasperserver.api.metadata.common.service.RepositoryService;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.ReportUnit;
import com.jaspersoft.jasperserver.remote.exception.IllegalParameterValueException;
import com.jaspersoft.jasperserver.remote.exception.MandatoryParameterNotFoundException;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.design.JasperDesign;
import com.jaspersoft.jasperserver.api.engine.jasperreports.util.CustomJRXmlLoader;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map;

import static com.jaspersoft.jasperserver.remote.resources.validation.ValidationHelper.empty;

/**
 * <p></p>
 *
 * @author Zakhar.Tomchenco
 * @version $Id$
 */
@Component
public class ReportUnitResourceValidator<T extends ReportUnit> extends GenericResourceValidator<T> {
	private static Log log = LogFactory.getLog(ReportUnitResourceValidator.class);

    @Resource(name = "concreteRepository")
    protected RepositoryService repositoryService;

    @Override
    protected void internalValidate(ExecutionContext ctx, T resource, List<Exception> errors, Map<String, String[]> additionalParameters) {
        final ResourceReference jrxmlReference = resource.getMainReport();
        if (empty(jrxmlReference)) {
            errors.add(new MandatoryParameterNotFoundException("JRXML"));
        } else if (jrxmlReference.isLocal()
                && ((FileResource) jrxmlReference.getLocalResource()).getData() == null
                && FileResource.VERSION_NEW == jrxmlReference.getLocalResource().getVersion()) {
            // if file resource doesn't exist yet, then it is local file creation.
            // in this case file content is mandatory
            errors.add(new MandatoryParameterNotFoundException("JRXML.content"));
        } else if (!isJrxmlValid(ctx, jrxmlReference)) {
            errors.add(new IllegalParameterValueException("Invalid JRXML", "JRXML.content", "JRXML.content"));
        }

        // raw type is used by core class. Cast is safe
        @SuppressWarnings("unchecked")
        List<ResourceReference> resources = resource.getResources();
        if (resources != null && !resources.isEmpty()) {
            for (ResourceReference currentReference : resources) {
                FileResource fileResource = currentReference.isLocal() ? (FileResource) currentReference.getLocalResource() : null;
                if (fileResource != null && !fileResource.isReference() && fileResource.getData() == null
                        && FileResource.VERSION_NEW == fileResource.getVersion()) {
                    // if file resource doesn't exist yet, then it is local file creation.
                    // in this case file content is mandatory
                    errors.add(new MandatoryParameterNotFoundException("resources." + fileResource.getName() + ".content"));
                }
            }
        }

        String inputControlRenderingView = resource.getInputControlRenderingView();
        if (inputControlRenderingView != null && inputControlRenderingView.length() > 0) {
            if (inputControlRenderingView.length() > 100) {
                errors.add(new IllegalParameterValueException("The JSP reference for input controls is too long. The maximum length is 100 characters", "resources.inputControlRenderingView", inputControlRenderingView));
            }
        }

        String reportRenderingView = resource.getReportRenderingView();
        if (reportRenderingView != null && reportRenderingView.length() > 0) {
            if (reportRenderingView.length() > 100) {
                errors.add(new IllegalParameterValueException("The JSP reference for the report display is too long. The maximum length is 100 characters", "resources.reportRenderingView", reportRenderingView));
            }
        }
    }

    protected boolean isJrxmlValid(ExecutionContext ctx, ResourceReference jrxmlReference) {
        boolean isValid = true;
        try {
            byte[] data = null;
            if (jrxmlReference.isLocal()) {
                data = ((FileResource) jrxmlReference.getLocalResource()).getData();
            }
            if (data == null) {
                data = repositoryService.getResourceData(ctx, jrxmlReference.getTargetURI()).getData();
            }
            loadJasperDesign(data);
        } catch (Exception e) {
        	log.error("Error validating JRXML resource reference", e);
            isValid = false;
        }
        return isValid;
    }

    protected JasperDesign loadJasperDesign(byte[] data) throws JRException {
        return CustomJRXmlLoader.load(new ByteArrayInputStream(data));
    }
}

