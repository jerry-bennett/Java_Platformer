@rem
@rem Copyright 2015 the original author or authors.
@rem
@rem Licensed under the Apache License, Version 2.0 (the "License");
@rem you may not use this file except in compliance with the License.
@rem You may obtain a copy of the License at
@rem
@rem     https://www.apache.org/licenses/LICENSE-2.0
@rem
@rem Unless required by applicable law or agreed to in writing, software
@rem distributed under the License is distributed on an "AS IS" BASIS,
@rem WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@rem See the License for the specific language governing permissions and
@rem limitations under the License.
@rem

@if "%DEBUG%"=="" @echo off
@REM ##########################################################################
@REM
@REM  Gradle startup script for Windows
@REM
@REM ##########################################################################

@rem Set local scope for the variables with windows shortcut syntax
if "%APPDATA%\gradle\wrapper\gradle-wrapper.jar" exist (
    set GRADLE_USER_HOME=%APPDATA%\gradle\wrapper
) else if "%USERPROFILE%\gradle\wrapper\gradle-wrapper.jar" exist (
    set GRADLE_USER_HOME=%USERPROFILE%\gradle\wrapper
)

@rem If the environment variables above are set then set a default path to the gradle wrapper
if defined JAVA_HOME (
    "java.exe" -classpath "%APPDATA%\gradle\wrapper\gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain %*
) else (
    "java.exe" -classpath "C:\Program Files\Gradle\8.5\lib\gradle-launcher-8.5.jar" org.gradle.wrapper.GradleWrapperMain %*
)

%JAVA_HOME%\bin/java -version
exit /b %EXIT_CODE%
