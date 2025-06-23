/*
 * DeskAgenda - Agenda Personal
 * 
 * Esta es la ventana principal de la agenda.
 * Permite gestionar tareas de forma fácil y segura, con sistema de respaldo automático.
 * 
 * Soporte completo de acentos y símbolos (UTF-8).
 */
package igu;

// === IMPORTS DE LÓGICA DE NEGOCIO ===
import logica.GestorTareas;        // Maneja el guardado y carga de tareas
import logica.SistemaBandeja;      // Muestra la aplicación en la bandeja del sistema
import logica.GestorFechas;        // Controla alertas y notificaciones por fecha/hora
import logica.GestorSonido;        // Reproduce sonidos de notificación
import logica.Tarea;               // Estructura de datos que representa una tarea

// === IMPORTS DE INTERFAZ GRÁFICA PERSONALIZADA ===
import igu.util.CircleBorder;      // Crea bordes circulares para botones
import igu.util.ComponentMover;    // Permite mover la ventana arrastrando cualquier parte
import igu.util.RoundBorder;       // Crea bordes redondeados
import igu.util.WindowResizer;     // Permite redimensionar la ventana desde los bordes

// === IMPORTS DE SWING (INTERFAZ GRÁFICA JAVA) ===
import javax.swing.*;              // Componentes básicos: JFrame, JButton, JTable, etc.
import javax.swing.table.*;        // Manejo de tablas: DefaultTableModel, TableCellRenderer

// === IMPORTS DE AWT (GRÁFICOS Y EVENTOS) ===
import java.awt.*;                 // Colores, fuentes, layouts (organización de componentes)
import java.awt.event.*;           // Eventos de mouse, teclado, botones

// === IMPORTS DE UTILIDADES JAVA ===
import java.util.List;             // Para manejar listas de tareas
import java.time.LocalDateTime;    // Para manejar fechas y horas
import java.time.temporal.ChronoUnit; // Para calcular diferencias de tiempo

/**
 * VENTANA PRINCIPAL DE DESKAGENDA
 * ================================
 * 
 * Esta clase representa la ventana principal donde el usuario:
 * - Ve todas sus tareas en una tabla
 * - Puede agregar, eliminar y marcar tareas como completadas
 * - Exportar e importar tareas 
 * - Recibe alertas automáticas
 * 
 * CARACTERÍSTICAS PRINCIPALES:
 * - Interfaz moderna con bordes redondeados
 * - Sin barra de título (se puede mover arrastrando)
 * - Actualización automática cada pocos segundos
 * - Sistema de bandeja (se minimiza al área de notificaciones)
 * - Respaldo automático de datos para evitar pérdidas
 */
public class VistaPrincipal extends JFrame {

    // Número de versión para serialización (requerido por JFrame)
    private static final long serialVersionUID = 1L;

    // === CONFIGURACIÓN DE CODIFICACIÓN UTF-8 ===
    // Esto asegura que todos los acentos y símbolos se vean bien
    static {
        System.setProperty("file.encoding", "UTF-8");
    }

    // === COMPONENTES PRINCIPALES DE LA INTERFAZ ===
    private JTable tabla;                    // Tabla que muestra todas las tareas
    private DefaultTableModel modelo;        // Modelo que contiene los datos de la tabla
    
    // === BOTONES DE LA APLICACIÓN ===
    private JButton btnAgregar;              // Botón para crear nuevas tareas
    private JButton btnEliminar;             // Botón para borrar tareas seleccionadas
    private JButton btnMarcar;               // Botón para marcar tareas como completadas
    private JButton btnConfigurar;           // Botón de configuración (futuro uso)
    private JButton btnImportExport;         // Botón para importar/exportar tareas
    private JButton btnCerrarVentana;        // Botón para cerrar la aplicación
    
    // === COMPONENTES DE DISEÑO ===
    private JScrollPane scrollPane;          // Panel con scroll para la tabla
    private JPanel titleBarPanel;            // Panel superior que actúa como barra de título
    
    // === COLORES Y FUENTES (TEMA OSCURO MODERNO) ===
    private Font font = new Font("Segoe UI Semibold", Font.BOLD, 16);  // Fuente principal
    private Color fondo = new Color(30, 30, 30);          // Color de fondo oscuro
    private Color texto = new Color(240, 240, 240);       // Color de texto claro
    private Color naranja = new Color(255, 152, 0);       // Color de acento (botones activos)
    private Color grisClaro = new Color(60, 60, 60);      // Color para elementos secundarios
    
    // === GESTORES DE LÓGICA DE NEGOCIO ===
    private GestorTareas gestorTareas;       // Maneja el guardado y carga de tareas
    private SistemaBandeja sistemaBandeja;   // Controla la bandeja del sistema
    private GestorSonido gestorSonido;       // Reproduce sonidos de notificación
    
    // === TIMERS PARA AUTOMATIZACIÓN ===
    // Estos timers se encargan de mantener la aplicación actualizada automáticamente
    private Timer timerActualizacion;        // Recarga datos cada 12 segundos (ahorro de RAM)
    private Timer timerAnimacion;            // Actualiza tiempo restante cada segundo (suave)
    private Timer timerDiarioAutoEliminacion; // Limpia tareas completadas diariamente
    private JButton btnMaximizarVentana;
    private JButton btnMinimizarVentana;

    // Variable para almacenar la lista ordenada de tareas que se muestra en la tabla.
    // Se actualiza en actualizarTabla() y se usa en los métodos de acción para referenciar la tarea correcta.
    private List<Tarea> tareasOrdenadasMostradas; 

    public VistaPrincipal() {
        // Inicializar gestores y sistema de bandeja
        gestorTareas = new GestorTareas();
        gestorSonido = new GestorSonido();
        sistemaBandeja = new SistemaBandeja(this, gestorTareas); 
        new GestorFechas(gestorTareas, gestorSonido); 
        
        setUndecorated(true);
        setSize(1200, 700);
        setLocationRelativeTo(null);
        setLayout(null); // Usando layout nulo para el panel de contenido principal del frame
        getContentPane().setBackground(fondo);
        setResizable(true);

        crearBotonesVentana(); // Crea y añade el titleBarPanel con sus botones

        btnAgregar = crearBoton("AGREGAR ACTIVIDAD", 0, 0);
        btnEliminar = crearBoton("ELIMINAR ACTIVIDAD", 0, 0);
        btnMarcar = crearBoton("MARCAR COMO REALIZADA", 0, 0);
        btnConfigurar = crearBoton("CONFIGURAR ACTIVIDAD", 0, 0);

        btnAgregar.addActionListener(e -> abrirDialogoAgregarActividad());
        btnEliminar.addActionListener(e -> eliminarActividadSeleccionada());
        btnMarcar.addActionListener(e -> cambiarEstadoActividad());
        btnConfigurar.addActionListener(e -> editarActividadSeleccionada());

        getContentPane().add(btnAgregar);
        getContentPane().add(btnEliminar);
        getContentPane().add(btnMarcar);
        getContentPane().add(btnConfigurar);

        String[] columnas = {"Actividad", "Fecha", "Hora", "Tipo", "Estado", "Repeticiones", "Alerta"};
        modelo = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tabla = new JTable(modelo);
        tabla.setFont(font);
        tabla.setForeground(texto);
        tabla.setBackground(grisClaro);
        tabla.setRowHeight(30);
        tabla.setGridColor(naranja);
        tabla.setShowGrid(true);
        tabla.setSelectionBackground(new Color(80, 80, 80));
        tabla.setSelectionForeground(naranja);

        tabla.getTableHeader().setBackground(new Color(40, 40, 40));
        tabla.getTableHeader().setForeground(naranja);
        tabla.getTableHeader().setFont(font);
        tabla.setFillsViewportHeight(true);

        // Add MouseListener to JTable to handle deselection on empty area clicks
        // Añadir MouseListener a JTable para manejar la deselección al hacer clic en un área vacía
        tabla.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int row = tabla.rowAtPoint(e.getPoint());
                if (row == -1) {
                    Rectangle tableBounds = tabla.getBounds();
                    if (e.getX() >= 0 && e.getX() <= tableBounds.width &&
                        e.getY() >= 0 && e.getY() <= tableBounds.height) {
                        if (tabla.getTableHeader().getBounds().contains(e.getPoint())) {
                            return;
                        }
                        tabla.clearSelection();
                    }
                }
            }
        });

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < tabla.getColumnCount(); i++) {
            tabla.getColumnModel().getColumn(i).setCellRenderer(center);
        }

        DefaultTableCellRenderer customRenderer = new DefaultTableCellRenderer() {
            private final Font estadoFont = new Font("Segoe UI Emoji", Font.BOLD, 16);
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setForeground(texto);
                c.setBackground(grisClaro);
                setHorizontalAlignment(JLabel.CENTER);
                
                if (column == 4) {
                    setFont(estadoFont);
                    String estado = value != null ? value.toString() : "";
                    if ("✓".equals(estado)) {
                        setText("✓");
                        setForeground(Color.GREEN);
                    } else {
                        setText(estado);
                        if (isSelected) {
                            setForeground(naranja);
                        } else {
                            setForeground(texto);
                        }
                    }
                } else if (column == 6) {
                    setFont(estadoFont);
                    String alerta = value != null ? value.toString() : "";
                    if ("🔔".equals(alerta)) {
                        setText("🔔");
                        setForeground(Color.ORANGE);
                    } else {
                        setText("");
                        setForeground(texto);
                    }
                } else {
                    setFont(font);
                    setText(value != null ? value.toString() : "");
                    setForeground(texto);
                }
                
                if (isSelected) {
                    c.setBackground(new Color(80, 80, 80));
                    if (column != 4 && column != 6) setForeground(naranja);
                    setBorder(BorderFactory.createLineBorder(naranja, 2));
                } else if (hasFocus) {
                    setBorder(BorderFactory.createLineBorder(naranja, 2));
                } else {
                    setBorder(BorderFactory.createLineBorder(naranja, 1));
                }
                return c;
            }
        };
        for (int i = 0; i < tabla.getColumnCount(); i++) {
            tabla.getColumnModel().getColumn(i).setCellRenderer(customRenderer);
        }
        tabla.setDefaultRenderer(Object.class, customRenderer);

        tabla.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                lbl.setBackground(new Color(40, 40, 40));
                lbl.setForeground(naranja);
                lbl.setFont(font);
                lbl.setHorizontalAlignment(JLabel.CENTER);
                lbl.setBorder(BorderFactory.createLineBorder(naranja, 2));
                return lbl;
            }
        });

        scrollPane = new JScrollPane(tabla);
        scrollPane.setBounds(30, 130, 1140, 520);
        scrollPane.setBorder(BorderFactory.createLineBorder(naranja));
        getContentPane().add(scrollPane);

        // Reiniciar tareas al inicializar la aplicación
        gestorTareas.reiniciarTareasSiNecesario();
        actualizarTabla();
        
        // Timer para animación: actualiza solo tiempos cada segundo (eficiente)
        timerAnimacion = new Timer(1000, e -> actualizarSoloTiempos());
        timerAnimacion.setCoalesce(true);
        timerAnimacion.start();
        
        // Timer para datos: recarga toda la tabla cada 12 segundos
        timerActualizacion = new Timer(12000, e -> {
            // Reiniciar tareas diarias/semanales si es necesario
            gestorTareas.reiniciarTareasSiNecesario();
            actualizarTabla();
            // Garbage collection periódico para liberar memoria
            if (System.currentTimeMillis() % 60000 < 12000) {
                System.gc();
            }
        });
        timerActualizacion.setCoalesce(true);
        timerActualizacion.start();

        iniciarTimerDiarioAutoEliminacion();
        
        MouseAdapter deseleccionListener = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Component source = (Component) e.getSource();
                if (source != tabla && source != scrollPane &&
                    !(source == btnAgregar || source == btnEliminar || 
                      source == btnMarcar || source == btnConfigurar)) {
                    
                    boolean clickEnBotonAccion = false;
                    Component parent = source;
                    while(parent != null) {
                        if (parent == btnAgregar || parent == btnEliminar || 
                            parent == btnMarcar || parent == btnConfigurar) {
                            clickEnBotonAccion = true;
                            break;
                        }
                        parent = parent.getParent();
                    }

                    if (!clickEnBotonAccion) {
                        tabla.clearSelection();
                    }
                }
            }
        };
        
        this.addMouseListener(deseleccionListener);
        for (Component comp : getContentPane().getComponents()) {
            if (comp != scrollPane && comp != tabla) {
                comp.addMouseListener(deseleccionListener);
            }
        }
        
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                redimensionarComponentes();
            }
        });
        
        SwingUtilities.invokeLater(() -> redimensionarComponentes());
        
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                limpiarRecursos(); // Limpiar timers antes de cerrar
                if (sistemaBandeja != null) {
                    sistemaBandeja.minimizarABandeja();
                } else {
                    System.exit(0);
                }
            }
        });
    }
    
    private void redimensionarComponentes() {
        int width = getWidth();
        int height = getHeight();
        int titleBarHeight = 40;
        
        if (this.titleBarPanel != null) {
            this.titleBarPanel.setBounds(0, 0, width, titleBarHeight);

            if (this.btnImportExport != null) {
                this.btnImportExport.setBounds(15, (titleBarHeight - 28) / 2, 28, 28);
            }

            int ctrlButtonX = width - 40;
            if (this.btnCerrarVentana != null) {
                this.btnCerrarVentana.setBounds(ctrlButtonX, (titleBarHeight - 28) / 2, 28, 28);
                ctrlButtonX -= 34; 
            }
            if (this.btnMaximizarVentana != null) {
                this.btnMaximizarVentana.setBounds(ctrlButtonX, (titleBarHeight - 28) / 2, 28, 28);
                ctrlButtonX -= 34; 
            }
            if (this.btnMinimizarVentana != null) {
                this.btnMinimizarVentana.setBounds(ctrlButtonX, (titleBarHeight - 28) / 2, 28, 28);
            }
        }
        
        int buttonY = titleBarHeight + 30; 

        int buttonWidth = Math.min(250, (width - 80) / 4);
        int totalButtonsWidth = buttonWidth * 4;
        int availableSpace = width - 80;
        int spacing = Math.max(10, (availableSpace - totalButtonsWidth) / 3);
        
        if (totalButtonsWidth + spacing * 3 > availableSpace) {
            buttonWidth = (availableSpace - 30) / 4;
            spacing = 10;
        }
        
        int startX = 30; 
        
        btnAgregar.setBounds(startX, buttonY, buttonWidth, 45);
        btnEliminar.setBounds(startX + buttonWidth + spacing, buttonY, buttonWidth, 45);
        btnMarcar.setBounds(startX + 2 * (buttonWidth + spacing), buttonY, buttonWidth, 45);
        btnConfigurar.setBounds(startX + 3 * (buttonWidth + spacing), buttonY, buttonWidth, 45);
        
        int scrollPaneY = buttonY + 45 + 10; 
        for (Component comp : getContentPane().getComponents()) {
            if (comp == scrollPane) {
                comp.setBounds(30, scrollPaneY, width - 60, height - scrollPaneY - 30); 
                break;
            }
        }
    }

    private JButton crearBoton(String texto, int x, int y) {
        JButton btn = new JButton(texto);
        btn.setBounds(x, y, 250, 45);
        btn.setFocusPainted(false);
        btn.setBackground(fondo);
        btn.setForeground(naranja);
        btn.setFont(font);
        btn.setBorder(new RoundBorder(20));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBorder(new RoundBorder(20, Color.WHITE));
            }
            public void mouseExited(MouseEvent e) {
                btn.setBorder(new RoundBorder(20, naranja));
            }
        });
        return btn;
    }

    private void crearBotonesVentana() {
        this.titleBarPanel = new JPanel(null);
        this.titleBarPanel.setBounds(0, 0, getWidth(), 40); 
        this.titleBarPanel.setBackground(new Color(20, 20, 20));

        this.btnImportExport = new JButton("\\u2261");
        this.btnImportExport.setFont(new Font("Segoe UI Symbol", Font.BOLD, 16));
        this.btnImportExport.setForeground(naranja);
        this.btnImportExport.setBackground(new Color(20,20,20));
        this.btnImportExport.setBorder(new RoundBorder(8, naranja));
        this.btnImportExport.setFocusPainted(false);
        this.btnImportExport.setCursor(new Cursor(Cursor.HAND_CURSOR));
        this.btnImportExport.addActionListener(e -> mostrarMenuImportExport());
        this.btnImportExport.setBounds(15, (40 - 28) / 2, 28, 28); 
        this.titleBarPanel.add(this.btnImportExport);

        this.btnCerrarVentana = crearControlVentana(Color.RED, "cerrar");
        this.btnCerrarVentana.setBounds(getWidth() - 40, (40 - 28) / 2, 28, 28);
        this.btnCerrarVentana.addActionListener(e -> {
            if (sistemaBandeja != null) {
                 sistemaBandeja.minimizarABandeja();
            } else {
                System.exit(0);
            }
        });

        this.btnMaximizarVentana = crearControlVentana(naranja, "maximizar");
        this.btnMaximizarVentana.setBounds(getWidth() - 74, (40-28)/2, 28, 28);
        this.btnMaximizarVentana.addActionListener(e -> {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice gd = ge.getDefaultScreenDevice();
            Rectangle screenBounds = gd.getDefaultConfiguration().getBounds();
            
            if (getBounds().equals(screenBounds)) {
                setSize(1200, 700);
                setLocationRelativeTo(null);
            } else {
                setBounds(screenBounds);
            }
            redimensionarComponentes(); 
        });

        this.btnMinimizarVentana = crearControlVentana(Color.GRAY, "minimizar");
        this.btnMinimizarVentana.setBounds(getWidth() - 108, (40 - 28) / 2, 28, 28);
        this.btnMinimizarVentana.addActionListener(e -> setState(Frame.ICONIFIED));

        this.titleBarPanel.add(this.btnCerrarVentana);
        this.titleBarPanel.add(this.btnMaximizarVentana);
        this.titleBarPanel.add(this.btnMinimizarVentana);
        getContentPane().add(this.titleBarPanel);

        new WindowResizer(this);
        new ComponentMover(this, this.titleBarPanel);
    }

    private JButton crearControlVentana(Color color, String tipo) {
        Color iconColor = new Color(220, 220, 220);
        JButton btn = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.fillOval(0, 0, getWidth() - 1, getHeight() - 1);
                g2.setStroke(new BasicStroke(2.2f));
                g2.setColor(iconColor);
                int margin = 9;
                int w = getWidth(), h = getHeight();
                if ("cerrar".equals(tipo)) {
                    g2.drawLine(margin, margin, w - margin, h - margin);
                    g2.drawLine(w - margin, margin, margin, h - margin);
                } else if ("maximizar".equals(tipo)) {
                    g2.drawRect(margin, margin, w - 2 * margin, h - 2 * margin);
                } else if ("minimizar".equals(tipo)) {
                    g2.drawLine(margin, h / 2, w - margin, h / 2);
                }
                g2.dispose();
            }
        };
        btn.setBorderPainted(true);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new CircleBorder(new Color(0, 0, 0, 0)));
        
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBorder(new CircleBorder(Color.WHITE));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBorder(new CircleBorder(new Color(0, 0, 0, 0)));
            }
        });
        return btn;
    }

    private void abrirDialogoAgregarActividad() {
        Tarea nuevaTarea = DialogoAgregarTarea.mostrarDialogo(this);
        if (nuevaTarea != null) {
            gestorTareas.agregarTarea(nuevaTarea);
            actualizarTabla();
        }
    }

    private void eliminarActividadSeleccionada() {
        int filaSeleccionadaVista = tabla.getSelectedRow();
        if (filaSeleccionadaVista >= 0) {
            int modelRow = tabla.convertRowIndexToModel(filaSeleccionadaVista); 
            
            if (modelRow < tareasOrdenadasMostradas.size()) {
                Tarea tareaAEliminar = tareasOrdenadasMostradas.get(modelRow);
                String nombre = tareaAEliminar.getNombre();

                JDialog dialogo = new JDialog(this, "Confirmar eliminación", true);
                dialogo.setUndecorated(true);
                dialogo.setSize(400, 200);
                dialogo.setLocationRelativeTo(this);
                dialogo.getContentPane().setBackground(fondo);
                dialogo.setLayout(null);
                
                JLabel lblPregunta = new JLabel("<html><center>¿Está seguro de que desea eliminar<br>la actividad '" + nombre + "'?</center></html>");
                lblPregunta.setBounds(20, 40, 360, 60);
                lblPregunta.setFont(font);
                lblPregunta.setForeground(texto);
                lblPregunta.setHorizontalAlignment(JLabel.CENTER);
                
                JButton btnSi = new JButton("SI");
                btnSi.setBounds(80, 130, 100, 40);
                btnSi.setFont(font);
                btnSi.setBackground(fondo);
                btnSi.setForeground(naranja);
                btnSi.setBorder(new RoundBorder(20, naranja));
                btnSi.setFocusPainted(false);
                btnSi.setCursor(new Cursor(Cursor.HAND_CURSOR));
                
                JButton btnNo = new JButton("NO");
                btnNo.setBounds(220, 130, 100, 40);
                btnNo.setFont(font);
                btnNo.setBackground(fondo);
                btnNo.setForeground(new Color(180, 180, 180));
                btnNo.setBorder(new RoundBorder(20, new Color(180, 180, 180)));
                btnNo.setFocusPainted(false);
                btnNo.setCursor(new Cursor(Cursor.HAND_CURSOR));
                
                btnSi.addActionListener(e -> {
                    gestorTareas.eliminarTareaObjeto(tareaAEliminar); 
                    actualizarTabla();
                    dialogo.dispose();
                });
                
                btnNo.addActionListener(e -> dialogo.dispose());
                
                dialogo.add(lblPregunta);
                dialogo.add(btnSi);
                dialogo.add(btnNo);
                
                dialogo.getRootPane().setBorder(BorderFactory.createLineBorder(naranja, 2));
                
                dialogo.setVisible(true);

            }
        } else {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione una actividad para eliminar.", "Información", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void cambiarEstadoActividad() {
        int filaSeleccionadaVista = tabla.getSelectedRow();
        if (filaSeleccionadaVista >= 0) {
            int modelRow = tabla.convertRowIndexToModel(filaSeleccionadaVista);
            
            if (modelRow < tareasOrdenadasMostradas.size()) {
                Tarea tarea = tareasOrdenadasMostradas.get(modelRow);
                
                if (gestorTareas.actualizarEstadoTarea(tarea)) {
                    // Reiniciar tareas si es necesario después de cambiar el estado
                    gestorTareas.reiniciarTareasSiNecesario();
                    actualizarTabla();
                    
                    int nuevaVistaFila = -1;
                    for(int i=0; i < tareasOrdenadasMostradas.size(); i++){
                        if(tareasOrdenadasMostradas.get(i).equals(tarea)){ 
                            nuevaVistaFila = tabla.convertRowIndexToView(i); 
                            break;
                        }
                    }
                    if (nuevaVistaFila != -1 && nuevaVistaFila < tabla.getRowCount()) {
                        tabla.setRowSelectionInterval(nuevaVistaFila, nuevaVistaFila);
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Error al actualizar el estado de la tarea.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione una actividad para marcar.", "Información", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void iniciarTimerDiarioAutoEliminacion() {
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime medianoche = ahora.toLocalDate().plusDays(1).atStartOfDay();
        long delayInicial = ChronoUnit.MILLIS.between(ahora, medianoche);
        if (delayInicial < 0) delayInicial += 24 * 60 * 60 * 1000;

        timerDiarioAutoEliminacion = new Timer((int)delayInicial, e -> {
            eliminarTareasUnicasCompletadasAyer();
            ((Timer)e.getSource()).setInitialDelay(24 * 60 * 60 * 1000);
            ((Timer)e.getSource()).restart();
        });
        timerDiarioAutoEliminacion.setRepeats(false);
        timerDiarioAutoEliminacion.start();
    }

    private void eliminarTareasUnicasCompletadasAyer() {
        int tareasEliminadas = gestorTareas.eliminarTareasUnicasCompletadasAyer();
        if (tareasEliminadas > 0) {
            actualizarTabla();
            System.out.println(tareasEliminadas + " tareas únicas completadas ayer fueron eliminadas.");
        }
    }

    private void actualizarTabla() {
        int filaSeleccionadaPreviaEnVista = tabla.getSelectedRow();
        Tarea tareaSeleccionadaPreviamente = null;

        if (filaSeleccionadaPreviaEnVista != -1) {
            int modelRowPrevio = tabla.convertRowIndexToModel(filaSeleccionadaPreviaEnVista);
            if (this.tareasOrdenadasMostradas != null && modelRowPrevio >= 0 && modelRowPrevio < this.tareasOrdenadasMostradas.size()) {
                 tareaSeleccionadaPreviamente = this.tareasOrdenadasMostradas.get(modelRowPrevio);
            }
        }

        modelo.setRowCount(0);
        this.tareasOrdenadasMostradas = gestorTareas.obtenerTareasParaTabla();

        for (Tarea tarea : this.tareasOrdenadasMostradas) {
            String[] fila = gestorTareas.crearFilaTabla(tarea);
            modelo.addRow(fila);
        }
        
        if (tareaSeleccionadaPreviamente != null) {
            for (int i = 0; i < this.tareasOrdenadasMostradas.size(); i++) {
                if (this.tareasOrdenadasMostradas.get(i).equals(tareaSeleccionadaPreviamente)) { 
                    int viewRow = tabla.convertRowIndexToView(i); 
                    if (viewRow != -1) {
                        tabla.setRowSelectionInterval(viewRow, viewRow);
                    }
                    break;
                }
            }
        }
    }

    /**
     * Actualiza solo los tiempos restantes en la tabla sin recargar los datos.
     * Método optimizado para la animación segundo a segundo.
     */
    private void actualizarSoloTiempos() {
        if (this.tareasOrdenadasMostradas == null || modelo.getRowCount() == 0) {
            return;
        }
        
        // Solo actualizar la columna de estado (que contiene el tiempo restante)
        for (int i = 0; i < this.tareasOrdenadasMostradas.size() && i < modelo.getRowCount(); i++) {
            Tarea tarea = this.tareasOrdenadasMostradas.get(i);
            String[] filaActualizada = gestorTareas.crearFilaTabla(tarea);
            
            // Solo actualizar la columna del estado/tiempo restante (columna 4)
            if (filaActualizada.length > 4) {
                modelo.setValueAt(filaActualizada[4], i, 4);
            }
        }
    }

    private void mostrarMenuImportExport() {
        String[] opciones = {"Exportar Actividades", "Importar Actividades", "Cancelar"};
        int seleccion = JOptionPane.showOptionDialog(
            this,
            "¿Qué desea hacer?",
            "Importar/Exportar",
            JOptionPane.YES_NO_CANCEL_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            opciones,
            opciones[2]
        );
        
        switch (seleccion) {
            case 0: // Exportar
                exportarTareas();
                break;
            case 1: // Importar
                importarTareas();
                break;
            default:
                // Cancelar o cerrar diálogo
                break;
        }
    }
    
    private void editarActividadSeleccionada() {
        int filaSeleccionadaVista = tabla.getSelectedRow();
        if (filaSeleccionadaVista >= 0) {
            int modelRow = tabla.convertRowIndexToModel(filaSeleccionadaVista);
            if (modelRow >= 0 && modelRow < tareasOrdenadasMostradas.size()) {
                Tarea tareaExistente = tareasOrdenadasMostradas.get(modelRow); 
                Tarea tareaEditada = DialogoAgregarTarea.mostrarDialogoEdicion(this, tareaExistente);
                
                if (tareaEditada != null) {
                    if (gestorTareas.editarTarea(tareaExistente, tareaEditada)) {
                        actualizarTabla();
                    } else {
                        JOptionPane.showMessageDialog(this, "Error al editar la tarea.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo obtener la tarea seleccionada para editar. Intente de nuevo.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione una actividad para editar.", "Información", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void exportarTareas() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Exportar Actividades");
        fileChooser.setSelectedFile(new java.io.File("actividades_exportadas.json"));
        
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                String archivo = fileChooser.getSelectedFile().getAbsolutePath();
                if (!archivo.toLowerCase().endsWith(".json")) {
                    archivo += ".json";
                }
                gestorTareas.exportarTareas(archivo);
                JOptionPane.showMessageDialog(this, "Actividades exportadas exitosamente a: " + archivo, "Éxito", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al exportar actividades: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void importarTareas() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Importar Actividades");
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                String archivo = fileChooser.getSelectedFile().getAbsolutePath();
                gestorTareas.importarTareas(archivo);
                actualizarTabla();
                JOptionPane.showMessageDialog(this, "Actividades importadas exitosamente desde: " + archivo, "Éxito", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al importar actividades: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void mostrarVentana() {
        setVisible(true);
        setState(Frame.NORMAL); // Asegurarse de que la ventana no esté minimizada o iconificada
        toFront(); // Traer la ventana al frente
    }

    /**
     * Limpia los recursos (timers) antes de cerrar la aplicación.
     * Importante para evitar memory leaks.
     */
    private void limpiarRecursos() {
        if (timerActualizacion != null && timerActualizacion.isRunning()) {
            timerActualizacion.stop();
        }
        if (timerAnimacion != null && timerAnimacion.isRunning()) {
            timerAnimacion.stop();
        }
        if (timerDiarioAutoEliminacion != null && timerDiarioAutoEliminacion.isRunning()) {
            timerDiarioAutoEliminacion.stop();
        }
        
        // Liberar bloqueo de instancia única
        logica.InstanciaUnica.liberarBloqueo();
    }

    public static void main(String[] args) {
        // Verificar si ya hay una instancia ejecutándose
        if (!logica.InstanciaUnica.esPrimeraInstancia()) {
            // Ya hay una instancia ejecutándose, la señal para activarla ya se envió
            System.out.println("DeskAgenda ya está ejecutándose. Activando ventana existente...");
            System.exit(0);
            return;
        }
        
        // Esta es la primera instancia, configurar liberación automática del bloqueo
        logica.InstanciaUnica.configurarLiberacionAutomatica();
        
        SwingUtilities.invokeLater(() -> {
            VistaPrincipal ventana = new VistaPrincipal();
            
            // Configurar la ventana para el sistema de instancia única
            logica.InstanciaUnica.configurarVentanaPrincipal(ventana);
            logica.InstanciaUnica.configurarLiberacionAlCerrar(ventana);
            
            ventana.setVisible(true);
        });
    }
}
