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

package com.jaspersoft.jasperserver.api.metadata.user.service.impl;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jaspersoft.jasperserver.api.JSConstraintViolationException;
import com.jaspersoft.jasperserver.api.JSException;
import com.jaspersoft.jasperserver.api.JSProfileAttributeException;
import com.jaspersoft.jasperserver.api.common.domain.AttributedObject;
import com.jaspersoft.jasperserver.api.common.domain.impl.ExecutionContextImpl;
import com.jaspersoft.jasperserver.api.metadata.common.domain.Folder;
import com.jaspersoft.jasperserver.api.metadata.common.domain.Resource;
import com.jaspersoft.jasperserver.api.metadata.common.domain.client.ResourceImpl;
import com.jaspersoft.jasperserver.api.metadata.user.domain.ProfileAttribute;
import com.jaspersoft.jasperserver.api.metadata.user.service.ProfileAttributeCategory;
import com.jaspersoft.jasperserver.api.metadata.user.service.ProfileAttributeEscapeStrategy;
import com.jaspersoft.jasperserver.api.metadata.user.service.ProfileAttributeService;
import com.jaspersoft.jasperserver.api.metadata.user.service.ProfileAttributesResolver;
import com.jaspersoft.jasperserver.dto.common.AttributeErrorCode;
import com.jaspersoft.jasperserver.dto.common.ErrorDescriptor;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import jakarta.annotation.PostConstruct;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.groups.Default;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.function.Supplier;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static com.jaspersoft.jasperserver.dto.common.AttributeErrorCode.Codes.PROFILE_ATTRIBUTE_SUBSTITUTION_CATEGORY_INVALID;
import static com.jaspersoft.jasperserver.dto.common.AttributeErrorCode.Codes.PROFILE_ATTRIBUTE_SUBSTITUTION_NOT_FOUND;
import static org.apache.commons.lang.StringUtils.isNotEmpty;

/**
 * The default {@link ProfileAttributesResolver} implementation.
 *
 * @author Volodya Sabadosh
 * @author Vlad Zavadskii
 */
public class ProfileAttributesResolverImpl implements ProfileAttributesResolver {
    private static final Log log = LogFactory.getLog(ProfileAttributesResolverImpl.class);

    // Names of the captured-groups for attribute placeholder patterns
    public static final String attributeNameGroup = "name";
    public static final String categoryGroup = "category";
    // Named-capturing group expression template
    public static final String groupExpression = "?<%1$s>";
    public static final String PROFILE_ATTRIBUTE_EXCEPTION_SUBSTITUTION_BASE =
            "profile.attribute.exception.substitution.base";
    public static final String IN_RESOURCE_SUFFIX =
            ".in.resource";

    @CheckAttributePatterns
    private List<String> attributePlaceholderPatterns;
    @CheckAttributePatterns
    private List<String> parametrizedResourcePatterns;
    @jakarta.annotation.Resource(name = "beanValidator")
    private Validator validator;
    private Pattern compiledAttributePlaceholderPattern;
    private Pattern compiledParametrizedResourcePattern;
    private ProfileAttributeService profileAttributeService;
    private ObjectMapper objectMapper;
    private List<ProfileAttributeCategory> profileAttributeCategories;
    private MessageSource messageSource;
    private Set<String> excludedResourcesFromAttrResolving;
    private boolean enabledResolving = true;

    public ProfileAttributesResolverImpl() {
        //Setup Polymorphic Object Mapper that do mapping using only class property fields (not getter and setter)
        objectMapper = new ObjectMapper();
        objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.setVisibility(objectMapper.getSerializationConfig().getDefaultVisibilityChecker()
                .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withCreatorVisibility(JsonAutoDetect.Visibility.NONE));
    }

    @PostConstruct
    public void init() {
        validator.validate(this);

        Set<ConstraintViolation<ProfileAttributesResolverImpl>> violations = validator.validate(this, Default.class);
        if (!violations.isEmpty()) {
            throw new JSConstraintViolationException("Failed to validate profileAttribute resolver.", violations);
        }

        compiledAttributePlaceholderPattern = compileAttributePattern(attributePlaceholderPatterns, "attributePlaceholderPatterns");
        compiledParametrizedResourcePattern = compileAttributePattern(parametrizedResourcePatterns, "parametrizedResourcePatterns");
    }

    /**
     * Checks if this service should perform profile attribute resolving for the specified resource.
     *
     * @param resource a resource to check
     * @return true if profile attribute resolving should be skipped. Otherwise returns false
     */
    public static boolean isSkipProfileAttributesResolving(AttributedObject resource) {
        return resource.getAttributes() != null && resource.getAttributes().contains(SKIP_PROFILE_ATTRIBUTES_RESOLVING);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsAttribute(String str) {
        return compiledAttributePlaceholderPattern.matcher(str).find();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isParametrizedResource(Object resource, ProfileAttributeCategory... categories) {
        try {
            OutputStream outputStream = new ByteArrayOutputStream();
            objectMapper.writeValue(outputStream, resource);
            String outputStreamStr = outputStream.toString();

            Scanner scanner = new Scanner(outputStreamStr);
            String foundAttribute;
            boolean hasAttributes = false;
            while ((foundAttribute = scanner.findInLine(compiledParametrizedResourcePattern)) != null || scanner.hasNextLine()) {
                if (foundAttribute == null) {
                    scanner.nextLine();
                    continue;

                }

                MatchResult matchResult = scanner.match();
                String foundCategory = matchResult.group(3);
                ProfileAttributeCategory attrCategory = ProfileAttributeCategory.HIERARCHICAL;

                if (foundCategory != null) {
                    try {
                        attrCategory = ProfileAttributeCategory.valueOf(foundCategory.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        continue;
                    }
                }
                hasAttributes = true;

                if (categories != null) {
                    for (ProfileAttributeCategory category : categories) {
                        if (!category.equals(attrCategory)) {
                            return false;
                        }
                    }
                } else {
                    return true;
                }
            }

            return hasAttributes;
        } catch (IOException e) {
            throw new JSException(e.toString());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T extends Resource> T mergeResource(T resource) {
        boolean resolve = enabledResolving && !isSkipProfileAttributesResolving(resource);

        if (!resolve || excludedResourcesFromAttrResolving.contains(resource.getClass().getCanonicalName())) {
            return resource;
        }

        String resourceUri = resource.getURIString();
        Resource baseResourceCopy = copyBaseResource(resource);
        escapeBaseResourceFieldsFromResolving(resource);

        T resultResource = mergeObject(resource, resourceUri);
        revertEscapedBaseResourceFields(baseResourceCopy, resultResource);
        revertEscapedBaseResourceFields(baseResourceCopy, resource);

        return resultResource;
    }

    public <T> T mergeObject(T object, String identifier) {
        try {
            OutputStream outputStream = new ByteArrayOutputStream();
            objectMapper.writeValue(outputStream, object);
            String outputStreamStr = outputStream.toString();

            Object result;
            if (containsAttribute(outputStreamStr)) {
                String mergedJSON = merge(outputStream.toString(), identifier);

                result = objectMapper.readValue(mergedJSON, object.getClass());
            } else {
                result = object;
            }
            return (T) result;
        } catch (IOException e) {
            throw new JSProfileAttributeException(
                    new ErrorDescriptor().setMessage(e.toString())
                            .setErrorCode(PROFILE_ATTRIBUTE_EXCEPTION_SUBSTITUTION_BASE));
        }
    }

    /**
     * {@inheritDoc}
     */
    public String merge(String templateString, String identifier) {
        return merge(templateString, identifier, null);
    }

    /**
     * {@inheritDoc}
     */
    public String merge(String templateString, String identifier, ProfileAttributeEscapeStrategy escapeStrategy) {
        Map<ProfileAttributeCategory, Map<String, ProfileAttribute>> profileAttributeCategoryMap =
                new HashMap<>();
        StringBuffer replacementBuffer = new StringBuffer();
        Matcher matcher = compiledAttributePlaceholderPattern.matcher(templateString);

        while (matcher.find()) {
            MatcherDecorator matcherDecorator = new MatcherDecorator(matcher);

            String attrPlaceholder = matcherDecorator.getPlaceholder();
            String attrName = matcherDecorator.getAttributeName();
            String foundCategory = matcherDecorator.getCategory();
            ProfileAttributeCategory attrCategory = ProfileAttributeCategory.HIERARCHICAL;

            if (foundCategory != null) {
                try {
                    attrCategory = ProfileAttributeCategory.valueOf(foundCategory.toUpperCase());
                } catch (IllegalArgumentException e) {
                    String[] args = new String[]{foundCategory, attrName, profileAttributeCategories.toString()};

                    throw generateProfileAttributeException(PROFILE_ATTRIBUTE_SUBSTITUTION_CATEGORY_INVALID,
                            templateString, identifier, attrPlaceholder, args);
                }
            }

            Map<String, ProfileAttribute> profileAttributeMap;
            if (!profileAttributeCategoryMap.containsKey(attrCategory)) {
                profileAttributeMap = getProfileAttributeMap(attrCategory);
                profileAttributeCategoryMap.put(attrCategory, profileAttributeMap);
            } else {
                profileAttributeMap = profileAttributeCategoryMap.get(attrCategory);
            }

            if (profileAttributeMap.containsKey(attrName)) {
                String attrValue = profileAttributeMap.get(attrName).getAttrValue();
                if (escapeStrategy != null) {
                    attrValue = escapeStrategy.escape(attrValue);
                }
                matcher.appendReplacement(replacementBuffer, quoteMatcherReplacement(attrValue));

                if (log.isDebugEnabled()) {
                    log.debug(messageSource.getMessage("profile.attribute.debug.substitution.success",
                            new Object[]{attrName, identifier, getErrorFieldName(templateString, attrPlaceholder), attrValue},
                            LocaleContextHolder.getLocale()));
                }
            } else {
                String[] args = new String[]{attrName, attrCategory.getLabel()};

                throw generateProfileAttributeException(PROFILE_ATTRIBUTE_SUBSTITUTION_NOT_FOUND,
                        templateString, identifier, attrPlaceholder, args);
            }
        }
        matcher.appendTail(replacementBuffer);

        return replacementBuffer.toString();
    }

    protected String quoteMatcherReplacement(String attrValue) {
        return Matcher.quoteReplacement(attrValue);
    }

    JSProfileAttributeException generateProfileAttributeException(String baseErrorCode, String templateString,
                                                                  String identifier, String attrPlaceholder, String[] baseArgs) {
        String[] resourceSpecificErrorArgs = getResourceSpecificErrorArgs(templateString, identifier, attrPlaceholder);
        String errorCode = baseErrorCode;

        String[] messageBundleArgs = baseArgs;
        if (resourceSpecificErrorArgs != null) {
            errorCode = baseErrorCode.concat(IN_RESOURCE_SUFFIX);
            messageBundleArgs = (String[])ArrayUtils.addAll(resourceSpecificErrorArgs, baseArgs);
        }

        String localizedMessage = messageSource.getMessage(errorCode, messageBundleArgs, LocaleContextHolder.getLocale());

        log.error(localizedMessage);

        return new JSProfileAttributeException(localizedMessage, AttributeErrorCode.
                fromCode(errorCode).createDescriptor((Object[])messageBundleArgs));
    }

    String[] getResourceSpecificErrorArgs(String templateString, String identifier, String attrPlaceholder) {
        String[] baseErrorArgs = null;
        if (isNotEmpty(identifier) && !identifier.equals(Folder.SEPARATOR)) {
            baseErrorArgs = new String[]{identifier, getErrorFieldName(templateString, attrPlaceholder)};
        }

        return baseErrorArgs;
    }

    protected Map<String, ProfileAttribute> getProfileAttributeMap(ProfileAttributeCategory category) {
        Map<String, ProfileAttribute> profileAttributeMap = new HashMap<String, ProfileAttribute>();
        List<ProfileAttribute> profileAttributes = profileAttributeService.
                getCurrentUserProfileAttributes(ExecutionContextImpl.getRuntimeExecutionContext(), category);

        if (profileAttributes != null) {
            for (ProfileAttribute profileAttribute : profileAttributes) {
                profileAttributeMap.put(profileAttribute.getAttrName(), profileAttribute);
            }
        }

        return profileAttributeMap;
    }

    protected String getErrorFieldName(String templateString, String attrPlaceholder) {
        String attrLiteral = Pattern.quote(attrPlaceholder);

        Supplier<Pattern> findJsonFieldNamePattern =
                () -> Pattern.compile("(?m)\"(?<name>[\\w$]+)\"\\s*:\\s*\"([^\"]|(\\\\\"))*" + attrLiteral);
        Supplier<Pattern> findXmlFieldNamePattern =
                () -> Pattern.compile("(?s)<(?<name>[\\w.-]+)((?!<[\\w.-]+).)+" + attrLiteral);

        return Stream.of(findJsonFieldNamePattern, findXmlFieldNamePattern)
                .map(pattern -> {
                    Matcher matcher = pattern.get().matcher(templateString);
                    if (matcher.find()) {
                        return matcher.group("name");
                    } else {
                        return "";
                    }
                })
                .filter(name -> !name.isEmpty())
                .findFirst()
                .orElse("");
    }

    private Resource copyBaseResource(final Resource resource) {
        Resource baseResourceCopy = new ResourceImpl() {
            @Override
            protected Class getImplementingItf() {
                return resource.getClass();
            }
        };
        baseResourceCopy.setDescription(resource.getDescription());
        baseResourceCopy.setLabel(resource.getLabel());

        return baseResourceCopy;
    }

    private void revertEscapedBaseResourceFields(Resource copyBaseResource, Resource originalResource) {
        originalResource.setDescription(copyBaseResource.getDescription());
        originalResource.setLabel(copyBaseResource.getLabel());
    }

    private void escapeBaseResourceFieldsFromResolving(Resource resource) {
        resource.setDescription("");
        resource.setLabel("");
    }

    private Pattern compileAttributePattern(List<String> patterns, String propertyName) {
        StringBuilder sb = new StringBuilder();
        int uniqueId = 0;
        List<String> allGroups = Arrays.asList(attributeNameGroup, categoryGroup);
        for (String attributePattern : patterns) {
            // Make captured-groups unique - each group will have its own unique name (e.g. name0, category0, name1 etc)
            for (String group : allGroups) {
                String oldGroup = String.format(groupExpression, group);
                String newGroup = String.format(groupExpression, group + uniqueId);
                attributePattern = attributePattern.replace(oldGroup, newGroup);
            }

            uniqueId++;
            sb.append("(?:").append(attributePattern).append(")").append("|");
        }

        sb.deleteCharAt(sb.length() - 1);

        return Pattern.compile(sb.toString());
    }

    public void setAttributePlaceholderPatterns(List<String> attributePlaceholderPatterns) {
        this.attributePlaceholderPatterns = attributePlaceholderPatterns;
    }

    public void setProfileAttributeService(ProfileAttributeService profileAttributeService) {
        this.profileAttributeService = profileAttributeService;
    }

    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public void setProfileAttributeCategories(List<ProfileAttributeCategory> profileAttributeCategories) {
        this.profileAttributeCategories = profileAttributeCategories;
    }

    public void setExcludedResourcesFromAttrResolving(Set<String> excludedResourcesFromAttrResolving) {
        this.excludedResourcesFromAttrResolving = excludedResourcesFromAttrResolving;
    }

    public void setEnabledResolving(boolean enabledResolving) {
        this.enabledResolving = enabledResolving;
    }

    public void setParametrizedResourcePatterns(List<String> parametrizedResourcePatterns) {
        this.parametrizedResourcePatterns = parametrizedResourcePatterns;
    }

    public void setValidator(Validator validator) {
        this.validator = validator;
    }

    private class MatcherDecorator {
        private Matcher matcher;
        private int usedPattern;

        public MatcherDecorator(Matcher matcher) {
            this.matcher = matcher;

            for (int i = 0; i < attributePlaceholderPatterns.size(); i++) {
                if (matcher.group(attributeNameGroup + i) != null) {
                    usedPattern = i;
                    break;
                }
            }
        }

        public String getAttributeName() {
            return matcher.group(attributeNameGroup + usedPattern);
        }

        public String getCategory() {
            return matcher.group(categoryGroup + usedPattern);
        }

        public String getPlaceholder() {
            return matcher.group(0);
        }
    }

}

