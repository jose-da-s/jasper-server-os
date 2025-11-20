#!/bin/bash

#
# Jasper Server OS Pro script that performs migrate by updating existing database.
#
# Usage: js-migrate-to-split-samedb.sh {option:(<EMPTY>|help|test)}
#

./bin/do-js-migrate.sh pro inDatabase $*
