@echo off
echo ================================
echo  Construyendo DeskAgenda ejecutable
echo ================================

:: Limpiar builds anteriores
if exist "dist" rmdir /s /q "dist"
if exist "build" rmdir /s /q "build"

:: Crear directorios necesarios
mkdir build\classes 2>nul
mkdir dist 2>nul

echo.
echo [1/4] Compilando codigo fuente...
javac -d build\classes -encoding UTF-8 -cp . src\igu\*.java src\igu\util\*.java src\logica\*.java src\persistencia\*.java
if %errorlevel% neq 0 (
    echo ERROR: Fallo en la compilacion
    pause
    exit /b 1
)

echo.
echo [2/4] Copiando recursos...
xcopy "src\notification" "build\classes\notification\" /E /I /Y >nul
if exist "src\persistencia\tareas.json" (
    copy "src\persistencia\tareas.json" "build\classes\persistencia\" >nul
) else (
    copy "src\persistencia\tareas.example.json" "build\classes\persistencia\tareas.json" >nul
)

echo.
echo [3/4] Creando JAR ejecutable...
echo Main-Class: logica.AgendaAct > manifest.txt
jar cfm dist\DeskAgenda.jar manifest.txt -C build\classes .
del manifest.txt

echo.
echo [4/4] Generando ejecutable nativo con jlink...
jlink --module-path "%JAVA_HOME%\jmods" --add-modules java.base,java.desktop,java.logging --output dist\DeskAgenda-Runtime --strip-debug --compress=2 --no-header-files --no-man-pages --launcher DeskAgenda=dist/DeskAgenda.jar

if %errorlevel% neq 0 (
    echo.
    echo INFO: jlink fallo. Creando ejecutable con jpackage...
    goto :use_jpackage
)

:: Copiar JAR al runtime
copy "dist\DeskAgenda.jar" "dist\DeskAgenda-Runtime\bin\" >nul

echo.
echo ================================
echo  EXITO! Ejecutable creado en:
echo  dist\DeskAgenda-Runtime\bin\DeskAgenda.exe
echo ================================
goto :end

:use_jpackage
echo Intentando con jpackage (Java 14+)...
jpackage --input dist --main-jar DeskAgenda.jar --main-class logica.AgendaAct --name DeskAgenda --app-version 1.0 --dest dist --win-console

:end
echo.
echo Presiona cualquier tecla para continuar...
pause >nul
