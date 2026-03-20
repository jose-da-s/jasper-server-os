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

package com.jaspersoft.jasperserver.war.common;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Iterator;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * @author Andriy Tivodar <ativodar@tibco>
 * @version $Id$
 */
public class UploadMultipartFilterTest {
    private static final int CONTENT_LENGTH = 100;
    private static final String POST_METHOD = "POST";
    private static final String CONTENT_TYPE_NOT_MULTIPART = "contentTypeNotMultipart";
    private static final String CONTENT_TYPE_MULTIPART = "multipart/;boundary=test";
    private static final String FILE_NAME = "fileName";
    private static final String ORIGINAL_FILE_NAME = "originalFileName";
    private static final String ORIGINAL_FILE_EXTENSION = ".extension";
    private static final String ORIGINAL_FILE_NAME_WITH_EXTENSION = ORIGINAL_FILE_NAME + ORIGINAL_FILE_EXTENSION;

    private UploadMultipartFilter objectUnderTest = new UploadMultipartFilter();

    private HttpServletRequest servletRequest = mock(HttpServletRequest.class);
    private HttpServletResponse servletResponse = mock(HttpServletResponse.class);
    private FilterChain filterChain = mock(FilterChain.class);
    private MultipartResolver multipartResolver = mock(CommonsMultipartResolver.class);
    private MultipartHttpServletRequest multipartHttpServletRequest = mock(MultipartHttpServletRequest.class);
    private Iterator fileNamesIterator = mock(Iterator.class);
    private MultipartFile multipartFile = mock(MultipartFile.class);

    @BeforeEach
    public void setup() {
        doReturn(true).when(multipartResolver).isMultipart(servletRequest);
        doReturn(CONTENT_LENGTH).when(servletRequest).getContentLength();
        doReturn(CONTENT_TYPE_MULTIPART).when(servletRequest).getContentType();
        doReturn(POST_METHOD).when(servletRequest).getMethod();
        doReturn(multipartHttpServletRequest).when(multipartResolver).resolveMultipart(servletRequest);
        doReturn(multipartFile).when(multipartHttpServletRequest).getFile(FILE_NAME);
        doReturn(ORIGINAL_FILE_NAME).when(multipartFile).getOriginalFilename();
        doReturn(fileNamesIterator).when(multipartHttpServletRequest).getFileNames();
        doReturn(true).doReturn(false).when(fileNamesIterator).hasNext();
        doReturn("doesNotMatterString").doReturn(FILE_NAME).when(fileNamesIterator).next();
    }

    @Test
    public void doFilter_requestIsNotMultipartAndContentLengthIsUndefined_nextFilterIsInvokedWithRequest() throws IOException, ServletException {
        doReturn(CONTENT_TYPE_NOT_MULTIPART).when(servletRequest).getContentType();
        doReturn(-1).when(servletRequest).getContentLength();

        objectUnderTest.doFilter(servletRequest, servletResponse, filterChain);
        verify(filterChain).doFilter(servletRequest, servletResponse);
    }

    @Test
    public void doFilter_requestIsNotMultipartAndContentLengthIsDefined_nextFilterIsInvokedWithRequest() throws IOException, ServletException {
        doReturn(CONTENT_TYPE_NOT_MULTIPART).when(servletRequest).getContentType();

        objectUnderTest.doFilter(servletRequest, servletResponse, filterChain);
        verify(filterChain).doFilter(servletRequest, servletResponse);
    }

    @Test
    public void doFilter_requestIsMultipartAndContentLengthIsUndefined_nextFilterIsInvokedWithRequest() throws IOException, ServletException {
        doReturn(-1).when(servletRequest).getContentLength();

        objectUnderTest.doFilter(servletRequest, servletResponse, filterChain);
        verify(filterChain).doFilter(servletRequest, servletResponse);
    }

    @Disabled("MultipartResolver is initiating directly by calling the constructor and can not be mocked")
    @Test
    public void doFilter_requestIsMultipartAndContentLengthIsDefined_nextFilterIsInvokedWithNullRequest() throws IOException, ServletException {
        doReturn(null).when(multipartResolver).resolveMultipart(servletRequest);

        objectUnderTest.doFilter(servletRequest, servletResponse, filterChain);
        verify(filterChain).doFilter(null, servletResponse);
    }

    @Disabled("MultipartResolver is initiating directly by calling the constructor and can not be mocked")
    @Test
    public void doFilter_moreThanOneFileUploadedPerPage_lastFileIsTaken() throws IOException, ServletException {
        objectUnderTest.doFilter(servletRequest, servletResponse, filterChain);
        verify(multipartHttpServletRequest).getFile(FILE_NAME);
    }

    @Disabled("MultipartResolver is initiating directly by calling the constructor and can not be mocked")
    @Test
    public void doFilter_fileInRequestIsNotUploaded_nextFilterIsInvokedWithMultipartRequest() throws IOException, ServletException {
        doReturn(null).when(multipartHttpServletRequest).getFile(FILE_NAME);

        objectUnderTest.doFilter(servletRequest, servletResponse, filterChain);
        verify(filterChain).doFilter(null, servletResponse);
    }

    @Disabled("MultipartResolver is initiating directly by calling the constructor and can not be mocked")
    @Test
    public void doFilter_originalFileNameIsNull_nextFilterIsInvokedWithMultipartRequest() throws IOException, ServletException {
        doReturn(null).when(multipartFile).getOriginalFilename();

        objectUnderTest.doFilter(servletRequest, servletResponse, filterChain);
        verify(filterChain).doFilter(null, servletResponse);
    }

    @Disabled("MultipartResolver is initiating directly by calling the constructor and can not be mocked")
    @Test
    public void doFilter_originalFileNameIsEmpty_nextFilterIsInvokedWithMultipartRequest() throws IOException, ServletException {
        doReturn("").when(multipartFile).getOriginalFilename();

        objectUnderTest.doFilter(servletRequest, servletResponse, filterChain);
        verify(filterChain).doFilter(null, servletResponse);
    }

    @Disabled("MultipartResolver is initiating directly by calling the constructor and can not be mocked")
    @Test
    public void doFilter_originalFileNameHasNotExtension_nextFilterIsInvokedWithMultipartRequest() throws IOException, ServletException {
        objectUnderTest.doFilter(servletRequest, servletResponse, filterChain);
        verify(filterChain).doFilter(null, servletResponse);
    }

    @Disabled("MultipartResolver is initiating directly by calling the constructor and can not be mocked")
    @Test
    public void doFilter_originalFileNameHasExtension_nextFilterIsInvokedWithMultipartRequest() throws IOException, ServletException {
        doReturn(ORIGINAL_FILE_NAME_WITH_EXTENSION).when(multipartFile).getOriginalFilename();

        objectUnderTest.doFilter(servletRequest, servletResponse, filterChain);

        verify(multipartHttpServletRequest).setAttribute(JasperServerConst.UPLOADED_FILE_NAME, ORIGINAL_FILE_NAME);
        verify(multipartHttpServletRequest).setAttribute(JasperServerConst.UPLOADED_FILE_NAME, ORIGINAL_FILE_EXTENSION);
        verify(filterChain).doFilter(multipartHttpServletRequest, servletResponse);
    }

    @Test
    public void init_nothingIsInvoked() throws ServletException {
        objectUnderTest.init(null);
    }

    @Test
    public void destroy_nothingIsInvoked() {
        objectUnderTest.destroy();
    }
}

