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

package com.jaspersoft.jasperserver.war.httpheaders;

import org.apache.http.Consts;
import org.apache.http.Header;
import org.apache.http.protocol.HTTP;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This class created to be able to pre-define headers for the response (before it's processed) and keep possibility
 * to override defaults, because servlet API don't support header removal.
 *
 * It tracks all headers added to the response and
 * keeps list of <b>pre-defined headers which will be set only if header was not defined in the service</b>.
 * Predefined headers will be set right before first write to the output stream.
 *
 * If also Jersey API is used to build response
 * sometime we may ended up with repeated/duplicated headers in this case all values will be combined together and
 * if value is correct that it may workout but if it's not, than unpredicted results may happened (especially in case of Cache-Control)
 *
 */
public class HttpResponseHeadersAccumulator extends HttpServletResponseWrapper {


    private List<Header> predefinedHeaders;
    private Map<String, String> definedHeaders;

    private ServletOutputStream outputStream;
    private PrintWriter printWriter;

    /**
     * We can use only stream or writer.
     */
    protected boolean usingOutputStream = false;
    protected boolean usingWriter = false;

    protected boolean headersApplied = false;

    public HttpResponseHeadersAccumulator(HttpServletResponse response, List<Header> predefinedHeaders) {
        super(response);
        this.predefinedHeaders = predefinedHeaders;
        this.definedHeaders = new LinkedHashMap<String, String>();
    }

    public boolean isHeadersApplied() {
        return headersApplied;
    }

    public List<Header> getPredefinedHeaders() {
        return predefinedHeaders;
    }

    public void trackHeaderSet(String name, String value) {
        if (name == null || name.length() == 0 || value == null) {
            return;
        }

        if (isCommitted()) {
            return;
        }

        this.definedHeaders.put(name, value);
    }

    @Override
    public void setHeader(String name, String value) {
        super.setHeader(name, value);
        trackHeaderSet(name, value);
    }

    @Override
    public void addHeader(String name, String value) {
        super.addHeader(name, value);
        trackHeaderSet(name, value);
    }

    @Override
    public void reset() {
        super.reset();
        this.definedHeaders.clear();
    }

    public ServletOutputStreamObserver createOutputStreamObserver() throws IOException {
        return new ServletOutputStreamObserver(getResponse().getOutputStream(), this) {

            public boolean isReady() {
                return true;
            }


            public void setWriteListener(WriteListener writeListener) {

            }

            @Override
            void beforeUsing(HttpResponseHeadersAccumulator response) {
                response.applyHeadersOnce();
            }
        };
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if (usingWriter) {
            throw new IllegalStateException("PrintWriter has been already used.");
        }

        usingOutputStream = true;
        if (this.outputStream == null) {
            this.outputStream = createOutputStreamObserver();
        }

        return this.outputStream;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if (usingOutputStream) {
            throw new IllegalStateException("OutputStream has been already used.");
        }

        usingWriter = true;
        if (this.printWriter == null) {
            String characterEncoding = this.getCharacterEncoding();
            this.printWriter = new PrintWriter(
                    new OutputStreamWriter(
                        createOutputStreamObserver(),
                        characterEncoding != null ? characterEncoding : Consts.ISO_8859_1.name()));
        }
        return this.printWriter;
    }

    @Override
    public void flushBuffer() throws IOException {
        this.applyHeadersOnce();

        if (printWriter != null) {
            printWriter.flush();
        }

        if (outputStream != null) {
            outputStream.flush();
        }

        super.flushBuffer();
    }

    public boolean wasHeaderDefined(String name) {
        return this.definedHeaders.containsKey(name);
    }

    public void applyHeadersOnce() {
        if (!headersApplied) {
            headersApplied = true;
            applyHeaders(this);
        }
    }

    public static void applyHeaders(HttpResponseHeadersAccumulator response) {
        for (Header header : response.getPredefinedHeaders()) {

            if (!response.wasHeaderDefined(header.getName())) {
                response.setHeader(header.getName(), header.getValue());
            }
        }
    }

    abstract class ServletOutputStreamObserver extends ServletOutputStream {

        private ServletOutputStream outputStream;
        private HttpResponseHeadersAccumulator response;

        protected AtomicBoolean isFirstWrite;

        public ServletOutputStreamObserver(ServletOutputStream outputStream, HttpResponseHeadersAccumulator response) throws IOException {
            this.response = response;
            this.outputStream = outputStream;

            this.isFirstWrite = new AtomicBoolean(true);
        }

        @Override
        public void write(int b) throws IOException {
            if (isFirstWrite.compareAndSet(true, false)) {
                this.beforeUsing(this.response);
            }
            outputStream.write(b);
        }

        @Override
        public void flush() throws IOException {
            outputStream.flush();
        }

        @Override
        public void close() throws IOException {
            outputStream.close();
        }

        abstract void beforeUsing(HttpResponseHeadersAccumulator response);
    }
}

