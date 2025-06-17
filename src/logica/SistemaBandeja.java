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
                    // Acá iría la lógica para registrar/desregistrar la app del inicio del sistema
                    // Por ahora, solo muestra un mensaje y guarda la preferencia
                    if (seleccionado) {
                        System.out.println("INFO: Opción 'Ejecutar al inicio' seleccionada (acción de registro no implementada).");
                        // TODO: Implementar lógica de registro para el inicio del sistema operativo
                    } else {
                        System.out.println("INFO: Opción 'Ejecutar al inicio' deseleccionada (acción de desregistro no implementada).");
                        // TODO: Implementar lógica de desregistro del inicio del sistema operativo
                    }
                }
            });
            popup.add(ejecutarAlInicioItemAWT);
            popup.addSeparator(); // Separador antes de Salir

            MenuItem salirItem = new MenuItem("Salir");
            salirItem.addActionListener(e -> System.exit(0));
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
    
}