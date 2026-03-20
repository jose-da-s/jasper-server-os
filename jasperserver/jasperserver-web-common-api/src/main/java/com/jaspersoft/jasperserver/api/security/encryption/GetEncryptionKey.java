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

package com.jaspersoft.jasperserver.api.security.encryption;

import com.jaspersoft.jasperserver.api.security.SecurityConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

import java.security.KeyPair;

import static com.jaspersoft.jasperserver.api.common.util.spring.StaticApplicationContext.getApplicationContext;

/**
 * Created by IntelliJ IDEA.
 * User: dlitvak
 * Date: 3/5/12
 * Time: 2:50 PM
 * To change this template use File | Settings | File Templates.
 */

/**
 * Obtain the public encryption key from server
 */
public class GetEncryptionKey extends HttpServlet {
    private static final Logger logger = LogManager.getLogger(GetEncryptionKey.class);
    private static final String ENCRYPTION_MANAGER = "encryptionManager";

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        try(PrintWriter out  = res.getWriter()) {
            if (SecurityConfiguration.isEncryptionOn()) {
                KeyPair keyPair = (KeyPair) req.getSession().getAttribute(EncryptionManager.KEYPAIR_SESSION_KEY);
                final boolean dynamicKeyGeneration = SecurityConfiguration.isEncryptionDynamicKeyGeneration();

                EncryptionManager manager = (EncryptionManager) getApplicationContext().getBean(ENCRYPTION_MANAGER);

                if (dynamicKeyGeneration || keyPair == null) {
                    keyPair = manager.generateKeys(dynamicKeyGeneration);

                    req.getSession().setAttribute(EncryptionManager.KEYPAIR_SESSION_KEY, keyPair);
                }

                JSONObject jsonObj = manager.buildPublicKeyJSON(keyPair.getPublic());
                out.print(jsonObj.toString());
            } else {
                out.print("{\"Error\": \"Key generation is off\"}");
            }
        } catch (Exception e) {
            logger.error("Unable to generate keys.", e);
        }
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        doGet(req, res);
    }

}

