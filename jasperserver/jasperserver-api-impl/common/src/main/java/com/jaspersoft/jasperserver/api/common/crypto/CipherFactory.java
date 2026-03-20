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

package com.jaspersoft.jasperserver.api.common.crypto;


import com.jaspersoft.jasperserver.api.security.encryption.PlainCipher;
import com.jaspersoft.jasperserver.crypto.EncryptionProperties;
import com.jaspersoft.jasperserver.crypto.JrsKeystore;
import com.jaspersoft.jasperserver.crypto.KeyProperties;
import com.jaspersoft.jasperserver.crypto.KeystoreManager;
import com.jaspersoft.jasperserver.crypto.conf.EncConf;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanInstantiationException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import jakarta.annotation.PostConstruct;
import javax.crypto.spec.SecretKeySpec;
import java.lang.reflect.Constructor;
import java.security.Key;
import java.util.*;

import static com.jaspersoft.jasperserver.api.common.crypto.Hexer.parse;
import static java.lang.Integer.parseInt;
import static java.lang.String.format;
import static java.lang.System.getProperty;

/**
 * This class provides the utilities of a cryptographic cipher for encryption and decryption.
 */
public class CipherFactory extends EncryptionConfiguration implements FactoryBean<PlainCipher> {

    private static Log log = LogFactory.getLog(CipherFactory.class);

    @Autowired
    private ApplicationContext ctx;

    @Autowired
    private KeystoreManager keystoreManager;
    private JrsKeystore keystore;

    private Key key;

    private Class<? extends PlainCipher> cipherClass;

    private String confId;

    private PlainCipher fallbackDecipher;

    private CipherFactory fallbackFactory;

    private boolean allowEncryption = true;

    /**
     * Invoked by a BeanFactory after it has set all bean properties supplied
     * (and satisfied BeanFactoryAware and ApplicationContextAware).
     * <p>This method allows the bean instance to perform initialization only
     * possible when all bean properties have been set and to throw an
     * exception in the event of misconfiguration.
     *
     * @throws Exception in the event of misconfiguration (such
     *                   as failure to set an essential property) or if initialization fails.
     */
    @PostConstruct
    public void init() throws Exception {
        if (fallbackDecipher == null && fallbackFactory != null) fallbackDecipher = fallbackFactory.getObject();

        this.init((Key) null);
    }

    private String resolveValue(final String value, final String propKey, final String defaultValue) {
        return value == null
                ? (propKey != null) ? keystore.getEncryptionProperty(propKey).orElse(value) : defaultValue
                : value;
    }

    private Integer resolveValue(final Integer value, final String propKey, final Integer defaultValue) {
        return value == null
                ? (propKey != null) ? keystore.getEncryptionProperty(propKey).map(Integer::parseInt).orElse(value) : defaultValue
                : value;
    }

    /**
     * All below depend on the one above
     */
//    private Optional<String> keyBytes = empty();

    private EncryptionProperties encryptionProperties;

    private void init(final Key key) throws Exception {
        this.keystore = keystoreManager.getKeystore(null);
        KeyProperties keystoreProperties = null;

        if (confId != null) {
            final EncryptionProperties enc = keystore.getEncryptionProperties(confId);
            this.encryptionProperties = new EncryptionProperties(
                    resolveValue(this.blockSize, blockSizeProp, enc.getBlockSize()),
                    resolveValue(this.initializationVector, initializationVectorProp, enc.getInitializationVector()),
                    resolveValue(this.transformation, transformationProp, enc.getCipherTransformation()),
                    new KeyProperties(
                            resolveValue(this.keyAlgorithm, keyAlgorithmProp, enc.getKeyProperties().getKeyAlg()),
                            resolveValue(this.keySize, keySizeProp, enc.getKeyProperties().getKeySize())
                    ),
                    resolveValue(this.secretKey, secretKeyProp, enc.getSecretKey())
            );
        } else {
            final Integer blockSize = resolveValue(this.blockSize, blockSizeProp, null);
            if (blockSize == null) throw new BeanInstantiationException(cipherClass, "'blockSize' must be specified");

            final Integer keySize = resolveValue(this.keySize, keySizeProp, null);
            if (keySize == null) throw new BeanInstantiationException(cipherClass, "'keySize' must be specified");

            encryptionProperties = new EncryptionProperties(
                    blockSize,
                    resolveValue(this.initializationVector, initializationVectorProp, null),
                    resolveValue(this.transformation, transformationProp, null),
                    new KeyProperties(
                            resolveValue(this.keyAlgorithm, keyAlgorithmProp, null),
                            keySize
                    ),
                    resolveValue(this.secretKey, secretKeyProp, null)
            );
        }

        final String secretKey = this.encryptionProperties.getSecretKey();
        final String keyAlias = resolveValue(this.keyAlias, this.keyAliasProp, null);
        final String keyPass = resolveValue(this.keyPass, this.keyPassProp, null);

        String keyUuid = resolveValue(this.keyUuid, this.keyUuid, UUID.randomUUID().toString());

        try {
            if (key != null) {
                this.key = key;
                this.keyUuid = keyUuid;

            } else if (secretKey != null && !secretKey.isEmpty()) {
                final String keyAlg = this.encryptionProperties.getKeyProperties().getKeyAlg();
                try {
                    this.key = new SecretKeySpec(parse(secretKey), keyAlg);
                    this.keyUuid = keyUuid;
                } catch (Exception e) {
                    log.error(format("Failed to create key using %s. %s", keyAlg, e.getMessage()));
                    throw e;
                }

            } else if (keyAlias != null) {
                this.key = keystore.getKey(new KeyProperties(keyAlias, keyPass));
                this.keyUuid = keyUuid;

            } else if (confId != null) {
                this.key = keystore.getKey(confId);

                try {
                    keystoreProperties = this.keystore.getKeyProperties(confId);
                    String storeKeyUuid = keystoreProperties != null ? keystoreProperties.getKeyUuid().toString() : null;
                    this.keyUuid = storeKeyUuid != null ? storeKeyUuid : keyUuid;
                } catch (Exception e) {
                    this.keyUuid = keyUuid;
                }
            }
        } catch (Exception e) {
            throw new BeanInstantiationException(cipherClass
                    , "Unable to retrieve the key, password maybe invalid", e);
        }

        if (this.key == null || this.key.getEncoded().length == 0) {
            throw new BeanInstantiationException(cipherClass
                    , "Secret key is not specified or incorrectly configured.");
        }
    }

    @Override
    public PlainCipher getObject() {
        try {
            final Constructor<? extends PlainCipher> constructor =
                    cipherClass.getConstructor(EncryptionProperties.class, Key.class, String.class, PlainCipher.class);
            PlainCipher fallbackCipherer = null;
            if (getFallbackFactory() != null) {
                fallbackCipherer = getFallbackFactory().getObject();
            }

            PlainCipher c = constructor.newInstance(this.encryptionProperties, key, keyUuid, fallbackCipherer);
            c.setAllowEncryption(isAllowEncryption());
            return c;
        } catch (Exception e) {
            throw new BeanInstantiationException(cipherClass, "Failed to initialize instance of a cipher", e);
        }
    }

    @Override
    public Class<?> getObjectType() {
        return PlainCipher.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    private CipherFactory createSelf() {
        final CipherFactory factory = new CipherFactory();

        factory.ctx = ctx;
        factory.keystoreManager = keystoreManager;
        factory.cipherClass = cipherClass;
        factory.setConfId(confId);
        copyTo(factory);

        return factory;
    }

    public CipherFactory fork(final Key key) throws Exception {
        return fork(key, null);
    }

    public CipherFactory fork(final Key key, final String keyUuid) throws Exception {
        Objects.requireNonNull(key);

        final CipherFactory factory = createSelf();
        factory.init(key);
        if (keyUuid != null && !keyUuid.isEmpty()) { factory.setKeyUuid(keyUuid); }

        return factory;
    }

    public CipherFactory fork(final String keyAlias, final String keyPasswd) throws Exception {
        Objects.requireNonNull(keyAlias);
        Objects.requireNonNull(keyPasswd);

        final CipherFactory factory = createSelf();
        factory.setKeyAlias(keyAlias);
        factory.setKeyPass(keyPasswd);
        factory.init((Key) null);
        return factory;
    }

    public void setKeystoreManager(KeystoreManager keystoreManager) {
        this.keystoreManager = keystoreManager;
    }

    public void setCipherClass(Class<? extends PlainCipher> cipherClass) {
        this.cipherClass = cipherClass;
    }

    public String getConfId() {
        return confId;
    }

    public void setConfId(String confId) {
        this.confId = confId;
    }

    public void setFallbackDecipher(PlainCipher fallbackDecipher) {
        this.fallbackDecipher = fallbackDecipher;
    }

    public CipherFactory getFallbackFactory() {
        return fallbackFactory;
    }

    public void setFallbackFactory(CipherFactory fallbackFactory) {
        this.fallbackFactory = fallbackFactory;
    }

    public boolean isAllowEncryption() {
        return allowEncryption;
    }

    public void setAllowEncryption(boolean allowEncryption) {
        this.allowEncryption = allowEncryption;
    }
}

