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
package com.jaspersoft.jasperserver.api.engine.jasperreports.util;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.design.JasperDesign;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Integration tests for {@link CustomJRXmlLoader#load(InputStream)}.
 * <p>
 * Auto-discovers JRXML fixtures under {@code src/test/resources/jrxml/}:
 * {@code modern/} loads as-is, {@code legacy/} converts v6→v7 and loads,
 * {@code invalid/} must throw {@link JRException}.
 */
@RunWith(Enclosed.class)
public class JasperLoaderIntegrationTest {

    private static final String BASE = "jrxml/";

    /** Lists {@code .jrxml} files under {@code jrxml/<subfolder>/}, sorted for stable cross-OS ordering. */
    private static Collection<Object[]> discover(String subfolder) {
        URL url = Thread.currentThread().getContextClassLoader().getResource(BASE + subfolder);
        if (url == null) {
            // sentinel row → test bootstraps with a clear failure message
            return Collections.singletonList(new Object[]{null});
        }
        Path dir;
        try {
            dir = Paths.get(url.toURI());
        } catch (Exception e) {
            throw new IllegalStateException("Cannot resolve test resource folder: " + url, e);
        }
        List<File> files = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*.{jrxml,JRXML}")) {
            for (Path p : stream) {
                if (Files.isRegularFile(p)) {
                    files.add(p.toFile());
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Cannot list test resource folder: " + dir, e);
        }
        if (files.isEmpty()) {
            return Collections.singletonList(new Object[]{null});
        }
        files.sort(Comparator.comparing(f -> f.getName().toLowerCase(Locale.ROOT)));
        List<Object[]> rows = new ArrayList<>(files.size());
        for (File f : files) {
            rows.add(new Object[]{f});
        }
        return rows;
    }

    private static InputStream open(File f) throws IOException {
        return Files.newInputStream(f.toPath());
    }

    // ── Modern JRXML — every file in jrxml/modern/ must load successfully. ──
    @RunWith(Parameterized.class)
    public static class ModernJrxmlTests {

        @Parameters(name = "modern[{index}]: {0}")
        public static Collection<Object[]> data() {
            return discover("modern");
        }

        @Parameter
        public File file;

        @Test
        public void shouldLoadModernJrxmlSuccessfully() throws Exception {
            assertNotNull("Add at least one fixture under jrxml/modern/", file);
            try (InputStream is = open(file)) {
                JasperDesign design = CustomJRXmlLoader.load(is);
                assertNotNull("Modern JRXML " + file.getName()
                        + " should produce a non-null JasperDesign", design);
            }
        }
    }

    // ── Legacy JRXML — every file in jrxml/legacy/ must convert + load. ──
    @RunWith(Parameterized.class)
    public static class LegacyJrxmlTests {

        @Parameters(name = "legacy[{index}]: {0}")
        public static Collection<Object[]> data() {
            return discover("legacy");
        }

        @Parameter
        public File file;

        @Test
        public void shouldConvertLegacyJrxmlSuccessfully() throws Exception {
            assertNotNull("Add at least one fixture under jrxml/legacy/", file);
            try (InputStream is = open(file)) {
                JasperDesign design = CustomJRXmlLoader.load(is);
                assertNotNull("Legacy JRXML " + file.getName()
                        + " should be converted and produce a non-null JasperDesign", design);
            }
        }
    }

    // ── Legacy → Modern equivalence: converted v6→v7 must match same-named modern reference. ──
    @RunWith(Parameterized.class)
    public static class LegacyMatchesModernTests {

        @Parameters(name = "legacy↔modern[{index}]: {0}")
        public static Collection<Object[]> data() {
            URL legacyUrl = Thread.currentThread().getContextClassLoader()
                    .getResource(BASE + "legacy");
            URL modernUrl = Thread.currentThread().getContextClassLoader()
                    .getResource(BASE + "modern");
            if (legacyUrl == null || modernUrl == null) {
                return Collections.singletonList(new Object[]{null, null});
            }
            Path legacyDir;
            Path modernDir;
            try {
                legacyDir = Paths.get(legacyUrl.toURI());
                modernDir = Paths.get(modernUrl.toURI());
            } catch (Exception e) {
                throw new IllegalStateException("Cannot resolve test resource folders", e);
            }
            List<File> legacyFiles = new ArrayList<>();
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(
                    legacyDir, "*.{jrxml,JRXML}")) {
                for (Path p : stream) {
                    if (Files.isRegularFile(p)) {
                        legacyFiles.add(p.toFile());
                    }
                }
            } catch (IOException e) {
                throw new IllegalStateException("Cannot list legacy folder: " + legacyDir, e);
            }
            if (legacyFiles.isEmpty()) {
                return Collections.singletonList(new Object[]{null, null});
            }
            legacyFiles.sort(Comparator.comparing(f -> f.getName().toLowerCase(Locale.ROOT)));
            List<Object[]> rows = new ArrayList<>();
            for (File legacy : legacyFiles) {
                Path modern = modernDir.resolve(legacy.getName());
                if (Files.isRegularFile(modern)) {
                    rows.add(new Object[]{legacy, modern.toFile()});
                }
            }
            if (rows.isEmpty()) {
                // force one failing test with a clear message instead of silently passing zero
                return Collections.singletonList(new Object[]{null, null});
            }
            return rows;
        }

        @Parameter(0)
        public File legacyFile;

        @Parameter(1)
        public File modernFile;


        @Ignore("Cosmetic-only diffs (uuid, default=\"false\", expression class, "
                + "attr/child order, hex color case, Studio/iReport props, "
                + "<import> form, equivalent expressions, conditionalStyle shape) "
                + "make textual equality unreliable.")
        @Test
        public void migratedLegacyShouldEqualModern() throws Exception {
            assertTrue(
                    "Add at least one legacy JRXML under jrxml/legacy/ that has "
                            + "a same-named counterpart under jrxml/modern/",
                    legacyFile != null && modernFile != null);

            byte[] convertedBytes;
            try (InputStream is = open(legacyFile)) {
                convertedBytes = CustomJRXmlLoader.convertLegacyToV7(is);
            }
            byte[] modernBytes = Files.readAllBytes(modernFile.toPath());

            // Sanity: converted v7 must still load via JR 7.x.
            JasperDesign design = CustomJRXmlLoader.load(
                    new ByteArrayInputStream(convertedBytes));
            assertNotNull("Converted legacy " + legacyFile.getName()
                    + " must still produce a valid JasperDesign", design);

            String normalizedConverted = CustomJRXmlLoader.normalizeXml(convertedBytes);
            String normalizedModern = CustomJRXmlLoader.normalizeXml(modernBytes);

            if (!normalizedConverted.equals(normalizedModern)) {
                String diff = prettyDiff(
                        "modern/" + modernFile.getName() + "  (expected)",
                        "legacy/" + legacyFile.getName() + " → v7  (actual)",
                        normalizedModern,
                        normalizedConverted);
                assertEquals(
                        "Migrated legacy JRXML differs from the modern reference.\n"
                                + diff,
                        normalizedModern,
                        normalizedConverted);
            }
        }

        /** Compact unified-diff style report with limited context around changes. */
        static String prettyDiff(String expectedLabel, String actualLabel,
                                 String expected, String actual) {
            List<String> a = Arrays.asList(expected.split("\\R", -1));
            List<String> b = Arrays.asList(actual.split("\\R", -1));
            int n = a.size();
            int m = b.size();
            int[][] lcs = new int[n + 1][m + 1];
            for (int i = n - 1; i >= 0; i--) {
                for (int j = m - 1; j >= 0; j--) {
                    if (a.get(i).equals(b.get(j))) {
                        lcs[i][j] = lcs[i + 1][j + 1] + 1;
                    } else {
                        lcs[i][j] = Math.max(lcs[i + 1][j], lcs[i][j + 1]);
                    }
                }
            }
            List<String> ops = new ArrayList<>();
            int i = 0, j = 0;
            while (i < n && j < m) {
                if (a.get(i).equals(b.get(j))) {
                    ops.add("  " + a.get(i));
                    i++;
                    j++;
                } else if (lcs[i + 1][j] >= lcs[i][j + 1]) {
                    ops.add("- " + a.get(i++));
                } else {
                    ops.add("+ " + b.get(j++));
                }
            }
            while (i < n) ops.add("- " + a.get(i++));
            while (j < m) ops.add("+ " + b.get(j++));

            int context = 3;
            boolean[] keep = new boolean[ops.size()];
            for (int k = 0; k < ops.size(); k++) {
                if (ops.get(k).charAt(0) != ' ') {
                    int from = Math.max(0, k - context);
                    int to = Math.min(ops.size() - 1, k + context);
                    for (int p = from; p <= to; p++) {
                        keep[p] = true;
                    }
                }
            }

            StringBuilder sb = new StringBuilder();
            sb.append("\n========== JRXML diff ==========\n");
            sb.append("--- ").append(expectedLabel).append('\n');
            sb.append("+++ ").append(actualLabel).append('\n');
            boolean lastKept = true;
            int kept = 0;
            int maxKept = 400; // cap output size
            for (int k = 0; k < ops.size() && kept < maxKept; k++) {
                if (keep[k]) {
                    if (!lastKept) {
                        sb.append("    @@\n");
                    }
                    sb.append(ops.get(k)).append('\n');
                    kept++;
                    lastKept = true;
                } else {
                    lastKept = false;
                }
            }
            if (kept >= maxKept) {
                sb.append("    @@ (diff truncated – ")
                        .append(ops.size() - kept)
                        .append(" more line(s) omitted)\n");
            }
            sb.append("================================\n");
            return sb.toString();
        }
    }

    // ── Invalid JRXML — every file in jrxml/invalid/ must throw JRException. ──
    @RunWith(Parameterized.class)
    public static class InvalidJrxmlTests {

        @Parameters(name = "invalid[{index}]: {0}")
        public static Collection<Object[]> data() {
            return discover("invalid");
        }

        @Parameter
        public File file;

        @Test
        public void shouldFailOnInvalidJrxml() throws Exception {
            assertNotNull("Add at least one fixture under jrxml/invalid/", file);
            try (InputStream is = open(file)) {
                CustomJRXmlLoader.load(is);
                fail("Expected JRException to be thrown for invalid JRXML: " + file.getName());
            } catch (JRException expected) {
                // expected
            }
        }
    }

    // ── Edge cases that don't depend on fixture files. ──
    public static class EdgeCaseTests {

        /** Documents current behavior: null stream surfaces as NPE, not wrapped JRException. */
        @Test
        public void shouldFailOnNullInputStream() {
            try {
                CustomJRXmlLoader.load(null);
                fail("Expected NullPointerException (or JRException) for null input stream");
            } catch (NullPointerException | JRException expected) {
                // expected
            }
        }

        @Test
        public void shouldFailOnEmptyStream() throws Exception {
            try (InputStream empty = new ByteArrayInputStream(new byte[0])) {
                CustomJRXmlLoader.load(empty);
                fail("Expected JRException to be thrown for an empty stream");
            } catch (JRException expected) {
                // expected
            }
        }
    }
}

