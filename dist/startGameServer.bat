@echo off
title L2jTesla gameserver console
:start
REM -------------------------------------
REM Default parameters for a basic server.
java -Xmx2G -cp ./libs/*; dev.l2j.tesla.gameserver.GameServer
REM -------------------------------------
if ERRORLEVEL 2 goto restart
if ERRORLEVEL 1 goto error
goto end
:restart
echo.
echo Admin have restarted, please wait.
echo.
goto start
:error
echo.
echo Server have terminated abnormaly.
echo.
:end
echo.
echo Server terminated.
echo.
pause
