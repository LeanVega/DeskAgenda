# Script PowerShell para crear ejecutable con Launch4j
# Requiere descargar Launch4j primero

param(
    [string]$Launch4jPath = "C:\Program Files (x86)\Launch4j"
)

Write-Host "================================" -ForegroundColor Green
Write-Host " Construyendo con Launch4j" -ForegroundColor Green  
Write-Host "================================" -ForegroundColor Green

# Verificar si Launch4j esta instalado
if (!(Test-Path "$Launch4jPath\launch4j.exe")) {
    Write-Host "ERROR: Launch4j no encontrado en $Launch4jPath" -ForegroundColor Red
    Write-Host "Descarga Launch4j desde: http://launch4j.sourceforge.net/" -ForegroundColor Yellow
    Write-Host "O especifica la ruta: .\build-launch4j.ps1 -Launch4jPath 'C:\ruta\a\launch4j'" -ForegroundColor Yellow
    exit 1
}

# Compilar primero
Write-Host "[1/3] Compilando aplicacion..." -ForegroundColor Cyan
& .\build-portable.bat

if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Fallo la compilacion" -ForegroundColor Red
    exit 1
}

# Crear archivo de configuracion para Launch4j
$configXml = @"
<?xml version="1.0" encoding="UTF-8"?>
<launch4jConfig>
  <dontWrapJar>false</dontWrapJar>
  <headerType>gui</headerType>
  <jar>dist\DeskAgenda-Portable\DeskAgenda.jar</jar>
  <outfile>dist\DeskAgenda.exe</outfile>
  <errTitle>DeskAgenda</errTitle>
  <cmdLine></cmdLine>
  <chdir>.</chdir>
  <priority>normal</priority>
  <downloadUrl>http://java.com/download</downloadUrl>
  <supportUrl></supportUrl>
  <stayAlive>false</stayAlive>
  <restartOnCrash>false</restartOnCrash>
  <manifest></manifest>
  <icon></icon>
  <jre>
    <path></path>
    <bundledJre64Bit>false</bundledJre64Bit>
    <bundledJreAsFallback>false</bundledJreAsFallback>
    <minVersion>11.0.0</minVersion>
    <maxVersion></maxVersion>
    <jdkPreference>preferJre</jdkPreference>
    <runtimeBits>64/32</runtimeBits>
  </jre>
</launch4jConfig>
"@

Write-Host "[2/3] Creando configuracion Launch4j..." -ForegroundColor Cyan
$configXml | Out-File -FilePath "launch4j-config.xml" -Encoding UTF8

Write-Host "[3/3] Generando ejecutable .exe..." -ForegroundColor Cyan
& "$Launch4jPath\launch4j.exe" "launch4j-config.xml"

if (Test-Path "dist\DeskAgenda.exe") {
    Write-Host "================================" -ForegroundColor Green
    Write-Host " EXITO! Ejecutable creado:" -ForegroundColor Green
    Write-Host " dist\DeskAgenda.exe" -ForegroundColor Yellow
    Write-Host "================================" -ForegroundColor Green
} else {
    Write-Host "ERROR: No se pudo crear el ejecutable" -ForegroundColor Red
}

# Limpiar
Remove-Item "launch4j-config.xml" -ErrorAction SilentlyContinue

Write-Host "Presiona Enter para continuar..."
Read-Host
