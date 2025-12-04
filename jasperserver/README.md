# Jasper Server OS Community Edition Build Guide
## Build prerequisites
- Install [Java 1.8](https://www.oracle.com/java/technologies/downloads) or higher. Open JDK 1.8 or higher is supported too.
- Install [Apache Ant](https://ant.apache.org). Recommended version 1.10.2 or later.
- Install [Ant Contrib](http://ant-contrib.sourceforge.net/#install) library (version 1.0b3).
- Install [Maven](http://maven.apache.org/download.html#installation) build tool.
- Install [Tomcat](https://tomcat.apache.org/), [JBoss EAP](https://developers.redhat.com/products/eap/download) or [Wildfly](https://www.wildfly.org/downloads) application server.
- Install [PostgreSQL](https://www.postgresql.org/) or [MySQL](https://www.mysql.com/) database (Community Edition versions are enough)

## Set up
### Build UI artifacts
First you need to build UI and Deploy (described in the section below) it to the local maven repository. 

#### Set up UI build environment
To start you should set up UI build environment as described in [../jasperserver-ui/ce/README.md#set-up](../jasperserver-ui/ce/README.md#set-up) including `yarn install` command.

#### Build UI artifacts
To build UI artifacts run the following commands:

in [../jasperserver-ui/ce](../jasperserver-ui/ce) folder:
```shell script
yarn run clean
yarn run build
```
in [../jasperserver-ui/ce/jrs-ui](../jasperserver-ui/ce/jrs-ui) folder:
```shell script
yarn run overlay
```

#### Deploy UI artifacts to the local maven repository 
Backend build process expects frontend distribution to be deployed to the maven repository.

So before starting backend build we can publish our frontend distribution to the local maven repository,
so backend build will pick it up.

In order to do this 
- set `BUILD_ARTIFACT_VERSION_NAME=master` env variable. It should be configured in [../jasperserver-ui/ce/jrs-ui/.env.local](../jasperserver-ui/ce/jrs-ui/.env.local) file (you need to create this file) to override default parameters defined in [../jasperserver-ui/ce/jrs-ui/.env](../jasperserver-ui/ce/jrs-ui/.env) file. For more information see [Parametrization](../jasperserver-ui/ce/jrs-ui/README.md#parametrization). 
> You can set this variable to any other value, but you will need additionally to configure the server to use it instead.
> Otherwise it will not be able to find UI artifacts   
- navigate to your jrs-ui folder [../jasperserver-ui/ce/jrs-ui](../jasperserver-ui/ce/jrs-ui)
- run
    ```shell script
    yarn run mvnPublish
    ```
This command will publish UI artifacts to `<path-to-maven-repo>/.m2/repository/com/jaspersoft/jrs-ui/master` maven repo path.
By default, artifacts have the same name as a git branch, in our case it is **master**.

### Configuring the buildomatic properties
- In order to build the project you need to provide a configuration file with all required settings.
- Navigate to `<path-to-project>/jasperreports-server-ce/jasperserver/buildomatic/sample_conf/source` directory.
- Copy `postgresql_master.properties` or `mysql_master.properties` to the `buildomatic` root folder and
rename a file to `default_master.propeties`.
- Open `default_master.properties` and configure next required properties:
  - `js-path` - path to downloaded JasperReports CE Server;
  - `appServerType` - type of app server to use, **tomcat** is the default value;
  - `appServerDir` - path to an app server;
  - `maven` - path to a maven executable file;
  - `dbType`, `dbHost`, `dbUsername`, `dbPassword` - database type, host, username and password related properties; 
  - `maven.build.type` - set it to `community` value. Build uses this property to check what maven settings should be used.
  - `fafBuildArtifactVersionName` - set it to `master`. This property checks what UI artifact to use. If you have specified a different value for the `BUILD_ARTIFACT_VERSION_NAME` env variable - you should use it.
  - `buildArtifactVersionName` - set it to `master`. The value of this property should be the same as the value for `fafBuildArtifactVersionName`.
  
Configuration example:
```properties
js-path=/opt/jasperreports-server-ce/jasperserver
appServerType=tomcat
appServerDir=/opt/tomcat
maven=/opt/maven/bin/mvn
maven.build.type=community
dbType=postgresql
dbHost=localhost
dbUsername=postgres
dbPassword=password
fafBuildArtifactVersionName=master
buildArtifactVersionName=master
```

### Build server
Run following commands in `buildomatic` folder: 

Linux/MacOS:
```shell script
./js-ant build-all-ce
./js-ant run-production-data-ce
./js-ant prepare-foodmart-db
./js-ant prepare-sugarcrm-db
./js-ant deploy-webapp-ce
```
Windows: 
```shell script
js-ant.bat build-all-ce
js-ant.bat run-production-data-ce
js-ant.bat prepare-foodmart-db
js-ant.bat prepare-sugarcrm-db
js-ant.bat deploy-webapp-ce
```

>After successful build the Jasper Server OS will be deployed as a **jasperserver** WAR file to the specified Application Server (_appServerDir_ location). In our case it will be _/opt/tomcat/webapps/jasperserver_

### Starting instance
To start Jasper Server OS:
- For **Tomcat** execute `<path-to-tomcat>/bin/startup.sh` (or `startup.bat` if you are on Windows)
- For **JBoss** execute `<path-to-jboss>/bin/standalone.sh` (or `standalone.bat` for Windows)
- Open Browser and navigate to `http://localhost:8080/jasperserver`
- Log into Jasper Server OS as jasperadmin:
    - User ID: jasperadmin
    - Password: jasperadmin

### Other build commands
There is no need to execute full-build commands all the time. The Buildomatic contains many simplified ant tasks, most common of them are:
- `build-ce` - to build the server, this also executes all unit tests; 
- `run-integration-tests-ce` - to run integration tests. To debug them add `-Dremote.debug=true` to the command line and debugger attach to port 8000;
- `deploy-webapp-ce` - copies war file to the application server directory (specified by the `appServerDir` property);
- `refresh-config` - cleans and re-creates configuration files from scratch. Useful, when you made changes to the `default_master.properties`;
- `help` - displays help page. It contains information about other ant tasks that hasn't been listed here.   
> Please make sure you are using **./js-ant** (or **./js-ant.bat** for Windows) executable to run all these tasks, otherwise there might be an error message   
