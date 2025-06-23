/*
 * DeskAgenda - Sistema de Instancia Única
 * 
 * Esta clase se encarga de asegurar que solo haya una instancia de DeskAgenda
 * ejecutándose al mismo tiempo. Si se intenta abrir otra instancia, se enfoca
 * la ventana existente en lugar de crear una nueva.
 */
package logica;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.InetAddress;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 * SISTEMA DE INSTANCIA ÚNICA
 * ===========================
 * 
 * Utiliza un archivo de bloqueo (lock file) para detectar si ya hay una
 * instancia de la aplicación ejecutándose y un socket para comunicación
 * entre instancias.
 */
public class InstanciaUnica {
    
    private static final String LOCK_FILE_NAME = ".deskagenda.lock";
    private static final int PUERTO_COMUNICACION = 52847; // Puerto específico para DeskAgenda
    private static File lockFile;
    private static RandomAccessFile randomAccessFile;
    private static FileChannel fileChannel;
    private static FileLock fileLock;
    private static ServerSocket serverSocket;
    private static JFrame ventanaPrincipal;
    
    /**
     * Verifica si esta es la primera instancia de la aplicación.
     * Si no es la primera, intenta activar la ventana existente.
     * 
     * @return true si es la primera instancia, false si ya hay otra ejecutándose
     */
    public static boolean esPrimeraInstancia() {
        try {
            // Crear el archivo de bloqueo en el directorio del usuario
            String userHome = System.getProperty("user.home");
            lockFile = new File(userHome, LOCK_FILE_NAME);
            
            // Intentar crear y bloquear el archivo
            randomAccessFile = new RandomAccessFile(lockFile, "rw");
            fileChannel = randomAccessFile.getChannel();
            fileLock = fileChannel.tryLock();
            
            if (fileLock == null) {
                // No se pudo obtener el bloqueo, ya hay otra instancia
                cerrarRecursos();
                // Intentar comunicarse con la instancia existente
                enviarSeñalActivacion();
                return false;
            }
            
            // Se obtuvo el bloqueo exitosamente, esta es la primera instancia
            // Configurar el servidor para escuchar señales de activación
            configurarServidorActivacion();
            return true;
            
        } catch (Exception e) {
            System.err.println("Error al verificar instancia única: " + e.getMessage());
            cerrarRecursos();
            return true; // En caso de error, permitir que se ejecute
        }
    }
    
    /**
     * Configura la ventana principal que debe ser activada cuando llegue una señal
     */
    public static void configurarVentanaPrincipal(JFrame ventana) {
        ventanaPrincipal = ventana;
    }
    
    /**
     * Configura un servidor para escuchar señales de activación de otras instancias
     */
    private static void configurarServidorActivacion() {
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(PUERTO_COMUNICACION, 1, InetAddress.getLoopbackAddress());
                while (!serverSocket.isClosed()) {
                    Socket clientSocket = serverSocket.accept();
                    // Se recibió una señal de activación
                    activarVentanaPrincipal();
                    clientSocket.close();
                }
            } catch (IOException e) {
                // Socket cerrado normalmente o error
                if (!serverSocket.isClosed()) {
                    System.err.println("Error en servidor de activación: " + e.getMessage());
                }
            }
        }).start();
    }
    
    /**
     * Envía una señal a la instancia existente para que se active
     */
    private static void enviarSeñalActivacion() {
        try (Socket socket = new Socket(InetAddress.getLoopbackAddress(), PUERTO_COMUNICACION)) {
            // Solo necesitamos conectarnos para enviar la señal
            socket.getOutputStream().write(1);
        } catch (IOException e) {
            System.err.println("No se pudo comunicar con la instancia existente: " + e.getMessage());
        }
    }
    
    /**
     * Activa y trae al frente la ventana principal
     */
    private static void activarVentanaPrincipal() {
        if (ventanaPrincipal != null) {
            SwingUtilities.invokeLater(() -> {
                // Restaurar la ventana si está minimizada
                if (ventanaPrincipal.getState() == JFrame.ICONIFIED) {
                    ventanaPrincipal.setState(JFrame.NORMAL);
                }
                
                // Traer la ventana al frente
                ventanaPrincipal.setVisible(true);
                ventanaPrincipal.toFront();
                ventanaPrincipal.requestFocus();
                
                // En Windows, a veces es necesario hacer esto para forzar el foco
                if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                    ventanaPrincipal.setAlwaysOnTop(true);
                    ventanaPrincipal.setAlwaysOnTop(false);
                }
            });
        }
    }
    
    /**
     * Cierra todos los recursos abiertos de manera segura
     */
    private static void cerrarRecursos() {
        try {
            if (fileChannel != null && fileChannel.isOpen()) {
                fileChannel.close();
            }
            if (randomAccessFile != null) {
                randomAccessFile.close();
            }
        } catch (IOException e) {
            System.err.println("Error al cerrar recursos: " + e.getMessage());
        }
    }      /**
     * Libera el bloqueo del archivo cuando la aplicación se cierra.
     * Este método tiene que llamarse al cerrar la aplicación.
     */
    public static void liberarBloqueo() {
        try {
            if (fileLock != null && fileLock.isValid()) {
                fileLock.release();
            }
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            cerrarRecursos();
            if (lockFile != null && lockFile.exists()) {
                lockFile.delete();
            }
        } catch (Exception e) {
            System.err.println("Error al liberar bloqueo: " + e.getMessage());
        }
    }
    
    /**
     * Configura un shutdown hook para liberar automáticamente el bloqueo
     * cuando la aplicación se cierre de cualquier manera.
     */
    public static void configurarLiberacionAutomatica() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            liberarBloqueo();
        }));
    }
    
    /**
     * Configura un listener para liberar el bloqueo cuando se cierre la ventana principal.
     * 
     * @param ventanaPrincipal La ventana principal de la aplicación
     */
    public static void configurarLiberacionAlCerrar(java.awt.Window ventanaPrincipal) {
        ventanaPrincipal.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                liberarBloqueo();
            }
        });
    }
}
