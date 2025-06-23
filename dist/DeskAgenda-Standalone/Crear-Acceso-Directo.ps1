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
