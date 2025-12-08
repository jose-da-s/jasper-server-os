@ECHO OFF

rem ///
rem /// Jasper Server OS Pro script that performs upgrade by creating new database (standard strategy).
rem ///
rem /// Usage: js-upgrade-newdb.bat {option:(<EMPTY>|with-samples|regen-config|test)} {option(for standard upgrade only):import file(<path-to-file-and-filename>)}
rem ///        If <path-to-file-and-filename> contains spaces then need to enclose <path-to-file-and-filename> to double quotes.
rem ///

CALL "bin/do-js-upgrade.bat" pro standard %*
