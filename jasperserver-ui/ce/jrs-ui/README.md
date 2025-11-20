# jrs-ui

Jasper Server OS Community edition WEB UI

This is *basic* development documentation.

For *advanced* development documentation see here [./README_DEVELOPMENT.md](./README_DEVELOPMENT.md)

Table of Contents:

- [Current folder](#current-folder)
- [Set Up](#set-up)
- [Parametrization](#parametrization)
    - [Pass parameters through custom file](#pass-parameters-through-custom-file)
    - [Pass parameters through environment variables](#pass-parameters-through-environment-variables)
- [Build](#build)
- [Deploy](#deploy)
    - [Deploy to local maven repository](#deploy-to-local-maven-repository)
    - [Deploy theme](#deploy-theme)
      - [Deploy theme to the repository DB for production](#deploy-theme-to-the-repository-db-for-production)
      - [Deploy theme to the filesystem for development](#deploy-theme-to-the-filesystem-for-development)
    - [Deploy localization bundles](#deploy-localization-bundles)
- [Rapid development environment](#rapid-development-environment)
    - [Webpack Dev Server](#webpack-dev-server)
        - [Start Dev Server](#start-dev-server)
        - [Develop default theme](#develop-default-theme)
        - [Changes in localization bundles](#changes-in-localization-bundles)

## Current folder
All commands mentioned in this document (except commands for [Set Up](#set-up))
should be executed in a current folder.
In case of Community edition WEB UI this is `jrs-ui` folder

## Set Up
Read [../README.md#set-up](../README.md#set-up)

## Parametrization
All default parameter values used for all commands which described here are set in `.env` file.

### Pass parameters through custom file
> Linux/MacOS/Windows compatible.

If you want to override any parameter - create `.env.local` file in this folder and 
create a parameter which you want to override there.

For example. If you want to override parameter `JRSInstancePath`, create file `.env.local` next to `.env`
and add the following line to this file:
```properties
JRSInstancePath=c:/tomcat/webapps/jasperserver
```

### Pass parameters through environment variables
> Linux/MacOS compatible.

Another option for Linux/MacOS is to set environment variable before running command which uses given parameter.

For example, you can override `JRSInstancePath` during running `yarn run deploy` command:
```shell script
JRSInstancePath="/usr/share/tomcat/webapps/jasperserver" yarn run deploy
```

## Build
To prepare javascript files for deployment run the following commands in the `current folder`:
```shell script
yarn run clean
yarn run build
```

Build output will be placed here: `jrs-ui/build/overlay/scripts`

Read [Deploy](#deploy) section to learn how to deploy these files to the production instance
of **Jasper Server OS**

Available build parameters (see [Parametrization](#parametrization) section):
- `NODE_ENV`: 
    - default value: `production`.
    - Available values: 
        - `development`: used during development. Build will be much faster 
        but size of the built scripts will be much bigger. 
        This type of the build has not been tested and thus is not officially supported.
        - `production`: used for production. Build is slower but is more optimized.       

## Deploy
> If you want to test your js/theme changes before deployment to the server -
> consider usage of [Rapid development environment](#rapid-development-environment) before deployment

After source code build has been completed successfully, build output could be deployed
to the JRS deployment folder.

> If localization bundles and themes also should be deployed - prepare them for deployment first
> by running `yarn run overlay` in `current folder`

For example, let's assume that our app server is **Tomcat** and **Jasper Server OS** deployed here:
`c:\tomcat\webapps\jasperserver`.

To deploy our build to Jasper Server OS instance we have to do the following:
- set `JRSInstancePath=c:/tomcat/webapps/jasperserver` (see [Parametrization](#parametrization))
- run command in a `current folder`:
    ```shell script
    yarn run deploy
    ```

This command will copy content of the following folder `jrs-ui/build/overlay` to the path specified by `JRSInstancePath`
variable

`jrs-ui/build/overlay` folder might contain several sub-folders:
  - `scripts`: optimized JavaScript bundles (this folder is the result of `yarn run build` command)
  - `WEB-INF/bundles`: UI i18n bundles (created by `yarn run overlay` command)
  - `themes`: css and images (created by `yarn run overlay` command)

`yarn run deploy` will copy all three folders to the local JRS instance.

Now changes in js, themes and i18n bundles should be available on the JRS instance.

Changes in themes (css and images) and i18n bundles might not be visible immediately after the upload:
- Usually server restart is necessary to see changes in i18n bundles
- Themes usually should be uploaded to the repository DB to work properly
  read [Deploy theme](#deploy-theme) to learn how to deploy the updated theme).
  also read [Develop default theme](#default-theme-css-and-images-livereload).


> If your JRS instance is not accessible via file system - ask JRS instance admin about how to deploy WEB UI.

### Deploy to local maven repository
If you are developing both frontend and backend of the **Jasper Server OS**
most likely you want updated fronted distribution to be picked up during backend build.
 
Backend build process expects frontend distribution to be deployed to the maven repository.

So before starting backend build we can publish our frontend distribution to the local maven repository,
so backend build will pick it up.

In order to do this follow these steps:
- set `BUILD_ARTIFACT_VERSION_NAME=$version` env variable (see [Parametrization](#parametrization)).
  value of `$version` should be the same as defined in file `jasperserver-war/pom.xml`:
    ```xml
    <dependency>
        <groupId>com.jaspersoft</groupId>
        <artifactId>jrs-ui</artifactId>
        <version>$version</version>
        <type>zip</type>
    </dependency>
    ```

- run
    ```shell script
    yarn run mvnPublish
    ```
> You should have `maven` set up on your local env
> Setting up backend build (which includes `maven`) is beyond this documentation

### Deploy theme
Prepare theme for deployment:
```shell
yarn run overlay
```
this will build and copy all theme-related files to `build/overlay/themes` folder

> Note: also i18n bundles will be prepared with this command

#### Deploy theme to the repository DB for production
- Change dir to the necessary theme, let's say `default`:
    ```shell script
    cd build/overlay/themes/default
    ``` 
- Compress content for the selected theme folder using `zip`:
    - on OSX/Linux run: `zip -r default.zip .`
    - on Windows: install cli tool or use GUI to compress.
- Upload zip file to the JRS instance. 
([Read Here](https://community.jaspersoft.com/documentation/tibco-jasperreports-server-administrator-guide/v780/creating-themes#Downloading_and_Uploading_Theme_ZIP_Files) 
to learn how to do this)  
  
#### Deploy theme to the filesystem for development
  - edit `applicationContext-themes.xml` in `JRS` application deployed to application server
  - uncomment two spring beans:
  ```xml
    <bean id="themeResolver" class="org.springframework.web.servlet.theme.FixedThemeResolver">...</bean>
    <bean id="themeSource" class="org.springframework.ui.context.support.ResourceBundleThemeSource">...</bean>
  ```
  - comment out three spring beans:
  ```xml
    <bean id="themeResolver" class="com.jaspersoft.jasperserver.war.themes.JSThemeResolver"  lazy-init="true">...</bean>
    <bean id="jsThemeResolver" class="com.jaspersoft.jasperserver.war.themes.MTThemeResolver"  lazy-init="true">...</bean>
    <bean id="themeSource" class="com.jaspersoft.jasperserver.war.themes.RepositoryFolderThemeSourceImpl"  lazy-init="true">...</bean>
  ```
  - now you can deploy themes together with javascript and i18n bundles by running `yarn deploy` (read main part of [Deploy](#deploy))
  - save file and restart server.

### Deploy localization bundles
Prepare bundles for deployment:
```shell
yarn run overlay
```
this will copy all bundles to `build/overlay/WEB-INF/bundles` folder

> Note: themes also will be prepared with this command

- deploy bundles by running `yarn deploy` (read main part of [Deploy](#deploy))
- restart server

## Rapid development environment
**Jasper Server OS WEB UI** uses [Webpack](https://webpack.js.org/) as a build system,
so it's possible to use different webpack capabilities to speed up development of WEB UI.

### Webpack Dev Server
Webpack provides [dev server](https://webpack.js.org/configuration/dev-server/) to speedup development.
- Dev Server allows us to develop WEB UI without necessity to set up local instance of Jasper Server OS
- Dev Server allows us to have live reload: once source code (javascript) has been changed dev server
automatically re-builds source code and reloads browser page 
so changes automatically become visible

#### Start Dev Server
You should have access to running JRS instance.
Let's say running JRS instance could be accessed here: [https://somehost.com/jasperserver](https://somehost.com/jasperserver)

Set the following parameters (see [Parametrization](#parametrization)):
```properties
ROOT_PATH=jasperserver
HOST=https://somehost.com
```

> If your JRS instance is accessible here (for Community edition): [http://localhost:8080/jasperserver](http://localhost:8080/jasperserver) 
> you do not need to configure these props.

To Start Webpack Dev Server run:
```shell script
yarn run start
```

After you will see the following message in the console:
```shell script
Compiled successfully.
```

you can open browser using this url 

[http://localhost:9000/jasperserver](http://localhost:9000/jasperserver)
 
(where `jasperserver` part of the URL is the value of `ROOT_PATH` parameter).

You should see **Jasper Server OS** login page.

> Note that during development using Webpack Dev Server some JRS functionality might not work as expected.
> In this case to check if everything is OK - use production [build](#build) and [deploy](#deploy) 
> bundle to the real JRS instance and use real JRS url to test (in our sample: [https://somehost.com/jasperserver](https://somehost.com/jasperserver))

#### Develop default theme

If you develop with the help of Webpack Dev Server - `default` theme from the sources file system will be used.
(see [Build](#build) chapter to find out the path to the themes used by Dev Server)
Even if other theme selected in the JRS.
It's possible to change current theme when you develop with Webpack Dev Server. 
In order to do this - set `DEV_SERVER_THEME_NAME` property
to one of the existing themes (see [Parametrization](#parametrization)).

> Theme source files placed here: `<faf-module>/themes` where `faf-module` is `js-sdk`, `jrs-ui`, etc.
> When Webpack Dev Server started - all theme data from all faf modules copied to the `./build/overlay/themes` folder (see [Build](#build)).
> And all requests to css or images redirected to that folder.

It's possible to develop theme and have live reload capability.
In order to do this run Webpack Dev Server using this command:
```shell script
yarn run start:themes
```
In this special *themes* mode Webpack Dev Server will reload page once
any theme source file will be changed.

It is possible to pass theme request to the JRS backend instead of using local theme data 
from `build/overlay/themes` folder.
Just set the following property before starting Dev Server (see [Parametrization](#parametrization)): 
```properties
DEV_SERVER_THEMES=false
```

#### Changes in localization bundles
Unfortunately currently it's not possible to listen for changes in i18n bundles during starting webpack server.
So these changes will not be automatically picked up. To see changes in bundles during development with webpack - 
you have to deploy them to the application server, see [Deploy localization bundles](#deploy-localization-bundles) for details:
- set `JRSInstancePath` parameter
- `yarn overlay`
- `yarn deploy`
- restart server