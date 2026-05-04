
@echo off

REM Collect the command line args

set JS_CMD_NAME=%0

set CMD_LINE_ARGS="--transfer"

set CONFIG_DIR=conf_source\iePro
set JS_EDITION="pro"

REM additional config dir to find the js.jdbc.properties
set ADDITIONAL_CONFIG_DIR=build_conf\default

REM Add all jars to the export classpath variable
set EXP_CLASSPATH=%CONFIG_DIR%\lib\*

rem
rem Initializing time variable.
rem
for /f "delims=" %%# in ('powershell get-date -format "{yyyy-MM-dd_HH-mm}"') do @set _date=%%#
SET JS_CURRENT_TIME=%_date%
SET JS_SETUP_MODE="transfer-audit-data"

rem
rem Defining log file name, creating log directory if it doesn't exist.
rem
SET /a JS_LOG_FILE_PREFIX=%RANDOM%+10000
SET JS_LOG_FILE=logs/js-%JS_SETUP_MODE%-%JS_EDITION%_%JS_CURRENT_TIME%_%JS_LOG_FILE_PREFIX%.log
IF NOT EXIST logs (
  md logs
)
ECHO Writing to log file: %JS_LOG_FILE%


REM Set the java command

if exist "..\java\bin\java.exe" goto setLocalJava

goto setStandardJava
:setLocalJava

set JAVA_HOME="..\java"
set PATH=..\java\bin;%PATH%

goto doneJava
:setStandardJava

if "%JAVA_HOME%"=="" goto warnJava

goto doneJava
:warnJava

echo "WARNING: Did not find a JAVA_HOME environment variable setting. Script will continue."

:doneJava

:initializeAntEnvironment
IF EXIST "..\apache-ant" ( GOTO :useBundledAnt )
CALL :log "Bundled Ant not found. Using system Ant."
SET ANT_RUN=ant
GOTO :endAntSetup

:useBundledAnt
SET ANT_HOME=..\apache-ant
SET ANT_RUN=%ANT_HOME%\bin\ant
SET PATH=%PATH%;%ANT_HOME%\bin

:endAntSetup


:runAnt


IF "%BUILDOMATIC_MODE%"=="" set BUILDOMATIC_MODE=interactive
SET JS_ANT_TARGET="validate-keystore"

CALL :log "Running %JS_ANT_TARGET% Ant task"
CALL :log
rem This "hack" addresses the problem that a pipe solely returns the status
rem of the last command. Like this, we can pipe the output into the logfile
rem while we also get access to ANT's exit code.
(CALL %ANT_RUN% -nouserlib -lib . -lib lib -f build.xml %JS_ANT_TARGET% %JS_ANT_OPTIONS% 2>&1 & CALL echo %%^^errorlevel%% ^> %JS_LOG_FILE_PREFIX%-status.txt) | "%~dp0/bin/wtee" -a %JS_LOG_FILE%
FOR /f %%A IN (%JS_LOG_FILE_PREFIX%-status.txt) DO SET JS_ANT_STATUS=%%A
DEL %JS_LOG_FILE_PREFIX%-status.txt
IF %JS_ANT_STATUS% GEQ 1 (
    CALL :log "Checking Ant return code: BAD (1)"
    EXIT /b 1
)
CALL :log "Checking Ant return code: OK"
CALL :log
IF %ERRORLEVEL% == 0 ( GOTO :checkInstallType )

:checkInstallType
CALL :log "Running check-install-type Ant task"
CALL :log
CALL %ANT_RUN% -nouserlib -lib . -lib lib -f build.xml "check-install-type"
IF %ERRORLEVEL% == 0 ( GOTO :runTransfer )
IF not %ERRORLEVEL% == 0 ( GOTO :checkInstallTypeFailed )


rem
rem Console + file logging subroutine. TODO
rem
:log
SET JS_LOG_MESSAGE=
IF "%~1" == "" SET JS_LOG_MESSAGE=----------------------------------------------------------------------
IF NOT "%~1" == "" SET JS_LOG_MESSAGE=%~1
ECHO %JS_LOG_MESSAGE% | "%~dp0/bin/wtee" -a %JS_LOG_FILE%
GOTO:EOF

:runTransfer
REM Set the java memory options

set JAVA_OPTS=%JAVA_OPTS% -Xms128m -Xmx512m -noverify

REM Add config dirs to EXP_CLASSPATH

set EXP_CLASSPATH=%CONFIG_DIR%;%CONFIG_DIR%\classes;%ADDITIONAL_CONFIG_DIR%;%EXP_CLASSPATH%;.

java -classpath "%EXP_CLASSPATH%" %JAVA_OPTS% com.jaspersoft.jasperserver.export.AuditAccessTransferCommand %JS_CMD_NAME% %CMD_LINE_ARGS%
IF %ERRORLEVEL% == 0 ( GOTO :dropAuditTablesFromJasperDB )
IF not %ERRORLEVEL% == 0 ( GOTO :runAntFailed )
GOTO:EOF

:dropAuditTablesFromJasperDB
CALL %ANT_RUN% -nouserlib -lib . -lib lib -f build.xml "drop-audit-tables"
GOTO:EOF

:checkInstallTypeFailed
CALL :log "InstallType is not split. The script supports only for split install type"
EXIT /b 1
