@echo off
title Zuul Game Launcher

:: Switch to script directory (project root)
cd /d "%~dp0"

:: Check Java
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Java not found. Please install JDK 8 or higher.
    echo Download: https://www.oracle.com/java/technologies/downloads/
    pause
    exit /b 1
)

echo ============================================
echo       World of Zuul - Game Launcher
echo ============================================
echo.
echo   Starting game server...

:: Start server in a new window
start "Zuul Game Server" java -jar "target\zuul-1.0-SNAPSHOT.jar"

:: Wait for server to be ready
echo   Waiting for server to start (approx 5s)...
timeout /t 5 /nobreak >nul

:: Open browser
start http://localhost:8000

echo.
echo   Game is now running in your browser!
echo   http://localhost:8000
echo.
echo   Close this window anytime - the game server will keep running.
echo   To stop the game, close the "Zuul Game Server" console window.
echo.
pause