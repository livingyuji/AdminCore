@echo off
REM ==============================================================================
REM AdminCore Build Script
REM Developed and credited entirely to LivingYuji
REM ==============================================================================

echo [AdminCore] Starting clean build...
set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot
set PATH=%JAVA_HOME%\bin;%PATH%

pushd "%~dp0.."
call .\gradlew.bat clean build
set BUILD_STATUS=%ERRORLEVEL%
popd

if %BUILD_STATUS% NEQ 0 (
    echo [ERROR] Build failed!
    exit /b %BUILD_STATUS%
)

echo [AdminCore] Build successful!
exit /b 0
