@ECHO OFF

rem ///
rem /// Jasper Server OS CE script that performs upgrade by updating existing database.
rem ///
rem /// Usage: js-upgrade-samedb-ce.bat {option:(<EMPTY>|with-samples|regen-config|test)}
rem ///

CALL "bin/do-js-upgrade.bat" ce inDatabase %*
