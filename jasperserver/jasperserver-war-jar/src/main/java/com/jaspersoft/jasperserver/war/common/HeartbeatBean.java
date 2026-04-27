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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import jakarta.servlet.ServletContext;
import javax.sql.DataSource;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.engine.util.JRSaver;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ProtocolException;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.springframework.web.context.ServletContextAware;

import com.jaspersoft.jasperserver.api.engine.common.service.EngineService;
import com.jaspersoft.jasperserver.api.metadata.common.service.RepositoryService;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.CustomReportDataSource;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.JdbcReportDataSource;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.JndiJdbcReportDataSource;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.ReportDataSource;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.service.ReportDataSourceService;
import com.jaspersoft.jasperserver.api.metadata.user.service.TenantService;
import com.jaspersoft.jasperserver.api.metadata.view.domain.FilterCriteria;
import com.jaspersoft.jasperserver.api.metadata.view.domain.FilterElementDisjunction;
import com.jaspersoft.jasperserver.api.metadata.view.domain.PropertyFilter;

/**
 * @author Teodor Danciu (teodord@users.sourceforge.net)
 * @version $Id: HibernateLoggingService.java 8408 2007-05-29 23:29:12Z melih $
 */
public class HeartbeatBean implements ServletContextAware, HeartbeatContributor
{
	private static final Log log = LogFactory.getLog(HeartbeatBean.class);
	
	private static final String PROPERTY_VERSION = "version";
	private static final String PROPERTY_HEARTBEAT_ID = "heartbeat.id";
	private static final String PROPERTY_PERMISSION_GRANTED = "permission.granted";

	private static final HeartbeatContributor OPTOUT_HEARTBEAT_CONTRIBUTOR = 
		new HeartbeatContributor()
		{
			public void contributeToHttpCall(HeartbeatCall call) 
			{
				call.addParameter("callCount", "-1");
			}
		};
	
	private ServletContext servletContext = null;
	private DataSource dataSource = null;
	private TenantService tenantService = null;
	private RepositoryService repositoryService = null;
	private EngineService engineService = null;
    private HeartbeatContributor awsEc2Contributor;
	private LocalesList localesList = null;
	private HeartbeatContributor optionalContributor = null;
	private List<String> customDSClassPatterns;

	private boolean enabled = false;
	private boolean askForPermission = false;
	private boolean permissionGranted = false;
	private String url = null;
	private int maxCacheSize = 0;
	private long cacheSaveInterval = 0;
	
	private String localId = null;
	private String heartbeatId = null;
	private int callCount = 0;
	private Properties localIdProperties = new Properties();
	private long lastCacheSaveTime = 0;

	private String osName = null;
	private String osVersion = null;
	private String javaVendor = null;
	private String javaVersion = null;
	private String serverInfo = null;
	private String productName = null;
	private String productVersion = null;
	private String location = null;
	private String dbName = null;
	private String dbVersion = null;

	private HeartbeatInfoCache clientInfoCache = new HeartbeatInfoCache();
	private HeartbeatInfoCache databaseInfoCache = new HeartbeatInfoCache();
	private HeartbeatInfoCache customDSInfoCache = new HeartbeatInfoCache();

	public void setServletContext(ServletContext servletContext)
	{
		this.servletContext = servletContext;
	}

	public void setTenantService(TenantService tenantService)
	{
		this.tenantService = tenantService;
	}

	public void setRepositoryService(RepositoryService repositoryService)
	{
		this.repositoryService = repositoryService;
	}

    public void setEngineService(EngineService engineService)
	{
		this.engineService = engineService;
	}

	public void setLocalesList(LocalesList localesList)
	{
		this.localesList = localesList;
	}

	public void setContributor(HeartbeatContributor contributor)
	{
		this.optionalContributor = contributor;
	}

	public void setDataSource(DataSource dataSource)
	{
		this.dataSource = dataSource;
	}

	public boolean getEnabled() 
	{
		return enabled;
	}

	public void setEnabled(boolean enabled) 
	{
		this.enabled = enabled;
	}

	public boolean getAskForPermission() 
	{
		return askForPermission;
	}

	public void setAskForPermission(boolean askForPermission) 
	{
		this.askForPermission = askForPermission;
	}

	public boolean getPermissionGranted() 
	{
		return permissionGranted;
	}

	public void setPermissionGranted(boolean permissionGranted) 
	{
		this.permissionGranted = permissionGranted;
	}

	public String getUrl() 
	{
		return url;
	}

	public void setUrl(String url) 
	{
		this.url = url;
	}

	public int getMaxCacheSize() 
	{
		return maxCacheSize;
	}

	public void setMaxCacheSize(int maxCacheSize) 
	{
		this.maxCacheSize = maxCacheSize;
	}

	public long getCacheSaveInterval() 
	{
		return cacheSaveInterval;
	}

	public void setCacheSaveInterval(long cacheSaveInterval) 
	{
		this.cacheSaveInterval = cacheSaveInterval;
	}

	public void setProductName(String productName) 
	{
		this.productName = productName;
	}

	public void setProductVersion(String productVersion) 
	{
		this.productVersion = productVersion;
	}

	public String getProductVersion() 
	{
		return productVersion;
	}

	private String getLocalId()
	{
		return localId;
	}
	
	private File getLocalIdFile()
	{
		File jsHomeDir = new File(new File(System.getProperty("user.home")), ".jasperserver");
		
		return new File(jsHomeDir, getLocalId());
	}
	
	private File getClientInfoCacheFile()
	{
		File jsHomeDir = new File(new File(System.getProperty("user.home")), ".jasperserver");
		
		return new File(jsHomeDir, getLocalId() + ".ser");
	}
	
	public void init()
	{
		if (enabled)
		{
			try
			{
				initHeartbeat();
			}
			catch(Exception e) // just protect the server from heartbeat initialization problems. 
			{
				if (log.isDebugEnabled())
					log.debug("Heartbeat initialization failed.", e);
			}
		}
	}
	
	private void initHeartbeat()
	{
		osName = System.getProperty("os.name");
		osVersion = System.getProperty("os.version");
		javaVendor = System.getProperty("java.vendor");
		javaVersion = System.getProperty("java.version");
		serverInfo = servletContext.getServerInfo();
		location = servletContext.getRealPath("/");
		dbName = null;
		dbVersion = null;

		Connection connection = null;

		try
		{
			connection = dataSource.getConnection();
			DatabaseMetaData metaData = connection.getMetaData();
			
			dbName = metaData.getDatabaseProductName();
			dbVersion = metaData.getDatabaseProductVersion();
		}
		catch (SQLException e)
		{
			if (log.isDebugEnabled())
				log.debug("Getting database metadata failed.", e);
		}
		finally
		{
			if (connection != null)
			{
				try
				{
					connection.close();
				}
				catch (SQLException e)
				{
				}
			}
		}
		
		String idSource = 
			//javaVendor + "|" 
			//+ javaVersion + "|" 
			serverInfo + "|" 
			+ productName + "|" 
			//+ productVersion + "|" 
			+ (location == null ? "" : location);
		
		MessageDigest messageDigest = null;
		try
		{
			messageDigest = MessageDigest.getInstance("MD5");
		}
		catch(NoSuchAlgorithmException e)
		{
			//heartbeat is always silent
		}
		
		if (messageDigest == null)
		{
			localId = String.valueOf(idSource.hashCode());
		}
		else
		{
			byte[] idBytes = messageDigest.digest(idSource.getBytes());
			StringBuffer idBuffer = new StringBuffer(2 * idBytes.length);
			for(int i = 0; i < idBytes.length; i++)
			{
				String hexa = Integer.toHexString(128 + idBytes[i]).toUpperCase();
				hexa = ("00" + hexa).substring(hexa.length());
				idBuffer.append(hexa);
			}
			localId = idBuffer.toString();
		}
		
		/*   */
		File localIdFile = getLocalIdFile();
		if (localIdFile.exists() && localIdFile.isFile())
		{
			localIdProperties = new Properties();

			FileInputStream fis = null;
			try
			{
				fis = new FileInputStream(localIdFile);
				localIdProperties.load(fis);
			}
			catch (IOException e)
			{
				if (log.isDebugEnabled())
					log.debug("Loading heartbeat local ID properties file failed.", e);
			}
			finally
			{
				if (fis != null)
				{
					try
					{
						fis.close();
					}
					catch (IOException e)
					{
					}
				}
			}
		}
		heartbeatId = localIdProperties.getProperty(PROPERTY_HEARTBEAT_ID);
		
		/*   */
		File clientInfoCacheFile = getClientInfoCacheFile();
		if (clientInfoCacheFile.exists() && clientInfoCacheFile.isFile())
		{
			try
			{
				clientInfoCache = (HeartbeatInfoCache)JRLoader.loadObject(clientInfoCacheFile);
			}
			catch (Exception e)
			{
				if (log.isDebugEnabled())
					log.debug("Loading heartbeat cache from serialized file failed.", e);
			}
		}
		lastCacheSaveTime = System.currentTimeMillis();
	}
	
	public boolean haveToAskForPermissionNow() 
	{
		return 
			enabled 
			&& getAskForPermission()
			&& (
				!localIdProperties.containsKey(PROPERTY_PERMISSION_GRANTED)
				|| (
					!Boolean.valueOf(localIdProperties.getProperty(PROPERTY_PERMISSION_GRANTED))
					&& !getProductVersion().equals(localIdProperties.getProperty(PROPERTY_VERSION))
					)
				);
	}
	
	public boolean isMakingCalls()
	{
		return
			enabled
			&& (
				(getAskForPermission() && Boolean.valueOf(localIdProperties.getProperty(PROPERTY_PERMISSION_GRANTED)).booleanValue())
				|| (!getAskForPermission() && getPermissionGranted())
				);
	}
	
	public synchronized void permitCall(boolean isCallPermitted) 
	{
		if (enabled)
		{
			localIdProperties.setProperty(PROPERTY_PERMISSION_GRANTED, String.valueOf(isCallPermitted));
			localIdProperties.setProperty(PROPERTY_VERSION, getProductVersion());
			
			if (heartbeatId != null)
			{
				//this is probably not needed since the heartbeat id is always set upon retrieval from URL
				localIdProperties.setProperty(PROPERTY_HEARTBEAT_ID, heartbeatId);
			}
			
			saveLocalIdProperties();
			
			//initial call after first login
			httpCall(getUrl(), isCallPermitted ? this : OPTOUT_HEARTBEAT_CONTRIBUTOR);
		}
	}
	
	public synchronized void updateClientInfo(HeartbeatClientInfo info)
	{
		if (isMakingCalls())
		{
			clientInfoCache.update(info);
			
			if (getMaxCacheSize() > 0 && clientInfoCache.size() > getMaxCacheSize())
			{
				httpCall(url, this);
			}
			else if (getCacheSaveInterval() > 0 && System.currentTimeMillis() - lastCacheSaveTime > getCacheSaveInterval())
			{
				saveCache();
			}
		}
	}
	
	public synchronized void call() 
	{
		if (enabled)
		{
			if (isMakingCalls())
			{
				createDatabaseInfoCache();
				createCustomDSInfoCache();
				
				httpCall(getUrl(), this);
			}
			else if (
				!getAskForPermission() 
				&& !getPermissionGranted() 
				&& !localIdProperties.containsKey(PROPERTY_PERMISSION_GRANTED)
				)
			{
				localIdProperties.setProperty(PROPERTY_PERMISSION_GRANTED, "false");
				saveLocalIdProperties();
				
				httpCall(getUrl(), OPTOUT_HEARTBEAT_CONTRIBUTOR);
			}
				
			callCount++;
		}
		else
		{
			if (log.isDebugEnabled())
				log.debug("Heartbeat is DISABLED.");
		}
	}
	
	private synchronized void httpCall(String url, HeartbeatContributor contributor) 
	{
		if (log.isDebugEnabled())
			log.debug("Heartbeat calling: " + url);
		
		CloseableHttpClient client = createHttpClient();
		try
		{
			HttpPost postRequest = new HttpPost(url);
			try
			{
				StandardHeartbeatCall call = new StandardHeartbeatCall();
				if (heartbeatId != null)
				{
					call.addParameter("id", heartbeatId);
				}

				if (contributor != null)
				{
					contributor.contributeToHttpCall(call);
				}
				
				UrlEncodedFormEntity postData = new UrlEncodedFormEntity(call.getParameters(), "UTF-8");
				postRequest.setEntity(postData);
				
				CloseableHttpResponse response = client.execute(postRequest);
				try
				{
					int statusCode = response.getStatusLine().getStatusCode();
					if (statusCode == HttpStatus.SC_OK)
					{
						String responseString = EntityUtils.toString(response.getEntity());
						if (heartbeatId == null)
						{
							heartbeatId = responseString == null ? null : responseString.trim();
							
							localIdProperties.setProperty(PROPERTY_HEARTBEAT_ID, heartbeatId);

							saveLocalIdProperties();
						}
					}
					else
					{
						EntityUtils.consume(response.getEntity());
						if (log.isDebugEnabled())
							log.debug("Connecting to heartbeat listener URL failed. Status code: " + statusCode);
					}
				}
				finally
				{
					response.close();
				}
			}
			catch (IOException e)
			{
				if (log.isDebugEnabled())
					log.debug("Connecting to heartbeat listener URL failed.", e);
			}
			finally
			{
				clearCache();
			}
		}
		finally
		{
			try
			{
				client.close();
			}
			catch (IOException e)
			{
				if (log.isWarnEnabled())
				{
					log.warn("Error while closing the heartbeat client", e);
				}
			}
		}
	}

	protected CloseableHttpClient createHttpClient()
	{
		HttpClientBuilder clientBuilder = HttpClients.custom();
		// https://www.jaspersoft.com/heartbeat/heartbeat.php redirects with 301, we want to redirect the POST
		clientBuilder.setRedirectStrategy(new LaxRedirectStrategy()
		{
			@Override
			public HttpUriRequest getRedirect(HttpRequest request,
					HttpResponse response, HttpContext context) throws ProtocolException
			{
				HttpUriRequest defaultRedirect = super.getRedirect(request, response, context);
				HttpUriRequest redirect = defaultRedirect;
				
		        String origMethod = request.getRequestLine().getMethod();
	            int status = response.getStatusLine().getStatusCode();
				// LaxRedirectStrategy changes to GET all POST redirects other than 307, see https://issues.apache.org/jira/browse/HTTPCLIENT-1248
				// what we want is reposting on redirect 
				if (origMethod.equalsIgnoreCase(HttpPost.METHOD_NAME))
				{
					if (defaultRedirect.getRequestLine().getMethod().equalsIgnoreCase(HttpGet.METHOD_NAME)
							&& (status == HttpStatus.SC_MOVED_PERMANENTLY || status == HttpStatus.SC_MOVED_TEMPORARILY
							|| status == HttpStatus.SC_SEE_OTHER || status == HttpStatus.SC_TEMPORARY_REDIRECT))
					{
						// create a repost request
						HttpUriRequest repostRequest = RequestBuilder.copy(request).setUri(defaultRedirect.getURI()).build();
						redirect = repostRequest;
					}
					
					// there's a bug in apache httpclient where a redirected POST preserves its generated headers,
					// causing org.apache.http.protocol.RequestContent.process to throw new ProtocolException("Content-Length header already present")
					// in theory we should restore original request headers here (via HttpRequestWrapper.getOriginal), but it our case there are none so we'll simply remove all headers
					redirect.setHeaders(new Header[0]);
				}
				
				return redirect;
			}
		});
		
		// single connection
		BasicHttpClientConnectionManager connManager = new BasicHttpClientConnectionManager();
		clientBuilder.setConnectionManager(connManager);
		
		// ignore cookies
		RequestConfig requestConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.IGNORE_COOKIES).build();
		clientBuilder.setDefaultRequestConfig(requestConfig);
		
		CloseableHttpClient client = clientBuilder.build();
		return client;
	}
	
	public void contributeToHttpCall(HeartbeatCall call)
	{
		call.addParameter("callCount", String.valueOf(callCount));
		call.addParameter("osName", osName);
		call.addParameter("osVersion", osVersion);
		call.addParameter("javaVendor", javaVendor);
		call.addParameter("javaVersion", javaVersion);
		call.addParameter("serverInfo", serverInfo);
		call.addParameter("productName", productName);
		call.addParameter("productVersion", productVersion);
		call.addParameter("dbName", dbName);
		call.addParameter("dbVersion", dbVersion);
		call.addParameter("serverLocale", Locale.getDefault().toString());

		try
		{
			call.addParameter("tenants", String.valueOf(tenantService.getNumberOfTenants(null)));
		}
		catch (Exception e)
		{
			if (log.isDebugEnabled())
				log.debug("Getting number of tenants failed.", e);
		}
		
		try
		{
			UserLocale[] userLocales = localesList.getUserLocales(Locale.getDefault());
			if (userLocales != null && userLocales.length > 0)
			{
				StringBuffer sbuffer = new StringBuffer();
				for(int i = 0; i < userLocales.length; i++)
				{
					sbuffer.append(", " + userLocales[i].getCode());
				}
				call.addParameter("userLocales", sbuffer.substring(2));
			}
		}
		catch (Exception e)
		{
			if (log.isDebugEnabled())
				log.debug("Getting user locales failed.", e);
		}
		
		clientInfoCache.contributeToHttpCall(call);

		databaseInfoCache.contributeToHttpCall(call);
		customDSInfoCache.contributeToHttpCall(call);

        awsEc2Contributor.contributeToHttpCall(call);

        if (optionalContributor != null)
		{
			optionalContributor.contributeToHttpCall(call);
		}
	}
	
	public void createDatabaseInfoCache()
	{
		databaseInfoCache = new HeartbeatInfoCache();
		
		List dataSources = new ArrayList();
		
		try
		{
			List jdbcDataSources = 
				repositoryService.loadClientResources(
					FilterCriteria.createFilter(JdbcReportDataSource.class)
					);
			if (jdbcDataSources != null)
				dataSources.addAll(jdbcDataSources);
		}
		catch (Exception e)
		{
			if (log.isDebugEnabled())
				log.debug("Getting JDBC data sources list failed.", e);
		}
			
		try
		{
			List jndiDataSources = 
				repositoryService.loadClientResources(
					FilterCriteria.createFilter(JndiJdbcReportDataSource.class)
					);
			if (jndiDataSources != null)
				dataSources.addAll(jndiDataSources);
		}
		catch (Exception e)
		{
			if (log.isDebugEnabled())
				log.debug("Getting JNDI data sources list failed.", e);
		}

		for(Iterator it = dataSources.iterator(); it.hasNext();)
		{
			ReportDataSource dataSource = (ReportDataSource)it.next();

			Map paramValues = new HashMap();
			
			try
			{
				ReportDataSourceService dataSourceService = engineService.createDataSourceService(dataSource);
				dataSourceService.setReportParameterValues(paramValues);
			}
			catch(Exception e)
			{
				if (log.isDebugEnabled())
					log.debug("Getting connection to data source failed.", e);
			}
			
			Connection connection = (Connection)paramValues.get(JRParameter.REPORT_CONNECTION);
			if (connection != null)
			{
				try
				{
					DatabaseMetaData metaData = connection.getMetaData();
					
					HeartbeatDatabaseInfo dbInfo = new HeartbeatDatabaseInfo();
					dbInfo.setDatabaseName(metaData.getDatabaseProductName());
					dbInfo.setDatabaseVersion(metaData.getDatabaseProductVersion());
					databaseInfoCache.update(dbInfo);
				}
				catch (SQLException e)
				{
					if (log.isDebugEnabled())
						log.debug("Getting database metadata failed.", e);
				}
				finally
				{
					if (connection != null)
					{
						try
						{
							connection.close();
						}
						catch (SQLException e)
						{
						}
					}
				}
			}
		}
	}
	
	public void createCustomDSInfoCache()
	{
		customDSInfoCache = new HeartbeatInfoCache();
		
		if (customDSClassPatterns != null && !customDSClassPatterns.isEmpty())
		{
			try
			{
				if (log.isDebugEnabled())
				{
					log.debug("fetching custom DS information for patterns " + customDSClassPatterns);
				}
				
				FilterCriteria filter = FilterCriteria.createFilter(CustomReportDataSource.class);
				FilterElementDisjunction classFilters = filter.addDisjunction();
				for (String classPattern : customDSClassPatterns)
				{
					PropertyFilter classFilter = FilterCriteria.createPropertyLikeFilter("serviceClass", classPattern);
					classFilters.addFilterElement(classFilter);
				}
				
				List<?> dataSources = repositoryService.loadClientResources(filter);
				if (dataSources != null && !dataSources.isEmpty())
				{
					for(Iterator<?> it = dataSources.iterator(); it.hasNext();)
					{
						CustomReportDataSource dataSource = (CustomReportDataSource) it.next();

						HeartbeatCustomDSInfo customDSInfo = new HeartbeatCustomDSInfo();
						customDSInfo.setServiceClass(dataSource.getServiceClass());
						customDSInfoCache.update(customDSInfo);
					}
				}
			}
			catch (Exception e)
			{
				if (log.isDebugEnabled())
				{
					log.debug("Getting custom data sources list failed.", e);
				}
			}
		}
	}
	
	private synchronized void saveLocalIdProperties() 
	{
		File localIdFile = getLocalIdFile();
		if (!localIdFile.exists())
		{
			localIdFile.getParentFile().mkdirs();
		}
		
		FileOutputStream fos = null;
		
		try
		{
			fos = new FileOutputStream(localIdFile);
			localIdProperties.store(fos, "heartbeat local ID file");
			fos.flush();
		}
		catch (IOException e)
		{
			if (log.isDebugEnabled())
				log.debug("Creating heartbeat local ID properties file failed.", e);
		}
		finally
		{
			if (fos != null)
			{
				try
				{
					fos.close();
				}
				catch (IOException e)
				{
				}
			}
		}
	}
	
	private synchronized void saveCache() 
	{
		File clientInfoCacheFile = getClientInfoCacheFile();
		if (!clientInfoCacheFile.exists())
		{
			clientInfoCacheFile.getParentFile().mkdirs();
		}
		
		try
		{
			JRSaver.saveObject(clientInfoCache, clientInfoCacheFile);
			lastCacheSaveTime = System.currentTimeMillis();
		}
		catch (JRException e)
		{
			if (log.isDebugEnabled())
				log.debug("Saving heartbeat cache failed.", e);
		}
	}
	
	private synchronized void clearCache() 
	{
		clientInfoCache = new HeartbeatInfoCache();
		getClientInfoCacheFile().delete();
		lastCacheSaveTime = System.currentTimeMillis();
	}

	public List<String> getCustomDSClassPatterns()
	{
		return customDSClassPatterns;
	}

	public void setCustomDSClassPatterns(List<String> customDSClassPatterns)
	{
		this.customDSClassPatterns = customDSClassPatterns;
	}

    public void setAwsEc2Contributor(HeartbeatContributor awsEc2Contributor) {
        this.awsEc2Contributor = awsEc2Contributor;
    }
}

