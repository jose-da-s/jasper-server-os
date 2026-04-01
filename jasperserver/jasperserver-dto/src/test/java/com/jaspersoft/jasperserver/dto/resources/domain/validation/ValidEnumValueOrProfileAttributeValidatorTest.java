/*
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

package com.jaspersoft.jasperserver.dto.resources.domain.validation;

import com.jaspersoft.jasperserver.dto.bridge.BridgeRegistry;
import com.jaspersoft.jasperserver.dto.bridge.SettingsBridge;
import com.jaspersoft.jasperserver.dto.common.ErrorDescriptor;
import com.jaspersoft.jasperserver.dto.resources.ClientProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mockito;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;

/**
 * @author Andriy Tivodar <ativodar@tibco>
 */

public class ValidEnumValueOrProfileAttributeValidatorTest {
    private static final String ERROR_CODE = "errorCode";
    private static final String PATH = "path";
    private static final String INVALID_VALUE = "invalid";
    private static final Object SETTINGS_OBJECT = "settingsObject";
    private static final String ERROR_MESSAGE = "path should be one of " + Arrays.toString(TestEnum.values()) + " or profile attribute placeholder but is [invalid]";
    private static final String KEY_PROPERTY_PATH = "propertyPath";
    private static final String KEY_INVALID_VALUE = "invalidValue";
    private static final String KEY_ALLOWED_VALUES = "allowedValues";

    private static final ErrorDescriptor ERROR_DESCRIPTOR = new ErrorDescriptor()
            .setErrorCode(ERROR_CODE)
            .setMessage(ERROR_MESSAGE)
            .setProperties(Arrays.asList(
                    new ClientProperty().setKey(KEY_PROPERTY_PATH).setValue(PATH),
                    new ClientProperty().setKey(KEY_INVALID_VALUE).setValue(INVALID_VALUE),
                    new ClientProperty().setKey(KEY_ALLOWED_VALUES).setValue(Arrays.toString(TestEnum.values()))
            ));

    private ValidEnumValueOrProfileAttribute validEnumValueOrProfileAttribute;
    private SettingsBridge settingsBridge;
    private ConstraintViolation constraintViolation;
    private Path path;
    private ValidEnumValueOrProfileAttributeValidator validEnumValueOrProfileAttributeValidator;

    @BeforeEach
    public void beforeEach() {
        init();
        setup();
    }

    private void init() {
        validEnumValueOrProfileAttribute = Mockito.mock(ValidEnumValueOrProfileAttribute.class);
        settingsBridge = Mockito.mock(SettingsBridge.class);
        constraintViolation = Mockito.mock(ConstraintViolation.class);
        path = Mockito.mock(Path.class);
        validEnumValueOrProfileAttributeValidator = new ValidEnumValueOrProfileAttributeValidator();
    }

    private void setup() {
        doReturn(TestEnum.class).when(validEnumValueOrProfileAttribute).enumClass();
        doReturn(ERROR_CODE).when(validEnumValueOrProfileAttribute).message();
        doReturn(SETTINGS_OBJECT).when(settingsBridge).getSetting("profileAttributes", "$.attributePlaceholderSimplePattern");
        doReturn(path).when(constraintViolation).getPropertyPath();
        doReturn(INVALID_VALUE).when(constraintViolation).getInvalidValue();
        doReturn(PATH).when(path).toString();

        BridgeRegistry.registerBridge(SettingsBridge.class, settingsBridge);
        validEnumValueOrProfileAttributeValidator.initialize(validEnumValueOrProfileAttribute);
    }

    @Test
    public void initialize_notEnumClass_illegalStateException() {
        doReturn(Object.class).when(validEnumValueOrProfileAttribute).enumClass();
        assertThrows(IllegalStateException.class, new Executable() {
            @Override
            public void execute() {
                validEnumValueOrProfileAttributeValidator.initialize(validEnumValueOrProfileAttribute);
            }
        });
    }

    @Test
    public void isValid_nullValue_valid() {
        boolean result = validEnumValueOrProfileAttributeValidator.isValid(null, null);
        assertTrue(result);
    }

    @Test
    public void isValid_enumValue_valid() {
        boolean result = validEnumValueOrProfileAttributeValidator.isValid(TestEnum.one.name(), null);
        assertTrue(result);
    }

    @Test
    public void isValid_nullPattern_valid() {
        BridgeRegistry.registerBridge(SettingsBridge.class, null);

        boolean result = validEnumValueOrProfileAttributeValidator.isValid(SETTINGS_OBJECT.toString(), null);
        assertTrue(result);
    }

    @Test
    public void isValid_valueMatchPattern_valid() {
        boolean result = validEnumValueOrProfileAttributeValidator.isValid(SETTINGS_OBJECT.toString(), null);
        assertTrue(result);
    }

    @Test
    public void isValid_invalid() {
        boolean result = validEnumValueOrProfileAttributeValidator.isValid(INVALID_VALUE, null);
        assertFalse(result);
    }

    @Test
    public void build_invalidValue_errorDescriptor() {
        ErrorDescriptor result = validEnumValueOrProfileAttributeValidator.build(constraintViolation);
        assertEquals(ERROR_DESCRIPTOR, result);
    }

    private enum TestEnum {
        one,
        two
    }
}
