package logica;

import igu.VistaPrincipal;
import javax.swing.SwingUtilities;

public class AgendaAct {

    public static void main(String[] args) {
        // Verificar si ya hay una instancia ejecutándose
        if (!InstanciaUnica.esPrimeraInstancia()) {
            // Ya hay una instancia ejecutándose, la señal para activarla ya se envió
            System.out.println("DeskAgenda ya está ejecutándose. Activando ventana existente...");
            System.exit(0);
            return;
        }
        
        // Esta es la primera instancia, configurar liberación automática del bloqueo
        InstanciaUnica.configurarLiberacionAutomatica();
        
        SwingUtilities.invokeLater(() -> {
            VistaPrincipal ventana = new VistaPrincipal();
            
            // Configurar la ventana para el sistema de instancia única
            InstanciaUnica.configurarVentanaPrincipal(ventana);
            InstanciaUnica.configurarLiberacionAlCerrar(ventana);
            
            ventana.setVisible(true);
        });
    }
    
}
