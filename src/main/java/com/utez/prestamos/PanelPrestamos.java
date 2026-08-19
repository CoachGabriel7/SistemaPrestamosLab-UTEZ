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

public class PanelPrestamos extends PanelBase {

    private final DefaultTableModel modelo = new DefaultTableModel(
            new String[]{"ID", "Solicitante", "Recurso", "Prestado", "Vence", "Estado"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
        @Override public Class<?> getColumnClass(int column) {
            return column == 0 ? Integer.class : String.class;
        }
    };
    private final JTable tabla = new JTable(modelo);
    private final TableRowSorter<DefaultTableModel> ordenador = new TableRowSorter<>(modelo);
    private final JTextField buscar = Estilo.campoBusqueda("Buscar por ID / Nombre / Artículo");
    private final JComboBox<String> estados = new JComboBox<>(new String[]{"TODOS LOS ESTADOS", "ACTIVO", "VENCIDO", "DEVUELTO"});

    public PanelPrestamos(VentanaPrincipal ventana, Repositorio repositorio) {
        super(ventana, repositorio);

        // Botones de acción
        JButton nuevo = Estilo.botonPrincipal("+ Nuevo préstamo");
        JButton devolver = Estilo.botonPrincipal("• Devolver préstamo");
        JButton eliminar = Estilo.botonPrincipal("- Eliminar préstamo");

        // Agrupamos los botones a la derecha del encabezado
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panelBotones.setOpaque(false);
        panelBotones.add(eliminar);
        panelBotones.add(devolver);
        panelBotones.add(nuevo);

        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.setOpaque(false);
        encabezado.add(titulo("Gestión de préstamos"), BorderLayout.WEST);
        encabezado.add(panelBotones, BorderLayout.EAST);

        buscar.setPreferredSize(new Dimension(330, 38));
        estados.setPreferredSize(new Dimension(190, 38));
        JPanel filtros = new JPanel(new BorderLayout(12, 0));
        filtros.setOpaque(false);
        filtros.add(buscar, BorderLayout.WEST);
        filtros.add(estados, BorderLayout.EAST);

        Estilo.prepararTabla(tabla);
        tabla.setRowSorter(ordenador);
        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(215, 218, 212)));

        JPanel centro = new JPanel(new BorderLayout(0, 14));
        centro.setOpaque(false);
        centro.add(filtros, BorderLayout.NORTH);
        centro.add(scroll, BorderLayout.CENTER);

        // Eventos
        nuevo.addActionListener(e -> new DialogoPrestamo(ventana, repositorio).setVisible(true));
        devolver.addActionListener(e -> devolverPrestamoSeleccionado());
        eliminar.addActionListener(e -> eliminarPrestamoSeleccionado());

        buscar.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filtrar(); }
            public void removeUpdate(DocumentEvent e) { filtrar(); }
            public void changedUpdate(DocumentEvent e) { filtrar(); }
        });
        estados.addActionListener(e -> filtrar());

        add(encabezado, BorderLayout.NORTH);
        add(centro, BorderLayout.CENTER);
    }

    private void devolverPrestamoSeleccionado() {

        int fila = tabla.getSelectedRow();

        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona un préstamo.");
            return;
        }

        int filaModelo = tabla.convertRowIndexToModel(fila);

        int idPrestamo = Integer.parseInt(modelo.getValueAt(filaModelo, 0).toString());

        String estado = modelo.getValueAt(filaModelo, 5).toString();

        if (!estado.equalsIgnoreCase("ACTIVO")) {
            JOptionPane.showMessageDialog(this, "Solo se pueden devolver préstamos activos");
            return;
        }

        new DialogoDevolucion(
                ventana,
                repositorio,
                idPrestamo
        ).setVisible(true);
    }

    private void eliminarPrestamoSeleccionado() {
        int filaVista = tabla.getSelectedRow();

        // 1. Validar selección
        if (filaVista == -1) {
            JOptionPane.showMessageDialog(this, "Por favor, selecciona un préstamo de la tabla para eliminar.", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int filaModelo = tabla.convertRowIndexToModel(filaVista);
        Object id = modelo.getValueAt(filaModelo, 0);
        Object solicitante = modelo.getValueAt(filaModelo, 1);
        Object recurso = modelo.getValueAt(filaModelo, 2);

        // 2. Mensaje de confirmación
        int respuesta = JOptionPane.showConfirmDialog(
                this,
                "¿Estás seguro de eliminar el préstamo ID: " + id + " (" + recurso + " - " + solicitante + ")?",
                "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        // 3. Eliminación en BD y actualización general
        if (respuesta == JOptionPane.YES_OPTION) {
            try {
                repositorio.eliminarPrestamo(id);
                ventana.actualizarTodo(); // Actualiza todas las pantallas para liberar contadores/listas
                JOptionPane.showMessageDialog(this, "Préstamo eliminado correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                error(e);
            }
        }
    }

    private void filtrar() {
        List<RowFilter<Object, Object>> filtros = new ArrayList<>();
        String texto = buscar.getForeground().equals(Estilo.GRIS) ? "" : buscar.getText().trim();
        if (!texto.isEmpty()) filtros.add(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(texto)));
        if (estados.getSelectedIndex() > 0) {
            filtros.add(RowFilter.regexFilter("(?i)^" + estados.getSelectedItem() + "$", 5));
        }
        ordenador.setRowFilter(filtros.isEmpty() ? null : RowFilter.andFilter(filtros));
    }

    @Override
    public void cargarDatos() {
        try {
            modelo.setRowCount(0);
            repositorio.consultarPrestamos().forEach(p -> {
                modelo.addRow(new Object[]{
                        p.getId(),
                        p.getSolicitante(),
                        p.getRecurso(),
                        p.getFechaPrestamo(),
                        p.getFechaLimite(),
                        p.getEstado()
                });
            });
        } catch (Exception e) {
            error(e);
        }
    }
}
