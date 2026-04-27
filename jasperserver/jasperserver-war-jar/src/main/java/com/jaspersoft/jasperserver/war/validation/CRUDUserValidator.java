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
package com.jaspersoft.jasperserver.war.validation;

import com.jaspersoft.jasperserver.api.metadata.user.domain.User;
import com.jaspersoft.jasperserver.api.metadata.user.service.UserAuthorityService;
import com.jaspersoft.jasperserver.core.util.validators.InputValidator;
import com.jaspersoft.jasperserver.core.util.validators.ValidationUtil;
import com.jaspersoft.jasperserver.war.dto.UserWrapper;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class CRUDUserValidator implements Validator {

	private UserAuthorityService userService;

    @jakarta.annotation.Resource(name = "emailInputValidator")
    private InputValidator emailValidator;

    public UserAuthorityService getUserService()
	{
		return userService;
	}

	public void setUserService(UserAuthorityService userService)
	{
		this.userService = userService;
	}

    public InputValidator getEmailValidator() {
        return emailValidator;
    }

    public void setEmailValidator(InputValidator emailValidator) {
        this.emailValidator = emailValidator;
    }

    public boolean supports(Class clazz) {
		return UserWrapper.class.isAssignableFrom(clazz);
	}

	public void validate(Object bean, Errors errors) {
		UserWrapper wrapper = (UserWrapper) bean;
		User user = wrapper.getUser();

		if(user.getUsername() == null || user.getUsername().trim().length() == 0) {
			errors.rejectValue("user.username", "CRUDUserValidator.error.not.empty");
		} else {
			if(!ValidationUtil.regExValidateName(user.getUsername())) {
				errors.rejectValue("user.username", "CRUDUserValidator.error.invalid.chars");
			}

			if (wrapper.isNewMode() && userService.userExists(null, user.getUsername())) {
				errors.rejectValue("user.username", "CRUDUserValidator.error.duplicate");
			}

		}

		if(user.getFullName() == null || user.getFullName().trim().length() == 0) {
			errors.rejectValue("user.fullName", "CRUDUserValidator.error.not.empty");
		} else {
			if(!ValidationUtil.regExValidateLabel(user.getFullName())) {
				errors.rejectValue("user.fullName", "CRUDUserValidator.error.invalid.chars");
			}
		}

		// Only internally defined users have a password

		if (!user.isExternallyDefined()) {
			if(user.getPassword() == null || user.getPassword().trim().length() == 0) {
				errors.rejectValue("user.password", "CRUDUserValidator.error.not.empty");
			} else if (wrapper.getConfirmedPassword() == null || !wrapper.getConfirmedPassword().equals(user.getPassword())) {
				errors.rejectValue("confirmedPassword", "CRUDUserValidator.error.password.not.matches");
			}
		}

		if(user.getEmailAddress() != null) {
			if(user.getEmailAddress().trim().length() > 0) {
				if(!emailValidator.isValid(user.getEmailAddress())) {
					errors.rejectValue("user.emailAddress", "CRUDUserValidator.error.invalid.format");
				}
			} else  {
				if(user.getEmailAddress().trim().length() != user.getEmailAddress().length()) {
					errors.rejectValue("user.emailAddress", "CRUDUserValidator.error.contains.spaces");
				}
			}
		}
	}
}

