@echo off

java -cp ${project.artifactId}-${assembly-func}.jar -Dloader.path=. org.springframework.boot.loader.PropertiesLauncher

pause