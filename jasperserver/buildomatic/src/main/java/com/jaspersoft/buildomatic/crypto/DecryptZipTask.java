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

package com.jaspersoft.buildomatic.crypto;

import com.jaspersoft.jasperserver.crypto.*;

import static com.jaspersoft.jasperserver.crypto.conf.Defaults.DiagnosticDataEnc;

import com.jaspersoft.jasperserver.crypto.conf.EncConf;
import com.jaspersoft.jasperserver.dto.logcapture.CollectorSettings;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import javax.crypto.spec.SecretKeySpec;
import jakarta.xml.bind.JAXBException;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Key;
import java.util.UUID;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.tools.ant.Project.MSG_ERR;

public class DecryptZipTask extends Task implements com.jaspersoft.buildomatic.crypto.DiagnosticAntTask {

    public static final String COLLECTOR_SETTINGS_XML = "collectorSettings.xml";
    public static final String JS_ENCRYPTED = "jsEncrypted";

    private String inFile;
    private String encProps;
    private String secretKey;
    private String keyAlias;
    private String keyPass;
    private com.jaspersoft.buildomatic.crypto.DiagnosticCryptoUtil diagnosticCryptoUtil = new com.jaspersoft.buildomatic.crypto.DiagnosticCryptoUtil(this);

    /**
     * Decrypts diagnostic ZIP file
     *
     * @throws BuildException
     */
    @Override
    public void execute() throws BuildException {

        File inF = Paths.get(inFile).normalize().toAbsolutePath().toFile();

        if (!inF.exists()) {
            throw new IllegalArgumentException("Input file not exists.");
        }

        CollectorSettings settings = getSettings(inF);

        try {
            KeystoreManager mng = KeystoreManager.getInstance();
            JrsKeystore keystore = mng.getKeystore(null);
            EncryptionProperties encryptionProperties = diagnosticCryptoUtil.getEncryptionProperties(encProps);
            KeyProperties keystoreProperties = diagnosticCryptoUtil.getKeyProperties(encProps);

            Key key = null;
            if (secretKey != null && secretKey.trim().startsWith("0x")) {
                key = new SecretKeySpec(parse(secretKey.trim()),encryptionProperties.getKeyProperties().getKeyAlg());
                logMessage("Using provided key: " + EncryptZipTask.stringify(key.getEncoded()));

            } else if (keyAlias != null && !keyAlias.isEmpty()) {
                try {
                    keystoreProperties = keystore.getKeyProperties(keyAlias);
                    if (keystoreProperties.getKeyAlias() == null) {
                        key = keystore.getKey(keyAlias);
                    } else {
                        final KeyProperties props = KeyProperties.builder()
                                .keyAlias(keystoreProperties.getKeyAlias())
                                .keyPasswd(keyPass == null ? keystoreProperties.getKeyPasswd() : keyPass).build();
                        key = keystore.getKey(props);
                    }
                } catch (Exception e) {
                    key = keystore.getKey(EncConf.from(KeyProperties.builder().keyAlias(keyAlias).keyPasswd(keyPass).build()));
                }

            } else if (keystoreProperties != null) {
                String keyalias = keystoreProperties.getKeyAlias();
                String keypasswd = keystoreProperties.getKeyPasswd();
                if (keyalias != null && keypasswd != null) {
                    key = keystore.getKey(EncConf.from(KeyProperties.builder().keyAlias(keyalias).keyPasswd(keypasswd).build()));
                }
            }
            else if(settings != null && settings.getKeyalias() != null) {
                keystoreProperties = keystore.getKeyProperties(settings.getKeyalias());
                if (keystoreProperties.getKeyAlias() != null) {
                    key = keystore.getKey(
                            EncConf.from(
                                    KeyProperties.builder().keyAlias(keystoreProperties.getKeyAlias()).keyPasswd(keystoreProperties.getKeyPasswd()).build()));
                }
            } else {
                key = keystore.getKey(DiagnosticDataEnc.getConfId());
            }

            if (key == null) {
                throw new IllegalArgumentException("Failed to resolve secret key.");
            }
            if (inF.isDirectory()) {
                File[] files = inF.listFiles();
                if (files != null) {
                    for (File file : files) {
                        if (file.getName().contains("log") && file.getName().contains(JS_ENCRYPTED)) {
                            final Key currentKey = resolveKey(file, keystore, key, settings);

                            File outF = diagnosticCryptoUtil.createDecryptedOutputFile(file);
                            EncryptionEngine.decryptFile(file, outF, currentKey, encryptionProperties);
                        }
                    }
                } else {
                    throw new IllegalArgumentException("No files to decrypt found in directory: " + inF.getName());
                }
            } else {
                final Key currentKey = resolveKey(inF, keystore, key, settings);

                File outF = diagnosticCryptoUtil.createDecryptedOutputFile(inF);
                EncryptionEngine.decryptFile(inF, outF, currentKey, encryptionProperties);
            }
        } catch (Exception e) {
            log("Error decrypting diagnostic file. ", e, MSG_ERR);
        }
    }

    private Key resolveKey(File file, JrsKeystore mng, Key defaultKey, CollectorSettings settings) {
        if(secretKey != null && !secretKey.isEmpty()) return defaultKey;

        final String fileKeyAlias = getKeyAlias(file);
        if(fileKeyAlias == null) return defaultKey;

        final String settingsKeyAlias = settings != null ? settings.getKeyalias() : null;
        if(fileKeyAlias.equalsIgnoreCase(settingsKeyAlias)) return defaultKey;

        final KeyProperties props = mng.getKeyProperties(fileKeyAlias);
        if(props.getKeyAlias() == null) return defaultKey;

        System.out.println(props.getKeyAlias());

        return mng.getKey(EncConf.from(KeyProperties.builder()
                .keyAlias(props.getKeyAlias())
                .keyPasswd(props.getKeyPasswd() != null ? props.getKeyPasswd() : "").build()));
    }

    private String getKeyAlias(File inF) {
        try(BufferedInputStream stream =
                    new BufferedInputStream(new FileInputStream(inF), 512)) {

            byte[] jsEncryptedData = new byte[JS_ENCRYPTED.getBytes().length];
            if (stream.read(jsEncryptedData, 0, jsEncryptedData.length) > 0
                    && JS_ENCRYPTED.equals(new String(jsEncryptedData, UTF_8))) {
                System.out.println(new String(jsEncryptedData, UTF_8));

                byte[] keyAliasData = new byte[36];
                if (stream.read(keyAliasData, 0, keyAliasData.length) > 0) {
                    System.out.println(new String(keyAliasData, UTF_8));

                    try {
                        return UUID.fromString(new String(keyAliasData, UTF_8)).toString();
                    } catch (IllegalArgumentException e) {
                        return null;
                    }
                }
            }
        } catch (IOException e) {
            log("Filed to inspect content of " + inF.getPath(), e, MSG_ERR);
        }

        return null;
    }

    private CollectorSettings getSettings(File file) {
        final Path collectorSettings;
        if (file.isDirectory()) {
            collectorSettings = Paths.get(file.getPath(), COLLECTOR_SETTINGS_XML);
        } else {
            collectorSettings = Paths.get(file.getParent(), COLLECTOR_SETTINGS_XML);
        }

        if (Files.exists(collectorSettings)) {
            try {
                return CollectorSettings.unMarshall(collectorSettings.toString());
            } catch (JAXBException e) {
                log("Error while reading collector settings from file collectorSettings.xml" + e, MSG_ERR);
            }
        }

        return null;
    }

    public static byte[] parse(final String stringOfSpaceSeparatedHex) {
        if (stringOfSpaceSeparatedHex == null || stringOfSpaceSeparatedHex.isEmpty()) {
            return new byte[0];
        }

        String[] hexValues = stringOfSpaceSeparatedHex.split("\\s+");
        return parse(hexValues);
    }

    public static byte[] parse(final String[] hexValues) {
        if (hexValues.length == 0) {
            return new byte[0];
        }

        int length = hexValues.length;

        byte[] bytes = new byte[length];
        for (int i = 0; i < length; i++) {
            bytes[i] = Integer.decode(hexValues[i]).byteValue();
        }

        return bytes;
    }

    public void logMessage(String message) {
        log(message);
    }

    public void setInFile(String inFile) {
        this.inFile = inFile;
    }

    public void setEncProps(String encProps) {
        this.encProps = encProps;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public void setKeyAlias(String keyAlias) {
        this.keyAlias = keyAlias;
    }

    public void setKeyPass(String keyPass) {
        this.keyPass = keyPass;
    }
}
