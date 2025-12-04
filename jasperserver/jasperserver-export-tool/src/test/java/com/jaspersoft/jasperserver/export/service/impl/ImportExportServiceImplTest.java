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

package com.jaspersoft.jasperserver.export.service.impl;

import com.jaspersoft.jasperserver.api.common.crypto.KeystoreManagerFactory;
import com.jaspersoft.jasperserver.api.common.util.spring.ApplicationContextProvider;
import com.jaspersoft.jasperserver.crypto.KeyProperties;
import com.jaspersoft.jasperserver.crypto.KeystoreManager;
import com.jaspersoft.jasperserver.test.ks.KeystoreUtils;
import com.jaspersoft.jasperserver.dto.common.WarningDescriptor;
import com.jaspersoft.jasperserver.export.Exporter;
import com.jaspersoft.jasperserver.export.Importer;
import com.jaspersoft.jasperserver.export.ProfileAttributeServiceMock;
import com.jaspersoft.jasperserver.export.UserAuthorityServiceMock;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import static java.lang.System.getenv;
import static org.springframework.util.ResourceUtils.getFile;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

/**
    @author Zakhar.Tomchenco
*/
@ContextConfiguration(locations={"classpath:applicationContext-export-import-test.xml","classpath:applicationContext-export-import-web.xml"})
public class ImportExportServiceImplTest extends AbstractTestNGSpringContextTests {

    @Resource(name = "importExportService")
    ImportExportServiceImpl service;

    @Resource(name = "profileAttributeService")
    ProfileAttributeServiceMock profileAttributeService;

    @Resource(name = "userAuthorityService")
    UserAuthorityServiceMock authorityService;

    @Resource(name = "importExportPrivilegeRoles")
    protected Set<String> importExportPrivilegeRoles;

    @Resource(name = "keystoreManager")
    protected KeystoreManager keystoreManager;

    //needed for import/export beans using password decryption;
	// without this resource init, UserBean's importExportCipher is
	// not initialized correctly because StaticApplicationContext.getApplicationContext()
	// is null.
    @Resource(name = "applicationContextProvider")
	ApplicationContextProvider applicationContextProvider;

    private Set<Importer> importerInstances = Collections.synchronizedSet(new HashSet<Importer>());
    private Set<Exporter> exporterInstances = Collections.synchronizedSet(new HashSet<Exporter>());

    private Locale enUsLocale = new Locale("en,", "US");

    @BeforeClass(
            alwaysRun = true,
            dependsOnMethods = {"springTestContextBeforeTestClass"}
    )
    protected void springTestContextPrepareTestInstance() throws Exception {
        KeystoreUtils.createIfNotExists(ImportExportServiceImplTest.class);

        super.springTestContextPrepareTestInstance();
    }

    @Test(threadPoolSize = 20, invocationCount = 20, enabled = true)
    public void testInjectExporterPrototype1(){
        exporterInstances.add(service.getExporter());
    }

    @Test(dependsOnMethods = {"testInjectExporterPrototype1"}, enabled = true)
    public void testInjectExporterPrototype2(){
        assertEquals(exporterInstances.size(), 20);
    }

    @Test(threadPoolSize = 20, invocationCount = 20, enabled = true)
    public void testInjectImporterPrototype1(){
        importerInstances.add(service.getImporter());
    }

    @Test(dependsOnMethods = {"testInjectImporterPrototype1"}, enabled = true)
    public void testInjectImporterPrototype2(){
        assertEquals(importerInstances.size(), 20);
    }

    @Test(threadPoolSize = 10, invocationCount = 200, enabled = true)
    public void testExportRoles() throws Exception {
        Random r = new Random(new Date().getTime());
        StringBuilder builder = new StringBuilder();

        List<String> expected = new ArrayList<String>();
        if (r.nextBoolean()) expected.add("ROLE_USER");
        if (r.nextBoolean()) expected.add("ROLE_DEMO");
        if (r.nextBoolean()) expected.add("ROLE_SUPERUSER");
        if (r.nextBoolean()) expected.add("ROLE_ADMINISTRATOR");

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

//        try {
            service.doExport(outputStream, null, null, null, expected, null, null, null, enUsLocale, new ArrayList<WarningDescriptor>());
//        }
//        catch (Exception e){
//            fail(e.getMessage());
//        }

        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

        ZipInputStream zip = new ZipInputStream(inputStream);

        Set<String> entries = new HashSet<String>();

        try {
            ZipEntry entry = zip.getNextEntry();
            while (entry != null) {
                entries.add(entry.getName());
                entry = zip.getNextEntry();
            }
        } catch (Exception er) {
            fail(er.getMessage());
        }

        if (expected.size() > 0) {
            assertEquals(entries.size(), expected.size() + 2, "Quantity of items is different");
        }
        else {
            assertEquals(entries.size(), 6, "Quantity of items is different"); // all roles were exported
        }
        assertTrue(entries.contains("roles/"));
        assertTrue(entries.contains("index.xml"));

        for (String s : expected) {
            if (!entries.contains("roles/" + s + ".xml"))
                fail("roles/" + s + ".xml not found");
        }
    }

    @Test(threadPoolSize = 20, invocationCount = 200, enabled = true)
    public void testExportRolesWithUsers() {
        List<String> expected = new ArrayList<String>();
        expected.add("ROLE_USER");
        expected.add("ROLE_DEMO");

        Map<String, String> params = new HashMap<>();
        params.put("role-users", Boolean.TRUE.toString());

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            service.doExport(outputStream, params, null, null, expected, null, null, null, enUsLocale, new ArrayList<WarningDescriptor>());
        }
        catch (Exception e){
            fail(e.getMessage());
        }

        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

        ZipInputStream zip = new ZipInputStream(inputStream);

        Set<String> entries = new HashSet<String>();

        try {
            ZipEntry entry = zip.getNextEntry();
            while (entry != null) {
                entries.add(entry.getName());
                entry = zip.getNextEntry();
            }
        } catch (Exception er) {
            fail(er.getMessage());
        }

        assertEquals(entries.size(), expected.size() + 4);
        assertTrue(entries.contains("roles/"));
        assertTrue(entries.contains("index.xml"));
        assertTrue(entries.contains("users/"));
        assertTrue(entries.contains("users/demo.xml"));

        for (String s : expected) {
            if (!entries.contains("roles/" + s + ".xml"))
                fail("roles/" + s + ".xml not found");
        }
    }

    @Test(enabled = true)
    public void testExportRolesWithUsersUsingNonDefaultKey() {
        List<String> expected = new ArrayList<String>();
        expected.add("ROLE_USER");
        expected.add("ROLE_DEMO");

        Map<String, String> params = new HashMap<>();
        params.put("role-users", Boolean.TRUE.toString());
        params.put("keyalias", "diagnosticDataEncSecret");

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            service.doExport(outputStream, params, null, null, expected, null, null, null, enUsLocale, new ArrayList<WarningDescriptor>());
        }
        catch (Exception e){
            fail(e.getMessage());
        }

        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

        ZipInputStream zip = new ZipInputStream(inputStream);

        final KeyProperties properties = keystoreManager.getKeystore(null).getKeyProperties("diagnosticDataEncSecret");

        boolean foundExpectedKeyByUUID = false;
        try {
            ZipEntry entry = zip.getNextEntry();
            while (entry != null) {
                if ("index.xml".equals( entry.getName())){
                    String text = "";
                    try (Scanner scanner = new Scanner(zip, StandardCharsets.UTF_8.name())) {
                        final Scanner scan = scanner.useDelimiter("<property");
                        while (!text.equalsIgnoreCase("</export>")) {
                            text = scan.next();

                            foundExpectedKeyByUUID = text.contains(properties.getKeyUuid().toString());
                            if (foundExpectedKeyByUUID) break;
                        }
                    }
                }

                if(foundExpectedKeyByUUID) break;
                entry = zip.getNextEntry();
            }
        } catch (Exception er) {
            fail(er.getMessage());
        }

        assertTrue(foundExpectedKeyByUUID);
    }

    @Test(enabled = true)
    public void testInputUsersAndRoles(){
        InputStream stream = this.getClass().getResourceAsStream("/testRolesAndUsers.zip");

        assertNull(authorityService.getUser(null, "superuser"));
        assertNull(authorityService.getRole(null, "ROLE_SUPERMART_MANAGER"));
        assertNull(authorityService.getRole(null, "ROLE_PORTLET"));
        assertNull(profileAttributeService.getProfileAttribute(null,null));

        try {
            service.doImport(stream,null, null, null, enUsLocale, new ArrayList<WarningDescriptor>());
        } catch (Exception e) {
            fail(e.getMessage());
        }

        assertNotNull(authorityService.getUser(null, "superuser"));
        assertNotNull(authorityService.getRole(null, "ROLE_SUPERMART_MANAGER"));
        assertNotNull(authorityService.getRole(null, "ROLE_PORTLET"));
        assertNotNull(profileAttributeService.getProfileAttribute(null,null));
    }

    @Test(enabled = true)
    public void tesJapaneseMessageForInvalidContentShouldBeLocalized(){
        InputStream stream = this.getClass().getResourceAsStream("/invalidContentStructure.zip");

        String key = "exception.remote.import.failed.content.error";
        Locale jaJpLocale = new Locale("ja");
        try {
            service.doImport(stream, null, null, null, jaJpLocale, new ArrayList<WarningDescriptor>());
        } catch (Exception e) {
            assertEquals(e.getMessage(), "インポートに失敗しました。理由：指定された zip ファイルが、有効な Jasper Server OS エクスポートファイルではありません。");
        }
    }

    @Test(enabled = true)
    public void testSpanishMessageForInvalidContentShouldBeLocalized(){
        InputStream stream = this.getClass().getResourceAsStream("/invalidContentStructure.zip");

        String key = "exception.remote.import.failed.content.error";
        Locale esEsLocale = new Locale("es");
        try {
            service.doImport(stream, null, null, null, esEsLocale, new ArrayList<WarningDescriptor>());
        } catch (Exception e) {
            assertEquals(e.getLocalizedMessage(), "Error en la importación. Motivo: el archivo zip proporcionado no es un archivo de exportación de Jasper Server OS válido.");
        }
    }

    @Test(enabled = true)
    public void testGermanMessageForInvalidZipShouldBeLocalized(){
        InputStream stream = this.getClass().getResourceAsStream("/invalidZipFile.zip");

        String key = "exception.remote.import.failed.zip.error";
        Locale esEsLocale = new Locale("de");
        try {
            service.doImport(stream, null, null, null, esEsLocale, new ArrayList<WarningDescriptor>());
        } catch (Exception e) {
            assertEquals(e.getMessage(), "Der Import ist fehlgeschlagen. Grund: Die ZIP-Datei kann nicht gelesen werden.");
        }
    }

    @Test(enabled = true)
    public void testChineseMessageForInvalidZipShouldBeLocalized(){
        InputStream stream = this.getClass().getResourceAsStream("/invalidZipFile.zip");

        String key = "exception.remote.import.failed.zip.error";
        Locale esEsLocale = new Locale("zh", "CN");
        try {
            service.doImport(stream, null, null, null, esEsLocale, new ArrayList<WarningDescriptor>());
        } catch (Exception e) {
            assertEquals(e.getMessage(), "导入失败。原因: 无法读取 zip 文件。");
        }
    }
}
