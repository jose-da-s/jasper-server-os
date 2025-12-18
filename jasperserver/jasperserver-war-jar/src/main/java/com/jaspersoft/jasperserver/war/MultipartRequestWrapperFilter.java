/*
 * Copyright (C) 2025 the Jasper Server OS Authors
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

import com.jaspersoft.jasperserver.war.common.JasperServerConst;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartResolver;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Iterator;

/**
 * The filter decodes multipart requests and substitute the original
 * request object by {@link com.jaspersoft.jasperserver.war.MultipartHttpServletRequestWrapper} instance 
 *
 * User: Andrew Sokolnikov
 * Date: Nov 4, 2011
 */
public class MultipartRequestWrapperFilter implements Filter {

    protected final Log log = LogFactory.getLog(this.getClass());
    private MultipartResolver multipartResolver;

    public void setMultipartResolver(MultipartResolver multipartResolver) {
        this.multipartResolver = multipartResolver;
    }

    public void init(FilterConfig filterConfig) throws ServletException {
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        // unwrap multipart request
        try {
            if (multipartResolver.isMultipart((HttpServletRequest) request) && request.getContentLength() > 0) {
                MultipartHttpServletRequest multipartHttpServletRequest = multipartResolver.resolveMultipart((HttpServletRequest) request);
                request = new MultipartHttpServletRequestWrapper(multipartHttpServletRequest);

                // support for file resource wizard
                {
                    MultipartHttpServletRequest mreq = (MultipartHttpServletRequest) request;
                    Iterator iterator = mreq.getFileNames();
                    String fieldName = null;
                    while(iterator.hasNext()){
                        fieldName=(String)iterator.next();
                        // Assuming only 1 file is uploaded per page
                        // can be modified to handle multiple uploads per request
                    }
                    MultipartFile file = mreq.getFile(fieldName);
                    if(file != null){
                        String fullName = file.getOriginalFilename();
                        if(fullName != null && fullName.trim().length() != 0){
                            int lastIndex = fullName.lastIndexOf(".");
                            if (lastIndex != -1){
                                String fileName = fullName.substring(0, lastIndex);
                                String extension = fullName.substring(lastIndex + 1);
                                mreq.setAttribute(JasperServerConst.UPLOADED_FILE_NAME,fileName);
                                mreq.setAttribute(JasperServerConst.UPLOADED_FILE_EXT,extension);
                            } else {
                                mreq.setAttribute(JasperServerConst.UPLOADED_FILE_NAME, fullName);
                            }
                        }
                    }
                }

            }
        } catch (MultipartException e) {
            log.error("Cannot resolve multipart data", e);
        }

        chain.doFilter(request, response);

    }

    public void destroy() {
    }

}
