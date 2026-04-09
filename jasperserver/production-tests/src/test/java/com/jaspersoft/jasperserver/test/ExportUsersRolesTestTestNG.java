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

/**
 * @author tkavanagh
 * @version $id $
 */
import com.jaspersoft.jasperserver.api.common.domain.ExecutionContext;
import com.jaspersoft.jasperserver.api.metadata.common.domain.FileResource;
import com.jaspersoft.jasperserver.api.metadata.common.domain.Folder;
import com.jaspersoft.jasperserver.api.metadata.common.domain.client.FolderImpl;
import com.jaspersoft.jasperserver.api.metadata.common.service.RepositoryService;
import com.jaspersoft.jasperserver.api.metadata.user.domain.Role;
import com.jaspersoft.jasperserver.api.metadata.user.domain.User;
import com.jaspersoft.jasperserver.api.metadata.user.service.UserAuthorityService;
import com.jaspersoft.jasperserver.export.Parameters;

import com.jaspersoft.jasperserver.test.BaseExportTestCaseTestNG;
import com.jaspersoft.jasperserver.util.test.BaseServiceSetupTestNG;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;
import static org.testng.AssertJUnit.*;

public class ExportUsersRolesTestTestNG extends BaseExportTestCaseTestNG {

    private static final Log m_logger = LogFactory.getLog(ExportUsersRolesTestTestNG.class);

	static final String PATH_SEP = "/";
	static final String LABEL = "_label";
	static final String DESC = "_description";

	static final String USER1 = "ExportTestUser01";
	static final String USER2 = "ExportTestUser02";
	static final String USER3 = "ExportTestUser03";
	static final String ROLE1 = "ROLE_USER";
	static final String ROLE2 = "ROLE_ADMINISTRATOR";
	static final String ROLE3 = "ROLE_EXPORT_TEST";

	static final String[] USERNAMES = {"joeuser", "admin"};
	static final String[] ROLENAMES3 = {"ROLE_ADMINISTRATOR"};

	static final String FOLDER_NAME = "exportTest01";
	static final String IMAGE_NAME = "ExportTestImage01";

	private RepositoryService repo;
	UserAuthorityService auth;

	private ExecutionContext context;

    public ExportUsersRolesTestTestNG(){
        m_logger.info("ExportUsersRolesTestTestNG => constructor() called");
    }

    @BeforeClass()
	public void onSetUp() throws Exception {
        m_logger.info("ExportUsersRolesTestTestNG => onSetUp() called");
		super.onSetUp();

		repo = (RepositoryService) getBean("repositoryService");
		auth = (UserAuthorityService) getBean("userAuthorityService");
		createExtraUsersAndRoles();
	}

    @AfterClass()
	public void onTearDown() throws Exception {
        m_logger.info("ExportUsersRolesTestTestNG => onSetUp() called");
		super.onTearDown();

        auth.deleteRole(context, ROLE3);
        auth.deleteUser(context, USER1);
        auth.deleteUser(context, USER2);
        auth.deleteUser(context, USER3);
	}

	private void createExtraUsersAndRoles() {
		createUsers();
	}

	/*
	 * Export all users and roles
	 */
    @Test()
	public void doExportImport_AllUsersRolesTest() {
        m_logger.info("ExportUsersRolesTestTestNG => doExportImport_AllUsersRolesTest() called");

		String exportDir = createExportDir();
		Parameters exportParams = createParameters()
			.addParameterValue(PARAM_EXPORT_DIR, exportDir)
			.addParameter(PARAM_EXPORT_ROLES)
			.addParameter(PARAM_EXPORT_USERS);
		performExport(exportParams);

		Parameters importParams = createParameters()
			.addParameterValue(PARAM_IMPORT_DIR, exportDir);
		performImport(importParams);
	}

	/*
	 * Export a named set of users
	 */
    @Test()
	public void doExportImport_NamedUsersTest() {
        m_logger.info("ExportUsersRolesTestTestNG => doExportImport_NamedUsersTest() called");

		String exportDir = createExportDir();
		Parameters exportParams = createParameters()
			.addParameterValue(PARAM_EXPORT_DIR, exportDir)
			.addParameterValues(PARAM_EXPORT_USERS, USERNAMES);
		performExport(exportParams);

		Parameters importParams = createParameters()
			.addParameterValue(PARAM_IMPORT_DIR, exportDir);
		performImport(importParams);
	}

	/*
	 * Export a named set of roles
	 */
    @Test()
	public void doExportImport_NamedRolesTest() {
        m_logger.info("ExportUsersRolesTestTestNG => doExportImport_NamedRolesTest() called");

		String exportDir = createExportDir();
		Parameters exportParams = createParameters()
			.addParameterValue(PARAM_EXPORT_DIR, exportDir)
			.addParameterValues(PARAM_EXPORT_ROLES, ROLENAMES3);
		performExport(exportParams);

		Parameters importParams = createParameters()
			.addParameterValue(PARAM_IMPORT_DIR, exportDir);
		performImport(importParams);
	}

	/*
	 * Export a named set of users and named set of roles
	 */
    @Test()
	public void doExportImport_NamedUsersRolesTest() {
        m_logger.info("ExportUsersRolesTestTestNG => doExportImport_NamedUsersRolesTest() called");

		String exportDir = createExportDir();
		Parameters exportParams = createParameters()
			.addParameterValue(PARAM_EXPORT_DIR, exportDir)
			.addParameterValues(PARAM_EXPORT_USERS, USERNAMES)
			.addParameterValues(PARAM_EXPORT_ROLES, ROLENAMES3);
		performExport(exportParams);

		Parameters importParams = createParameters()
			.addParameterValue(PARAM_IMPORT_DIR, exportDir);
		performImport(importParams);
	}

	/*
	 * Export Users, Roles, and a FileResource together
	 */
    @Test()
	public void doExportImport_AllUsers_And_FileResourceTest() {
        m_logger.info("ExportUsersRolesTestTestNG => doExportImport_AllUsers_And_FileResourceTest() called");
        setAuthenticatedUser(BaseServiceSetupTestNG.USER_JASPERADMIN);

		createImageResource();
		String exportDir = createExportDir();
		Parameters exportParams = createParameters()
			.addParameterValue(PARAM_EXPORT_DIR, exportDir)
			.addParameterValue(PARAM_EXPORT_URIS, PATH_SEP + FOLDER_NAME + PATH_SEP + IMAGE_NAME)
			.addParameter(PARAM_EXPORT_USERS)
			.addParameter(PARAM_EXPORT_ROLES);
		performExport(exportParams);

		deleteImageResource();

		Parameters importParams = createParameters()
			.addParameterValue(PARAM_IMPORT_DIR, exportDir);
		performImport(importParams);

		deleteImageResource();
	}


	protected void createUsers() {
		User user1 = auth.newUser(null);
		user1.setUsername(USER1);
		user1.setPassword("password");
		user1.setFullName("New test user");
		user1.setEnabled(true);
		user1.setExternallyDefined(false);
		auth.putUser(null, user1);

		Role role1 = addRole(user1, ROLE1, false);

		User user2 = auth.newUser(null);
		user2.setUsername(USER2);
		user2.setPassword("password");
		user2.setFullName("New test user");
		user2.setEnabled(true);
		user2.setExternallyDefined(false);
		auth.putUser(null, user2);

		Role role2 = addRole(user2, ROLE2, false);

		User user3 = auth.newUser(null);
		user3.setUsername(USER3);
		user3.setPassword("password");
		user3.setFullName("New test user");
		user3.setEnabled(true);
		user3.setExternallyDefined(false);
		auth.putUser(null, user3);

		Role role3 = addRole(user3, ROLE3, false);
	}

	protected void deleteUsers() {
		// hmmm, no way to truly delete a user...?
		User user1 = auth.getUser(context, USER1);
		Role role1 = auth.getRole(context, ROLE1);
		auth.removeRole(context, user1, role1);

		User user2 = auth.getUser(context, USER2);
		Role role2 = auth.getRole(context, ROLE2);
		auth.removeRole(context, user2, role2);

		User user3 = auth.getUser(context, USER3);
		Role role3 = auth.getRole(context, ROLE3);
		auth.removeRole(context, user3, role3);

		auth.disableUser(context, USER1);
		auth.disableUser(context, USER2);
		auth.disableUser(context, USER3);
	}

	protected void createImageResource() {

		Folder folder = new FolderImpl();
		folder.setName(FOLDER_NAME);
		folder.setLabel(FOLDER_NAME + LABEL);
		folder.setDescription(FOLDER_NAME + DESC);
		folder.setParentFolder("/");
		repo.saveFolder(null, folder);

		FileResource image = (FileResource) repo.newResource(null, FileResource.class);
		image.setFileType(FileResource.TYPE_IMAGE);
		image.readData(getClass().getResourceAsStream("/images/jasperreports.png"));
		image.setName(IMAGE_NAME);
		image.setLabel(IMAGE_NAME + LABEL);
		image.setDescription(IMAGE_NAME + DESC);
		image.setParentFolder(folder);
		repo.saveResource(null, image);
	}

	protected void deleteImageResource() {
		repo.deleteFolder(null, PATH_SEP + FOLDER_NAME);
	}

	// method for creating a role, and adding to existing user
	protected Role addRole(User user, String roleName, boolean externallyDefined) {
		Role role = auth.getRole(null, roleName);

		if (role == null) {
			role = auth.newRole(null);
			role.setRoleName(roleName);
			role.setExternallyDefined(externallyDefined);

			auth.putRole(null, role);
		}

		auth.addRole(null, user, role);
		return role;
	}

}
