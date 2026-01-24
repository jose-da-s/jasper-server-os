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

package com.jaspersoft.jasperserver.export.modules.favorites;

import com.jaspersoft.jasperserver.api.common.domain.ExecutionContext;
import com.jaspersoft.jasperserver.api.metadata.common.domain.Folder;
import com.jaspersoft.jasperserver.api.metadata.common.domain.client.FavoriteResourceImpl;
import com.jaspersoft.jasperserver.api.metadata.common.service.RepositoryService;
import com.jaspersoft.jasperserver.api.metadata.common.service.impl.hibernate.FavoriteResourceService;
import com.jaspersoft.jasperserver.export.ExportTask;
import com.jaspersoft.jasperserver.export.Parameters;
import com.jaspersoft.jasperserver.export.ParametersImpl;
import com.jaspersoft.jasperserver.export.io.ExportOutput;
import com.jaspersoft.jasperserver.export.io.ObjectSerializer;
import com.jaspersoft.jasperserver.export.modules.ExporterModuleContext;
import com.jaspersoft.jasperserver.export.modules.ModuleRegister;
import com.jaspersoft.jasperserver.export.modules.favorites.beans.FavoriteResourceBean;
import com.jaspersoft.jasperserver.export.modules.favorites.beans.FavoriteResourceIndexBean;
import com.jaspersoft.jasperserver.export.modules.repository.RepositoryExportFilter;
import org.dom4j.Element;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.io.PipedOutputStream;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@RunWith(MockitoJUnitRunner.class)
public class FavoritesExporterTest {
    protected final PipedOutputStream pipedOutputStream = new PipedOutputStream();

    protected final ObjectSerializer objectSerializer = mock(ObjectSerializer.class);

    protected final ExporterModuleContext moduleContext = mock(ExporterModuleContext.class);

    protected final ExportTask exportTask = mock(ExportTask.class);

    protected final ExportOutput output = mock(ExportOutput.class);

    protected final Element indexElement = mock(Element.class);

    protected final Parameters parameters = new ParametersImpl();

    protected final ModuleRegister moduleRegister = mock(ModuleRegister.class);

    protected final RepositoryService repositoryService = mock(RepositoryService.class);

    protected final RepositoryExportFilter exportFilter = mock(RepositoryExportFilter.class);

    @Before
    public void baseSetUp() throws IOException {
        lenient().doAnswer(invocation -> invocation.getArgument(0).toString() + invocation.getArgument(1))
                .when(output).mkdir(anyString(), anyString());
        lenient().doReturn(pipedOutputStream).when(output).getFileOutputStream(anyString(), anyString());
        lenient().doReturn(parameters).when(exportTask).getParameters();
        lenient().doReturn(output).when(exportTask).getOutput();
        lenient().doReturn(indexElement).when(moduleContext).getModuleIndexElement();
        lenient().doReturn(exportTask).when(moduleContext).getExportTask();
        lenient().doReturn(moduleRegister).when(moduleContext).getModuleRegister();
    }

    private static final String FAVORITE_DIR = "/favorites";
    private static final String INDEX_FILENAME = "favoriteIndexFilename";
    public static final String INDEX_FAVORITE_ELEMENT = "indexFavoriteElement";
    private static final String EVERYTHING_ARG = "everythingArg";
    @InjectMocks
    private FavoritesExporter favoritesExporter;

    private final FavoriteResourceService favoriteService = mock(FavoriteResourceService.class);

    private final FavoritesModuleConfiguration configuration = new FavoritesModuleConfiguration();

    private final Element indexFavoriteElement = mock(Element.class);

    private Folder rootFolder = mock(Folder.class, "rootFolder");

    private static final String ROOT_URI = "/";

    private Folder publicFolder = mock(Folder.class, "publicFolder");

    private static final String PUBLIC_URI = "/public";

    @Captor
    private ArgumentCaptor<?> favoriteBeanArgumentCaptor;

    @Before
    public void setUp() throws IOException {
        baseSetUp();

        configuration.setFavoritesDir(FAVORITE_DIR);
        configuration.setFavoriteResourceService(favoriteService);
        configuration.setSerializer(objectSerializer);
        configuration.setFavoriteIndexFilename(INDEX_FILENAME);
        configuration.setIndexFavoriteElement(INDEX_FAVORITE_ELEMENT);
        configuration.setRepository(repositoryService);
        favoritesExporter.setConfiguration(configuration);
        favoritesExporter.setEverythingArg(EVERYTHING_ARG);
        favoritesExporter.init(moduleContext);

        doReturn(indexFavoriteElement).when(indexElement).addElement(INDEX_FAVORITE_ELEMENT);
        doReturn(rootFolder).when(configuration.getRepository()).getFolder(nullable(ExecutionContext.class), eq(ROOT_URI));
        doReturn(singletonList(publicFolder)).when(configuration.getRepository()).getSubFolders(nullable(ExecutionContext.class), eq(ROOT_URI));
        doReturn(null).when(configuration.getRepository()).getSubFolders(nullable(ExecutionContext.class), eq(PUBLIC_URI));
        doReturn(PUBLIC_URI).when(publicFolder).getURIString();


    }

    @Test
    public void processFolderWithFavorites_success() throws IOException {

        // Arrange
        FavoriteResourceImpl favoriteResource = new FavoriteResourceImpl();
        favoriteResource.setResourceURI("/public/resourceUri");
        favoriteResource.setUserName("superuser");
        favoriteResource.setId(1111);
        doReturn(false).when(exportFilter).excludeFolder(anyString(), any());

        doReturn(singletonList(favoriteResource)).when(favoriteService).getFavoritesForUri(
                nullable(ExecutionContext.class), eq("/public"), any()
        );


        favoritesExporter.process();

        // Assert
        verify(objectSerializer, times(2)).write(favoriteBeanArgumentCaptor.capture(), any(), eq(moduleContext));

        FavoriteResourceBean favoriteResourceBean = (FavoriteResourceBean) favoriteBeanArgumentCaptor.getAllValues().get(0);
        assertEquals(favoriteResource.getId(), favoriteResourceBean.getId());

        FavoriteResourceIndexBean favoriteResourceIndexBean = (FavoriteResourceIndexBean) favoriteBeanArgumentCaptor.getAllValues().get(1);
        assertEquals(favoriteResource.getId(), favoriteResourceIndexBean.getFavoriteIds()[0]);
    }

    @Test
    public void processFolderWithoutFavoriteResources_nothing() {
        // Arrange
        FavoriteResourceImpl favoriteResource = new FavoriteResourceImpl();
        favoriteResource.setResourceURI("/public/resourceUri");
        favoriteResource.setUserName("superuser");
        favoriteResource.setId(1111);

        // Act
        favoritesExporter.process();

        // Assert
        verifyNoInteractions(objectSerializer);
        verifyNoInteractions(indexFavoriteElement);
    }
}