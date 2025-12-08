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

package com.jaspersoft.jasperserver.api.engine.jasperreports.util;

import com.jaspersoft.jasperserver.api.common.domain.ExecutionContext;
import com.jaspersoft.jasperserver.api.metadata.user.domain.ProfileAttribute;
import com.jaspersoft.jasperserver.api.metadata.user.domain.Role;
import com.jaspersoft.jasperserver.api.metadata.user.domain.User;
import com.jaspersoft.jasperserver.api.metadata.user.domain.client.ProfileAttributeImpl;
import com.jaspersoft.jasperserver.api.metadata.user.domain.client.RoleImpl;
import com.jaspersoft.jasperserver.api.metadata.user.domain.client.UserImpl;
import com.jaspersoft.jasperserver.api.metadata.user.domain.impl.client.MetadataUserDetails;
import com.jaspersoft.jasperserver.api.metadata.user.service.ProfileAttributeCategory;
import com.jaspersoft.jasperserver.api.metadata.user.service.ProfileAttributeService;
import net.sf.jasperreports.engine.JRParameter;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * <p></p>
 *
 * @author Vlad Zavadskii
 * @version $Id$
 */
public class UserProfileBuiltInParameterProviderTest {
    @InjectMocks
    private UserProfileBuiltInParameterProvider userProfileBuiltInParameterProvider = new UserProfileBuiltInParameterProvider() {
        @Override
        protected MetadataUserDetails getUserDetails() {
            if (user != null) {
                return new MetadataUserDetails(user);
            }

            return null;
        }
    };
    @Mock
    private ProfileAttributeService profileAttributeService;

    private User user;
    private List<ProfileAttribute> userProfileAttributes;
    private List<ProfileAttribute> allProfileAttributes;

    @BeforeClass
    public void init() {
        MockitoAnnotations.initMocks(this);
    }

    @BeforeMethod
    public void refresh() {
        reset(profileAttributeService);

        userProfileAttributes = new ArrayList<ProfileAttribute>();
        List<ProfileAttribute> tenantProfileAttributes = new ArrayList<ProfileAttribute>();
        List<ProfileAttribute> hierarchicalProfileAttributes = new ArrayList<ProfileAttribute>();
        List<ProfileAttribute> serverProfileAttributes = new ArrayList<ProfileAttribute>();
        allProfileAttributes = new ArrayList<ProfileAttribute>();

        ProfileAttribute profileAttribute = new ProfileAttributeImpl();
        profileAttribute.setAttrName("userAttr");
        profileAttribute.setAttrValue("userValue");
        userProfileAttributes.add(profileAttribute);

        profileAttribute = new ProfileAttributeImpl();
        profileAttribute.setAttrName("tenantAttr");
        profileAttribute.setAttrValue("tenantValue");
        tenantProfileAttributes.add(profileAttribute);

        profileAttribute = new ProfileAttributeImpl();
        profileAttribute.setAttrName("hierarchicalAttr");
        profileAttribute.setAttrValue("hierarchicalValue");
        hierarchicalProfileAttributes.add(profileAttribute);

        profileAttribute = new ProfileAttributeImpl();
        profileAttribute.setAttrName("serverAttr");
        profileAttribute.setAttrValue("serverValue");
        serverProfileAttributes.add(profileAttribute);

        allProfileAttributes.addAll(userProfileAttributes);
        allProfileAttributes.addAll(tenantProfileAttributes);
        allProfileAttributes.addAll(hierarchicalProfileAttributes);
        allProfileAttributes.addAll(serverProfileAttributes);

        when(profileAttributeService.getCurrentUserProfileAttributes(any(ExecutionContext.class), eq(ProfileAttributeCategory.USER)))
                .thenReturn(userProfileAttributes);
        when(profileAttributeService.getCurrentUserProfileAttributes(any(ExecutionContext.class), eq(ProfileAttributeCategory.TENANT)))
                .thenReturn(tenantProfileAttributes);
        when(profileAttributeService.getCurrentUserProfileAttributes(any(ExecutionContext.class), eq(ProfileAttributeCategory.SERVER)))
                .thenReturn(serverProfileAttributes);
        when(profileAttributeService.getCurrentUserProfileAttributes(any(ExecutionContext.class), eq(ProfileAttributeCategory.HIERARCHICAL)))
                .thenReturn(allProfileAttributes);

        List<ProfileAttributeCategory> allowedCategories = new ArrayList<ProfileAttributeCategory>();
        allowedCategories.add(ProfileAttributeCategory.USER);
        allowedCategories.add(ProfileAttributeCategory.TENANT);
        allowedCategories.add(ProfileAttributeCategory.SERVER);
        userProfileBuiltInParameterProvider.setProfileAttributeCategories(allowedCategories);

        user = new UserImpl();
    }

    @Test
    public void makeParameter_Any_passed() {
        String name = "name";
        String value = "value";
        Class<String> type = String.class;
        Object[] parameter = userProfileBuiltInParameterProvider.makeParameter(name, type, value);
        JRParameter jrParameter = (JRParameter) parameter[0];

        assertEquals(parameter.length, 2);
        assertEquals(jrParameter.getName(), name);
        assertEquals(jrParameter.getValueClass(), type);
        assertEquals(parameter[1], value);
    }

    @Test
    public void getProfileAttributes_hasUserCategory_passed() {
        Collection<ProfileAttribute> profileAttributes = userProfileBuiltInParameterProvider
                .getProfileAttributes(ProfileAttributeCategory.USER);

        assertEquals(profileAttributes, userProfileAttributes);
    }

    @Test
    public void getProfileAttributes_hasHierarchicalCategory_passed() {
        Collection<ProfileAttribute> profileAttributes = userProfileBuiltInParameterProvider
                .getProfileAttributes(ProfileAttributeCategory.HIERARCHICAL);

        assertEquals(profileAttributes, allProfileAttributes);
    }

    @Test
    public void getProfileAttributes_missedUserCategory_passed() {
        userProfileBuiltInParameterProvider.setProfileAttributeCategories(new ArrayList<ProfileAttributeCategory>());
        Collection<ProfileAttribute> profileAttributes = userProfileBuiltInParameterProvider
                .getProfileAttributes(ProfileAttributeCategory.USER);

        assertTrue(profileAttributes.isEmpty());
    }

    @Test
    public void getParameter_LoggedInUser_passed() {
        user.setUsername("username");
        Object result = userProfileBuiltInParameterProvider.getParameter(null, null, null,
                "LoggedInUser")[1];
        assertTrue(result instanceof MetadataUserDetails);
        MetadataUserDetails loggedInUser = (MetadataUserDetails) result;
        assertEquals(user.getUsername(), loggedInUser.getUsername());
    }

    @Test
    public void getParameter_LoggedInUsername_passed() {
        String username = "username";
        user.setUsername(username);
        String value = (String) userProfileBuiltInParameterProvider.getParameter(null, null, null,
                "LoggedInUsername")[1];

        assertEquals(value, username);
    }

    @Test
    public void getParameter_LoggedInUserFullname_passed() {
        String fullName = "fullname";
        user.setFullName(fullName);
        String value = (String) userProfileBuiltInParameterProvider.getParameter(null, null, null,
                "LoggedInUserFullname")[1];

        assertEquals(value, fullName);
    }

    @Test
    public void getParameter_LoggedInUserEmailAddress_passed() {
        String emailAddress = "test@gmail.com";
        user.setEmailAddress(emailAddress);
        String value = (String) userProfileBuiltInParameterProvider.getParameter(null, null, null,
                "LoggedInUserEmailAddress")[1];

        assertEquals(value, emailAddress);
    }

    @Test
    public void getParameter_LoggedInUserEnabled_passed() {
        user.setEnabled(true);
        Boolean value = (Boolean) userProfileBuiltInParameterProvider.getParameter(null, null, null,
                "LoggedInUserEnabled")[1];

        assertEquals(value, Boolean.valueOf(true));
    }

    @Test
    public void getParameter_LoggedInUserTenantId_passed() {
        String tenantId = "organization_1";
        user.setTenantId(tenantId);
        String value = (String) userProfileBuiltInParameterProvider.getParameter(null, null, null,
                "LoggedInUserTenantId")[1];

        assertEquals(value, tenantId);
    }

    @Test
    public void getParameter_LoggedInUserRoles_passed() {
        Set<Role> roles = new HashSet<Role>();
        Role role = new RoleImpl();
        role.setRoleName("testrole");
        roles.add(role);
        user.setRoles(roles);
        Collection value = (Collection) userProfileBuiltInParameterProvider.getParameter(null, null, null,
                "LoggedInUserRoles")[1];

        assertTrue(value.contains(role.getRoleName()));
    }

    @Test
    public void getParameter_LoggedInUserAttributes_passed() {
        @SuppressWarnings("unchecked")
        Collection<ProfileAttribute> value = (Collection<ProfileAttribute>) userProfileBuiltInParameterProvider
                .getParameter(null, null, null, "LoggedInUserAttributes")[1];

        assertEquals(value, userProfileAttributes);
    }

    @Test
    public void getParameter_LoggedInUserAttributeNames_passed() {
        @SuppressWarnings("unchecked")
        Collection<ProfileAttribute> value = (Collection<ProfileAttribute>) userProfileBuiltInParameterProvider
                .getParameter(null, null, null, "LoggedInUserAttributeNames")[1];
        List<String> attributeNames = new ArrayList<String>();
        for (ProfileAttribute profileAttribute : userProfileAttributes) {
            attributeNames.add(profileAttribute.getAttrName());
        }

        assertEquals(value, attributeNames);
    }

    @Test
    public void getParameter_LoggedInUserAttributeValues_passed() {
        @SuppressWarnings("unchecked")
        Collection<ProfileAttribute> value = (Collection<ProfileAttribute>) userProfileBuiltInParameterProvider
                .getParameter(null, null, null, "LoggedInUserAttributeValues")[1];
        List<String> attributeValues = new ArrayList<String>();
        for (ProfileAttribute profileAttribute : userProfileAttributes) {
            attributeValues.add(profileAttribute.getAttrValue());
        }

        assertEquals(value, attributeValues);
    }

    @Test
    public void getParameter_ExistentLoggedInUserAttribute_passed() {
        String value = (String) userProfileBuiltInParameterProvider.getParameter(null, null, null,
                "LoggedInUserAttribute_userAttr")[1];
        assertEquals(value, "userValue");
    }

    @Test
    public void getParameter_nonExistentLoggedInUserAttribute_passed() {
        Object[] parameter = userProfileBuiltInParameterProvider.getParameter(null, null, null,
                "LoggedInUserAttribute_nonExistentAttr");
        assertNull(parameter);
    }

    @Test
    public void getParameter_ExistentLoggedInTenantAttribute_passed() {
        String value = (String) userProfileBuiltInParameterProvider.getParameter(null, null, null,
                "LoggedInTenantAttribute_tenantAttr")[1];
        assertEquals(value, "tenantValue");
    }

    @Test
    public void getParameter_nonExistentLoggedInTenantAttribute_passed() {
        Object[] parameter = userProfileBuiltInParameterProvider.getParameter(null, null, null,
                "LoggedInTenantAttribute_nonExistentAttr");
        assertNull(parameter);
    }

    @Test
    public void getParameter_ExistentServerAttribute_passed() {
        String value = (String) userProfileBuiltInParameterProvider.getParameter(null, null, null,
                "ServerAttribute_serverAttr")[1];
        assertEquals(value, "serverValue");
    }

    @Test
    public void getParameter_nonExistentServerAttribute_passed() {
        Object[] parameter = userProfileBuiltInParameterProvider.getParameter(null, null, null,
                "ServerAttribute_nonExistentAttr");
        assertNull(parameter);
    }

    @Test
    public void getParameter_ExistentAttribute_passed() {
        String value = (String) userProfileBuiltInParameterProvider.getParameter(null, null, null,
                "Attribute_hierarchicalAttr")[1];
        assertEquals(value, "hierarchicalValue");
    }

    @Test
    public void getParameter_nonExistentAttribute_passed() {
        Object[] parameter = userProfileBuiltInParameterProvider.getParameter(null, null, null,
                "Attribute_nonExistentAttr");
        assertNull(parameter);
    }

    @Test
    public void getParameters_StandardParameters_passed() {
        user.setUsername("username");
        List<Object[]> parameters = userProfileBuiltInParameterProvider.getParameters(null, null, null);

        // The provider returns a MetadataUserDetails object, not the raw UserImpl
        assertTrue(parameters.get(0)[1] instanceof MetadataUserDetails);
        MetadataUserDetails details = (MetadataUserDetails) parameters.get(0)[1];
        assertEquals(user.getUsername(), details.getUsername());

        // Username comparison is still valid
        assertEquals(user.getUsername(), parameters.get(1)[1]);
    }

    @Test
    public void getParameters_preDefinedAttributesAreNotNull_passed() {
        reset(profileAttributeService);
        final String[] stringAttributes = {
                "LoggedInUsername",
                "LoggedInUserFullname",
                "LoggedInUserEmailAddress",
                "LoggedInUserTenantId"
        };
        final String[] booleanAttributes = {
                "LoggedInUserEnabled",
                "LoggedInUserExternallyDefined"
        };
        final String[] collectionAttributes = {
                "LoggedInUserRoles",
                "LoggedInUserAttributes",
                "LoggedInUserAttributeNames",
                "LoggedInUserAttributeValues"
        };

        for (String attribute : stringAttributes) {
            Object value = userProfileBuiltInParameterProvider.getParameter(null, null, null, attribute)[1];
            assertEquals(value, "");
        }

        for (String attribute : booleanAttributes) {
            Boolean value = (Boolean) userProfileBuiltInParameterProvider.getParameter(null, null, null, attribute)[1];
            assertFalse(value);
        }

        Collection<Object> emptyCollection = new ArrayList<Object>();
        for (String attribute : collectionAttributes) {
            Object value = userProfileBuiltInParameterProvider.getParameter(null, null, null, attribute)[1];
            assertEquals(value, emptyCollection);
        }
    }

    @Test
    public void getParameters_profileAttributesAreNull_passed() {
        final String[] attributes = {
                "LoggedInUserAttribute_",
                "LoggedInTenantAttribute_",
                "ServerAttribute_",
                "Attribute_"
        };

        for (String attribute : attributes) {
            Object value = userProfileBuiltInParameterProvider.getParameter(null, null, null, attribute + "attrName");
            assertNull(value);
        }
    }

    @Test
    public void getParameters_allAttributesExceptProfileOneAreCaseInsensitive_passed() {
        final String[] attributes = {
                "loggedinuser",
                "loggedinusername",
                "loggedinuserfullname",
                "loggedinuseremailaddress",
                "loggedinuserenabled",
                "loggedinuserexternallydefined",
                "loggedinusertenantid",
                "loggedinuserroles",
                "loggedinuserattributes",
                "loggedinuserattributenames",
                "loggedinuserattributevalues"
        };

        for (String attribute : attributes) {
            Object value = userProfileBuiltInParameterProvider.getParameter(null, null, null, attribute.toLowerCase());
            assertNotNull(value);
        }
    }

    @Test
    public void getParameters_profileAttributesNamesAreCaseSensitive_passed() {
        final String[] attributesWithRigthCase = {
                "loggedinuserattribute_userAttr",
                "loggedintenantattribute_tenantAttr",
                "serverattribute_serverAttr",
                "attribute_hierarchicalAttr"
        };

        final String[] attributesWithLowerCase = {
                "loggedinuserattribute_userattr",
                "loggedintenantattribute_tenantattr",
                "serverattribute_serverattr",
                "attribute_hierarchicalattr"
        };

        for (String attribute : attributesWithRigthCase) {
            Object value = userProfileBuiltInParameterProvider.getParameter(null, null, null, attribute);
            assertNotNull(value);
        }

        for (String attribute : attributesWithLowerCase) {
            Object value = userProfileBuiltInParameterProvider.getParameter(null, null, null, attribute);
            assertNull(value);
        }
    }

    @Test
    public void getParameters_withAnonymousUser_passed() {
        reset(profileAttributeService);
        user = null;

        final String[] stringAttributes = {
                "LoggedInUsername",
                "LoggedInUserFullname",
                "LoggedInUserEmailAddress",
                "LoggedInUserTenantId"
        };
        final String[] booleanAttributes = {
                "LoggedInUserEnabled",
                "LoggedInUserExternallyDefined"
        };
        final String[] collectionAttributes = {
                "LoggedInUserRoles",
                "LoggedInUserAttributes",
                "LoggedInUserAttributeNames",
                "LoggedInUserAttributeValues"
        };

        assertNull(userProfileBuiltInParameterProvider.getUserDetails());

        for (String attribute : stringAttributes) {
            Object value = userProfileBuiltInParameterProvider.getParameter(null, null, null, attribute)[1];
            assertEquals(value, "");
        }

        for (String attribute : booleanAttributes) {
            Boolean value = (Boolean) userProfileBuiltInParameterProvider.getParameter(null, null, null, attribute)[1];
            assertFalse(value);
        }

        Collection<Object> emptyCollection = new ArrayList<Object>();
        for (String attribute : collectionAttributes) {
            Object value = userProfileBuiltInParameterProvider.getParameter(null, null, null, attribute)[1];
            assertEquals(value, emptyCollection);
        }
    }
}
