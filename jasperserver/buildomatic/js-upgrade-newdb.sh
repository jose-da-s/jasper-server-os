#!/bin/bash

#
# Jasper Server OS Pro script that performs upgrade by creating new database (standard strategy).
#
# Usage: js-upgrade-newdb.sh {option:(<EMPTY>|with-samples|regen-config|test)} {option(for standard upgrade only):import file(-DimportFile=<path-to-file-and-filename>)}
#

./bin/do-js-upgrade.sh pro standard $*
