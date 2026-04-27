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
package com.jaspersoft.jasperserver.war.tags;

import com.jaspersoft.jasperserver.war.SessionXssNonceSetterFilter;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.tagext.TagSupport;


/**
 * @author Stanislav Chubar
 */
public class TemplateNonceTag extends TagSupport {
    private static final long serialVersionUID = 1L;
    private static final String DEFAULT_NONCE_MESSAGE = "";

    private String message;

    public TemplateNonceTag() {
        this.message = DEFAULT_NONCE_MESSAGE;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public int doEndTag() throws JspException {
        try {
            final String tokenValue =
                    (String) pageContext.findAttribute(SessionXssNonceSetterFilter.XSS_NONCE_ATTRIB_NAME);

            pageContext.getOut().write(String.format("<!-- %s %s-->\n", tokenValue, this.message));
        } catch (Exception e) {
            throw new JspException(e);
        }

        return EVAL_PAGE;
    }
}


