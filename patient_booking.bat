@echo off
cd /d "%~dp0"
title SmartCare Hospital - Patient Online Booking Portal
color 0B
cls

echo ================================================================
echo    +  +  +   SmartCare Hospital - Patient Booking Portal   +  +  +
echo ================================================================
echo.

set "JAVA_CMD=java"
if exist "C:\Program Files\Common Files\Oracle\Java\javapath\java.exe" (
    set "JAVA_CMD=C:\Program Files\Common Files\Oracle\Java\javapath\java.exe"
)

:: Check if port 8080 is already active
set "PORT_IN_USE=0"
for /f "tokens=5" %%a in ('netstat -aon ^| findstr ":8080" 2^>nul') do (
    set "PORT_IN_USE=1"
)

if "%PORT_IN_USE%"=="0" (
    echo [1/2] Starting SmartCare Web Server on port 8080...
    start "SmartCare Patient Web Server" "%JAVA_CMD%" -cp "target\classes;target\dependency\*" com.smartcare.web.WebPortalServer "web-portal"
    timeout /t 2 /nobreak >nul
) else (
    echo [1/2] SmartCare Server is already active on port 8080.
)

echo [2/2] Opening Patient Booking Portal in browser...
start http://localhost:8080

cls
echo ================================================================
echo    PATIENT BOOKING PORTAL IS OPEN IN YOUR BROWSER!
echo ================================================================
echo.
echo    Direct Link:  http://localhost:8080
echo.
echo    How to Book:
echo      1. Select Department
echo      2. Choose Doctor and Time Slot
echo      3. Enter Name and Phone Number
echo      4. Click "Confirm Booking" to get your Digital Token!
echo.
echo ================================================================
echo.
echo   Keep this window open while booking.
echo   Press any key to close this window.
pause >nul
