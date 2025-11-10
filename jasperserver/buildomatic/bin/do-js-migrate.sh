#!/bin/bash

#
# JasperReports Server common migrate script.
#
# Usage: do-js-migrate.sh {edition:(ce|pro)} {strategy:(standard|inDatabase)} {option:(<EMPTY>|help|test)} {option(for standard migrate only):import file(-DimportFile=<path-to-file-and-filename>)}
#

# -----------------------------------------------------------------------------

fail() {
  if [[ -n $1 ]]; then
    echo $1
  fi
  showUsage
  exit 1;
}

showUsage() {
  echo "Please enter one of the following options: <EMPTY>, help, test"
}

# -----------------------------------------------------------------------------

#
# Validating and setting edition.
#
if [[ $# -lt 1 || "$1" != "pro" ]]; then
  fail "JasperReports Server edition pro expected as input"
fi
JS_EDITION=$1

#
# Validating and setting upgrade strategy.
#
if [[ $# -lt 2 || "$2" != "standard" && "$2" != "inDatabase" ]]; then
  fail "JasperReports Server migrate strategy (standard|inDatabase) expected as input"
fi
JS_UPGRADE_STRATEGY=$2

#
# Choosing setup parameters depending on the given arguments.
#
if [[ "$JS_UPGRADE_STRATEGY" == "standard" && $# -gt 6  || "$JS_UPGRADE_STRATEGY" == "inDatabase" && $# -gt 3 ]]; then
  fail "Too many arguments!"
else
  JS_OPTION=$3
  IS_INCLUDE_SERVER_SETTINGS=false
  if [[ "$JS_UPGRADE_STRATEGY" == "standard" ]]; then
    IS_INCLUDE_ACCESS_EVENTS=false
    IS_INCLUDE_AUDIT_EVENTS=false
    IS_INCLUDE_MONITORING_EVENTS=false
    IS_INCLUDE_SERVER_SETTINGS=true
    if [[ "$JS_OPTION" != "test" && "$JS_OPTION" != "help" ]]; then
      IMPORT_FILE=$JS_OPTION
      JS_OPTION=""
      if [[ "$4" == "--include-access-events" || "$5" == "--include-access-events" || "$6" == "--include-access-events" ]]; then
        IS_INCLUDE_ACCESS_EVENTS=true
      fi
      if [[ "$4" == "--include-audit-events" || "$5" == "--include-audit-events" || "$6" == "--include-audit-events" ]]; then
        IS_INCLUDE_AUDIT_EVENTS=true
      fi
      if [[ "$4" == "--include-monitoring-events" || "$5" == "--include-monitoring-events" || "$6" == "--include-monitoring-events" ]]; then
        IS_INCLUDE_MONITORING_EVENTS=true
      fi
    fi
  fi
  IS_INVALID_JS_OPTION_VALUE=false
  if [[ "$JS_OPTION" != "test" && "$JS_OPTION" != "help" ]]; then
    IS_INVALID_JS_OPTION_VALUE=true
  fi

  if [[ "$JS_UPGRADE_STRATEGY" == "standard" && "$JS_OPTION" != "test" && "$JS_OPTION" != "help" && "$IMPORT_FILE" == "" ]]; then
    fail "JasperReports Server import file(<path-to-file-and-filename>) expected as input"
  fi


  if [ "$JS_OPTION" == "" ]; then
    JS_ANT_TARGET=migrate-split-$JS_EDITION
    JS_OPTION=default
  else
    case "$JS_OPTION" in
      test)
        JS_ANT_TARGET=pre-upgrade-test-$JS_EDITION
      ;;

      help)
        if [[ "$JS_UPGRADE_STRATEGY" == "standard" ]]; then
          cat ./bin/migrate-to-split-newdb.help
        elif [[ "$JS_UPGRADE_STRATEGY" == "inDatabase" ]]; then
          cat ./bin/migrate-to-split-samedb.help
        fi
        exit 0
      ;;

      *)
        fail "Invalid option specified"
      ;;
    esac
  fi
fi

JS_ANT_OPTIONS="-Djava.net.preferIPv4Stack=true"
if [ "$IMPORT_FILE" == "" ]; then
  JS_ANT_OPTIONS="$JS_ANT_OPTIONS -Dstrategy=$JS_UPGRADE_STRATEGY"
else
  JS_ANT_OPTIONS="$JS_ANT_OPTIONS -Dstrategy=$JS_UPGRADE_STRATEGY -DimportFile=$IMPORT_FILE"
fi
if [[ "$IS_INCLUDE_ACCESS_EVENTS" == "true" ]]; then
  if [ "$JS_IMPORT_ARGS" != "" ]; then
    JS_IMPORT_ARGS="--include-access-events $JS_IMPORT_ARGS"
  else
    JS_IMPORT_ARGS="--include-access-events"
  fi
fi
if [ "$IS_INCLUDE_AUDIT_EVENTS" == "true" ]; then
  if [ "$JS_IMPORT_ARGS" != "" ]; then
      JS_IMPORT_ARGS="--include-audit-events $JS_IMPORT_ARGS"
  else
      JS_IMPORT_ARGS="--include-audit-events"
  fi
fi
if [ "$IS_INCLUDE_MONITORING_EVENTS" == "true" ]; then
  if [ "$JS_IMPORT_ARGS" != "" ]; then
    JS_IMPORT_ARGS="--include-monitoring-events $JS_IMPORT_ARGS"
  else
    JS_IMPORT_ARGS="--include-monitoring-events"
  fi
fi
if [ "$IS_INCLUDE_SERVER_SETTINGS" == "true" ]; then
  if [ "$JS_IMPORT_ARGS" != "" ]; then
    JS_IMPORT_ARGS="--include-server-settings $JS_IMPORT_ARGS"
  else
    JS_IMPORT_ARGS="--include-server-settings"
  fi
fi

export BUILDOMATIC_MODE=${BUILDOMATIC_MODE:-interactive}
#
# Calling core setup script with determined parameters.
#
if [ "$JS_IMPORT_ARGS" != "" ]; then
  ./bin/do-js-setup.sh upgrade $JS_EDITION $JS_OPTION $JS_ANT_TARGET $JS_ANT_OPTIONS "-DimportArgs=\"$JS_IMPORT_ARGS\""
else
  ./bin/do-js-setup.sh upgrade $JS_EDITION $JS_OPTION $JS_ANT_TARGET $JS_ANT_OPTIONS
fi