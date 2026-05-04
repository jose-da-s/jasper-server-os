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
package com.jaspersoft.jasperserver.war.control;

import com.jaspersoft.jasperserver.api.common.domain.impl.ExecutionContextImpl;
import com.jaspersoft.jasperserver.api.common.util.StaticExecutionContextProvider;
import com.jaspersoft.jasperserver.api.common.util.TimeZoneContextHolder;
import com.jaspersoft.jasperserver.api.common.util.TimeZonesList;
import com.jaspersoft.jasperserver.api.engine.scheduling.service.ReportSchedulingService;
import com.jaspersoft.jasperserver.api.metadata.common.domain.Resource;
import com.jaspersoft.jasperserver.api.metadata.common.service.RepositoryService;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.ReportUnit;
import com.jaspersoft.jasperserver.api.metadata.user.domain.ProfileAttribute;
import com.jaspersoft.jasperserver.api.metadata.user.service.ProfileAttributeCategory;
import com.jaspersoft.jasperserver.api.metadata.user.service.ProfileAttributeService;
import com.jaspersoft.jasperserver.dto.common.ExportType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * @author Chaim Arbiv
 * @version $id$
 * Controller to handle all the scheduling interaction.
 * It should not depend to Web-Flow and use only the REST v2 end points.
 */
@Controller
public class SchedulerController {

    private static final Log log = LogFactory.getLog(SchedulerController.class);

    public static final String REPORT_PARAMETER_NAME = "reportUnitURI";
    public static final String PARENT_REPORT_PARAMETER_NAME = "parentReportUnitURI";

    private TimeZonesList timezones;
    private ReportSchedulingService scheduler;

    @Autowired(required=true)
    private HttpServletRequest request;

    private RepositoryService repository;
    private ProfileAttributeService profileAttributeService;

    private boolean enableSaveToHostFS;
    private boolean enableDataSnapshot;
    private Map reportJobDefaults = new HashMap();
    private List<String> availableReportJobOutputFormats;
    private List<String> availableDashboardJobScreenshotOutputFormats;
    private List<String> availableDashboardJobDetailedOutputFormats;

    private static final String LICENSE_MANAGER = "com.jaspersoft.ji.license.LicenseManager";

    @RequestMapping("/scheduler/main.html")
    public ModelAndView main(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("modules/reportScheduling/main");
        mav.addObject("timezone", TimeZoneContextHolder.getTimeZone().getID());
        mav.addObject("isPro", isProVersion());
        mav.addObject("userTimezones", timezones.getTimeZones(request.getLocale()));

        mav.addObject("enableSaveToHostFS", getEnableSaveToHostFS());
        mav.addObject("enableDataSnapshot", getEnableDataSnapshot());
        mav.addObject("reportJobDefaults", new JSONObject(getResolvedReportJobDefaults()));
        mav.addObject("availableReportJobOutputFormats", availableReportJobOutputFormatsJSON());
        mav.addObject("availableDashboardJobOutputFormats", availableDashboardJobOutputFormatsJSON());
        mav.addObject("controlsDisplayForm", getParametersForm(getReportUri(request)));

        return mav;
    }

    private JSONArray availableReportJobOutputFormatsJSON() throws JSONException {
        JSONArray categories = new JSONArray();
        categories.put(jobOutputFormatsCategoryJSON(ExportType.DEFAULT.name(), getAvailableReportJobOutputFormats()));
        return categories;
    }

    private JSONArray availableDashboardJobOutputFormatsJSON() throws JSONException {
        JSONArray categories = new JSONArray();
        categories.put(jobOutputFormatsCategoryJSON(ExportType.DEFAULT.name(), getAvailableDashboardJobScreenshotOutputFormats()));
        categories.put(jobOutputFormatsCategoryJSON(ExportType.DASHBOARD_DETAILED.name(), getAvailableDashboardJobDetailedOutputFormats()));
        return categories;
    }

	public JSONObject jobOutputFormatsCategoryJSON(String exportType, List<String> formats) throws JSONException {
		JSONObject categoryObject = new JSONObject();
		categoryObject.put("exportType", exportType);
		categoryObject.put("formats", new JSONArray(formats));
		return categoryObject;
	}

    private String getReportUri(HttpServletRequest request){
        String reportURI = request.getParameter(PARENT_REPORT_PARAMETER_NAME);

        if (reportURI == null || reportURI.isEmpty()) {
            reportURI = request.getParameter(REPORT_PARAMETER_NAME);
        }

        return reportURI;
    }

    private String getParametersForm(String reportUnitUri) {
        if (reportUnitUri == null) {
            return null;
        }

        Resource reportUnit = repository.getResource(StaticExecutionContextProvider.getExecutionContext(), reportUnitUri);

        if (reportUnit instanceof ReportUnit){
            return ((ReportUnit)reportUnit).getInputControlRenderingView();
        }

        return null;
    }

    public ReportSchedulingService getScheduler() {
        return scheduler;
    }

    public void setScheduler(ReportSchedulingService scheduler) {
        this.scheduler = scheduler;
    }

    /**
     * Helper method to determine if we are running the Pro or Enterprise edition
     * @return boolean indicating success..
     */
    public boolean isProVersion(){
        boolean isPro = false;
        try {
            Class clazz = Class.forName(LICENSE_MANAGER);
            if(clazz != null){
                isPro = true;
            }
        } catch (ClassNotFoundException e) {
            if(log.isDebugEnabled()){
                log.info("This is not a pro version. Access is denied");
            }
        }
        return isPro;
    }

    private Map getResolvedReportJobDefaults() {
        Map<String, String> resolvedDefaults = new HashMap<String, String>();

        // SUPPORTED_JOB_FIELDS: list of attribute names for supported of Report Job Model fields defaults.
        final Set<String> SUPPORTED_JOB_FIELDS = new HashSet<String>(Arrays.asList(new String[]{
                "scheduler.job.repositoryDestination.folderURI"
        }));

        // resolve from xml config first
        if (reportJobDefaults != null && !reportJobDefaults.isEmpty()) {
            for (String field : SUPPORTED_JOB_FIELDS) {
                if (reportJobDefaults.containsKey(field)) {
                    resolvedDefaults.put(field, (String) reportJobDefaults.get(field));
                }
            }
        }

        // Resolve from current user profile attributes
        List<ProfileAttribute> allUserAttributes = profileAttributeService.
                getCurrentUserProfileAttributes(ExecutionContextImpl.getRuntimeExecutionContext(), ProfileAttributeCategory.HIERARCHICAL);
        for (ProfileAttribute attr : allUserAttributes) {
            if (SUPPORTED_JOB_FIELDS.contains(attr.getAttrName())) {
                resolvedDefaults.put(attr.getAttrName(), attr.getAttrValue());
            }
        }

        return resolvedDefaults;
    }


    public TimeZonesList getTimezones()
    {
        return timezones;
    }

    public void setTimezones(TimeZonesList timezones)
    {
        this.timezones = timezones;
    }

    public String getEnableSaveToHostFS() {
        return Boolean.toString(isEnableSaveToHostFS());
    }

    public boolean isEnableSaveToHostFS() {
        return enableSaveToHostFS;
    }

    public void setEnableSaveToHostFS(boolean enableSaveToHostFS) {
        this.enableSaveToHostFS = enableSaveToHostFS;
    }

    public String getEnableDataSnapshot() {
        return Boolean.toString(isEnableDataSnapshot());
    }

    public boolean isEnableDataSnapshot() {
        return enableDataSnapshot;
    }

    public void setEnableDataSnapshot(boolean enableDataSnapshot) {
        this.enableDataSnapshot = enableDataSnapshot;
    }

    public Map getReportJobDefaults() {
        return reportJobDefaults;
    }

    public void setReportJobDefaults(Map reportJobDefaults) {
        this.reportJobDefaults = reportJobDefaults;
    }

    public List<String> getAvailableReportJobOutputFormats() {
        if (availableReportJobOutputFormats.contains("DATA_SNAPSHOT") && !enableDataSnapshot) {
            availableReportJobOutputFormats.remove("DATA_SNAPSHOT");
        }
        return availableReportJobOutputFormats;
    }

    public void setAvailableReportJobOutputFormats(List<String> availableReportJobOutputFormats) {
        this.availableReportJobOutputFormats = availableReportJobOutputFormats;
    }

    public List<String> getAvailableDashboardJobScreenshotOutputFormats() {
        return availableDashboardJobScreenshotOutputFormats;
    }

    public void setAvailableDashboardJobScreenshotOutputFormats(List<String> availableDashboardJobScreenshotOutputFormats) {
        this.availableDashboardJobScreenshotOutputFormats = availableDashboardJobScreenshotOutputFormats;
    }

	public List<String> getAvailableDashboardJobDetailedOutputFormats() {
		return availableDashboardJobDetailedOutputFormats;
	}

	public void setAvailableDashboardJobDetailedOutputFormats(List<String> availableDashboardJobDetailedOutputFormats) {
		this.availableDashboardJobDetailedOutputFormats = availableDashboardJobDetailedOutputFormats;
	}

    public void setRepository(RepositoryService repository) {
        this.repository = repository;
    }

    public void setProfileAttributeService(ProfileAttributeService profileAttributeService) {
        this.profileAttributeService = profileAttributeService;
    }

}

