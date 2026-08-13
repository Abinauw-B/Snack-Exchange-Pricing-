@echo off
title Pub Exchange Cross-Panel Server Launcher
color 0A
echo =========================================================================
echo  NOIDA PUB EXCHANGE - DYNAMIC BEVERAGE STOCK MARKET PLATFORM
echo =========================================================================
echo  Starting Shared Local HTTP Server on http://localhost:8000 ...
echo.

cd /d "%~dp0"
python -m http.server 8000

pause
