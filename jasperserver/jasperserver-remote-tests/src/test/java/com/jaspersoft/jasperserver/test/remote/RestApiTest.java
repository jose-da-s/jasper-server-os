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

package com.jaspersoft.jasperserver.test.remote;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;
import java.util.Scanner;
import java.util.TimeZone;

import javax.xml.bind.annotation.XmlElementWrapper;

import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.*;

import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.message.BasicClassicHttpRequest;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.Method;
import org.apache.hc.core5.http.io.entity.ByteArrayEntity;
import org.apache.hc.core5.http.io.entity.StringEntity;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;

import com.jaspersoft.jasperserver.dto.resources.ClientResourceListWrapper;
import com.jaspersoft.jasperserver.dto.common.OutputFormat;
import com.jaspersoft.jasperserver.dto.importexport.ExportTask;
import com.jaspersoft.jasperserver.dto.importexport.ImportTask;
import com.jaspersoft.jasperserver.dto.importexport.State;
import com.jaspersoft.jasperserver.dto.job.ClientIntervalUnitType;
import com.jaspersoft.jasperserver.dto.job.ClientJobRepositoryDestination;
import com.jaspersoft.jasperserver.dto.job.ClientJobSimpleTrigger;
import com.jaspersoft.jasperserver.dto.job.ClientJobSource;
import com.jaspersoft.jasperserver.dto.job.ClientJobTrigger;
import com.jaspersoft.jasperserver.dto.job.ClientReportJob;
import com.jaspersoft.jasperserver.dto.resources.ClientFile;
import com.jaspersoft.jasperserver.dto.resources.ClientFolder;
import com.jaspersoft.jasperserver.dto.resources.ClientJdbcDataSource;
import com.jaspersoft.jasperserver.dto.resources.ClientReference;
import com.jaspersoft.jasperserver.dto.resources.ClientReportUnit;

public class RestApiTest {
    private static final Logger logger = 
        LoggerFactory.getLogger(RestApiTest.class);

    private static final String CONTENT_TYPE = "json";
    private static final String ACCEPT_TYPE = "json";
    private static final String TIMEZONE = "America/New_York";
    // All objects are created inside this one folder to avoid clashes with other tests
    // or still other uses of the Jasper Server
    private static final String REPO_FOLDER_NAME = "RestApiTest81945036";
    private static final String JASPERSERVER_USERS_JRXML = "jasperserver-users.jrxml";

    private static JasperServerConstants constants;
    private static CloseableHttpClient httpClient;
    private static BasicCredentialsProvider credsProvider;
    private static ObjectMapper objectMapper;

    @BeforeClass
    public static void setUp() throws IOException {
        constants = JasperServerConstants.instance();
        credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(
                new AuthScope(constants.HOST, Integer.parseInt(constants.PORT)),
                new UsernamePasswordCredentials(constants.USERNAME, constants.PASSWORD.toCharArray()));
        httpClient = HttpClientBuilder.create()
                .setDefaultCredentialsProvider(credsProvider)
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JaxbAnnotationModule());
    
        // delete our folder
        Optional<ClientFolder> testFolder = getRepositoryResourceDescriptor(
                REPO_FOLDER_NAME.toLowerCase(),
                false,
                ClientFolder.class);
        if (testFolder.isPresent())
            deleteRepositoryResource(REPO_FOLDER_NAME.toLowerCase());
    }

    @AfterClass
    public static void tearDown() throws IOException {
        httpClient.close();
    }

    @Test
    public void resourcesShouldContainThemes() throws Exception {
        String url = constants.REST_RESOURCES_URL + "?recursive=false";
        BasicClassicHttpRequest request = new BasicClassicHttpRequest(Method.GET, url);
        request.addHeader("Accept", "application/" + ACCEPT_TYPE);
        Optional<ClientResourceListWrapper> resourcesO = listRepositoryFolder("", false);
        assertThat(resourcesO).isNotEmpty();
        ClientResourceListWrapper resources = resourcesO.get();
        assertThat(resources.getResourceLookups()).isNotEmpty();
        assertThat(resources.getResourceLookups()).extracting("resourceType", "uri", "label")
                .contains(tuple("folder", "/themes", "Themes"));
    }

    /**
     * A full test of a basic Jasper Server workflow: Create some folders, a data source, and
     * a report. Run the report and download its output. Create a schedule. Export and import
     * the repository.
     * @throws Exception if any communication with the Jasper Server fails or if reading test
     * resources fails
     */
    @Test
    public void uploadResourcesRunReportExportImportTest() throws Exception {
        String repoPath = "/" + REPO_FOLDER_NAME.toLowerCase();
        ClientFolder testFolder = new ClientFolder()
                .setLabel(REPO_FOLDER_NAME)
                .setDescription("")
                .setPermissionMask(0);
        createRepositoryResource(repoPath, "folder", testFolder, ClientFolder.class);

        // add a data source
        String dsrcPath = repoPath + "/datasources";
        ClientFolder datasourcesFolder = new ClientFolder()
                .setLabel("Data Sources")
                .setDescription("")
                .setPermissionMask(0);
        createRepositoryResource(dsrcPath, "folder", datasourcesFolder, ClientFolder.class);

        String jrsDsrcPath = dsrcPath + "/jasperserverJdbc";
        ClientJdbcDataSource jasperserverDsrc = new ClientJdbcDataSource()
                .setLabel("Jasper Server Data Source")
                .setDescription("JDBC Data Source connecting to Jasper Server's repository DB")
                .setPermissionMask(0)
                .setDriverClass(getRepositoryDBJdbcDriverClass())
                .setConnectionUrl(getRepositoryDBJdbcUrl())
                .setUsername(getRepositoryDBUsername())
                .setPassword(getRepositoryDBPassword());
        createRepositoryResource(jrsDsrcPath, "jdbcDataSource", jasperserverDsrc, ClientJdbcDataSource.class);

        // add a report file
        String jrxmlPath = repoPath + "/jrxml";
        ClientFolder jrxmlFolder = new ClientFolder()
                .setLabel("JRXML Files")
                .setDescription("")
                .setPermissionMask(0);
        createRepositoryResource(jrxmlPath, "folder", jrxmlFolder, ClientFolder.class);

        String jrsUsersJrxmlPath = jrxmlPath + "/jasperserver_users.jrxml";
        String jrxml = getJasperserverUsersJrxml();
        ClientFile jrxmlFile = new ClientFile()
                .setLabel("jasperserver-users.jrxml")
                .setDescription("List of Jasper Server users")
                .setPermissionMask(0)
                .setType(ClientFile.FileType.jrxml)
                .setContent(Base64.getEncoder().encodeToString(jrxml.getBytes()));
        createRepositoryResource(jrsUsersJrxmlPath, "file", jrxmlFile, ClientFile.class);

        // create a report unit
        String reportPath = repoPath + "/reports";
        ClientFolder reportFolder = new ClientFolder()
                .setLabel("Reports")
                .setDescription("")
                .setPermissionMask(0);
        createRepositoryResource(reportPath, "folder", reportFolder, ClientFolder.class);

        String jrsUsersReportName = "jasperserver_users";
        String jrsUsersReportPath = reportPath + "/" + jrsUsersReportName;
        ClientReportUnit jrsUsersReport = new ClientReportUnit()
                .setLabel("Jasper Server Users Report")
                .setDescription("")
                .setPermissionMask(0)
                .setJrxml(new ClientReference().setUri(jrsUsersJrxmlPath))
                .setDataSource(new ClientReference().setUri(jrsDsrcPath));
        createRepositoryResource(jrsUsersReportPath, "reportUnit", jrsUsersReport, ClientReportUnit.class);

        // run the report and export to CSV (because that one's easy to process)
        byte[] csv = runReportSimple(jrsUsersReportPath, "csv");
        assertThat(new String(csv)).contains("jasperadmin", "joeuser");

        // schedule a job - not for actually running, just to test whether export/import
        // works even with a schedule (as of 2025-08-11, this does not work on java 17)
        ClientReportJob jrsUsersJob = new ClientReportJob()
                .setLabel("Jasper Server Users Daily")
                .setDescription("")
                .setTrigger(new ClientJobSimpleTrigger()
                        .setOccurrenceCount(1)
                        .setRecurrenceInterval(1)
                        .setRecurrenceIntervalUnit(ClientIntervalUnitType.DAY)
                        .setStartType(ClientJobTrigger.START_TYPE_SCHEDULE)
                        .setTimezone(TimeZone.getDefault().getID())
                        .setStartDate(Date.from(ZonedDateTime.now().plusHours(4).toInstant())))
                .setSource(new ClientJobSource()
                        .setReportUnitURI(jrsUsersReportPath))
                .setBaseOutputFilename(jrsUsersReportName)
                .setOutputFormats(Collections.singleton(OutputFormat.PDF))
                .setRepositoryDestination(new ClientJobRepositoryDestination()
                        .setSaveToRepository(true)
                        .setFolderURI(reportPath)
                        .setOverwriteFiles(true)
                        .setSequentialFilenames(false));
        scheduleJob(jrsUsersJob);

        // Export the repository
        // Need to manually build the JSON because jackson doesn't work properly with
        // the @XmlElementWrapper annotation, meanwhile JAXB can't do JSON, MEANWHILE
        // tibco apparently just decided that report jobs, as the ONLY TYPE OF OBJECT,
        // exclusively support JSON and not XML.
        // There must be some way that you can have both JSON and full JAXB annotation
        // support, after all JRS itself automatically marshals and unmarshals request
        // bodies. but it's been eight hours now and i'm not going to learn yet another
        // giant framework (of _course_ the REST API uses yet another framework, Glassfish
        // Jersey) just to get this stupid test working.
        String exportJson = "{\"parameters\":[\"everything\"]}";
        State exportState = startExportTask(exportJson);
        String exportId = exportState.getId();
        while ("inprogress".equals(exportState.getPhase())) {
            Thread.sleep(50);
            exportState = pollExportTask(exportId);
        }
        if (!("finished".equals(exportState.getPhase()))) {
            throw new Exception(exportState.toString());
        }
        byte[] exportZip = getExportOutput(exportId, jrsUsersReportName + ".zip");

        // Delete our report, it should be restored by the import in the next step
        deleteRepositoryResource(jrsUsersReportPath);
        assertThat(getRepositoryResourceDescriptor(jrsUsersReportPath, false, ClientReportUnit.class))
                .isEmpty();

        State importState = startImportTask(exportZip);
        String importId = importState.getId();
        while ("inprogress".equals(importState.getPhase())) {
            Thread.sleep(50);
            importState = pollImportTask(importId);
        }
        if (!("finished".equals(importState.getPhase()))) {
            throw new Exception(importState.toString());
        }

        assertThat(getRepositoryResourceDescriptor(jrsUsersReportPath, false, ClientReportUnit.class))
                .isNotEmpty();
    }



    // =========================== REST API UTILITIES ==========================
    /**
     * List the contents of a repository folder.
     * @param uri The folder's URI. The leading slash is optional.
     * @param recursive If true, list contents recursively.
     * @return a ClientResourceListWrapper with the listed resources, or Optional.empty() if the
     *         server returned 404.
     * @throws IOException
     */
    private static Optional<ClientResourceListWrapper> listRepositoryFolder(
            String uri,
            boolean recursive) throws IOException {
        if (uri.length() > 0 && uri.charAt(0) != '/')
            uri = "/" + uri;
        String url = constants.REST_RESOURCES_URL + uri + "?recursive=" + Boolean.toString(recursive);
        BasicClassicHttpRequest request = new BasicClassicHttpRequest(Method.GET, url);
        request.addHeader("Accept", "application/" + ACCEPT_TYPE);
        Optional<ClientResourceListWrapper> resources = httpClient.execute(request, response -> {
            if (response.getCode() == 200)
                try (InputStream responseBody = response.getEntity().getContent()) {
                    return Optional.of(objectMapper.readValue(responseBody, ClientResourceListWrapper.class));
                }
            else if (response.getCode() == 404) {
                return Optional.empty();
            } else {
                throw new IOException("HTTP status: expecting 200 or 404, got " + response.getCode());
            }
        });
        return resources;
    }

    /**
     * Get the resource descriptor for a repository resource. If requesting a folder,
     * this method passes Accept=application/repository.folder+&lt;format&gt; to get
     * the resource descriptor instead of the contents.
     * @param <T> The Java class of the resource being created (usually some variant of
     *        com.jaspersoft.jasperserver.dto.resources.Client*)
     * @param uri The resource's URI. The leading slash is optional.
     * @param expanded true if nested resources should be fully expanded
     * @param resourceClass The Class object for T. Must match the resource type being
     *        returned, or there will be an error when deserializing the body.
     * @return The resource descriptor, or Optional.empty if the server returns 404.
     * @throws IOException
     */
    private static<T> Optional<T> getRepositoryResourceDescriptor(
            String uri,
            boolean expanded,
            Class<T> resourceClass) throws IOException {
        if (uri.length() > 0 && uri.charAt(0) != '/')
            uri = "/" + uri;
        String url = constants.REST_RESOURCES_URL + uri + "?expanded=" + Boolean.toString(expanded);
        BasicClassicHttpRequest request = new BasicClassicHttpRequest(Method.GET, url);

        if (resourceClass.getName().equals("ClientFolder"))
            request.addHeader("Accept", "application/repository.folder+" + ACCEPT_TYPE);
        else
            request.addHeader("Accept", "application/" + ACCEPT_TYPE);

        Optional<T> resource = httpClient.execute(request, response -> {
            if (response.getCode() == 200)
                try (InputStream responseBody = response.getEntity().getContent()) {
                    return Optional.of(objectMapper.readValue(responseBody, resourceClass));
                }
            else if (response.getCode() == 404) {
                return Optional.empty();
            } else {
                throw new IOException("HTTP status: expecting 200 or 404, got " + response.getCode());
            }
        });
        return resource;
    }
    
    /**
     * Create a resource on the server
     * @param <T> The Java class of the resource being created (usually some variant of
     *        com.jaspersoft.jasperserver.dto.resources.Client*)
     * @param uri The URI at which the resource should be created. Must include the resource's own ID
     *        (using PUT). The leading slash is optional.
     * @param resourceType The repository resource type (folder, jdbcDataSource, etc.) to use for
     *        Content-Type
     * @param resource The resource, of class T, to be serialized
     * @param resourceClass The Class object for T. Should be the same as that of resource, because
     *        Jasper Server always returns an object of the same type that you sent.
     * @return
     * @throws IOException if an error occurs during Jackson serialization, network I/O, or
     *         Jackson deserialization, or if the request is invalid.
     */
    private static<T> T createRepositoryResource(
            String uri,
            String resourceType,
            T resource,
            Class<T> resourceClass) throws IOException {
        if (uri.length() > 0 && uri.charAt(0) != '/')
            uri = "/" + uri;
        String url = constants.REST_RESOURCES_URL + uri;
        BasicClassicHttpRequest request = new BasicClassicHttpRequest(Method.PUT, url);
        request.addHeader("Accept", "application/" + ACCEPT_TYPE);
        request.addHeader("Content-Type", "application/repository." + resourceType + "+" + CONTENT_TYPE);
        String body = objectMapper.writeValueAsString(resource);
        request.setEntity(new StringEntity(body));
        T created = httpClient.execute(request, response -> {
            if (response.getCode() == 201)
                try (InputStream responseBody = response.getEntity().getContent()) {
                    return objectMapper.readValue(responseBody, resourceClass);
                }
            else if (response.getCode() == 400) {
                try (InputStream responseBody = response.getEntity().getContent()) {
                    try (Scanner s = new Scanner(responseBody, StandardCharsets.UTF_8.name())){
                        logger.error(s.useDelimiter("\\A").next());
                        throw new IOException("Bad API call");
                    }
                }
            } else {
                throw new IOException("HTTP status: expecting 201, got " + response.getCode());
            }
        });
        return created;
    }

    /**
     * Delete a repository resource.
     * @param uri The resource to delete. The leading slash is optional.
     * @throws IOException
     */
    private static void deleteRepositoryResource(
            String uri) throws IOException {
        if (uri.length() > 0 && uri.charAt(0) != '/')
            uri = "/" + uri;
        String url = constants.REST_RESOURCES_URL + uri;
        BasicClassicHttpRequest request = new BasicClassicHttpRequest(Method.DELETE, url);
        httpClient.execute(request, response -> {
            if (response.getCode() != 204)
                throw new IOException("HTTP status: expecting 204, got " + response.getCode());
            return new Integer(0); // to keep the type checker happy
        });
    }


    /**
     * Execute a report and export it. No options or input controls at the moment, since we're
     * using a trivial report.
     * @param uri The report's URI. The leading slash is optional.
     * @param format Export format. Also with or without dot.
     * @return
     * @throws IOException
     */
    private static byte[] runReportSimple(
            String uri,
            String format
            ) throws IOException {
        if (uri.length() > 0 && uri.charAt(0) != '/')
            uri = "/" + uri;
        if (format.charAt(0) != '.')
            format = "." + format;
        String url = constants.REST_REPORTS_URL + uri + format;
        BasicClassicHttpRequest request = new BasicClassicHttpRequest(Method.GET, url);
        byte[] exported = httpClient.execute(request, response -> {
            int code = response.getCode();
            if (code == 200) {
                try (InputStream reportStream = response.getEntity().getContent()) {
                    // apparently, they introduced InputStream#readAllBytes in java 9... we're on 1.8 :(
                    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                    int nRead;
                    byte[] bufferBuffer = new byte[4096];
                    while ((nRead = reportStream.read(bufferBuffer, 0, bufferBuffer.length))!= -1)
                        buffer.write(bufferBuffer, 0, nRead);
                    return buffer.toByteArray();
                }
            }
            if ((code == 400 || code == 404) && response.getEntity() != null) {
                try (InputStream reportStream = response.getEntity().getContent()) {
                    try (Scanner s = new Scanner(reportStream, StandardCharsets.UTF_8.name())){
                        logger.error(s.useDelimiter("\\A").next());
                    }
                }
            }
            throw new IOException("Run report: HTTP status:" + code);
        });
        return exported;
    }

    /**
     * Schedule a report job.
     * @param job
     * @return
     * @throws IOException
     */
    private static ClientReportJob scheduleJob(
            ClientReportJob job
            ) throws IOException {
        String url = constants.REST_JOBS_URL;
        BasicClassicHttpRequest request = new BasicClassicHttpRequest(Method.PUT, url);
        request.addHeader("Content-Type", "application/job+" + CONTENT_TYPE);
        String body = objectMapper.writeValueAsString(job);
        request.setEntity(new StringEntity(body));
        ClientReportJob createdJob = httpClient.execute(request, response -> {
            // API doc says it should be 201, but my tests produce 200.
            if (response.getCode() == 200)
                try (InputStream responseBody = response.getEntity().getContent()) {
                    return objectMapper.readValue(responseBody, ClientReportJob.class);
                }
            else if (response.getCode() == 400) {
                try (InputStream responseBody = response.getEntity().getContent()) {
                    try (Scanner s = new Scanner(responseBody, StandardCharsets.UTF_8.name())){
                        logger.error(s.useDelimiter("\\A").next());
                        throw new IOException("Bad API call");
                    }
                }
            } else {
                throw new IOException("HTTP status: expecting 200, got " + response.getCode());
            }
        });
        return createdJob;
    }

    /**
     * Start an export. All parameters can be specified through the ExportTask.
     * @param task
     * @return
     * @throws IOException
     */
    private static State startExportTask(String exportJson) throws IOException {
        String url = constants.REST_EXPORT_URL;
        BasicClassicHttpRequest request = new BasicClassicHttpRequest(Method.POST, url);
        request.addHeader("Content-Type", "application/" + CONTENT_TYPE);
        request.addHeader("Accept", "application/" + ACCEPT_TYPE);
        request.setEntity(new StringEntity(exportJson));
        State state = httpClient.execute(request, response -> {
            if (response.getCode() == 200)
                try (InputStream responseBody = response.getEntity().getContent()) {
                    return objectMapper.readValue(responseBody, State.class);
                }
            else {
                throw new IOException("HTTP status: expecting 200, got " + response.getCode());
            }
        });
        return state;
    }

    /**
     * Poll a running export task using its ID. Returns the State object.
     * @param exportId ID of a pending export task
     * @return
     * @throws IOException if the response was not 200 with a State object.
     */
    private static State pollExportTask(String exportId) throws IOException {
        return pollExportImportTask(constants.REST_EXPORT_URL, exportId, "state");
    }

    private static State pollExportImportTask(
            String baseUrl,
            String id,
            String stateNode
            ) throws IOException {
        String url = baseUrl + "/" + id + "/" + stateNode;
        BasicClassicHttpRequest request = new BasicClassicHttpRequest(Method.GET, url);
        request.addHeader("Accept", "application/" + ACCEPT_TYPE);
        State state = httpClient.execute(request, response -> {
            if (response.getCode() == 200)
                try (InputStream responseBody = response.getEntity().getContent()) {
                    return objectMapper.readValue(responseBody, State.class);
                }
            else {
                throw new IOException("HTTP status: expecting 200, got " + response.getCode());
            }
        });
        return state;
    }

    /**
     * Get the output of an export task as a byte array.
     * @param exportId
     * @return
     * @throws IOException
     */
    private static byte[] getExportOutput(String exportId, String zipFileName) throws IOException {
        String url = constants.REST_EXPORT_URL + "/" + exportId + "/" + zipFileName;
        BasicClassicHttpRequest request = new BasicClassicHttpRequest(Method.GET, url);
        byte[] zipFile = httpClient.execute(request, response -> {
            if (response.getCode() == 200) {
                try (InputStream reportStream = response.getEntity().getContent()) {
                    // apparently, they introduced InputStream#readAllBytes in java 9... we're on 1.8 :(
                    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                    int nRead;
                    byte[] bufferBuffer = new byte[4096];
                    while ((nRead = reportStream.read(bufferBuffer, 0, bufferBuffer.length))!= -1)
                        buffer.write(bufferBuffer, 0, nRead);
                    return buffer.toByteArray();
                }
            } else {
                throw new IOException("HTTP status: expecting 200, got " + response.getCode());
            }
        });
        return zipFile;
    }

    /** Start an import task. Unlike {@link #startExportTask(ExportTask)}, you cannot specify
     * any parameters.
     * 
     * @param keyAlias
     * @param exportZip
     * @return
     * @throws IOException
     */
    private static State startImportTask(byte[] exportZip) throws IOException {
        String url = constants.REST_IMPORT_URL;
        BasicClassicHttpRequest request = new BasicClassicHttpRequest(Method.POST, url);
        String contentType = "application/zip";
        request.addHeader("Content-Type", contentType);
        request.addHeader("Accept", "application/" + ACCEPT_TYPE);
        ByteArrayEntity body = new ByteArrayEntity(exportZip, ContentType.create(contentType));
        request.setEntity(body);
        State state = httpClient.execute(request, response -> {
            if (response.getCode() == 200)
                try (InputStream responseBody = response.getEntity().getContent()) {
                    return objectMapper.readValue(responseBody, State.class);
                }
            else {
                throw new IOException("HTTP status: expecting 200, got " + response.getCode());
            }
        });
        return state;
    }

    /**
     * Poll the status of an import task.
     * @param importId
     * @return
     * @throws IOException
     */
    private static State pollImportTask(String importId) throws IOException {
        return pollExportImportTask(constants.REST_IMPORT_URL, importId, "state");
    }
    // ================================ CONFIG VALUES ==========================
    /**
     * Get the JDBC Driver class to use for a data source that accesses the repository DB
     * (=> defined by dbType)
     * @return
     */
    private static String getRepositoryDBJdbcDriverClass() {
        switch(constants.DB_TYPE) {
        case "postgresql": return "org.postgresql.Driver";
        case "mysql": return "org.mariadb.jdbc.Driver";
        default: throw new IllegalArgumentException("Unknown dbType: " + constants.DB_TYPE);
        }
    }

    /**
     * Get the JDBC URL to use for a data source that accesses the repository DB
     * @return
     */
    private static String getRepositoryDBJdbcUrl() {
        JasperServerConstants c = constants;
        switch(c.DB_TYPE) {
        case "postgresql":
            return "jdbc:postgresql://" + c.DB_HOST + ":" + c.DB_PORT + "/" + c.DB_NAME;
        case "mysql":
            return "jdbc:mysql://" + c.DB_HOST + ":" + c.DB_PORT + "/" + c.DB_NAME + "?tinyInt1isBit=false";
        default: throw new IllegalArgumentException("Unknown dbType: " + constants.DB_TYPE);
        }
    }

    /**
     * Get the username to use for a data source that accesses the repository DB
     * @return
     */
    private static String getRepositoryDBUsername() {
        switch(constants.DB_TYPE) {
        case "postgresql":
        case "mysql":
            return constants.DB_USERNAME;
        default: throw new IllegalArgumentException("Unknown dbType: " + constants.DB_TYPE);
        }
    }

    /**
     * Get the password to use for a data source that accesses the repository DB
     * @return
     */
    private static String getRepositoryDBPassword() {
        switch(constants.DB_TYPE) {
        case "postgresql":
        case "mysql":
            return constants.DB_PASSWORD;
        default: throw new IllegalArgumentException("Unknown dbType: " + constants.DB_TYPE);
        }
    }

    /**
     * Get the contents of the jasperserver-users.jrxml report for upload to the server.
     * @return
     * @throws IOException
     */
    private String getJasperserverUsersJrxml() throws IOException {
        try (InputStream reportStream = this.getClass().getResourceAsStream(JASPERSERVER_USERS_JRXML)){
            try (Scanner reportScanner = new Scanner(reportStream, StandardCharsets.UTF_8.name())){
                return reportScanner.useDelimiter("\\A").next();
            }
        }
    }
}
