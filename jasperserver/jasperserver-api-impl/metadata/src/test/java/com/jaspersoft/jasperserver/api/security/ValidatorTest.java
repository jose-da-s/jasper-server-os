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

package com.jaspersoft.jasperserver.api.security;

import com.jaspersoft.jasperserver.api.JSSecurityException;
import com.jaspersoft.jasperserver.api.security.validators.Validator;
import com.jaspersoft.jasperserver.api.security.validators.ValidatorRule;
import com.jaspersoft.jasperserver.api.security.validators.ValidatorRuleImpl;
import com.jaspersoft.jasperserver.core.util.StringUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.owasp.esapi.errors.ValidationException;

import javax.servlet.http.HttpServletRequest;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.jaspersoft.jasperserver.api.security.SecurityConfiguration.isInputValidationOn;
import static com.jaspersoft.jasperserver.api.security.SecurityConfiguration.isSQLValidationOn;
import static com.jaspersoft.jasperserver.api.security.validators.Validator.SEMICOLON_SUBSTITUTION_VALUE;
import static com.jaspersoft.jasperserver.api.security.validators.Validator.getDefaultEncoding;
import static com.jaspersoft.jasperserver.api.security.validators.Validator.getDefaultEncodingErrorMessage;
import static com.jaspersoft.jasperserver.api.security.validators.Validator.isParamValueValid;
import static com.jaspersoft.jasperserver.api.security.validators.Validator.validateSQL;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.matches;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test for Validation
 *
 * @author Anton Fomin
 * @author Normando Macaraeg
 * @version $Id$
 */
@RunWith(MockitoJUnitRunner.class)
public class ValidatorTest {

    private static final String CHECK_QUERY_REGEX =
            "(?s)^SELECT \\* FROM \\(.*" + SEMICOLON_SUBSTITUTION_VALUE + ".*\\) SUBQUERY8173082FDC24 WHERE 1=0$";

    private Connection connection = mock(Connection.class);
    private Statement statement = mock(Statement.class);

    @Before
    public void setUp() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(mock(PreparedStatement.class));
        when(connection.createStatement()).thenReturn(statement);
    }

    @Test
    public void testSQLValidationIsEnabled() {
        assertTrue("SQL Validation is disabled. This is too dangerous to ship as a default!", isSQLValidationOn());
    }

    @Test
    public void validateSql_sqlIsNull_valid() {
        assertTrue(validateSQL(null, connection));
    }

    @Test
    public void validateSql_sqlIsEmpty_valid() {
        assertTrue(validateSQL("", connection));
    }

    @Test
    public void validateSql_sqlIsWhitespaces_valid() {
        assertTrue(validateSQL(" \t ", connection));
    }

    @Test
    public void validateSQLTest1() {
        validateSQL("  SeLeCt * from blah where a = 'ololo';", connection);
    }

    @Test
    public void validateSQLTest2() {
        validateSQL("  SeLeCt * from blah where a = 'ololo';;;;;   ", connection);
    }

    @Test
    public void validateSQLWithComments() {
        validateSQL("-- Request SB 224 Arrest Gross Counts for 2014-2018\n" +
                "-- P Tsao written 4/12/2019. \"RC#19-023IN, RR#2019-012\"\n" +
                "\n" +
                "      --  Convictions --  Statewide  \n" +
                "    Select * from jiuser", connection);
    }


    @Test(expected = JSSecurityException.class)
    public void validateSQLTest3() throws Exception {
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(connection.prepareStatement(matches(CHECK_QUERY_REGEX))).thenReturn(preparedStatement);
        when(preparedStatement.getMetaData()).thenThrow(new SQLException("Error"));

        validateSQL("  SeLeCt * from blah where a = 'ololo';create table test (col blob);", connection);
    }

    @Test(expected = JSSecurityException.class)
    public void validateSQLTest4() throws Exception {
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(connection.prepareStatement(matches(CHECK_QUERY_REGEX))).thenReturn(preparedStatement);
        when(preparedStatement.getMetaData()).thenThrow(new SQLException("Error"));

        validateSQL("  SeLeCt * from blah where a = 'ololo';create table test (col blob)", connection);
    }

    @Test
    public void testValidateWithGoodRequest() throws ValidationException
    {
        if (!isInputValidationOn()) return;

        String param = "j_username";
        String valueGood = "jasperadmin";
        String paramPwd = "j_password";
        String valueGoodPwd = "jasperadmin";
        String paramTimezone = "userTimezone";
        String valueGoodTimezone = "America/New_York";
        String paramLocale = "userLocale";
        String valueGoodLocale = "zh_TW";

        LinkedHashMap<String, String[]> paramMap = new LinkedHashMap<>();
        paramMap.put(param, new String[] {valueGood});
        paramMap.put(paramPwd, new String[] {valueGoodPwd});
        paramMap.put(paramTimezone, new String[] {valueGoodTimezone});
        paramMap.put(paramLocale, new String[] {valueGoodLocale});
        HttpServletRequest request = getRequest(paramMap);

        assertTrue(Validator.validateRequestParams(request));
    }

    @Test
    public void testValidateWithMaliciousRequest() throws ValidationException
    {
        if (!isInputValidationOn()) return;

        String param = "j_username";
        String badUsrName = "<script>alert(1)</script>";
        String paramPwd = "j_password";
        String valueGoodPwd = "jasperadmin";
        String paramTimezone = "userTimezone";
        String valueGoodTimezone = "America/New_York";
        String paramLocale = "userLocale";
        String valueGoodLocale = "zh_TW";


        LinkedHashMap<String, String[]> paramMap = new LinkedHashMap<>();
        paramMap.put(paramPwd, new String[]{valueGoodPwd});
        paramMap.put(paramTimezone, new String[]{valueGoodTimezone});
        paramMap.put(paramLocale, new String[]{valueGoodLocale});
        paramMap.put(param, new String[]{badUsrName});
        HttpServletRequest request = getRequest(paramMap);

        assertFalse(Validator.validateRequestParams(request));
    }

    @Test
    public void testValidateNoScript()
    {
        if (!isInputValidationOn()) return;

        String input_2 = "<script>alert('evil')</script>";
        // TEST #2
        // validate should see the script tag and throw the exception
        assertFalse(isParamValueValid(input_2, input_2, "!Script"));
    }

    @Test
    public void testValidateNoTagViaBlacklist()
    {
        if (!isInputValidationOn()) return;

        String input_2 = "test <script>alert('evil')</script> test2 ";
        // TEST #2
        // validate should see the tag and throw the exception
        assertFalse(isParamValueValid(input_2, input_2, "!Script"));
    }

    /**
     * Test that backlist rule passes empty string as a valid input when rule allows nulls
     * @throws Exception
     */
    @Test
    public void testValidateEmptyStringViaBlacklist2()
    {
        if (!isInputValidationOn()) return;

        String input_2 = "";
        boolean allowNull = true;
        ValidatorRule rule = new ValidatorRuleImpl(null, "!Script", 200, allowNull, Validator.class.getName());
        assertTrue(isParamValueValid(input_2, input_2, rule));
    }

    /**
     * Test that backlist rule fails empty string as a valid input when rule does NOT allow nulls
     * @throws Exception
     */
    @Test
    public void testValidateEmptyStringBlacklist3()
    {
        if (!isInputValidationOn()) return;

        String input_2 = "";
        boolean allowNull = false;
        ValidatorRule rule = new ValidatorRuleImpl(null, "!Script", 200, allowNull, Validator.class.getName());
        assertFalse(isParamValueValid(input_2, input_2, rule));
    }

    @Test
    public void testValidateMultiLineNoTagViaBlacklist()
    {
        if (!isInputValidationOn()) return;

        String input_2 = "test <script>\n alert('evil')\n</script> test2 ";
        // TEST #2
        // validate should see the tag and throw the exception
        assertFalse(isParamValueValid(input_2, input_2, "!Script"));
    }

    @Test
    public void testValidateNoTagHex()
    {
        if (!isInputValidationOn()) return;

        String input_1 = "%3C%73%63%72%69%70%74%3E %3C%2F%73%63%72%69%70%74%3E"; // hex encoded <script>  </script>

        assertFalse(isParamValueValid(input_1, input_1, "!Script")); // canonicalization is done BEFORE validation.

    }

    @Test
    public void testValidateDoubleDashViaBlacklist()
    {
        if (!isInputValidationOn()) return;

        String input_2 = "test -- test - test";
        // TEST #2
        // validate should see the tag and throw the exception
        assertFalse(isParamValueValid(input_2, input_2, "!DoubleDash"));
    }

    @Test
    public void testValidateMultilineCommentBlacklist()
    {
        if (!isInputValidationOn()) return;

        String input_2 = "test /*\n test\n*/ - test";
        // TEST #2
        // validate should see the tag and throw the exception
        assertFalse(isParamValueValid(input_2, input_2, "!SQLComment"));
    }

    @Test
    public void testValidateSQLCommentBlacklist()
    {
        if (!isInputValidationOn()) return;

        String input_2 = "test \n test -- test \n - test";
        // TEST #2
        // validate should see the tag and throw the exception
        assertFalse(isParamValueValid(input_2, input_2, "!SQLComment"));
    }

    @Test
    public void testValidateJSONArrayGoodInput()
    {
        if (!isInputValidationOn()) return;

        String inputGood = "[{id:'JServerJdbcDS.accounts',label:'accounts',type:'ItemGroupType',itemId:'accounts',descr:'accounts',labelId:'',descrId:'',resourceId:'accounts',children:[{id:'JServerJdbcDS.accounts.account_type',label:'account_type',type:'ItemType',itemId:'account_type',descr:'account_type',labelId:'',descrId:'',resourceId:'accounts.account_type',dataType:'java.lang.String',defaultMask:'none',defaultAgg:'none'},{id:'JServerJdbcDS.accounts.annual_revenue',label:'annual_revenue',type:'ItemType',itemId:'annual_revenue',descr:'annual_revenue',labelId:'',descrId:'',resourceId:'accounts.annual_revenue',dataType:'java.lang.String',defaultMask:'none',defaultAgg:'none'},{id:'JServerJdbcDS.accounts.assigned_user_id',label:'assigned_user_id',type:'ItemType',itemId:'assigned_user_id',descr:'assigned_user_id',labelId:'',descrId:'',resourceId:'accounts.assigned_user_id',dataType:'java.lang.String',defaultMask:'none',defaultAgg:'none'},{id:'JServerJdbcDS.accounts.billing_address_city',label:'billing_address_city',type:'ItemType',itemId:'billing_address_city',descr:'billing_address_city',labelId:'',descrId:'',resourceId:'accounts.billing_address_city',dataType:'java.lang.String',defaultMask:'none',defaultAgg:'none'},{id:'JServerJdbcDS.accounts.billing_address_country',label:'billing_address_country',type:'ItemType',itemId:'billing_address_country',descr:'billing_address_country',labelId:'',descrId:'',resourceId:'accounts.billing_address_country',dataType:'java.lang.String',defaultMask:'none',defaultAgg:'none'},{id:'JServerJdbcDS.accounts.billing_address_postalcode',label:'billing_address_postalcode',type:'ItemType',itemId:'billing_address_postalcode',descr:'billing_address_postalcode',labelId:'',descrId:'',resourceId:'accounts.billing_address_postalcode',dataType:'java.lang.String',defaultMask:'none',defaultAgg:'none'},{id:'JServerJdbcDS.accounts.billing_address_state',label:'billing_address_state',type:'ItemType',itemId:'billing_address_state',descr:'billing_address_state',labelId:'',descrId:'',resourceId:'accounts.billing_address_state',dataType:'java.lang.String',defaultMask:'none',defaultAgg:'none'},{id:'JServerJdbcDS.accounts.billing_address_street',label:'billing_address_street',type:'ItemType',itemId:'billing_address_street',descr:'billing_address_street',labelId:'',descrId:'',resourceId:'accounts.billing_address_street',dataType:'java.lang.String',defaultMask:'none',defaultAgg:'none'},{id:'JServerJdbcDS.accounts.created_by',label:'created_by',type:'ItemType',itemId:'created_by',descr:'created_by',labelId:'',descrId:'',resourceId:'accounts.created_by',dataType:'java.lang.String',defaultMask:'none',defaultAgg:'none'},{id:'JServerJdbcDS.accounts.date_entered',label:'date_entered',type:'ItemType',itemId:'date_entered',descr:'date_entered',labelId:'',descrId:'',resourceId:'accounts.date_entered',dataType:'java.sql.Timestamp',defaultMask:'none',defaultAgg:'none'},{id:'JServerJdbcDS.accounts.date_modified',label:'date_modified',type:'ItemType',itemId:'date_modified',descr:'date_modified',labelId:'',descrId:'',resourceId:'accounts.date_modified',dataType:'java.sql.Timestamp',defaultMask:'none',defaultAgg:'none'},{id:'JServerJdbcDS.accounts.deleted',label:'deleted',type:'ItemType',itemId:'deleted',descr:'deleted',labelId:'',descrId:'',resourceId:'accounts.deleted',dataType:'java.lang.Boolean',defaultMask:'none',defaultAgg:'none'},{id:'JServerJdbcDS.accounts.description',label:'description',type:'ItemType',itemId:'description',descr:'description',labelId:'',descrId:'',resourceId:'accounts.description',dataType:'java.lang.String',defaultMask:'none',defaultAgg:'none'},{id:'JServerJdbcDS.accounts.email1',label:'email1',type:'ItemType',itemId:'email1',descr:'email1',labelId:'',descrId:'',resourceId:'accounts.email1',dataType:'java.lang.String',defaultMask:'none',defaultAgg:'none'},{id:'JServerJdbcDS.accounts.email2',label:'email2',type:'ItemType',itemId:'email2',descr:'email2',labelId:'',descrId:'',resourceId:'accounts.email2',dataType:'java.lang.String',defaultMask:'none',defaultAgg:'none'},{id:'JServerJdbcDS.accounts.employees',label:'employees',type:'ItemType',itemId:'employees',descr:'employees',labelId:'',descrId:'',resourceId:'accounts.employees',dataType:'java.lang.String',defaultMask:'none',defaultAgg:'none'},{id:'JServerJdbcDS.accounts.id',label:'id',type:'ItemType',itemId:'id',descr:'id',labelId:'',descrId:'',resourceId:'accounts.id',dataType:'java.lang.String',defaultMask:'none',defaultAgg:'none'},{id:'JServerJdbcDS.accounts.industry',label:'industry',type:'ItemType',itemId:'industry',descr:'industry',labelId:'',descrId:'',resourceId:'accounts.industry',dataType:'java.lang.String',defaultMask:'none',defaultAgg:'none'},{id:'JServerJdbcDS.accounts.modified_user_id',label:'modified_user_id',type:'ItemType',itemId:'modified_user_id',descr:'modified_user_id',labelId:'',descrId:'',resourceId:'accounts.modified_user_id',dataType:'java.lang.String',defaultMask:'none',defaultAgg:'none'},{id:'JServerJdbcDS.accounts.name',label:'name',type:'ItemType',itemId:'name',descr:'name',labelId:'',descrId:'',resourceId:'accounts.name',dataType:'java.lang.String',defaultMask:'none',defaultAgg:'none'},{id:'JServerJdbcDS.accounts.ownership',label:'ownership',type:'ItemType',itemId:'ownership',descr:'ownership',labelId:'',descrId:'',resourceId:'accounts.ownership',dataType:'java.lang.String',defaultMask:'none',defaultAgg:'none'},{id:'JServerJdbcDS.accounts.parent_id',label:'parent_id',type:'ItemType',itemId:'parent_id',descr:'parent_id',labelId:'',descrId:'',resourceId:'accounts.parent_id',dataType:'java.lang.String',defaultMask:'none',defaultAgg:'none'},{id:'JServerJdbcDS.accounts.phone_alternate',label:'phone_alternate',type:'ItemType',itemId:'phone_alternate',descr:'phone_alternate',labelId:'',descrId:'',resourceId:'accounts.phone_alternate',dataType:'java.lang.String',defaultMask:'none',defaultAgg:'none'},{id:'JServerJdbcDS.accounts.phone_fax',label:'phone_fax',type:'ItemType',itemId:'phone_fax',descr:'phone_fax',labelId:'',descrId:'',resourceId:'accounts.phone_fax',dataType:'java.lang.String',defaultMask:'none',defaultAgg:'none'},{id:'JServerJdbcDS.accounts.phone_office',label:'phone_office',type:'ItemType',itemId:'phone_office',descr:'phone_office',labelId:'',descrId:'',resourceId:'accounts.phone_office',dataType:'java.lang.String',defaultMask:'none',defaultAgg:'none'},{id:'JServerJdbcDS.accounts.rating',label:'rating',type:'ItemType',itemId:'rating',descr:'rating',labelId:'',descrId:'',resourceId:'accounts.rating',dataType:'java.lang.String',defaultMask:'none',defaultAgg:'none'},{id:'JServerJdbcDS.accounts.shipping_address_city',label:'shipping_address_city',type:'ItemType',itemId:'shipping_address_city',descr:'shipping_address_city',labelId:'',descrId:'',resourceId:'accounts.shipping_address_city',dataType:'java.lang.String',defaultMask:'none',defaultAgg:'none'},{id:'JServerJdbcDS.accounts.shipping_address_country',label:'shipping_address_country',type:'ItemType',itemId:'shipping_address_country',descr:'shipping_address_country',labelId:'',descrId:'',resourceId:'accounts.shipping_address_country',dataType:'java.lang.String',defaultMask:'none',defaultAgg:'none'},{id:'JServerJdbcDS.accounts.shipping_address_postalcode',label:'shipping_address_postalcode',type:'ItemType',itemId:'shipping_address_postalcode',descr:'shipping_address_postalcode',labelId:'',descrId:'',resourceId:'accounts.shipping_address_postalcode',dataType:'java.lang.String',defaultMask:'none',defaultAgg:'none'},{id:'JServerJdbcDS.accounts.shipping_address_state',label:'shipping_address_state',type:'ItemType',itemId:'shipping_address_state',descr:'shipping_address_state',labelId:'',descrId:'',resourceId:'accounts.shipping_address_state',dataType:'java.lang.String',defaultMask:'none',defaultAgg:'none'},{id:'JServerJdbcDS.accounts.shipping_address_street',label:'shipping_address_street',type:'ItemType',itemId:'shipping_address_street',descr:'shipping_address_street',labelId:'',descrId:'',resourceId:'accounts.shipping_address_street',dataType:'java.lang.String',defaultMask:'none',defaultAgg:'none'},{id:'JServerJdbcDS.accounts.sic_code',label:'sic_code',type:'ItemType',itemId:'sic_code',descr:'sic_code',labelId:'',descrId:'',resourceId:'accounts.sic_code',dataType:'java.lang.String',defaultMask:'none',defaultAgg:'none'},{id:'JServerJdbcDS.accounts.ticker_symbol',label:'ticker_symbol',type:'ItemType',itemId:'ticker_symbol',descr:'ticker_symbol',labelId:'',descrId:'',resourceId:'accounts.ticker_symbol',dataType:'java.lang.String',defaultMask:'none',defaultAgg:'none'},{id:'JServerJdbcDS.accounts.website',label:'website',type:'ItemType',itemId:'website',descr:'website',labelId:'',descrId:'',resourceId:'accounts.website',dataType:'java.lang.String',defaultMask:'none',defaultAgg:'none'}]}, {id:'JServerJdbcDS.accounts_bugs',label:'accounts_bugs',type:'ItemGroupType',itemId:'accounts_bugs',descr:'accounts_bugs',labelId:'',descrId:'',resourceId:'accounts_bugs',children:[{id:'JServerJdbcDS.accounts_bugs.account_id',label:'account_id',type:'ItemType',itemId:'account_id',descr:'account_id',labelId:'',descrId:'',resourceId:'accounts_bugs.account_id',dataType:'java.lang.String',defaultMask:'none',defaultAgg:'none'},{id:'JServerJdbcDS.accounts_bugs.bug_id',label:'bug_id',type:'ItemType',itemId:'bug_id',descr:'bug_id',labelId:'',descrId:'',resourceId:'accounts_bugs.bug_id',dataType:'java.lang.String',defaultMask:'none',defaultAgg:'none'},{id:'JServerJdbcDS.accounts_bugs.date_modified',label:'date_modified',type:'ItemType',itemId:'date_modified1',descr:'date_modified1',labelId:'',descrId:'',resourceId:'accounts_bugs.date_modified',dataType:'java.sql.Timestamp',defaultMask:'none',defaultAgg:'none'},{id:'JServerJdbcDS.accounts_bugs.deleted',label:'deleted',type:'ItemType',itemId:'deleted1',descr:'deleted1',labelId:'',descrId:'',resourceId:'accounts_bugs.deleted',dataType:'java.lang.Boolean',defaultMask:'none',defaultAgg:'none'},{id:'JServerJdbcDS.accounts_bugs.id',label:'id',type:'ItemType',itemId:'id1',descr:'id1',labelId:'',descrId:'',resourceId:'accounts_bugs.id',dataType:'java.lang.String',defaultMask:'none',defaultAgg:'none'}]}, {id:'JServerJdbcDS.accounts_contacts',label:'accounts_contacts',type:'ItemGroupType',itemId:'accounts_contacts',descr:'accounts_contacts',labelId:'',descrId:'',resourceId:'accounts_contacts',children:[{id:'JServerJdbcDS.accounts_contacts.account_id',label:'account_id',type:'ItemType',itemId:'account_id1',descr:'account_id1',labelId:'',descrId:'',resourceId:'accounts_contacts.account_id',dataType:'java.lang.String',defaultMask:'none',defaultAgg:'none'},{id:'JServerJdbcDS.accounts_contacts.contact_id',label:'contact_id',type:'ItemType',itemId:'contact_id',descr:'contact_id',labelId:'',descrId:'',resourceId:'accounts_contacts.contact_id',dataType:'java.lang.String',defaultMask:'none',defaultAgg:'none'},{id:'JServerJdbcDS.accounts_contacts.date_modified',label:'date_modified',type:'ItemType',itemId:'date_modified2',descr:'date_modified2',labelId:'',descrId:'',resourceId:'accounts_contacts.date_modified',dataType:'java.sql.Timestamp',defaultMask:'none',defaultAgg:'none'},{id:'JServerJdbcDS.accounts_contacts.deleted',label:'deleted',type:'ItemType',itemId:'deleted2',descr:'deleted2',labelId:'',descrId:'',resourceId:'accounts_contacts.deleted',dataType:'java.lang.Boolean',defaultMask:'none',defaultAgg:'none'},{id:'JServerJdbcDS.accounts_contacts.id',label:'id',type:'ItemType',itemId:'id2',descr:'id2',labelId:'',descrId:'',resourceId:'accounts_contacts.id',dataType:'java.lang.String',defaultMask:'none',defaultAgg:'none'}]}, {id:'JServerJdbcDS.config',label:'config',type:'ItemGroupType',itemId:'config',descr:'config',labelId:'',descrId:'',resourceId:'config',children:[{id:'JServerJdbcDS.config.category',label:'category',type:'ItemType',itemId:'category',descr:'category',labelId:'',descrId:'',resourceId:'config.category',dataType:'java.lang.String',defaultMask:'none',defaultAgg:'none'},{id:'JServerJdbcDS.config.name',label:'name',type:'ItemType',itemId:'name1',descr:'name1',labelId:'',descrId:'',resourceId:'config.name',dataType:'java.lang.String',defaultMask:'none',defaultAgg:'none'},{id:'JServerJdbcDS.config.value',label:'value',type:'ItemType',itemId:'value',descr:'value',labelId:'',descrId:'',resourceId:'config.value',dataType:'java.lang.String',defaultMask:'none',defaultAgg:'none'}]}]]";
        assertTrue(isParamValueValid(inputGood, inputGood, "AlphaNumPunctuation"));
    }

    @Test
    public void testValidateJSONArrayMaliciousHexInput() {
        if (!isInputValidationOn()) return;

        String inputBad = "[{id:'JServerJdbcDS.accounts%3Cscript%3Ealert(1)%3C%2Fscript%3E'}]";
        assertFalse(isParamValueValid(inputBad, inputBad, "AlphaNumPunctuation"));
    }

    @Test
    public void testValidateJSONObjectGoodInput()
    {
        if (!isInputValidationOn()) return;

        String inputGood = "{'userName': 'normID', 'fullName': 'norm macaraeg', 'password': 'mypassword', 'email': 'norm@norm.com', 'enabled': true, 'roles': [{'roleName': 'ROLE_USER', 'FLOW_ID': 'roleListFlow'}], 'attributes': [], 'FLOW_ID': 'userListFlow'}";
        assertTrue(isParamValueValid(inputGood, inputGood, "AlphaNumPunctuation"));
    }

    @Test
    public void testValidateBoolean1() {
        if (!isInputValidationOn()) return;

        String param = "autoGenerateJoins";
        String valueGood_1 = "tRuE";

        HttpServletRequest request = getOneParamRequest(param, valueGood_1);
        assertTrue(Validator.validateRequestParams(request));
    }

    @Test
    public void testValidateBoolean2() {
        if (!isInputValidationOn()) return;

        String param = "autoGenerateJoins";
        String valueGood_2 = "FaLsE";

        HttpServletRequest request = getOneParamRequest(param, valueGood_2);
        assertTrue(Validator.validateRequestParams(request));
    }

    @Test
    public void testValidateBoolean3() {
        if (!isInputValidationOn()) return;

        String param = "autoGenerateJoins";
        String valueBad = "neitherTRUEnorFALSE";

        HttpServletRequest request = getOneParamRequest(param, valueBad);
        assertFalse(Validator.validateRequestParams(request));
    }

    private String valueGood1 = "{'userName': 'normID', 'fullName': 'norm macaraeg', 'password': 'mypassword', 'email': 'norm@norm.com', 'enabled': true, 'roles': [{'roleName': 'ROLE_USER', 'FLOW_ID': 'roleListFlow'}], 'attributes': [], 'FLOW_ID': 'userListFlow'}";
    private String valueGood12 = "{'userName': 'billID', 'fullName': 'billy gean', 'password': 'mypassword', 'email': 'bill@bill.com', 'enabled': true, 'roles': [{'roleName': 'ROLE_ADMINISTRATOR', 'FLOW_ID': 'roleListFlow'}], 'attributes': [], 'FLOW_ID': 'userListFlow'}";
    private String valueBad1 = "{'userName': 'normID', 'fullName': '%3Cscript%3Ealert(1)%3C%2Fscript%3E', 'password': 'mypassword', 'email': 'norm@norm.com', 'enabled': true, 'roles': [{'roleName': 'ROLE_USER', 'FLOW_ID': 'roleListFlow'}], 'attributes': [], 'FLOW_ID': 'userListFlow'}";
    private String valueGood2 = "normID|organization_1";
    private String valueGood21 = "billID|organization_2";
    private String valueBad2 = "normID|organization_1|%3Cscript%3Ealert(1)%3C%2Fscript%3E";
    

    private HttpServletRequest getRequest(Map<String, String[]> paramMap) {
        HttpServletRequest requestMock = Mockito.mock(HttpServletRequest.class);
        Mockito.lenient().when(requestMock.getAttribute(anyString())).thenReturn(null);
        Mockito.lenient().when(requestMock.getParameterMap()).thenReturn(paramMap);
        return requestMock;
    }

    private HttpServletRequest getOneParamRequest(String key, String value) {
        LinkedHashMap<String, String[]> paramMap = new LinkedHashMap<>();
        paramMap.put(key, new String[]{value});
        return getRequest(paramMap);
    }

    @Test
    public void getDecodedMapTest() throws Exception {
        Map<String, String[]> paramMap = new HashMap<String, String[]>();
        paramMap.put(URLEncoder.encode("ключ1", "UTF-8"), new String[] {URLEncoder.encode("Jožin z bažin", "UTF-8"), URLEncoder.encode("močálem se plíží", "UTF-8")});
        paramMap.put(URLEncoder.encode("ключ2", "UTF-8"), new String[] {URLEncoder.encode("Jožin z bažin", "UTF-8"), URLEncoder.encode("k vesnici se blíží", "UTF-8")});
        paramMap.put(URLEncoder.encode("ключ3", "UTF-8"), new String[] {"Jožin z bažin %", "k vesnici %se blíží"});

        Map<String, String[]> decodedParamMap = StringUtil.getDecodedMap(paramMap, getDefaultEncoding(), getDefaultEncodingErrorMessage());

        assertTrue(decodedParamMap.keySet().contains("ключ1"));
        assertTrue(decodedParamMap.keySet().contains("ключ2"));
        assertArrayEquals(new String[] {"Jožin z bažin", "močálem se plíží"} , decodedParamMap.get("ключ1"));
        assertArrayEquals(new String[] {"Jožin z bažin", "k vesnici se blíží"} , decodedParamMap.get("ключ2"));
        assertArrayEquals(new String[]{"Jožin z bažin %", "k vesnici %se blíží"}, decodedParamMap.get("ключ3"));
    }

    @Test
    public void testGoodRequestDate1() {
        HttpServletRequest request = getOneParamRequest("RequestDate", "99-99-9999"); // good mm-dd-yyyy
        Validator.validateRequestParams(request);
    }
    @Test
    public void testGoodRequestDate2() {
        HttpServletRequest request = getOneParamRequest("RequestDate", "12-01-9900"); // good dd-mm-yyyy
        Validator.validateRequestParams(request);
    }
    @Test
    public void testGoodRequestDate3() {
        HttpServletRequest request = getOneParamRequest("RequestDate", "9999-99-99"); // good nnnn-nn-nn
        Validator.validateRequestParams(request);
    }
    @Test
    public void testGoodRequestDate4() {
        HttpServletRequest request = getOneParamRequest("RequestDate", "19970601000000"); // good yyyymmdd000000
        Validator.validateRequestParams(request);
    }
    @Test
    public void testGoodRequestDate5() {
        HttpServletRequest request = getOneParamRequest("RequestDate", "15 Juillet 1901"); // good dd MMM yyyy
        Validator.validateRequestParams(request);
    }

    @Test(expected = JSSecurityException.class)
    public void validateSQL_semicolonSeparatedStatements_metadataReturnsNull_exception() throws Exception {
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(connection.prepareStatement(matches(CHECK_QUERY_REGEX))).thenReturn(preparedStatement);
        when(preparedStatement.getMetaData()).thenReturn(null);
        when(statement.execute(matches(CHECK_QUERY_REGEX))).thenThrow(new SQLException());

        validateSQL("SELECT CITY FROM STORE; DROP TABLE STORE", connection);
    }

    @Test
    public void validateSQL_semicolonInData_valid() throws Exception {
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(connection.prepareStatement(matches(CHECK_QUERY_REGEX))).thenReturn(preparedStatement);
        when(preparedStatement.getMetaData()).thenReturn(mock(ResultSetMetaData.class));

        validateSQL("SELECT CITY FROM STORE WHERE TEXT = ';'", connection);
    }

    @Test(expected = JSSecurityException.class)
    public void validateSQL_notAllowedQuery_exception() {
        validateSQL("DROP TABLE STORE", connection);
    }

    @Test(expected = JSSecurityException.class)
    public void validateSQL_semicolonSeparatedStatements_metadataThrowsSQLFeatureNotSupportedException_exception() throws Exception {
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(connection.prepareStatement(matches(CHECK_QUERY_REGEX))).thenReturn(preparedStatement);
        when(preparedStatement.getMetaData()).thenThrow(new SQLFeatureNotSupportedException("error"));

        validateSQL("SELECT CITY FROM STORE; DROP TABLE STORE", connection);
    }

    @Test(expected = JSSecurityException.class)
    public void validateSQL_notAllowedQueryWithSemicolon_exception() {
        validateSQL("DROP TABLE ';STORE'", connection);
    }
}
