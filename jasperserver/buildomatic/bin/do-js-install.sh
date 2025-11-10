#!/bin/bash

#
# JasperReports Server common installation script.
#
# Usage: do-js-install.sh {edition:(ce|pro)} {option:(<EMPTY>|minimal|drop-db|regen-config|test)}
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
  echo "Please enter one of the following options: <EMPTY>, minimal, regen-config, drop-db, test"
}

# -----------------------------------------------------------------------------

#
# Validating and setting edition.
#
if [[ $# -lt 1 || "$1" != "ce" && "$1" != "pro" && "$1" != "docker" ]]; then
  fail "JasperReports Server edition (ce|pro|docker) expected as input"
fi
JS_EDITION=$1

#
# Choosing setup parameters depending on the given arguments.
#
if [ $# -gt 2 ]; then
  fail "Too many arguments!"
else
  JS_OPTION=$2

  if [ "$JS_OPTION" == "" ]; then
    JS_ANT_TARGET=install-normal-$JS_EDITION
    JS_OPTION=default

  elif [ "$1" == "docker" ]; then
    JS_EDITION=pro

    case "$JS_OPTION" in
      minimal)
        JS_ANT_TARGET=install-docker-$JS_EDITION
      ;;

      db)
        JS_ANT_TARGET=install-docker-db
      ;;

      regen-config)
        JS_ANT_TARGET=refresh-config
      ;;

      drop-db)
        JS_ANT_TARGET=drop-js-db
      ;;

      test)
        JS_ANT_TARGET=pre-install-test-docker
      ;;

      help)
        cat ./bin/install.help
        exit 0
      ;;

      *)
        fail "Invalid option specified"
      ;;
    esac

  else
    case "$JS_OPTION" in
      minimal)
        JS_ANT_TARGET=install-minimal-$JS_EDITION
      ;;

      regen-config)
        JS_ANT_TARGET=refresh-config
      ;;

      drop-db)
        JS_ANT_TARGET=drop-js-db
      ;;

      test)
        JS_ANT_TARGET=pre-install-test-$JS_EDITION
      ;;

      help)
        cat ./bin/install.help
        exit 0
      ;;

      *)
        fail "Invalid option specified"
      ;;
    esac
  fi
fi

#
# Calling core setup script with determined parameters.
#
./bin/do-js-setup.sh install $JS_EDITION $JS_OPTION $JS_ANT_TARGET
