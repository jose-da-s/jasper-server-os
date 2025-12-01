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

package com.jaspersoft.jasperserver.api.common.crypto;

import com.jaspersoft.jasperserver.api.security.encryption.BaseCipher;
import com.jaspersoft.jasperserver.api.security.encryption.PlainCipher;
import com.jaspersoft.jasperserver.crypto.EncryptionEngine;
import com.jaspersoft.jasperserver.crypto.EncryptionProperties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.security.Key;
import java.util.Objects;

import static com.jaspersoft.jasperserver.api.common.crypto.Hexer.stringify;
import static com.jaspersoft.jasperserver.api.common.util.spring.StaticApplicationContext.getApplicationContext;


/**
 * This class is used to encrypt/decrypt passwords both in spring security domain and also throughout the application.
 * Can used to compare the password presented by a user during auth to a password stored in the database.
 */
public class PasswordCipherer extends BaseCipher implements PasswordEncoder {
	
	private static Log log = LogFactory.getLog(PasswordCipherer.class);
	
    public static final String ID = "passwordEncoder";

    private Cipherer cipherer;
    
    //checks whether password encoding is required. It is configured in Spring environment.
    private boolean allowEncoding = true;
    
    /* Checks whether the submitted key for SecretKeySpec in plain text or a Integer represantation of byte sequence.
     * It is configured in Spring environment.
     */
    private boolean keyInPlainText = false;

    /*
     * the name of the secret-key algorithm to be associated with the given key.
     * the value to be set in Cipherer.keyAlgorithm. It is configured in Spring environment.
     */

    private String secretKeyAlgorithm = null;

    private int secretKeySize;

    /*
     * the name of the transformation, e.g., DES/CBC/PKCS5Padding.
     * the value to be set in Cipherer.keyAlgorithm. It is configured in Spring environment.
     */
    private String cipherTransformation = null;
    private int blockSize;

	/**
     * Bean static accessor.
     *
     * @return PasswordCipherer - can be null if not initialized by Spring yet. Be careful ;)
     */
    public static PasswordCipherer getInstance() {
        return (PasswordCipherer) getApplicationContext().getBean(ID);
    }

    public PasswordCipherer(final EncryptionProperties properties, final Key key, final String keyUuid, final PlainCipher fallbackCipherer) throws NoSuchMethodException {
		super(properties, key, keyUuid, fallbackCipherer);
		cipherer = new Cipherer(properties, key, keyUuid, fallbackCipherer);
		cipherer.setAllowEncryption(allowEncoding);

		this.cipherTransformation = properties.getCipherTransformation();
		this.blockSize = properties.getBlockSize();
		this.keyInPlainText = false;
		this.secretKeyAlgorithm = properties.getKeyProperties().getKeyAlg();
		this.secretKeySize = properties.getKeyProperties().getKeySize();
	}

    public String decryptSecureAttribute(String encPass) {
        if (EncryptionEngine.isEncrypted(encPass)) {
            encPass = encPass.trim().replaceFirst(EncryptionEngine.ENCRYPTION_PREFIX, "").
                    replaceAll(EncryptionEngine.ENCRYPTION_SUFFIX + "$", "");
            return decodePassword(encPass);
        } else {
            return encPass;
        }
    }

    public String encryptSecureAttribute(String rawPass) throws DataAccessException {
        return EncryptionEngine.ENCRYPTION_PREFIX + encodePassword(rawPass) + EncryptionEngine.ENCRYPTION_SUFFIX;
    }

	@Override
	public String encode(String content) {
		return encodePassword(content);
	}

	@Override
	public String decode(String content) {
		return decodePassword(content);
	}

    public boolean isEncrypted(String secureValue) {
        return EncryptionEngine.isEncrypted(secureValue);
    }

    /**
     * <p>Decodes the specified raw password with an implementation specific algorithm if allowEncoding is TRUE.</p>
     * Otherwise it returns encPass.
     *
     * @param encPass
     * @return
     * @throws DataAccessException
     */
    public String decodePassword(String encPass) {
		log.debug("Decode password: " + allowEncoding);
		if(!allowEncoding) return encPass;
		try{
			return cipherer.decode(encPass);
		} catch (Exception ex) {
			log.warn("Password decryption failed", ex);
			throw new DataAccessResourceFailureException(ex.getMessage(), ex.getCause());
		}
	}
    
    /**
     * <p>Encodes the specified raw password with an implementation specific algorithm if allowEncoding is TRUE.</p>
     * Otherwise it returns rawPass.
     *
     * @param rawPass
     * @return
     * @throws DataAccessException
     */
    public String encodePassword(String rawPass) throws DataAccessException {
		log.debug("Encode password: " + allowEncoding);
		if(!allowEncoding) return rawPass;
		try {
			return cipherer.encode(rawPass);
		} catch (Exception ex) {
			log.warn("Password decryption failed", ex);
			throw new DataAccessResourceFailureException(ex.getMessage(), ex.getCause());
		}
	}
    
    
    /********** PasswordEncoder METHODS ****************/
	/* (non-Javadoc)
	 * @see org.springframework.security.authentication.encoding.PasswordEncoder#encodePassword(java.lang.String, java.lang.Object)
	 * NOTE: salt will be ignored since we will use the "secretkey" defined in Spring configuration
	 */
	/*public String encodePassword(String rawPass, Object salt) throws DataAccessException {
		return encodePassword(rawPass);
	}*/

	/**
	 * by this time the encPass should already be decrypted in {@link RepoUser}
	 * @see com.jaspersoft.jasperserver.api.metadata.user.domain.impl.hibernate.RepoUser
	 * @see org.springframework.security.authentication.encoding.PasswordEncoder#isPasswordValid(java.lang.String, java.lang.String, java.lang.Object)
	 * @param encPass
	 * @param otherPass
	 * @param salt is ignore by this implementation
	 * @return
	 * @throws DataAccessException
	 */
	/*public boolean isPasswordValid(String encPass, String otherPass, Object salt) throws DataAccessException {
		*//* (non-Javadoc)
		 * @see org.springframework.security.authentication.encoding.PasswordEncoder#isPasswordValid(java.lang.String, java.lang.String, java.lang.Object)
		 * NOTE: salt will be ignored since we will use the "secretkey" defined in Spring configuration
		 *//*
		Objects.requireNonNull(otherPass);
		return otherPass.equals(encPass);
	}*/

	@Override
	public void setAllowEncryption(boolean flag) {
		setAllowEncoding(flag);
	}

	/********** SPRING BEAN CALLBACKS ****************/
    
	/**
	 * @return Returns the allowEncoding.
	 */
	public boolean isAllowEncoding() {
		return allowEncoding;
	}

	/**
	 * @param allowEncoding The allowEncoding to set.
	 */
	public void setAllowEncoding(boolean allowEncoding) {
		this.allowEncoding = allowEncoding;
	}

	/**
	 * @return Returns the cipherTransformation.
	 */
	public String getCipherTransformation() {
		return cipherTransformation;
	}

	@Override
	public int getBlockSize() {
		return blockSize;
	}

	/**
	 * @return Returns the keyInPlainText.
	 */
	public boolean isKeyInPlainText() {
		return keyInPlainText;
	}

	/**
	 * @return Returns the secretKeyAlgorithm.
	 */
	public String getSecretKeyAlgorithm() {
		return secretKeyAlgorithm;
	}

	@Override
	public String getKeyAlgorithm() {
		return secretKeyAlgorithm;
	}

	@Override
	public int getKeySize() {
		return secretKeySize;
	}

	@Override
	public String getKeyUuid() {
		return this.cipherer.getKeyUuid();
	}

	@Override
	public Key getKey() {
		return this.cipherer.getKey();
	}

	@Override
	public String encode(CharSequence rawPassword) {
		return encodePassword((String) rawPassword);
	}

	@Override
	public boolean matches(CharSequence rawPassword, String encodedPassword) {
	    // To allow setting empty password on Oracle: treat NULL as the empty string
		if (rawPassword == null)
		    rawPassword = "";
		return rawPassword.equals(encodedPassword);
	}
}

