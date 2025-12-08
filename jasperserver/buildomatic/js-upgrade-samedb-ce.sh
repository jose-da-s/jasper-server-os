#!/bin/bash

#
# Jasper Server OS CE script that performs upgrade by updating existing database.
#
# Usage: js-upgrade-samedb-ce.sh {option:(<EMPTY>|with-samples|regen-config|test)}
#

./bin/do-js-upgrade.sh ce inDatabase $*
