@ECHO OFF

rem ///
rem /// Jasper Server OS Pro script that performs migrate by creating new database (standard strategy).
rem ///
rem /// Usage: js-migrate-to-split-newdb.bat {option:(<EMPTY>|help|test)} {option(for standard upgrade only):import file(<path-to-file-and-filename>)}
rem ///        If <path-to-file-and-filename> contains spaces then need to enclose <path-to-file-and-filename> to double quotes.
rem ///

CALL "bin/do-js-migrate.bat" pro standard %*
