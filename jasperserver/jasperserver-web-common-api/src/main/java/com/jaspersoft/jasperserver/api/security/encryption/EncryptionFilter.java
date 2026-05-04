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
import com.jaspersoft.jasperserver.core.util.JSONUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.context.MessageSource;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyStoreException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * This filter envelopes logic connected with jCryption request parameters encryption. Basically it does:
 * <p/>
 * 1) returns jcryption.js file on urls:
 * - /jCryption/jquery.jcryption.js
 * - /jCryption/jquery.jcryption.min.js
 * 2) generates private + public key on url:
 * - /jCryption/generateKeyPair
 * and public key sends to the browser, private key stores into the session
 * 3) looks for jCryption parameter in HTTP request and when parameters is found tries to decrypt passed data with
 * last private key stored in session (private key is for single use only and then is removed)
 * <p/>
 * Filters and servlets further in the chain works with HttpServletRequestWrapper, that transparently provides both
 * original parameters and decrypted ones as if they all comes in original request.
 *
 * @author Michal Franc, Jan Novotný, FG Forrest, donated to the www.jcryption.org
 * @version $Id: EncryptionFilter.java 21872 2012-01-12 18:57:35Z nmacaraeg $
 */
public class EncryptionFilter implements Filter
{
    private static final Logger logger = LogManager.getLogger(EncryptionFilter.class);
    public static final String DECRYPTED_PREFIX = "DECRYPTED.";

    private FilterConfig filterConfig = null;
    private MessageSource messages;
    private EncryptionManager encryptionMgr;

    public void setEncryptionManager(EncryptionManager encryptionMgr) {
        this.encryptionMgr = encryptionMgr;
    }

    public void init(FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;
    }

    public void destroy() {
        this.filterConfig = null;
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException
    {
        if (encryptionMgr.isEncryptionProcessOn() && request instanceof HttpServletRequest) {
            final HttpServletRequest httpRequest = (HttpServletRequest) request;
            KeyPair keyPair = (KeyPair) httpRequest.getSession().getAttribute(EncryptionManager.KEYPAIR_SESSION_KEY);

            if (keyPair == null && !SecurityConfiguration.isEncryptionDynamicKeyGeneration()) {
                //if login is via url/webservices and the separate request to GetEncryptionKey was not made, read keystore here.
                try {
                    final boolean dynamicKeygenPerRequest = false;
                    keyPair = this.encryptionMgr.generateKeys(dynamicKeygenPerRequest);
                    httpRequest.getSession().setAttribute(EncryptionManager.KEYPAIR_SESSION_KEY, keyPair);
                } catch (Exception e) {
                    logger.error("Key pair was not read correctly from keystore.", e);
                }
            }

            final KeyPair keys = keyPair;
            if (keys != null) {
                boolean decryptionHappenedRemoveKey = false;
                Map<String, String[]> parmaMap = httpRequest.getParameterMap();
                for (Map.Entry<String, String[]> pair : parmaMap.entrySet()) {
                    final Boolean[] decryptHappenedFlag = new Boolean[] {false};
                    final String reqParamKey = pair.getKey();
                    final String[] reqParamValueArray = pair.getValue();

                    if (reqParamValueArray != null && reqParamValueArray.length > 0) {
                        if (EncryptionManager.isEncryptedParam(reqParamKey)) {
                            List<String> decryptedList = encryptionMgr.decrypt(keys.getPrivate(), reqParamValueArray);
                            httpRequest.setAttribute(EncryptionFilter.DECRYPTED_PREFIX + reqParamKey, decryptedList);
                            decryptionHappenedRemoveKey = true;
                        }
                        else if (EncryptionManager.maybeEncryptedJSONParam(reqParamKey)) {
                            List<Object> jsonObjList = new ArrayList<Object>();
                            for (int j = 0; j < reqParamValueArray.length; ++j) {
                                JSONObject reqJsonObj = JSONUtil.getJSONObject(reqParamValueArray[j]);
                                JSONArray reqJsonArr = JSONUtil.getJSONArray(reqParamValueArray[j]);
                                if (reqJsonObj != null || reqJsonArr != null) {
                                    try {
                                        JSONUtil.applyFunctorToJson(reqJsonObj != null ? reqJsonObj : reqJsonArr, new JSONUtil.Functor() {
                                            public String call(String jsonKey, String jsonValue) {
                                                String paramKey = reqParamKey + "." + jsonKey;
                                                if (EncryptionManager.isEncryptedParam(paramKey)) {
                                                    String decryptVal = encryptionMgr.decrypt(keys.getPrivate(), jsonValue).get(0);
                                                    decryptHappenedFlag[0] = true;
                                                    return decryptVal;
                                                }
                                                return jsonValue;
                                            }
                                        });
                                    }
                                    catch (Exception e) {
                                         logger.warn("Skipping decryption for request param '" + reqParamKey + "'.  The json passed in this param " +
                                            "was malformed or decryption failed (URI: " + httpRequest.getRequestURI() + ", Query String: " + httpRequest.getQueryString() + ")", e);
                                    }
                                    jsonObjList.add(reqJsonObj != null ? reqJsonObj : reqJsonArr);
                                }

                            }

                            if (decryptHappenedFlag[0]) {
                                httpRequest.setAttribute(DECRYPTED_PREFIX + reqParamKey, jsonObjList);
                                decryptionHappenedRemoveKey = true;
                            }
                        }
                    }
                }

                if (SecurityConfiguration.isEncryptionDynamicKeyGeneration() && decryptionHappenedRemoveKey)  //Allow key to be used only once (per request)
                    httpRequest.getSession().removeAttribute(EncryptionManager.KEYPAIR_SESSION_KEY);
            }

            filterChain.doFilter(httpRequest, response);
        }
        else {
            filterChain.doFilter(request, response);
        }
    }

    public MessageSource getMessages() {
        return messages;
    }

    public void setMessages(MessageSource messages) {
        this.messages = messages;
    }
}

