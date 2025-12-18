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

package com.jaspersoft.jasperserver.api.engine.jasperreports.service.impl;


import com.jaspersoft.jasperserver.api.common.domain.ExecutionContext;
import com.jaspersoft.jasperserver.api.engine.jasperreports.util.DataSourceServiceFactory;
import com.jaspersoft.jasperserver.api.logging.audit.context.AuditContext;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.ReportDataSource;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.ReportUnit;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.service.ReportDataSourceService;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.service.ReportDataSourceServiceFactory;
import com.jaspersoft.jasperserver.api.metadata.user.domain.client.RoleImpl;
import com.jaspersoft.jasperserver.api.metadata.user.domain.client.UserImpl;
import net.sf.jasperreports.engine.JasperReport;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class EngineServiceImplTest {

    @InjectMocks
    private EngineServiceImpl service;

    @Mock
    private AuditContext auditContext;
    @Mock
    private Appendable testAppendable;
    @Mock(name = "engineExecutions")
    private Map<String, EngineServiceImpl.ReportExecutionStatus> engineExecutions;
    @Mock(name = "errorReportDSFactory")
    private Map<String, ErrorTemplateReportService> errorReportDSFactory;
    @Mock
    private DataSourceServiceFactory dataSourceServiceFactories;

    @Mock
    private ReportDataSourceServiceFactory factory;
    @Mock
    private ReportDataSourceService dataSourceService;
    @Mock
    private EngineServiceImpl.ReportFiller filler;
    @Mock
    private ExecutionContext context;
    @Mock
    private ReportUnit reportUnit;
    @Mock
    private JasperReport report;
    @Mock
    private ReportDataSource datasource;

    @Before
    public void setUp() throws Exception {
        doNothing().when(auditContext).doInAuditContext(anyString(), any());
        doReturn(factory).when(dataSourceServiceFactories).getBean(any());
        doReturn(dataSourceService).when(factory).createService(any(ReportDataSource.class), anyBoolean());
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void fillReport_hasCachedDataParameterIsTrue_usedCorrectReportDataSourceService() {
        verify_fillReport_hasCachedDataParameter_usedCorrectReportDataSourceService(true);
    }

    @Test
    public void fillReport_hasCachedDataParameterIsFalse_usedCorrectReportDataSourceService() {
        verify_fillReport_hasCachedDataParameter_usedCorrectReportDataSourceService(false);
    }

    public void verify_fillReport_hasCachedDataParameter_usedCorrectReportDataSourceService(boolean hasCachedData) {
        Map reportParameters = mock(Map.class);

        service.fillReport(context, reportUnit, report, reportParameters, datasource, null, filler, hasCachedData);

        verify(factory).createService(same(datasource), eq(hasCachedData));
        verify(dataSourceService).setReportParameterValues(same(reportParameters), eq(hasCachedData));
    }


    @Test
    public void inludeUser() {
        service.setAuthorizedAllExecutionsRolesList(Arrays.asList("ROLE_SUPERUSER"));
        service.setAuthorizedOrgExecutionsRolesList(Arrays.asList("ROLE_ADMINISTRATOR", "ROLE_ADMINISTRATOR"));
        Assert.assertTrue(includeUser("superuser", null, "superuser", null));
        Assert.assertTrue(includeUser("superuser", null, "joeuser", null));
        Assert.assertTrue(includeUser("superuser", null, "joeuser", "org1"));
        Assert.assertTrue(includeUser("superuser", null, "jasperadmin", null));
        Assert.assertTrue(includeUser("superuser", null, "jasperadmin", "org1"));
        Assert.assertFalse(includeUser("superuser", null, "jasperadmin", null, "jasperadmin|org1"));
        Assert.assertTrue(includeUser("superuser", null, "jasperadmin", "org1", "jasperadmin"));
        Assert.assertTrue(includeUser("superuser", null, "jasperadmin", "org1", "jasperadmin|org1"));
        Assert.assertFalse(includeUser("superuser", null, "jasperadmin", "org1", "superuser"));

        Assert.assertFalse(includeUser("jasperadmin", null, "superuser", null));
        Assert.assertTrue(includeUser("jasperadmin", null, "jasperamdin", null));
        Assert.assertTrue(includeUser("jasperadmin", null, "joeuser", null));
        Assert.assertTrue(includeUser("jasperadmin", "org1", "jasperadmin", "org1"));
        Assert.assertTrue(includeUser("jasperadmin", "org1", "joeuser", "org1"));
        Assert.assertFalse(includeUser("jasperadmin", "org1", "jasperadmin", "org2"));
        Assert.assertFalse(includeUser("jasperadmin", "org1", "joeuser", "org2"));

        Assert.assertFalse(includeUser("joeuser", null, "superuser", null));
        Assert.assertFalse(includeUser("joeuser", null, "jasperamdin", null));
        Assert.assertTrue(includeUser("joeuser", null, "joeuser", null));
        Assert.assertFalse(includeUser("joeuser", null, "joeuser", "org1"));
        Assert.assertFalse(includeUser("joeuser", "org1", "jasperadmin", "org1"));
        Assert.assertTrue(includeUser("joeuser", "org1", "joeuser", "org1"));
        Assert.assertFalse(includeUser("joeuser", "org1", "jasperadmin", "org2"));
        Assert.assertFalse(includeUser("joeuser", "org1", "joeuser", "org2"));
    }

    private boolean includeUser(String loginUser, String loginOrg, String curUser, String curOrg, String searchUser) {
        return service.includeUser(createUser(loginUser, loginOrg), createUser(curUser, curOrg), searchUser);
    }

    private boolean includeUser(String loginUser, String loginOrg, String curUser, String curOrg) {
        return service.includeUser(createUser(loginUser, loginOrg), createUser(curUser, curOrg));
    }

    private UserImpl createUser(String userName, String userOrg){
        UserImpl user = new UserImpl();
        user.setUsername(userName);
        user.setTenantId(userOrg);
        if (userName.equals("superuser") || userName.equals("jasperadmin")) {
            RoleImpl role1 = new RoleImpl();
            role1.setRoleName("ROLE_ADMINISTRATOR");
            user.addRole(role1);
        }
        if (userName.equals("superuser")) {
            RoleImpl role2 = new RoleImpl();
            role2.setRoleName("ROLE_SUPERUSER");
            user.addRole(role2);
        }
        return user;
    }

    @Test
    public void getDefaultValuesFromJasperReport() throws Exception {
        List<String> defValues = Arrays.asList("ABC\\, 123", "EDG", " HIG\\\\, 345", "XYZ");
        Map<String, Object> map = new HashMap<>();
        map.put("Name1", defValues);

        List<String> defValues2 = Arrays.asList("123", "\\,456", "75\\,6");
        map.put("Name2", defValues2);
        map.put("Name3", "xxx");
        Map<String, Object> result = service.getDefaultValuesFromJasperReport(map);

        Assert.assertTrue(((Collection<?>) result.get("Name1")).contains("ABC, 123"));
        Assert.assertTrue(((Collection<?>) result.get("Name1")).contains(" HIG\\, 345"));
        Assert.assertTrue(((Collection<?>) result.get("Name2")).contains(",456"));
        Assert.assertTrue(result.get("Name3").equals("xxx"));

    }



    @Test
    public void getDefaultValuesFromJasperReport2() throws Exception {
        List<String> defValues = Arrays.asList("ABC\\, 123");
        Map<String, Object> map = new HashMap<>();
        map.put("Name1", defValues);

        List<String> defValues2 = new ArrayList<>();
        defValues2.add(null);
        map.put("Name2", defValues2);
        map.put("Name3", null);
        Map<String, Object> result = service.getDefaultValuesFromJasperReport(map);

        Assert.assertTrue(((Collection<?>) result.get("Name1")).contains("ABC, 123"));
        Assert.assertTrue(((Collection<?>) result.get("Name2")).contains(null));
        Assert.assertNull(result.get("Name3"));

    }
}
