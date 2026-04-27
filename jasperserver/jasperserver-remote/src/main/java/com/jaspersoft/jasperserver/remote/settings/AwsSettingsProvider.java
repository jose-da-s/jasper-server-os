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
package com.jaspersoft.jasperserver.remote.settings;

import com.jaspersoft.jasperserver.api.engine.jasperreports.util.AwsEc2MetadataClient;
import com.jaspersoft.jasperserver.api.engine.jasperreports.util.AwsProperties;
import com.jaspersoft.jasperserver.api.engine.jasperreports.util.InstanceProductTypeResolver;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * <p></p>
 *
 * @author yaroslav.kovalchyk
 * @version $Id$
 */
@Service
public class AwsSettingsProvider implements SettingsProvider {
    @Resource(name = "filteredAwsRegions")
    private List<String> awsRegions;
    @Resource
    private AwsEc2MetadataClient awsEc2MetadataClient;
    @Resource
    private AwsProperties awsProperties;
    @Resource
    private InstanceProductTypeResolver instanceProductTypeResolverBean;
    @Override
    public Object getSettings() {
        final HashMap<String, Object> settings = new HashMap<String, Object>();
        settings.put("awsRegions", new ArrayList<String>(awsRegions));
        settings.put("isEc2Instance", awsEc2MetadataClient.isEc2Instance());
        settings.put("hasSecurityCredentials", awsEc2MetadataClient.hasSecurityCredentials());
        settings.put("suppressEc2CredentialsWarnings", awsProperties.isSuppressEc2CredentialsWarnings());
        settings.put("productTypeIsEc2",instanceProductTypeResolverBean.isEC2());
        settings.put("productTypeIsJrsAmi",instanceProductTypeResolverBean.isJrsAmi());
        settings.put("productTypeIsMpAmi",instanceProductTypeResolverBean.isMpAmi());
        return settings;
    }
}

