#!/bin/bash

#
# Jasper Server OS Pro Docker installation script.
#
# Usage: js-install.sh {option:(<EMPTY>|minimal|drop-db|regen-config|test)}
#

yes | ./bin/do-js-install.sh docker $*
