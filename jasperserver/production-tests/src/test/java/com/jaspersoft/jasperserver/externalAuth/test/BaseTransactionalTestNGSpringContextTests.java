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
package com.jaspersoft.jasperserver.externalAuth.test;

import com.jaspersoft.jasperserver.api.common.domain.impl.ExecutionContextImpl;
import com.jaspersoft.jasperserver.api.metadata.common.domain.Folder;
import com.jaspersoft.jasperserver.api.metadata.common.domain.client.FolderImpl;
import com.jaspersoft.jasperserver.api.metadata.common.service.impl.hibernate.HibernateRepositoryService;
import com.jaspersoft.jasperserver.api.metadata.user.domain.Role;
import com.jaspersoft.jasperserver.api.metadata.user.domain.client.TenantImpl;
import com.jaspersoft.jasperserver.api.metadata.user.service.TenantService;
import com.jaspersoft.jasperserver.api.metadata.user.service.UserAuthorityService;
import com.jaspersoft.jasperserver.common.test.MockServletContextLoader;
import com.jaspersoft.jasperserver.util.test.BaseServiceSetupTestNG;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTransactionalTestNGSpringContextTests;

import jakarta.annotation.Resource;
import javax.sql.DataSource;
import java.util.List;

import static org.testng.Assert.assertTrue;

/**
 * TODO do we need to load applicationContext-unit-test?
 * User: dlitvak
 * Date: 9/19/12
 */
@ContextConfiguration(loader = MockServletContextLoader.class, locations = {"classpath:applicationContext*.xml"})
public abstract class BaseTransactionalTestNGSpringContextTests extends AbstractTransactionalTestNGSpringContextTests {
	public static final String FORWARD_SLASH = "/";
	public static final String TEMP_DIR = "temp";
	public static final String TEMP_DIR_LAB = "Temp";
	public static final String ORGS_DIR = TenantService.ORGANIZATIONS;
	public static final String ORGS_URI = "/" + ORGS_DIR;
	public static final String ORGS_DIR_LAB = "Organizations";
	public static final String TEMPLATE_DIR = TenantService.ORG_TEMPLATE;
	public static final String TEMPLATE_URI = ORGS_URI + "/" + TEMPLATE_DIR;
	public static final String TEMPLATE_DIR_LAB = "Folder Template";
	public static final String DEFAULT_ORG_NAME = "organization_1";
	public static final String DEFAULT_ORG_URI = ORGS_URI + "/" + DEFAULT_ORG_NAME;
	public static final String DEFAULT_ORG_LAB = "Organization";

	public static final String PROPERTIES_DIR = "properties";  // should only be in /root dir
	public static final String ADHOC_DIR = "adhoc";
	public static final String ADHOC_DIR_LAB = "Ad Hoc Components";
	public static final String TOPICS_DIR = "topics";
	public static final String TOPICS_DIR_LAB = "Topics";
	public static final String THEMES_DIR = "themes";
	public static final String THEMES_DIR_LABEL = "Themes";
	public static final String DEFAULT_THEME_DIR = "default";
	public final String DEFAULT_THEME = "default";

	public static final String ROLE_ADMINISTRATOR = "ROLE_ADMINISTRATOR";
	public static final String ROLE_USER = "ROLE_USER";

	@Autowired
	protected DataSource dataSource;

	@Resource(name = "tenantService")
	protected TenantService tenantService;

	@Resource(name = "unsecureRepositoryService")
	protected HibernateRepositoryService repositoryService;

	@Resource(name = "userAuthorityService")
	protected UserAuthorityService userAuthorityService;

	/*
	 * create a tenant
	 */
	protected void createTenant(String parentTenantId, String tenantId, String tenantName,
								String tenantDesc, String tenantNote, String relativeUri, String uri, String theme) {

		TenantImpl aTenant = new TenantImpl();

		if (!(TenantService.ORGANIZATIONS.equals(tenantId)) && !(TenantService.ORGANIZATIONS.equals(parentTenantId))) {
			tenantId = parentTenantId + "_" + tenantId;
		}

		aTenant.setParentId(parentTenantId);
		aTenant.setId(tenantId);
		aTenant.setAlias(tenantId);
		aTenant.setTenantName(tenantName);
		aTenant.setTenantDesc(tenantDesc);
		aTenant.setTenantNote(tenantNote);
		aTenant.setTenantUri(relativeUri);   // this is not a true repository URI (describes org hierarchy)
		aTenant.setTenantFolderUri(uri);
		aTenant.setTheme(theme);

		tenantService.putTenant(null, aTenant);
	}

	/**
	 * constructMultiTenancyFolderStructure
	 *
	 * After standard unit tests run the main folders have
	 * a flat structure.
	 *
	 * For multi-tenancy we want a folder structure like so:
	 *
	 *    /organizations
	 *                  /org_template
	 *                  /organization_1
	 *                                 /organizations
	 *                                               /org_template
	 *
	 * For multi-tenancy we want most sample data content in:
	 *
	 *    /organizations/organization_1
	 */
	protected void constructMultiTenancyFolderStructure() {
		logger.info("constructMultiTenancyFolderStructure() called");

		// create /organizations dir
		FolderImpl orgsFolder = createFolder(ORGS_DIR, ORGS_DIR_LAB, ORGS_DIR_LAB, FORWARD_SLASH);

		// create /organizations/organization_1 dir
		createFolder(DEFAULT_ORG_NAME, DEFAULT_ORG_LAB, DEFAULT_ORG_LAB, orgsFolder);

		// create /organizations/org_template dir
		createFolder(TEMPLATE_DIR, TEMPLATE_DIR_LAB, TEMPLATE_DIR_LAB, orgsFolder);

		// create /organizations/org_template/temp dir
		createFolder(TEMP_DIR, TEMP_DIR_LAB, TEMP_DIR_LAB, TEMPLATE_URI);

		// create /organizations/org_template/adhoc dir
		createFolder(ADHOC_DIR, ADHOC_DIR_LAB, ADHOC_DIR_LAB, TEMPLATE_URI);

		// create /organizations/org_template/adhoc/topics dir
		createFolder(TOPICS_DIR, TOPICS_DIR_LAB, TOPICS_DIR_LAB, TEMPLATE_URI + FORWARD_SLASH + ADHOC_DIR);

		// create /organizations/org_template/themes dir
		createFolder(THEMES_DIR, THEMES_DIR_LABEL, THEMES_DIR, TEMPLATE_URI);

		// create /organizations/org_template/themes/default dir
		createFolder(DEFAULT_THEME_DIR, DEFAULT_THEME_DIR, DEFAULT_THEME_DIR, TEMPLATE_URI + FORWARD_SLASH + THEMES_DIR);

		// move root "/" folders down to "/organizations/organization_1"
		// skip "/organizations" and "/properties"
		List folders = repositoryService.getSubFolders(null, FORWARD_SLASH);
		for (Object folder : folders) {
			if (ORGS_DIR.equalsIgnoreCase(((Folder) folder).getName()) ||
					PROPERTIES_DIR.equalsIgnoreCase(((Folder) folder).getName())) {
				continue;
			}
			repositoryService.moveFolder(new ExecutionContextImpl(), ((Folder) folder).getURIString(), DEFAULT_ORG_URI);
		}
	}

	private FolderImpl createFolder(String name, String label, String description, String parentFolder) {
		FolderImpl folder = new FolderImpl();
		folder.setName(name);
		folder.setLabel(label);
		folder.setDescription(description);
		folder.setParentFolder(parentFolder);
		repositoryService.saveFolder(new ExecutionContextImpl(), folder);
		return folder;
	}

	private Folder createFolder(String name, String label, String description, FolderImpl parentFolder) {
		return createFolder(name, label, description, (parentFolder == null ? null : parentFolder.getURIString()));
	}

	/*
	 * create users and roles needed for the core ce data
	 */
	protected void createUsersAndRoles() {
		logger.info("createUsersAndRoles() called");

		// create ROLE_ADMINISTRATOR and ROLE_USER to jasperadmin
		createRole( BaseServiceSetupTestNG.ROLE_ADMINISTRATOR );
		createRole( BaseServiceSetupTestNG.ROLE_USER );
	}

	/*
		* create a role
		*
		* MOD: This method needs a tenantId parameter.
		*      - tenantId can be null (thus superuser level)
		*      - tenantId can be set to an existing tenant such as organization_1
		*/
	private Role createRole(String roleName) {
		Role role = userAuthorityService.getRole(null, roleName);

		// check for role being null (ie shouldn't already exist)
		assertTrue(role == null, "Error: trying to create a role that already exists, roleName="
				+ roleName);

		role = userAuthorityService.newRole(null);
		role.setRoleName(roleName);
		role.setExternallyDefined(false);
		userAuthorityService.putRole(null, role);

		return role;
	}
}

