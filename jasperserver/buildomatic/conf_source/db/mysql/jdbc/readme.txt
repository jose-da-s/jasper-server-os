buildomatic/conf_source/db/mysql/jdbc/readme.txt
------------------------------------------------

2014-04-18
----------

If you would like to use the JDBC driver that is maintained and distributed
by the MySQL project, you can download the most current driver from this 
location: 

  https://dev.mysql.com/downloads/connector/j/

A downloaded MySQL driver typically has a name like the following: 

   mysql-connector-java-8.0.23.jar

To set your buildomatic properties so that it uses the driver above instead
of the JDBC driver maintained by the MariaDB project, you can do the following:

  Copy the MySQL driver to the following location: 

    buildomatic/conf_source/db/mysql/jdbc

  Next, edit your default_master.properties file. Comment out and edit to
  set the following: 

    jdbcDriverClass=com.mysql.cj.jdbc.Driver
    maven.jdbc.groupId=mysql
    maven.jdbc.artifactId=mysql-connector-java
    maven.jdbc.version=8.0.23

2013-05-23
----------

Updated buildomatic default configurations to specify the MariaDB 
JDBC driver. So, now, the MariaDB JDBC driver will be used by default
when installing Jasper Server OS to a MySQL database. The MariaDB
JDBC driver is compatible with the MySQL database.


2013-03-29
----------

Adding the MariaDB JDBC client driver. This should be completely 
compatible with MySQL (MariaDB is a fork of the MySQL src base). 


2011-07-27
----------

MySQL JDBC drivers can be downloaded from the following location:

  http://dev.mysql.com/downloads/connector/j/

A downloaded driver should be placed in this folder in order to 
run with the buildomatic scripts. 

