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

package com.jaspersoft.jasperserver.jaxrs.poc.hypermedia.resource.activity;

import com.jaspersoft.jasperserver.dto.resources.ResourceMediaType;
import com.jaspersoft.jasperserver.jaxrs.poc.hypermedia.common.MediaTypes;
import com.jaspersoft.jasperserver.jaxrs.poc.hypermedia.common.Relation;
import com.jaspersoft.jasperserver.jaxrs.poc.hypermedia.common.provider.RequestInfoProvider;
import com.jaspersoft.jasperserver.jaxrs.poc.hypermedia.common.representation.Link;
import com.jaspersoft.jasperserver.jaxrs.poc.hypermedia.common.representation.embedded.EmbeddedElement;
import com.jaspersoft.jasperserver.remote.services.BatchRepositoryService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jakarta.annotation.Resource;
import java.text.MessageFormat;

/**
 * @author: Igor.Nesterenko
 * @version: ${Id}
 */

public class CreateResourceActivity extends ReadResourceActivity {

    protected static final Log log = LogFactory.getLog(CreateResourceActivity.class);

    @Resource
    private RequestInfoProvider requestInfoProvider;

    @Resource
    private BatchRepositoryService batchRepositoryService;

    @Override
    public Relation getOwnRelation() {
        return Relation.create;
    }

    @Override
    public EmbeddedElement buildLink() {

        Link link = null;

        String url = null;

        String resourceType = data.getResourceType();

        if (resourceType != null) {
            url = MessageFormat.format("{0}flow.html?_flowId=", requestInfoProvider.getBaseUrl());
        }

        if (ResourceMediaType.SEMANTIC_LAYER_DATA_SOURCE_CLIENT_TYPE.equals(resourceType)) {
            url = MessageFormat.format("{0}", requestInfoProvider.getBaseUrl());
            url += "domaindesigner.html?ParentFolderUri=";
        } else if (ResourceMediaType.DATASOURCE_TYPES.contains(resourceType)
                || ResourceMediaType.ANY_DATASOURCE_TYPE.equals(resourceType)) {
            url += "addDataSourceFlow&ParentFolderUri=%2Fdatasources";
        } else if (ResourceMediaType.ADHOC_DATA_VIEW_CLIENT_TYPE.equals(resourceType)) {
            url += "adhocFlow";
        } else if (ResourceMediaType.DASHBOARD_CLIENT_TYPE.equals(resourceType)
                && requestInfoProvider.isSupportedDevice()) {
            url = requestInfoProvider.getBaseUrl().concat("dashboard/designer.html");
        } else if (ResourceMediaType.REPORT_UNIT_CLIENT_TYPE.equals(resourceType)){
            url = "addReport";
        }

        if (url != null) {
            link = new Link()
                    .setTitle(this.getMessage("create"))
                    .setHref(url)
                    .setRelation(getOwnRelation())
                    .setType(MediaTypes.TEXT_HTML)
                    .setProfile("GET");
        }

        return link;

    }



}

