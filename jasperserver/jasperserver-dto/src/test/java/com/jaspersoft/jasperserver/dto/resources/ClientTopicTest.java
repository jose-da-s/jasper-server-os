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

import com.google.common.collect.ImmutableMap;
import com.jaspersoft.jasperserver.dto.basetests.BaseDTOPresentableTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.jaspersoft.jasperserver.dto.utils.CustomAssertions.assertNotSameCollection;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

/**
 * @author Andriy Tivodar <ativodar@tibco>
 */

public class ClientTopicTest extends BaseDTOPresentableTest<ClientTopic> {

    @Override
    protected List<ClientTopic> prepareInstancesWithAlternativeParameters() {
        return Arrays.asList(
                createFullyConfiguredInstance().setCreationDate("creationDate2"),
                createFullyConfiguredInstance().setDataSource(new ClientAdhocDataView().setLabel("label2")),
                createFullyConfiguredInstance().setDescription("description2"),
                createFullyConfiguredInstance().setLabel("label2"),
                createFullyConfiguredInstance().setPermissionMask(24),
                createFullyConfiguredInstance().setUri("uri2"),
                createFullyConfiguredInstance().setVersion(24),
                createFullyConfiguredInstance().setUpdateDate("updateDate2"),
                createFullyConfiguredInstance().setAlwaysPromptControls(false),
                createFullyConfiguredInstance().setFiles(ImmutableMap.<String, ClientReferenceableFile>of("testA2", new ClientFile(), "testB2", new ClientFile().setContent("content2"))),
                createFullyConfiguredInstance().setInputControlRenderingView("renderingView2"),
                createFullyConfiguredInstance().setControlsLayout(AbstractClientReportUnit.ControlsLayoutType.separatePage),
                createFullyConfiguredInstance().setJrxml(new ClientFile().setContent("content2")),
                createFullyConfiguredInstance().setQuery(new ClientQuery().setValue("value2")),
                createFullyConfiguredInstance().setInputControls(Arrays.<ClientReferenceableInputControl>asList(new ClientReference(), new ClientReference().setUri("uri2"))),
                createFullyConfiguredInstance().setReportRenderingView("renderingView2"),
                // fields with null values
                createFullyConfiguredInstance().setCreationDate(null),
                createFullyConfiguredInstance().setDataSource(null),
                createFullyConfiguredInstance().setDescription(null),
                createFullyConfiguredInstance().setLabel(null),
                createFullyConfiguredInstance().setPermissionMask(null),
                createFullyConfiguredInstance().setUri(null),
                createFullyConfiguredInstance().setVersion(null),
                createFullyConfiguredInstance().setUpdateDate(null),
                createFullyConfiguredInstance().setFiles(null),
                createFullyConfiguredInstance().setInputControlRenderingView(null),
                createFullyConfiguredInstance().setControlsLayout(null),
                createFullyConfiguredInstance().setJrxml(null),
                createFullyConfiguredInstance().setQuery(null),
                createFullyConfiguredInstance().setInputControls(null),
                createFullyConfiguredInstance().setReportRenderingView(null)
        );
    }

    @Override
    protected ClientTopic createFullyConfiguredInstance() {
        ClientTopic clientTopic = new ClientTopic();
        clientTopic.setCreationDate("creationDate");
        clientTopic.setDataSource(new ClientAdhocDataView().setLabel("label"));
        clientTopic.setDescription("description");
        clientTopic.setLabel("label");
        clientTopic.setPermissionMask(23);
        clientTopic.setUri("uri");
        clientTopic.setVersion(23);
        clientTopic.setUpdateDate("updateDate");
        clientTopic.setAlwaysPromptControls(true);
        clientTopic.setFiles(ImmutableMap.<String, ClientReferenceableFile>of("testA", new ClientFile(), "testB", new ClientFile().setContent("content")));
        clientTopic.setInputControlRenderingView("renderingView");
        clientTopic.setControlsLayout(AbstractClientReportUnit.ControlsLayoutType.popupScreen);
        clientTopic.setJrxml(new ClientFile().setContent("content"));
        clientTopic.setQuery(new ClientQuery().setValue("value"));
        clientTopic.setInputControls(Arrays.<ClientReferenceableInputControl>asList(new ClientReference(), new ClientReference().setUri("uri")));
        clientTopic.setReportRenderingView("renderingView");
        return clientTopic;
    }

    @Override
    protected ClientTopic createInstanceWithDefaultParameters() {
        return new ClientTopic();
    }

    @Override
    protected ClientTopic createInstanceFromOther(ClientTopic other) {
        return new ClientTopic(other);
    }

    @Override
    protected void assertFieldsHaveUniqueReferences(ClientTopic expected, ClientTopic actual) {
        assertNotSameCollection(expected.getInputControls(), actual.getInputControls());
        assertNotSameCollection(expected.getFiles().values(), actual.getFiles().values());
        assertNotSame(expected.getDataSource(), actual.getDataSource());
        assertNotSame(expected.getJrxml(), actual.getJrxml());
        assertNotSame(expected.getQuery(), actual.getQuery());
    }

    private List<ClientReferenceableDataSource> prepareDifferentClientReferenceableDataSource() {
        String uri = "uri";
        return Arrays.<ClientReferenceableDataSource>asList(
                new ClientAwsDataSource().setUri(uri),
                new ClientBeanDataSource().setUri(uri),
                new ClientJdbcDataSource().setUri(uri),
                new ClientJndiJdbcDataSource().setUri(uri),
                new ClientSemanticLayerDataSource().setUri(uri),
                new ClientVirtualDataSource().setUri(uri)
        );
    }

    @Test
    public void instanceCanBeCreatedFromOtherWithClientReferenceJrxml() {
        ClientReferenceableFile jrxml = new ClientReference().setUri("uri");
        fullyConfiguredTestInstance.setJrxml(jrxml);

        ClientTopic result = new ClientTopic(fullyConfiguredTestInstance);

        assertEquals(jrxml, result.getJrxml());
    }

    @Test
    public void instanceCanBeCreatedFromOtherWithClientReferenceFiles() {
        ClientReferenceableFile file = new ClientReference().setUri("uri");
        Map<String, ClientReferenceableFile> files = Collections.singletonMap("key", file);
        fullyConfiguredTestInstance.setFiles(files);

        ClientTopic result = new ClientTopic(fullyConfiguredTestInstance);

        assertEquals(files, result.getFiles());
    }

    @ParameterizedTest
    @MethodSource(value = "prepareDifferentClientReferenceableDataSource")
    public void instanceCanBeCreatedFromOtherWithDifferentDataSource(ClientReferenceableDataSource instance) {
        fullyConfiguredTestInstance.setDataSource(instance);

        ClientTopic result = new ClientTopic(fullyConfiguredTestInstance);

        assertEquals(instance, result.getDataSource());
    }
}
