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
package com.jaspersoft.jasperserver.war.tags;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.tagext.TagSupport;

import com.jaspersoft.jasperserver.war.common.WebConfiguration;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.jaspersoft.jasperserver.war.tags.PaginatorLinksTag.PaginatorInfo;

/**
 * @author Ionut Nedelcu (ionutned@users.sourceforge.net)
 * @version $Id
 */
public class PaginatorTag extends TagSupport
{
	public static final String CURRENT_PAGE_REQUEST_PARAMETER = "currentPage";
	public static final String PAGINATED_ITEMS_REQUEST_PARAMETER = "paginatedItems";
	public static final String PAGINATOR_INFO_REQUEST_PARAMETER = "paginatorInfo";
	public static final String FORM_NAME_REQUEST_PARAMETER = "paginatorFormName";
	
	private List items = null;
	private String page = null;
	private String strItemsPerPage = null;
	private String strPagesRange = null;
	private String formName = null;


	public List getItems() {
		return items;
	}

	public void setItems(List items) {
		this.items = items;
	}

	public String getPage() {
		return page;
	}

	public void setPage(String page) {
		this.page = page;
	}

	public String getItemsPerPage() {
		return strItemsPerPage;
	}

	public void setItemsPerPage(String itemsPerPage) {
		this.strItemsPerPage = itemsPerPage;
	}

	public String getPagesRange() {
		return strPagesRange;
	}

	public void setPagesRange(String pagesRange) {
		this.strPagesRange = pagesRange;
	}

	public String getFormName() {
		return formName;
	}

	public void setFormName(String formName) {
		this.formName = formName;
	}


	public int doStartTag() throws JspException
	{
		if(items == null || items.size() == 0) 
			return SKIP_BODY;

		int itemsPerPage = -1;

		if(strItemsPerPage != null && strItemsPerPage.trim().length() > 0)
			itemsPerPage = Integer.parseInt(strItemsPerPage);

		if(itemsPerPage <= 0)
			itemsPerPage = 
				((WebConfiguration)WebApplicationContextUtils.getRequiredWebApplicationContext(
					pageContext.getServletContext()
					).getBean("configurationBean")).getPaginatorItemsPerPage();

		int pagesRange = -1;

		if(strPagesRange != null && strPagesRange.trim().length() > 0)
			pagesRange = Integer.parseInt(strPagesRange);

		if(pagesRange <= 0)
			pagesRange = 
				((WebConfiguration)WebApplicationContextUtils.getRequiredWebApplicationContext(
					pageContext.getServletContext()
					).getBean("configurationBean")).getPaginatorPagesRange();

		HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();

		int allItemsCount = items.size();
		int allPagesCount = allItemsCount / itemsPerPage;
		if (allItemsCount % itemsPerPage > 0)
			allPagesCount += 1;

		List paginatedItems = null;

		int crtPage = 1;
		String strCrtPage = request.getParameter(CURRENT_PAGE_REQUEST_PARAMETER);

		if(strCrtPage == null || strCrtPage.trim().length() == 0) 
			strCrtPage = page;

		if(strCrtPage != null && strCrtPage.trim().length() > 0) 
			crtPage = Integer.parseInt(strCrtPage);

		if(allItemsCount <= crtPage * itemsPerPage) 
		{
			crtPage = allPagesCount;
		}

		if(allItemsCount < crtPage * itemsPerPage)
			paginatedItems = items.subList((crtPage - 1) * itemsPerPage, allItemsCount);
		else
			paginatedItems = items.subList((crtPage - 1) * itemsPerPage, crtPage * itemsPerPage);

		PaginatorInfo info = new PaginatorLinksTag.PaginatorInfo();
		info.currentPage = crtPage;
		info.firstPage = (crtPage - pagesRange >= 1 ? crtPage - pagesRange : 1);
		info.lastPage = info.firstPage + 2 * pagesRange;
		info.lastPage = (info.lastPage <= allPagesCount ? info.lastPage : allPagesCount);
		info.firstPage = info.lastPage - 2 * pagesRange;
		info.firstPage = (info.firstPage >= 1 ? info.firstPage : 1);
		info.pageCount = allPagesCount;
		
		request.setAttribute(PAGINATED_ITEMS_REQUEST_PARAMETER, paginatedItems);
		request.setAttribute(PAGINATOR_INFO_REQUEST_PARAMETER, info);
		request.setAttribute(FORM_NAME_REQUEST_PARAMETER, formName);

		return EVAL_BODY_INCLUDE;
	}

	public int doEndTag() throws JspException
	{
		HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();

		request.setAttribute(PAGINATED_ITEMS_REQUEST_PARAMETER, null);
		request.setAttribute(PAGINATOR_INFO_REQUEST_PARAMETER, null);

		return EVAL_PAGE;
	}

}

