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
package com.jaspersoft.jasperserver.remote.services.impl;

import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * <p>Use this checker to check if some URI is hard modify protected</p>
 * Such hard protection is used to protect technical folders, such as foot folder, /temp and others.
 * Inject set of regular expressions to specify protected resources.
 * Currently this class is called from remote module, but this check should be moved to core.
 *
 * @author Yaroslav.Kovalchyk
 * @version $Id$
 */
@Service
public class UriHardModifyProtectionChecker {
    @jakarta.annotation.Resource
    protected Set<String> modifyProtected;

    public boolean isHardModifyProtected(String uri){
        return isUriInSet(uri, modifyProtected);
    }

    private boolean isUriInSet(String uri, Set<String> uris){
        boolean result = false;
        for(String currentPattern : uris){
            if(Pattern.matches(currentPattern, uri)){
                result = true;
                break;
            }
        }
        return result;
    }
}

