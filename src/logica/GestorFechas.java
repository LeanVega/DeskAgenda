package logica;

import java.time.LocalDate; // Para manejar fechas sin hora
import java.time.LocalDateTime; // Para fechas con hora específica
import java.time.format.DateTimeFormatter; // Para formatear fechas como texto
import java.time.format.TextStyle; // Para obtener nombres de días en formato completo
import java.util.List; // Para trabajar con listas de tareas
import java.util.Locale; // Para localización en español
import javax.swing.Timer; // Timer para verificaciones periódicas
import java.awt.event.ActionEvent; // Eventos del timer
import java.awt.event.ActionListener; // Listener para eventos del timer

public class GestorFechas {
    private GestorTareas gestorTareas;    private GestorSonido gestorSonido;
    private Timer timerVerificacion;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public GestorFechas(GestorTareas gestorTareas, GestorSonido gestorSonido) {        this.gestorTareas = gestorTareas;
        this.gestorSonido = gestorSonido;
        iniciarTimerVerificacionInterno();
    }

    public String formatearFecha(LocalDate fecha) {
        if (fecha == null) return "";
        return fecha.format(DATE_FORMATTER);
    }

    public String getDiaSemana(LocalDate fecha) {
        if (fecha == null) return "";        return fecha.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.getDefault());
    }

    private void iniciarTimerVerificacionInterno() {
        if (timerVerificacion == null) {
            timerVerificacion = new Timer(30000, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    verificarTareasProximasYNotificar();
                }
            });            timerVerificacion.start();
        }
    }

    public void verificarTareasProximasYNotificar() {
        System.out.println("Verificando tareas próximas para notificación...");
        List<Tarea> tareas = gestorTareas.getTareas();
        LocalDateTime ahora = LocalDateTime.now();

        for (Tarea tarea : tareas) {
            if (!tarea.isCompletada() && tarea.isAlertaActiva()) {
                LocalDateTime fechaHoraTarea = LocalDateTime.of(tarea.getFecha(), tarea.getHora());
                // Usar la configuración real de segundos antes de alerta de la tarea
                int segundosAntes = tarea.getSegundosAntesAlerta();
                LocalDateTime tiempoAlerta = fechaHoraTarea.minusSeconds(segundosAntes);
                
                // Verificar si es tiempo de alertar
                if (ahora.isAfter(tiempoAlerta) && ahora.isBefore(fechaHoraTarea.plusMinutes(1))) {
                    int minutos = segundosAntes / 60;
                    int segundos = segundosAntes % 60;
                    String tiempoTexto = minutos > 0 ? minutos + " min " + segundos + " seg" : segundos + " seg";
                    System.out.println("¡Alerta! Tarea próxima: " + tarea.getNombre() + " a las " + tarea.getHoraTexto() + " (alertando " + tiempoTexto + " antes)");
                    if (gestorSonido != null) {
                        gestorSonido.reproducirSonido();
                    }
                    tarea.setAlertaActiva(false);
                    gestorTareas.guardarTareas();
                }
            }
        }    }
    
    public void detenerTimer() {
        if (timerVerificacion != null && timerVerificacion.isRunning()) {
            timerVerificacion.stop();
        }
    }
}