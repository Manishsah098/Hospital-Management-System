@echo off
title SmartCare: AI-Enhanced Hospital Management System
cls
echo ================================================================
echo   SmartCare: AI-Enhanced Hospital Management System
echo ================================================================
echo.

set "JAVAC_EXE=C:\Program Files\Common Files\Oracle\Java\javapath\javac.exe"
if not exist "%JAVAC_EXE%" set "JAVAC_EXE=javac"

set "JAVA_EXE=C:\Program Files\Common Files\Oracle\Java\javapath\java.exe"
if not exist "%JAVA_EXE%" set "JAVA_EXE=java"

echo [1/2] Compiling latest source code...
if not exist "target\classes" mkdir target\classes
powershell -NoProfile -Command "(Get-ChildItem -Recurse -Filter *.java src/main/java | Resolve-Path -Relative) -replace '\\','/' | Set-Content target/sources.txt"
"%JAVAC_EXE%" -encoding UTF-8 -cp "target\dependency\*;src\main\java" -d target\classes "@target\sources.txt"
if exist "target\sources.txt" del "target\sources.txt"

echo [2/2] Starting SmartCare Application...
echo Configuration loaded from: db.properties
echo.

"%JAVA_EXE%" --enable-native-access=ALL-UNNAMED -cp "target\classes;target\dependency\*" com.smartcare.Main

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo Application exited with code %ERRORLEVEL%.
    pause
)
