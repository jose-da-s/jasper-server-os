/*
 * Copyright (C) 2025 the Jasper Server OS Authors
 * SPDX-License-Identifier: AGPL-3.0-or-later
 * Copyright (C) 2005 - 2020 TIBCO Software Inc. All rights reserved.
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

const webpackConfigFactory = require("bi-report/webpack.config");
const path = require("path");
const fs = require("fs");
const TerserPlugin = require('terser-webpack-plugin');
const ruleTest = require('js-sdk/webpack/utils/ruleTest');

const prototypePath = 'prototype/dist/prototype';
const prototypeCacheGroup = 'prototypejs';

module.exports = (env = {}, argv = {mode: 'development'}) => {
    const webpackConfig = webpackConfigFactory(env, argv);
    const {mode} = argv;
    const isProduction = mode === 'production';
    const {test = false} = env;

    const prototypeImport = {
        'prototype': 'prototype',
        '$': '>prototype.$',
        '$$': '>prototype.$$',
        '$w': '>prototype.$w',
        'Prototype': '>prototype.Prototype',
        'Position': '>prototype.Position',
        'Hash': '>prototype.Hash',
        '$A': '>prototype.$A',
        'Template': '>prototype.Template',
        'Class': '>prototype.Class',
        '$F': '>prototype.$F',
        'Form': '>prototype.Form',
        '$break': '>prototype.$break',
        '$H': '>prototype.$H',
        'Selector': '>prototype.Selector',
        'Field': '>prototype.Field',
        'Enumerable': '>prototype.Enumerable'
    };

    const dragdropImport = {
        'dragdrop': 'scriptaculous/src/dragdrop',
        'Droppables': '>dragdrop.Droppables',
        'Draggables': '>dragdrop.Draggables',
        'Draggable': '>dragdrop.Draggable',
        'Sortable': '>dragdrop.Sortable'
    };

    const webpackBundlesPath = process.env.WEBPACK_DEV_SERVER_PUBLIC_PATH;
    const rootPath = process.env.ROOT_PATH;
    const contentBasePublicPath = '/sources'
    const themeName = process.env.DEV_SERVER_THEME_NAME;
    const themeFolderPath = path.join(process.cwd(), process.env.BUILD_THEMES_DIR)
    const themeFolderPublicPath = `${contentBasePublicPath}/${process.env.THEMES_FOLDER_NAME}`
    const serveThemes = process.env.DEV_SERVER_THEMES === 'true';

    // /jasperserver/runtime/A1VER2/scripts or /jasperserver/scripts
    const scriptsPattern = new RegExp(`/${rootPath}/(runtime/[^/]+/)?scripts`);
    // /jasperserver/_themes/A1VER2 or /jasperserver/_themes/default or /jasperserver/runtime/A1VER2/themes
    const themesPattern = new RegExp(`^/${rootPath}/(runtime/[^/]+/themes|(_themes|themes)/[^/]+)`);
    const webFontPattern = new RegExp(`\.(ttf|woff|woff2)$`);

    return {
        ...webpackConfig,
        resolve: {
            ...webpackConfig.resolve,
            alias: {
                ...webpackConfig.resolve.alias,
                'prototype': prototypePath,
                ...(isProduction ? {
                    'react': 'react/umd/react.production.min',
                    'react-dom': 'react-dom/umd/react-dom.production.min',
                    'redux': 'redux/dist/redux.min',
                    'react-redux': 'react-redux/dist/react-redux.min'
                } : {}),
                ...(!test ? {
                    //this alias is used in webpack/loaders/settingsLoader
                    'settingsStore': path.resolve(__dirname, 'src/settings/settingsStore'),
                    //this alias is used in webpack/loaders/bundleLoader
                    'bundleStore': path.resolve(__dirname, 'src/i18n/bundleStore'),
                } : {})
            },
            modules: isProduction ? [
                path.resolve(process.cwd(), process.env.RUNTIME_DEPENDENCIES),
                // added to be able to find faf modules
                path.resolve(__dirname, '..')
            ] : webpackConfig.resolve.modules
        },
        resolveLoader: !test ? {
            modules: [
                'node_modules',
                path.resolve(__dirname, './webpack/loaders')
            ]
        } : undefined,
        module: {
            ...webpackConfig.module,
            rules: [
                ...webpackConfig.module.rules,
                !test ? {
                    test: /\.properties\.(js|ts)$/,
                    use: ['bundleLoader']
                } : {},
                !test ? {
                    test: /\.settings\.(js|ts)$/,
                    use: ['settingsLoader']
                } : {},
                {
                    test: ruleTest(prototypePath),
                    use: [
                        'imports-loader?this=>window,define=>false,module=>false',
                        {
                            loader: 'exports-loader',
                            options: {
                                '$': '$',
                                '$$': '$$',
                                '$w': '$w',
                                'Prototype': 'Prototype',
                                'Position': 'Position',
                                'Hash': 'Hash',
                                '$A': '$A',
                                'Template': 'Template',
                                'Class': 'Class',
                                '$F': '$F',
                                'Form': 'Form',
                                '$break': '$break',
                                '$H': '$H',
                                'Selector': 'Selector',
                                'Field': 'Field',
                                'Enumerable': 'Enumerable'
                            }
                        }
                    ]
                },
                //provide prototype to scriptaculous
                //and use proper global var as a default export
                {
                    test: ruleTest("scriptaculous/src/effects"),
                    use: [
                        {
                            loader: 'imports-loader',
                            options: prototypeImport
                        },
                        'exports-loader?Effect'
                    ]
                },
                {
                    test: ruleTest("scriptaculous/src/builder"),
                    use: [
                        {
                            loader: 'imports-loader',
                            options: Object.assign({
                                'this': '>window'
                            }, prototypeImport)
                        },
                        'exports-loader?Builder'
                    ]
                },
                {
                    test: ruleTest("scriptaculous/src/dragdrop"),
                    use: [
                        {
                            loader: 'imports-loader',
                            options: Object.assign({
                                'Effect': 'scriptaculous/src/effects'
                            }, prototypeImport)
                        },
                        'exports-loader?Droppables,Draggables,Draggable,Sortable'
                    ]
                },
                {
                    test: ruleTest('dragdropextra/dragdropextra'),
                    use: [
                        {
                            loader: 'imports-loader',
                            options: Object.assign({
                                'jQuery': 'jquery'
                            }, prototypeImport, dragdropImport)
                        },
                        'exports-loader?Droppables,Draggables,Draggable,Sortable'
                    ]
                }
            ]
        },
        entry: !test ? {
            'commons/commonsMain': "./src/commons/commonsMain",
            'addResource/dataType/addDataTypeMain': "./src/addResource/dataType/addDataTypeMain",
            'addResource/fileResource/addFileResourceMain': "./src/addResource/fileResource/addFileResourceMain",
            'addResource/inputControls/addInputControlMain': "./src/addResource/inputControls/addInputControlMain",
            'addResource/inputControls/addInputControlQueryInformationMain': "./src/addResource/inputControls/addInputControlQueryInformationMain",
            'addResource/inputControls/dataTypeLocateMain': "./src/addResource/inputControls/dataTypeLocateMain",
            'addResource/inputControls/listOfValuesLocateMain': "./src/addResource/inputControls/listOfValuesLocateMain",
            'addResource/jasperReport/addJasperReportMain': "./src/addResource/jasperReport/addJasperReportMain",
            'addResource/jasperReport/addJasperReportLocateControlMain': "./src/addResource/jasperReport/addJasperReportLocateControlMain",
            'addResource/jasperReport/addJasperReportResourceNamingMain': "./src/addResource/jasperReport/addJasperReportResourceNamingMain",
            'addResource/jasperReport/addJasperReportResourcesAndControlsMain': "./src/addResource/jasperReport/addJasperReportResourcesAndControlsMain",
            'addResource/listOfValues/addListOfValuesMain': "./src/addResource/listOfValues/addListOfValuesMain",
            'addResource/query/addQueryMain': "./src/addResource/query/addQueryMain",
            'addResource/query/addQueryWithResourceLocatorMain': "./src/addResource/query/addQueryWithResourceLocatorMain",
            'administer/administerCustomAttributesMain': "./src/administer/administerCustomAttributesMain",
            'administer/administerExportMain': "./src/administer/administerExportMain",
            'administer/administerImportMain': "./src/administer/administerImportMain",
            'administer/administerLoggingMain': "./src/administer/administerLoggingMain",
            'administer/administerAwsOptionsMain': "./src/administer/administerAwsOptionsMain",
            'administer/resetSettings/resetSettingsMain': "./src/administer/resetSettings/resetSettingsMain",
            'administer/administerGeneralSettingsMain': "./src/administer/administerGeneralSettingsMain",
            'manage/manageRolesMain': "./src/manage/manageRolesMain",
            'manage/manageUsersMain': "./src/manage/manageUsersMain",
            'dataSource/dataSourceMain': "./src/dataSource/dataSourceMain",
            'addResource/query/locateQueryMain': "./src/addResource/query/locateQueryMain",
            'login/loginMain': "./src/login/loginMain",
            'reportViewer/reportViewerMain': "./src/reportViewer/reportViewerMain",
            'repository/repositoryMain': "./src/repository/repositoryMain",
            'messages/details/messageDetailsMain': "./src/messages/details/messageDetailsMain",
            'messages/list/messageListMain': "./src/messages/list/messageListMain",
            'scheduler/schedulerMain': "./src/scheduler/schedulerMain",
            'encrypt/encryptMain': "./src/encrypt/encryptMain",
            'system/systemErrorMain': "./src/system/systemErrorMain",
            'system/errorMain': "./src/system/errorMain"
        } : undefined,
        devServer: !test ? {
            host: process.env.DEV_SERVER_HOST,
            writeToDisk: process.env.DEV_SERVER_WRITE_TO_DISK === 'true',
            contentBase: [
                themeFolderPath
            ],
            contentBasePublicPath: [
                themeFolderPublicPath
            ],
            watchContentBase: true,
            publicPath: webpackBundlesPath,
            compress: false,
            disableHostCheck: true,
            port: Number(process.env.DEV_SERVER_PORT),
            openPage: rootPath,
            overlay: {
                warnings: false,
                errors: true
            },
            before: (app, server, compiler) => {
                app.all('*', function(req, res, next) {
                    if (req.url.match(webFontPattern)) {
                        // Fix CORS for fonts
                        res.setHeader('Access-Control-Allow-Origin', '*')
                    }

                    next();
                });
            },
            proxy: {
                // dev server proxy config
                // see https://webpack.js.org/configuration/dev-server/#devserverproxy
                '*': {
                    target: process.env.HOST,
                    bypass: function (req) {
                        const url = req.url;
                        if (url.match(scriptsPattern)) {
                            return url.replace(scriptsPattern, webpackBundlesPath);
                        } else if (url.match(themesPattern) && serveThemes) {
                            let result = url.replace(themesPattern, `${themeFolderPublicPath}`);

                            const shouldApplyTheme = !url.match(/reset\.css$/);
                            if (shouldApplyTheme) {
                                result = url.replace(themesPattern, `${themeFolderPublicPath}/${themeName}`);
                                if (themeName !== 'default') {
                                    // for non-default theme we are trying to emulate
                                    // cascading themebility feature: first check if current theme file exists
                                    // and if not - fallback to default theme
                                    const themeFilePath = url.replace(themesPattern, `${themeFolderPath}/${themeName}`);
                                    if (!fs.existsSync(path.resolve(themeFilePath))) {
                                        result = url.replace(themesPattern, `${themeFolderPublicPath}/default`)
                                    }
                                }
                            }

                            return result;
                        } else {
                            return null;
                        }
                    },
                    cookiePathRewrite: {
                        "*": "/"
                    },
                    autoRewrite: "true",
                    logLevel: "debug"
                }
            }
        } : undefined,
        optimization: {
            minimize: isProduction && process.env.WEBPACK_OPTIMIZATION_MINIMIZE === 'true',
            // moduleIds: 'natural',
            // chunkIds: 'named',
            // runtimeChunk: {
            //     name: 'runtime'
            // },
            minimizer: [
                new TerserPlugin({
                    extractComments: false,
                    // JS-60254: Prototype.js is not working properly when minimized.
                    // we have to extract it to a separate chunk
                    // and skip minification for it
                    exclude: new RegExp(`${prototypeCacheGroup}`),
                    terserOptions: {
                        // https://github.com/webpack-contrib/terser-webpack-plugin#terseroptions
                    }
                }),
            ],
            splitChunks: !test ? {
                // chunks: 'async',
                // minSize: 20000,
                // minRemainingSize: 0,
                // maxSize: 0,
                // minChunks: 1,
                // maxAsyncRequests: 30,
                // maxInitialRequests: 30,
                // automaticNameDelimiter: '~',
                // enforceSizeThreshold: 50000,
                cacheGroups: {
                    prototypejs: {
                        // JS-60254: Prototype.js is not working properly when minimized.
                        // we have to extract it to a separate chunk
                        // and skip minification for it
                        name: prototypeCacheGroup,
                        test(module, chunks) {
                            return module.resource &&
                                module.resource.replace(/\\/g, "/").includes(prototypePath);
                        },
                        priority: 10
                    },
                    runtimeDependencies: {
                        test: new RegExp(`[\\/]${process.env.RUNTIME_DEPENDENCIES}[\\/]`),
                        priority: 0
                    },
                    // defaultVendors: {
                    //     test: /[\\/]node_modules[\\/]/,
                    //     priority: -10
                    // },
                    // default: {
                    //     minChunks: 2,
                    //     priority: -20,
                    //     reuseExistingChunk: true
                    // }
                }
            } : undefined
        },
        output: !test ? {
            path: path.resolve(process.cwd(), `./${process.env.BUILD_SCRIPTS_DIR}`),
            publicPath: process.env.WEBPACK_PUBLIC_PATH,
            filename: '[name].js',
            chunkFilename: '_chunks/chunk.[name].js',
            chunkLoadTimeout: Number(process.env.SCRIPT_LOAD_TIMEOUT_SEC || 60) * 1000
        } : undefined
    };
}
