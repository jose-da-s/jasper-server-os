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

package com.jaspersoft.jasperserver.jaxrs.poc.hypermedia.admin;

import com.jaspersoft.jasperserver.jaxrs.poc.hypermedia.common.representation.HypermediaRepresentation;
import com.jaspersoft.jasperserver.jaxrs.poc.hypermedia.common.Relation;
import com.jaspersoft.jasperserver.jaxrs.poc.hypermedia.common.activity.GenericRequest;
import com.jaspersoft.jasperserver.jaxrs.poc.hypermedia.common.activity.AbstractActivity;
import com.jaspersoft.jasperserver.jaxrs.poc.hypermedia.common.representation.embedded.EmbeddedElement;
import com.jaspersoft.jasperserver.jaxrs.poc.hypermedia.common.representation.Link;
import com.jaspersoft.jasperserver.jaxrs.poc.hypermedia.common.provider.RequestInfoProvider;

import jakarta.annotation.Resource;
import java.text.MessageFormat;

/**
 * @author: Igor.Nesterenko
 * @version: ${Id}
 */

public class ReadAdminActivity extends AbstractActivity {

    @Resource
    protected RequestInfoProvider requestInfoProvider;

    protected String section;

    @Override
    public Object findData(GenericRequest request) {
        //not connected with services
        return null;
    }

    @Override
    public HypermediaRepresentation buildRepresentation() {
        //not support representation
        return null;
    }

    @Override
    public Relation getOwnRelation() {
        return Relation.admin;
    }


    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    @Override
    public EmbeddedElement buildLink() {


        String url = null;
        String title = null;

        if (section != null){

            if ("logging".equals(section)){
                url = MessageFormat.format(
                        "{0}log_settings.html",
                        requestInfoProvider.getBaseUrl()
                );
                title = getMessage("view.settings");
            }else if ("roles".equals(section)){
                url = MessageFormat.format(
                        "{0}flow.html?_flowId=roleListFlow",
                        requestInfoProvider.getBaseUrl()
                );
                title = getMessage("manage.roles");
            }else if ("users".equals(section)){
                url = MessageFormat.format(
                        "{0}flow.html?_flowId=userListFlow",
                        requestInfoProvider.getBaseUrl()
                );
                title = getMessage("manage.users");
            }
        }

        Link link = null;

        if (url != null){
            link = new Link()
                    .setHref(url)
                    .setRelation(getOwnRelation())
                    .setType("text/html")
                    .setProfile("GET")
                    .setTitle(title);
        }



        return link;
    }


}

