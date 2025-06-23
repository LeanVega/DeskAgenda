package igu;

import logica.Tarea;
import logica.Tarea.TipoTarea;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.time.DayOfWeek;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.Document; 
import java.awt.Toolkit;

public class DialogoAgregarTarea extends JDialog {
    
    private static final long serialVersionUID = 1L;
    
    // Configurar UTF-8 de forma ligera para evitar problemas con acentos y símbolos
    static {
        System.setProperty("file.encoding", "UTF-8");
    }

    private JTextField txtNombre;
    private JComboBox<TipoTarea> cmbTipo; 
    private JSpinner spnFecha;
    private JSpinner spnHora;
    private JSpinner spnHorasAlerta, spnMinutosAlerta, spnSegundosAlerta; 
    private JCheckBox chkAlerta;
    private JCheckBox[] chkDiasSemana; 
    private JButton btnAceptar;
    private JButton btnCancelar;
    private Tarea tareaResultado;
    private JPanel panelAlerta; 

    private Font font = new Font("Segoe UI Semibold", Font.PLAIN, 14);
    private Color fondo = new Color(30, 30, 30);
    private Color texto = new Color(240, 240, 240);
    private Color naranja = new Color(255, 152, 0);
    private Color grisClaro = new Color(60, 60, 60);    public DialogoAgregarTarea(JFrame parent) {
        super(parent, "Agregar Nueva Actividad", true);
        initComponents();
        applyNewTaskDefaults();
    }

    public DialogoAgregarTarea(JFrame parent, Tarea tareaExistente) {
        super(parent, "Editar Actividad", true);
        initComponents();
        cargarDatosTarea(tareaExistente);
    }

    private void initComponents() {
        setSize(500, 650); 
        setLocationRelativeTo(getParent());
        setUndecorated(true);
        getContentPane().setBackground(fondo);
        setLayout(null);

        JLabel lblNombre = crearLabel("Nombre de la actividad:", 30, 30);
        txtNombre = crearTextField(30, 60, 440);

        JLabel lblTipo = crearLabel("Tipo de actividad:", 30, 110);
        cmbTipo = new JComboBox<>(TipoTarea.values()); 
        cmbTipo.setBounds(30, 140, 200, 35);
        cmbTipo.setFont(font);
        cmbTipo.setBackground(grisClaro);
        cmbTipo.setForeground(texto);        cmbTipo.setBorder(BorderFactory.createLineBorder(naranja));
        cmbTipo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (isSelected) {
                    setBackground(naranja);
                    setForeground(fondo);
                } else {
                    setBackground(grisClaro);
                    setForeground(texto);
                }
                setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
                return this;
            }
        });        SwingUtilities.invokeLater(() -> {
            Object popup = cmbTipo.getUI().getAccessibleChild(cmbTipo, 0);
            if (popup instanceof JPopupMenu) {
                JPopupMenu p = (JPopupMenu) popup;
                p.setBackground(grisClaro);
                p.setBorder(BorderFactory.createLineBorder(naranja, 1));
                Component[] components = p.getComponents();
                for (Component comp : components) {
                    if (comp instanceof JScrollPane) {
                        JScrollPane scrollPane = (JScrollPane) comp;
                        JList<?> listInScrollPane = (JList<?>)scrollPane.getViewport().getView();
                        listInScrollPane.setBackground(grisClaro);
                        listInScrollPane.setSelectionBackground(naranja);
                        listInScrollPane.setSelectionForeground(fondo);
                        scrollPane.setBorder(BorderFactory.createEmptyBorder());
                    } else if (comp instanceof JList) { 
                        ((JList<?>)comp).setBackground(grisClaro);
                        ((JList<?>)comp).setSelectionBackground(naranja);
                        ((JList<?>)comp).setSelectionForeground(fondo);
                    }
                }
            }
        });

        JLabel lblFecha = crearLabel("Fecha:", 30, 190);
        spnFecha = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor editorFecha = new JSpinner.DateEditor(spnFecha, "dd-MM-yyyy");
        spnFecha.setEditor(editorFecha);
        spnFecha.setBounds(30, 220, 200, 35);
        configurarEditorSpinner(editorFecha.getTextField(), spnFecha, true);
        configurarNavegacionFecha(editorFecha.getTextField());        JLabel lblHora = crearLabel("Hora:", 270, 190);
        
        spnHora = new JSpinner(new SpinnerDateModel()); 
        JSpinner.DateEditor editorHora = new JSpinner.DateEditor(spnHora, "HH:mm");
        spnHora.setEditor(editorHora);
        spnHora.setBounds(270, 220, 150, 35);
        configurarEditorSpinner(editorHora.getTextField(), spnHora, false);
        configurarNavegacionHora(editorHora.getTextField());
        
        chkAlerta = new JCheckBox("Activar alerta");
        chkAlerta.setBounds(30, 270, 150, 25); 
        chkAlerta.setFont(font);
        chkAlerta.setBackground(fondo);
        chkAlerta.setForeground(texto);
        chkAlerta.setFocusPainted(false);

        panelAlerta = new JPanel(null);
        panelAlerta.setBounds(30, 300, 440, 70); 
        panelAlerta.setBackground(fondo);
        panelAlerta.setVisible(false);

        JLabel lblAlertaAntes = crearLabel("Alertar antes:", 0, 0);
        panelAlerta.add(lblAlertaAntes);

        SpinnerModel smHoras = new SpinnerNumberModel(0, 0, 23, 1);
        spnHorasAlerta = new JSpinner(smHoras);
        configurarSpinnerTiempoAlerta(spnHorasAlerta, "H", 0, 30);

        SpinnerModel smMinutos = new SpinnerNumberModel(0, 0, 59, 1);
        spnMinutosAlerta = new JSpinner(smMinutos);
        configurarSpinnerTiempoAlerta(spnMinutosAlerta, "M", 100, 30);

        SpinnerModel smSegundos = new SpinnerNumberModel(0, 0, 59, 1);
        spnSegundosAlerta = new JSpinner(smSegundos);
        configurarSpinnerTiempoAlerta(spnSegundosAlerta, "S", 200, 30);
        
        panelAlerta.add(spnHorasAlerta);
        panelAlerta.add(crearLabelUnidad("H", 70, 30));
        panelAlerta.add(spnMinutosAlerta);
        panelAlerta.add(crearLabelUnidad("M", 170, 30));
        panelAlerta.add(spnSegundosAlerta);
        panelAlerta.add(crearLabelUnidad("S", 270, 30));

        JLabel lblDias = crearLabel("Días de la semana:", 30, 380); 
        chkDiasSemana = new JCheckBox[7];
        String[] nombresDias = {"Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"};
        for (int i = 0; i < 7; i++) {
            chkDiasSemana[i] = new JCheckBox(nombresDias[i]);
            chkDiasSemana[i].setBounds(30 + (i % 2) * 220, 410 + (i / 2) * 30, 180, 25); 
            chkDiasSemana[i].setFont(new Font("Segoe UI", Font.PLAIN, 12));
            chkDiasSemana[i].setBackground(fondo);
            chkDiasSemana[i].setForeground(texto);
            chkDiasSemana[i].setFocusPainted(false);
            chkDiasSemana[i].setVisible(false);
        }
        btnAceptar = crearBoton("ACEPTAR", 80, 570, naranja); 
        btnCancelar = crearBoton("CANCELAR", 280, 570, new Color(180, 180, 180)); 

        add(lblNombre); add(txtNombre);
        add(lblTipo); add(cmbTipo);
        add(lblFecha); add(spnFecha);
        add(lblHora); add(spnHora);
        add(chkAlerta); add(panelAlerta);
        add(lblDias);
        for (JCheckBox chk : chkDiasSemana) add(chk);
        add(btnAceptar); add(btnCancelar);        getRootPane().setBorder(BorderFactory.createLineBorder(naranja, 2));
        
        setupEvents();
    }
    
    private JLabel crearLabel(String texto, int x, int y) {
        JLabel lbl = new JLabel(texto);
        lbl.setBounds(x, y, 200, 25);
        lbl.setFont(font);
        lbl.setForeground(this.texto);
        return lbl;
    }

    private JTextField crearTextField(int x, int y, int width) {
        JTextField txt = new JTextField();
        txt.setBounds(x, y, width, 35);
        txt.setFont(font);
        txt.setBackground(grisClaro);
        txt.setForeground(texto);
        txt.setBorder(BorderFactory.createLineBorder(naranja));
        txt.setCaretColor(texto);
        return txt;
    }
    
    private JButton crearBoton(String texto, int x, int y, Color color) {
        JButton btn = new JButton(texto);
        btn.setBounds(x, y, 140, 40); 
        btn.setFont(new Font("Segoe UI Semibold", Font.BOLD, 16));
        btn.setBackground(fondo);
        btn.setForeground(color);
        btn.setBorder(new igu.util.RoundBorder(20, color));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBorder(new igu.util.RoundBorder(20, Color.WHITE));
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBorder(new igu.util.RoundBorder(20, color));
            }
        });
        return btn;
    }
      private void applyNewTaskDefaults() {
        spnFecha.setValue(new java.util.Date());

        java.util.Calendar calDefaultHora = java.util.Calendar.getInstance();
        calDefaultHora.set(java.util.Calendar.HOUR_OF_DAY, 23);
        calDefaultHora.set(java.util.Calendar.MINUTE, 59);
        calDefaultHora.set(java.util.Calendar.SECOND, 0);
        calDefaultHora.set(java.util.Calendar.MILLISECOND, 0);
        spnHora.setValue(calDefaultHora.getTime());
        spnHora.setEnabled(true);
    }

    private void setupEvents() {        cmbTipo.addActionListener(e -> {
            TipoTarea tipo = (TipoTarea) cmbTipo.getSelectedItem();
            boolean mostrarDias = (tipo == TipoTarea.SEMANAL);
            for (JCheckBox chk : chkDiasSemana) {
                chk.setVisible(mostrarDias);
            }
        });

        btnAceptar.addActionListener(e -> {
            if (validarDatos()) {
                crearTarea();
                dispose();
            }
        });

        btnCancelar.addActionListener(e -> {
            tareaResultado = null;
            dispose();
        });

        KeyStroke escapeKeyStroke = KeyStroke.getKeyStroke("ESCAPE");
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escapeKeyStroke, "ESCAPE");
        getRootPane().getActionMap().put("ESCAPE", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) { 
                btnCancelar.doClick();
            }
        });
        chkAlerta.addActionListener(e -> panelAlerta.setVisible(chkAlerta.isSelected())); 
    }
    
    private boolean validarDatos() {
        if (txtNombre.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "El nombre de la actividad es obligatorio.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (!validarFecha()) return false;
        if (!validarHora()) return false;
        TipoTarea tipo = (TipoTarea) cmbTipo.getSelectedItem();
        if (tipo == TipoTarea.SEMANAL) {
            boolean alMenosUnDia = false;
            for (JCheckBox chk : chkDiasSemana) {
                if (chk.isSelected()) {
                    alMenosUnDia = true;
                    break;
                }
            }
            if (!alMenosUnDia) {
                JOptionPane.showMessageDialog(this, "Para actividades semanales debe seleccionar al menos un día.", "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        return true;
    }    private boolean validarFecha() {
        try {
            JSpinner.DateEditor editor = (JSpinner.DateEditor) spnFecha.getEditor();
            String textoFecha = editor.getTextField().getText();
            if (textoFecha.trim().isEmpty() || textoFecha.contains("_")) { 
                if (textoFecha.contains("_")) {
                    JOptionPane.showMessageDialog(this, "Por favor complete la fecha (DD-MM-YYYY).", "Error de Formato", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            }

            try {
                spnFecha.commitEdit();
            } catch (java.text.ParseException pe) {
                JOptionPane.showMessageDialog(this, "Formato de fecha inválido. Use DD-MM-YYYY.\\nDetalle: " + pe.getMessage(), "Error de Formato", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            
            java.util.Date utilDate = (java.util.Date) spnFecha.getValue();
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.setTime(utilDate);            
            int dia = cal.get(java.util.Calendar.DAY_OF_MONTH);
            int mes = cal.get(java.util.Calendar.MONTH) + 1;
            int anio = cal.get(java.util.Calendar.YEAR);            if (anio < 2000 || anio > 2099) { 
                JOptionPane.showMessageDialog(this, "Año debe estar entre 2000 y 2099.", "Error de Año", JOptionPane.ERROR_MESSAGE);
                return false; 
            }
            if (mes < 1 || mes > 12) { 
                JOptionPane.showMessageDialog(this, "Mes inválido (01-12).", "Error de Mes", JOptionPane.ERROR_MESSAGE); 
                return false; 
            }
            if (dia < 1 || dia > cal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)) {
                 JOptionPane.showMessageDialog(this, "Día inválido para el mes y año seleccionados.", "Error de Día", JOptionPane.ERROR_MESSAGE); 
                 return false; 
            }
            return true;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error en el formato o valor de fecha.", "Error de Fecha", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }    private boolean validarHora() {
        try {
             try {
                spnHora.commitEdit();
            } catch (java.text.ParseException pe) {
                JOptionPane.showMessageDialog(this, "Formato de hora inválido. Use HH:MM (formato 24 horas).\\nDetalle: " + pe.getMessage(), "Error de Formato", JOptionPane.ERROR_MESSAGE);
                return false;
            }

            java.util.Date horaDate = (java.util.Date) spnHora.getValue();
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.setTime(horaDate);
            int hora = cal.get(java.util.Calendar.HOUR_OF_DAY);
            int minutos = cal.get(java.util.Calendar.MINUTE);

            if (hora < 0 || hora > 23) {
                JOptionPane.showMessageDialog(this, "Hora inválida (debe ser entre 00 y 23).", "Error de Hora", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            if (minutos < 0 || minutos > 59) {
                JOptionPane.showMessageDialog(this, "Minutos inválidos (debe ser entre 00 y 59).", "Error de Minutos", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            return true;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error en el formato o valor de hora. Use HH:MM (formato 24 horas).", "Error de Hora", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private void crearTarea() {
        String nombre = txtNombre.getText().trim();
        Tarea.TipoTarea tipo = (Tarea.TipoTarea) cmbTipo.getSelectedItem();
        java.util.Date fechaUtil = (java.util.Date) spnFecha.getValue();
        java.util.Date horaUtil = (java.util.Date) spnHora.getValue();
        LocalDate fecha = fechaUtil.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        LocalTime hora = horaUtil.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalTime();

        List<DayOfWeek> diasSeleccionados = new ArrayList<>();
        if (tipo == Tarea.TipoTarea.SEMANAL) {
            for (int i = 0; i < chkDiasSemana.length; i++) {
                if (chkDiasSemana[i].isSelected()) {
                    diasSeleccionados.add(DayOfWeek.of(i + 1)); 
                }
            }
        }
        tareaResultado = new Tarea(nombre, fecha, hora, tipo);
        if (tipo == Tarea.TipoTarea.SEMANAL) {
            tareaResultado.setDiasSemana(new java.util.HashSet<>(diasSeleccionados));
        }
        tareaResultado.setAlertaActiva(chkAlerta.isSelected());
        if (chkAlerta.isSelected()) {
            int horasAntes = (Integer) spnHorasAlerta.getValue();
            int minutosAntes = (Integer) spnMinutosAlerta.getValue();
            int segundosAntes = (Integer) spnSegundosAlerta.getValue();
            // Convertir todo a segundos totales
            int totalSegundosAntes = (horasAntes * 3600) + (minutosAntes * 60) + segundosAntes; 
            tareaResultado.setSegundosAntesAlerta(totalSegundosAntes);
        }
    }
      private void cargarDatosTarea(Tarea tarea) {
        if (tarea == null) return;
        txtNombre.setText(tarea.getDescripcion());
        cmbTipo.setSelectedItem(tarea.getTipo());
        
        java.util.Date fechaDate = java.util.Date.from(tarea.getFecha().atStartOfDay(java.time.ZoneId.systemDefault()).toInstant());
        spnFecha.setValue(fechaDate);

        java.util.Calendar calHora = java.util.Calendar.getInstance();
        calHora.clear(); 
        calHora.set(java.util.Calendar.HOUR_OF_DAY, tarea.getHora().getHour());
        calHora.set(java.util.Calendar.MINUTE, tarea.getHora().getMinute());
        
        spnHora.setValue(calHora.getTime());
        spnHora.setEnabled(true);

        chkAlerta.setSelected(tarea.isAlertaActiva());
        panelAlerta.setVisible(tarea.isAlertaActiva());
        if (tarea.isAlertaActiva()) {
            int totalSegundos = tarea.getSegundosAntesAlerta();
            spnHorasAlerta.setValue(totalSegundos / 3600);
            spnMinutosAlerta.setValue((totalSegundos % 3600) / 60);
            spnSegundosAlerta.setValue(totalSegundos % 60);
        }
        if (tarea.getTipo() == Tarea.TipoTarea.SEMANAL && tarea.getDiasSemana() != null) {
            for (DayOfWeek dow : tarea.getDiasSemana()) {
                chkDiasSemana[dow.getValue() - 1].setSelected(true);
            }
        }
        cmbTipo.getActionListeners()[0].actionPerformed(null); 
    }    private void configurarSpinnerTiempoAlerta(JSpinner spinner, String unidad, int x, int y) {
        spinner.setBounds(x, y, 60, 30);
        spinner.setFont(font);
        spinner.setBorder(BorderFactory.createLineBorder(naranja));

        JComponent editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            JFormattedTextField tf = ((JSpinner.DefaultEditor) editor).getTextField();
            tf.setFont(font);
            tf.setBackground(grisClaro);
            tf.setForeground(texto);
            tf.setBorder(BorderFactory.createLineBorder(naranja)); 
            tf.setHorizontalAlignment(JTextField.CENTER);
            tf.setOpaque(true);
            tf.setCaretColor(texto);

            editor.setBackground(grisClaro); 
        }

        for (Component comp : spinner.getComponents()) {
            if (comp instanceof JButton) {
                JButton button = (JButton) comp;
                button.setBackground(grisClaro);
                button.setForeground(naranja);
                button.setBorder(BorderFactory.createLineBorder(naranja, 1));
                button.setFocusPainted(false);
            }
        }
    }

    private JLabel crearLabelUnidad(String textoUnidad, int x, int y) {
        JLabel lbl = new JLabel(textoUnidad);
        lbl.setBounds(x, y, 20, 30);
        lbl.setFont(font);
        lbl.setForeground(this.texto);
        return lbl;
    }

    private void configurarEditorSpinner(JFormattedTextField textField, JSpinner spinner, boolean isDate) {
        textField.setBackground(grisClaro);
        textField.setForeground(texto);
        textField.setBorder(BorderFactory.createLineBorder(naranja)); 
        textField.setFont(font);
        textField.setCaretColor(texto);
        textField.setOpaque(true); 

        spinner.setBackground(fondo); 
        spinner.setBorder(BorderFactory.createLineBorder(naranja)); 

        JComponent editorComponent = spinner.getEditor();
        if (editorComponent instanceof JSpinner.DefaultEditor) {
            ((JSpinner.DefaultEditor) editorComponent).getTextField().setBackground(grisClaro);
            editorComponent.setBackground(grisClaro); 
        }
        
        for (Component comp : spinner.getComponents()) {
            if (comp instanceof JButton) { 
                JButton button = (JButton) comp;
                button.setBackground(grisClaro);
                button.setForeground(naranja); 
                button.setBorder(BorderFactory.createLineBorder(naranja, 1));
                button.setFocusPainted(false);
            }
        }
    }    private void configurarNavegacionFecha(JFormattedTextField textField) {
        ((AbstractDocument) textField.getDocument()).setDocumentFilter(new FixedCharFilter(textField, new int[]{2, 5}, '-', true, 10)); 
        textField.addKeyListener(new AutoSkipKeyListener(textField, new int[]{1, 4}, new int[]{3, 6}));
        textField.addFocusListener(new SelectOnFocusListener(textField, 0, 2)); 
    }

    private void configurarNavegacionHora(JFormattedTextField textField) {
        ((AbstractDocument) textField.getDocument()).setDocumentFilter(new FixedCharFilter(textField, new int[]{2}, ':', false, 5));
        textField.addKeyListener(new AutoSkipKeyListener(textField, new int[]{1}, new int[]{3}));
        textField.addFocusListener(new SelectOnFocusListener(textField, 0, 2)); 
    }

    // Para el input de JSpinner
    class FixedCharFilter extends DocumentFilter {
        private int[] positions;
        private char[] fixedChars;
        private boolean isDateField; 
        private int maxLength;

        public FixedCharFilter(JFormattedTextField textField, int[] positions, char fixedChar, boolean isDateField, int maxLength) {
            this(textField, positions, new char[]{fixedChar}, isDateField, maxLength);
        }

        public FixedCharFilter(JFormattedTextField textField, int[] positions, char[] fixedChars, boolean isDateField, int maxLength) {
            this.positions = positions;
            this.fixedChars = fixedChars;
            this.isDateField = isDateField;
            this.maxLength = maxLength;
        }        private boolean isValidCharForPos(char c, int posInProposedText, String proposedText) {
            if (isFixedPosition(posInProposedText)) {
                return c == getFixedCharForPosition(posInProposedText);
            }
        
            if (isDateField) {
                if (posInProposedText == 0 || posInProposedText == 1 || 
                    posInProposedText == 3 || posInProposedText == 4 ||
                    posInProposedText == 6 || posInProposedText == 7 || 
                    posInProposedText == 8 || posInProposedText == 9) {
                    return Character.isDigit(c);
                }
            } else {
                if (posInProposedText == 0 || posInProposedText == 1 || 
                    posInProposedText == 3 || posInProposedText == 4) {
                    return Character.isDigit(c);
                }
            }
            return false; 
        }
        
        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
            if (string == null) return;
            Document doc = fb.getDocument();
            String currentText = doc.getText(0, doc.getLength());
            
            String proposedText = currentText.substring(0, offset) + string + currentText.substring(offset);

            if (proposedText.length() > maxLength) {
                Toolkit.getDefaultToolkit().beep();
                return;
            }            for (int i = 0; i < proposedText.length(); i++) {
                char c = proposedText.charAt(i);
                if (!isValidCharForPos(c, i, proposedText)) {
                    Toolkit.getDefaultToolkit().beep();
                    return;
                }
            }
            super.insertString(fb, offset, string, attr);
        }

        @Override
        public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
            Document doc = fb.getDocument();
            String currentText = doc.getText(0, doc.getLength());            for (int i = 0; i < length; i++) {
                int charToRemovePos = offset + i;
                if (isFixedPosition(charToRemovePos) && currentText.charAt(charToRemovePos) == getFixedCharForPosition(charToRemovePos)) {
                    Toolkit.getDefaultToolkit().beep();
                    return;
                }
            }
            super.remove(fb, offset, length);
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
            if (text == null) text = "";
            Document doc = fb.getDocument();
            String currentText = doc.getText(0, doc.getLength());

            String proposedText = currentText.substring(0, offset) + text + currentText.substring(offset + length);

            if (proposedText.length() > maxLength) {
                Toolkit.getDefaultToolkit().beep();
                return;
            }
              for (int i = 0; i < length; i++) {
                int docPosBeingReplaced = offset + i;
                if (isFixedPosition(docPosBeingReplaced) && currentText.charAt(docPosBeingReplaced) == getFixedCharForPosition(docPosBeingReplaced)) {
                    if (i < text.length()) { 
                        if (text.charAt(i) != getFixedCharForPosition(docPosBeingReplaced)) {
                            Toolkit.getDefaultToolkit().beep(); 
                            return;
                        }
                    } else { 
                        Toolkit.getDefaultToolkit().beep();
                        return;
                    }
                }
            }
            
            for (int i = 0; i < proposedText.length(); i++) {
                char charInProposed = proposedText.charAt(i);
                 if (!isValidCharForPos(charInProposed, i, proposedText)) {
                    Toolkit.getDefaultToolkit().beep();
                    return;
                }
            }
            super.replace(fb, offset, length, text, attrs);
        }

        private boolean isFixedPosition(int pos) {
            for (int p : positions) {
                if (pos == p) return true;
            }
            return false;
        }
        private char getFixedCharForPosition(int pos){
            for(int i=0; i<positions.length; i++){
                if(positions[i] == pos) return (i < fixedChars.length ? fixedChars[i] : '\0');
            }
            return '\0'; 
        }
    }

    class AutoSkipKeyListener extends java.awt.event.KeyAdapter {
        private JFormattedTextField textField;
        private int[] skipAtSegmentEndPositions; // e.g., for DD-MM-YYYY, these are 1, 4 (after D|D, after M|M)
        private int[] jumpToPositions;           // e.g., for DD-MM-YYYY, these are 3, 6 (to M|M, to Y|YYY)

        public AutoSkipKeyListener(JFormattedTextField textField, int[] skipAtSegmentEndPositions, int[] jumpToPositions) {
            this.textField = textField;
            this.skipAtSegmentEndPositions = skipAtSegmentEndPositions; // Indices of the LAST char of a segment
            this.jumpToPositions = jumpToPositions;                     // Indices of the FIRST char of the NEXT segment
        }

        // Helper
        private boolean isFixedPositionInferred(int pos, boolean isDate, String text) {
            if (isDate) { // DD-MM-YYYY
                return pos == 2 || pos == 5;
            } else { // hh:mm AM/PM
                // Separadores en 2 (:), 5
                return pos == 2 || pos == 5;
            }
        }

        @Override
        public void keyTyped(java.awt.event.KeyEvent e) {
            char typedChar = e.getKeyChar();

            boolean isCurrentFieldDateType; // Renamed from isDateField for clarity
            Document doc = textField.getDocument();
            FixedCharFilter fixedCharFilter = null;
            if (doc instanceof AbstractDocument) {
                DocumentFilter currentDocFilter = ((AbstractDocument) doc).getDocumentFilter();
                if (currentDocFilter instanceof FixedCharFilter) {
                    fixedCharFilter = (FixedCharFilter) currentDocFilter;
                }
            }

            if (fixedCharFilter != null) {
                isCurrentFieldDateType = fixedCharFilter.isDateField;
            } else {
                // No debería pasar
                isCurrentFieldDateType = textField.getText().contains("-"); 
            }

            // Validamos
            if (isCurrentFieldDateType) { 
                if (!Character.isDigit(typedChar) && typedChar != '-' && 
                    typedChar != java.awt.event.KeyEvent.VK_BACK_SPACE && 
                    typedChar != java.awt.event.KeyEvent.VK_DELETE &&
                    !e.isControlDown()) {
                    e.consume();
                    return;
                }
            } else { //Lo dejo así?
                if (!Character.isDigit(typedChar) && typedChar != ':' &&
                    typedChar != java.awt.event.KeyEvent.VK_BACK_SPACE && 
                    typedChar != java.awt.event.KeyEvent.VK_DELETE &&
                    !e.isControlDown()) {
                    e.consume();
                    return;
                }
            }

            SwingUtilities.invokeLater(() -> {
                String textAfterKeyTyped = textField.getText();
                int caretPosAfterKeyTyped = textField.getCaretPosition();

                
                FixedCharFilter currentFixedCharFilter = null;
                Document currentDoc = textField.getDocument();
                if (currentDoc instanceof AbstractDocument) {
                    DocumentFilter docFilter = ((AbstractDocument) currentDoc).getDocumentFilter();
                    if (docFilter instanceof FixedCharFilter) {
                        currentFixedCharFilter = (FixedCharFilter) docFilter;
                    }
                }
                boolean finalIsDateFieldType = (currentFixedCharFilter != null) ? 
                                                currentFixedCharFilter.isDateField : 
                                                textField.getText().contains("-");

                for (int i = 0; i < skipAtSegmentEndPositions.length; i++) {
                    if (caretPosAfterKeyTyped == skipAtSegmentEndPositions[i] + 1) {
                        int segmentStart = (i == 0) ? 0 : jumpToPositions[i-1];
                        int segmentEnd = skipAtSegmentEndPositions[i] + 1;

                        if (segmentEnd > textAfterKeyTyped.length()) continue;
                        String segment = textAfterKeyTyped.substring(segmentStart, segmentEnd);
                        
                        boolean segmentFilledAndValid = true;
                        if (segment.length() != (segmentEnd - segmentStart)) {
                            segmentFilledAndValid = false;
                        } else {
                            for (int charIdx = 0; charIdx < segment.length(); charIdx++) {
                                char cInSegment = segment.charAt(charIdx);
                                int docPos = segmentStart + charIdx;
                                

                                boolean isFixedPos = (currentFixedCharFilter != null) ? 
                                                     currentFixedCharFilter.isFixedPosition(docPos) :
                                                     isFixedPositionInferred(docPos, finalIsDateFieldType, textAfterKeyTyped);

                                if (!isFixedPos) {
                                    if (finalIsDateFieldType) { 
                                        if (!Character.isDigit(cInSegment)) { segmentFilledAndValid = false; break; }
                                    } else { 
                                        if (!Character.isDigit(cInSegment)) { segmentFilledAndValid = false; break; }
                                    }
                                }
                            }
                        }

                        if (segmentFilledAndValid && jumpToPositions[i] <= textAfterKeyTyped.length()) { 
                            textField.setCaretPosition(jumpToPositions[i]);
                            
                            int nextSegmentStart = jumpToPositions[i];
                            int nextSegmentEnd = textAfterKeyTyped.length(); 
                            if (i + 1 < skipAtSegmentEndPositions.length) { 
                                 nextSegmentEnd = jumpToPositions[i+1]-1;
                            } else { 

                                int tempNextSegEnd = nextSegmentStart;
                                while(tempNextSegEnd < textAfterKeyTyped.length()) {
                                    boolean isFixed = (currentFixedCharFilter != null) ? 
                                                      currentFixedCharFilter.isFixedPosition(tempNextSegEnd) :
                                                      isFixedPositionInferred(tempNextSegEnd, finalIsDateFieldType, textAfterKeyTyped);
                                    if (isFixed) break;
                                    tempNextSegEnd++;
                                }
                                nextSegmentEnd = tempNextSegEnd;
                            }
                            nextSegmentEnd = Math.min(nextSegmentEnd, textAfterKeyTyped.length());

                            if (nextSegmentStart < nextSegmentEnd) {
                                textField.setSelectionStart(nextSegmentStart);
                                textField.setSelectionEnd(nextSegmentEnd);
                            } else if (nextSegmentStart == nextSegmentEnd && nextSegmentStart < textAfterKeyTyped.length()){
                                textField.setCaretPosition(nextSegmentStart);
                            } else if (nextSegmentStart >= textAfterKeyTyped.length()){
                                textField.setCaretPosition(textAfterKeyTyped.length());
                            }
                            break; 
                        }
                    }
                }
                
            });
        }
    }
    
    class SelectOnFocusListener extends java.awt.event.FocusAdapter {
        private JFormattedTextField textField;
        private int selectionStart;
        private int selectionEnd;

        public SelectOnFocusListener(JFormattedTextField textField, int selectionStart, int selectionEnd) {
            this.textField = textField;
            this.selectionStart = selectionStart;
            this.selectionEnd = selectionEnd;
        }

        @Override
        public void focusGained(java.awt.event.FocusEvent e) {
            SwingUtilities.invokeLater(() -> {
                textField.setSelectionStart(selectionStart);
                textField.setSelectionEnd(selectionEnd);
            });
        }
    }

    public static Tarea mostrarDialogo(JFrame parent) {
        DialogoAgregarTarea dialogo = new DialogoAgregarTarea(parent);
        dialogo.setVisible(true);
        return dialogo.tareaResultado;
    }

    public static Tarea mostrarDialogoEdicion(JFrame parent, Tarea tareaExistente) {
        DialogoAgregarTarea dialogo = new DialogoAgregarTarea(parent, tareaExistente);
        dialogo.setVisible(true);
        return dialogo.tareaResultado;
    }
}
//AAAAAAAAAAAAAAAAAAA