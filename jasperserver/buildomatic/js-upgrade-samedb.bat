@ECHO OFF

rem ///
rem /// Jasper Server OS Pro script that performs upgrade by updating existing database.
rem ///
rem /// Usage: js-upgrade-samedb.bat {option:(<EMPTY>|with-samples|regen-config|test)}
rem ///

CALL "bin/do-js-upgrade.bat" pro inDatabase %*
