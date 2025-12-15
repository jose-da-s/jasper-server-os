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

package com.jaspersoft.jasperserver.dto.resources;

import com.jaspersoft.jasperserver.dto.basetests.BaseDTOTest;
import com.jaspersoft.jasperserver.dto.resources.domain.ClientDomain;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

/**
 * @author Andriy Tivodar <ativodar@tibco>
 */

class ClientDashboardResourceTest extends BaseDTOTest<ClientDashboardResource> {

    private static final String NAME = "name";
    private static final String TYPE = "type";
    private static final ClientReferenceable CLIENT_REFERENCEABLE = new ClientFile().setContent("content");

    @Override
    protected List<ClientDashboardResource> prepareInstancesWithAlternativeParameters() {
        return Arrays.asList(
                createFullyConfiguredInstance().setName("name2"),
                createFullyConfiguredInstance().setType("type2"),
                createFullyConfiguredInstance().setResource(new ClientFile().setContent("content2")),
                // with null values
                createFullyConfiguredInstance().setName(null),
                createFullyConfiguredInstance().setType(null),
                createFullyConfiguredInstance().setResource(null)
        );
    }

    @Override
    protected ClientDashboardResource createFullyConfiguredInstance() {
        return createInstanceWithDefaultParameters()
                .setName(NAME)
                .setType(TYPE)
                .setResource(CLIENT_REFERENCEABLE);
    }

    @Override
    protected ClientDashboardResource createInstanceWithDefaultParameters() {
        return new ClientDashboardResource();
    }

    @Override
    protected ClientDashboardResource createInstanceFromOther(ClientDashboardResource other) {
        return new ClientDashboardResource(other);
    }

    @Override
    protected void assertFieldsHaveUniqueReferences(ClientDashboardResource expected, ClientDashboardResource actual) {
        assertNotSame(expected.getResource(), actual.getResource());
    }

    @Test
    public void copiedInstanceEqualsToOriginalInstanceWithClientAdhocDataViewResource() {
        testInstanceWithDefaultParameters.setResource(new ClientAdhocDataView());
        ClientDashboardResource copied = createInstanceFromOther(testInstanceWithDefaultParameters);

        assertEquals(testInstanceWithDefaultParameters.getResource(), copied.getResource());
        assertNotSame(testInstanceWithDefaultParameters.getResource(), copied.getResource());
    }

    @Test
    public void copiedInstanceEqualsToOriginalInstanceWithClientAwsDataSourceResource() {
        testInstanceWithDefaultParameters.setResource(new ClientAwsDataSource());
        ClientDashboardResource copied = createInstanceFromOther(testInstanceWithDefaultParameters);

        assertEquals(testInstanceWithDefaultParameters.getResource(), copied.getResource());
        assertNotSame(testInstanceWithDefaultParameters.getResource(), copied.getResource());
    }

    @Test
    public void copiedInstanceEqualsToOriginalInstanceWithClientAzureSqlDataSourceResource() {
        testInstanceWithDefaultParameters.setResource(new ClientAzureSqlDataSource());
        ClientDashboardResource copied = createInstanceFromOther(testInstanceWithDefaultParameters);

        assertEquals(testInstanceWithDefaultParameters.getResource(), copied.getResource());
        assertNotSame(testInstanceWithDefaultParameters.getResource(), copied.getResource());
    }

    @Test
    public void copiedInstanceEqualsToOriginalInstanceWithClientBeanDataSourceResource() {
        testInstanceWithDefaultParameters.setResource(new ClientBeanDataSource());
        ClientDashboardResource copied = createInstanceFromOther(testInstanceWithDefaultParameters);

        assertEquals(testInstanceWithDefaultParameters.getResource(), copied.getResource());
        assertNotSame(testInstanceWithDefaultParameters.getResource(), copied.getResource());
    }

    @Test
    public void copiedInstanceEqualsToOriginalInstanceWithClientCustomDataSourceResource() {
        testInstanceWithDefaultParameters.setResource(new ClientCustomDataSource());
        ClientDashboardResource copied = createInstanceFromOther(testInstanceWithDefaultParameters);

        assertEquals(testInstanceWithDefaultParameters.getResource(), copied.getResource());
        assertNotSame(testInstanceWithDefaultParameters.getResource(), copied.getResource());
    }

    @Test
    public void copiedInstanceEqualsToOriginalInstanceWithClientDataTypeResource() {
        testInstanceWithDefaultParameters.setResource(new ClientDataType());
        ClientDashboardResource copied = createInstanceFromOther(testInstanceWithDefaultParameters);

        assertEquals(testInstanceWithDefaultParameters.getResource(), copied.getResource());
        assertNotSame(testInstanceWithDefaultParameters.getResource(), copied.getResource());
    }

    @Test
    public void copiedInstanceEqualsToOriginalInstanceWithClientDomainResource() {
        testInstanceWithDefaultParameters.setResource(new ClientDomain());
        ClientDashboardResource copied = createInstanceFromOther(testInstanceWithDefaultParameters);

        assertEquals(testInstanceWithDefaultParameters.getResource(), copied.getResource());
        assertNotSame(testInstanceWithDefaultParameters.getResource(), copied.getResource());
    }

    @Test
    public void copiedInstanceEqualsToOriginalInstanceWithClientDomainTopicResource() {
        testInstanceWithDefaultParameters.setResource(new ClientDomainTopic());
        ClientDashboardResource copied = createInstanceFromOther(testInstanceWithDefaultParameters);

        assertEquals(testInstanceWithDefaultParameters.getResource(), copied.getResource());
        assertNotSame(testInstanceWithDefaultParameters.getResource(), copied.getResource());
    }

    @Test
    public void copiedInstanceEqualsToOriginalInstanceWithClientFileResource() {
        testInstanceWithDefaultParameters.setResource(new ClientFile());
        ClientDashboardResource copied = createInstanceFromOther(testInstanceWithDefaultParameters);

        assertEquals(testInstanceWithDefaultParameters.getResource(), copied.getResource());
        assertNotSame(testInstanceWithDefaultParameters.getResource(), copied.getResource());
    }

    @Test
    public void copiedInstanceEqualsToOriginalInstanceWithClientInputControlResource() {
        testInstanceWithDefaultParameters.setResource(new ClientInputControl());
        ClientDashboardResource copied = createInstanceFromOther(testInstanceWithDefaultParameters);

        assertEquals(testInstanceWithDefaultParameters.getResource(), copied.getResource());
        assertNotSame(testInstanceWithDefaultParameters.getResource(), copied.getResource());
    }

    @Test
    public void copiedInstanceEqualsToOriginalInstanceWithClientJdbcDataSourceResource() {
        testInstanceWithDefaultParameters.setResource(new ClientJdbcDataSource());
        ClientDashboardResource copied = createInstanceFromOther(testInstanceWithDefaultParameters);

        assertEquals(testInstanceWithDefaultParameters.getResource(), copied.getResource());
        assertNotSame(testInstanceWithDefaultParameters.getResource(), copied.getResource());
    }

    @Test
    public void copiedInstanceEqualsToOriginalInstanceWithClientJndiJdbcDataSourceResource() {
        testInstanceWithDefaultParameters.setResource(new ClientJndiJdbcDataSource());
        ClientDashboardResource copied = createInstanceFromOther(testInstanceWithDefaultParameters);

        assertEquals(testInstanceWithDefaultParameters.getResource(), copied.getResource());
        assertNotSame(testInstanceWithDefaultParameters.getResource(), copied.getResource());
    }

    @Test
    public void copiedInstanceEqualsToOriginalInstanceWithClientListOfValuesResource() {
        testInstanceWithDefaultParameters.setResource(new ClientListOfValues());
        ClientDashboardResource copied = createInstanceFromOther(testInstanceWithDefaultParameters);

        assertEquals(testInstanceWithDefaultParameters.getResource(), copied.getResource());
        assertNotSame(testInstanceWithDefaultParameters.getResource(), copied.getResource());
    }

    @Test
    public void copiedInstanceEqualsToOriginalInstanceWithClientQueryResource() {
        testInstanceWithDefaultParameters.setResource(new ClientQuery());
        ClientDashboardResource copied = createInstanceFromOther(testInstanceWithDefaultParameters);

        assertEquals(testInstanceWithDefaultParameters.getResource(), copied.getResource());
        assertNotSame(testInstanceWithDefaultParameters.getResource(), copied.getResource());
    }

    @Test
    public void copiedInstanceEqualsToOriginalInstanceWithClientReferenceResource() {
        testInstanceWithDefaultParameters.setResource(new ClientReference());
        ClientDashboardResource copied = createInstanceFromOther(testInstanceWithDefaultParameters);

        assertEquals(testInstanceWithDefaultParameters.getResource(), copied.getResource());
        assertNotSame(testInstanceWithDefaultParameters.getResource(), copied.getResource());
    }

    @Test
    public void copiedInstanceEqualsToOriginalInstanceWithClientReportUnitResource() {
        testInstanceWithDefaultParameters.setResource(new ClientReportUnit());
        ClientDashboardResource copied = createInstanceFromOther(testInstanceWithDefaultParameters);

        assertEquals(testInstanceWithDefaultParameters.getResource(), copied.getResource());
        assertNotSame(testInstanceWithDefaultParameters.getResource(), copied.getResource());
    }

    @Test
    public void copiedInstanceEqualsToOriginalInstanceWithClientSemanticLayerDataSourceResource() {
        testInstanceWithDefaultParameters.setResource(new ClientSemanticLayerDataSource());
        ClientDashboardResource copied = createInstanceFromOther(testInstanceWithDefaultParameters);

        assertEquals(testInstanceWithDefaultParameters.getResource(), copied.getResource());
        assertNotSame(testInstanceWithDefaultParameters.getResource(), copied.getResource());
    }

    @Test
    public void copiedInstanceEqualsToOriginalInstanceWithClientTopicResource() {
        testInstanceWithDefaultParameters.setResource(new ClientTopic());
        ClientDashboardResource copied = createInstanceFromOther(testInstanceWithDefaultParameters);

        assertEquals(testInstanceWithDefaultParameters.getResource(), copied.getResource());
        assertNotSame(testInstanceWithDefaultParameters.getResource(), copied.getResource());
    }

    @Test
    public void copiedInstanceEqualsToOriginalInstanceWithClientVirtualDataSourceResource() {
        testInstanceWithDefaultParameters.setResource(new ClientVirtualDataSource());
        ClientDashboardResource copied = createInstanceFromOther(testInstanceWithDefaultParameters);

        assertEquals(testInstanceWithDefaultParameters.getResource(), copied.getResource());
        assertNotSame(testInstanceWithDefaultParameters.getResource(), copied.getResource());
    }

    @Test
    public void copiedInstanceEqualsToOriginalInstanceWithClientSqlExecutionRequestResource() {
        testInstanceWithDefaultParameters.setResource(new SqlExecutionRequest());
        ClientDashboardResource copied = createInstanceFromOther(testInstanceWithDefaultParameters);

        assertEquals(testInstanceWithDefaultParameters.getResource(), copied.getResource());
        assertNotSame(testInstanceWithDefaultParameters.getResource(), copied.getResource());
    }

    @Test
    public void gettersReturnCorrectValue() {
        Assertions.assertSame(NAME, fullyConfiguredTestInstance.getName());
        Assertions.assertSame(TYPE, fullyConfiguredTestInstance.getType());
        Assertions.assertSame(CLIENT_REFERENCEABLE, fullyConfiguredTestInstance.getResource());
    }
}