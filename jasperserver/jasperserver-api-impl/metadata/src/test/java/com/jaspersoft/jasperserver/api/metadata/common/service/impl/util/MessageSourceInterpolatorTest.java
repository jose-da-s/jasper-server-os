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
package com.jaspersoft.jasperserver.api.metadata.common.service.impl.util;

import com.jaspersoft.jasperserver.api.metadata.common.util.MessageSourceInterpolator;
import org.hibernate.validator.internal.engine.MessageInterpolatorContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.MessageSource;

import jakarta.validation.MessageInterpolator;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.jaspersoft.jasperserver.api.metadata.common.util.ConstraintValidatorContextDecorator.ARGUMENTS;
import static com.jaspersoft.jasperserver.api.metadata.user.service.impl.ProfileAttributesResolverImpl.attributeNameGroup;
import static com.jaspersoft.jasperserver.api.metadata.user.service.impl.ProfileAttributesResolverImpl.categoryGroup;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * <p></p>
 *
 * @author Volodya Sabadosh
 * @version $Id: $
 */
@RunWith(MockitoJUnitRunner.class)
public class MessageSourceInterpolatorTest {
    @InjectMocks
    private MessageSourceInterpolator interpolator;

    @Mock
    private MessageSource messageSource;

    @Mock
    private MessageInterpolator baseInterpolator;

    private String msgKey = "test.message.key";
    private String msgTemplate = "Test message {0} test {1}";
    private String resolvedMessage = "Test message name test category";


    private Map<String, Object> paramsMap = new HashMap<String, Object>() {{
        put(ARGUMENTS, Arrays.asList(attributeNameGroup, categoryGroup));
    }};

    @Before
    public void resetMocks(){
        reset(messageSource, baseInterpolator);
    }

    @Test
    public void interpolate_parametrizedMessageIsIncludedInBundleArgsInNotNull_success() {
        MessageInterpolatorContext messageInterpolatorContext = Mockito.mock(MessageInterpolatorContext.class);
        when(messageInterpolatorContext.getMessageParameters()).thenReturn(paramsMap);
        when(baseInterpolator.interpolate(msgKey, messageInterpolatorContext, Locale.getDefault())).thenReturn(msgKey);
        List<String> paramList = ((List<String>)paramsMap.get(ARGUMENTS));

        when(messageSource.getMessage(msgKey, paramList.toArray(new String[paramList.size()]), Locale.getDefault())).thenReturn(resolvedMessage);

        assertEquals(interpolator.interpolate(msgKey, messageInterpolatorContext, Locale.getDefault()), resolvedMessage);
    }

    @Test
    public void interpolate_parametrizedMessageIsIncludedInBundleArgsInNull_success() {
        MessageInterpolatorContext messageInterpolatorContext = Mockito.mock(MessageInterpolatorContext.class);
        when(messageInterpolatorContext.getMessageParameters()).thenReturn(null);
        when(baseInterpolator.interpolate(msgKey, messageInterpolatorContext, Locale.getDefault())).thenReturn(msgKey);
        when(messageSource.getMessage(msgKey, null, Locale.getDefault())).thenReturn(msgTemplate);

        assertEquals(interpolator.interpolate(msgKey, messageInterpolatorContext, Locale.getDefault()), msgTemplate);
    }

    @Test
    public void interpolate_ArgsInNull_success() {
        MessageInterpolatorContext messageInterpolatorContext = Mockito.mock(MessageInterpolatorContext.class);
        when(messageInterpolatorContext.getMessageParameters()).thenReturn(null);
        when(baseInterpolator.interpolate(msgKey, messageInterpolatorContext, Locale.getDefault())).thenReturn(msgKey);

        interpolator.interpolate(msgKey, messageInterpolatorContext, Locale.getDefault());

        verify(baseInterpolator).interpolate(msgKey, messageInterpolatorContext, Locale.getDefault());
        verify(messageSource).getMessage(msgKey, null, Locale.getDefault());
    }

}
