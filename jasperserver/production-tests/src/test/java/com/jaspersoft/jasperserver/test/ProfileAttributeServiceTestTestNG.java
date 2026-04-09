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
package com.jaspersoft.jasperserver.test;

import com.jaspersoft.jasperserver.api.common.domain.ExecutionContext;
import com.jaspersoft.jasperserver.api.common.properties.PropertyChanger;
import com.jaspersoft.jasperserver.api.metadata.user.domain.ProfileAttribute;
import com.jaspersoft.jasperserver.api.metadata.user.domain.Tenant;
import com.jaspersoft.jasperserver.api.metadata.user.domain.User;
import com.jaspersoft.jasperserver.api.metadata.user.domain.client.ProfileAttributeImpl;
import com.jaspersoft.jasperserver.api.metadata.user.service.AttributesSearchCriteria;
import com.jaspersoft.jasperserver.api.metadata.user.service.AttributesSearchResult;
import com.jaspersoft.jasperserver.api.metadata.user.service.ProfileAttributeCategory;
import com.jaspersoft.jasperserver.api.metadata.user.service.ProfileAttributeGroup;
import com.jaspersoft.jasperserver.api.metadata.user.service.TenantService;
import com.jaspersoft.jasperserver.util.test.BaseServiceSetupTestNG;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.apache.commons.collections.CollectionUtils.find;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

/**
 * <p></p>
 *
 * @author sbirney
 * @author Vlad Zavadskii
 * @version $Id$
 */
public class ProfileAttributeServiceTestTestNG extends BaseServiceSetupTestNG {
    protected static final String USERNAME = "testuser";
    protected static final String CUSTOM_GROUP = "custom";
    protected static final String PROFILE_ATTRIBUTE_NAME = "testAttr";
    protected static final String PROFILE_ATTRIBUTE_VALUE = "testValue";
    protected static final String UPDATED_PROFILE_ATTRIBUTE_VALUE = "updatedTestValue";

    private static final Log log = LogFactory.getLog(ProfileAttributeServiceTestTestNG.class);
    private static final String CLASS_NAME = ProfileAttributeServiceTestTestNG.class.getName();

    protected User user;
    protected Tenant server;
    protected List<ProfileAttribute> allProfileAttributesWithoutServerSettings = new ArrayList<ProfileAttribute>();
    protected List<ProfileAttribute> allProfileAttributes = new ArrayList<ProfileAttribute>();
    protected List<ProfileAttribute> allProfileAttributesWithoutNoAccess = new ArrayList<ProfileAttribute>();
    protected List<ProfileAttribute> userProfileAttributes = new ArrayList<ProfileAttribute>();
    protected List<ProfileAttribute> serverProfileAttributesCustom = new ArrayList<ProfileAttribute>();
    protected List<ProfileAttribute> allServerProfileAttributes = new ArrayList<ProfileAttribute>();

    protected final ExecutionContext context = getExecutionContext();

    public User createUser() {
        return createUser(USERNAME, "", "");
    }

    @BeforeMethod
    public void handleTestMethodName(Method method) {
        log.debug(CLASS_NAME + " => " + method.getName() + "() called");
    }

    protected void deleteCustomProfileAttributesIfExist() {
        AttributesSearchResult<ProfileAttribute> searchResult = getProfileAttributeService()
                .getProfileAttributesForPrincipal(null, server,
                        new AttributesSearchCriteria.Builder()
                                .setGroups(Collections.singleton(ProfileAttributeGroup.CUSTOM.toString()))
                                .build());
        if (searchResult.getTotalCount() > 0) {
            for (ProfileAttribute attribute : searchResult.getList()) {
                getProfileAttributeService().deleteProfileAttribute(null, attribute);
            }
        }
    }

    @BeforeClass
    public void onSetup() {
        log.info("Creating test user");
        user = createUser();

        addRole(user, ROLE_ADMINISTRATOR);

        log.info("Creating user attributes");
        userProfileAttributes.add(createTestAttr(user, "State", "CA", CUSTOM_GROUP));
        userProfileAttributes.add(createTestAttr(user, "Cities",
                "San Francisco, Oakland, San Jose, Los Angeles, Sacramento, Fresno", CUSTOM_GROUP));

        List<ProfileAttribute> tenantAttributes = createTenantsAttributes();

        log.info("Creating server attributes");

        createTenantForRootIfMissing();

        server = getTenantService().getTenant(context, TenantService.ORGANIZATIONS);
        deleteCustomProfileAttributesIfExist();
        serverProfileAttributesCustom.add(createTestAttr(server, "customGroup:customProperty", "[SYSTEM]", CUSTOM_GROUP));

        allProfileAttributesWithoutServerSettings.addAll(userProfileAttributes);
        allProfileAttributesWithoutServerSettings.addAll(tenantAttributes);
        allProfileAttributesWithoutServerSettings.addAll(serverProfileAttributesCustom);

        for (ProfileAttribute profileAttribute : allProfileAttributesWithoutServerSettings) {
            getProfileAttributeService().putProfileAttribute(context, profileAttribute);
        }

        List<ProfileAttribute> serverSettings = new LinkedList<ProfileAttribute>();
        Map<String, PropertyChanger> changerMap = getChangerObjects();
        for (Map.Entry<String, PropertyChanger> entry : changerMap.entrySet()) {
            for (Map.Entry<String, String> property : entry.getValue().getProperties().entrySet()) {
                ProfileAttribute profileAttribute = createTestAttr(server, property.getKey(), property.getValue(),
                        entry.getKey());
                serverSettings.add(profileAttribute);
            }
        }

        allServerProfileAttributes.addAll(serverProfileAttributesCustom);
        allServerProfileAttributes.addAll(serverSettings);

        allProfileAttributes.addAll(serverSettings);
        allProfileAttributes.addAll(allProfileAttributesWithoutServerSettings);

        allProfileAttributesWithoutNoAccess.addAll(userProfileAttributes);
        allProfileAttributesWithoutNoAccess.addAll(serverProfileAttributesCustom);
        allProfileAttributesWithoutNoAccess.addAll(serverSettings);
    }

    @Test
    public void doGetProfileAttributesForPrincipalTest() {
        setAuthenticatedUser(BaseServiceSetupTestNG.USER_JASPERADMIN);
        AttributesSearchCriteria searchCriteria = new AttributesSearchCriteria.Builder().build();
        AttributesSearchResult<ProfileAttribute> profileAttributes = getProfileAttributeService()
                .getProfileAttributesForPrincipal(context, user, searchCriteria);
        assertTrue("Found user profile attributes", CollectionUtils.isEqualCollection(userProfileAttributes,
                profileAttributes.getList()));

        searchCriteria = new AttributesSearchCriteria.Builder().setEffective(true).setSkipServerSettings(true).build();
        profileAttributes = getProfileAttributeService().getProfileAttributesForPrincipal(context, user, searchCriteria);
        assertTrue("Found user profile attributes hierarchically without server settings",
                CollectionUtils.isEqualCollection(allProfileAttributesWithoutServerSettings, profileAttributes.getList()));

        searchCriteria = new AttributesSearchCriteria.Builder().setEffective(true).build();
        profileAttributes = getProfileAttributeService().getProfileAttributesForPrincipal(context, user, searchCriteria);
        assertTrue("Found user profile attributes hierarchically including server settings",
                CollectionUtils.isEqualCollection(allProfileAttributes, profileAttributes.getList()));

        searchCriteria = new AttributesSearchCriteria.Builder().setMaxRecords(1).build();
        profileAttributes = getProfileAttributeService().getProfileAttributesForPrincipal(context, user, searchCriteria);
        assertTrue("Found one user profile attribute", profileAttributes.getList().size() == 1 &&
                allProfileAttributes.containsAll(profileAttributes.getList()));

        searchCriteria = new AttributesSearchCriteria.Builder().setStartIndex(1).build();
        profileAttributes = getProfileAttributeService().getProfileAttributesForPrincipal(context, user, searchCriteria);
        assertTrue("Found one user profile attribute starting from index 1", profileAttributes.getList().size() == 1 &&
                userProfileAttributes.containsAll(profileAttributes.getList()));

        searchCriteria = new AttributesSearchCriteria.Builder().setRecursive(true).build();
        profileAttributes = getProfileAttributeService().getProfileAttributesForPrincipal(context, server, searchCriteria);
        assertTrue("Found server profile attributes recursively (including all child attributes)",
                profileAttributes.getList().containsAll(allProfileAttributes));

        Set<String> groups = new HashSet<String>();
        groups.add(CUSTOM_GROUP);
        searchCriteria = new AttributesSearchCriteria.Builder().setEffective(true).setGroups(groups).build();
        profileAttributes = getProfileAttributeService().getProfileAttributesForPrincipal(context, user, searchCriteria);
        for (ProfileAttribute profileAttribute : profileAttributes.getList()) {
            assertTrue("Found all profile attributes with group \"customGroup\"",
                    profileAttribute.getGroup().equals(CUSTOM_GROUP));
        }

        Set<String> names = new HashSet<String>();
        String attrName = userProfileAttributes.get(0).getAttrName();
        names.add(attrName);
        searchCriteria = new AttributesSearchCriteria.Builder().setNames(names).build();
        profileAttributes = getProfileAttributeService().getProfileAttributesForPrincipal(context, user, searchCriteria);
        assertTrue("Found one profile attribute by specific name", profileAttributes.getList().size() == 1 &&
                profileAttributes.getList().get(0).getAttrName().equals(attrName));
    }

    @Test
    public void doPutAndDeleteProfileAttributeTest() {
        setAuthenticatedUser(BaseServiceSetupTestNG.USER_JASPERADMIN);
        ProfileAttribute testProfileAttribute =
                createTestAttr(user, PROFILE_ATTRIBUTE_NAME, PROFILE_ATTRIBUTE_VALUE, CUSTOM_GROUP);
        getProfileAttributeService().putProfileAttribute(context, testProfileAttribute);

        AttributesSearchCriteria searchCriteria = new AttributesSearchCriteria.Builder()
                .setNames(Collections.<String>singleton(PROFILE_ATTRIBUTE_NAME))
                .build();

        AttributesSearchResult<ProfileAttribute> profileAttributes =
                getProfileAttributeService().getProfileAttributesForPrincipal(context, user, searchCriteria);
        ProfileAttribute profileAttributeFromDB = profileAttributes.getList().get(0);
        assertEquals("Put a new profile attribute", testProfileAttribute, profileAttributeFromDB);

        testProfileAttribute.setAttrValue(UPDATED_PROFILE_ATTRIBUTE_VALUE);
        getProfileAttributeService().putProfileAttribute(context, testProfileAttribute);
        profileAttributes = getProfileAttributeService().getProfileAttributesForPrincipal(context, user, searchCriteria);
        profileAttributeFromDB = profileAttributes.getList().get(0);
        assertEquals("Update existing profile attribute",
                UPDATED_PROFILE_ATTRIBUTE_VALUE, profileAttributeFromDB.getAttrValue());

        getProfileAttributeService().deleteProfileAttribute(context, testProfileAttribute);

        profileAttributes = getProfileAttributeService().getProfileAttributesForPrincipal(context, user, searchCriteria);
        assertTrue("Delete a new profile attribute", profileAttributes.getList().isEmpty());
    }

    @Test
    public void doDeleteProfileAttributesTest() {
        getCurrentUserProfileAttributesAuthenticate();

        ProfileAttribute testProfileAttribute =
                createTestAttr(user, PROFILE_ATTRIBUTE_NAME, PROFILE_ATTRIBUTE_VALUE, CUSTOM_GROUP);

        getProfileAttributeService().putProfileAttribute(context, testProfileAttribute);

        AttributesSearchCriteria searchCriteria = new AttributesSearchCriteria.Builder()
                .setNames(Collections.<String>singleton(PROFILE_ATTRIBUTE_NAME))
                .build();

        getProfileAttributeService().deleteProfileAttributes(Collections.<ProfileAttribute>emptyList());

        AttributesSearchResult<ProfileAttribute> profileAttributes =
                getProfileAttributeService().getProfileAttributesForPrincipal(context, user, searchCriteria);
        assertTrue("Don't delete anythings with empty list", profileAttributes.getList().size() == 1);

        getProfileAttributeService().deleteProfileAttributes(Collections.singleton(testProfileAttribute));

        profileAttributes = getProfileAttributeService().getProfileAttributesForPrincipal(context, user, searchCriteria);
        assertTrue("Delete all profile attributes", profileAttributes.getList().isEmpty());
    }

    @Test
    public void doGetProfileAttributeTest() {
        // Get user profile attribute
        ProfileAttribute expectedProfileAttribute = userProfileAttributes.get(0);
        ProfileAttribute profileAttribute = new ProfileAttributeImpl();
        profileAttribute.setAttrName(expectedProfileAttribute.getAttrName());
        profileAttribute.setPrincipal(expectedProfileAttribute.getPrincipal());

        ProfileAttribute actualProfileAttribute = getProfileAttributeService()
                .getProfileAttribute(context, profileAttribute);

        assertEquals("Found 1 user profile attribute", actualProfileAttribute, expectedProfileAttribute);

        // Get server profile attribute
        expectedProfileAttribute = serverProfileAttributesCustom.get(0);
        profileAttribute.setAttrName(expectedProfileAttribute.getAttrName());
        profileAttribute.setPrincipal(expectedProfileAttribute.getPrincipal());

        actualProfileAttribute = getProfileAttributeService().getProfileAttribute(context, expectedProfileAttribute);

        assertEquals("Found 1 server profile attribute", actualProfileAttribute, expectedProfileAttribute);
    }

    @Test
    public void doGetCurrentUserProfileAttributesTest() {
        getCurrentUserProfileAttributesAuthenticate();

        List<ProfileAttribute> profileAttributes = getProfileAttributeService()
                .getCurrentUserProfileAttributes(context, ProfileAttributeCategory.USER);
        assertTrue("Found user profile attributes", CollectionUtils.isEqualCollection(userProfileAttributes,
                profileAttributes));

        profileAttributes = getProfileAttributeService()
                .getCurrentUserProfileAttributes(context, ProfileAttributeCategory.SERVER);
        assertTrue("Found server profile attributes", CollectionUtils.isEqualCollection(allServerProfileAttributes,
                profileAttributes));

        profileAttributes = getProfileAttributeService()
                .getCurrentUserProfileAttributes(context, ProfileAttributeCategory.HIERARCHICAL);
        assertTrue("Found all profile attributes", CollectionUtils.isEqualCollection(allProfileAttributesWithoutNoAccess,
                profileAttributes));

        // Authenticate Anonymous user
        getCurrentUserProfileAttributesAuthenticateAnonymous();

        profileAttributes = getProfileAttributeService()
                .getCurrentUserProfileAttributes(context, ProfileAttributeCategory.USER);
        assertTrue("Found 0 user profile attributes for Anonymous user", profileAttributes.isEmpty());

        profileAttributes = getProfileAttributeService()
                .getCurrentUserProfileAttributes(context, ProfileAttributeCategory.SERVER);
        assertTrue("Found 0 server profile attributes for Anonymous user", profileAttributes.isEmpty());

        profileAttributes = getProfileAttributeService()
                .getCurrentUserProfileAttributes(context, ProfileAttributeCategory.HIERARCHICAL);
        assertTrue("Found 0 hierarchical profile attributes for Anonymous user", profileAttributes.isEmpty());

        // Authenticate previous user
        getCurrentUserProfileAttributesAuthenticate();
    }

    @Test
    public void doSetCurrentUserPreferenceValueTest() throws Exception {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            setAuthenticatedUser(USER_JASPERADMIN);
        }
        User authenticatedUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        final int size = authenticatedUser.getAttributes() == null ? 0 : authenticatedUser.getAttributes().size();

        String attrName = UUID.randomUUID().toString();
        String attrValue = "first-value";
        getProfileAttributeService().setCurrentUserPreferenceValue(attrName, attrValue);

        assertListContainsAttribute(authenticatedUser.getAttributes(), attrName, attrValue);
        assertEquals(size + 1, authenticatedUser.getAttributes().size());

        List list = getProfileAttributeService().getProfileAttributesForPrincipal(null, authenticatedUser);
        assertListContainsAttribute(list, attrName, attrValue);
        assertEquals(size + 1, list.size());

        attrValue = "other-value";
        getProfileAttributeService().setCurrentUserPreferenceValue(attrName, attrValue);

        assertListContainsAttribute(authenticatedUser.getAttributes(), attrName, attrValue);
        assertEquals(size + 1, authenticatedUser.getAttributes().size());

        list = getProfileAttributeService().getProfileAttributesForPrincipal(null, authenticatedUser);
        assertListContainsAttribute(list, attrName, attrValue);
        assertEquals(size + 1, list.size());
    }

    @AfterClass
    public void onTearDown() {
        log.info("Deleting all attributes");
        setAuthenticatedUser(BaseServiceSetupTestNG.USER_JASPERADMIN);
        for (ProfileAttribute profileAttribute : allProfileAttributesWithoutServerSettings) {
            getProfileAttributeService().deleteProfileAttribute(context, profileAttribute);
        }

        log.info("Deleting test user");
        deleteUser(user.getUsername());
    }

    protected void getCurrentUserProfileAttributesAuthenticate() {
        setAuthenticatedUser(user.getUsername());
    }

    protected void getCurrentUserProfileAttributesAuthenticateAnonymous() {
        Authentication aUser = new TestingAuthenticationToken(USER_ANONYMOUS, null, new ArrayList<GrantedAuthority>());
        aUser.setAuthenticated(true);
        SecurityContextHolder.getContext().setAuthentication(aUser);
    }

    protected List<ProfileAttribute> createTenantsAttributes() {
        return new ArrayList<ProfileAttribute>();
    }

    private void assertListContainsAttribute(List list, final String attrName, final String attrValue) {
        assertNotNull(find(list, new Predicate() {
            public boolean evaluate(Object object) {
                return object instanceof ProfileAttribute
                        && attrName.equals(((ProfileAttribute) object).getAttrName())
                        && attrValue.equals(((ProfileAttribute) object).getAttrValue());
            }
        }));
    }
}
