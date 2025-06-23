@echo off
echo ================================
echo  Construyendo DeskAgenda Portable
echo ================================

:: Limpiar builds anteriores
if exist "dist" rmdir /s /q "dist"
if exist "build" rmdir /s /q "build"

:: Crear directorios necesarios
mkdir build\classes 2>nul
mkdir dist\DeskAgenda-Portable 2>nul

echo.
echo [1/3] Compilando codigo fuente...
javac -d build\classes -encoding UTF-8 -cp . src\igu\*.java src\igu\util\*.java src\logica\*.java src\persistencia\*.java
if %errorlevel% neq 0 (
    echo ERROR: Fallo en la compilacion
    pause
    exit /b 1
)

echo.
echo [2/3] Copiando recursos...
xcopy "src\notification" "build\classes\notification\" /E /I /Y >nul
if exist "src\persistencia\tareas.json" (
    copy "src\persistencia\tareas.json" "build\classes\persistencia\" >nul
) else (
    copy "src\persistencia\tareas.example.json" "build\classes\persistencia\tareas.json" >nul
)

echo.
echo [3/3] Creando JAR ejecutable...
echo Main-Class: logica.AgendaAct > manifest.txt
jar cfm dist\DeskAgenda-Portable\DeskAgenda.jar manifest.txt -C build\classes .
del manifest.txt

:: Crear script de ejecucion
echo @echo off > "dist\DeskAgenda-Portable\Ejecutar-DeskAgenda.bat"
echo cd /d "%%~dp0" >> "dist\DeskAgenda-Portable\Ejecutar-DeskAgenda.bat"
echo java -jar DeskAgenda.jar >> "dist\DeskAgenda-Portable\Ejecutar-DeskAgenda.bat"
echo pause >> "dist\DeskAgenda-Portable\Ejecutar-DeskAgenda.bat"

:: Crear README
echo DeskAgenda - Version Portable > "dist\DeskAgenda-Portable\LEEME.txt"
echo. >> "dist\DeskAgenda-Portable\LEEME.txt"
echo Para ejecutar: >> "dist\DeskAgenda-Portable\LEEME.txt"
echo 1. Asegurate de tener Java instalado >> "dist\DeskAgenda-Portable\LEEME.txt"
echo 2. Haz doble clic en "Ejecutar-DeskAgenda.bat" >> "dist\DeskAgenda-Portable\LEEME.txt"
echo. >> "dist\DeskAgenda-Portable\LEEME.txt"
echo Esta version es portable y puede ejecutarse desde USB >> "dist\DeskAgenda-Portable\LEEME.txt"

echo.
echo ================================
echo  EXITO! Version portable creada en:
echo  dist\DeskAgenda-Portable\
echo ================================
echo.
echo Para usar: Ejecuta "Ejecutar-DeskAgenda.bat"
echo.
pause
