@ECHO OFF

rem ///
rem /// Jasper Server OS common upgrade script.
rem ///
rem /// Usage: do-js-upgrade.bat {edition:(ce|pro)} {strategy:(standard|inDatabase)} {option:(<EMPTY>|with-samples|regen-config|test)} {option(for standard upgrade only):import file(<path-to-file-and-filename>)}
rem ///        If <path-to-file-and-filename> contains spaces then need to enclose <path-to-file-and-filename> to double quotes.
rem ///

rem -----------------------------------------------------------------------------

rem
rem Determining argument count.
rem
SET ARGUMENT_COUNT=0
FOR %%X IN (%*) DO SET /A ARGUMENT_COUNT+=1

rem
rem Validating and setting edition.
rem
SET JS_EDITION=%1
IF NOT "%JS_EDITION%"=="ce" IF NOT "%JS_EDITION%"=="pro" (
  CALL :fail "Jasper Server OS edition expected as input"
  EXIT /b 1
)

rem
rem Validating and setting upgrade strategy.
rem
SET JS_UPGRADE_STRATEGY=%2
IF NOT "%JS_UPGRADE_STRATEGY%"=="standard" IF NOT "%JS_UPGRADE_STRATEGY%"=="inDatabase" (
  CALL :fail "Jasper Server OS upgrade strategy expected as input"
  EXIT /b 1
)

rem
rem Validating argument count.
rem
IF "%JS_UPGRADE_STRATEGY%"=="standard" (
  IF %ARGUMENT_COUNT% GTR 5 (
    CALL :fail "Too many arguments"
    EXIT /b 1
  )
  IF %ARGUMENT_COUNT% LSS 3 (
    CALL :fail "Invalid argument count"
    EXIT /b 1
  )
)
IF "%JS_UPGRADE_STRATEGY%"=="inDatabase" IF %ARGUMENT_COUNT% GTR 3 (
  CALL :fail "Too many arguments"
  EXIT /b 1
)

SET JS_OPTION=%~3
SET IMPORT_FILE=%~4
SET IS_INCLUDE_ACCESS_EVENTS=FALSE
SET IS_INCLUDE_SERVER_SETTINGS=FALSE
IF ""%JS_UPGRADE_STRATEGY%""==""standard"" (
  SET IS_INCLUDE_SERVER_SETTINGS=TRUE
  IF ""%3""==""include-access-events"" (
    SET IS_INCLUDE_ACCESS_EVENTS=TRUE
    SET JS_OPTION=%~4
    SET IMPORT_FILE=%~5
  )
  IF ""%4""==""include-access-events"" (
    SET IS_INCLUDE_ACCESS_EVENTS=TRUE
    SET JS_OPTION=%~3
    SET IMPORT_FILE=%~5
  )
  IF ""%5""==""include-access-events"" (
    SET IS_INCLUDE_ACCESS_EVENTS=TRUE
    SET JS_OPTION=%~3
    SET IMPORT_FILE=%~4
  )
)

SET IS_INVALID_JS_OPTION_VALUE=FALSE
IF NOT ""%JS_OPTION%""==""with-samples"" IF NOT ""%JS_OPTION%""==""regen-config"" IF NOT ""%JS_OPTION%""==""test"" IF NOT ""%JS_OPTION%""==""help"" (
  SET IS_INVALID_JS_OPTION_VALUE=TRUE
)

SET IS_INVALID_PARAM_VALUE=FALSE
IF NOT ""%IMPORT_FILE%""==""with-samples"" IF NOT ""%IMPORT_FILE%""==""regen-config"" IF NOT ""%IMPORT_FILE%""==""test"" IF NOT ""%IMPORT_FILE%""==""help"" (
  SET IS_INVALID_PARAM_VALUE=TRUE
)

SET IMPORT_FILE2=%IMPORT_FILE%
IF "%JS_UPGRADE_STRATEGY%"=="standard" (
  IF ""%IMPORT_FILE%""=="""" (
    IF "%IS_INVALID_JS_OPTION_VALUE%"=="TRUE" (
      SET IMPORT_FILE=%JS_OPTION%
      SET JS_OPTION=
    ) ELSE (
      IF NOT ""%JS_OPTION%""==""regen-config"" IF NOT ""%JS_OPTION%""==""test"" IF NOT ""%JS_OPTION%""==""help"" (
        CALL :fail "Jasper Server OS import file[path-to-file-and-filename] expected as input"
        EXIT /b 1
      )
    )
  ) ELSE (
    IF "%IS_INVALID_JS_OPTION_VALUE%"=="TRUE" (
      IF "%IS_INVALID_PARAM_VALUE%"=="TRUE" (
        CALL :fail "Invalid option specified"
        EXIT /b 1
      ) ELSE (
        SET IMPORT_FILE=%JS_OPTION%
        SET JS_OPTION=%IMPORT_FILE2%
      )
    )
  )
)

IF ""%JS_UPGRADE_STRATEGY%""==""standard"" IF NOT ""%JS_OPTION%""==""regen-config"" IF NOT ""%JS_OPTION%""==""test"" IF NOT ""%JS_OPTION%""==""help"" IF "%IMPORT_FILE%"=="" (
  CALL :fail "Jasper Server OS import file[path-to-file-and-filename] expected as input"
  EXIT /b 1
)

IF ""%JS_OPTION%""=="""" ( GOTO :default )
IF ""%JS_OPTION%""==""with-samples"" ( GOTO :withSamples )
IF ""%JS_OPTION%""==""regen-config"" ( GOTO :regenConfig )
IF ""%JS_OPTION%""==""test"" ( GOTO :test )
IF ""%JS_OPTION%""==""help"" ( GOTO :help )
CALL :fail "Invalid option specified"
EXIT /b 1

:default
SET JS_ANT_TARGET=upgrade-minimal-%JS_EDITION%
SET JS_OPTION=default
GOTO :proceedToSetup

:withSamples
SET JS_ANT_TARGET=upgrade-normal-%JS_EDITION%
GOTO :proceedToSetup

:regenConfig
SET JS_ANT_TARGET=refresh-config
GOTO :proceedToSetup

:test
SET JS_ANT_TARGET=pre-upgrade-test-%JS_EDITION%
GOTO :proceedToSetup

:help
IF "%JS_UPGRADE_STRATEGY%"=="standard" ( TYPE bin\upgrade-newdb.help )
IF "%JS_UPGRADE_STRATEGY%"=="inDatabase" ( TYPE bin\upgrade-samedb.help )
GOTO :end

IF "%BUILDOMATIC_MODE%"=="" set BUILDOMATIC_MODE=interactive

rem
rem Calling core setup script with determined parameters.
rem
:proceedToSetup
SETLOCAL ENABLEDELAYEDEXPANSION
SET _JS_ANT_OPTIONS=
IF "%IMPORT_FILE%"=="" (
  SET _JS_ANT_OPTIONS=!JS_UPGRADE_STRATEGY!
) ELSE (
  SET _JS_ANT_OPTIONS=!JS_UPGRADE_STRATEGY! "!IMPORT_FILE!"
)
IF "%JS_UPGRADE_STRATEGY%"=="standard" (
  IF "%IS_INCLUDE_ACCESS_EVENTS%"=="TRUE" (
    SET _JS_ANT_OPTIONS=!_JS_ANT_OPTIONS! include-access-events
  )
  IF "%IS_INCLUDE_SERVER_SETTINGS%"=="TRUE" (
    SET _JS_ANT_OPTIONS=!_JS_ANT_OPTIONS! include-server-settings
  )
)
ENDLOCAL & SET JS_ANT_OPTIONS=%_JS_ANT_OPTIONS%
CALL "%~dp0"do-js-setup.bat upgrade %JS_EDITION% %JS_OPTION% %JS_ANT_TARGET% %JS_ANT_OPTIONS%
GOTO :end

rem -----------------------------------------------------------------------------

:showUsage
ECHO Please enter one of the following options: 'EMPTY', with-samples, regen-config, test
IF "%JS_UPGRADE_STRATEGY%"=="standard" (
  ECHO "Also enter import file option(<path-to-file-and-filename>). If <path-to-file-and-filename> contains spaces then need to enclose <path-to-file-and-filename> to double quotes."
)
GOTO:EOF

:fail
IF NOT "%~1" == "" ( ECHO %~1 )
CALL :showUsage
EXIT /b 1

:end
EXIT /b 0
