#!/bin/bash

#
# JasperReports Server Pro script that performs upgrade by updating existing database.
#
# Usage: js-upgrade-samedb.sh {option:(<EMPTY>|with-samples|regen-config|test)}
#

./bin/do-js-upgrade.sh pro inDatabase $*
