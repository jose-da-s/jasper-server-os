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

package com.jaspersoft.hibernate;

import jakarta.xml.bind.annotation.XmlSchema;


import org.hibernate.tool.ant.HibernateToolTask;
import java.text.MessageFormat;

public class JSHibernateToolTask extends HibernateToolTask {

	public JSHibernateToolTask() {
		super();
        setupBindInfoPackage();
	}

	void setupBindInfoPackage() {
	    String nsuri = "http://www.hibernate.org/xsd/orm/cfg";
	    String[] packageInfoClassName = {"org.hibernate.boot.jaxb.hbm.spi.package-info","org.hibernate.boot.jaxb.cfg.spi.package-info"};
	    StringBuilder msg = new StringBuilder();
	    boolean found = false;
            for(String packageName: packageInfoClassName){
		    try {
		    	Class<?> packageInfoClass = Class.forName(packageName);
		        final XmlSchema xmlSchema = packageInfoClass.getAnnotation(XmlSchema.class);
		        if (xmlSchema == null) {
		        	msg.append(MessageFormat.format(
		                    "Class [{0}] is missing the [{1}] annotation. Processing bindings will probably fail.",
		                    packageName, XmlSchema.class.getName()));
		        	msg.append('\n');
		        } else {
		            final String namespace = xmlSchema.namespace();
		            if (!nsuri.equals(namespace)) {
		            	msg.append(MessageFormat.format(
			            "Namespace [{0}] of the [{1}] annotation does not match [{2}]. Processing bindings will probably fail.",
		                    namespace, XmlSchema.class.getName(), nsuri));
		                msg.append('\n');
		            } else {
		            	found = true;
		            	break;
		            }
		        }
		    } catch (ClassNotFoundException cnfex) {
		    	msg.append(MessageFormat.format(
	                    "Class [{0}] could not be found. Processing bindings will probably fail.",
		                packageName));
		    	msg.append('\n');
		    }
             }     
             if(!found){
        	System.out.println(msg.toString());
             }
	}
}
