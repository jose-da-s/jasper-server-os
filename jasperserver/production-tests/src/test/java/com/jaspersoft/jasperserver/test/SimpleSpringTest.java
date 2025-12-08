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
package com.jaspersoft.jasperserver.test;

import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import javax.annotation.Resource;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.jaspersoft.jasperserver.test.MyDumbBean;
import com.jaspersoft.jasperserver.test.MySmartBean;

/**
 * SimpleSpringTest class
 *
 * @author Steve Rosen
 */

@ContextConfiguration(locations={"classpath:simpleSpringTestContext.xml"})
public class SimpleSpringTest extends AbstractTestNGSpringContextTests {

    protected final static Log m_logger = LogFactory.getLog(SimpleSpringTest.class);

    private static MyDumbBean  m_dumb_bean;
    private static MySmartBean m_smart_bean;

    public void setMyDumbBean(MyDumbBean dumb_bean)
    {
        m_logger.info("setMyDumbBean() has been called, dumb_bean = " + dumb_bean );
        m_dumb_bean = dumb_bean;
    }

    @Resource(name = "simpleSpringTestSmartBeanResource")
    public void setMySmartBean(MySmartBean smart_bean)
    {
        m_logger.info("setMySmartBean() has been called, smart_bean = " + smart_bean );
        m_smart_bean = smart_bean;
    }

    /**
     * methodBeforeClass
     *
     * This method will get run BEFORE any of the @Test methods in this class
     */
    @BeforeClass()
    public void methodBeforeClass()
    {
        m_logger.info("methodBeforeClass() has been called...");
        m_logger.info("methodBeforeClass() ...m_dumb_bean  = " + m_dumb_bean );
        m_logger.info("methodBeforeClass() ...m_smart_bean = " + m_smart_bean );
    }

    /**
     * methodAfterClass
     *
     * This method will get run AFTER any of the @Test methods in this class
     */
    @AfterClass()
    public void methodAfterClass()
    {
        m_logger.info("methodAfterClass() has been called...");
        m_logger.info("methodAfterClass() ...m_dumb_bean  = " + m_dumb_bean );
        m_logger.info("methodAfterClass() ...m_smart_bean = " + m_smart_bean );
    }

    /**
     * main
     * 
     * The main test driver for the simple spring test
     * 
     * @param   args
     *          the command line arguments to this unit test driver
     *          
     * @return  nothing
     */
    @Test
    @org.testng.annotations.Parameters({ "simple-spring-test-input" })
    public void mainSpringTest(String inputParam) {

        // introduce ourselves...
        m_logger.info("mainSpringTest() has been called, inputParam = " + inputParam);

        // time to say goodbye...
        m_logger.info("mainSpringTest() is all done and on its way out!");
    }
}
