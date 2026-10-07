@echo off
cd /d "%~dp0"
title SmartCare Hospital - Launcher
color 0B
cls

:: ================================================================
::   SmartCare Hospital - Combined Launcher
::   Patient Booking Portal + Hospital Staff Login
:: ================================================================

:MAIN_MENU
cls
echo.
echo  ================================================================
echo.
echo       +  +  +    S M A R T C A R E    +  +  +
echo              AI-Enhanced Hospital Management System
echo.
echo  ================================================================
echo.
echo   Who are you?
echo.
echo   [1]  PATIENT          --  Book / Track My Appointment
echo.
echo   [2]  HOSPITAL STAFF   --  Login to SmartCare HMS (Desktop App)
echo.
echo   [3]  EXIT
echo.
echo  ================================================================
echo.
set /p CHOICE="  Enter your choice (1, 2 or 3): "

if "%CHOICE%"=="1" goto :PATIENT_PORTAL
if "%CHOICE%"=="2" goto :STAFF_LOGIN
if "%CHOICE%"=="3" goto :EXIT_CLEAN
echo.
echo  [!] Invalid choice. Please enter 1, 2, or 3.
timeout /t 2 /nobreak >nul
goto :MAIN_MENU


:: ================================================================
::   OPTION 1 — PATIENT BOOKING PORTAL
:: ================================================================
:: Find Java executable
set "JAVA_EXE=C:\Program Files\Common Files\Oracle\Java\javapath\java.exe"
if not exist "%JAVA_EXE%" set "JAVA_EXE=java"

:PATIENT_PORTAL
cls
echo.
echo  ================================================================
echo     PATIENT  --  SmartCare Online Appointment Booking
echo  ================================================================
echo.

:: Check if port 8080 is already active
set "SERVER_ALREADY_RUNNING=0"
for /f "tokens=5" %%a in ('netstat -aon ^| findstr ":8080" 2^>nul') do (
    set "SERVER_ALREADY_RUNNING=1"
)

if "%SERVER_ALREADY_RUNNING%"=="1" (
    echo  [OK] SmartCare Web Portal Server is already running on port 8080!
    echo.
    echo  Opening Patient Booking Portal in your browser...
    start http://localhost:8080
    goto :PORTAL_INFO
)

:: Start WebPortalServer via Java if classes are available
if exist "target\classes\com\smartcare\web\WebPortalServer.class" (
    echo  [1/2] Starting SmartCare Web Server (Java + MySQL backend)...
    start "SmartCare Patient Web Server" "%JAVA_EXE%" -cp "target\classes;target\dependency\*" com.smartcare.web.WebPortalServer "web-portal"
    timeout /t 2 /nobreak >nul
    echo  [2/2] Opening Patient Booking Portal in your browser...
    start http://localhost:8080
    goto :PORTAL_INFO
)

:: Fallback: Check Python
python --version >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo  [1/2] Starting Patient Booking Web Server via Python on port 8080...
    start "SmartCare-WebServer" /min python -m http.server 8080 --directory "%~dp0web-portal"
    timeout /t 2 /nobreak >nul
    echo  [2/2] Opening Patient Booking Portal in your browser...
    start http://localhost:8080
    goto :PORTAL_INFO
)

:: Ultimate fallback: Open HTML directly
echo  [INFO] Opening booking page directly in browser...
start "" "%~dp0web-portal\index.html"

:PORTAL_INFO
cls
echo.
echo  ================================================================
echo     PATIENT BOOKING PORTAL IS NOW OPEN IN YOUR BROWSER!
echo  ================================================================
echo.
echo   BOOKING LINK:  http://localhost:8080
echo.
echo  ----------------------------------------------------------------
echo   HOW TO BOOK YOUR APPOINTMENT  (4 Easy Steps):
echo  ----------------------------------------------------------------
echo.
echo    STEP 1  --  Click "Book Appointment"
echo.
echo    STEP 2  --  Select Department  (e.g. Cardiology, Pediatrics)
echo.
echo    STEP 3  --  Choose your Doctor and preferred Time Slot
echo.
echo    STEP 4  --  Enter Name, Phone, and Reason for Visit
echo.
echo    STEP 5  --  Click "Confirm Booking"
echo.
echo    STEP 6  --  SAVE your Token Number  (e.g. SC-2024-0011)
echo                Show it at the hospital reception counter.
echo.
echo  ----------------------------------------------------------------
echo   TO TRACK YOUR APPOINTMENT LATER:
echo     Open the portal  >>  Click "Track Appointment"
echo     Enter your Token Number  OR  Phone Number
echo  ----------------------------------------------------------------
echo.
echo   EMERGENCY HELPLINE:  +91 98765 43210  (24 x 7)
echo.
echo  ================================================================
echo.
echo   NOTE: Keep this window open while booking.
echo.
echo   Press [B] to go Back to Main Menu
echo   Press any other key to STOP server and EXIT
echo.
set /p ACTION="  Your choice: "
if /i "%ACTION%"=="B" goto :STOP_AND_BACK

:STOP_SERVER
echo.
echo  Stopping web server...
taskkill /FI "WINDOWTITLE eq SmartCare-WebServer" /F >nul 2>&1
goto :EXIT_CLEAN

:STOP_AND_BACK
echo.
echo  Stopping web server...
taskkill /FI "WINDOWTITLE eq SmartCare-WebServer" /F >nul 2>&1
goto :MAIN_MENU


:: ================================================================
::   OPTION 2 — HOSPITAL STAFF LOGIN (SmartCare HMS Desktop App)
:: ================================================================
:STAFF_LOGIN
cls
echo.
echo  ================================================================
echo     HOSPITAL STAFF  --  SmartCare HMS Desktop Application
echo  ================================================================
echo.

:: Check Java
java -version >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo  [ERROR] Java is not installed or not in PATH.
    echo.
    echo  Install Java 21 from: https://adoptium.net/
    echo.
    pause
    goto :MAIN_MENU
)

:: Check if compiled classes exist
if not exist "%~dp0target\classes\com\smartcare\Main.class" (
    echo  [INFO] Compiled classes not found. Building with javac...
    echo.
    if not exist "%~dp0target\classes" mkdir "%~dp0target\classes"
    powershell -NoProfile -Command "(Get-ChildItem -Recurse -Filter *.java '%~dp0src/main/java' | Resolve-Path -Relative) -replace '\\','/' | Set-Content '%~dp0target/sources.txt'"
    "%JAVAC_EXE%" -encoding UTF-8 -cp "%~dp0target\dependency\*;%~dp0src\main\java" -d "%~dp0target\classes" @"%~dp0target\sources.txt"
    if exist "%~dp0target\sources.txt" del "%~dp0target\sources.txt"
)

:: Check dependency JARs
if not exist "%~dp0target\dependency" (
    echo  [INFO] Downloading dependencies via Maven...
    call mvn dependency:copy-dependencies -q >nul 2>&1
)

echo.
echo  ----------------------------------------------------------------
echo   STAFF LOGIN CREDENTIALS (Default):
echo  ----------------------------------------------------------------
echo.
echo   Role            Username          Password
echo   ----------      -----------       ----------
echo   Admin           admin             Admin@123
echo   Doctor          dr.sharma         Admin@123
echo   Doctor          dr.patel          Admin@123
echo   Receptionist    receptionist1     Admin@123
echo   Lab Tech        labtech1          Admin@123
echo   Pharmacist      pharmacist1       Admin@123
echo  ----------------------------------------------------------------
echo.
echo  Starting SmartCare Hospital Management System...
echo.
timeout /t 2 /nobreak >nul

:: Launch the Java Swing app
set "JAVA_EXE=C:\Program Files\Common Files\Oracle\Java\javapath\java.exe"
if not exist "%JAVA_EXE%" set "JAVA_EXE=java"

start "SmartCare HMS" "%JAVA_EXE%" --enable-native-access=ALL-UNNAMED ^
    -cp "%~dp0target\classes;%~dp0target\dependency\*" com.smartcare.Main

echo.
echo  SmartCare HMS is launching...
echo.
echo  Press any key to return to the Main Menu...
pause >nul
goto :MAIN_MENU


:: ================================================================
::   EXIT
:: ================================================================
:EXIT_CLEAN
echo.
echo  ================================================================
echo   Thank you for using SmartCare Hospital!
echo   For emergencies call: +91 98765 43210
echo  ================================================================
echo.
timeout /t 2 /nobreak >nul
exit /b 0
