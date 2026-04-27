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

package com.jaspersoft.jasperserver.test;

import com.jaspersoft.jasperserver.crypto.KeyProperties;
import com.jaspersoft.jasperserver.export.Parameters;
import com.jaspersoft.jasperserver.export.util.EncryptionParams;
import com.jaspersoft.jasperserver.util.test.BaseServiceSetupTestNG;

import java.util.Optional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.tiles.request.collection.CollectionUtil;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.ContextConfiguration;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

public class ExportResourcesFullTestNG extends BaseServiceSetupTestNG {

    protected static Log m_logger = LogFactory.getLog(ExportResourcesFullTestNG.class);

    public ExportResourcesFullTestNG() {
    }

    @BeforeClass()
    public void onSetUp() throws Exception {
        m_logger.info("onSetUp() called");
    }

    @AfterClass()
    public void onTearDown() throws Exception {
        m_logger.info("onTearDown() called");
    }

    @Test()
    public void doExportEverything() {
        m_logger.info("doExportEverything() called");
        setAuthenticatedUser(BaseServiceSetupTestNG.USER_JASPERADMIN);

        String databaseVendor = getDatabaseProductNameFromProp();

        // set export filename. Like: js-catalog-postgresql-ce.zip, js-catalog-minimal-mysql-ce.zip
       	String outputZipFile = OUTPUT_ZIP_FILE_START + OUTPUT_DASH + databaseVendor
       	                       + OUTPUT_DASH + OUTPUT_EDITION_CE + OUTPUT_DOT + OUTPUT_ZIP_EXT;

        m_logger.info("databaseVendor=" + databaseVendor + ", outputZipFile=" + outputZipFile);
        m_logger.info("databaseVendor=" + databaseVendor + ", outputZipFile=" + outputZipFile);

    	Parameters params = createParameters().addParameterValue(PARAM_EXPORT_ZIP,
                                                      TEST_BASE_DIR + FILE_SEPARATOR + outputZipFile);
        KeyProperties keystoreProperties = keystoreManager.getKeystore(null).getKeyProperties(cipherFactory.getConfId());
        String keyAlias = keystoreProperties.getKeyAlias();
        String keyPwd = keystoreProperties.getKeyPasswd();
    	params.addParameterValue(EncryptionParams.KEY_ALIAS_PARAMETER, keyAlias);
    	params.addParameterValue(EncryptionParams.KEY_PASSWD_PARAMETER, keyPwd);
    	params.addParameter(PARAM_EVERYTHING);

        m_logger.info("About to export all resources from repository...");
    	performExport(params);
    }

}
