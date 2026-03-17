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

import com.jaspersoft.jasperserver.api.JSException;
import com.jaspersoft.jasperserver.api.JSExceptionWrapper;
import com.jaspersoft.jasperserver.api.security.encryption.PlainCipher;
import com.jaspersoft.jasperserver.api.common.util.ImportRunMonitor;
import com.jaspersoft.jasperserver.api.common.util.diagnostic.DiagnosticSnapshotPropertyHelper;
import com.jaspersoft.jasperserver.export.io.ImportInput;
import com.jaspersoft.jasperserver.export.modules.Attributes;
import com.jaspersoft.jasperserver.export.modules.ImporterModule;
import com.jaspersoft.jasperserver.export.modules.ImporterModuleContext;
import com.jaspersoft.jasperserver.export.modules.MapAttributes;
import com.jaspersoft.jasperserver.export.modules.common.CipherLookup;
import com.jaspersoft.jasperserver.export.service.ImportExportService;
import com.jaspersoft.jasperserver.export.service.ImportFailedException;
import com.jaspersoft.jasperserver.export.service.impl.ImportExportServiceImpl;
import com.jaspersoft.jasperserver.export.util.EncryptionParams;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.util.Assert;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * @author Lucian Chirita (lucianc@users.sourceforge.net)
 * @version $Id$
 */
public class ImporterImpl extends BaseExporterImporter implements Importer {
	
	private static final Log log = LogFactory.getLog(ImporterImpl.class);

	protected class ModuleContextImpl implements ImporterModuleContext {
		
		private final Element moduleElement;
		private final Attributes attributes;

		public ModuleContextImpl(final Element moduleElement, 
				final Attributes attributes) {
			this.moduleElement = moduleElement;
			this.attributes = attributes;
		}

		public String getCharacterEncoding() {
			return ImporterImpl.this.getCharacterEncoding();
		}

		public ImportTask getImportTask() {
			return task;
		}

		public Element getModuleIndexElement() {
			return moduleElement;
		}

		public Attributes getAttributes() {
			return attributes;
		}

		@Override
		public Map<String, String> getNewGeneratedTenantIds() {
			return BaseExporterImporter.getNewGeneratedTenantIds(attributes);
		}
	}
	
	protected ImportTask task;
	protected ImportInput input;

	public void setTask(ImportTask task) {
		Assert.notNull(task, "Import task cannot be null");
		this.task = task;
		this.input = task.getInput();

		boolean close = false;
		try {
			input.open();
			close = true;

			task.setInputMetadata(getImportInputMetadata());
		} catch (IOException e) {
			log.error(e);
			throw new JSExceptionWrapper(e);
		} finally {
			if (close) {
				try {
					input.close();
				} catch (IOException ioe) {
					log.info("Error while closing input", ioe);
				}
			}
		}

		//Tenant id we import into
		String destinationTenantId = task.getParameters().getParameterValue(ImportExportServiceImpl.ORGANIZATION);
		if (StringUtils.isBlank(destinationTenantId)) {
			destinationTenantId = getAuthenticatedTenantId();
			task.getParameters().setParameterValue(ImportExportServiceImpl.ORGANIZATION, destinationTenantId);
		}
	}

	public void performImport() throws ImportFailedException {
		try {
			input.open();
			boolean close = true;
			try {
				process(createContextAttributes());
				
				close = false;
				input.close();
			} finally {
				if (close) {
					try {
						input.close();
					} catch (IOException ioe) {
						log.info("Error while closing input", ioe);
					}
				}
			}			
		} catch (IOException e) {
			log.error(e);
			throw new JSExceptionWrapper(e);
		}
	}

	protected void process(Attributes contextAttributes) throws ImportFailedException {
		task.setAttributes(contextAttributes);
		//Root Tenant from import input
		String sourceTenantId = task.getInputMetadata().getProperty(ImportExportService.ROOT_TENANT_ID);

		//Tenant id we import into
		String destinationTenantId = task.getParameters().getParameterValue(ImportExportServiceImpl.ORGANIZATION);

		if (sourceTenantId != null
				&& !StringUtils.equals(sourceTenantId, destinationTenantId)
				&& StringUtils.isNotBlank(destinationTenantId)) {

			BaseExporterImporter.getNewGeneratedTenantIds(contextAttributes).put(sourceTenantId, destinationTenantId);
		}

		Document indexDocument = readDocument(getIndexFilename());
		Element indexRoot = indexDocument.getRootElement();
		
		Properties properties = new Properties();
		for (Iterator it = indexRoot.elementIterator(getPropertyElementName()); it.hasNext(); ) {
			Element propElement = (Element) it.next();
			String propKey = propElement.attribute(getPropertyNameAttribute()).getValue();
			Attribute valueAttr = propElement.attribute(getPropertyValueAttribute());
			String value = valueAttr == null ? null : valueAttr.getValue();
			properties.setProperty(propKey, value);
		}
		input.propertiesRead(properties);

        contextAttributes.setAttribute("sourceJsVersion",properties.getProperty(VERSION_ATTR));
        contextAttributes.setAttribute("targetJsVersion",super.getJsVersion());
        
        if (DiagnosticSnapshotPropertyHelper.isDiagSnapshotSet(properties))
        {
        	contextAttributes.setAttribute(DiagnosticSnapshotPropertyHelper.ATTRIBUTE_IS_DIAG_SNAPSHOT, Boolean.TRUE.toString());
        }

		final EncryptionParams encParams = new EncryptionParams(this.task.getParameters());
		final PlainCipher cipher = getCipher(encParams, contextAttributes, properties);
        String encryptedValue = properties.getProperty(ENCRYPTION_ATTR);
        if (encryptedValue != null )  {
			boolean isCorrectKey = Boolean.parseBoolean(cipher.decode(encryptedValue));
			if (!isCorrectKey && encParams.getFailOnWrongKey().orElse(true)) {
				log.error("Import failed as resources cannot be decoded");
				throw new ImportFailedException("Import failed as resources cannot be decoded", IMPORT_FAILED_DECODE_ERROR_CODE, new String[] {});
			}
		}
		CipherLookup.INSTANCE.set(cipher);

		for (Iterator it = indexRoot.elementIterator(getIndexModuleElementName()); it.hasNext(); ) {
            if (Thread.interrupted()){
                throw new RuntimeException("Cancelled");
            }
			Element moduleElement = (Element) it.next();
			String moduleId = moduleElement.attribute(getIndexModuleIdAttributeName()).getValue();
			ImporterModule module = getModuleRegister().getImporterModule(moduleId);
			if (module == null) {
				throw new JSException("jsexception.import.module.not.found", new Object[] {moduleId});
			}

			commandOut.debug("Invoking module " + module);

            contextAttributes.setAttribute("appContext",this.task.getApplicationContext());
			ModuleContextImpl moduleContext = new ModuleContextImpl(moduleElement,
					contextAttributes);
			module.init(moduleContext);

            List<String> messages = new ArrayList<String>();
            ImportRunMonitor.start();
            try{
			    List<String> moduleMessages = module.process();
                if(moduleMessages != null) {
                    messages.addAll(moduleMessages);
                }
            } finally {
                ImportRunMonitor.stop();
                for(String message: messages) {
                    commandOut.info(message);
                }
            }
		}
	}

	protected Attributes createContextAttributes() {
		Attributes contextAttributes = new MapAttributes();
		return contextAttributes;
	}

	protected Document readDocument(String fileName) {
		InputStream indexInput = getInput(fileName);
		boolean close = true;
		try {
			SAXReader reader = new SAXReader();
			reader.setEncoding(getCharacterEncoding());
			Document document = reader.read(indexInput);

			close = false;
			indexInput.close();

			return document;
		} catch (IOException e) {
			log.error(e);
			throw new JSExceptionWrapper(e);
		} catch (DocumentException e) {
			log.error(e);
			throw new JSExceptionWrapper(e);
		} finally {
			if (close) {
				try {
					indexInput.close();
				} catch (IOException e) {
					log.error(e);
				}
			}
		}
	}

	protected InputStream getInput(String fileName) {
		try {
			return input.getFileInputStream(fileName);
		} catch (IOException e) {
			log.error(e);
			throw new JSExceptionWrapper(e);
		}
	}

	private ImportInputMetadata getImportInputMetadata() {
		ImportInputMetadata inputMetadata = new ImportInputMetadata();

		Document indexDocument = readDocument(getIndexFilename());
		Element indexRoot = indexDocument.getRootElement();

		for (Iterator it = indexRoot.elementIterator(getPropertyElementName()); it.hasNext(); ) {
			Element propElement = (Element) it.next();
			String propKey = propElement.attribute(getPropertyNameAttribute()).getValue();
			Attribute valueAttr = propElement.attribute(getPropertyValueAttribute());
			String value = valueAttr == null ? null : valueAttr.getValue();
			inputMetadata.setProperty(propKey, value);
		}

		// broken dependencies
		inputMetadata.setBrokenDependencies(getBrokenDependencies());

		return inputMetadata;
	}

	private Set<String> getBrokenDependencies() {
		Set<String> result = new HashSet<String>();

		if (input.fileExists(getBrokenDependenceFilename())) {
			Document document = readDocument(getBrokenDependenceFilename());
			Element root = document.getRootElement();

			for (Iterator it = root.elementIterator("resource"); it.hasNext(); ) {
				Element element = (Element) it.next();
				String text = element.getText();
				result.add(text);
			}
		}

		return result;
	}
}
