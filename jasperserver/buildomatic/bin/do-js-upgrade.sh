#!/bin/bash

#
# JasperReports Server common upgrade script.
#
# Usage: do-js-upgrade.sh {edition:(ce|pro)} {strategy:(standard|inDatabase)} {option:(<EMPTY>|with-samples|regen-config|test)} {option(for standard upgrade only):import file(-DimportFile=<path-to-file-and-filename>)}
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
  echo "Please enter one of the following options: <EMPTY>, with-samples, regen-config, test"
}

# -----------------------------------------------------------------------------

#
# Validating and setting edition.
#
if [[ $# -lt 1 || "$1" != "ce" && "$1" != "pro" ]]; then
  fail "JasperReports Server edition (ce|pro) expected as input"
fi
JS_EDITION=$1

#
# Validating and setting upgrade strategy.
#
if [[ $# -lt 2 || "$2" != "standard" && "$2" != "inDatabase" ]]; then
  fail "JasperReports Server upgrade strategy (standard|inDatabase) expected as input"
fi
JS_UPGRADE_STRATEGY=$2

#
# Choosing setup parameters depending on the given arguments.
#
if [[ "$JS_UPGRADE_STRATEGY" == "standard" && $# -gt 5  || "$JS_UPGRADE_STRATEGY" == "inDatabase" && $# -gt 3 ]]; then
  fail "Too many arguments!"
else
  JS_OPTION=$3
  IMPORT_FILE=$4
  IS_INCLUDE_SERVER_SETTINGS=false
  if [[ "$JS_UPGRADE_STRATEGY" == "standard" ]]; then
    IS_INCLUDE_ACCESS_EVENTS=false
    IS_INCLUDE_SERVER_SETTINGS=true
    if [[ "$3" == "include-access-events" ]]; then
      IS_INCLUDE_ACCESS_EVENTS=true
      JS_OPTION=$4
      IMPORT_FILE=$5
    elif [[ "$4" == "include-access-events" ]]; then
      IS_INCLUDE_ACCESS_EVENTS=true
      JS_OPTION=$3
      IMPORT_FILE=$5
    elif [[ "$5" == "include-access-events" ]]; then
      IS_INCLUDE_ACCESS_EVENTS=true
      JS_OPTION=$3
      IMPORT_FILE=$4
    fi
  fi

  IS_INVALID_JS_OPTION_VALUE=false
  if [[ "$JS_OPTION" != "with-samples" && "$JS_OPTION" != "regen-config" && "$JS_OPTION" != "test" && "$JS_OPTION" != "help" ]]; then
    IS_INVALID_JS_OPTION_VALUE=true
  fi

  IS_INVALID_PARAM_VALUE=false
  if [[ "$IMPORT_FILE" != "with-samples" && "$IMPORT_FILE" != "regen-config" && "$IMPORT_FILE" != "test" && "$IMPORT_FILE" != "help" ]]; then
    IS_INVALID_PARAM_VALUE=true
  fi

  if [[ "$JS_UPGRADE_STRATEGY" == "standard" ]]; then
    if [[ "$IMPORT_FILE" == "" ]]; then
      if [[ "$IS_INVALID_JS_OPTION_VALUE" == "true" ]]; then
        IMPORT_FILE=$JS_OPTION
        JS_OPTION=""
      elif [[ "$JS_OPTION" != "regen-config" && "$JS_OPTION" != "test" && "$JS_OPTION" != "help" ]]; then
        fail "JasperReports Server import file(<path-to-file-and-filename>) expected as input"
      fi
    else
      if [[ "$IS_INVALID_JS_OPTION_VALUE" == "true" ]]; then
        if [[ "$IS_INVALID_PARAM_VALUE" == "true" ]]; then
          fail "Invalid option specified"
        else
          TEMP_IMPORT_FILE=$IMPORT_FILE
          IMPORT_FILE=$JS_OPTION
          JS_OPTION=$TEMP_IMPORT_FILE
        fi
      fi
    fi
  fi


  if [[ "$JS_UPGRADE_STRATEGY" == "standard" && "$JS_OPTION" != "regen-config" && "$JS_OPTION" != "test" && "$JS_OPTION" != "help" && "$IMPORT_FILE" == "" ]]; then
    fail "JasperReports Server import file(<path-to-file-and-filename>) expected as input"
  fi

  if [ "$JS_OPTION" == "" ]; then
    JS_ANT_TARGET=upgrade-minimal-$JS_EDITION
    JS_OPTION=default
  else
    case "$JS_OPTION" in
      with-samples)
        JS_ANT_TARGET=upgrade-normal-$JS_EDITION
      ;;

      regen-config)
        JS_ANT_TARGET=refresh-config
      ;;

      test)
        JS_ANT_TARGET=pre-upgrade-test-$JS_EDITION
      ;;

      help)
        if [[ "$JS_UPGRADE_STRATEGY" == "standard" ]]; then
          cat ./bin/upgrade-newdb.help
        elif [[ "$JS_UPGRADE_STRATEGY" == "inDatabase" ]]; then
          cat ./bin/upgrade-samedb.help
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
if [[ "$JS_UPGRADE_STRATEGY" == "standard" && "$IS_INCLUDE_ACCESS_EVENTS" == "true" ]]; then
  JS_IMPORT_ARGS="--include-access-events"
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