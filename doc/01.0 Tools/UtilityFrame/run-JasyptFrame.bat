@echo off
setlocal enabledelayedexpansion
set LIB_PATH=.
for %%i in (lib\*.jar) do (
set LIB_PATH=!LIB_PATH!;%%i
)
java -cp "%LIB_PATH%" -Dfile.encoding=UTF-8 com.syscom.fep.tools.utilityframe.JasyptFrame
pause