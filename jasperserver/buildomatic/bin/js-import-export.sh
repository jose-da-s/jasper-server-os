#!/bin/bash
#
# script to run export-import command
#

# Collect the command line args

CMD_LINE_ARGS=$*


# Set the main config folder to use and collect all
# the jars onto the classpath
#
# If Pro config exists, then set to Pro config
# Otherwise, assume CE config

if test -d $BASEDIR/conf_source/iePro
then
    echo "Executing Pro version"
    export CONFIG_BASE_DIR=$BASEDIR/conf_source/iePro
    export CONFIG_DIR=$CONFIG_BASE_DIR/wrapper
    for i in $CONFIG_DIR/lib/*.jar
    do
        EXP_CLASSPATH="$EXP_CLASSPATH:$i"
    done

else
    echo "Executing CE version"
    export CONFIG_BASE_DIR=$BASEDIR/conf_source/ieCe
    export CONFIG_DIR=$CONFIG_BASE_DIR/wrapper
    for i in $CONFIG_DIR/lib/*.jar
    do
        EXP_CLASSPATH="$EXP_CLASSPATH:$i"
    done
fi


# Additional config folder. This will be used to 
# get js.jdbc.properties from buildomatic setup
export ADDITIONAL_CONFIG_DIR=$BASEDIR/build_conf/default


# Locate the java binary bundled with installer
#
# If "../java/bin/java" exists, use it

JAVA_EXEC=java

if test -f $BASEDIR/../java/bin/java
then
    echo "Using Bundled version of Java" 
    JAVA_HOME=$BASEDIR/../java
    PATH=$JAVA_HOME/bin:$PATH
    JAVA_EXEC=$JAVA_HOME/bin/java
fi

export BUILDOMATIC_MODE=${BUILDOMATIC_MODE:-interactive}

# Add the java memory options to JAVA_OPTS

export JAVA_OPTS="$JAVA_OPTS -Xms128m -Xmx512m -Djava.net.preferIPv4Stack=true -noverify"
#export JAVA_OPTS="$JAVA_OPTS -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005"

# module opens for java 17
export JAVA_OPTS="$JAVA_OPTS \
 --add-opens=java.base/java.io=ALL-UNNAMED \
 --add-opens=java.base/java.lang.ref=ALL-UNNAMED \
 --add-opens=java.base/java.lang=ALL-UNNAMED \
 --add-opens=java.base/java.nio.channels.spi=ALL-UNNAMED \
 --add-opens=java.base/java.nio.channels=ALL-UNNAMED \
 --add-opens=java.base/java.nio=ALL-UNNAMED \
 --add-opens=java.base/java.security=ALL-UNNAMED \
 --add-opens=java.base/java.text=ALL-UNNAMED \
 --add-opens=java.base/java.util.concurrent.atomic=ALL-UNNAMED \
 --add-opens=java.base/java.util.concurrent.locks=ALL-UNNAMED \
 --add-opens=java.base/java.util.concurrent=ALL-UNNAMED \
 --add-opens=java.base/java.util.regex=ALL-UNNAMED \
 --add-opens=java.base/java.util=ALL-UNNAMED \
 --add-opens=java.base/javax.security.auth.login=ALL-UNNAMED \
 --add-opens=java.base/javax.security.auth=ALL-UNNAMED \
 --add-opens=java.base/jdk.internal.access.foreign=ALL-UNNAMED \
 --add-opens=java.base/sun.net.util=ALL-UNNAMED \
 --add-opens=java.base/sun.nio.ch=ALL-UNNAMED \
 --add-opens=java.rmi/sun.rmi.transport=ALL-UNNAMED \
 --add-opens=java.base/sun.util.calendar=ALL-UNNAMED"

# Add the config folders to EXP_CLASSPATH

export EXP_CLASSPATH="$CONFIG_BASE_DIR:$CONFIG_DIR:$CONFIG_DIR/classes:$ADDITIONAL_CONFIG_DIR$EXP_CLASSPATH:."

# run java

$JAVA_EXEC -cp "$EXP_CLASSPATH" $JAVA_OPTS $JS_EXP_CMD_CLASS $JS_CMD_NAME $CMD_LINE_ARGS

