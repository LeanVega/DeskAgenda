@echo off
cd /d "%~dp0"
start /b "" bin\java.exe -Xms32m -Xmx128m -jar bin\DeskAgenda.jar
