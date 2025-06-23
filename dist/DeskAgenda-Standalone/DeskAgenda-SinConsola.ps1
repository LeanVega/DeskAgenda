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
