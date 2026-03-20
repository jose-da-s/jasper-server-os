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

package com.jaspersoft.jasperserver.war.common;


import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import static com.jaspersoft.jasperserver.war.common.JavascriptOptimizationSettings.OPTIMIZE_JAVASCRIPT_SESSION_PARAM;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Olexandr Dahno <odahno@tibco.com>
 */
class JavascriptOptimizationSettingsTest {

    private static final String TEST_OPTIMIZED_JAVASCRIPT_PATH = "TEST_OPTIMIZED_JAVASCRIPT_PATH";
    private static final String TEST_PATH = "TEST_PATH";
    private JavascriptOptimizationSettings objectUnderTest = new JavascriptOptimizationSettings();

    @AfterEach
    void setup() {
        resetRequestAttribute();
    }

    @Nested
    @DisplayName("accessors and mutators")
    class GetAndSet {
        @Test
        void getAndSet_instanceWithDefaultValues() {
            final JavascriptOptimizationSettings instance = new JavascriptOptimizationSettings();

            assertAll("an instance with default values",
                    new Executable() {
                        @Override
                        public void execute() {
                            assertNull(instance.getOptimizedJavascriptPath());
                        }
                    });
        }

        @Test
        void getAndSet_fullyConfiguredInstance() {
            final JavascriptOptimizationSettings instance = new JavascriptOptimizationSettings();
            instance.setOptimizedJavascriptPath(TEST_OPTIMIZED_JAVASCRIPT_PATH);

            assertAll("a fully configured instance",
                    new Executable() {
                        @Override
                        public void execute() {
                            assertEquals(TEST_OPTIMIZED_JAVASCRIPT_PATH, instance.getOptimizedJavascriptPath());
                        }
                    });
        }
    }

    @Nested
    @DisplayName("get runtime hash")
    class getRuntimeHash {
        @Test
        void getRuntimeHash_returnNotNullValue() {
            String runtimeHash = objectUnderTest.getRuntimeHash();

            assertNotNull(runtimeHash);
        }

        @Test
        void getRuntimeHash_returnEqualValuesOnConsecutiveCalls() {
            String firstCallRuntimeHash = objectUnderTest.getRuntimeHash();
            String secondCallRuntimeHash = objectUnderTest.getRuntimeHash();

            assertEquals(firstCallRuntimeHash, secondCallRuntimeHash);
        }
    }

    @Nested
    @DisplayName("regenerate runtime hash")
    class reGenerateRuntimeHash {
        @Test
        void reGenerateRuntimeHash_returnNotNullValue() {
            objectUnderTest.reGenerateRuntimeHash();
            String runtimeHash = objectUnderTest.getRuntimeHash();

            assertNotNull(runtimeHash);
        }

        @Test
        void reGenerateRuntimeHash_returnNewHash() {
            String beforeRehashing = objectUnderTest.getRuntimeHash();
            objectUnderTest.reGenerateRuntimeHash();
            String afterRehashing = objectUnderTest.getRuntimeHash();

            assertNotEquals(beforeRehashing, afterRehashing);
        }
    }

    @Nested
    @DisplayName("get using optimized javascript")
    class getUseOptimizedJavascript {
        @Test
        void getUseOptimizedJavascript_requestAttributesIsNullAndUseOptimizedJSIsNull_null() {
            Boolean useOptimizedJavascript = objectUnderTest.getUseOptimizedJavascript();
            assertNull(useOptimizedJavascript);
        }

        @Test
        void getUseOptimizedJavascript_requestAttributesIsNullAndUseOptimizedJSIsTrue_true() {
            objectUnderTest.setUseOptimizedJavascript(true);

            Boolean useOptimizedJavascript = objectUnderTest.getUseOptimizedJavascript();
            assertTrue(useOptimizedJavascript);
        }

        @Test
        void getUseOptimizedJavascript_requestAttributesIsNullAndUseOptimizedJSIsFalse_false() {
            objectUnderTest.setUseOptimizedJavascript(false);

            Boolean useOptimizedJavascript = objectUnderTest.getUseOptimizedJavascript();
            assertFalse(useOptimizedJavascript);
        }

        @Test
        void getUseOptimizedJavascript_requestAttributesIsNullAndUseOptimizedJSIsNullAndOptimizeIsTrue_true() {
            setRequestAttributes(true);

            Boolean useOptimizedJavascript = objectUnderTest.getUseOptimizedJavascript();
            assertTrue(useOptimizedJavascript);
        }

        @Test
        void getUseOptimizedJavascript_requestAttributesIsNullAndUseOptimizedJSIsNullAndOptimizeIsFalse_false() {
            setRequestAttributes(false);

            Boolean useOptimizedJavascript = objectUnderTest.getUseOptimizedJavascript();
            assertFalse(useOptimizedJavascript);
        }

        @Test
        void getUseOptimizedJavascript_requestAttributesIsNullAndUseOptimizedJSIsNullAndOptimizeIsNull_throws() {
            setRequestAttributes(null);

            Assertions.assertThrows(NullPointerException.class, new Executable() {
                @Override
                public void execute() {
                    objectUnderTest.getUseOptimizedJavascript();
                }
            });
        }

        @Test
        void getUseOptimizedJavascript_requestAttributesIsNullAndUseOptimizedJSIsFalseAndOptimizeIsTrue_null() {
            setRequestAttributes(null);

            objectUnderTest.setUseOptimizedJavascript(false);

            Boolean useOptimizedJavascript = objectUnderTest.getUseOptimizedJavascript();
            assertNull(useOptimizedJavascript);
        }

        @Test
        void getUseOptimizedJavascript_requestAttributesIsNullAndUseOptimizedJSIsTrueAndOptimizeIsTrue_null() {
            setRequestAttributes(null);

            objectUnderTest.setUseOptimizedJavascript(true);

            Boolean useOptimizedJavascript = objectUnderTest.getUseOptimizedJavascript();
            assertNull(useOptimizedJavascript);
        }

        @Test
        void getUseOptimizedJavascript_requestAttributesIsNullAndUseOptimizedJSIsNullAndOptimizeIsTrueAndServletContextIsNull_null() {
            setRequestAttributes(null, false);

            objectUnderTest.setUseOptimizedJavascript(true);

            Boolean useOptimizedJavascript = objectUnderTest.getUseOptimizedJavascript();
            assertNull(useOptimizedJavascript);
        }

        @Test
        void getUseOptimizedJavascript_requestAttributesIsNullAndUseOptimizedJSIsNullAndOptimizeIsTrueAndServletContextIsSomeValue_null() {
            setRequestAttributes(null, true);

            objectUnderTest.setUseOptimizedJavascript(true);

            Boolean useOptimizedJavascript = objectUnderTest.getUseOptimizedJavascript();
            assertNull(useOptimizedJavascript);
        }

        @Test
        void getUseOptimizedJavascript_requestAttributesIsNullAndUseOptimizedJSIsNullAndOptimizeIsTrueAndServletContextIsNullAndPathIsSomeValue_null() {
            setRequestAttributes(null, true, TEST_PATH);

            objectUnderTest.setUseOptimizedJavascript(true);

            Boolean useOptimizedJavascript = objectUnderTest.getUseOptimizedJavascript();
            assertNull(useOptimizedJavascript);
        }

        @Test
        void getUseOptimizedJavascript_requestAttributesIsNullAndUseOptimizedJSIsNullAndOptimizeIsTrueAndServletContextIsNullAndPathIsNull_null() {
            setRequestAttributes(null, true, null);

            objectUnderTest.setUseOptimizedJavascript(true);

            Boolean useOptimizedJavascript = objectUnderTest.getUseOptimizedJavascript();
            assertNull(useOptimizedJavascript);
        }
    }

    /*
     * Helpers
     */

    private void setRequestAttributes(Boolean optimizeValue) {
        setRequestAttributes(optimizeValue, false);
    }

    private void setRequestAttributes(Boolean optimizeValue, boolean withServletContext) {
        setRequestAttributes(optimizeValue, withServletContext, null);
    }

    private void setRequestAttributes(Boolean optimizeValue, boolean withServletContext, String path) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        ServletRequestAttributes attributes = new ServletRequestAttributes(request);
        HttpSession session = mock(HttpSession.class);
        ServletContext servletContext = mock(ServletContext.class);

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute(OPTIMIZE_JAVASCRIPT_SESSION_PARAM)).thenReturn(optimizeValue);

        if (withServletContext) {
            when(session.getServletContext()).thenReturn(servletContext);
            if (path != null) {
                when(servletContext.getRealPath(anyString())).thenReturn(path);
            }
        }

        RequestContextHolder.setRequestAttributes(attributes);
    }

    private void resetRequestAttribute() {
        RequestContextHolder.setRequestAttributes(null);
    }

}

