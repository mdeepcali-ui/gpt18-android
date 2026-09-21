@rem
@rem Gradle startup script for Windows
@rem

@if "%DEBUG%"=="" @echo off
@setlocal
set DIRNAME=%~dp0
if "%DIRNAME%"=="" set DIRNAME=.
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%

set CLASSPATH=%APP_HOME%\gradle\wrapper\gradle-wrapper.jar
set DEFAULT_JVM_OPTS="-Xmx64m" "-Xms64m"

"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% -classpath "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*
@endlocal
