@echo off
title SmartCare: AI-Enhanced Hospital Management System
cls
echo ================================================================
echo   SmartCare: AI-Enhanced Hospital Management System (Java 21)
echo ================================================================
echo.

set "JAVA_EXE=C:\Program Files\Java\jdk-26.0.1\bin\java.exe"
if not exist "%JAVA_EXE%" (
    set "JAVA_EXE=java"
)

echo Starting SmartCare Application...
echo Configuration loaded from: db.properties
echo.

"%JAVA_EXE%" -jar "target\smartcare-hospital-management-1.0.0-jar-with-dependencies.jar"

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo Application exited with code %ERRORLEVEL%.
    pause
)
