/*
 * DeskAgenda - Gestión de Tareas
 * 
 * Este archivo contiene toda la lógica para manejar las tareas.
 * Coordina el guardado, carga y organización de tareas.
 */
package logica;

// === IMPORTS DE PERSISTENCIA ===
import persistencia.IRepositorioTareas;  // Interfaz para guardar/cargar tareas
import persistencia.RepositorioJSON;     // Implementación que guarda en JSON

// === IMPORTS DE FECHA Y HORA ===
import java.time.LocalDate;             // Para manejar fechas (día/mes/año)
import java.time.LocalDateTime;          // Para manejar fecha y hora completa

// === IMPORTS DE COLECCIONES ===
import java.util.ArrayList;             // Lista dinámica para guardar tareas
import java.util.List;                  // Interfaz de lista
import java.util.Comparator;            // Para ordenar tareas por fecha

// === IMPORTS DEL SISTEMA ===
import java.io.File;                    // Para verificar si existen archivos

/**
 * GESTOR DE TAREAS - CEREBRO DE LA APLICACIÓN
 * ==========================================
 * 
 * Esta clase es la más importante de DeskAgenda. Se encarga de:
 * 
 * 1. GUARDAR Y CARGAR: Mantiene todas las tareas en memoria y las guarda en archivo
 * 2. ORGANIZAR: Ordena las tareas por fecha para mostrarlas correctamente
 * 3. GESTIONAR: Agregar, eliminar, completar tareas
 * 4. BACKUP AUTOMÁTICO: Cada vez que hay un cambio, crea respaldos de seguridad
 * 
 * ¿POR QUÉ ES IMPORTANTE?
 * - Separa la lógica de la interfaz
 * - Asegura que los datos no se pierdan
 * - Mantiene todo un poquiiiito más organizado
 */
public class GestorTareas {
    
    // === DATOS EN MEMORIA ===
    private List<Tarea> tareas;              // Lista de todas las tareas en memoria
    private IRepositorioTareas repositorio;  // Encargado de guardar/cargar desde archivo

    /**
     * CONSTRUCTOR - Se ejecuta al crear un GestorTareas
     * ================================================
     * 
     * Inicializa la lista de tareas y carga las tareas existentes desde el archivo.
     * Usa ArrayList con capacidad inicial pequeña para ahorrar memoria.
     */public GestorTareas() {
        // Usar ArrayList con capacidad inicial para evitar redimensionamientos
        this.tareas = new ArrayList<>(10); // Capacidad inicial pequeña
        this.repositorio = new RepositorioJSON();
        cargarTareas();
    }
    
    /**
     * Agrega una nueva tarea a la lista y la guarda en el archivo.
     * Si la tarea es null, no hace nada para evitar errores.
     */
    public void agregarTarea(Tarea tarea) {
        if (tarea != null) {
            tareas.add(tarea);
            guardarTareas();
        }
    }
    
    /**
     * Devuelve una copia de la lista de tareas para que no se pueda modificar directamente.
     */
    public List<Tarea> getTareas() {
        return new ArrayList<>(tareas);
    }
    
    /**
     * Devuelve las tareas ordenadas por fecha, hora y nombre.
     * Útil para mostrar las tareas en orden cronológico.
     */
    public List<Tarea> getTareasOrdenadas() {
        List<Tarea> tareasOrdenadas = new ArrayList<>(tareas);
        tareasOrdenadas.sort(Comparator.comparing(Tarea::getFecha)
                .thenComparing(Tarea::getHora)
                .thenComparing(Tarea::getNombre));
        return tareasOrdenadas;
    }
    
    /**
     * Actualiza una tarea en la posición indicada.
     * Verifica que el índice sea válido antes de hacer el cambio.
     */
    public void actualizarTarea(int indice, Tarea tareaEditada) {
        if (indice >= 0 && indice < tareas.size()) {
            tareas.set(indice, tareaEditada);
            guardarTareas();
        }
    }
    
    /**
     * Elimina una tarea por su posición en la lista.
     */
    public void eliminarTarea(int indice) {
        if (indice >= 0 && indice < tareas.size()) {
            tareas.remove(indice);            guardarTareas();
        }
    }
    
    /**
     * Elimina una tarea por el objeto directamente (no por índice).
     */
    public void eliminarTareaObjeto(Tarea tarea) {
        if (tareas.remove(tarea)) {
            guardarTareas();
        }
    }
    
    /**
     * Reinicia las tareas que sean necesarias (por ejemplo, las diarias).
     * Útil para llamar al inicio del día.
     */
    public void reiniciarTareasSiNecesario() {
        for (Tarea tarea : tareas) {
            tarea.reiniciarSiNecesario();
        }
        guardarTareas();
    }
    
    /**
     * Elimina las tareas únicas que se completaron ayer.
     * Esto ayuda a mantener limpia la lista de tareas.
     * Devuelve cuántas tareas eliminó.
     */
    public int eliminarTareasUnicasCompletadasAyer() {
        LocalDate ayer = LocalDate.now().minusDays(1);
        List<Tarea> tareasAEliminar = new ArrayList<>();
        
        // Buscar tareas que cumplan las condiciones
        for (Tarea tarea : tareas) {
            if (tarea.getTipo() == Tarea.TipoTarea.UNICA && 
                tarea.isCompletada() && 
                tarea.getFechaUltimaCompletada() != null &&
                tarea.getFechaUltimaCompletada().equals(ayer)) {
                tareasAEliminar.add(tarea);
            }
        }
        
        // Eliminar las tareas encontradas
        for (Tarea tarea : tareasAEliminar) {
            tareas.remove(tarea);
        }
        
        // Guardar solo si se eliminó algo
        if (!tareasAEliminar.isEmpty()) {
            guardarTareas();
        }
        
        return tareasAEliminar.size();
    }
      /**
     * Exporta todas las tareas a un archivo específico.
     */
    public void exportarTareas(String archivo) {
        repositorio.exportarTareas(tareas, archivo);
    }
    
    /**
     * Importa tareas desde un archivo y las agrega a la lista actual.
     */
    public void importarTareas(String archivo) {
        List<Tarea> tareasImportadas = repositorio.importarTareas(archivo);
        if (tareasImportadas != null) {
            tareas.addAll(tareasImportadas);
            guardarTareas();
        }
    }
    
    /**    /**
     * Guarda todas las tareas en el archivo JSON.
     */
    public void guardarTareas() {
        repositorio.guardarTareas(tareas);
    }
      /**
     * Calcula y devuelve el estado de una tarea (completada, vencida, días restantes).
     * Devuelve texto que se muestra en la tabla.
     */
    public String calcularEstadoTarea(Tarea tarea) {
        if (tarea.isCompletada()) {
            return "✓";
        }
        
        LocalDate hoy = LocalDate.now();
        LocalDate fechaTarea = tarea.getFecha();
        
        if (fechaTarea.isBefore(hoy)) {
            return "Vencida";
        } else if (fechaTarea.equals(hoy)) {
            return "Hoy";
        } else {
            long diasRestantes = java.time.temporal.ChronoUnit.DAYS.between(hoy, fechaTarea);
            return diasRestantes + " día" + (diasRestantes != 1 ? "s" : "");
        }
    }
    
    /**
     * Devuelve el ícono de alerta si la tarea la tiene activada.
     */
    public String obtenerTextoAlerta(Tarea tarea) {
        return tarea.isAlertaActiva() ? "🔔" : "";
    }
    
    /**
     * Cambia el estado de completada/pendiente de una tarea.
     * Busca la tarea en la lista y actualiza su estado.
     */
    public boolean actualizarEstadoTarea(Tarea tarea) {
        List<Tarea> listaOriginal = getTareas();
        int indice = listaOriginal.indexOf(tarea);
        
        if (indice != -1) {
            if (tarea.isCompletada()) {
                tarea.marcarPendiente();
            } else {
                tarea.marcarCompletada();
            }
            actualizarTarea(indice, tarea);
            return true;
        }
        return false;
    }
    
    /**
     * Reemplaza una tarea existente con una versión editada.
     */
    public boolean editarTarea(Tarea tareaOriginal, Tarea tareaEditada) {
        List<Tarea> listaOriginal = getTareas();
        int indice = listaOriginal.indexOf(tareaOriginal);
        
        if (indice != -1) {
            actualizarTarea(indice, tareaEditada);
            return true;
        }
        return false;
    }
      /**
     * Obtiene las tareas ordenadas para mostrar en la tabla.
     * Las ordena por prioridad: pendientes urgentes, vencidas, completadas.
     */
    public List<Tarea> obtenerTareasParaTabla() {
        LocalDateTime ahora = LocalDateTime.now();
        List<Tarea> tareasParaMostrar = new ArrayList<>(tareas);

        // Ordenar por grupos de prioridad y luego por fecha/hora
        tareasParaMostrar.sort((t1, t2) -> {
            boolean comp1 = t1.isCompletada();
            boolean comp2 = t2.isCompletada();
            LocalDateTime dateTime1 = LocalDateTime.of(t1.getFecha(), t1.getHora());
            LocalDateTime dateTime2 = LocalDateTime.of(t2.getFecha(), t2.getHora());
            boolean overdue1 = !comp1 && dateTime1.isBefore(ahora);
            boolean overdue2 = !comp2 && dateTime2.isBefore(ahora);
        
            // Asignar grupos: 1=pendientes, 2=vencidas, 3=completadas
            int group1 = comp1 ? 3 : (overdue1 ? 2 : 1);
            int group2 = comp2 ? 3 : (overdue2 ? 2 : 1);
        
            if (group1 != group2) {
                return Integer.compare(group1, group2);
            }
        
            return dateTime1.compareTo(dateTime2);
        });
        
        return tareasParaMostrar;
    }
    
    /**
     * Crea una fila de datos para mostrar en la tabla.
     * Calcula el tiempo restante y formatea toda la información.
     */
    public String[] crearFilaTabla(Tarea tarea) {
        LocalDateTime ahora = LocalDateTime.now();
        String estado;
        
        if (tarea.isCompletada()) {
            estado = "✓";
        } else {
            LocalDateTime fechaHoraTarea = LocalDateTime.of(tarea.getFecha(), tarea.getHora());
            if (ahora.isAfter(fechaHoraTarea)) {
                estado = "Vencida";
            } else {
                // Calcular tiempo restante en formato legible
                long diffSegundos = java.time.temporal.ChronoUnit.SECONDS.between(ahora, fechaHoraTarea);
                long dias = diffSegundos / (24 * 3600);
                diffSegundos %= (24 * 3600);
                long horas = diffSegundos / 3600;
                diffSegundos %= 3600;
                long minutos = diffSegundos / 60;
                diffSegundos %= 60;
                long segundos = diffSegundos;
                
                if (dias > 0) {
                    estado = String.format("%d d %02d h", dias, horas);
                } else {
                    estado = String.format("%02d:%02d:%02d", horas, minutos, segundos);
                }
            }
        }

        return new String[]{
            tarea.getNombre(),
            tarea.getFechaTexto(),
            tarea.getHoraTexto(),
            tarea.getTipo().toString(), 
            estado,
            tarea.getRepeticiones(), 
            tarea.isAlertaActiva() ? "🔔" : ""
        };
    }
    
    /**
     * Verifica si hay archivos de respaldo disponibles para importar.
     * Útil para mostrar al usuario si puede recuperar datos.
     */
    public boolean hayRespaldosDisponibles() {
        if (repositorio instanceof RepositorioJSON) {
            RepositorioJSON repo = (RepositorioJSON) repositorio;
            // Verificar si existe respaldo principal
            String rutaArchivo = repo.obtenerRutaArchivoTareas();
            File respaldoPrincipal = new File(rutaArchivo + ".backup");
            return respaldoPrincipal.exists() && respaldoPrincipal.length() > 10;
        }
        return false;
    }
    
    /**
     * Carga las tareas desde el repositorio al inicializar.
     */
    private void cargarTareas() {
        List<Tarea> tareasRecuperadas = repositorio.cargarTareas();
        this.tareas.clear();
        this.tareas.addAll(tareasRecuperadas);
    }
}
