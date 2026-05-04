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
package com.jaspersoft.jasperserver.war;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import jakarta.servlet.http.HttpServletRequestWrapper;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * The class wraps MultipartHttpServletRequest and overwrites getContentType() method
 * to prevent multipart resolution downstream in the filter chain (which otherwise messes up parameters)
 * <p/>
 * @author Andrew Sokolnikov
 * @since Nov 5, 2011
 */
public class MultipartHttpServletRequestWrapper extends HttpServletRequestWrapper implements MultipartHttpServletRequest {

    public MultipartHttpServletRequestWrapper(MultipartHttpServletRequest request) {
        super(request);
    }

    /**
     * Returns null to prevent double decoding for multipart request
     * @return null
     */
    @Override
    public String getContentType() {
        return null;
    }

    public Iterator<String> getFileNames() {
        return ((MultipartHttpServletRequest) getRequest()).getFileNames();
    }

    public MultipartFile getFile(String name) {
        return ((MultipartHttpServletRequest) getRequest()).getFile(name);
    }

    public Map<String, MultipartFile> getFileMap() {
        return ((MultipartHttpServletRequest) getRequest()).getFileMap();
    }

    public MultiValueMap<String, MultipartFile> getMultiFileMap() {
        return ((MultipartHttpServletRequest) getRequest()).getMultiFileMap();
    }

    public List<MultipartFile> getFiles(String name) {
        return ((MultipartHttpServletRequest) getRequest()).getFiles(name);
    }

    public String getMultipartContentType(String paramOrFileName) {
        return ((MultipartHttpServletRequest) getRequest()).getMultipartContentType(paramOrFileName);
    }

    public HttpMethod getRequestMethod() {
        return ((MultipartHttpServletRequest) getRequest()).getRequestMethod();
    }

    public HttpHeaders getRequestHeaders() {
        return ((MultipartHttpServletRequest) getRequest()).getRequestHeaders();
    }

    public HttpHeaders getMultipartHeaders(String paramOrFileName) {
        return ((MultipartHttpServletRequest) getRequest()).getMultipartHeaders(paramOrFileName);
    }
}

