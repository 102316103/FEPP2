@echo off

set programName=%1
set arguments=%2
set jarFile=${project.artifactId}${assembly-func}.jar

if "%programName%"=="" goto help
if "%programName%"=="h" goto help
if "%programName%"=="help" goto help
if "%programName%"=="?" goto help

java -jar %jarFile% -p %programName% -a %arguments%

goto finish

:help
java -jar %jarFile% -h
goto finish

:finish
pause