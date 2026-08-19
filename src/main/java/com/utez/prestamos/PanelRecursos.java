package com.utez.prestamos;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

public class PanelRecursos extends PanelBase {

    private final DefaultTableModel modelo = new DefaultTableModel(
            new String[]{"ID", "Descripción", "Serie", "Tipo", "Estado"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
        @Override public Class<?> getColumnClass(int column) {
            return column == 0 ? Integer.class : String.class;
        }
    };
    private final JTable tabla = new JTable(modelo);
    private final TableRowSorter<DefaultTableModel> ordenador = new TableRowSorter<>(modelo);
    private final JTextField buscar = Estilo.campoBusqueda("Buscar por ID / Nombre / Serie");
    private final JComboBox<String> tipos = new JComboBox<>(new String[]{"TODOS LOS TIPOS", "EQUIPO", "ACCESORIO", "HERRAMIENTA"});

    public PanelRecursos(VentanaPrincipal ventana, Repositorio repositorio) {
        super(ventana, repositorio);

        // Botones de acción
        JButton agregar = Estilo.botonPrincipal("+ Agregar recurso");
        JButton eliminar = Estilo.botonPrincipal("- Eliminar recurso");

        // Agrupamos los botones en el encabezado
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panelBotones.setOpaque(false);
        panelBotones.add(eliminar);
        panelBotones.add(agregar);

        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.setOpaque(false);
        encabezado.add(titulo("Gestión de recursos"), BorderLayout.WEST);
        encabezado.add(panelBotones, BorderLayout.EAST);

        buscar.setPreferredSize(new Dimension(330, 38));
        tipos.setPreferredSize(new Dimension(190, 38));
        JPanel filtros = new JPanel(new BorderLayout(12, 0));
        filtros.setOpaque(false);
        filtros.add(buscar, BorderLayout.WEST);
        filtros.add(tipos, BorderLayout.EAST);

        Estilo.prepararTabla(tabla);
        tabla.setRowSorter(ordenador);
        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(215, 218, 212)));
        JPanel centro = new JPanel(new BorderLayout(0, 14));
        centro.setOpaque(false);
        centro.add(filtros, BorderLayout.NORTH);
        centro.add(scroll, BorderLayout.CENTER);

        // Eventos
        agregar.addActionListener(e -> new DialogoRecurso(ventana, repositorio).setVisible(true));
        eliminar.addActionListener(e -> eliminarRecursoSeleccionado());

        buscar.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filtrar(); }
            public void removeUpdate(DocumentEvent e) { filtrar(); }
            public void changedUpdate(DocumentEvent e) { filtrar(); }
        });
        tipos.addActionListener(e -> filtrar());

        add(encabezado, BorderLayout.NORTH);
        add(centro, BorderLayout.CENTER);
    }

    private void eliminarRecursoSeleccionado() {
        int filaVista = tabla.getSelectedRow();

        // 1. Validar que se seleccionó una fila
        if (filaVista == -1) {
            JOptionPane.showMessageDialog(this, "Por favor, selecciona un recurso de la tabla para eliminar.", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int filaModelo = tabla.convertRowIndexToModel(filaVista);
        Object id = modelo.getValueAt(filaModelo, 0);
        Object descripcion = modelo.getValueAt(filaModelo, 1);
        Object serie = modelo.getValueAt(filaModelo, 2);

        // 2. Pedir confirmación
        int respuesta = JOptionPane.showConfirmDialog(
                this,
                "¿Estás seguro que deseas eliminar el recurso '" + descripcion + "' (Serie: " + serie + ")?",
                "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        // 3. Ejecutar eliminación
        if (respuesta == JOptionPane.YES_OPTION) {
            try {
                repositorio.eliminarRecurso(id);
                ventana.actualizarTodo();
                JOptionPane.showMessageDialog(this, "Recurso eliminado correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                error(e);
            }
        }
    }

    private void filtrar() {
        List<RowFilter<Object, Object>> filtros = new ArrayList<>();
        String texto = buscar.getForeground().equals(Estilo.GRIS) ? "" : buscar.getText().trim();
        if (!texto.isEmpty()) filtros.add(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(texto)));
        if (tipos.getSelectedIndex() > 0) filtros.add(RowFilter.regexFilter("(?i)^" + tipos.getSelectedItem() + "$", 3));
        ordenador.setRowFilter(filtros.isEmpty() ? null : RowFilter.andFilter(filtros));
    }

    @Override
    public void cargarDatos() {
        try {
            modelo.setRowCount(0);
            repositorio.consultarRecursos().forEach(modelo::addRow);
        } catch (Exception e) {
            error(e);
        }
    }
}
