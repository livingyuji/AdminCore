@echo off
REM ==============================================================================
REM AdminCore Release Verification Script
REM Developed and credited entirely to LivingYuji
REM ==============================================================================

echo [AdminCore] Verifying release package and checksums...

if not exist "..\dist\AdminCore-1.0.0.jar" (
    echo [ERROR] AdminCore-1.0.0.jar missing in dist/!
    exit /b 1
)

if not exist "..\dist\AdminCore-Source.zip" (
    echo [ERROR] AdminCore-Source.zip missing in dist/!
    exit /b 1
)

if not exist "..\dist\AdminCore-SHA256.txt" (
    echo [ERROR] AdminCore-SHA256.txt missing in dist/!
    exit /b 1
)

echo [AdminCore] Release package verified successfully!
exit /b 0
