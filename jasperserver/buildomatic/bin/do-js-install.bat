@ECHO OFF

rem ///
rem /// Jasper Server OS common installation script.
rem ///
rem /// Usage: do-js-install.bat {edition:(ce|pro)} {option:(<EMPTY>|minimal|drop-db|regen-config|test)}
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
IF NOT "%JS_EDITION%"=="ce" IF NOT "%JS_EDITION%"=="pro" ( CALL :fail "Jasper Server OS edition expected as input" )

rem
rem Validating argument count.
rem
IF %ARGUMENT_COUNT% GTR 2 ( CALL :fail "Too many arguments" )

rem
rem Choosing setup parameters depending on the given arguments.
rem
SET JS_OPTION=%2
IF "%JS_OPTION%" == "" ( GOTO :default )
IF %JS_OPTION% == minimal ( GOTO :minimal )
IF %JS_OPTION% == regen-config ( GOTO :regenConfig )
IF %JS_OPTION% == drop-db ( GOTO :dropDb )
IF %JS_OPTION% == test ( GOTO :test )
IF %JS_OPTION% == help ( GOTO :help )
CALL :fail "Invalid option specified"
EXIT /b 1

:default
SET JS_ANT_TARGET=install-normal-%JS_EDITION%
SET JS_OPTION=default
GOTO :proceedToSetup

:minimal
SET JS_ANT_TARGET=install-minimal-%JS_EDITION%
GOTO :proceedToSetup

:regenConfig
SET JS_ANT_TARGET=refresh-config
GOTO :proceedToSetup

:dropDb
SET JS_ANT_TARGET=drop-js-db
GOTO :proceedToSetup

:test
SET JS_ANT_TARGET=pre-install-test-%JS_EDITION%
GOTO :proceedToSetup

:help
TYPE bin\install.help
GOTO :end

rem
rem Calling core setup script with determined parameters.
rem
:proceedToSetup
CALL "%~dp0"do-js-setup.bat install %JS_EDITION% %JS_OPTION% %JS_ANT_TARGET%
GOTO :end

rem -----------------------------------------------------------------------------

:showUsage
ECHO Please enter one of the following options: 'EMPTY', minimal, regen-config, drop-db, test
GOTO:EOF

:fail
IF NOT "%~1" == "" ( ECHO %~1 )
CALL :showUsage
EXIT /b 1

:end
EXIT /b 0
