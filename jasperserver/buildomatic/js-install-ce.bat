@ECHO OFF

rem ///
rem /// Jasper Server OS CE installation script.
rem ///
rem /// Usage: js-install-ce.bat {option:(<EMPTY>|minimal|drop-db|regen-config|test)}
rem ///

CALL "bin/do-js-install.bat" ce %*
