# Script para crear un ejecutable completamente autonomo
# usando jlink (disponible en Java 11+)

Write-Host "========================================" -ForegroundColor Green
Write-Host " DeskAgenda - Ejecutable Autonomo" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green

# Compilar la aplicacion primero
Write-Host "[1/4] Compilando aplicacion..." -ForegroundColor Cyan
& ".\crear ejecutables\build-portable.bat"
if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Fallo la compilacion" -ForegroundColor Red
    exit 1
}

Write-Host "[2/4] Detectando modulos necesarios..." -ForegroundColor Cyan

# Crear un runtime minimo con jlink
$javaHome = $env:JAVA_HOME
if (-not $javaHome) {
    $javaHome = (Get-Command java).Source | Split-Path | Split-Path
}

Write-Host "[3/4] Creando runtime personalizado..." -ForegroundColor Cyan

# Crear un runtime minimo con jlink
$jlinkCmd = & "$javaHome\bin\jlink.exe" --module-path "$javaHome\jmods" --add-modules java.base,java.desktop,java.logging,java.prefs --output dist\DeskAgenda-Standalone --strip-debug --compress=2 --no-header-files --no-man-pages

if (-not (Test-Path "dist\DeskAgenda-Standalone")) {
    Write-Host "ERROR: No se pudo crear el runtime personalizado" -ForegroundColor Red
    exit 1
}

Write-Host "[4/4] Configurando ejecutable final..." -ForegroundColor Cyan

# Copiar el JAR al directorio del runtime
Copy-Item "dist\DeskAgenda-Portable\DeskAgenda.jar" "dist\DeskAgenda-Standalone\bin\"

# Crear script de ejecucion (con consola)
$launcherScript = @'
@echo off
cd /d "%~dp0"
start /b "" bin\java.exe -Xms32m -Xmx128m -jar bin\DeskAgenda.jar
'@

$launcherScript | Out-File -FilePath "dist\DeskAgenda-Standalone\DeskAgenda.bat" -Encoding ASCII

# Crear lanzador VBS sin consola (RECOMENDADO)
$vbsLauncher = @'
Set objShell = CreateObject("WScript.Shell")
Set objFSO = CreateObject("Scripting.FileSystemObject")

' Obtener la ruta actual del script
strScriptPath = objFSO.GetParentFolderName(WScript.ScriptFullName)

' Cambiar al directorio del script
objShell.CurrentDirectory = strScriptPath

' Ejecutar Java sin mostrar ventana de consola
objShell.Run "bin\java.exe -Xms32m -Xmx128m -jar bin\DeskAgenda.jar", 0, False
'@

$vbsLauncher | Out-File -FilePath "dist\DeskAgenda-Standalone\DeskAgenda.vbs" -Encoding ASCII

# Crear script PowerShell sin consola (alternativa)
$psLauncher = @'
# Configurar proceso sin ventana
$psi = New-Object System.Diagnostics.ProcessStartInfo
$psi.FileName = "$PSScriptRoot\bin\java.exe"
$psi.Arguments = "-Xms32m -Xmx128m -jar bin\DeskAgenda.jar"
$psi.WorkingDirectory = $PSScriptRoot
$psi.WindowStyle = [System.Diagnostics.ProcessWindowStyle]::Hidden
$psi.CreateNoWindow = $true
$psi.UseShellExecute = $false

# Iniciar el proceso
[System.Diagnostics.Process]::Start($psi) | Out-Null
'@

$psLauncher | Out-File -FilePath "dist\DeskAgenda-Standalone\DeskAgenda-SinConsola.ps1" -Encoding UTF8

# Crear script para generar acceso directo
$shortcutScript = @'
# Script para crear acceso directo en el escritorio
param(
    [string]$DesktopPath = [Environment]::GetFolderPath("Desktop")
)

Write-Host "Creando acceso directo en el escritorio..." -ForegroundColor Cyan

$WshShell = New-Object -comObject WScript.Shell
$Shortcut = $WshShell.CreateShortcut("$DesktopPath\DeskAgenda.lnk")
$Shortcut.TargetPath = "$PSScriptRoot\DeskAgenda.vbs"
$Shortcut.WorkingDirectory = $PSScriptRoot
$Shortcut.Description = "DeskAgenda - Gestor de Tareas"
$Shortcut.WindowStyle = 7  # Minimizada
$Shortcut.Save()

Write-Host "Acceso directo creado en: $DesktopPath\DeskAgenda.lnk" -ForegroundColor Green
Write-Host "Ahora puedes ejecutar DeskAgenda desde el escritorio sin consola!" -ForegroundColor Yellow
'@

$shortcutScript | Out-File -FilePath "dist\DeskAgenda-Standalone\Crear-Acceso-Directo.ps1" -Encoding UTF8

# Copiar recursos necesarios
if (Test-Path "src\persistencia\tareas.json") {
    Copy-Item "src\persistencia\tareas.json" "dist\DeskAgenda-Standalone\"
} else {
    Copy-Item "src\persistencia\tareas.example.json" "dist\DeskAgenda-Standalone\tareas.json"
}

# Crear README
$readme = @"
DeskAgenda - Version Autonoma
============================

Esta version incluye todo lo necesario para ejecutar DeskAgenda
sin necesidad de tener Java instalado en el sistema.

OPCIONES DE EJECUCION:
=====================

1. DeskAgenda.vbs (RECOMENDADO)
   - Haz doble clic en este archivo
   - NO muestra consola negra
   - Experiencia mas limpia

2. DeskAgenda.bat  
   - Muestra consola en segundo plano
   - Util para depuracion

3. DeskAgenda-SinConsola.ps1
   - Version PowerShell sin consola
   - Alternativa al VBS

Caracteristicas:
- No requiere Java instalado
- Completamente portable
- Tamaño optimizado (~50MB vs 200MB+ con JRE completo)
- Funciona en Windows x64
- Uso minimo de memoria (32-128MB)

RECOMENDACION: Usa "DeskAgenda.vbs" para la mejor experiencia
sin ventanas de consola molestas.

Nota: Este ejecutable solo funciona en Windows x64.
Para otras plataformas, usa la version portable con Java instalado.
"@

$readme | Out-File -FilePath "dist\DeskAgenda-Standalone\README.txt" -Encoding UTF8

Write-Host "========================================" -ForegroundColor Green
Write-Host " EXITO! Ejecutable autonomo creado en:" -ForegroundColor Green
Write-Host " dist\DeskAgenda-Standalone\" -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Host "Para distribuir: Comprime toda la carpeta 'DeskAgenda-Standalone'" -ForegroundColor Cyan
Write-Host "Los usuarios solo necesitan extraer y ejecutar DeskAgenda.bat" -ForegroundColor Cyan
Write-Host ""
Write-Host "Presiona Enter para continuar..."
Read-Host
