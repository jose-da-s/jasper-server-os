#!/bin/bash

#
# JasperReports Server CE script that performs upgrade by creating new database (standard strategy).
#
# Usage: js-upgrade-newdb-ce.sh {option:(<EMPTY>|with-samples|regen-config|test)} {option(for standard upgrade only):import file(-DimportFile=<path-to-file-and-filename>)}
#

./bin/do-js-upgrade.sh ce standard $*
