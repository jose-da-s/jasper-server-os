#!/bin/bash

#
# Jasper Server OS common installation and upgrade script.
#
# Usage: do-js-setup.sh (install|upgrade){setup mode} (ce|pro){edition} {script option} {Ant target} [Ant options]
#

# -----------------------------------------------------------------------------
handleExtraJarsForJdk17() {
  # Directory paths are relative to JRS_HOME\buildomatic.
  javaVersion=$(java -version 2>&1 | head -1 | cut -d '"' -f 2 | cut -d '.' -f1)
  if [ ${javaVersion} -ge 17 ]; then
    echo "Copying additional jar file(s) that are needed very specific to jdk 17+ runtime."
    cp ./install_resources/extra-jars-jdk17/nashorn*.jar ./lib
    cp ./install_resources/extra-jars-jdk17/asm*.jar ./lib
  else
    echo "Deleting any existing jar file(s) that are needed very specific to jdk 17+ runtime."
    rm ./lib/asm*.jar
    rm ./lib/nashorn*.jar
  fi
}

fail() {
  if [[ -n $1 ]]; then
    echo $1
  fi
  showUsage
  exit 1;
}

showUsage() {
  echo "Usage: ./do-js-setup.sh (install|upgrade){setup mode} (ce|pro){edition} {script option} {Ant target} [Ant options]"
}

#
# Selects bundled or existing Ant.
#
initializeAntEnvironment() {
  handleExtraJarsForJdk17
  if test -d ../apache-ant
  then
    ANT_HOME=../apache-ant
    export ANT_HOME

    ANT_RUN=$ANT_HOME/bin/ant
    export ANT_RUN

    PATH=$ANT_HOME/bin:$PATH
    export PATH
  else
    log "Bundled Ant not found. Using system Ant."

    ANT_RUN=ant
  fi
}

#
# Runs Ant.
#
runAnt() {
  targetName=$1
  shift
  options=$@

  if [ "$targetName" != "" ]; then
    log "Running $targetName Ant task."
    log

    $ANT_RUN --noconfig -nouserlib -lib . -lib lib -f build.xml -logfile $JS_LOG_FILE $targetName $options 2>&1
    antReturnCode=$?

    antReturnCodeMessage="Checking Ant return code:"
    if [ $antReturnCode -ne 0 ]; then
      log "$antReturnCodeMessage BAD ($antReturnCode)"
      exit 1;
    else
      log "$antReturnCodeMessage OK"
    fi

  else
    log "Name of Ant target not specified."
    exit 1;
  fi
}

#
# Console + file logging subroutine.
#
log() {
  if [ "$1" == "" ]; then
    JS_OUT_STR="----------------------------------------------------------------------"
  else
    JS_OUT_STR=$1
  fi
  echo $JS_OUT_STR | tee -a $JS_LOG_FILE
}

# -----------------------------------------------------------------------------

#
# Validating arguments and setting internal variables.
#

if [[ $# -lt 4 || $# -gt 10 ]]; then
  fail "Invalid argument count"
fi

JS_SETUP_MODE=$1
if [[ "$JS_SETUP_MODE" != "install" && "$JS_SETUP_MODE" != "upgrade" ]]; then
  fail "Setup mode (install|upgrade) expected as input"
fi

JS_EDITION=$2
if [[ "$JS_EDITION" != "ce" && "$JS_EDITION" != "pro" ]]; then
  fail "Jasper Server OS edition (ce|pro) expected as input"
fi

JS_OPTION=$3
JS_ANT_TARGET=$4
JS_ANT_OPTIONS="$5 $6 $7 $8 $9 ${10}  -Djs.setup.mode=$JS_SETUP_MODE -Djava.net.preferIPv4Stack=true -Djs.option=$JS_OPTION"

#
# Initializing time variable.
#
JS_CURRENT_TIME=`date +%Y-%m-%d_%H-%M`

#
# Defining log file name, creating log directory if it doesn't exist.
#
JS_LOG_FILE_SUFFIX=$(($RANDOM + 10000))
JS_LOG_FILE=logs/js-$JS_SETUP_MODE-$JS_EDITION\_$JS_CURRENT_TIME-$JS_LOG_FILE_SUFFIX.log
if [ ! -d logs ]; then
  mkdir -p logs
fi
echo "Writing to log file: $JS_LOG_FILE"

#
# Printing entry information.
#
log
log "Running Jasper Server OS $JS_SETUP_MODE script at $JS_CURRENT_TIME"
log

export ANT_OPTS="$ANT_OPTS -Dnet.sf.ehcache.disabled=true -Xms512m -Xmx2048m -noverify"

log
log "Using ANT_OPTS: $ANT_OPTS"
log

#
# Checking JAVA_HOME.
#
if [ "$JAVA_HOME" == "" ]; then
  log "WARNING: JAVA_HOME environment variable not found"
fi

#
# Running Ant.
#
log "[$JS_OPTION]"
initializeAntEnvironment
runAnt $JS_ANT_TARGET $JS_ANT_OPTIONS
log
