package logica;

import igu.VistaPrincipal;
import javax.swing.SwingUtilities;

public class AgendaAct {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new VistaPrincipal().setVisible(true));
    }
    
}
