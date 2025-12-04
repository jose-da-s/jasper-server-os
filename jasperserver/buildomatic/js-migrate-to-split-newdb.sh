#!/bin/bash

#
# JasperReports Server Pro script that performs migrate by creating new database (standard strategy).
#
# Usage: js-migrate-to-split-newdb.sh {option:(<EMPTY>|help|test)} {option(for standard upgrade only):import file(-DimportFile=<path-to-file-and-filename>)}
#

./bin/do-js-migrate.sh pro standard $*
