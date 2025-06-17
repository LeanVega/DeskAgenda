/*
 * DeskAgenda - Persistencia de Datos
 * 
 * Este archivo maneja el guardado y carga de tareas en formato JSON.
 * Incluye un sistema de backup triple para máxima seguridad.
 */
package persistencia;

// === IMPORTS DE LÓGICA ===
import logica.Tarea;                    // Estructura de datos de una tarea

// === IMPORTS DE ENTRADA/SALIDA (I/O) ===
import java.io.*;                       // Para leer y escribir archivos
import java.nio.charset.StandardCharsets; // Para usar UTF-8 y evitar problemas de acentos
import java.nio.file.Files;             // Operaciones avanzadas con archivos
import java.nio.file.StandardCopyOption; // Para copiar archivos de forma segura

// === IMPORTS DE FECHA Y HORA ===
import java.time.LocalDate;             // Para fechas
import java.time.LocalTime;             // Para horas
import java.time.DayOfWeek;             // Para días de la semana

// === IMPORTS DE COLECCIONES ===
import java.util.*;                     // List, ArrayList, Set, etc.

/**
 * REPOSITORIO JSON - PERSISTENCIA
 * =============================================
 * 
 * Esta clase es LA KEEEEY para DeskAgenda. Se encarga de:
 * 
 * 1. GUARDAR TAREAS: En formato JSON legible y con acentos
 * 2. CARGAR TAREAS: Al iniciar la aplicación
 * 3. SISTEMA DE BACKUP TRIPLE: Para proteger contra pérdidas de datos
 * 
 * ARQUITECTURA DE SEGURIDAD IMPLEMENTADA:
 * =====================================
 * 
 *  tareas.json          ← ARCHIVO PRINCIPAL (uso diario)
 *  tareas.backup1.json ← BACKUP INMUNE #1 (alternante)
 *  tareas.backup2.json ← BACKUP INMUNE #2 (alternante)
 * 
 * FLUJO DE SEGURIDAD:
 *  Al abrir: Backup más reciente → Archivo principal
 *  Durante uso: Solo lee archivo principal (rápido)
 *  Al guardar: Archivo principal + Backup alternante
 *  Backups: Mínima exposición = Inmunes a apagones
 * 
 * ¿POR QUÉ ESTE DISEÑO?
 * - No se pierden datos por cortes de luz
 * - Máximo 1 cambio perdido (casi imposible)
 * - Archivos siempre legibles con acentos
 * - Sistema silencioso que no molesta al usuario
 */
public class RepositorioJSON implements IRepositorioTareas {
    
    // === CONFIGURACIÓN DE ARCHIVOS ===
    private static final String ARCHIVO_TAREAS = obtenerRutaArchivo(); // Ruta del archivo principal
    
    // === CONTROL DE ALTERNANCIA DE BACKUPS ===
    // Esta variable controla si el próximo backup va a backup1 o backup2
    private static boolean usarBackup1 = true;

    /**
     * DETERMINA DÓNDE GUARDAR LOS ARCHIVOS
     * ===================================
     * 
     * Esta función detecta si estamos en:
     * - DESARROLLO: Guarda en src/persistencia/tareas.json
     * - PRODUCCIÓN: Guarda junto al JAR (tareas.json)
     * 
     * Esto permite que el proyecto funcione tanto en NetBeans como cuando
     * se distribuye el JAR a los usuarios.
     */private static String obtenerRutaArchivo() {
        // Intentar obtener el directorio donde está el JAR
        try {
            String jarPath = RepositorioJSON.class.getProtectionDomain()
                .getCodeSource().getLocation().toURI().getPath();
            File jarFile = new File(jarPath);
            String directorioApp = jarFile.getParent();
              
            // Si es un directorio build/classes (desarrollo), usar src/persistencia
            if (directorioApp != null && directorioApp.contains("build")) {
                return "src/persistencia/tareas.json"; // Para desarrollo desde NetBeans
            }
            
            // Si es un JAR (producción), usar el mismo directorio del JAR
            return directorioApp + File.separator + "tareas.json";
            
        } catch (Exception e) {
            // Fallback: directorio actual
            return "tareas.json";
        }
    }
    
    /**
     * Obtiene la ruta del primer backup alternante.
     */
    private static String obtenerRutaBackup1() {
        String rutaBase = ARCHIVO_TAREAS;
        int puntoIndex = rutaBase.lastIndexOf('.');
        if (puntoIndex > 0) {
            return rutaBase.substring(0, puntoIndex) + ".backup1.json";
        }
        return rutaBase + ".backup1";
    }
    
    /**
     * Obtiene la ruta del segundo backup alternante.
     */
    private static String obtenerRutaBackup2() {
        String rutaBase = ARCHIVO_TAREAS;
        int puntoIndex = rutaBase.lastIndexOf('.');
        if (puntoIndex > 0) {
            return rutaBase.substring(0, puntoIndex) + ".backup2.json";
        }
        return rutaBase + ".backup2";
    }    @Override
    public void guardarTareas(List<Tarea> tareas) {
        // 📝 PASO 1: Guardar en archivo principal (uso diario)
        guardarEnArchivoPrincipal(tareas);
        
        // 🛡️ PASO 2: Crear backup inmune alternante (solo en cambios)
        crearBackupAlternante(tareas);
    }
    
    /**
     * Guarda las tareas en el archivo principal con escritura atómica.
     */
    private void guardarEnArchivoPrincipal(List<Tarea> tareas) {
        String archivoTemporal = ARCHIVO_TAREAS + ".tmp";
        
        try {
            // Escribir a archivo temporal primero (escritura atómica)
            escribirTareasAArchivo(tareas, archivoTemporal);
            
            // Solo si se escribió correctamente, reemplazar el archivo principal
            Files.move(new File(archivoTemporal).toPath(), 
                      new File(ARCHIVO_TAREAS).toPath(), 
                      StandardCopyOption.REPLACE_EXISTING);
                      
        } catch (IOException e) {
            System.err.println("Error al guardar tareas en archivo principal: " + e.getMessage());
        }
    }
    
    /**
     * Crea backup alternante demínima exposición
     */
    private void crearBackupAlternante(List<Tarea> tareas) {
        String backupActual = usarBackup1 ? obtenerRutaBackup1() : obtenerRutaBackup2();
        String archivoTemporal = backupActual + ".tmp";
        
        try {
            // 🔒 ESCRITURA RÁPIDA: Escribir y cerrar inmediatamente
            escribirTareasAArchivo(tareas, archivoTemporal);
            
            // Mover archivo temporal → backup (operación atómica)
            Files.move(new File(archivoTemporal).toPath(), 
                      new File(backupActual).toPath(), 
                      StandardCopyOption.REPLACE_EXISTING);
            
            // Alternar para próximo backup (backup1 ↔ backup2)
            usarBackup1 = !usarBackup1;
            
        } catch (IOException e) {
            // Error en backup, continuar silenciosamente (archivo principal ya guardado)
        }
    }
    
    /**
     * Método auxiliar para escribir tareas a cualquier archivo.
     */
    private void escribirTareasAArchivo(List<Tarea> tareas, String rutaArchivo) throws IOException {
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(rutaArchivo), StandardCharsets.UTF_8))) {
            writer.println("[");
            for (int i = 0; i < tareas.size(); i++) {
                Tarea tarea = tareas.get(i);
                writer.print("  ");
                escribirTareaJSON(writer, tarea);
                if (i < tareas.size() - 1) {
                    writer.println(",");
                } else {
                    writer.println();
                }
            }
            writer.println("]");
            writer.flush(); // Asegurar que se escriba todo
        }
    }@Override
    public List<Tarea> cargarTareas() {
        // AL INICIO: Restaurar desde backup más reciente al archivo principal
        restaurarDesdeBackupMasReciente();
        
        // USAR ARCHIVO PRINCIPAL para operaciones normales
        return intentarCargarArchivo(ARCHIVO_TAREAS);
    }
    
    /**
     * Restaura el estado más reciente desde los backups al archivo principal.
     * Se ejecuta solo al iniciar la aplicación.
     */
    private void restaurarDesdeBackupMasReciente() {
        String backup1 = obtenerRutaBackup1();
        String backup2 = obtenerRutaBackup2();
        
        File archivoBackup1 = new File(backup1);
        File archivoBackup2 = new File(backup2);
        String backupMasReciente = null;
        
        // Determinar cuál backup es más reciente
        if (archivoBackup1.exists() && archivoBackup2.exists()) {
            backupMasReciente = (archivoBackup1.lastModified() > archivoBackup2.lastModified()) 
                               ? backup1 : backup2;
        } else if (archivoBackup1.exists()) {
            backupMasReciente = backup1;
        } else if (archivoBackup2.exists()) {
            backupMasReciente = backup2;
        }
        
        // Restaurar backup más reciente → archivo principal
        if (backupMasReciente != null) {
            try {
                Files.copy(new File(backupMasReciente).toPath(), 
                          new File(ARCHIVO_TAREAS).toPath(), 
                          StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                // Error al restaurar, continuar silenciosamente/sin mostrar alertas
            }
        }
    }
    
    /**
     * Intenta cargar tareas desde un archivo específico.
     * Maneja errores de archivos corruptos sin lanzar excepciones.
     */
    private List<Tarea> intentarCargarArchivo(String nombreArchivo) {
        List<Tarea> tareas = new ArrayList<>();
        File archivo = new File(nombreArchivo);
        
        if (!archivo.exists()) {
            return tareas; // Lista vacía si no existe
        }
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(archivo), StandardCharsets.UTF_8))) {
            StringBuilder contenido = new StringBuilder();
            String linea;
            while ((linea = reader.readLine()) != null) {
                contenido.append(linea);
            }            // Verificar que el contenido sea un JSON válido
            String contenidoStr = contenido.toString().trim();
            if (contenidoStr.isEmpty() || contenidoStr.length() < 3 || 
                !contenidoStr.startsWith("[") || !contenidoStr.endsWith("]") ||
                contenidoStr.equals("[]") || contenidoStr.equals("[")) {
                return tareas; // Archivo corrupto, devolver lista vacía silenciosamente
            }            tareas = parsearTareasJSON(contenidoStr);
        } catch (Exception e) {
            // Error al leer archivo, devolver lista vacía silenciosamente
        }
        
        return tareas;
    }
      @Override
    public void exportarTareas(List<Tarea> tareas, String archivo) {
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(archivo), StandardCharsets.UTF_8))) {
            writer.println("[");
            for (int i = 0; i < tareas.size(); i++) {
                Tarea tarea = tareas.get(i);
                writer.print("  ");
                escribirTareaJSON(writer, tarea);
                if (i < tareas.size() - 1) {
                    writer.println(",");
                } else {
                    writer.println();
                }
            }
            writer.println("]");
        } catch (IOException e) {
            throw new RuntimeException("Error al exportar tareas: " + e.getMessage());
        }
    }
      @Override
    public List<Tarea> importarTareas(String archivo) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(archivo), StandardCharsets.UTF_8))) {
            StringBuilder contenido = new StringBuilder();
            String linea;
            while ((linea = reader.readLine()) != null) {
                contenido.append(linea);
            }
            
            return parsearTareasJSON(contenido.toString());
        } catch (IOException e) {
            throw new RuntimeException("Error al importar tareas: " + e.getMessage());
        }
    }
    
    private void escribirTareaJSON(PrintWriter writer, Tarea tarea) {
        writer.print("{");
        writer.print("\"nombre\":\"" + escaparJSON(tarea.getNombre()) + "\",");
        writer.print("\"fecha\":\"" + tarea.getFecha().toString() + "\",");
        writer.print("\"hora\":\"" + tarea.getHora().toString() + "\",");
        writer.print("\"tipo\":\"" + tarea.getTipo().name() + "\",");
        writer.print("\"completada\":" + tarea.isCompletada() + ",");
        writer.print("\"alertaActiva\":" + tarea.isAlertaActiva() + ",");
        writer.print("\"minutosAntesAlerta\":" + tarea.getMinutosAntesAlerta() + ",");
        writer.print("\"repeticionesCompletadas\":" + tarea.getRepeticionesCompletadas());
        if (tarea.getFechaUltimaCompletada() != null) {
            writer.print(",\"fechaUltimaCompletada\":\"" + tarea.getFechaUltimaCompletada().toString() + "\"");
        }
        if (!tarea.getDiasSemana().isEmpty()) {
            writer.print(",\"diasSemana\":[");
            boolean primero = true;
            for (DayOfWeek dia : tarea.getDiasSemana()) {
                if (!primero) writer.print(",");
                writer.print("\"" + dia.name() + "\"");
                primero = false;
            }
            writer.print("]");
        }
        writer.print("}");
    }
    
    private String escaparJSON(String texto) {
        if (texto == null) return "";
        return texto.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }
      private List<Tarea> parsearTareasJSON(String contenido) {
        List<Tarea> tareas = new ArrayList<>();
        
        try {
            contenido = contenido.trim();
            if (!contenido.startsWith("[") || !contenido.endsWith("]")) {
                return tareas;
            }
            
            // Remover corchetes externos
            contenido = contenido.substring(1, contenido.length() - 1).trim();
            
            if (contenido.isEmpty()) {
                return tareas;
            }
            
            // Separar objetos JSON
            List<String> objetosJSON = new ArrayList<>();
            int nivel = 0;
            StringBuilder objetoActual = new StringBuilder();
            
            for (int i = 0; i < contenido.length(); i++) {
                char c = contenido.charAt(i);
                
                if (c == '{') {
                    nivel++;
                } else if (c == '}') {
                    nivel--;
                }
                
                objetoActual.append(c);
                
                if (nivel == 0 && c == '}') {
                    objetosJSON.add(objetoActual.toString().trim());
                    objetoActual = new StringBuilder();
                    // Saltar coma y espacios
                    while (i + 1 < contenido.length() && (contenido.charAt(i + 1) == ',' || Character.isWhitespace(contenido.charAt(i + 1)))) {
                        i++;
                    }
                }
            }
            
            // Parsear cada objeto
            for (String objetoJSON : objetosJSON) {
                Tarea tarea = parsearTareaIndividual(objetoJSON);
                if (tarea != null) {
                    tareas.add(tarea);
                }
            }
              } catch (Exception e) {
            // Error al parsear JSON, devolver lista vacía
        }
        
        return tareas;
    }
    
    private Tarea parsearTareaIndividual(String objetoJSON) {
        try {
            String nombre = extraerValor(objetoJSON, "nombre");
            String fechaStr = extraerValor(objetoJSON, "fecha");
            String horaStr = extraerValor(objetoJSON, "hora");
            String tipoStr = extraerValor(objetoJSON, "tipo");
            boolean completada = Boolean.parseBoolean(extraerValor(objetoJSON, "completada"));
            boolean alertaActiva = Boolean.parseBoolean(extraerValor(objetoJSON, "alertaActiva"));
            int minutosAntesAlerta = Integer.parseInt(extraerValor(objetoJSON, "minutosAntesAlerta", "0"));
            int repeticionesCompletadas = Integer.parseInt(extraerValor(objetoJSON, "repeticionesCompletadas", "0"));
            
            LocalDate fecha = LocalDate.parse(fechaStr);
            LocalTime hora = LocalTime.parse(horaStr);
            Tarea.TipoTarea tipo = Tarea.TipoTarea.valueOf(tipoStr);
            
            Tarea tarea = new Tarea(nombre, fecha, hora, tipo);
            tarea.setCompletada(completada);
            tarea.setAlertaActiva(alertaActiva);
            tarea.setMinutosAntesAlerta(minutosAntesAlerta);
            tarea.setRepeticionesCompletadas(repeticionesCompletadas);
            
            String fechaUltimaCompletadaStr = extraerValor(objetoJSON, "fechaUltimaCompletada");
            if (fechaUltimaCompletadaStr != null && !fechaUltimaCompletadaStr.isEmpty()) {
                tarea.setFechaUltimaCompletada(LocalDate.parse(fechaUltimaCompletadaStr));
            }
            
            return tarea;
              } catch (Exception e) {
            // Error al parsear tarea individual, devolver null
            return null;
        }
    }
    
    private String extraerValor(String json, String clave) {
        return extraerValor(json, clave, null);
    }
    
    private String extraerValor(String json, String clave, String valorPorDefecto) {
        String patron = "\"" + clave + "\":";
        int inicio = json.indexOf(patron);
        if (inicio == -1) {
            return valorPorDefecto;
        }
        
        inicio += patron.length();
        while (inicio < json.length() && Character.isWhitespace(json.charAt(inicio))) {
            inicio++;
        }
        
        if (inicio >= json.length()) {
            return valorPorDefecto;
        }
        
        int fin;
        if (json.charAt(inicio) == '"') {
            // Valor string
            inicio++; // Saltar comilla inicial
            fin = inicio;
            while (fin < json.length() && json.charAt(fin) != '"') {
                if (json.charAt(fin) == '\\') {
                    fin++; // Saltar caracter
                }
                fin++;
            }
            return json.substring(inicio, fin);
        } else {
            // Valor numérico o booleano
            fin = inicio;
            while (fin < json.length() && json.charAt(fin) != ',' && json.charAt(fin) != '}') {
                fin++;
            }            return json.substring(inicio, fin).trim();
        }
    }
      /**
     * Obtiene la ruta del archivo de tareas (para verificación).
     */
    public String obtenerRutaArchivoTareas() {
        return ARCHIVO_TAREAS;
    }
    
    /**
     * Obtiene las rutas de los archivos de backup para verificación.
     */
    public String[] obtenerRutasBackups() {
        return new String[]{obtenerRutaBackup1(), obtenerRutaBackup2()};
    }
}
