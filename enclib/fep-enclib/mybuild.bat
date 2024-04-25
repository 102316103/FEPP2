@echo off
SET JAVA_HOME=C:\Program Files\Semeru\jdk-17.0.6.10-openj9
SET MAVEN_HOME=D:\apache-maven-3.8.8
SET PATH=%PATH%;%JAVA_HOME%/bin;%MAVEN_HOME%/bin
call mvn clean install -Dmaven.repo.local=D:/maven/repository -f pom.xml
rem call mvn clean install -Dmaven.repo.local=D:/maven/repository -pl fep-web -Pwar
pause