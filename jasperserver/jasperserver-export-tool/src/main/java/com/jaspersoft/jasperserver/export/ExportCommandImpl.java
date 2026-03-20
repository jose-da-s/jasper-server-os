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

package com.jaspersoft.jasperserver.export;

import java.util.ArrayList;
import java.util.Locale;

import com.jaspersoft.jasperserver.dto.common.WarningDescriptor;
import com.jaspersoft.jasperserver.export.modules.common.ExportImportWarningCode;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.jaspersoft.jasperserver.api.common.domain.ExecutionContext;
import com.jaspersoft.jasperserver.api.common.domain.impl.ExecutionContextImpl;
import com.jaspersoft.jasperserver.export.io.ExportImportIOFactory;
import com.jaspersoft.jasperserver.export.io.ExportOutput;
import com.jaspersoft.jasperserver.export.util.CommandOut;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import jakarta.annotation.Resource;

/**
 * @author Lucian Chirita (lucianc@users.sourceforge.net)
 * @version $Id$
 */
public class ExportCommandImpl implements CommandBean, ApplicationContextAware {
	
	private static final CommandOut commandOut = CommandOut.getInstance();
	private static final String EXPORT_WARNING_MESSAGE_CODE = "ji.export.broken-dependencies";

	@Resource(name = "exportImportMessageSource")
	private MessageSource messageSource;

	private ApplicationContext ctx;
	
	private ExportImportIOFactory exportImportIOFactory;
	private String exporterPrototypeBeanName;

	public void setApplicationContext(ApplicationContext ctx) throws BeansException {
		this.ctx = ctx;
	}
	
	public void process(Parameters parameters) {
		ExportTask task = createTask(parameters);
		Exporter exporter = createPrototypeExporter(parameters);
		exporter.setTask(task);
		exporter.performExport();

		if (!task.getWarnings().isEmpty()) {
			String message = messageSource.getMessage(EXPORT_WARNING_MESSAGE_CODE, null,
					EXPORT_WARNING_MESSAGE_CODE, LocaleContextHolder.getLocale());
			commandOut.info("\n\n" + message);

			for (WarningDescriptor warning : task.getWarnings()) {
				if (warning.getCode().equals(ExportImportWarningCode.EXPORT_BROKEN_DEPENDENCY.toString())) {
					commandOut.warn("  " + warning.getParameters()[0]);
				}
			}
		}
	}

	protected ExportTask createTask(Parameters parameters) {
		ExportTaskImpl task = new ExportTaskImpl();
		task.setParameters(parameters);
		task.setExecutionContext(getExecutionContext(parameters));
		task.setOutput(getExportOutput(parameters));
		task.setWarnings(new ArrayList<WarningDescriptor>());
		return task;
	}

	protected ExportOutput getExportOutput(Parameters parameters) {
		return getExportImportIOFactory().createOutput(parameters);
	}

	protected ExecutionContext getExecutionContext(Parameters parameters) {
		ExecutionContextImpl context = new ExecutionContextImpl();
		context.setLocale(getLocale(parameters));
		return context;
	}

	protected Locale getLocale(Parameters parameters) {
		return Locale.getDefault();
	}

	protected Exporter createPrototypeExporter(Parameters parameters) {
		String exporterBeanName = getExporterPrototypeBeanName(parameters);
		
		commandOut.debug("Using " + exporterBeanName + " exporter prototype bean.");
		
		return (Exporter) ctx.getBean(exporterBeanName, Exporter.class);
	}

	protected String getExporterPrototypeBeanName(Parameters parameters) {
		return getExporterPrototypeBeanName();
	}

	public String getExporterPrototypeBeanName() {
		return exporterPrototypeBeanName;
	}

	public void setExporterPrototypeBeanName(String exporterPrototypeBeanName) {
		this.exporterPrototypeBeanName = exporterPrototypeBeanName;
	}

	public ExportImportIOFactory getExportImportIOFactory() {
		return exportImportIOFactory;
	}

	public void setExportImportIOFactory(ExportImportIOFactory ioFactory) {
		this.exportImportIOFactory = ioFactory;
	}
}

