@echo off
REM Wrapper for Java build tools
java -version 2>nul || exit /b 1

%JAVA_HOME%/bin/java -jar .gradle/wrapper/dists/*.jar --stacktrace %*
