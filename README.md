# DeskAgenda - Documentación

## 📋 ÍNDICE
1. [Introducción](#introducción)
2. [Instalación y Configuración](#instalación-y-configuración)
3. [Evolución del Proyecto](#evolución-del-proyecto)
4. [Arquitectura del Sistema](#arquitectura-del-sistema)
5. [Sistema de Backup Revolucionario](#sistema-de-backup)
6. [Estructura del Código](#estructura-del-código)
7. [Guía de Uso](#guía-de-uso)
8. [Resolución de Problemas](#resolución-de-problemas)
9. [Conclusiones](#conclusiones)

---

## 🚀 INTRODUCCIÓN

**DeskAgenda** es una aplicación de gestión de tareas. Combina simplicidad de uso con robustez técnica, garantizando que nunca se pierdan datos importantes.

### Características Principales:
- ✅ **Interfaz moderna** con tema oscuro
- ✅ **Soporte completo UTF-8** (acentos, ñ, emojis)
- ✅ **Sistema de backup triple** muy insane
- ✅ **Alertas automáticas** con sonido 
- ✅ **Optimizado para RAM** (funciona con 64MB, y puede ajustarse para que consuma bastante menos con sólo ajustar un valor)
- ✅ **Portabilidad total** (funciona desde USB. También puede colocar en cualquier disco/carpeta y crear un acceso directo al escritorio)

---

## ⚙️ INSTALACIÓN Y CONFIGURACIÓN

### **Requisitos del Sistema:**
- **Java JDK 11** o superior
- **NetBeans IDE** (cualquier versión reciente)
- **4GB RAM** mínimo (optimizado para funcionar con menos)

### **Instalación desde GitHub:**
1. **Clonar el repositorio:**
   ```bash
   git clone https://github.com/LeanVega/DeskAgenda.git
   ```

2. **Configurar archivo de datos:**
   ```bash
   cd DeskAgenda/src/persistencia/
   copy tareas.example.json tareas.json
   ```

3. **Abrir en NetBeans:**
   - File → Open Project
   - Seleccionar la carpeta `DeskAgenda`

4. **Compilar y ejecutar:**
   - Clean and Build (F11)
   - Run Project (F6)

### **Instalación Portable:**
- Descargar `DeskAgenda_Portable.zip` o `DarkAgenda_Portable.zip`
- Extraer en cualquier ubicación
- Ejecutar directamente (no requiere instalación)

---

## 🔄 EVOLUCIÓN DEL PROYECTO

### FASE 1: Proyecto Base
**Problema inicial:** Diseño de vista principal básica (acabar esto es lo que más tiempo llevó. Un dolor de eggs)

**Desafíos encontrados:**
- Acentos y símbolos se veían mal (codificación incorrecta)
- No había respaldos de seguridad (se me cortó la luz una vez y se fueron todas las actividades a la mierd*)
- Código difícil de entender (al menos para mi, que soy nuevo en Java)
- Consumo excesivo de memoria (aprox 200MB)
- Timers mal optimizados

### FASE 2: Optimización de Arquitectura
**Cambios implementados:**
- Separación clara de capas (interfaz, lógica, persistencia)
- Más comentarios, porque es un TP que hasta a mi me cuesta enteder.
- Optimización de timers y memoria
- Configuración UTF-8 forzada en toda la aplicación

### FASE 3: Sistema de Codificación UTF-8
**Problema detectado:** Los acentos se seguían viendo mal en algunos casos, lpm.

**Solución implementada:**
- Compilación forzada con `-encoding UTF-8`
- Ejecución con `-Dfile.encoding=UTF-8`
- Verificación de todos los mensajes de la aplicación
- Scripts BAT actualizados para mantener UTF-8

### FASE 4: Sistema de Backup Simple
**Primera implementación:** Backup único que se actualizaba con cada cambio.

**Limitaciones detectadas:**
- Si había corte de luz durante escritura (CELO y lpm), se perdían tanto archivo principal como backup
- No había redundancia suficiente

### FASE 5: Sistema de Backup Alternante (Primera Versión)
**Implementación:** Dos backups que se alternaban (backup1 ↔ backup2).

**Problema detectado:** 
- El archivo principal nunca se actualizaba
- Solo se leían los backups
- Confusión sobre qué archivo era el "real", por así decirlo.

### FASE 6: Sistema de Backup Triple DEFINITIVO
**Diseño "final" (abierto a cambios, obviamente):**

```
📁 tareas.json          ← ARCHIVO PRINCIPAL (se consulta constantemente)
🛡️ tareas.backup1.json ← BACKUP INMUNE #1 (alternante)
🛡️ tareas.backup2.json ← BACKUP INMUNE #2 (alternante)
```

**Flujo:**
1. **Al abrir:** Restaurar backup más reciente → archivo principal
2. **Durante uso:** Solo acceder a archivo principal (rápido)
3. **Al guardar:** Actualizar principal + crear backup alternante
4. **Backups inmunes:** Mínima exposición = máxima protección

---

## 🏗️ ARQUITECTURA DEL SISTEMA

### Capas de la Aplicación

#### 1. CAPA DE INTERFAZ (`igu/`)
- **VistaPrincipal.java**: Ventana principal con tabla de tareas
- **DialogoAgregarTarea.java**: Formulario para crear/editar tareas
- **util/**: Componentes personalizados para que se vea moderno

#### 2. CAPA DE LÓGICA (`logica/`)
- **GestorTareas.java**: Cerebro de la aplicación, maneja todas las operaciones
- **Tarea.java**: Estructura de datos de una tarea individual
- **GestorFechas.java**: Maneja alertas y notificaciones temporales
- **GestorSonido.java**: Reproduce sonidos de notificación
- **SistemaBandeja.java**: Integración con bandeja del sistema

#### 3. CAPA DE "PERSISTENCIA" (`persistencia/`) LO QUE MÁS HARÍA FALTA CAMBIAR
- **IRepositorioTareas.java**: Interfaz que define cómo guardar/cargar
- **RepositorioJSON.java**: Implementación que guarda en JSON con backup triple

### Principios de Diseño Aplicados

1. **Separación de Responsabilidades**: Cada clase tiene un propósito específico
2. **Bajo Acoplamiento**: Las capas no dependen entre sí directamente
3. **Alta Cohesión**: Cada módulo agrupa funcionalidad relacionada
4. **Principio Abierto/Cerrado**: Fácil agregar nuevos tipos de repositorio

---

### Backup Triple Alternante

#### Arquitectura de Archivos
```
📁 tareas.json          ← ARCHIVO PRINCIPAL
   ├─ Uso: Lectura/escritura diaria
   ├─ Propósito: Acceso rápido y frecuente
   └─ Riesgo: Medio (se usa mucho)

🛡️ tareas.backup1.json ← BACKUP #1
   ├─ Uso: Solo escritura en cambios
   ├─ Alternancia: Cambios impares (1, 3, 5...)
   └─ Riesgo: Mínimo (exposición mínima)

🛡️ tareas.backup2.json ← BACKUP #2
   ├─ Uso: Solo escritura en cambios
   ├─ Alternancia: Cambios pares (2, 4, 6...)
   └─ Riesgo: Mínimo (exposición mínima)
```

#### Flujo de Operaciones

**AL INICIAR LA APLICACIÓN:**
```
1. Comparar fechas de backup1 y backup2
2. Seleccionar el más reciente
3. Copiar contenido → tareas.json
4. Usar tareas.json para todo el trabajo diario
```

**DURANTE USO NORMAL:**
```
✅ Leer tareas: Solo tareas.json (rápido)
✅ Mostrar interfaz: Solo tareas.json
✅ Filtrar/buscar: Solo tareas.json
```

**AL GUARDAR CAMBIOS:**
```
1. Guardar en tareas.json (escritura atómica)
2. Determinar backup alternante (backup1 o backup2)
3. Escribir a backup.tmp
4. Mover backup.tmp → backup final (atómico)
5. Cerrar backup inmediatamente
6. Alternar para próximo cambio
```

#### Protección Contra Escenarios de Fallo

**Escenario 1: Corte de luz durante uso normal**
```
❌ Problema: Usuario perdería trabajo
✅ Solución: No hay problema, solo se lee tareas.json
```

**Escenario 2: Corte de luz al guardar archivo principal**
```
❌ Problema: tareas.json se podría corromper
✅ Solución: Al reiniciar, se restaura desde backup más reciente
```

**Escenario 3: Corte de luz al crear backup**
```
❌ Problema: Backup se podría corromper
✅ Solución: Archivo principal está intacto + otro backup disponible
```

**Escenario 4: Corrupción de archivo principal**
```
❌ Problema: No se pueden leer las tareas
✅ Solución: Al reiniciar, se restaura automáticamente desde backup
```

### Implementación Técnica

#### Escritura
```java
// NUNCA se escribe directamente al archivo final
String archivoTemporal = archivo + ".tmp";
escribirDatos(archivoTemporal);
Files.move(temporal, final); // Operación atómica del SO
```

#### Alternancia Inteligente
```java
private static boolean usarBackup1 = true;
String backup = usarBackup1 ? backup1 : backup2;
// ... escribir backup ...
usarBackup1 = !usarBackup1; // Alternar para próxima vez
```

#### Detección de Archivo Más Reciente
```java
long fecha1 = archivo1.lastModified();
long fecha2 = archivo2.lastModified();
String masReciente = (fecha1 > fecha2) ? archivo1 : archivo2;
```

---

## 💻 ESTRUCTURA DEL CÓDIGO

### Organización de Paquetes

```
src/
├── igu/                    Interface Gráfica de Usuario
│   ├── VistaPrincipal.java           ← Ventana principal
│   ├── DialogoAgregarTarea.java      ← Formulario de tareas
│   └── util/                         ← Utilidades UI
│       ├── CircleBorder.java         ← Bordes circulares
│       ├── ComponentMover.java       ← Mover ventanas
│       ├── RoundBorder.java          ← Bordes redondeados
│       └── WindowResizer.java        ← Redimensionar ventanas
├── logica/                 Lógica de Negocio
│   ├── GestorTareas.java             ← Administrador principal
│   ├── Tarea.java                    ← Estructura de datos
│   ├── GestorFechas.java             ← Alertas temporales
│   ├── GestorSonido.java             ← Sonidos
│   └── SistemaBandeja.java           ← Bandeja del sistema
└── persistencia/          Almacenamiento de Datos
    ├── IRepositorioTareas.java       ← Interfaz de persistencia
    └── RepositorioJSON.java          ← Implementación JSON
```

### Clases Principales Explicadas

#### VistaPrincipal.java
```java
// Esta es la ventana que ve el usuario
// Responsabilidades:
// - Mostrar tabla de tareas
// - Manejar clicks de botones
// - Actualizar interfaz automáticamente
// - Mostrar alertas visuales
```

#### GestorTareas.java
```java
// El "cerebro" de la aplicación
// Responsabilidades:
// - Coordinar guardado/carga de tareas
// - Mantener lista de tareas en memoria
// - Validar operaciones
// - Trigger de backups automáticos
```

#### RepositorioJSON.java
```java
// Responsabilidades:
// - Escribir/leer archivos JSON
// - Manejar codificación UTF-8
// - Implementar sistema de backup triple
// - Garantizar integridad de datos
```

#### Tarea.java
```java
// Representa una tarea individual
// Responsabilidades:
// - Almacenar información de la tarea
// - Validar datos (fechas, tipos)
// - Formatear para mostrar al usuario
// - Manejar repeticiones y alertas
```

### Patrones de Diseño Utilizados

#### 1. Repository Pattern
- **Problema**: ¿Cómo separar lógica de almacenamiento?
- **Solución**: Interfaz `IRepositorioTareas` + implementación `RepositorioJSON`
- **Beneficio**: Fácil cambiar a base de datos o XML sin tocar lógica

#### 2. MVC (Modelo-Vista-Controlador, ponele)
- **Model**: `Tarea.java`, `GestorTareas.java`
- **View**: `VistaPrincipal.java`, `DialogoAgregarTarea.java`
- **Controller**: Eventos y listeners en las vistas
- **Beneficio**: Separación clara de responsabilidades

#### 3. Strategy Pattern (implícito)
- **Problema**: Diferentes tipos de tareas (única, diaria, semanal)
- **Solución**: Enum `TipoTarea` con comportamientos específicos
- **Beneficio**: Fácil agregar nuevos tipos sin romper código existente

---

## 📖 GUÍA DE USO

### Para Usuarios que no van a leer esto

#### Instalación
1. Descargar `SimpleAgenda.jar`
3. Doble click en el JAR o ejecutar: `java -jar SimpleAgenda.jar`

#### Uso Básico
1. **Agregar Tarea**: Click en "AGREGAR ACTIVIDAD"
2. **Completar Tarea**: Seleccionar tarea → "MARCAR COMPLETADA"
3. **Eliminar Tarea**: Seleccionar tarea → "ELIMINAR ACTIVIDAD"
4. **Exportar/Importar**: Click en "..." (porque no se me ocurrió nada) → Seleccionar opción

#### Tipos de Tareas
- **Única**: Se hace una sola vez (ej: "Cita médica")
- **Diaria**: Se repite todos los días (ej: "Tomar medicamento")
- **Semanal**: Ciertos días de la semana (ej: "Gimnasio lunes y viernes". Es lo que tendría que hacer y no hago)

### Para el que quiera seguir esto

#### Configuración del Entorno
```bash
# Clonar proyecto
git clone [repositorio]

# Compilar con UTF-8
javac -encoding UTF-8 -cp src -d build/classes src/**/*.java

# Crear JAR
jar cfm SimpleAgenda.jar manifest.mf -C build/classes .

# Ejecutar con UTF-8
java -Dfile.encoding=UTF-8 -jar SimpleAgenda.jar
```

#### Estructura de Archivos Generados
```
📁 Directorio de la aplicación/
├── SimpleAgenda.jar          ← Aplicación principal
├── tareas.json              ← Archivo principal de datos
├── tareas.backup1.json      ← Backup inmune #1
├── tareas.backup2.json      ← Backup inmune #2
└── notification/            ← Sonidos y recursos
    ├── notification_sound.wav
    └── notification_icon.png
```

#### Personalización

**Cambiar colores del tema:**
```java
// En VistaPrincipal.java
private Color fondo = new Color(30, 30, 30);      // Fondo oscuro
private Color naranja = new Color(255, 152, 0);   // Color de acento
```

**Agregar nuevo tipo de tarea:**
```java
// En Tarea.java, agregar al enum TipoTarea
MENSUAL {
    @Override
    public String toString() {
        return "Mensual";
    }
},
```

**Cambiar intervalo de actualización:**
```java
// En VistaPrincipal.java
private Timer timerActualizacion = new Timer(12000, this); // 12 segundos, pero si se cambia a un valor más alto, no pasa nada. Dejé un timer separado para las animaciones. Aumentar este valor debería disminuir el uso de RAM.
```

---

## 🔧 RESOLUCIÓN DE PROBLEMAS

### Problemas Comunes

#### 1. Los acentos se ven mal
**Síntomas**: "año" aparece como "a?o" o caracteres extraños

**Solución**:
```bash
# Compilar siempre con UTF-8
javac -encoding UTF-8 ...

# Ejecutar siempre con UTF-8
java -Dfile.encoding=UTF-8 ...
```

#### 2. No se guardan las tareas
**Síntomas**: Las tareas desaparecen al cerrar la aplicación

**Diagnóstico**:
- Verificar que se crean los archivos `.json`
- Comprobar permisos de escritura en el directorio
- Revisar si hay errores en la consola

**Solución**:
- Ejecutar como administrador si es necesario
- Verificar que el directorio no sea de solo lectura

#### 3. La aplicación consume mucha memoria
**Síntomas**: El equipo se pone lento

**Solución**:
```bash
# Ejecutar con memoria limitada. Lo probé hasta con 10MB y no explotó/funcionaba, así que... no sé. Yo me conformo con lo que consume ahora.
java -Xmx64m -jar SimpleAgenda.jar
```

#### 4. No suenan las alertas
**Síntomas**: No se escuchan notificaciones

**Diagnóstico**:
- Verificar que existe `notification/notification_sound.wav`. Si hay un mp3 es porque me olvidé de agregarlo al gitignore, así que se puede borrar. Lo mismo si hay un png. Ya no se usa.
- Comprobar volumen del sistema
- Revisar permisos de archivos de sonido

#### 5. Error "Año debe estar entre 2000 y 2099"
**Síntomas**: No se pueden crear tareas futuras

**Explicación**: Limitación intencional para evitar fechas irreales. Hay que cambiar esto por si justo Wolverine o Deadpool usan la agenda.

**Solución**: Usar fechas entre 2000-2099 o modificar el código (o no me acuerdo si limité a 3000??? Bueno, ponele):
```java
// En DialogoAgregarTarea.java
if (anio < 1900 || anio > 2200) { // Rango más amplio
```

### Recuperación de Datos

#### Si se perdió tareas.json
```bash
# Los backups siempre están disponibles
# Al reiniciar, la aplicación recupera automáticamente desde:
# 1. tareas.backup1.json (si es más reciente)
# 2. tareas.backup2.json (si backup1 no existe)
```

#### Recuperación manual
```bash
# Si es necesario recuperar manualmente:
copy tareas.backup1.json tareas.json
# O usar la función "Importar" desde la aplicación
```

#### Verificar integridad de backups
```bash
# Si un backup está corrupto, usar el otro:
type tareas.backup1.json
type tareas.backup2.json
```

---

## 🎯 CONCLUSIONES

### Logros Técnicos

1. **Sistema de Backup bien... bien**: Implementamos una arquitectura de triple backup que garantiza la supervivencia de datos ante CASI cualquier fallo del sistema. Si se te funde tu disco... oops. Ya voy a trabajar en eso.

2. **Optimización de Memoria**: La aplicación funciona perfectamente con solo 64MB de RAM, haciéndola viable para equipos antiguos. No TAN antiguos.

3. **Soporte UTF-8 Completo**: Todos los caracteres españoles (acentos, ñ, símbolos) se manejan correctamente en toda la aplicación.

4. **Arquitectura Limpia**: Separación "clara" (ponele) de capas permite fácil mantenimiento y extensión.

5. **Comentarios Comprensibles**: Todo el código está comentado para que lo entiendan. Si no lo entienden, me mato, porque estuve como 4 horas agregando comentarios.

### Resumen

#### Sistema de Backup Triple
- **Problema Resuelto**: Pérdida de datos por cortes de luz o fallos del sistema
- **Innovación**: Archivo principal activo + dos backups inmunes alternantes
- **Resultado**: Riesgo de pérdida de datos casi eliminado

#### Optimización de Timers
- **Problema Resuelto**: Consumo excesivo de CPU y memoria
- **Innovación**: Timer de animación (1s) separado del timer de datos (12s)
- **Resultado**: Interfaz fluida con mínimo consumo de recursos

#### Detección Inteligente de Rutas
- **Problema Resuelto**: Archivos se guardan en lugares incorrectos
- **Innovación**: Detección automática de entorno (desarrollo vs producción)
- **Resultado**: Funciona perfectamente tanto si se ejecuta desde NetBeans como el JAR distribuido

### Filosofía de Desarrollo

Este proyecto siguió principios de **programación defensiva, o algo así**:

1. **Asumir que todo puede fallar**: Por eso el sistema de backup triple
2. **Validar todas las entradas**: Por eso las validaciones estrictas de fechas
3. **Fallar silenciosamente**: Por eso no se muestran errores técnicos al usuario
4. **Documentar todo**: Por eso estoy escribiendo esto

### Impacto en la Experiencia del Usuario

- **Confiabilidad**: Los usuarios nunca perderán sus tareas. O casi nunca.
- **Rendimiento**: Funciona rápido incluso en equipos antiguos
- **Usabilidad**: Interfaz intuitiva con diseño moderno
- **Accesibilidad**: Soporte completo para caracteres de habla hispana

### Lecciones Aprendidas

1. **La simplicidad es compleja**: Crear una interfaz simple fue un dolor de...
2. **Los detalles importan**: Problemas como UTF-8 pueden arruinar la experiencia
3. **La redundancia es saludable**: El backup triple evita dolores de "cabeza" futuros
4. **La documentación es keeeeyyyy**: Código comentado

### Futuras Mejoras Posibles

1. **Sincronización en la nube**: Backup automático a Google Drive/Dropbox.
2. **Categorías de tareas**: Organizar tareas por trabajo, personal, etc.
3. **Estadísticas**: Mostrar productividad y tendencias
4. **Temas personalizables**: Permitir al usuario elegir colores
5. **Plugins**: Sistema de extensiones para funcionalidades adicionales
6. **App??**: Tal vez. No sé.

---

**DeskAgenda** no es solo una aplicación de tareas: es un ejemplo de *inserte ejemplo*. No se me ocurre nada.

La combinación de arquitectura decente sin necesidad de consumir muchos recursos, con la posibilidad de consumir aún menos, y atención al detalle resulta en una aplicación que puede funcionar durante años sin problemas, protegiendo los datos importantes de los usuarios sin que ellos tengan que preocuparse por los aspectos técnicos.

---

*Documentación creada para SimpleAgenda v1.0, que ahora se llama DeskAgenda porque le cambié el nombre*  
*Proyecto Team Leandro, Jose e Ignacio - 2025.
