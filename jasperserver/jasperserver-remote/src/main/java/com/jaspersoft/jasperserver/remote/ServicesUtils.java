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


package com.jaspersoft.jasperserver.remote;

import com.jaspersoft.jasperserver.api.metadata.xml.domain.impl.OperationResult;
import com.jaspersoft.jasperserver.ws.xml.Marshaller;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;


/**
 *
 * @author gtoffoli
 * @version $Id$
 */
@Component
public class ServicesUtils {

    public static final String PARAMETER_ATTRIBUTE_SEPARATOR = ","; //separates between the values that require to define parameter
    public static final String PARAMETER_SEPARATOR = ";"; // separates between parameters

    private final static Log log = LogFactory.getLog(ServicesUtils.class);
    private final static String JS_PROPS_FILE="jasperserver.properties"; // No I18N
    private final static String JS_VERSION="JS_VERSION"; // No I18N
    private static List<String> adminRoles = new LinkedList<String>();

    private MessageSource messageSource; // Used to get various messages

    @Autowired
    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }
    /**
     * Convenient method to create an Operation result with a specific error code and message.
     * @param errorCode
     * @param message
     * @return
     */
    public OperationResult createOperationResult(int errorCode, String message)
    {
        OperationResult or = new OperationResult();
        or.setReturnCode(errorCode);
        or.setMessage(message);
        // this is supposed to be the server version
        or.setVersion( getVersion() );
        return or;
    }

    /**
     * convenient method to load the Jasper Server OS version
     * @return the JS version
     */
    public String getVersion()
    {
        return messageSource.getMessage(JS_VERSION, new Object[]{}, Locale.getDefault());
    }


    /**
     * Convenient method to convert a REST obejct (like OperationResult) in an XML string.
     * Not suggested for big objects.
     * @param obj
     * @return
     */
    public static String marshall(Object obj)
    {
        StringWriter sw = new StringWriter();
        Marshaller.marshal(obj, sw);
        return sw.toString();
    }
}
