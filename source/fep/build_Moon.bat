@echo off
SET JAVA_HOME=C:/Users/wisem/.jdks/semeru-21.0.2
SET MAVEN_HOME=D:/software/Maven/apache-maven-3.8.8
SET PATH=%PATH%;%JAVA_HOME%/bin;%MAVEN_HOME%/bin
call mvn clean install -Dmaven.repo.local=D:/maven/repository -f pom.xml
call mvn clean install -Dmaven.repo.local=D:/maven/repository -pl fep-web -Pwar
pause