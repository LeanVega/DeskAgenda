package logica;

import igu.VistaPrincipal;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.prefs.Preferences;
import java.io.File;

public class SistemaBandeja {
    private TrayIcon trayIcon;
    private VistaPrincipal vistaPrincipal;
    private GestorTareas gestorTareas;
    private CheckboxMenuItem ejecutarAlInicioItemAWT; // Checkbox para "Ejecutar al inicio" en la bandeja del sistema. Todavía no hace nada, pero se guarda la preferencia.
    private Preferences prefs;

    // Clave para guardar la preferencia de "ejecutar al inicio"
    private static final String EJECUTAR_AL_INICIO_PREF_KEY = "ejecutarAlInicio";

    public SistemaBandeja(VistaPrincipal vistaPrincipal, GestorTareas gestorTareas) {
        this.vistaPrincipal = vistaPrincipal;
        this.gestorTareas = gestorTareas;
        // Inicializar Preferences API (nodo específico para esta aplicación)
        this.prefs = Preferences.userNodeForPackage(SistemaBandeja.class);

        if (SystemTray.isSupported()) {
            SystemTray tray = SystemTray.getSystemTray();
            
            // Crear una imagen por defecto para el TrayIcon
            // Esto elimina toda la lógica de carga de un archivo de icono personalizado.
            Image image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = ((BufferedImage) image).createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(new Color(0, 0, 0, 0)); // Fondo transparente
            g2d.fillRect(0, 0, 16, 16);
            g2d.setColor(Color.ORANGE); // Círculo naranja simple como icono por defecto
            g2d.fillOval(0, 0, 15, 15);
            g2d.dispose();

            PopupMenu popup = new PopupMenu();
            MenuItem mostrarItem = new MenuItem("Mostrar Agenda");
            mostrarItem.addActionListener(e -> vistaPrincipal.mostrarVentana());
            popup.add(mostrarItem);

            ejecutarAlInicioItemAWT = new CheckboxMenuItem("Ejecutar al inicio");
            ejecutarAlInicioItemAWT.setState(cargarPreferenciaEjecutarAlInicio());
            ejecutarAlInicioItemAWT.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    boolean seleccionado = ejecutarAlInicioItemAWT.getState();
                    guardarPreferenciaEjecutarAlInicio(seleccionado);
                    
                    if (seleccionado) {
                        if (registrarEnInicioSistema()) {
                            System.out.println("INFO: DeskAgenda registrada para ejecutar al inicio del sistema.");
                        } else {
                            System.err.println("ERROR: No se pudo registrar DeskAgenda en el inicio del sistema.");
                            ejecutarAlInicioItemAWT.setState(false);
                            guardarPreferenciaEjecutarAlInicio(false);
                        }
                    } else {
                        if (desregistrarDeInicioSistema()) {
                            System.out.println("INFO: DeskAgenda desregistrada del inicio del sistema.");
                        } else {
                            System.err.println("ERROR: No se pudo desregistrar DeskAgenda del inicio del sistema.");
                        }
                    }
                }
            });
            popup.add(ejecutarAlInicioItemAWT);
            popup.addSeparator(); // Separador antes de Salir

            MenuItem salirItem = new MenuItem("Salir");
            salirItem.addActionListener(e -> {
                // Liberar bloqueo de instancia única antes de salir
                InstanciaUnica.liberarBloqueo();
                System.exit(0);
            });
            popup.add(salirItem);

            trayIcon = new TrayIcon(image, "Agenda de Actividades", popup);
            trayIcon.setImageAutoSize(true);
            trayIcon.addActionListener(e -> vistaPrincipal.mostrarVentana()); // Double click shows window

            trayIcon.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (SwingUtilities.isRightMouseButton(e)) {

                    }
                }
            });

            try {
                tray.add(trayIcon);
            } catch (AWTException e) {
                System.err.println("No se pudo agregar el ícono a la bandeja del sistema: " + e.getMessage());
            }
        } else {
            System.err.println("La bandeja del sistema no es soportada.");
        }
    }

    // Métodos para guardar y cargar la preferencia de "ejecutar al inicio"
    private void guardarPreferenciaEjecutarAlInicio(boolean valor) {
        prefs.putBoolean(EJECUTAR_AL_INICIO_PREF_KEY, valor);
        try {
            prefs.flush(); // Asegurar que se guarde
        } catch (java.util.prefs.BackingStoreException e) {
            System.err.println("Error al guardar la preferencia 'ejecutarAlInicio': " + e.getMessage());
        }
    }

    private boolean cargarPreferenciaEjecutarAlInicio() {
        return prefs.getBoolean(EJECUTAR_AL_INICIO_PREF_KEY, false); // Valor por defecto: false
    }

    public void minimizarABandeja() {
        if (vistaPrincipal != null) {
            vistaPrincipal.setVisible(false);
            if (trayIcon != null) {
                boolean hayPendientes = false;
                List<Tarea> tareas = gestorTareas.getTareas();
                for (Tarea tarea : tareas) {
                    if (!tarea.isCompletada()) {
                        hayPendientes = true;
                        break;
                    }
                }
                if (hayPendientes) {
                    trayIcon.displayMessage("Agenda Minimizada", "Tienes tareas pendientes.", TrayIcon.MessageType.INFO);
                } else {
                    trayIcon.displayMessage("Agenda Minimizada", "La agenda está en la bandeja del sistema.", TrayIcon.MessageType.INFO);
                }
            }
        }
    }
    
    private boolean registrarEnInicioSistema() {
        try {
            String osName = System.getProperty("os.name").toLowerCase();
            
            if (osName.contains("windows")) {
                return registrarEnInicioWindows();
            } else if (osName.contains("linux")) {
                return registrarEnInicioLinux();
            } else if (osName.contains("mac")) {
                return registrarEnInicioMac();
            } else {
                System.err.println("Sistema operativo no soportado para auto-inicio: " + osName);
                return false;
            }
        } catch (Exception e) {
            System.err.println("Error al registrar en inicio del sistema: " + e.getMessage());
            return false;
        }
    }
    
    private boolean desregistrarDeInicioSistema() {
        try {
            String osName = System.getProperty("os.name").toLowerCase();
            
            if (osName.contains("windows")) {
                return desregistrarDeInicioWindows();
            } else if (osName.contains("linux")) {
                return desregistrarDeInicioLinux();
            } else if (osName.contains("mac")) {
                return desregistrarDeInicioMac();
            } else {
                System.err.println("Sistema operativo no soportado para auto-inicio: " + osName);
                return false;
            }
        } catch (Exception e) {
            System.err.println("Error al desregistrar del inicio del sistema: " + e.getMessage());
            return false;
        }
    }    private boolean registrarEnInicioWindows() {
        try {
            String rutaEjecutable = obtenerRutaEjecutable();
            System.out.println("DEBUG: Ruta ejecutable detectada: " + rutaEjecutable);
            
            // Usar ProcessBuilder para manejar mejor las comillas y espacios
            ProcessBuilder pb = new ProcessBuilder(
                "reg", "add", 
                "HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Run",
                "/v", "DeskAgenda",
                "/t", "REG_SZ", 
                "/d", rutaEjecutable,
                "/f"
            );
            
            System.out.println("DEBUG: Comando ProcessBuilder: " + String.join(" ", pb.command()));
            
            Process proceso = pb.start();
            int exitCode = proceso.waitFor();
            
            // Imprimir el resultado del comando para debug
            if (exitCode == 0) {
                System.out.println("DEBUG: Comando reg add exitoso");
            } else {
                System.out.println("DEBUG: Comando reg add falló con código: " + exitCode);
                // Leer error del proceso
                java.io.BufferedReader errorReader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(proceso.getErrorStream())
                );
                String errorLine;
                while ((errorLine = errorReader.readLine()) != null) {
                    System.out.println("DEBUG: Error reg: " + errorLine);
                }
            }
            
            return exitCode == 0;
        } catch (Exception e) {
            System.err.println("Error al registrar en Windows: " + e.getMessage());
            return false;
        }
    }private boolean desregistrarDeInicioWindows() {
        try {
            // Usar ProcessBuilder para mejor manejo de argumentos
            ProcessBuilder pb = new ProcessBuilder(
                "reg", "delete",
                "HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Run", 
                "/v", "DeskAgenda",
                "/f"
            );
            
            System.out.println("DEBUG: Comando desregistrar ProcessBuilder: " + String.join(" ", pb.command()));
            
            Process proceso = pb.start();
            int exitCode = proceso.waitFor();
            
            return exitCode == 0;
        } catch (Exception e) {
            System.err.println("Error al desregistrar de Windows: " + e.getMessage());
            return false;
        }
    }
    
    private boolean registrarEnInicioLinux() {
        try {
            String homeDir = System.getProperty("user.home");
            String autostartDir = homeDir + "/.config/autostart";
            String desktopFile = autostartDir + "/DeskAgenda.desktop";
            
            new File(autostartDir).mkdirs();
            
            String rutaEjecutable = obtenerRutaEjecutable();
            String contenido = "[Desktop Entry]\n" +
                    "Type=Application\n" +
                    "Name=DeskAgenda\n" +
                    "Exec=" + rutaEjecutable + "\n" +
                    "Hidden=false\n" +
                    "NoDisplay=false\n" +
                    "X-GNOME-Autostart-enabled=true\n";
            
            java.nio.file.Files.write(java.nio.file.Paths.get(desktopFile), contenido.getBytes());
            return true;
        } catch (Exception e) {
            System.err.println("Error al registrar en Linux: " + e.getMessage());
            return false;
        }
    }
    
    private boolean desregistrarDeInicioLinux() {
        try {
            String homeDir = System.getProperty("user.home");
            String desktopFile = homeDir + "/.config/autostart/DeskAgenda.desktop";
            
            File archivo = new File(desktopFile);
            if (archivo.exists()) {
                return archivo.delete();
            }
            return true;
        } catch (Exception e) {
            System.err.println("Error al desregistrar de Linux: " + e.getMessage());
            return false;
        }
    }
    
    private boolean registrarEnInicioMac() {
        try {
            String homeDir = System.getProperty("user.home");
            String launchAgentsDir = homeDir + "/Library/LaunchAgents";
            String plistFile = launchAgentsDir + "/com.deskagenda.startup.plist";
            
            new File(launchAgentsDir).mkdirs();
            
            String rutaEjecutable = obtenerRutaEjecutable();
            String contenido = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<!DOCTYPE plist PUBLIC \"-//Apple//DTD PLIST 1.0//EN\" \"http://www.apple.com/DTDs/PropertyList-1.0.dtd\">\n" +
                    "<plist version=\"1.0\">\n" +
                    "<dict>\n" +
                    "    <key>Label</key>\n" +
                    "    <string>com.deskagenda.startup</string>\n" +
                    "    <key>ProgramArguments</key>\n" +
                    "    <array>\n" +
                    "        <string>" + rutaEjecutable + "</string>\n" +
                    "    </array>\n" +
                    "    <key>RunAtLoad</key>\n" +
                    "    <true/>\n" +
                    "</dict>\n" +
                    "</plist>\n";
            
            java.nio.file.Files.write(java.nio.file.Paths.get(plistFile), contenido.getBytes());
            return true;
        } catch (Exception e) {
            System.err.println("Error al registrar en Mac: " + e.getMessage());
            return false;
        }
    }
    
    private boolean desregistrarDeInicioMac() {
        try {
            String homeDir = System.getProperty("user.home");
            String plistFile = homeDir + "/Library/LaunchAgents/com.deskagenda.startup.plist";
            
            File archivo = new File(plistFile);
            if (archivo.exists()) {
                return archivo.delete();
            }
            return true;
        } catch (Exception e) {
            System.err.println("Error al desregistrar de Mac: " + e.getMessage());
            return false;
        }
    }      private String obtenerRutaEjecutable() {
        try {
            String directorioActual = System.getProperty("user.dir");
            System.out.println("DEBUG: Directorio actual: " + directorioActual);
            
            // 1. Verificar si hay un archivo VBS (versión standalone sin consola) - PRIORIDAD
            File vbsFile = new File(directorioActual, "DeskAgenda.vbs");
            System.out.println("DEBUG: Buscando VBS en: " + vbsFile.getAbsolutePath() + " - Existe: " + vbsFile.exists());
            if (vbsFile.exists()) {
                // Para el registro de Windows, usamos la ruta del VBS directamente
                // Windows automáticamente usará wscript para archivos .vbs
                String rutaVbs = vbsFile.getAbsolutePath();
                System.out.println("DEBUG: Usando VBS (para registro): " + rutaVbs);
                return rutaVbs;
            }
            
            // 2. Verificar si hay un archivo VBS para portable sin consola
            File vbsPortableFile = new File(directorioActual, "DeskAgenda-SinConsola.vbs");
            System.out.println("DEBUG: Buscando VBS portable en: " + vbsPortableFile.getAbsolutePath() + " - Existe: " + vbsPortableFile.exists());
            if (vbsPortableFile.exists()) {
                String rutaVbsPortable = vbsPortableFile.getAbsolutePath();
                System.out.println("DEBUG: Usando VBS portable (sin consola): " + rutaVbsPortable);
                return rutaVbsPortable;
            }
            
            // 3. Verificar si hay un archivo .bat (versión standalone)
            File batFile = new File(directorioActual, "DeskAgenda.bat");
            System.out.println("DEBUG: Buscando BAT en: " + batFile.getAbsolutePath() + " - Existe: " + batFile.exists());
            if (batFile.exists()) {
                String rutaBat = batFile.getAbsolutePath();
                System.out.println("DEBUG: Usando BAT: " + rutaBat);
                return rutaBat;
            }
            
            // 4. Verificar si hay un archivo .bat con nombre específico (versión portable)
            File batPortableFile = new File(directorioActual, "Ejecutar-DeskAgenda.bat");
            System.out.println("DEBUG: Buscando BAT portable en: " + batPortableFile.getAbsolutePath() + " - Existe: " + batPortableFile.exists());
            if (batPortableFile.exists()) {
                String rutaBatPortable = batPortableFile.getAbsolutePath();
                System.out.println("DEBUG: Usando BAT portable: " + rutaBatPortable);
                return rutaBatPortable;
            }
            
            // 5. Verificar si hay un ejecutable .exe directo
            File exeFile = new File(directorioActual, "DeskAgenda.exe");
            System.out.println("DEBUG: Buscando EXE en: " + exeFile.getAbsolutePath() + " - Existe: " + exeFile.exists());
            if (exeFile.exists()) {
                String rutaExe = exeFile.getAbsolutePath();
                System.out.println("DEBUG: Usando EXE: " + rutaExe);
                return rutaExe;
            }
            
            // 6. Para desarrollo o fallback, usar Java directo
            String javaHome = System.getProperty("java.home");
            String javaBin = javaHome + File.separator + "bin" + File.separator + "java";
            String classpath = System.getProperty("java.class.path");
            String rutaDesarrollo = "\"" + javaBin + "\" -cp \"" + classpath + "\" logica.AgendaAct";
            System.out.println("DEBUG: Usando desarrollo: " + rutaDesarrollo);
            return rutaDesarrollo;
            
        } catch (Exception e) {
            System.err.println("Error al obtener ruta del ejecutable: " + e.getMessage());
            return "java -cp . logica.AgendaAct";
        }
    }
    
}