@echo off
REM ==============================================================================
REM AdminCore Test Execution Script
REM Developed and credited entirely to LivingYuji
REM ==============================================================================

echo [AdminCore] Executing automated tests...
set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot
set PATH=%JAVA_HOME%\bin;%PATH%

pushd "%~dp0.."
call .\gradlew.bat test --info
set TEST_STATUS=%ERRORLEVEL%
popd

if %TEST_STATUS% NEQ 0 (
    echo [ERROR] Tests failed!
    exit /b %TEST_STATUS%
)

echo [AdminCore] All tests passed cleanly!
exit /b 0
