/*
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
package com.jaspersoft.jasperserver.api.metadata.common.service.impl.hibernate;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import com.jaspersoft.jasperserver.api.metadata.common.domain.ListOfValuesItem;
import com.jaspersoft.jasperserver.api.metadata.common.domain.ResourceLookup;
import com.jaspersoft.jasperserver.api.metadata.common.service.RepositoryService;
import com.jaspersoft.jasperserver.api.metadata.common.service.impl.hibernate.persistent.RepoListOfValuesItem;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.ReportUnit;
import com.jaspersoft.jasperserver.api.metadata.view.domain.FilterCriteria;

/**
 * Test class to an end point to connect across SOAP
 * @author tkavanagh 
 *
 */
public class TestListWrapper {

	protected static final Log log = LogFactory.getLog(TestListWrapper.class);
	
	private Properties jdbcProps;
	private RepositoryService repo;
	private StringBuffer strBuff = new StringBuffer();
	
	public String findReportUnits(String arg1) {
		
		try {
			setUp();
		
			log.warn("before findResource call: ");
			
			ResourceLookup[] units = repo.findResource(null, FilterCriteria.createFilter(ReportUnit.class));
						
			log.warn("After findResource call: units.length=" + units.length);
			
			// loop through the report units and pull out naming info
			if (units != null && units.length > 0) 
			{
				strBuff.append("\n\n<reportList>\n");
				for (int i = 0; i < units.length; i++) 
				{
					ResourceLookup unit = units[i];
					
					strBuff.append("\t<report>\n");
					strBuff.append("\t\t<name>" + unit.getName() + "</name>\n");
					strBuff.append("\t\t<label>" + unit.getLabel() + "</label>\n");
					strBuff.append("\t\t<description>" + unit.getDescription() + "</description>\n");
					strBuff.append("\t</report>\n");
				}
			} else {
				return "XYZ: units is null: ";
			}
			 
			strBuff.append("</reportList>\n");
			
			
		} catch (Exception e)  {
			log.warn("Caught exception: " + e.getMessage());
			//log.warn("Caught exception: stack trace" + e.getStackTrace());
			System.out.println("TestListWrapper: caught exception");
			System.out.println("exception: " + e.toString());
			e.printStackTrace();
		}
		
		return strBuff.toString();
	}
	
	
	protected void setUp() throws Exception {
		loadJdbcProps();

		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(new String[] {"viewService.xml"}, false);
		PropertySourcesPlaceholderConfigurer cfg = new PropertySourcesPlaceholderConfigurer();
		
		cfg.setProperties(jdbcProps);
		ctx.addBeanFactoryPostProcessor(cfg);
        ctx.refresh();

        repo = ctx.getBean("repoService", RepositoryService.class);
	}
	
	protected Properties loadJdbcProps() throws IOException, FileNotFoundException {
		jdbcProps = new Properties();
		String jdbcPropFile = System.getProperty("test.hibernate.jdbc.properties");
		//BufferedInputStream is = new BufferedInputStream(new FileInputStream("C:/Docume~1/tony/.m2/jdbc.properties"));
		BufferedInputStream is = new BufferedInputStream(new FileInputStream(jdbcPropFile));
		jdbcProps.load(is);
		is.close();
		return jdbcProps;
	}
	
}
