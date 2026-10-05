@echo off
title SmartCare MySQL Database Setup
cls
echo ================================================================
echo       SmartCare Database Schema Initialization Tool
echo ================================================================
echo.
echo This script will initialize the 'smartcare_hospital' database
echo and load all required tables and seed data from database\schema.sql.
echo.

set /p MYSQL_USER="Enter MySQL Username [default: root]: "
if "%MYSQL_USER%"=="" set "MYSQL_USER=root"

set /p MYSQL_PASS="Enter MySQL Password: "

echo.
echo Executing database\schema.sql into MySQL...
mysql -u %MYSQL_USER% -p%MYSQL_PASS% < "database\schema.sql"

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ================================================================
    echo [SUCCESS] Database and seed data created successfully!
    echo ================================================================
    echo Please make sure the password matches in 'db.properties'.
) else (
    echo.
    echo [NOTE] If 'mysql' command is not in PATH, you can open MySQL Workbench
    echo and execute the contents of 'database\schema.sql'.
)

echo.
pause
