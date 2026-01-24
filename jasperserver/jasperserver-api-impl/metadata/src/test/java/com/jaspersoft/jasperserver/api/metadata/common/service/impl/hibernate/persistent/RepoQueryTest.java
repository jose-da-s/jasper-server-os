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

package com.jaspersoft.jasperserver.api.metadata.common.service.impl.hibernate.persistent;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.Assert;
import org.junit.Test;

import com.jaspersoft.jasperserver.api.metadata.common.domain.Query;
import com.jaspersoft.jasperserver.api.metadata.common.domain.client.QueryImpl;
import com.jaspersoft.jasperserver.api.metadata.common.domain.util.DataContainerStreamUtil;

public class RepoQueryTest {
    
    /**
     * The Query repo object has a "sql" field which contains the actual query to run.
     * It was mapped to a varchar, which has lower limits on some platforms (looking at you, Oracle).
     * Domain queries were getting rather huge and exceeding the 3600-char limit, so we did compression and Base64 encoding.
     * At the time, the only encoder built in was sun.misc.Base64Encoder, which wasn't "official".
     * To support Java 9, we needed to get rid of these kinds of dependencies, and Java 8 added public Base64 encoding.
     * However, there are various flavors; MIME encoding has line breaks, and "basic" does not.
     * The old Base64Encoder was really a MIME encoder and we picked a new encoder that doesn't like the line feeds.
     * Here we are trying to read a query encoded with the old code to make sure it works.
     */
    @Test
    public void readCompressedQueryFromPreviousVersion() {
        RepoQuery repoQuery = new RepoQuery();
        repoQuery.setSql(readFileFromClasspath("js55928.oldCompressedQuery.txt"));
        Query clientQuery = new QueryImpl();
        repoQuery.copyTo(clientQuery, null);
        String expectedQuery = readFileFromClasspath("js55928.expectedQuery.txt");
        String actualQuery = clientQuery.getSql();

        expectedQuery = normalizeLineEndings(expectedQuery);
        actualQuery = normalizeLineEndings(actualQuery);
        
        Assert.assertEquals(expectedQuery, clientQuery.getSql());
    }
    
    public String readFileFromClasspath(String fileName) {
        InputStream is = getClass().getResourceAsStream("/" + fileName);
        byte[] bytes = DataContainerStreamUtil.readData(is);
        return new String(bytes, StandardCharsets.UTF_8);
    }
    
    private String normalizeLineEndings(String input) {
        return input.replace("\r\n", "\n").replace("\r", "\n");
    }
}
