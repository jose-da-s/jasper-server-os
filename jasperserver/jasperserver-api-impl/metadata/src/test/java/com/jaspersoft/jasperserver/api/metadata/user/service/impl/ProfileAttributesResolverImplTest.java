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

package com.jaspersoft.jasperserver.api.metadata.user.service.impl;

import com.jaspersoft.jasperserver.api.JSConstraintViolationException;
import com.jaspersoft.jasperserver.api.JSException;
import com.jaspersoft.jasperserver.api.JSProfileAttributeException;
import com.jaspersoft.jasperserver.api.common.domain.AttributedObject;
import com.jaspersoft.jasperserver.api.common.domain.ExecutionContext;
import com.jaspersoft.jasperserver.api.metadata.common.util.ConstraintValidatorContextDecorator;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.client.AwsReportDataSourceImpl;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.client.JdbcReportDataSourceImpl;
import com.jaspersoft.jasperserver.api.metadata.user.domain.ProfileAttribute;
import com.jaspersoft.jasperserver.api.metadata.user.domain.client.ProfileAttributeImpl;
import com.jaspersoft.jasperserver.api.metadata.user.service.ProfileAttributeCategory;
import com.jaspersoft.jasperserver.api.metadata.user.service.ProfileAttributeService;
import com.jaspersoft.jasperserver.dto.common.AttributeErrorCode;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.context.MessageSource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.jaspersoft.jasperserver.api.metadata.user.service.ProfileAttributeCategory.HIERARCHICAL;
import static com.jaspersoft.jasperserver.api.metadata.user.service.ProfileAttributeCategory.USER;
import static com.jaspersoft.jasperserver.api.metadata.user.service.ProfileAttributesResolver.SKIP_PROFILE_ATTRIBUTES_RESOLVING;
import static com.jaspersoft.jasperserver.api.metadata.user.service.impl.ProfileAttributesResolverImpl.attributeNameGroup;
import static com.jaspersoft.jasperserver.api.metadata.user.service.impl.ProfileAttributesResolverImpl.categoryGroup;
import static com.jaspersoft.jasperserver.api.metadata.user.service.impl.ProfileAttributesResolverImpl.isSkipProfileAttributesResolving;
import static com.jaspersoft.jasperserver.dto.common.AttributeErrorCode.Codes.ATTRIBUTE_PATTERNS_INCLUDES_INVALID;
import static com.jaspersoft.jasperserver.dto.common.AttributeErrorCode.PROFILE_ATTRIBUTE_SUBSTITUTION_CATEGORY_INVALID_IN_RESOURCE;
import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * <p></p>
 *
 * @author Volodya Sabadosh
 * @version $Id$
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:applicationContext-profileAttribute-mocks.xml"
})
@ActiveProfiles("test")
public class ProfileAttributesResolverImplTest {

    @Resource
    private ProfileAttributesResolverImpl profileAttributesResolverImpl;

    @Resource
    private List<String> invalidAttributePlaceholderPatterns;

    @Resource
    private List<String> attributePlaceholderPatterns;

    @Resource
    private Validator beanValidator;

    @Resource
    private List<ProfileAttributeCategory> profileAttributeCategories;

    @Mock
    @Resource
    private ProfileAttributeService profileAttributeService;

    @Mock
    Set<String> excludedResourcesFromAttrResolving;

    @Resource
    @Mock
    private MessageSource messageSource;

    private JdbcReportDataSourceImpl parametrizedJdbcReportDataSource;
    private JdbcReportDataSourceImpl notParametrizedJdbcReportDataSource;

    private String templateString = "templateString";
    private String identifier = "identifier";
    private String attrPlaceholder = "attrPlaceholder";
    private String arg1 = "arg1";
    private String arg2 = "arg2";

    private static String jdbcDriverClass = "4";
    private static String jdbcUserName = "joeUser";
    private static String jdbcUserPassword = "securePassword";
    private static String jdbcConnectionUrl = "jdbc:postgresql://localhost:5432/foodmart";
    private static final String functionPattern = "\\s*attribute\\s*\\(\\s*'([^\\\\/']+)'\\s*(,\\s*'([^\\\\/']+)'\\s*)?\\)\\s*";

    @Before
    public void refresh() throws SQLException {
        reset(profileAttributeService, messageSource);

        profileAttributesResolverImpl.setEnabledResolving(true);
        profileAttributesResolverImpl.setValidator(beanValidator);

        parametrizedJdbcReportDataSource = new JdbcReportDataSourceImpl();
        parametrizedJdbcReportDataSource.setName("Some name");
        parametrizedJdbcReportDataSource.setLabel("{attribute('name')}");
        parametrizedJdbcReportDataSource.setDescription("{attribute('description')}");
        parametrizedJdbcReportDataSource.setPassword("{attribute('password')}");
        parametrizedJdbcReportDataSource.setUsername("{attribute('userName', 'User')}");
        parametrizedJdbcReportDataSource.setConnectionUrl("{attribute('connectionUrl', 'Tenant')}");
        parametrizedJdbcReportDataSource.setDriverClass("{attribute('driverClass', 'Server')}");

        notParametrizedJdbcReportDataSource = new JdbcReportDataSourceImpl();
        notParametrizedJdbcReportDataSource.setName("Some name");
        notParametrizedJdbcReportDataSource.setDescription("Some description");
        notParametrizedJdbcReportDataSource.setPassword("Some password");
        notParametrizedJdbcReportDataSource.setUsername("Some userName");
        notParametrizedJdbcReportDataSource.setConnectionUrl("Some connectionUrl");
        notParametrizedJdbcReportDataSource.setDriverClass("Some driverClass");

        when(messageSource.getMessage(any(String.class), any(Object[].class), any(Locale.class))).thenReturn("Some message");

        List<ProfileAttribute> allProfileAttributesList = new ArrayList<ProfileAttribute>();
        List<ProfileAttribute> userProfileAttributesList = new ArrayList<ProfileAttribute>();
        List<ProfileAttribute> tenantProfileAttributesList = new ArrayList<ProfileAttribute>();
        List<ProfileAttribute> serverProfileAttributesList = new ArrayList<ProfileAttribute>();

        final String custom = "custom";

        ProfileAttribute profileAttribute = new ProfileAttributeImpl();
        profileAttribute.setAttrName("userName");
        profileAttribute.setAttrValue(jdbcUserName);
        profileAttribute.setGroup(custom);
        userProfileAttributesList.add(profileAttribute);

        profileAttribute = new ProfileAttributeImpl();
        profileAttribute.setAttrName("password");
        profileAttribute.setAttrValue(jdbcUserPassword);
        profileAttribute.setGroup(custom);
        userProfileAttributesList.add(profileAttribute);

        profileAttribute = new ProfileAttributeImpl();
        profileAttribute.setAttrName("name");
        profileAttribute.setAttrValue("name");
        profileAttribute.setGroup(custom);
        tenantProfileAttributesList.add(profileAttribute);

        profileAttribute = new ProfileAttributeImpl();
        profileAttribute.setAttrName("description");
        profileAttribute.setAttrValue("description");
        profileAttribute.setGroup(custom);
        tenantProfileAttributesList.add(profileAttribute);

        profileAttribute = new ProfileAttributeImpl();
        profileAttribute.setAttrName("connectionUrl");
        profileAttribute.setAttrValue(jdbcConnectionUrl);
        profileAttribute.setGroup(custom);
        tenantProfileAttributesList.add(profileAttribute);

        profileAttribute = new ProfileAttributeImpl();
        profileAttribute.setAttrName("driverClass");
        profileAttribute.setAttrValue(jdbcDriverClass);
        profileAttribute.setGroup(custom);
        serverProfileAttributesList.add(profileAttribute);

        allProfileAttributesList.addAll(userProfileAttributesList);
        allProfileAttributesList.addAll(tenantProfileAttributesList);
        allProfileAttributesList.addAll(serverProfileAttributesList);

        when(profileAttributeService.getCurrentUserProfileAttributes(any(ExecutionContext.class), eq(USER))).thenReturn(userProfileAttributesList);
        when(profileAttributeService.getCurrentUserProfileAttributes(any(ExecutionContext.class), eq(ProfileAttributeCategory.TENANT))).thenReturn(tenantProfileAttributesList);
        when(profileAttributeService.getCurrentUserProfileAttributes(any(ExecutionContext.class), eq(ProfileAttributeCategory.SERVER))).thenReturn(serverProfileAttributesList);
        when(profileAttributeService.getCurrentUserProfileAttributes(any(ExecutionContext.class), eq(ProfileAttributeCategory.HIERARCHICAL))).thenReturn(allProfileAttributesList);
    }

    @Test
    public void merge_allAttributesIsValidAndDefined_passed() {

        Logger log = LogManager.getLogger("com.jaspersoft.jasperserver.api.metadata.user.service.impl.ProfileAttributesResolverImpl");
        Configurator.setLevel(log.getName(), Level.DEBUG);

        JdbcReportDataSourceImpl result = profileAttributesResolverImpl.mergeResource(parametrizedJdbcReportDataSource);
        assertEquals(result.getDriverClass(), jdbcDriverClass);
        assertEquals(result.getUsername(), jdbcUserName);
        assertEquals(result.getPassword(), jdbcUserPassword);
        assertEquals(result.getConnectionUrl(), jdbcConnectionUrl);
        verify(messageSource, times(4)).getMessage(eq("profile.attribute.debug.substitution.success"), any(Object[].class), any(Locale.class));
        Configurator.setLevel(log, Level.ERROR);
    }

    @Test
    public void merge_descriptionAndLabelAreSkiped_passed() {
        JdbcReportDataSourceImpl result = profileAttributesResolverImpl.mergeResource(parametrizedJdbcReportDataSource);

        assertEquals(result.getLabel(), "{attribute('name')}");
        assertEquals(result.getDescription(), "{attribute('description')}");
    }

    @Test
    public void merge_ResourcesIsExcludedFromResolving_passed() {
        AwsReportDataSourceImpl awsReportDataSource = new AwsReportDataSourceImpl();
        awsReportDataSource.setUsername(parametrizedJdbcReportDataSource.getUsername());
        awsReportDataSource.setDriverClass(parametrizedJdbcReportDataSource.getDriverClass());
        awsReportDataSource.setPassword(parametrizedJdbcReportDataSource.getPassword());
        awsReportDataSource.setConnectionUrl(parametrizedJdbcReportDataSource.getConnectionUrl());

        JdbcReportDataSourceImpl result = profileAttributesResolverImpl.mergeResource(awsReportDataSource);

        assertEquals(result.getDriverClass(), awsReportDataSource.getDriverClass());
        assertEquals(result.getUsername(), awsReportDataSource.getUsername());
        assertEquals(result.getPassword(), awsReportDataSource.getPassword());
        assertEquals(result.getConnectionUrl(), awsReportDataSource.getConnectionUrl());
    }

    @Test
    public void merge_ResolvingIsDisabled_passed() {
        profileAttributesResolverImpl.setEnabledResolving(false);
        JdbcReportDataSourceImpl result = profileAttributesResolverImpl.mergeResource(parametrizedJdbcReportDataSource);

        assertEquals(result.getDriverClass(), parametrizedJdbcReportDataSource.getDriverClass());
        assertEquals(result.getUsername(), parametrizedJdbcReportDataSource.getUsername());
        assertEquals(result.getPassword(), parametrizedJdbcReportDataSource.getPassword());
        assertEquals(result.getConnectionUrl(), parametrizedJdbcReportDataSource.getConnectionUrl());
    }

    @Test
    public void merge_multilineTest_passed() {
        String template = "before text: %1$s, after text\n" +
                "newline before text: %2$s, newline after text %2$s\r\n" +
                "newline1 before text: %3$s, newline3 after text %3$s\r" +
                "newline2: %1$s, after...";
        String parameterized = String.format(
                template,
                "{attribute('userName', 'User')}",
                "{attribute(''connectionUrl'', ''Tenant'')}",
                "{attribute('driverClass')}"
        );
        String expected = String.format(
                template,
                jdbcUserName,
                jdbcConnectionUrl,
                jdbcDriverClass
        );
        String result = profileAttributesResolverImpl.merge(parameterized, "template text");

        assertEquals(expected, result);
    }

    @Test
    public void merge_attributePlaceholderWithDoubleSingleQuotes_passed() {
        parametrizedJdbcReportDataSource.setPassword("{attribute(''password'')}");
        parametrizedJdbcReportDataSource.setUsername("{attribute(''userName'', ''User'')}");

        JdbcReportDataSourceImpl result = profileAttributesResolverImpl.mergeResource(parametrizedJdbcReportDataSource);

        assertEquals(result.getUsername(), jdbcUserName);
        assertEquals(result.getPassword(), jdbcUserPassword);
    }

    @Test
    public void mergeResource_skipIfResourceHasSkipResolvingAttribute_success() {
        parametrizedJdbcReportDataSource.setAttributes(Collections.singletonList(
                SKIP_PROFILE_ATTRIBUTES_RESOLVING
        ));

        JdbcReportDataSourceImpl result = profileAttributesResolverImpl.mergeResource(parametrizedJdbcReportDataSource);

        assertEquals(result, parametrizedJdbcReportDataSource);
        verifyNoInteractions(profileAttributeService);
    }

    @Test
    public void init_attributePlaceholderPatternsAreInvalid_exception() throws Exception {
        ProfileAttributesResolverImpl resolver = new ProfileAttributesResolverImpl();
        resolver.setAttributePlaceholderPatterns(invalidAttributePlaceholderPatterns);
        resolver.setParametrizedResourcePatterns(invalidAttributePlaceholderPatterns);
        resolver.setValidator(beanValidator);

        try {
            resolver.init();
            fail("Expect " + JSConstraintViolationException.class.getSimpleName() + " exception");
        } catch(JSConstraintViolationException e) {
            assertEquals(e.getConstraintViolations().size(), 2);

            for (ConstraintViolation violation : e.getConstraintViolations()) {
                assertEquals(violation.getMessageTemplate(), ATTRIBUTE_PATTERNS_INCLUDES_INVALID);
            }
        }
    }

    @Test
    public void init_parametrizedResourcePatternsAreInvalid_exception() throws Exception {
        ProfileAttributesResolverImpl resolver = new ProfileAttributesResolverImpl();
        resolver.setAttributePlaceholderPatterns(attributePlaceholderPatterns);
        resolver.setParametrizedResourcePatterns(invalidAttributePlaceholderPatterns);
        resolver.setValidator(beanValidator);
        try {
            resolver.init();
            fail("Expect " + IllegalArgumentException.class.getSimpleName() + " exception");
        } catch(JSConstraintViolationException e) {
            assertEquals(e.getConstraintViolations().size(), 1);
            ConstraintViolation violation = e.getConstraintViolations().iterator().next();
            assertEquals(violation.getMessageTemplate(), ATTRIBUTE_PATTERNS_INCLUDES_INVALID);
            assertEquals(ConstraintValidatorContextDecorator.getArguments(violation).get(0), invalidAttributePlaceholderPatterns.get(0));
            assertEquals(ConstraintValidatorContextDecorator.getArguments(violation).get(1),
                    Arrays.asList(attributeNameGroup, categoryGroup).toString());
        }
    }

    @Test
    public void isParametrizedResource_RestrictedCategoriesIsNotSpecified_passed() throws Exception {
        assertTrue(profileAttributesResolverImpl.isParametrizedResource(parametrizedJdbcReportDataSource));
        assertFalse(profileAttributesResolverImpl.isParametrizedResource(notParametrizedJdbcReportDataSource));
    }

    @Test
    public void isParametrizedResource_parametrizedOnlyWithServerAttributes_passed() throws Exception {
        JdbcReportDataSourceImpl parametrizedOnlyWithServerAttr = new JdbcReportDataSourceImpl();
        parametrizedOnlyWithServerAttr.setName("Some name");
        parametrizedOnlyWithServerAttr.setDescription("Some description");
        parametrizedOnlyWithServerAttr.setPassword("Some password");
        parametrizedOnlyWithServerAttr.setUsername("{attribute('userName', 'Server')}");
        parametrizedOnlyWithServerAttr.setConnectionUrl("{attribute('connectionUrl', 'Server')}");
        parametrizedOnlyWithServerAttr.setDriverClass("{attribute('driverClass', 'Server')}");

        assertTrue(profileAttributesResolverImpl.isParametrizedResource(parametrizedOnlyWithServerAttr,
                ProfileAttributeCategory.SERVER));
        assertFalse(profileAttributesResolverImpl.isParametrizedResource(parametrizedJdbcReportDataSource,
                ProfileAttributeCategory.SERVER));
        assertFalse(profileAttributesResolverImpl.isParametrizedResource(notParametrizedJdbcReportDataSource,
                ProfileAttributeCategory.SERVER));
    }

    @Test
    public void isParametrizedResource_AttributeParamAsFunctionWithServerCategory_passed() throws Exception {
        JdbcReportDataSourceImpl parametrizedOnlyWithServerAttr = new JdbcReportDataSourceImpl();
        parametrizedOnlyWithServerAttr.setName("Some name");
        parametrizedOnlyWithServerAttr.setDescription("Some description");
        parametrizedOnlyWithServerAttr.setPassword("Some password");
        parametrizedOnlyWithServerAttr.setUsername("attribute('userName', 'Server')");
        parametrizedOnlyWithServerAttr.setConnectionUrl("Some Connection url");
        parametrizedOnlyWithServerAttr.setDriverClass("Some driver class");

        assertTrue(profileAttributesResolverImpl.isParametrizedResource(parametrizedOnlyWithServerAttr,
                ProfileAttributeCategory.SERVER));
    }

    @Test
    public void isParametrizedResource_AttributeParamAsAttributePlaceholderWithServerCategory_passed() throws Exception {
        JdbcReportDataSourceImpl parametrizedOnlyWithServerAttr = new JdbcReportDataSourceImpl();
        parametrizedOnlyWithServerAttr.setName("Some name");
        parametrizedOnlyWithServerAttr.setDescription("Some description");
        parametrizedOnlyWithServerAttr.setPassword("Some password");
        parametrizedOnlyWithServerAttr.setUsername("attribute('userName', 'Server')");
        parametrizedOnlyWithServerAttr.setConnectionUrl("Some Connection url");
        parametrizedOnlyWithServerAttr.setDriverClass("Some driver class");

        assertTrue(profileAttributesResolverImpl.isParametrizedResource(parametrizedOnlyWithServerAttr,
                ProfileAttributeCategory.SERVER));
     }

    @Test
    public void isParametrizedResource_AttributeParamAsAttributePlaceholderDoubleSingleQuotesWithServerCategory_passed() throws Exception {
        JdbcReportDataSourceImpl parametrizedOnlyWithServerAttr = new JdbcReportDataSourceImpl();
        parametrizedOnlyWithServerAttr.setName("Some name");
        parametrizedOnlyWithServerAttr.setDescription("Some description");
        parametrizedOnlyWithServerAttr.setPassword("Some password");
        parametrizedOnlyWithServerAttr.setUsername("{attribute(''userName'', ''Server'')}");
        parametrizedOnlyWithServerAttr.setConnectionUrl("Some Connection url");
        parametrizedOnlyWithServerAttr.setDriverClass("Some driver class");

        assertTrue(profileAttributesResolverImpl.isParametrizedResource(parametrizedOnlyWithServerAttr,
                ProfileAttributeCategory.HIERARCHICAL));
    }

    @Test
    public void merge_attributeCategoryIsNotSupported_exception() throws Exception {
        parametrizedJdbcReportDataSource.setUsername("{attribute('userName', 'UnknownCategory')}");
        try {
            profileAttributesResolverImpl.mergeResource(parametrizedJdbcReportDataSource);
            fail("Expected " + JSProfileAttributeException.class.getSimpleName());
        } catch (JSProfileAttributeException ex) {
            assertEquals(ex.getErrorDescriptor(), PROFILE_ATTRIBUTE_SUBSTITUTION_CATEGORY_INVALID_IN_RESOURCE.
                    createDescriptor("/Some name", "username", "UnknownCategory", "userName", profileAttributeCategories.toString()));
        }
    }

    @Test
    public void merge_someCategoricalAttributeIsNotDefined_exception() throws Exception {
        parametrizedJdbcReportDataSource.setUsername("{attribute('unknownAttribute', 'User')}");
        try {
            profileAttributesResolverImpl.mergeResource(parametrizedJdbcReportDataSource);
            fail("Expected " + JSProfileAttributeException.class.getSimpleName());
        } catch (JSProfileAttributeException ex) {
            assertEquals(ex.getErrorDescriptor(), AttributeErrorCode.PROFILE_ATTRIBUTE_SUBSTITUTION_NOT_FOUND_IN_RESOURCE.
                    createDescriptor("/Some name", "username", "unknownAttribute", USER.getLabel()));
        }
    }

    @Test
    public void merge_someHierarchicalAttributeIsNotDefined_exception() throws JSException {
        parametrizedJdbcReportDataSource.setUsername("{attribute('unknownAttribute')}");
        try {
            profileAttributesResolverImpl.mergeResource(parametrizedJdbcReportDataSource);
            fail("Expected " + JSProfileAttributeException.class.getSimpleName());
        } catch (JSProfileAttributeException ex) {
            assertEquals(ex.getErrorDescriptor(), AttributeErrorCode.PROFILE_ATTRIBUTE_SUBSTITUTION_NOT_FOUND_IN_RESOURCE.
                    createDescriptor("/Some name", "username", "unknownAttribute", HIERARCHICAL.getLabel()));
        }
    }

    @Test
    public void merge_someCategoricalAttributeIsNotDefinedAndResourceNotHaveIdentifier_exception() throws Exception {
        parametrizedJdbcReportDataSource.setUsername("{attribute('unknownAttribute', 'User')}");
        parametrizedJdbcReportDataSource.setName(null);
        try {
            profileAttributesResolverImpl.mergeResource(parametrizedJdbcReportDataSource);
            fail("Expected " + JSProfileAttributeException.class.getSimpleName());
        } catch (JSProfileAttributeException ex) {
            assertEquals(ex.getErrorDescriptor(), AttributeErrorCode.PROFILE_ATTRIBUTE_SUBSTITUTION_NOT_FOUND.
                    createDescriptor("unknownAttribute", USER.getLabel()));
        }
    }

    @Test
    public void merge_someHierarchicalAttributeIsNotDefinedAndResourceNotHaveIdentifier_exception() throws JSException {
        parametrizedJdbcReportDataSource.setUsername("{attribute('unknownAttribute')}");
        parametrizedJdbcReportDataSource.setName(null);
        try {
            profileAttributesResolverImpl.mergeResource(parametrizedJdbcReportDataSource);
            fail("Expected " + JSProfileAttributeException.class.getSimpleName());
        } catch (JSProfileAttributeException ex) {
            assertEquals(ex.getErrorDescriptor(), AttributeErrorCode.PROFILE_ATTRIBUTE_SUBSTITUTION_NOT_FOUND.
                    createDescriptor("unknownAttribute", HIERARCHICAL.getLabel()));
        }
    }

    @Test
    public void quoteMatcherReplacement_success() {
        String quoteValue = profileAttributesResolverImpl.quoteMatcherReplacement("spec_@_#_$_%_^_&_*_(_)_-_=_+_:_;_._\"_\\_/_?_<_>_~_[_]_{_}_`_'_ !");
        assertEquals("spec_@_#_\\$_%_^_&_*_(_)_-_=_+_:_;_._\"_\\\\_/_?_<_>_~_[_]_{_}_`_'_ !", quoteValue);

    }

    @Test
    public void isSkipProfileAttributesResolving_returnsFalseIfAttributesAreNull_success() {
        AttributedObject object = createAttributedObjectMock(null);

        assertFalse(isSkipProfileAttributesResolving(object));
    }

    @Test
    public void isSkipProfileAttributesResolving_returnsFalseIfAttributesMissSkipFlag_success() {
        AttributedObject object = createAttributedObjectMock(Collections.emptyList());

        assertFalse(isSkipProfileAttributesResolving(object));
    }

    @Test
    public void isSkipProfileAttributesResolving_returnsTrueIfAttributesHasSkipFlag_success() {
        AttributedObject object = createAttributedObjectMock(Arrays.asList(
                SKIP_PROFILE_ATTRIBUTES_RESOLVING, "anotherAttribute"
        ));

        assertTrue(isSkipProfileAttributesResolving(object));
    }

    private AttributedObject createAttributedObjectMock(List<?> attributes) {
        AttributedObject object = mock(AttributedObject.class);
        doReturn(attributes).when(object).getAttributes();
        return object;
    }

    @Test
    public void getErrorFieldName_jsonFile_returnFieldName() {
        String fieldName = profileAttributesResolverImpl.getErrorFieldName(
                "{\n" +
                        "  \"name1\": \"value1\",\n" +
                        "  \"name2\": \"${attr1}\"\n" +
                        "}",
                "${attr1}");

        assertEquals(fieldName, "name2");
    }

    @Test
    public void getErrorFieldName_xmlFile_returnFieldName() {
        String fieldName = profileAttributesResolverImpl.getErrorFieldName(
                "\n" +
                        "<el1>\n" +
                        "    <el2 attr1 = \"123\">\n" +
                        "        <el3 attr2=\"1\" attr3 = \"12\">\n" +
                        "            0e 3e\n" +
                        "            7e 52 ${attr1} 0a 57\n" +
                        "        </el3>\n" +
                        "    </el2>\n" +
                        "</el1>",
                "${attr1}");

        assertEquals(fieldName, "el3");
    }

    @Test
    public void getErrorFieldName_improperInputString_returnEmptyString() {
        String fieldName = profileAttributesResolverImpl.getErrorFieldName("abracadabra tra-la-la", "${attr1}");

        assertEquals("", fieldName);
    }
}
