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

package com.jaspersoft.jasperserver.core.util.validators;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author  schubar
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles({"default","engine","jrs"})
@ContextConfiguration(locations={"classpath:context.xml"})
public class EmailValidatorTest {

    @jakarta.annotation.Resource(name="emailValidator")
    private InputValidator<String> validator;

    @Test
    public void shouldValidateSimpleEmail() {
        assertTrue(validator.isValid("niceandsimple@example.com"));
    }
    @Test
    public void shouldValidateSimpleEmailWithApostrophe() {
        assertTrue(validator.isValid("nice'and'simple@example.com"));
    }

    @Test
    public void shouldValidateCommonEmail() {
        assertTrue(validator.isValid("very.common@example.com"));
    }
    @Test
    public void shouldValidateLongEmail() {
        assertTrue(validator.isValid("a.little.lengthy.but.fine@dept.example.com"));
    }
    @Test
    public void shouldValidateEmailWithPlus() {
        assertTrue(validator.isValid("disposable.style.email.with+symbol@example.com"));
    }

    /**
     * This email is valid by  RFC spec, but email validator will not pass it
     * @throws Exception
     */
    @Test
    @Ignore
    public void shouldValidateIP6Email() {
        assertTrue(validator.isValid("user@[IPv6:2001:db8:1ff::a0b:dbd0]"));
    }
    @Test
    public void shouldValidateEmailWithSpace() {
        assertTrue(validator.isValid("\"much.more unusual\"@example.com"));
    }
    @Test
    public void shouldValidateEmailWithSymbols() {
        assertTrue(validator.isValid("!#$%&'*+-/=?^_`{}|~@example.org"));
    }
    @Test
    public void shouldValidateLocalDomainEmail() {
        assertTrue(validator.isValid("admin@mailserver1"));
    }
    @Test
    public void shouldValidateCorrectMail() {
        assertTrue(validator.isValid("\"()<>[]:,;@\\\\\\\"!#$%&'*+-/=?^_`{}| ~.a\"@example.org"));
    }
    @Test
    public void shouldValidateVeryLongEmail() {
        assertTrue(validator.isValid("\"very.(),:;<>[]\\\".VERY.\\\"very@\\\\ \\\"very\\\".unusual\"@strange.example.com"));
    }
    @Test
    public void shouldValidateTopLevelDomainEmail() {
        assertTrue(validator.isValid("postbox@com"));
    }
    @Test
    public void shouldInvalidateEmailWithoutAt() {
        assertFalse(validator.isValid("Abc.example.com"));
    }
    @Test
    public void shouldInvalidateEmailWithToManyAt() {
        assertFalse(validator.isValid("A@b@c@example.com"));
    }
    /**
     *
     * This email is invalid by RFC spec, but email validator will not pass it
     * @throws Exception
     */
    @Ignore
    @Test
    public void shouldInvalidateEmail1() {
        assertFalse(validator.isValid("just\"not\"right@example.com"));
    }
    @Test
    public void shouldInvalidateEmail2() {
        assertFalse(validator.isValid("this is\"not\\allowed@example.com"));
    }
    @Test
    public void shouldInvalidateEmail3() {
        assertFalse(validator.isValid("this\\ still\\\"not\\\\allowed@example.com"));
    }
    @Test
    public void shouldInvalidateEmail4() {
        assertFalse(validator.isValid("a\"b(c)d,e:f;g<h>i[j\\k]l@example.com"));
    }
}

