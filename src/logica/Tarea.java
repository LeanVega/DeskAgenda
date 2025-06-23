/*
 * DeskAgenda
 * 
 * Esta clase representa una tarea individual con todas sus características.
 * Es la estructura básica que contiene toda la información de una actividad.
 */
package logica;

// === IMPORTS DE FECHA Y HORA ===
import java.time.LocalDate;            // Para manejar fechas (día/mes/año)
import java.time.LocalTime;            // Para manejar horas (hora:minuto)
import java.time.format.DateTimeFormatter; // Para formatear fechas en formato argentino
import java.time.DayOfWeek;            // Para días de la semana (lunes, martes, etc.)

// === IMPORTS DE COLECCIONES ===
import java.util.Set;                  // Para guardar conjunto de días (tareas semanales)

/**
 * CLASE TAREA - ESTRUCTURA DE DATOS PRINCIPAL
 * ==========================================
 * 
 * Esta clase representa UNA TAREA individual. Contiene toda la información
 * necesaria para gestionar una actividad:
 * 
 * INFORMACIÓN BÁSICA:
 * - Nombre de la tarea (ej: "Llamar al médico")
 * - Fecha y hora (cuándo hay que hacerla)
 * - Si está completada o no
 * 
 * TIPOS DE TAREA:
 * - ÚNICA: Se hace una sola vez
 * - DIARIA: Se repite todos los días
 * - SEMANAL: Se repite ciertos días de la semana
 * 
 * SISTEMA DE ALERTAS:
 * - Alertas configurables (X minutos antes)
 * - Control de cuándo activar notificaciones
 * 
 * REPETICIONES:
 * - Contador de cuántas veces se completó
 * - Control de fechas para evitar duplicados
 */
public class Tarea {
    
    // === INFORMACIÓN BÁSICA DE LA TAREA ===
    private String nombre;                    // Descripción de la tarea
    private LocalDate fecha;                  // Fecha cuando hay que hacerla
    private LocalTime hora;                   // Hora cuando hay que hacerla
    private TipoTarea tipo;                   // Tipo: UNICA, DIARIA, SEMANAL
    private boolean completada;               // Si ya se completó o no
    
    // === SISTEMA DE REPETICIONES ===
    private int repeticionesCompletadas;      // Cuántas veces se hizo (para DIARIA/SEMANAL)
    private LocalDate fechaUltimaCompletada;  // Cuándo se completó por última vez
    private Set<DayOfWeek> diasSemana;        // Qué días se repite (para SEMANAL)
    
    // === SISTEMA DE ALERTAS ===
    private boolean alertaActiva;             // Si debe mostrar alertas
    private int minutosAntesAlerta;           // Cuántos minutos antes avisar

    // === FORMATEADORES PARA MOSTRAR FECHAS EN FORMATO ESPAÑOL ===
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * TIPOS DE TAREA DISPONIBLES
     * =========================
     * 
     * Enum que define los diferentes tipos de tareas que puede manejar DeskAgenda.
     * Cada tipo tiene comportamientos específicos para repeticiones y alertas.
     */
    public enum TipoTarea {        // Tarea que se hace solo una vez (ej: "Ir al médico el viernes")
        DIARIA {
            @Override
            public String toString() {
                return "Diaria";
            }
        },
        // Tarea que se repite ciertos días de la semana (ej: "Gimnasio los lunes y viernes")
        SEMANAL {
            @Override
            public String toString() {
                return "Semanal";
            }
        },
        // Tarea que se hace todos los días (ej: "Tomar medicamento")
        UNICA {
            @Override
            public String toString() {
                return "Unica";
            }
        };
    }

    public Tarea(String descripcion, LocalDate fecha, LocalTime hora, TipoTarea tipo) { 
        this.nombre = descripcion;
        this.fecha = fecha;
        this.hora = hora;
        this.tipo = tipo;
        this.completada = false;
        this.alertaActiva = false;
        this.minutosAntesAlerta = 0;
        this.diasSemana = Set.of();
        this.repeticionesCompletadas = 0;
    }

    // Getters
    public String getDescripcion() { return nombre; }
    public String getNombre() { return nombre; } 
    public LocalDate getFecha() { return fecha; }
    public LocalTime getHora() { return hora; }
    public TipoTarea getTipo() { return tipo; }
    public boolean isCompletada() { return completada; }
    public boolean isAlertaActiva() { return alertaActiva; }
    public int getMinutosAntesAlerta() { return minutosAntesAlerta; }
    public Set<DayOfWeek> getDiasSemana() { return diasSemana; }
    public int getRepeticionesCompletadas() { return repeticionesCompletadas; } 
    public LocalDate getFechaUltimaCompletada() { return fechaUltimaCompletada; }

    // Setters
    public void setDescripcion(String descripcion) { this.nombre = descripcion; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public void setHora(LocalTime hora) { this.hora = hora; }
    public void setTipo(TipoTarea tipo) { this.tipo = tipo; }
    public void setCompletada(boolean completada) { this.completada = completada; }
    public void setAlertaActiva(boolean alertaActiva) { this.alertaActiva = alertaActiva; }
    public void setMinutosAntesAlerta(int minutosAntesAlerta) { this.minutosAntesAlerta = minutosAntesAlerta; }
    public void setDiasSemana(Set<DayOfWeek> diasSemana) { this.diasSemana = diasSemana; }
    public void setRepeticionesCompletadas(int repeticionesCompletadas) { this.repeticionesCompletadas = repeticionesCompletadas; }
    public void setFechaUltimaCompletada(LocalDate fechaUltimaCompletada) { this.fechaUltimaCompletada = fechaUltimaCompletada; }


    // Metodos para VistaPrincipal
    public void marcarPendiente() {
        boolean estabaCompletadaHoy = this.completada && this.fechaUltimaCompletada != null && this.fechaUltimaCompletada.isEqual(LocalDate.now());
        this.completada = false;
        if (tipo == TipoTarea.DIARIA || tipo == TipoTarea.SEMANAL) {
            if (estabaCompletadaHoy) {
                if (this.repeticionesCompletadas > 0) { // Evitar que sea negativo
                    this.repeticionesCompletadas--;
                }
                // No limpiar fechaUltimaCompletada aquí,
                // si se vuelve a marcar completada el mismo día, no debe contar dos veces.
                // Se limpiará/reseteará al cambiar de día o si la tarea se reinicia.
            }
        }
    }

    public void marcarCompletada() {
        if (!this.completada) { // Solo actuar si no estaba ya completada
            this.completada = true;
            if (tipo == TipoTarea.DIARIA || tipo == TipoTarea.SEMANAL) {
                LocalDate hoy = LocalDate.now();
                if (this.fechaUltimaCompletada == null || !this.fechaUltimaCompletada.isEqual(hoy)) {
                    this.repeticionesCompletadas++;
                    this.fechaUltimaCompletada = hoy;
                }
                
                // Para tareas semanales, si se completa hoy y hay más días en la semana, 
                // programar inmediatamente para el próximo día correspondiente
                if (tipo == TipoTarea.SEMANAL && diasSemana != null && diasSemana.size() > 1) {
                    LocalDate proximaFecha = calcularProximaFechaSemanalSinIncluirHoy(hoy);
                    if (!proximaFecha.equals(hoy)) {
                        // Crear una nueva instancia para el próximo día
                        this.completada = false;
                        this.fecha = proximaFecha;
                        this.fechaUltimaCompletada = hoy; // Mantener registro de que se completó hoy
                    }
                }
            }
        }
    }
    
    /**
     * Calcula la próxima fecha semanal excluyendo el día actual
     */
    private LocalDate calcularProximaFechaSemanalSinIncluirHoy(LocalDate fechaInicio) {
        if (diasSemana == null || diasSemana.isEmpty()) {
            return fechaInicio.plusDays(7); // Próxima semana el mismo día
        }
        
        // Buscar el próximo día que coincida con los días configurados (empezando desde mañana)
        for (int i = 1; i <= 7; i++) {
            LocalDate fechaCandidata = fechaInicio.plusDays(i);
            if (diasSemana.contains(fechaCandidata.getDayOfWeek())) {
                return fechaCandidata;
            }
        }
        
        // Si no se encuentra, devolver la próxima semana el mismo día
        return fechaInicio.plusDays(7);
    }

    public void reiniciarSiNecesario() {
        LocalDate hoy = LocalDate.now();
        if (this.completada && (this.tipo == TipoTarea.DIARIA || this.tipo == TipoTarea.SEMANAL)) {
            // Si la última vez que se completó fue antes de hoy, resetear y actualizar fecha
            if (fechaUltimaCompletada != null && fechaUltimaCompletada.isBefore(hoy)) {
                this.completada = false;
                this.fechaUltimaCompletada = null;
                
                // Actualizar la fecha a la próxima ocurrencia
                if (this.tipo == TipoTarea.DIARIA) {
                    // Para tareas diarias, la próxima ocurrencia es hoy
                    this.fecha = hoy;
                } else if (this.tipo == TipoTarea.SEMANAL) {
                    // Para tareas semanales, calcular el próximo día que coincida
                    this.fecha = calcularProximaFechaSemanal(hoy);
                }
            }
        } else if (!this.completada && (this.tipo == TipoTarea.DIARIA || this.tipo == TipoTarea.SEMANAL)) {
            // Para tareas pendientes que ya pasaron su fecha, actualizar a la próxima ocurrencia
            if (this.fecha != null && this.fecha.isBefore(hoy)) {
                if (this.tipo == TipoTarea.DIARIA) {
                    this.fecha = hoy;
                } else if (this.tipo == TipoTarea.SEMANAL) {
                    this.fecha = calcularProximaFechaSemanal(hoy);
                }
            }
        }
    }
    
    /**
     * Calcula la próxima fecha para una tarea semanal basada en los días configurados
     */
    private LocalDate calcularProximaFechaSemanal(LocalDate fechaInicio) {
        if (diasSemana == null || diasSemana.isEmpty()) {
            // Si no hay días específicos configurados, usar la fecha actual
            return fechaInicio;
        }
        
        LocalDate fechaActual = fechaInicio;
        
        // Primero verificar si hoy es uno de los días programados
        if (diasSemana.contains(fechaActual.getDayOfWeek())) {
            return fechaActual;
        }
        
        // Buscar el próximo día que coincida con los días configurados
        for (int i = 1; i <= 7; i++) {
            fechaActual = fechaInicio.plusDays(i);
            if (diasSemana.contains(fechaActual.getDayOfWeek())) {
                return fechaActual;
            }
        }
        
        // Si no se encuentra en los próximos 7 días, devolver la fecha de inicio
        return fechaInicio;
    }
    
    public String getFechaTexto() {
        return fecha != null ? fecha.format(DATE_FORMATTER) : "";
    }

    public String getHoraTexto() {
        return hora != null ? hora.format(TIME_FORMATTER) : "";
    }

    public String getEstadoTexto() {
        
        return completada ? "✓" : "Pendiente"; // Using checkmark for completed
    }

    public String getRepeticiones() {
        // Devuelve el conteo actual para Diaria/Semanal, o "N/A" para Única.
        if (tipo == TipoTarea.DIARIA || tipo == TipoTarea.SEMANAL) {
            return String.valueOf(repeticionesCompletadas);
        }
        return "N/A"; // O cadena vacía, o texto específico para Única
    }

    @Override
    public String toString() {
        return nombre + " - " + fecha + " " + hora + " (" + tipo + ")";
    }

    // Hay que implementar equals() y hashCode() si las tareas se buscan en colecciones
    // o se comparan después de ser recuperadas/recreadas, especialmente si no son la misma instancia de objeto.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tarea tarea = (Tarea) o;
        // Comparar por un conjunto de campos que definan unívocamente una tarea.
        // Si hay un ID único, usarlo sería lo ideal.
        // Por ahora, comparamos por nombre, fecha, hora y tipo.
        // Esto podría no ser suficiente si se permiten tareas con el mismo nombre, fecha, hora y tipo (posible pendiente)
        // pero que se consideran distintas (ej. creadas en momentos diferentes).
        return nombre.equals(tarea.nombre) &&
               fecha.equals(tarea.fecha) &&
               hora.equals(tarea.hora) &&
               tipo == tarea.tipo; 
               // Considerar también otros campos si son relevantes para la unicidad,
               // como alertaActiva, minutosAntesAlerta, diasSemana, etc.
               // O, si la identidad del objeto es lo que importa, esta implementación de equals
               // podría ser demasiado permisiva.
    }

    @Override
    public int hashCode() {
        // Generar hashCode basado en los mismos campos usados en equals().
        int result = nombre.hashCode();
        result = 31 * result + fecha.hashCode();
        result = 31 * result + hora.hashCode();
        result = 31 * result + tipo.hashCode();
        return result;
        // return java.util.Objects.hash(nombre, fecha, hora, tipo); // Alternativa más concisa
    }
}