@echo off

rem complie
javac -encoding utf-8 -cp ims-connect-api-3.2.0.7.jar IMSTesterMain.java

rem execute
java -Dfile.encoding=UTF-8 -cp .;ims-connect-api-3.2.0.7.jar IMSTesterMain

pause