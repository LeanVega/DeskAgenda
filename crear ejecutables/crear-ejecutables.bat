@echo off
title DeskAgenda - Crear Distribuibles
echo ===============================================
echo              DESKAGENDA BUILDER
echo ===============================================
echo.
echo Selecciona el tipo de ejecutable a crear:
echo.
echo [1] Portable (Requiere Java instalado) - 1MB
echo [2] Autonomo (No requiere Java) - ~50MB  
echo [3] Con Launch4j (.exe) - Requiere descargar Launch4j
echo [4] Crear TODOS los tipos
echo [5] Solo limpiar builds anteriores
echo.
set /p choice="Tu eleccion (1-5): "

if "%choice%"=="1" goto :portable
if "%choice%"=="2" goto :standalone  
if "%choice%"=="3" goto :launch4j
if "%choice%"=="4" goto :all
if "%choice%"=="5" goto :clean
goto :invalid

:clean
echo.
echo Limpiando builds anteriores...
if exist "dist" rmdir /s /q "dist"
if exist "build" rmdir /s /q "build"
echo Limpieza completada.
goto :end

:portable
echo.
echo Creando version PORTABLE...
call build-portable.bat
goto :package_portable

:standalone
echo.
echo Creando version AUTONOMA...
powershell -ExecutionPolicy Bypass -File .\build-standalone.ps1
goto :package_standalone

:launch4j
echo.
echo Creando ejecutable con Launch4j...
powershell -ExecutionPolicy Bypass -File .\build-launch4j.ps1
goto :end

:all
echo.
echo Creando TODAS las versiones...
echo.
echo [1/3] Version portable...
call build-portable.bat
echo.
echo [2/3] Version autonoma...  
powershell -ExecutionPolicy Bypass -File .\build-standalone.ps1
echo.
echo [3/3] Empaquetando todo...
goto :package_all

:package_portable
echo.
echo Empaquetando version portable...
cd dist
if exist "DeskAgenda-Portable.zip" del "DeskAgenda-Portable.zip"
powershell -Command "Compress-Archive -Path 'DeskAgenda-Portable' -DestinationPath 'DeskAgenda-Portable.zip'"
cd ..
echo Version portable empaquetada: dist\DeskAgenda-Portable.zip
goto :end

:package_standalone
echo.
echo Empaquetando version autonoma...
cd dist
if exist "DeskAgenda-Standalone.zip" del "DeskAgenda-Standalone.zip"
powershell -Command "Compress-Archive -Path 'DeskAgenda-Standalone' -DestinationPath 'DeskAgenda-Standalone.zip'"
cd ..
echo Version autonoma empaquetada: dist\DeskAgenda-Standalone.zip
goto :end

:package_all
cd dist
if exist "DeskAgenda-Portable.zip" del "DeskAgenda-Portable.zip"
if exist "DeskAgenda-Standalone.zip" del "DeskAgenda-Standalone.zip"
powershell -Command "Compress-Archive -Path 'DeskAgenda-Portable' -DestinationPath 'DeskAgenda-Portable.zip'"
powershell -Command "Compress-Archive -Path 'DeskAgenda-Standalone' -DestinationPath 'DeskAgenda-Standalone.zip'"
cd ..
echo.
echo ===============================================
echo           CREACION COMPLETADA
echo ===============================================
echo.
echo Archivos creados en dist\:
echo  - DeskAgenda-Portable.zip     (1MB - Requiere Java)
echo  - DeskAgenda-Standalone.zip   (50MB - No requiere Java)
echo.
echo RECOMENDACION: Usa la version Standalone para
echo distribuir a usuarios que no tienen Java.
goto :end

:invalid
echo.
echo Opcion invalida. Saliendo...
goto :end

:end
echo.
pause
