Set objShell = CreateObject("WScript.Shell")
Set objFSO = CreateObject("Scripting.FileSystemObject")

' Obtener la ruta actual del script
strScriptPath = objFSO.GetParentFolderName(WScript.ScriptFullName)

' Cambiar al directorio del script
objShell.CurrentDirectory = strScriptPath

' Ejecutar Java sin mostrar ventana de consola
objShell.Run "bin\java.exe -Xms32m -Xmx128m -jar bin\DeskAgenda.jar", 0, False
