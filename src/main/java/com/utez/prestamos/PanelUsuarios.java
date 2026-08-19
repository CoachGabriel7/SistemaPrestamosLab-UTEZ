package com.utez.prestamos;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
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
import javax.swing.table.DefaultTableCellRenderer;

public class PanelUsuarios extends PanelBase {

    private final DefaultTableModel modelo = new DefaultTableModel(
            new String[]{"ID", "Nombre", "Matrícula", "Tipo", "Correo", "Teléfono", "Estado"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
        @Override public Class<?> getColumnClass(int column) {
            return column == 0 ? Integer.class : String.class;
        }
    };
    private final JTable tabla = new JTable(modelo);
    private final TableRowSorter<DefaultTableModel> ordenador = new TableRowSorter<>(modelo);
    private final JTextField buscar = Estilo.campoBusqueda("Buscar por ID / Nombre / Matrícula");

    public PanelUsuarios(VentanaPrincipal ventana, Repositorio repositorio) {
        super(ventana, repositorio);

        // Botones de acción
        JButton agregar = Estilo.botonPrincipal("+ Nuevo usuario");
        JButton editar = Estilo.botonPrincipal("• Editar usuario");
        JButton eliminar = Estilo.botonPrincipal("- Eliminar usuario");

        // Agrupamos los botones a la derecha del encabezado
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panelBotones.setOpaque(false);
        panelBotones.add(eliminar);
        panelBotones.add(editar);
        panelBotones.add(agregar);

        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.setOpaque(false);
        encabezado.add(titulo("Usuarios"), BorderLayout.WEST);
        encabezado.add(panelBotones, BorderLayout.EAST);

        buscar.setPreferredSize(new Dimension(360, 38));
        JPanel filtros = new JPanel(new BorderLayout());
        filtros.setOpaque(false);
        filtros.add(buscar, BorderLayout.WEST);

        Estilo.prepararTabla(tabla);
//colocar en rojo un usuario sancionado
        DefaultTableCellRenderer rendererUsuarios = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

                Component componente = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                String estado = table.getValueAt(row, 6).toString();

                if (estado.equalsIgnoreCase("SANCIONADO")) {
                    componente.setBackground(new Color(128, 35, 55));
                    componente.setForeground(Color.WHITE);
                } else if (!isSelected) {
                    componente.setBackground(row % 2 == 0 ? Color.WHITE : new Color(245, 246, 244));
                    componente.setForeground(Color.BLACK);
                }

                return componente;
            }
        };

        for (int i = 0; i < tabla.getColumnCount(); i++) {
            tabla.getColumnModel()
                    .getColumn(i)
                    .setCellRenderer(rendererUsuarios);
        }

        tabla.setRowSorter(ordenador);
        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(215, 218, 212)));
        JPanel centro = new JPanel(new BorderLayout(0, 14));
        centro.setOpaque(false);
        centro.add(filtros, BorderLayout.NORTH);
        centro.add(scroll, BorderLayout.CENTER);

        // Eventos de los botones
        agregar.addActionListener(e -> new DialogoUsuario(ventana, repositorio).setVisible(true));
        editar.addActionListener(e -> editarUsuarioSeleccionado());
        eliminar.addActionListener(e -> eliminarUsuarioSeleccionado());

        buscar.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filtrar(); }
            public void removeUpdate(DocumentEvent e) { filtrar(); }
            public void changedUpdate(DocumentEvent e) { filtrar(); }
        });

        add(encabezado, BorderLayout.NORTH);
        add(centro, BorderLayout.CENTER);
    }

    private void editarUsuarioSeleccionado() {
        int filaVista = tabla.getSelectedRow();
        if (filaVista == -1) {
            JOptionPane.showMessageDialog(this, "Por favor, selecciona un usuario de la tabla para editar.", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int filaModelo = tabla.convertRowIndexToModel(filaVista);
        // Usamos toString() y parseo para evitar errores de casting si Oracle devuelve BigDecimal
        int id = Integer.parseInt(modelo.getValueAt(filaModelo, 0).toString());

        new DialogoUsuario(ventana, repositorio, id).setVisible(true);
    }

    private void eliminarUsuarioSeleccionado() {
        int filaVista = tabla.getSelectedRow();

        // 1.- Verificar si hay una fila seleccionada
        if (filaVista == -1) {
            JOptionPane.showMessageDialog(this, "Por favor, selecciona un usuario de la tabla.", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int filaModelo = tabla.convertRowIndexToModel(filaVista);
        Object id = modelo.getValueAt(filaModelo, 0);
        Object nombre = modelo.getValueAt(filaModelo, 1);

        // 2.- Pedir confirmación al usuario
        int respuesta = JOptionPane.showConfirmDialog(
                this,
                "¿Estás seguro que deseas eliminar al usuario '" + nombre + "' (ID: " + id + ")?",
                "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        // 3. Ejecutar eliminación en Oracle si se confirma
        if (respuesta == JOptionPane.YES_OPTION) {
            try {
                repositorio.eliminarUsuario(id);
                cargarDatos();
                JOptionPane.showMessageDialog(this, "Usuario eliminado correctamente", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                error(e);
            }
        }
    }

    private void filtrar() {
        String texto = buscar.getForeground().equals(Estilo.GRIS) ? "" : buscar.getText().trim();
        ordenador.setRowFilter(texto.isEmpty() ? null
                : RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(texto)));
    }

    @Override
    public void cargarDatos() {
        try {
            modelo.setRowCount(0);
            repositorio.consultarUsuarios().forEach(modelo::addRow);
        } catch (Exception e) {
            error(e);
        }
    }
}