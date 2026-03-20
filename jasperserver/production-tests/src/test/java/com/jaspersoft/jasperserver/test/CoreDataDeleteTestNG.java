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
import com.jaspersoft.jasperserver.api.common.util.StaticExecutionContextProvider;
import com.jaspersoft.jasperserver.api.metadata.common.service.impl.hibernate.persistent.RepoFolder;
import com.jaspersoft.jasperserver.api.metadata.security.JasperServerPermission;
import com.jaspersoft.jasperserver.api.metadata.user.domain.ObjectPermission;
import com.jaspersoft.jasperserver.api.metadata.user.domain.Role;
import com.jaspersoft.jasperserver.api.metadata.user.domain.User;
import com.jaspersoft.jasperserver.api.metadata.user.service.ObjectPermissionService;
import com.jaspersoft.jasperserver.api.metadata.user.service.TenantService;
import com.jaspersoft.jasperserver.util.test.BaseServiceSetupTestNG;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate5.HibernateCallback;
import org.springframework.orm.hibernate5.HibernateTemplate;
import org.springframework.orm.hibernate5.SessionFactoryUtils;
import org.springframework.orm.hibernate5.SessionHolder;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import jakarta.annotation.Resource;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

/**
 * @author srosen
 *
 * Deletes the core production data for CE using the TestNG framework
 */

public class CoreDataDeleteTestNG extends BaseServiceSetupTestNG {

    protected final Log m_logger = LogFactory.getLog(CoreDataDeleteTestNG.class);

	SessionFactory m_sessionFactory;
    HibernateTemplate m_template;
	HibernateDaoSupport m_jasperServerDao;

	public HibernateDaoSupport getJasperServerDao() {
		return m_jasperServerDao;
	}

    @Resource(name = "jasperServerDao")
	public void setJasperServerDao(HibernateDaoSupport jasperServerDao) {
		m_logger.info("setJasperServerDao() called");
		this.m_jasperServerDao = jasperServerDao;
	}

	public SessionFactory getSessionFactory() {
		return m_sessionFactory;
	}

    @Resource(name = "sessionFactory")
	public void setSessionFactory(SessionFactory sessionFactory) {
		m_logger.info("setSessionFactory() called");
		this.m_sessionFactory = sessionFactory;
	}

    @BeforeClass()
	protected void onSetUp() throws Exception {
		m_logger.info("onSetUp() called");

		/*
		 * The TransactionSynchronizationManager work is only needed in tests to allow multiple
		 * Hibernate transactions to occur. Otherwise, each "template." call is a transaction.
		 * Lazy initialization of collections will not occur otherwise. In a web app, there is Spring
		 * configuration to do a transaction per web request - OpenSessionInViewFilter.
		 */
        Session s = m_sessionFactory.openSession();
        TransactionSynchronizationManager.bindResource(m_sessionFactory, new SessionHolder(s));
        m_template = m_jasperServerDao.getHibernateTemplate();
	}

    @AfterClass()
	public void onTearDown() {
        m_logger.info("onTearDown() called");

        SessionHolder holder = (SessionHolder) TransactionSynchronizationManager.getResource(m_sessionFactory);
        Session s = holder.getSession();
//        s.flush();
        TransactionSynchronizationManager.unbindResource(m_sessionFactory);
        SessionFactoryUtils.closeSession(s);
	}

    /*
     * This method is the starting point for deleting resources that comprise the
     * core production data for the Community Edition (CE) product.
     */
    @Test()
	public void deleteCoreDataResources() throws Exception {
        m_logger.info("deleteCoreDataResources() called");

        deleteDefaultTheme();
        deleteUsersAndRoles();
        deleteDefaultDomainWhitelist();
        deleteDBProfileAttributes();
        deleteTenantForRoot();
        deleteRootFolder();
	}

    /*
     * delete the default theme files for the core ce data
     */
    private void deleteDefaultTheme() throws Exception {
        m_logger.info("deleteDefaultTheme() called");

        // apply permissions now that new folder and the themes are in the repository
        // /themes/default folder needs be R/W/C/D for the Administrator so we can delete resources and files...
        Role adminRole = getRole(ROLE_ADMINISTRATOR);
        ObjectPermission objPerm = createObjectPermission("/themes/default", adminRole, JasperServerPermission.READ_WRITE_CREATE_DELETE.getMask());
        ExecutionContext executionContext = StaticExecutionContextProvider.getExecutionContext();
        executionContext.getAttributes().add(ObjectPermissionService.PRIVILEGED_OPERATION);
        getObjectPermissionService().putObjectPermission(executionContext, objPerm);

        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("theme_files_list.txt");
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));

        String filePath = null;
        // set the authenticated user to be jasperadmin
        setAuthenticatedUser(BaseServiceSetupTestNG.USER_JASPERADMIN);

        // delete the theme resources and files
        while ((filePath = reader.readLine()) != null) {
            // filePath looks like "themes/themeName/styleOrImage.css"
            deleteThemeFile("/" + filePath);
        }

        reader.close();

        // delete the theme folders
        deleteFolder("/themes/pods_summer/images");
        deleteFolder("/themes/pods_summer");
        deleteFolder("/themes/default/images");
        deleteFolder("/themes/default");
        deleteFolder("/themes");
    }

    private void deleteThemeFile(String filePath) {
        m_logger.info("deleteThemeFile() => deleting file : " + filePath);
        getRepositoryService().deleteResource(getExecutionContext(), filePath);
    }

    /*
     * delete users and roles needed for the core ce data
     */
    private void deleteUsersAndRoles() {
        m_logger.info("deleteUsersAndRoles() called");

        // set the authenticated user to be jasperadmin
        setAuthenticatedUser(BaseServiceSetupTestNG.USER_JASPERADMIN);

        // remove role ROLE_ANONYMOUS from anonymousUser
        // delete role ROLE_ANONYMOUS
        // delete user anonymousUser
        User anonUser = getUser(BaseServiceSetupTestNG.USER_ANONYMOUS);
        removeRole(anonUser, ROLE_ANONYMOUS);
        deleteRole(ROLE_ANONYMOUS);
        deleteUser(BaseServiceSetupTestNG.USER_ANONYMOUS);

        // remove role ROLE_USER from jasperadmin
        // remove role ROLE_ADMINISTRATOR from jasperadmin
        // delete role ROLE_USER
        // delete role ROLE_ADMINISTRATOR
        // delete user jasperadmin
        User jasperadmin = getUser(BaseServiceSetupTestNG.USER_JASPERADMIN);
        removeRole(jasperadmin, BaseServiceSetupTestNG.ROLE_USER);
        removeRole(jasperadmin, BaseServiceSetupTestNG.ROLE_ADMINISTRATOR);
        deleteRole(BaseServiceSetupTestNG.ROLE_USER);
        deleteRole(BaseServiceSetupTestNG.ROLE_ADMINISTRATOR);
        deleteUser(BaseServiceSetupTestNG.USER_JASPERADMIN);
    }

    /*
     * delete a tenant from the root
     */
    private void deleteTenantForRoot() {
        m_logger.info("deleteTenantForRoot() called");
        deleteTenant("", TenantService.ORGANIZATIONS);
    }

    /*
     * delete a tenant
     */
    private void deleteTenant(String parentTenantId, String tenantId) {

        if (!(TenantService.ORGANIZATIONS.equals(tenantId)) && !(TenantService.ORGANIZATIONS.equals(parentTenantId))) {
            tenantId = parentTenantId + "_" + tenantId;
        }
        getTenantService().deleteTenant(null, tenantId);
    }

    /*
     * delete the root folder
     */
    private void deleteRootFolder() {
        m_logger.info("deleteRootFolder() called");

        //we need to delete a folder because RepoResource.parent is not nullable
        List result = (List)m_template.execute((HibernateCallback) session -> session
                .createQuery("from RepoFolder where uri = ?1")
                .setParameter(1, "/")
                .list());


//		assertNotNull("result", result);
//		assertTrue("result.size() == 1",result.size() == 1);

		RepoFolder root = (RepoFolder) result.get(0);

        if( root != null )
        {
		    m_template.delete(root);
        }
        else
        {
            m_logger.error("deleteRootFolder() => FAILED to delete the root folder - RATS!");
        }
    }
}

