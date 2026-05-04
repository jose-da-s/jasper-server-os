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
package com.jaspersoft.jasperserver.core.util.validators;

import com.jaspersoft.jasperserver.api.common.domain.ValidationError;
import com.jaspersoft.jasperserver.api.common.domain.ValidationErrors;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.validation.Errors;

import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * @author Teodor Danciu (teodord@users.sourceforge.net)
 * @author Andriy Godovanets
 * @version $Id$
 */
public class ValidationUtil {
    private static final Log log = LogFactory.getLog(ValidationUtil.class);

    private static final Pattern PATTERN_NAME = Pattern.compile("(\\p{L}|\\p{N}|(\\_)|(\\.)|(\\-)|[;@])+");

    /*
     * function to validate name
     * allows only valid word characters and doesn't allow
     * any space or any special characters for this field
     * arguments string
     * returns boolean
     */
    public static boolean regExValidateName(String inp) throws PatternSyntaxException {
        Matcher mat = PATTERN_NAME.matcher(inp);
        return mat.matches();
    }

    /*
     * function to validate label
     * allows only valid word characters and doesn't allow
     * any special characters for this field (allows space)
     * arguments string
     * returns boolean
     */
    public static boolean regExValidateLabel(String inp) throws PatternSyntaxException {

/*		 Pattern pat = Pattern.compile("([a-zA-Z]+([a-zA-Z0-9])*(((\\s)|(\\_)){1}[a-zA-Z0-9]+)*)");
		 Matcher mat = pat.matcher(inp.trim());
		 if(mat.matches())
		 	return true;
		 else
		 	return false;
*/
        //TODO validate?
        return true;
    }

    /**
     * This method is deprecated use emailInputValidator bean.
     * function to validate email address
     * @param email
     * @return boolean
     */
    @Deprecated
    public static boolean regExValidateEmail(String email) {
        try {
            new InternetAddress(email.trim(), true);
            if (email.trim().length() > 0) {
                Pattern p = Pattern.compile("^[^\\~\\`\\(\\)\\[\\]\\{\\}\\:\"\\;\'/\\?\\<\\>\\+\\=\\\\|\\!\\@\\#\\$\\%\\^\\&\\*]+@([\\w+\\-]+\\.)+(\\w+)");
                Matcher m = p.matcher(email);
                return m.matches();
            }
            return true;
        } catch (AddressException e) {
            if (log.isDebugEnabled()) {
                log.debug("Email address \"" + email + "\" not valid");
            }

            return false;
        }
    }

    /*
     * function to validate jndi Service Name
     * @param
     * @return
     * assumption: pattern is 'jndiName' or 'jdbc:jndiName' or 'jdbc/jndiName'
     */
    public static boolean regExValidateJndiServiceName(String jndiName) throws PatternSyntaxException {
        Pattern pat = Pattern.compile("([a-zA-Z]+(((\\:)|(\\/)){1}[a-zA-Z0-9_]+)*)");
        Matcher mat = pat.matcher(jndiName.trim());
        if(mat.matches())
            return true;
        else
            return false;
    }

    /*
     * function to validate jndi Service Name
     * @param
     * @return
     * assumption: pattern is 'jndiName' or 'jdbc:jndiName' or 'jdbc/jndiName'
     */
    public static boolean regExValidateDbDriver(String driverName) throws PatternSyntaxException {
        Pattern pat = Pattern.compile("([a-zA-Z]+((\\.){1}[a-zA-Z0-9]+)*)");
        Matcher mat = pat.matcher(driverName.trim());
        if(mat.matches())
            return true;
        else
            return false;
    }

    /*
     * function to validate JDBC URL
     * @param
     * @return
     * assumption: pattern is strictly jdbc:dbname://(com.jasper.jsoft)(192.168.192.29(9))(:)(port#)/dbname - MYSQL/HSQLDB
     * assumption: pattern is strictly jdbc:oracle:thin:@(com.jasper.jsoft)(192.168.192.29(9))(:)(port#)/dbname - ORACLE
     */
    public static boolean regExValidateJdbcURL(String jdbcUrl) throws PatternSyntaxException {

        //allow user to enter any free-text
        return true;

	/*
		Pattern pat = Pattern.compile("(jdbc:{1}([a-zA-Z]{3,}:){1,}(((\\/)(\\/))|(\\@)){1}(([0-9]{2,3}((\\.){1}[0-9]{2,3}){3}){1}|([a-zA-Z]+[a-zA-Z0-9]*((\\.){1}[a-zA-Z]+[a-zA-Z0-9]*)*){1}){1}((\\:){1}[0-9]{2,4})*((\\/)|(\\:)){1}[a-zA-Z]+[a-zA-Z0-9]*)");
		Matcher mat = pat.matcher(jdbcUrl.trim());
		if(mat.matches())
			return true;
		else
		 	return false;
	*/

    }

    /**
     *
     */
    public static void copyErrors(ValidationErrors errors, Errors uiErrors)
    {
        if (errors != null && uiErrors != null)
        {
            for(Iterator it = errors.getErrors().iterator(); it.hasNext();)
            {
                ValidationError error = (ValidationError)it.next();
                uiErrors.rejectValue(error.getField(), error.getErrorCode(),
                        error.getErrorArguments(), error.getDefaultMessage());
            }
        }
    }
}
