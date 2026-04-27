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
import com.jaspersoft.jasperserver.export.util.CommandOut;
import com.jaspersoft.jasperserver.export.util.CommandUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.*;

import static com.jaspersoft.jasperserver.export.util.CommandUtils.requestPassword;
import static com.jaspersoft.jasperserver.export.util.EncryptionParams.KEY_PASSWD_PARAMETER;

/**
 * @author Lucian Chirita (lucianc@users.sourceforge.net)
 * @version $Id$
 */
public abstract class BaseExportImportCommand {

	protected static final Log log = LogFactory.getLog(BaseExportImportCommand.class);

	protected static final CommandOut commandOut = CommandOut.getInstance();

	public static final String ARG_CONFIG_FILES = "configFiles";
	public static final String ARG_CONFIG_RESOURCES = "configResources";
	public static final String ARG_COMMAND_BEAN = "commandBean";
	public static final String ARG_HELP = "help";
	public static final String ARG_UPDATE = "update";
	public static final String ARG_TRANSFER = "transfer";

	public static final String ARG_PROFILE_ATTR_NAME = "profile-attr-name";
	public static final String ARG_PROFILE_ATTR_VALUE = "profile-attr-value";

	public static final String HELP_BEAN_NAME = "helpPrintBean";

	private final String defaultBeanName;
	private final String commandMetaBeanName;

	protected BaseExportImportCommand(String defaultBeanName, String commandMetaBeanName) {
		this.defaultBeanName = defaultBeanName;
		this.commandMetaBeanName = commandMetaBeanName;
	}

	protected static void debugArgs(String[] args) {
		if (log.isDebugEnabled()) {
			for (int i = 0; i < args.length; ++i) {
				log.debug("arg #" + i + " = \"" + args[i] + "\"");
			}
		}
	}

	protected boolean process(String[] args) throws IOException {
		Parameters exportParameters = parseArgs(args);
		ConfigurableApplicationContext ctx;
		// wrapped all files in an additional dir "wrapper" because of an apparent bug in Spring: 
		// if you load applicationContext.xml (without a folder), then the matcher class PathMatchingResourcePatternResolver
		// caches the folder path prefix "". Then, when trying to load a classpath resource, it tries to find a path prefix
		// matching the resource specifier "classpath*:<resource>" and, because the cached "" matches (with a naive string
		// prefix check), it decides the resource must be a file, and then crashes because no file starts with "classpath*:"
		// As a workaround, wrap everything in an extra dir and load these guys with a folder prefix.
        if (exportParameters.hasParameter(ARG_HELP)){
            ctx = createSpringContext(exportParameters, "wrapper/helpApplicationContext-export-import*.xml");
        } else if (exportParameters.hasParameter(ARG_TRANSFER)){
			ctx= createSpringContext(exportParameters,"wrapper/applicationContext*.xml,wrapper/transfer-audit-data-applicationContext.xml");
		} else {
            ctx= createSpringContext(exportParameters, "wrapper/applicationContext*.xml");
        }
		try {
			boolean success = true;
			CommandMetadata metadataBean = getCommandMetadataBean(ctx);
			if (exportParameters.hasParameter(ARG_HELP)) {
				CommandHelp helpBean = getHelpBean(ctx);
				helpBean.printHelp(args[0], metadataBean, System.out);
			} else {
				if (exportParameters.hasParameter(KEY_PASSWD_PARAMETER)) {
					String keypasswd = exportParameters.getParameterValue(KEY_PASSWD_PARAMETER);

					if (keypasswd == null) {
						keypasswd = requestPassword("Please enter key password");
						exportParameters.setParameterValue(KEY_PASSWD_PARAMETER, keypasswd);
					}
				}

				CommandBean commandBean = getCommandBean(exportParameters, ctx);
				try {
					metadataBean.validateParameters(exportParameters);
					commandOut.info("Processing started");
					commandBean.process(exportParameters);
					commandOut.info("Done");
				} catch (JSExceptionWrapper e) {
					throw e;
				} catch (JSException e) {
					success = false;
					if (log.isInfoEnabled()) {
						log.info(e.getMessage(), e);
					}

					String message = ctx.getMessage(e.getMessage(), e.getArgs(), Locale.getDefault());
					System.err.println(message);
				}
			}
			return success;
		} finally {
			ctx.close();
		}
	}

	protected Parameters parseArgs(String[] args) {
		Parameters exportParameters = CommandUtils.parse(args);
		return exportParameters;
	}

	protected ConfigurableApplicationContext createSpringContext(Parameters exportParameters, String resourceFileName) throws IOException {
		GenericApplicationContext ctx = new GenericApplicationContext();
		XmlBeanDefinitionReader configReader = new XmlBeanDefinitionReader(ctx);
		ctx.getEnvironment().setActiveProfiles("default","engine","jrs");
		String[] resourceFileNames=resourceFileName.split(",");
		List<Resource> resourcesList=new ArrayList<>();
		for(String fileName: resourceFileNames){
			resourcesList.addAll(Arrays.asList(ctx.getResources(fileName)));
		}
		Resource[] resources=resourcesList.toArray(new Resource[0]);
        commandOut.info("First resource path: " + resources[0].getFile().getParent());
		Arrays.sort(resources, new ResourceComparator());
        commandOut.info("Loading configuration resources");
		for (Resource resource : resources) {
            commandOut.debug("Resource name: " + resource.getFilename());
			configReader.loadBeanDefinitions(resource);
		}
		ctx.refresh();
		commandOut.info("Initialization complete");
		return ctx;
	}

	public static class ResourceComparator implements Comparator<Resource> {
		private final static String NAME_SUFFIX = ".xml";
		public int compare(Resource r1, Resource r2) {
			String name1 = r1.getFilename();
			if (name1.endsWith(NAME_SUFFIX)) {
				name1 = name1.substring(0, name1.length() - NAME_SUFFIX.length());
			}

			String name2 = r2.getFilename();
			if (name2.endsWith(NAME_SUFFIX)) {
				name2 = name2.substring(0, name2.length() - NAME_SUFFIX.length());
			}

			return name1.compareTo(name2);
		}
	}
	
	protected static interface SpringResourceFactory {
		Resource create(String location);
	}

	protected static final SpringResourceFactory fileSystemResourceFactory = new SpringResourceFactory() {
		public Resource create(String location) {
			commandOut.debug("Loading Spring configuration file " + location);
			return new FileSystemResource(location);
		}
	};

	protected static final SpringResourceFactory classPathResourceFactory = new SpringResourceFactory() {
		public Resource create(String location) {
			commandOut.debug("Loading Spring configuration classpath resource " + location);
			return new ClassPathResource(location);
		}
	};

	protected void registerConfig(String[] locations, XmlBeanDefinitionReader reader, SpringResourceFactory resourceFactory) {
		if (locations != null && locations.length > 0) {
			for (int i = 0; i < locations.length; i++) {
				String location = locations[i];
				Resource resource = resourceFactory.create(location);
				reader.loadBeanDefinitions(resource);
			}
		}
	}

	protected String getConfigSeparator() {
		return System.getProperty("path.separator");
	}

	protected CommandBean getCommandBean(Parameters exportParameters, ApplicationContext ctx) {
		String beanName = getCommandBeanName(exportParameters);
		return (CommandBean) ctx.getBean(beanName, CommandBean.class);
	}

	protected CommandHelp getHelpBean(ApplicationContext ctx) {
		return (CommandHelp) ctx.getBean(HELP_BEAN_NAME, CommandHelp.class);
	}

	protected CommandMetadata getCommandMetadataBean(ApplicationContext ctx) {
		return (CommandMetadata) ctx.getBean(commandMetaBeanName, CommandMetadata.class);
	}

	protected String getCommandBeanName(Parameters exportParameters) {
		String beanName = exportParameters.getParameterValue(ARG_COMMAND_BEAN);

		if (beanName == null) {
			commandOut.debug("Using default " + defaultBeanName + " command bean");

			beanName = defaultBeanName;
		} else {
			commandOut.debug("Using " + beanName + " command bean");
		}

		return beanName;
	}

}
