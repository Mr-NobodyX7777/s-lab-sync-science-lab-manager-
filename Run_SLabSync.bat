@echo off
title S Lab Sync
cd /d "%~dp0"

if not exist "lib\mysql-connector-j.jar" (
    echo.
    echo ============================================================
    echo   MySQL driver not found!
    echo   Please download "mysql-connector-j-9.x.x.jar" from:
    echo   https://dev.mysql.com/downloads/connector/j/
    echo   and place it inside the "lib" folder as:
    echo   lib\mysql-connector-j.jar
    echo ============================================================
    echo.
    pause
    exit /b 1
)

java -jar SLabSync.jar
pause
