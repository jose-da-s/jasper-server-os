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

package com.jaspersoft.jasperserver.remote.discovery.strategy;

import com.jaspersoft.jasperserver.api.JSException;
import com.jaspersoft.jasperserver.api.common.domain.ExecutionContext;
import com.jaspersoft.jasperserver.api.common.domain.impl.ExecutionContextImpl;
import com.jaspersoft.jasperserver.api.metadata.common.domain.FileResourceData;
import com.jaspersoft.jasperserver.api.metadata.common.domain.Folder;
import com.jaspersoft.jasperserver.api.metadata.common.domain.Resource;
import com.jaspersoft.jasperserver.api.metadata.common.domain.ResourceReference;
import com.jaspersoft.jasperserver.api.metadata.common.service.RepositoryService;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.ReportUnit;
import com.jaspersoft.jasperserver.dto.discovery.Parameter;
import com.jaspersoft.jasperserver.remote.discovery.DiscoveryStrategy;
import org.springframework.stereotype.Component;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.util.*;

/**
 * <p>Performs discovery of different abilities of resources</p>
 *
 * @author Zakhar.Tomchenco
 * @version $Id: $
 */
@Component
public class ReportUnitDiscoveryStrategy implements DiscoveryStrategy<ReportUnit> {
    private final static Set<String> technicalParams = Collections.unmodifiableSet(new HashSet(Arrays.asList(
            "hyperlinkType", "hyperlinkTarget"
            )));

    @jakarta.annotation.Resource(name = "concreteRepository")
    protected RepositoryService repositoryService;

    protected final ExecutionContext executionContext = ExecutionContextImpl.getRuntimeExecutionContext();

    private Map<String, Map<Integer, JRXMLParametersExtractor>> extractorCache = new HashMap<String, Map<Integer, JRXMLParametersExtractor>>();


    @Override
    public List<Parameter> discoverParameters(ReportUnit resource) {
        return obtainCachedExtractor(resource).parameters;
    }

    @Override
    public List<Parameter> discoverOutputParameters(ReportUnit resource) {
        return obtainCachedExtractor(resource).outputParameters;
    }

    @Override
    public Class<ReportUnit> getSupportedResourceType() {
        return ReportUnit.class;
    }

    protected ResourceReference obtainBaseReference(ReportUnit reportUnit) {
        return reportUnit.getMainReport();
    }

    protected JRXMLParametersExtractor obtainExtractor(ReportUnit reportUnit, ResourceReference baseReference) {
        JRXMLParametersExtractor extractor = new JRXMLParametersExtractor();
        FileResourceData data = repositoryService.getResourceData(executionContext, baseReference.getTargetURI());

        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser saxParser = factory.newSAXParser();
            saxParser.parse(data.getDataStream(), extractor);

            fillWithUri(extractor.parameters, reportUnit);
        } catch (Exception e) {
            throw new JSException(e);
        }
        return extractor;
    }

    protected Integer determineVersion(ResourceReference reference) {
        Integer version;
        if (reference.isLocal()){
            version = reference.getLocalResource().getVersion();
        } else {
            if (reference.getReferenceLookup() == null){
                Resource referenced = repositoryService.getResource(executionContext, reference.getReferenceURI());
                if (referenced == null){
                    throw new JSException("Invalid uri: " + reference.getReferenceURI());
                }
                version = referenced.getVersion();
            } else {
                version = reference.getReferenceLookup().getVersion();
            }
        }

        return version;
    }

    private JRXMLParametersExtractor obtainCachedExtractor(ReportUnit reportUnit){
        JRXMLParametersExtractor extractor = null;

        ResourceReference baseReference = obtainBaseReference(reportUnit);

        String baseUri = baseReference.getTargetURI();
        Integer currentVersion = determineVersion(baseReference);

        Map<Integer, JRXMLParametersExtractor> versions = extractorCache.get(baseUri);

        if (versions == null || !versions.containsKey(currentVersion)) {
            synchronized (baseUri){
                if (!extractorCache.containsKey(baseUri)) {
                    versions = new HashMap<Integer, JRXMLParametersExtractor>();
                    extractorCache.put(baseUri, versions);
                }

                if (!versions.containsKey(currentVersion)){
                    extractor = obtainExtractor(reportUnit, baseReference);

                    versions.clear();
                    versions.put(currentVersion, extractor);
                }
            }
        }

        versions.clear();

        return extractor == null ? versions.get(currentVersion) : extractor;
    }

    private void fillWithUri(List<Parameter> parameters, ReportUnit reportUnit){
        List<ResourceReference> controls = reportUnit.getInputControls();

        if (controls != null){
            for (ResourceReference control : controls){
                String name = control.getTargetURI().substring(control.getTargetURI().lastIndexOf(Folder.SEPARATOR) + Folder.SEPARATOR_LENGTH);

                for (Parameter parameter : parameters){
                    if (name.equals(parameter.getId())){
                        parameter.setUri(control.getTargetURI());
                        break;
                    }
                }
            }
        }
    }

    protected class JRXMLParametersExtractor extends DefaultHandler {
        List<Parameter> parameters = new ArrayList<Parameter>();
        List<Parameter> outputParameters = new ArrayList<Parameter>();


        private boolean isInHighChart = false;
        private boolean isInSubDataset = false;

        public JRXMLParametersExtractor(){}

        public JRXMLParametersExtractor(List<Parameter> parameters, List<Parameter> outputParameters){
            this.parameters = parameters;
            this.outputParameters = outputParameters;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if (qName.equalsIgnoreCase("subDataset")){
                isInSubDataset = true;
            } else

            if (!isInSubDataset && qName.equalsIgnoreCase("parameter")){
                Parameter param = new Parameter().setId(attributes.getValue("name")).setLabel(attributes.getValue("name"));

                if (attributes.getValue("nestedType") == null){
                    try {
                        if (Collection.class.isAssignableFrom(Class.forName(attributes.getValue("class")))){
                            param.setValueType(String.class.getName());
                            param.setMultipleValues(true);
                        } else {
                            param.setValueType(attributes.getValue("class"));
                        }
                    } catch (ClassNotFoundException e) {
                        param.setValueType(attributes.getValue("class"));
                    }
                } else {
                    param.setValueType(attributes.getValue("nestedType"));
                    param.setMultipleValues(true);
                }

                parameters.add(param);
            } else

            if (qName.equalsIgnoreCase("hc:contributor")) {
                if (attributes.getValue("name").equals("SeriesItemHyperlink")) {
                    isInHighChart = true;
                }
            } else

            if (qName.equalsIgnoreCase("hc:contributorProperty")) {
                if (isInHighChart && !technicalParams.contains(attributes.getValue("name"))) {
                    outputParameters.add(new Parameter()
                            .setId(attributes.getValue("name"))
                            .setLabel(attributes.getValue("name"))

                    );
                }
            }

            super.startElement(uri, localName, qName, attributes);
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (isInHighChart && qName.equalsIgnoreCase("hc:contributor")) {
                isInHighChart = false;
            } else

            if (qName.equalsIgnoreCase("subDataset")){
                isInSubDataset = false;
            }

            super.endElement(uri, localName, qName);
        }
    }
}

