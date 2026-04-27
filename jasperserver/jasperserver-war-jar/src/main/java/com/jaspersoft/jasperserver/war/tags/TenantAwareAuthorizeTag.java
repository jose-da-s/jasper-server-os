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
 *//*


package com.jaspersoft.jasperserver.war.tags;

import com.jaspersoft.jasperserver.api.metadata.user.domain.TenantQualified;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.taglibs.authz.JspAuthorizeTag;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.tagext.Tag;

public class TenantAwareAuthorizeTag extends JspAuthorizeTag {


    @Override
    public int doStartTag() throws JspException {

        if (super.getIfAllGranted() != null && super.getIfAllGranted().contains("|*")) {
            String tagRoles = getIfAllGranted();

            // check root roles
            super.setIfAllGranted(tagRoles.replace("|*",""));
            int act = super.doStartTag();
            if (act == Tag.SKIP_BODY || act == Tag.SKIP_PAGE) {
                // if not root then check org roles
                super.setIfAllGranted(tagRoles.replace("|*","|"+ getTenantId()));
                return super.doStartTag();
            } else {
                return act;
            }

        } else if (super.getIfNotGranted() != null && super.getIfNotGranted().contains("|*")) {
            String tagRoles = getIfNotGranted();

            // evaluate root roles
            String rootRoles = tagRoles.replace("|*","");

            // evaluate org roles
            String orgRoles = tagRoles.replace("|*","|" + getTenantId());

            super.setIfNotGranted(rootRoles + "," + orgRoles);

        } else if (super.getIfAnyGranted() != null && super.getIfAnyGranted().contains("|*")) {
            String tagRoles = getIfAnyGranted();

            // evaluate root roles
            String rootRoles =tagRoles.replace("|*","");

            // evaluate org roles
            String orgRoles = tagRoles.replace("|*","|" + getTenantId());

            super.setIfAnyGranted(rootRoles + "," + orgRoles);
        }

        return super.doStartTag();
    }


    private String getTenantId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal != null && principal instanceof TenantQualified) {
            return ((TenantQualified)principal).getTenantId();
        } else return "";
    }

}
*/

