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

package com.jaspersoft.jasperserver.jaxrs.poc.hypermedia.content.data;

import com.jaspersoft.jasperserver.jaxrs.poc.hypermedia.content.dto.ContentReference;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.*;

/**
 * @author: Igor.Nesterenko
 * @version: ${Id}
 */
@Component
public class ContentReferenceStorage {

    @Resource(name="messageSource")
    protected MessageSource messageSource;

    @Resource(name="contentReference")
    private LinkedHashMap<String, ContentReference> contentReferences;

    public ContentReference findById(String id){
        return localize(contentReferences.get(id));
    }

    public List<ContentReference> findAll(){
        return localize(contentReferences.values());
    }

    public List<ContentReference> findAllByGroup(String searchByGroup){
        List<ContentReference> result = new ArrayList<ContentReference>();
        for (ContentReference content : findAll()) {
            String currentGroup = content.getGroup();
            if (searchByGroup.equals(currentGroup)){
                result.add(content);
            }
        }
        return result;
    }

    protected String getMessage(String key){
        return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
    }

    public ContentReference localize(ContentReference contentReference) {
        ContentReference result = null;
        if (contentReference != null){
            result = new ContentReference(contentReference);
            result.setTitle(getMessage(contentReference.getTitle()));
            result.setDescription(getMessage(contentReference.getDescription()));
        }
        return result;
    }

    public List<ContentReference> localize(Collection<ContentReference> refs) {
        List<ContentReference> result = new ArrayList<ContentReference>();
        for (ContentReference ref : refs) {
            result.add(localize(ref));
        }
        return result;
    }
}


