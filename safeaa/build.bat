@echo off
SET JAVA_HOME=D:/Java/ibm-semeru-open-jdk_x64_windows_17.0.6_10_openj9-0.36.0
SET MAVEN_HOME=D:/Development/apache-maven-3.8.5
SET PATH=%PATH%;%JAVA_HOME%/bin;%MAVEN_HOME%/bin
call mvn clean install -Dmaven.repo.local=E:/maven/repository -f pom.xml
pause