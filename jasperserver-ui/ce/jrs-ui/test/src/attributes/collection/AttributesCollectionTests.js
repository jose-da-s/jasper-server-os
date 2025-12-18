/*
 * Copyright (C) 2025 the Jasper Server OS Authors
 * SPDX-License-Identifier: AGPL-3.0-or-later
 * Copyright (C) 2005 - 2022 TIBCO Software Inc. All rights reserved.
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

import sinon from 'sinon';
import AttributesCollection from 'src/attributes/collection/AttributesCollection';
import AttributeModel from 'src/attributes/model/AttributeModel';
import Backbone from 'backbone';
import $ from 'jquery';

describe("AttributesCollection Tests", function() {
    var attributesCollection,
        attrs = {
            attribute: [
                {name: "name1", value: "value1", description: "description1"},
                {name: "name2", value: "value2", description: "description2"},
                {name: "name3", value: "value3", description: "description3"}
            ]
        },
        defaultUrlPUTTemplate = "rest_v2/attributes?_embedded=permission",
        defaultUrlGETTemplate = defaultUrlPUTTemplate + "&group=custom",
        userContext = {
            urlGETTemplate: "rest_v2/attributes?includeInherited=true&holder=user:/{{=tenantId}}/{{=userName}}&group=custom",
            urlPUTTemplate: "rest_v2{{if (tenantId) { }}/organizations/{{=tenantId}}{{ } }}/users/{{=userName}}/attributes?_embedded=permission",
            tenantId: "organization1",
            userName: "user1"
        },
        orgContext = {
            urlGETTemplate: "rest_v2/attributes?_embedded=permission&includeInherited=true{{ if (id) { }}&holder=tenant:/{{=id}}{{ } }}&group=custom",
            urlPUTTemplate: "rest_v2{{ if (id) { }}/organizations/{{=id}}{{ } }}/attributes?_embedded=permission",
            id: "organization1"
        };

    it("should be properly initialized", function() {
        attributesCollection = new AttributesCollection();

        expect(attributesCollection.urlGETTemplate).toEqual(defaultUrlGETTemplate);
        expect(attributesCollection.urlPUTTemplate).toEqual(defaultUrlPUTTemplate);

        expect(attributesCollection.length).toBe(0);
    });

    it("should set context and fetch data from server level", function() {
        attributesCollection = new AttributesCollection();

        var fakeServer = sinon.fakeServer.create();

        var fetchSpy = sinon.spy(attributesCollection, "fetch");

        expect(attributesCollection.length).toBe(0);

        fakeServer.respondWith("GET", /rest_v2\/attributes\?_embedded=permission\&group=custom\&_=\d+/,
            [200, {"Content-Type": "application/json"}, JSON.stringify(attrs)]);

        attributesCollection.setContext();

        fakeServer.respond();

        expect(attributesCollection.length).toBe(3);
        expect(fetchSpy).toHaveBeenCalled();

        fetchSpy.restore();
        fakeServer.restore();
    });

    it("should set context and fetch data from tenant level", function() {

        var fakeServer = sinon.fakeServer.create();

        var attributesCollection = new AttributesCollection([], {context: orgContext});

        var fetchSpy = sinon.spy(attributesCollection, "fetch");

        fakeServer.respondWith("GET", /rest_v2\/attributes\?_embedded=permission\&includeInherited=true\&holder=tenant:\/organization1\&group=custom\&_=\d+/,
            [200, {"Content-Type": "application/json"}, JSON.stringify(attrs)]);

        expect(attributesCollection.length).toBe(0);

        attributesCollection.setContext(orgContext);

        fakeServer.respond();

        expect(attributesCollection.length).toBe(3);
        expect(fetchSpy).toHaveBeenCalled();

        fetchSpy.restore();
        fakeServer.restore();
    });

    it("should set context and fetch data from user level", function() {
        var fakeServer = sinon.fakeServer.create();

        var attributesCollection = new AttributesCollection([], {context: userContext});

        var fetchSpy = sinon.spy(attributesCollection, "fetch");

        expect(attributesCollection.length).toBe(0);

        fakeServer.respondWith("GET", /rest_v2\/attributes\?includeInherited=true\&holder=user:\/organization1\/user1\&group=custom\&_=\d+/,
            [200, {"Content-Type": "application/json"}, JSON.stringify(attrs)]);

        attributesCollection.setContext(userContext);

        fakeServer.respond();

        expect(attributesCollection.length).toBe(3);
        expect(fetchSpy).toHaveBeenCalled();

        fetchSpy.restore();
        fakeServer.restore();
    });

    it("should save attributes", function() {
        var attributesCollection = new AttributesCollection(),
            ajaxStub = sinon.stub($, "ajax").callsFake(() => $.Deferred()),
            _concatNamesSpy = sinon.spy(attributesCollection, "_concatNames"),
            _modelsToJSONSpy = sinon.spy(attributesCollection, "_modelsToJSON");

        var allModels = (new Backbone.Collection([
                {
                    name: "name1",
                    description: "desc1",
                    value: "value1"
                }
            ], {model: AttributeModel})).models,
            updatedModels = (new Backbone.Collection([
                {
                    name: "name2",
                    description: "desc2",
                    value: "value2"
                }
            ], {model: AttributeModel})).models;

        attributesCollection.save(allModels, updatedModels);

        expect(_modelsToJSONSpy).toHaveBeenCalledWith(updatedModels);
        expect(_concatNamesSpy).toHaveBeenCalledWith(allModels);
        expect(ajaxStub).toHaveBeenCalledWith({
            url: attributesCollection.url("PUT") + attributesCollection._concatNames(allModels),
            type: "PUT",
            cache: false,
            contentType: "application/hal+json",
            headers: {
                Accept: "application/hal+json",
                'Cache-Control': "no-cache, no-store",
                Pragma: "no-cache",
                'X-Suppress-Basic': "true"
            },
            data: JSON.stringify({"attribute": attributesCollection._modelsToJSON(updatedModels)})
        });

        _modelsToJSONSpy.restore();
        _concatNamesSpy.restore();
        ajaxStub.restore();
    });

    it("should search through attributes (respond from server is not empty)", function() {
        var attributesCollection = new AttributesCollection([], {context: {}});

        var fakeServer = sinon.fakeServer.create(),
            _successSearchCallbackSpy = sinon.spy(attributesCollection, "_successSearchCallback"),
            _modelsToJSONSpy = sinon.spy(attributesCollection, "_modelsToJSON");


        attributesCollection.context = {};

        fakeServer.respondWith("GET", /rest_v2\/attributes\?_embedded=permission\&group=custom\&name=name1\&recursive=true\&_=\d+/,
            [200, {"Content-Type": "application/json"}, JSON.stringify(attrs)]);

        var models = (new Backbone.Collection([
                {
                    name: "name1",
                    description: "description1",
                    value: "value1"
                }
            ], {model: AttributeModel})).models,
            newModels = (new Backbone.Collection([
                {
                    name: "name1",
                    description: "description1",
                    value: "value1"
                }
            ], {model: AttributeModel})).models;

        attributesCollection.validateSearch(models, newModels, false);

        fakeServer.respond();

        expect(_successSearchCallbackSpy).toHaveBeenCalled();
        expect(_modelsToJSONSpy).toHaveBeenCalled();
        expect(models[0].attr).toBeDefined();
        expect(models[0].attr).toEqual(attrs.attribute);

        _successSearchCallbackSpy.restore();
        _modelsToJSONSpy.restore();
        fakeServer.restore();
    });

    it("should search through attributes (respond from server is empty)", function() {
        var attributesCollection = new AttributesCollection([], {context: {}});

        var fakeServer = sinon.fakeServer.create(),
            _successSearchCallbackSpy = sinon.spy(attributesCollection, "_successSearchCallback"),
            _modelsToJSONSpy = sinon.spy(attributesCollection, "_modelsToJSON");


        attributesCollection.context = {};

        fakeServer.respondWith("GET", /rest_v2\/attributes\?_embedded=permission\&group=custom\&name=name1\&recursive=true\&_=\d+/,
            [200, {"Content-Type": "application/json"}, JSON.stringify({attribute: []})]);

        var models = (new Backbone.Collection([
                {
                    name: "name1",
                    description: "description1",
                    value: "value1"
                }
            ], {model: AttributeModel})).models,
            newModels = (new Backbone.Collection([
                {
                    name: "name1",
                    description: "description1",
                    value: "value1"
                },
                {
                    name: "name1",
                    description: "description1",
                    value: "value1"
                }
            ], {model: AttributeModel})).models;

        attributesCollection.validateSearch(models, newModels, false);

        fakeServer.respond();

        expect(_successSearchCallbackSpy).toHaveBeenCalled();
        expect(_modelsToJSONSpy).toHaveBeenCalled();
        expect(models[0].attr).toBeDefined();
        expect(models[0].attr).toEqual([
            {holder: "tenant:/"}
        ]);

        _successSearchCallbackSpy.restore();
        _modelsToJSONSpy.restore();
        fakeServer.restore();
    });

    it("should concat groups", function() {
        var attributesCollection = new AttributesCollection([], {context: {}});

        var groups = attributesCollection._concatGroups();

        expect(groups).toEqual('&group=log4j&group=aws&group=jdbc&group=adhoc&group=ji&group=customServerSettings');
    });

});