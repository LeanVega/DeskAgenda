@echo off
echo ================================
echo  Construyendo con GraalVM Native
echo ================================
echo.
echo NOTA: Requiere GraalVM instalado
echo Descarga desde: https://www.graalvm.org/downloads/
echo.

:: Verificar GraalVM
where native-image >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: GraalVM Native Image no encontrado
    echo Instala GraalVM y ejecuta: gu install native-image
    pause
    exit /b 1
)

:: Compilar JAR primero
call build-portable.bat
if %errorlevel% neq 0 exit /b 1

echo.
echo [4/4] Creando ejecutable nativo...
cd dist\DeskAgenda-Portable

native-image ^
    --no-fallback ^
    --enable-preview ^
    --initialize-at-build-time ^
    --allow-incomplete-classpath ^
    -jar DeskAgenda.jar ^
    DeskAgenda-Native

if %errorlevel% eq 0 (
    echo.
    echo ================================
    echo  EXITO! Ejecutable nativo creado:
    echo  dist\DeskAgenda-Portable\DeskAgenda-Native.exe
    echo ================================
    echo.
    echo Este ejecutable NO requiere Java instalado
) else (
    echo.
    echo ERROR: Fallo la creacion del ejecutable nativo
)

cd ..\..
pause
