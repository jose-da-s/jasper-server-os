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

import com.jaspersoft.jasperserver.api.engine.jasperreports.util.InputControlLabelResolver;
import org.springframework.context.MessageSource;

import jakarta.servlet.jsp.JspException;
import java.util.HashMap;
import java.util.Map;

/**
 * Label tag resolves i18n labels.
 *
 * @author Yuriy Plakosh
 */
public class LabelTag extends BaseTagSupport {

    public static final String DEFAULT_LABEL_JSP = "/WEB-INF/jsp/modules/inputControls/Label.jsp";
    private static final String BEAN_MESSAGE_SOURCE = "messageSource";
    private static final String LABEL_ATTRIBUTE_NAME = "label";

    protected MessageSource messageSource;
    protected String labelJsp;
    protected String key;

    @Override
    protected int doStartTagInternal() throws Exception {
        return SKIP_BODY;
    }

    @SuppressWarnings({"unchecked"})
    public int doEndTag() throws JspException {
        Map attributes = new HashMap();

        attributes.put(LABEL_ATTRIBUTE_NAME, resolveLabel(key));

        includeNested(getLabelJsp(), attributes);

        return EVAL_PAGE;
    }

    protected String resolveLabel(String key) {
        return InputControlLabelResolver.resolve(key, messageSource, getServerWideMessageSource());
    }

    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public String getLabelJsp() {
        String jsp = labelJsp;
        if (jsp == null) {
            jsp = DEFAULT_LABEL_JSP;
        }
        return jsp;
    }

    public void setLabelJsp(String labelJsp) {
        this.labelJsp = labelJsp;
    }

    public void setKey(String key) {
        this.key = key;
    }

    protected MessageSource getServerWideMessageSource() {
        return (MessageSource) getRequestContext().getWebApplicationContext()
                .getBean(BEAN_MESSAGE_SOURCE, MessageSource.class);
    }
}

