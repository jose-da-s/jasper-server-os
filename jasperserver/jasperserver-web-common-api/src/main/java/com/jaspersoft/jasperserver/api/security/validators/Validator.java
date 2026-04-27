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
package com.jaspersoft.jasperserver.api.security.validators;

import com.jaspersoft.jasperserver.api.JSSecurityException;
import com.jaspersoft.jasperserver.api.metadata.user.service.ProfileAttributesResolver;
import com.jaspersoft.jasperserver.api.security.SecurityConfiguration;
import com.jaspersoft.jasperserver.core.util.JSONUtil;
import com.jaspersoft.jasperserver.core.util.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.StringUtilities;
import org.owasp.esapi.errors.IntrusionException;
import org.owasp.esapi.errors.ValidationException;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import jakarta.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.jaspersoft.jasperserver.api.security.SecurityConfiguration.isInputValidationOn;
import static com.jaspersoft.jasperserver.api.security.SecurityConfiguration.isSQLValidationOn;
import static com.jaspersoft.jasperserver.api.security.validators.Validator.Props.*;
import static java.lang.String.format;

/**
 * Wrapper around OWASP ESAPI Validator.
 *
 * ESAPI validate methods provide canonicalization to ensure we are validating against the characters
 * we expect to validate against.  This helps to prevent bad guys from sending in funky unicode.
 *
 * @author Normando Macaraeg
 * @author Anton Fomin
 * @version $Id$
 */
public class Validator {
    private static final Logger log = LogManager.getLogger(Validator.class);

    /* Location of input validation parameter rules */
    private static final String RULES_LOCATION = "esapi/security.properties";
    public static final String SQL_QUERY_EXECUTOR_RULE_KEY = "sqlQueryExecutor";
    private static final String MSG_VALIDATION_SQL = "message.validation.sql";
    private static final String ERR_MSG_SQL_VALIDATION = "An error has occurred. Please contact your system administrator. (56632)";

    /* log msg fragments for security failure msg */
    private static final String LOG_SECURITY_FAILURE_CONTEXT = "[SECURITY FAILURE context=";
    private static final String LOG_SECURITY_FAILURE_KEY = ", key=";
    private static final String LOG_SECURITY_FAILURE_TYPE = ", type(";
    private static final String LOG_SECURITY_FAILURE_PATTERN = ")=";
    private static final String LOG_SECURITY_FAILURE_INPUT = ", input=";
    private static final String LOG_SECURITY_FAILURE_MAXLENGTH = ", maxLength=";
    private static final String LOG_SECURITY_FAILURE_BLACKLIST = ", isBlacklist=";
    private static final String END_BRACKET = "]";
    private static final String UNKNOWN_KEY_OR_VALUE = "unknown";

    public static final String SEMICOLON_SUBSTITUTION_VALUE = "SEMICOLON_SUB_VALUE";
    private static final Pattern SQL_VALIDATION_VIA_META_QUERY_PATTERN
            = Pattern.compile("(?is)^\\s*(select|call)\\b((?!\\binto\\b).)*;?\\s*$");
    private static final Pattern SQL_VALIDATION_PATTERN
            = Pattern.compile("(?is)^\\s*(select|call)\\b((?!\\binto\\b)[^;])*;?\\s*$");

    private static final String UNSUCCESSFUL_SQL_VALIDATION_PATTERN_SEMI = "Validation unsuccessful. Failed evaluating default rule 'Validator.SQL_VALIDATION_PATTERN' checking query with semicolon ';', no matches found ";
    private static final String UNSUCCESSFUL_SQL_VALIDATION_PATTERN = "Validation unsuccessful. Failed evaluating default rule 'Validator.SQL_VALIDATION_PATTERN', no matches found ";
    private static final String UNSUCCESSFUL_SQL_VALIDATION_VIA_META_QUERY = "Validation unsuccessful. Failed evaluating default rule 'Validator.SQL_VALIDATION_VIA_META_QUERY_PATTERN', no matches found";

    public static final String DECRYPTED_PREFIX = "DECRYPTED.";

    private static MessageSource messages;
    private static ProfileAttributesResolver profileAttributesResolver;
    private static final AtomicReference<Pattern> sqlCommentsPattern = new AtomicReference<>();

    /* Input validationRules */
    private static Map<String, ValidatorRule> validationRules;

    /* Instantiation is not allowed */
    private Validator() {
        /* Do not instantiate */
    }

    private static final ValidatorRule DEFAULT_VALIDATION_RULE;

    static {
        /* Set custom config implementation for ESAPI */
        setESAPISecurityConf();
        /* Load validation configuration */
        loadInputValidationConfig();
        List<ValidatorRule> defRules = getRulesForParameter(DEFAULT_RULE_NAME.<String>get());
        DEFAULT_VALIDATION_RULE = defRules.isEmpty() ?
                new ValidatorRuleImpl(DEFAULT_PARAM_NAME_VALIDATION_KEY.<String>get(),
                                      DEFAULT_VALUE_VALIDATION_KEY.<String>get(),
                                      DEFAULT_MAX_LENGTH.<Integer>get(),
                                      DEFAULT_ALLOW_NULL.<Boolean>get(),
                                      DEFAULT_VALUE_VALIDATION_CONTEXT.<String>get()
                ) : defRules.get(0);
    }

    /**
     * Validation properties having default and optionally taken properties file
     */
    static enum Props {
        DEFAULT_PARAM_NAME_VALIDATION_KEY(String.class, "Alpha"),
        DEFAULT_VALUE_VALIDATION_CONTEXT(String.class, "DEFAULT"),
        DEFAULT_VALUE_VALIDATION_KEY(String.class, "AlphaNumPunctuationBrackets"),
        DEFAULT_MAX_LENGTH(Integer.class, "200000"),
        DEFAULT_ALLOW_NULL(Boolean.class, "true"),
        DEFAULT_RULE_NAME(String.class,"DEFAULT"),
        DEFAULT_DELIMITER(String.class, ","),
        DEFAULT_ENCODING(String.class, "UTF-8"),
        DEFAULT_CANONICALIZE(Boolean.class, "true"),
        DEFAULT_MESSAGE_MISSING_RULE(String.class, "No rule for parameter [%s]. Using default validation on input=[%s]."),
        DEFAULT_MESSAGE_ENCODING_ERROR(String.class, "Please make sure your web application server is set to URIEncoding=UTF-8.");

        private Class clazz;

        private Object value;

        Props(Class clazz, String defaultValue) {
            this.clazz = clazz;
            set(defaultValue);
        }

        public void set(String value) {
            if (clazz.equals(Integer.class)) {
                this.value = Integer.parseInt(value);
            } else if (clazz.equals(Boolean.class)) {
                this.value = Boolean.parseBoolean(value);
            } else {
                this.value = value;
            }
        }

        /**
         * Get a casted object,
         * you should know which property value conform to which class.
         * This is to avoind cast in outer context.
         *
         * @param <T> desired type
         * @return desired type
         */
        public <T> T get() {
            return (T) value;
        }

        public static Set<String> getStringList() {
            Set<String> stringSet = new HashSet<String>();
            for (Props prop : Props.values()) {
                stringSet.add(prop.toString());
            }
            return stringSet;
        }
    }

    /**
     * Load properties file with validation rules.
     *
     * @throws JSSecurityException
     */
    private static void loadInputValidationConfig() {
        if (validationRules != null) return;

        Properties validationConfig = new Properties();

        try {
            InputStream is = Validator.class.getClassLoader().getResourceAsStream(RULES_LOCATION);
            validationConfig.load(is);
            is.close();
        } catch (Exception e) {
            throw new JSSecurityException("Input validation configuration cannot be loaded.", e);
        }

        resolveValidationProperties(validationConfig);
        validationRules = resolveInputValidationRules(validationConfig);
    }

    /**
     * Resolve validation properties.
     *
     * @param validationConfig
     *      Properties representing validation config file.
     */
    private static void resolveValidationProperties(Properties validationConfig) {
        Set<String> configPropertyNames = validationConfig.stringPropertyNames();
        for (Props prop : Props.values()) {
            if (configPropertyNames.contains(prop.toString())) {
                prop.set(validationConfig.getProperty(prop.toString()));
            }
        }
    }

    /**
     * Resolve input validation props.
     *
     * @param validationConfig
     *      Properties with validation rules.
     * @return
     *      Map of validation rules.
     */
    private static Map<String, ValidatorRule> resolveInputValidationRules(Properties validationConfig) {
        Map<String, ValidatorRule> validationRules = new HashMap<String, ValidatorRule>();
        try {
            for (String propName : validationConfig.stringPropertyNames()) {

                /* Skip configuration properties */
                if (Props.getStringList().contains(propName)) continue;

                String[] propValues = validationConfig.getProperty(propName).split("\\" + DEFAULT_DELIMITER.<String>get());

                /* key - param name */
                /* value - param name validation key, param value validation key, max length, allowNull, context */
                validationRules.put(propName, new ValidatorRuleImpl(propValues[0], propValues[1], Integer.parseInt(propValues[2]),
                        Boolean.parseBoolean(propValues[3]), propValues[4]));
            }
        } catch (Exception e) {
            throw new JSSecurityException("Input validation configuration cannot be resolved.", e);
        }
        return validationRules;
    }

    /**
     * Set custom SecurityConfiguration for ESAPI,
     * This is exact copy of original org.owasp.esapi.reference.DefaultSecurityConfiguration,
     * but removed iae.printStackTrace() which occurs at first attempt to load ESAPI properties,
     * however second attempt is successful.
     */
    private static void setESAPISecurityConf() {
        ESAPI.initialize("com.jaspersoft.jasperserver.api.security.JSESAPISecurityConfiguration");
    }

    /**
     * Validate all the parameter/value pairs in the request.
     * The parameter/value rules are specified in security.properties.
     * The regex patterns used by the rules are specified in validation.properties.
     *
     * @param request
     *      A ServletRequest.
     * @return true
     *      if all parameter/value pairs are valid.
     * @throws
     *      IntrusionException, ValidationException
     *
     * @deprecated Centralized input validation was replaced with output escaping.
     */
    public static boolean validateRequestParams(HttpServletRequest request) {
        if (!SecurityConfiguration.isInputValidationOn()) return true;

        Map<String, String[]> requestParamMap = StringUtil.getDecodedMap(request.getParameterMap(), DEFAULT_ENCODING.<String>get(), DEFAULT_MESSAGE_ENCODING_ERROR.<String>get());
        if (requestParamMap.size() == 0)
            return true;

        for (Map.Entry<String, String[]> reqNameValuePair : requestParamMap.entrySet()) {
            final String reqParamKey = reqNameValuePair.getKey();
            //look up the param in request attribs 1st: that is where the decrypted version may have been posted by EncryptionFilter.
            final List     reqParamAttribValueList = (List) request.getAttribute(DECRYPTED_PREFIX + reqParamKey);
            final Object[] reqParamValueArr       = reqParamAttribValueList != null ? reqParamAttribValueList.toArray() : reqNameValuePair.getValue();

            List<ValidatorRule> validationRuleList = getRulesForParameter(reqParamKey);
            if (validationRuleList == null || validationRuleList.size() == 0)
                validationRuleList = Arrays.asList(DEFAULT_VALIDATION_RULE);

            boolean allRulesFailed = true;
            for (ValidatorRule paramRule : validationRuleList) {
                final boolean[] allParamValuesValid = {true};
                for (Object reqParamValue : reqParamValueArr) {
                    //check if reqParamValue is a json object/array and validate json key-value pairs
                    JSONObject reqJsonObj = JSONUtil.getJSONObject(reqParamValue.toString());
                    JSONArray  reqJsonArr = JSONUtil.getJSONArray(reqParamValue.toString());
                    if (reqJsonObj != null || (reqJsonArr != null && reqJsonArr.length() != 0)) {
                        JSONUtil.applyFunctorToJson(reqJsonObj != null ? reqJsonObj : reqJsonArr, new JSONUtil.Functor() {
                            public String call(String jsonKey, String jsonValue) {
								if (jsonKey == null)
									jsonKey = reqParamKey; //if json array has no key, apply reqParamKey rule

                                //get a new rule for the json key
                                List<ValidatorRule> validJsonRuleList = getRulesForParameter(jsonKey);
                                if (validJsonRuleList == null || validJsonRuleList.size() == 0)
                                    validJsonRuleList = Arrays.asList(DEFAULT_VALIDATION_RULE);

                                for (ValidatorRule rule : validJsonRuleList) {
                                    if (!isParamValueValid(reqParamKey+"-"+jsonKey, jsonValue, rule)) {
                                        allParamValuesValid[0] = false;
                                        return null;  //DO NOT replace anything in JSON -- NOTE: Exception won't be thrown in this case
                                    }
                                }

                                return jsonValue;
                            }
                        });
                    }

                    if (!allParamValuesValid[0] || !isParamValueValid(reqParamKey, reqParamValue.toString(), paramRule)) {
                        allParamValuesValid[0] = false;
                        break;
                    }
                }

                if (allParamValuesValid[0]) {
                    allRulesFailed = false;
                    break; //next request param
                }
            }

            if (allRulesFailed)
                return false;
        }

        return true;
    }

    /**
     * Validate a single value set given a specific validation pattern key.
     * Default Context: [parameter-value-regExValidationPatternKey]
     *
     * @see "validation.properties" for the keys.
     * @param paramValue The value to validate.
     * @param rule Validation rule to be applied.
     *             Rule is configured as follows:
     *                  validationKey - The regex pattern to validate the parameter and value.
     *                  maxLength - Pass in a max length if the default is not sufficient.
     *                  allowNull - True if null is considered valid; false otherwise.
     *                  context - Context is a label you want to use for identifying in the logs where the error was thrown.
     *                        Follow this pattern: [parameter-value-regExValidationPatternKey]
     * @throws ValidationException when esapi determines invalid input.
     * @throws IntrusionException when esapi detects obvious attack.
     */
    public static boolean isParamValueValid(String paramName, String paramValue, ValidatorRule rule) {
        if (!SecurityConfiguration.isInputValidationOn()) return true;

        if (rule == null)
            throw new IllegalArgumentException("Missing validation rule.");

        // The following checks (if & else if) are necessary for the custom blacklist rule mechanism.  ESAPI makes
        // empty strings valid if the validation rule allows nulls (and vice versa).  That breaks the custom blacklist
        // rules mechanism as it works in reverse of the ESAPI whitelist mechanism.
        if (StringUtilities.isEmpty(paramValue)) {
            if (rule.isAllowNull())
                return true;
            else
                return false;
        }

        if (DEFAULT_CANONICALIZE.<Boolean>get()) {
            paramValue = ESAPI.encoder().canonicalize(paramValue);
        }

        if (profileAttributesResolver.containsAttribute(paramValue)) {
            return true;
        }

        boolean doesInputSatisfyValidationRule = ESAPI.validator().isValidInput(rule.getContext(), paramValue, rule.getValueValidationKey(), rule.getMaxLength(), rule.isAllowNull(), false);
        // if the value passes ESAPI validation and the validation rule is a blacklist one, the input is invalid
        if ((doesInputSatisfyValidationRule && rule.isBlacklistRule()) || (!doesInputSatisfyValidationRule && !rule.isBlacklistRule())) {
            logSecurityFailure(paramName, paramValue, rule);
            return false;
        }

        return true;
    }

    /**
     * Log Legend
     * <ul>
     * <li>context = the context in which the security alert is being triggered
     * <li>key = the key or param connected to the input for which the rule is applied; if generic, then rule simply applied to the value.
     * <li>type = the name of the rule (the regex pattern for the rule)
     * <li>input = the value being validated
     * <li>maxLength = the maximum allowed length of the input
     * </ul>
     *
     * @param key The key or param connected to the input for which the rule is applied; if generic, then rule simply applied to the value.
     * @param value The value that broke the validator rule.
     * @param rule The ValidatorRule for the value.
     */
    private static void logSecurityFailure(String key, String value, ValidatorRule rule)
    {
        key = (key == null) ? UNKNOWN_KEY_OR_VALUE:key;
        value = (value == null) ? UNKNOWN_KEY_OR_VALUE:value;

        StringBuilder errMsg = new StringBuilder();
        errMsg.append(LOG_SECURITY_FAILURE_CONTEXT).append( rule.getContext() );
        errMsg.append(LOG_SECURITY_FAILURE_KEY).append( key );
        errMsg.append(LOG_SECURITY_FAILURE_TYPE).append( rule.getValueValidationKey() );
        errMsg.append(LOG_SECURITY_FAILURE_PATTERN).append( ESAPI.securityConfiguration().getValidationPattern(rule.getValueValidationKey()) );
        errMsg.append(LOG_SECURITY_FAILURE_INPUT).append( value );

        if (rule != null) {
            errMsg.append(LOG_SECURITY_FAILURE_MAXLENGTH).append( rule.getMaxLength() ).append(LOG_SECURITY_FAILURE_BLACKLIST).append( rule.isBlacklistRule() ).append( END_BRACKET );
        } else {
            errMsg.append(LOG_SECURITY_FAILURE_MAXLENGTH).append( UNKNOWN_KEY_OR_VALUE ).append(LOG_SECURITY_FAILURE_BLACKLIST).append( UNKNOWN_KEY_OR_VALUE ).append( END_BRACKET );
        }

        log.error( errMsg.toString() );
    }

    /**
     * Calls on {@link #isParamNameValid(String, ValidatorRule)}  creating a temp rule out of valueValidationExpression
     * @param paramValue   input value to validate
     * @param valueValidationExpression  regex to validate input
     * @throws ValidationException
     * @throws IntrusionException
     */
    public static boolean isParamValueValid(String paramName, String paramValue, String valueValidationExpression) {
        ValidatorRule rule = new ValidatorRuleImpl(DEFAULT_VALIDATION_RULE.getParamValidationKey(), valueValidationExpression, DEFAULT_VALIDATION_RULE.getMaxLength(),
                DEFAULT_VALIDATION_RULE.isAllowNull(), DEFAULT_VALIDATION_RULE.getContext());
        return isParamValueValid(paramName, paramValue, rule);
    }

    /**
     * Use this to validate any value string against existing rules configured in validation.properties.
     * This method is not tied to a servlet request so it can be called internally.
     *
     * @param value input value to validate
     * @param validatorRuleName  regex to validate input
     * @throws ValidationException
     * @throws IntrusionException
     */
    public static boolean isValueValid(String value, String validatorRuleName) {
        ValidatorRule rule = new ValidatorRuleImpl(DEFAULT_VALIDATION_RULE.getParamValidationKey(), validatorRuleName, DEFAULT_VALIDATION_RULE.getMaxLength(),
                DEFAULT_VALIDATION_RULE.isAllowNull(), DEFAULT_VALIDATION_RULE.getContext());

        return isParamValueValid("genericKey", value, rule);
    }

    /**
     * Validate rule name
     * @param paramName
     * @param rule
     * @return
     *
     * @Deprecated Request parameter name should not be validated
     */
    @Deprecated
    public static boolean isParamNameValid(String paramName, ValidatorRule rule) {
        if (!SecurityConfiguration.isInputValidationOn()) return true;

        if (rule == null)
            throw new IllegalArgumentException("Missing validation rule.");

        if (DEFAULT_CANONICALIZE.<Boolean>get())
            paramName = ESAPI.encoder().canonicalize(paramName);

        return ESAPI.validator().isValidInput(rule.getContext(), paramName, rule.getParamValidationKey(), rule.getMaxLength(), rule.isAllowNull(), false);
    }

    /**
     * Validate a query string or query parameter value, trims spaces and trailing semicolons.
     * The queryRuleKey being passed in must the key from security.properties since it will use the rules as configured in this file.
     *
     * @see "validation.properties" for the regex keys, usually beginning with the prefix ValidSQL.  Example: Validator.ValidSQLSelectStart.
     * @see "security.properties" for the ValidatorRule keys.  Example: sqlDomainDesigner
     * @param queryOrParamString The value to validate. This is expected to be either the query string or a query parameter string.
     * @param connection A {@link Connection} to database, required for additional validation control.
     * @return True if valid.
	 * @throws JSSecurityException if queryOrParamString is not a secure query according to the rule pointed to by SQL_QUERY_EXECUTOR_RULE_KEY
     */
    public static boolean validateSQL(String queryOrParamString, Connection connection) {
        if (!SecurityConfiguration.isSQLValidationOn() || StringUtils.isBlank(queryOrParamString)) return true;

        queryOrParamString = queryOrParamString.trim().replaceAll(";+$", ""); // remove trailing semicolons
        queryOrParamString = removeCommentsFromSql(queryOrParamString);

        List<ValidatorRule> validationRuleList = getRulesForParameter(SQL_QUERY_EXECUTOR_RULE_KEY);
        boolean validationRulesUsed = false;

        for (ValidatorRule rule : validationRuleList) {
            boolean isInputNonEmptyStrings = StringUtil.checkAllInputStringsNonEmpty(queryOrParamString, rule.getContext(), rule.getValueValidationKey());

            if (isInputNonEmptyStrings && rule.getMaxLength() > 0) {
                validationRulesUsed = true;
                boolean isSQLValid = ESAPI.validator().isValidInput(rule.getContext(), queryOrParamString, rule.getValueValidationKey(), rule.getMaxLength(), false);
                if (!isSQLValid) {
                    log.error(format("Validation unsuccessful. Failed evaluating rule '%s' ", rule.getValueValidationKey()));
                    throw newSecurityException(queryOrParamString);
                }
            }
        }

        if (!validationRulesUsed) { // customer has no its own validation rules, then use default validation regexps

            if (SecurityConfiguration.isSqlValidationViaMetadataOn() && queryOrParamString.contains(";")) {
                Matcher matcher = SQL_VALIDATION_VIA_META_QUERY_PATTERN.matcher(queryOrParamString);
                if (!matcher.matches()) {
                    log.error(UNSUCCESSFUL_SQL_VALIDATION_VIA_META_QUERY);
                    throw newSecurityException(queryOrParamString);
                }

                // ';' is not allowed as statement separator. If it is,
                // its replacement with random value will brake SQL syntax
                // and it will fail on next test
                try {
                    trySqlSyntax(connection, queryOrParamString.replace(";", SEMICOLON_SUBSTITUTION_VALUE));
                } catch (SQLFeatureNotSupportedException e) {
                    matcher = SQL_VALIDATION_PATTERN.matcher(queryOrParamString);
                    if (!matcher.matches()) {
                        log.error(UNSUCCESSFUL_SQL_VALIDATION_PATTERN_SEMI, e);
                        throw newSecurityException(queryOrParamString);
                    }
                } catch (SQLException e) {
                    log.error(UNSUCCESSFUL_SQL_VALIDATION_PATTERN_SEMI, e);
                    throw newSecurityException(queryOrParamString);
                }
            } else {
                Matcher matcher = SQL_VALIDATION_PATTERN.matcher(queryOrParamString);
                if (!matcher.matches()) {
                    log.error(UNSUCCESSFUL_SQL_VALIDATION_PATTERN);
                    throw newSecurityException(queryOrParamString);
                }
            }
        }

        return true;
    }

    private static JSSecurityException newSecurityException(String sql) {
        String errMsg = (messages == null) ? ERR_MSG_SQL_VALIDATION:messages.getMessage(MSG_VALIDATION_SQL, new Object[]{}, LocaleContextHolder.getLocale());
        log.warn("Invalid SQL:"  + errMsg + ", SQL: " + sql);
        throw new JSSecurityException(errMsg);
    }

    // remove comments in sql queries. currently removing /**/, --, #.
    // This is optional and the user can delete the regexp from the properties file
    private static String removeCommentsFromSql(String sql) {
        String sqlCommentsRegexp = SecurityConfiguration.getProperty(SecurityConfiguration.SQL_COMMENTS_REGEXP);
        if (sqlCommentsRegexp != null && !sqlCommentsRegexp.isEmpty()) {
            if (sqlCommentsPattern.get() == null) {
                sqlCommentsPattern.compareAndSet(null, Pattern.compile(sqlCommentsRegexp, Pattern.DOTALL | Pattern.MULTILINE));
            }
            sql = sqlCommentsPattern.get().matcher(sql).replaceAll("");
        }
        return sql;
    }

    private static void trySqlSyntax(Connection connection, String sql) throws SQLException {
        sql = "SELECT * FROM (" + sql + ") SUBQUERY8173082FDC24 WHERE 1=0";
        ResultSetMetaData metaData = connection.prepareStatement(sql).getMetaData();
        if (metaData == null) {
            connection.createStatement().execute(sql);
        }
    }

    private static List<ValidatorRule> getRulesForParameter(String paramName) {
        List<ValidatorRule> paramRules = new ArrayList<ValidatorRule>();
        if (!validationRules.containsKey(paramName)) return paramRules;

        paramRules.add(validationRules.get(paramName));

        int count = 2;
        while(validationRules.containsKey(paramName + count)) {
            paramRules.add(validationRules.get(paramName + count));
            count ++;
        }

        return paramRules;
    }

    public static String getDefaultEncoding() {
        return DEFAULT_ENCODING.<String>get();
    }

    public static String getDefaultEncodingErrorMessage() {
        return DEFAULT_MESSAGE_ENCODING_ERROR.<String>get();
    }

    /*
     * Spring setters and getters for bean
     */
    public void setMessages(MessageSource messages) {
        this.messages = messages;
    }

    public MessageSource getMessages() {
        return messages;
    }

    public static void setProfileAttributesResolver(ProfileAttributesResolver profileAttributesResolver) {
        Validator.profileAttributesResolver = profileAttributesResolver;
    }

}

