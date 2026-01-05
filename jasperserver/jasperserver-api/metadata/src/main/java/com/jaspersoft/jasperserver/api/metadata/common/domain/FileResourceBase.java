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
package com.jaspersoft.jasperserver.api.metadata.common.domain;

import com.jaspersoft.jasperserver.api.JasperServerAPI;


/**
 * FileResource is the interface which represents the JasperServer resource containing some files
 * of any type: images, fonts, jrxml, jar, resource bundles, style templates, xml.
 * It extends {@link Resource}
 *
 * @author Lucian Chirita (lucianc@users.sourceforge.net)
 * @version $Id$
 */
@JasperServerAPI
public interface FileResourceBase
{
	String TYPE_IMAGE = "img";
	String TYPE_FONT = "font";
	String TYPE_JRXML = "jrxml";
	String TYPE_JAR = "jar";
	String TYPE_RESOURCE_BUNDLE = "prop";
	String TYPE_STYLE_TEMPLATE = "jrtx";
	String TYPE_XML = "xml";
	String TYPE_JSON = "json";
    String TYPE_CSS = "css";
    String TYPE_ACCESS_GRANT_SCHEMA = "accessGrantSchema";
    String TYPE_MONGODB_JDBC_CONFIG = "config";
    String TYPE_AZURE_CERTIFICATE = "cer";
    String TYPE_SECURE_FILE = "secureFile";
    String TYPE_MONGODB_SCHEMA = "mongoDbSchema";
    String TYPE_KEY = "key";
    String TYPE_PUB = "pub";
    String TYPE_PPK = "ppk";
    String TYPE_DASHBOARD_COMPONENTS_SCHEMA = "dashboardComponent";

}
