# Jasper Server OS Build Guide
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
To build UI artifacts, see `/jasperserver-ui/ce/README.md`.

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
./js-ant build-ce
./js-ant build-init-js-db-ce
./js-ant run-production-data-ce
./js-ant prepare-foodmart-db
./js-ant prepare-sugarcrm-db
./js-ant deploy-webapp-ce
```
Windows: 
```shell script
js-ant.bat build-ce
js-ant.bat build-init-js-db-ce
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