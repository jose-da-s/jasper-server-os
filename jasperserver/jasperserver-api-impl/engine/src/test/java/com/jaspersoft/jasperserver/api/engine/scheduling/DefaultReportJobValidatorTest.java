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
package com.jaspersoft.jasperserver.api.engine.scheduling;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.AssertionErrors.assertTrue;

import com.jaspersoft.jasperserver.api.common.domain.ValidationErrors;
import com.jaspersoft.jasperserver.api.common.domain.impl.ValidationErrorsImpl;
import com.jaspersoft.jasperserver.api.engine.scheduling.domain.ReportJobMailNotification;
import com.jaspersoft.jasperserver.core.util.validators.EmailInputValidator;
import com.jaspersoft.jasperserver.core.util.validators.InputValidator;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

public class DefaultReportJobValidatorTest {

    @Test
    public void validateMailNotificationValidAddresses() {
        ValidationErrors errors = new ValidationErrorsImpl();
        ReportJobMailNotification mailNotification = new ReportJobMailNotification();
        mailNotification.setToAddresses(Arrays.asList("valid@example.com", "another.valid@example.com"));
        mailNotification.setCcAddresses(Collections.singletonList("cc.valid@example.com"));
        mailNotification.setBccAddresses(Collections.singletonList("bcc.valid@example.com"));
        mailNotification.setSubject("Valid Subject");
        mailNotification.setMessageText("Valid Message");

        InputValidator<String> emailValidator = mock(EmailInputValidator.class);
        when(emailValidator.isValid(anyString())).thenReturn(true);

        DefaultReportJobValidator validator = new DefaultReportJobValidator();
        validator.setEmailValidator(emailValidator);

        validator.validateMailNotification(errors, mailNotification, true);

        assertTrue("No errors expected" ,errors.getErrors().isEmpty());
    }

    @Test
    public void validateMailNotificationInvalidAddresses() {
        ValidationErrors errors = new ValidationErrorsImpl();
        ReportJobMailNotification mailNotification = new ReportJobMailNotification();
        mailNotification.setToAddresses(Arrays.asList("invalid-email", "valid@example.com"));
        mailNotification.setCcAddresses(Collections.singletonList("cc.invalid-email"));
        mailNotification.setBccAddresses(Collections.singletonList("bcc.invalid-email"));
        mailNotification.setSubject("Valid Subject");
        mailNotification.setMessageText("Valid Message");

        InputValidator<String> emailValidator = mock(EmailInputValidator.class);
        when(emailValidator.isValid("invalid-email")).thenReturn(false);
        when(emailValidator.isValid("valid@example.com")).thenReturn(true);
        when(emailValidator.isValid("cc.invalid-email")).thenReturn(false);
        when(emailValidator.isValid("bcc.invalid-email")).thenReturn(false);

        DefaultReportJobValidator validator = new DefaultReportJobValidator();
        validator.setEmailValidator(emailValidator);

        validator.validateMailNotification(errors, mailNotification, true);

        assertEquals(3, errors.getErrors().size());
    }

    @Test
    public void validateMailNotificationEmptyAddresses() {
        ValidationErrors errors = new ValidationErrorsImpl();
        ReportJobMailNotification mailNotification = new ReportJobMailNotification();
        mailNotification.setToAddresses(Collections.emptyList());
        mailNotification.setCcAddresses(Collections.emptyList());
        mailNotification.setBccAddresses(Collections.emptyList());
        mailNotification.setSubject("Valid Subject");
        mailNotification.setMessageText("Valid Message");

        DefaultReportJobValidator validator = new DefaultReportJobValidator();

        validator.validateMailNotification(errors, mailNotification, true);

        assertTrue("No errors expected" ,errors.getErrors().isEmpty());
    }

    @Test
    public void validateMailNotificationSubjectTooLong() {
        ValidationErrors errors = new ValidationErrorsImpl();
        ReportJobMailNotification mailNotification = new ReportJobMailNotification();
        mailNotification.setSubject(String.join("", Collections.nCopies(256, "a")));
        mailNotification.setMessageText("Valid Message");

        DefaultReportJobValidator validator = new DefaultReportJobValidator();

        validator.validateMailNotification(errors, mailNotification, true);

        assertEquals(1, errors.getErrors().size());
    }

    @Test
    public void validateMailNotificationMessageTextTooLong() {
        ValidationErrors errors = new ValidationErrorsImpl();
        ReportJobMailNotification mailNotification = new ReportJobMailNotification();
        mailNotification.setSubject("Valid Subject");
        mailNotification.setMessageText(String.join("", Collections.nCopies(2001, "a")));

        DefaultReportJobValidator validator = new DefaultReportJobValidator();

        validator.validateMailNotification(errors, mailNotification, true);

        assertEquals(1, errors.getErrors().size());
    }
}