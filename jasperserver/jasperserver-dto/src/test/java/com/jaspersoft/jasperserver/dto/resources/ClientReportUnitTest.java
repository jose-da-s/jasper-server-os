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

import com.jaspersoft.jasperserver.dto.basetests.BaseDTOPresentableTest;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * <p></p>
 *
 * @author Yaroslav.Kovalchyk
 * @author Andriy Tivodar <ativodar@tibco>
 * @version $Id$
 */
public class ClientReportUnitTest extends BaseDTOPresentableTest<ClientReportUnit> {

    private static final String _03__Store_Segment_Performance_Report = "reports/_03__Store_Segment_Performance_Report.json";

    // Base class fields (AbstractClientReportUnit)

    private static final ClientReferenceableQuery TEST_QUERY = new ClientReference().setUri("TEST_URI");
    private static final ClientReferenceableQuery TEST_QUERY_1 = new ClientReference().setUri("TEST_URI_1");

    private static final ClientReferenceableFile TEST_JRXML = new ClientFile().setContent("TEST_CONTENT");
    private static final ClientReferenceableFile TEST_JRXML_1 = new ClientFile().setContent("TEST_CONTENT_1");

    private static final List<ClientReferenceableInputControl> TEST_INPUT_CONTROLS = Arrays.asList(
            (ClientReferenceableInputControl)new ClientInputControl().setMandatory(true),
            (ClientReferenceableInputControl)new ClientInputControl().setValueColumn("TEST_VALUE_COLUMN")
    );
    private static final List<ClientReferenceableInputControl> TEST_INPUT_CONTROLS_1 = Arrays.asList(
            (ClientReferenceableInputControl)new ClientInputControl().setMandatory(false),
            (ClientReferenceableInputControl)new ClientInputControl().setValueColumn("TEST_VALUE_COLUMN_1")
    );

    private static final Map<String, ClientReferenceableFile> TEST_FILES = createTestFiles("TEST_KEY", TEST_JRXML);
    private static final Map<String, ClientReferenceableFile> TEST_FILES_1 = createTestFiles("TEST_KEY_1", TEST_JRXML_1);

    private static final String TEST_INPUT_CONTROL_RENDERING_VIEW = "TEST_INPUT_CONTROL_RENDERING_VIEW";
    private static final String TEST_INPUT_CONTROL_RENDERING_VIEW_1 = "TEST_INPUT_CONTROL_RENDERING_VIEW_1";

    private static final String TEST_REPORT_RENDERING_VIEW = "TEST_REPORT_RENDERING_VIEW";
    private static final String TEST_REPORT_RENDERING_VIEW_1 = "TEST_REPORT_RENDERING_VIEW_1";

    private static final boolean TEST_ALWAYS_PROMPT_CONTROLS = true;
    private static final boolean TEST_ALWAYS_PROMPT_CONTROLS_1 = false;

    private static final AbstractClientReportUnit.ControlsLayoutType TEST_CONTROLS_LAYOUT = AbstractClientReportUnit.ControlsLayoutType.popupScreen;
    private static final AbstractClientReportUnit.ControlsLayoutType TEST_CONTROLS_LAYOUT_1 = AbstractClientReportUnit.ControlsLayoutType.inPage;

    private static Map<String, ClientReferenceableFile> createTestFiles(String key, ClientReferenceableFile value) {
        Map<String, ClientReferenceableFile> files = new HashMap<String, ClientReferenceableFile>();
        files.put(key, value);
        return files;
    }

    // Base class fields (AbstractClientDataSourceHolder)

    private static final ClientReferenceableDataSource TEST_DATA_SOURCE = new ClientCustomDataSource().setDataSourceName("TEST_DATA_SOURCE_NAME");
    private static final ClientReferenceableDataSource TEST_DATA_SOURCE_1 = new ClientCustomDataSource().setDataSourceName("TEST_DATA_SOURCE_NAME_1");

    // Base class fields (ClientResource)

    private static final Integer TEST_VERSION = 101;
    private static final Integer TEST_VERSION_1 = 1011;

    private static final Integer TEST_PERMISSION_MASK = 100;
    private static final Integer TEST_PERMISSION_MASK_1 = 1001;

    private static final String TEST_CREATION_DATE = "TEST_CREATION_DATE";
    private static final String TEST_CREATION_DATE_1 = "TEST_CREATION_DATE_1";

    private static final String TEST_UDPATE_DATE = "TEST_UPDATE_DATE";
    private static final String TEST_UDPATE_DATE_1 = "TEST_UPDATE_DATE_1";

    private static final String TEST_LABEL = "TEST_LABEL";
    private static final String TEST_LABEL_1 = "TEST_LABEL_1";

    private static final String TEST_DESCRIPTION = "TEST_DESCRIPTION";
    private static final String TEST_DESCRIPTION_1 = "TEST_DESCRIPTION_1";

    private static final String TEST_URI = "TEST_URI";
    private static final String TEST_URI_1 = "TEST_URI_1";

    @Override
    protected List<ClientReportUnit> prepareInstancesWithAlternativeParameters() {
        return Arrays.asList(
                // base class fields (AbstractClientReportUnit)
                createFullyConfiguredInstance().setQuery(TEST_QUERY_1),
                createFullyConfiguredInstance().setJrxml(TEST_JRXML_1),
                createFullyConfiguredInstance().setInputControls(TEST_INPUT_CONTROLS_1),
                createFullyConfiguredInstance().setFiles(TEST_FILES_1),
                createFullyConfiguredInstance().setInputControlRenderingView(TEST_INPUT_CONTROL_RENDERING_VIEW_1),
                createFullyConfiguredInstance().setReportRenderingView(TEST_REPORT_RENDERING_VIEW_1),
                createFullyConfiguredInstance().setAlwaysPromptControls(TEST_ALWAYS_PROMPT_CONTROLS_1),
                createFullyConfiguredInstance().setControlsLayout(TEST_CONTROLS_LAYOUT_1),
                // base class fields (AbstractClientDataSourceHolder)
                createFullyConfiguredInstance().setDataSource(TEST_DATA_SOURCE_1),
                // base class fields (ClientResource)
                createFullyConfiguredInstance().setVersion(TEST_VERSION_1),
                createFullyConfiguredInstance().setPermissionMask(TEST_PERMISSION_MASK_1),
                createFullyConfiguredInstance().setCreationDate(TEST_CREATION_DATE_1),
                createFullyConfiguredInstance().setUpdateDate(TEST_UDPATE_DATE_1),
                createFullyConfiguredInstance().setLabel(TEST_LABEL_1),
                createFullyConfiguredInstance().setDescription(TEST_DESCRIPTION_1),
                createFullyConfiguredInstance().setUri(TEST_URI_1),
                // fields with null values
                // base class fields (AbstractClientReportUnit)
                createFullyConfiguredInstance().setQuery(null),
                createFullyConfiguredInstance().setJrxml(null),
                createFullyConfiguredInstance().setInputControls(null),
                createFullyConfiguredInstance().setFiles(null),
                createFullyConfiguredInstance().setInputControlRenderingView(null),
                createFullyConfiguredInstance().setReportRenderingView(null),
                createFullyConfiguredInstance().setControlsLayout(null),
                // base class fields (AbstractClientDataSourceHolder)
                createFullyConfiguredInstance().setDataSource(null),
                // base class fields (ClientResource)
                createFullyConfiguredInstance().setVersion(null),
                createFullyConfiguredInstance().setPermissionMask(null),
                createFullyConfiguredInstance().setCreationDate(null),
                createFullyConfiguredInstance().setUpdateDate(null),
                createFullyConfiguredInstance().setLabel(null),
                createFullyConfiguredInstance().setDescription(null),
                createFullyConfiguredInstance().setUri(null)
        );
    }

    @Override
    protected ClientReportUnit createFullyConfiguredInstance() {
        return createInstanceWithDefaultParameters()
                // base class fields (AbstractClientReportUnit)
                .setQuery(TEST_QUERY)
                .setJrxml(TEST_JRXML)
                .setInputControls(TEST_INPUT_CONTROLS)
                .setFiles(TEST_FILES)
                .setInputControlRenderingView(TEST_INPUT_CONTROL_RENDERING_VIEW)
                .setReportRenderingView(TEST_REPORT_RENDERING_VIEW)
                .setAlwaysPromptControls(TEST_ALWAYS_PROMPT_CONTROLS)
                .setControlsLayout(TEST_CONTROLS_LAYOUT)
                // base class fields (AbstractClientDataSourceHolder)
                .setDataSource(TEST_DATA_SOURCE)
                // base class fields (ClientResource)
                .setVersion(TEST_VERSION)
                .setPermissionMask(TEST_PERMISSION_MASK)
                .setCreationDate(TEST_CREATION_DATE)
                .setUpdateDate(TEST_UDPATE_DATE)
                .setLabel(TEST_LABEL)
                .setDescription(TEST_DESCRIPTION)
                .setUri(TEST_URI);
    }

    @Override
    protected ClientReportUnit createInstanceWithDefaultParameters() {
        return new ClientReportUnit();
    }

    @Override
    protected ClientReportUnit createInstanceFromOther(ClientReportUnit other) {
        return new ClientReportUnit(other);
    }
}
