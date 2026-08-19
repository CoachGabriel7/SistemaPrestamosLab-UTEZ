package com.utez.prestamos;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class PanelInicio extends PanelBase {

    private final DefaultTableModel modelo = new DefaultTableModel(
            new String[]{"ID", "Solicitante", "Recurso", "Prestado", "Vence", "Estado"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
        @Override public Class<?> getColumnClass(int column) {
            return column == 0 ? Integer.class : String.class;
        }
    };
    private final JTable tabla = new JTable(modelo);
    private final JLabel lblPrestamos = tarjetaNumero("0", "Préstamos registrados");
    private final JLabel lblRecursos = tarjetaNumero("0", "Recursos registrados");
    private final JLabel lblUsuarios = tarjetaNumero("0", "Usuarios registrados");

    public PanelInicio(VentanaPrincipal ventana, Repositorio repositorio) {
        super(ventana, repositorio);

        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.setOpaque(false);
        encabezado.add(titulo("Bienvenido"), BorderLayout.WEST);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        acciones.setOpaque(false);
        JButton nuevoPrestamo = Estilo.botonPrincipal("+ Nuevo préstamo");
        JButton nuevoUsuario = Estilo.botonPrincipal("+ Nuevo usuario");
        JButton nuevoRecurso = Estilo.botonPrincipal("+ Nuevo recurso");
        acciones.add(nuevoPrestamo);
        acciones.add(nuevoUsuario);
        acciones.add(nuevoRecurso);

        nuevoPrestamo.addActionListener(e -> new DialogoPrestamo(ventana, repositorio).setVisible(true));
        nuevoUsuario.addActionListener(e -> new DialogoUsuario(ventana, repositorio).setVisible(true));
        nuevoRecurso.addActionListener(e -> new DialogoRecurso(ventana, repositorio).setVisible(true));

        Estilo.prepararTabla(tabla);
        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(215, 218, 212)));

        JPanel centro = new JPanel(new BorderLayout(0, 12));
        centro.setOpaque(false);
        JLabel subtitulo = new JLabel("Préstamos recientes");
        subtitulo.setFont(new Font("Montserrat", Font.BOLD, 17));
        centro.add(acciones, BorderLayout.NORTH);
        centro.add(subtitulo, BorderLayout.CENTER);

        JPanel tablaBloque = new JPanel(new BorderLayout(0, 10));
        tablaBloque.setOpaque(false);
        tablaBloque.add(centro, BorderLayout.NORTH);
        tablaBloque.add(scroll, BorderLayout.CENTER);

        JPanel resumen = new JPanel(new GridLayout(1, 3, 16, 0));
        resumen.setOpaque(false);
        resumen.add(lblPrestamos);
        resumen.add(lblRecursos);
        resumen.add(lblUsuarios);

        add(encabezado, BorderLayout.NORTH);
        add(tablaBloque, BorderLayout.CENTER);
        add(resumen, BorderLayout.SOUTH);
    }

    private JLabel tarjetaNumero(String numero, String texto) {
        JLabel etiqueta = new JLabel("<html><div style='text-align:center'><b style='font-size:22px'>"
                + numero + "</b><br>" + texto + "</div></html>", JLabel.CENTER);
        etiqueta.setOpaque(true);
        etiqueta.setBackground(Color.WHITE);
        etiqueta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(215, 218, 212)),
                BorderFactory.createEmptyBorder(14, 8, 14, 8)));
        return etiqueta;
    }

    private void actualizarTarjeta(JLabel etiqueta, int numero, String texto) {
        etiqueta.setText("<html><div style='text-align:center'><b style='font-size:22px'>"
                + numero + "</b><br>" + texto + "</div></html>");
    }

    @Override
    public void cargarDatos() {
        try {
            List<Object[]> prestamos = repositorio.consultarPrestamos();
            List<Object[]> recursos = repositorio.consultarRecursos();
            List<Object[]> usuarios = repositorio.consultarUsuarios();
            modelo.setRowCount(0);
            prestamos.stream().limit(8).forEach(modelo::addRow);
            actualizarTarjeta(lblPrestamos, prestamos.size(), "Préstamos registrados");
            actualizarTarjeta(lblRecursos, recursos.size(), "Recursos registrados");
            actualizarTarjeta(lblUsuarios, usuarios.size(), "Usuarios registrados");
        } catch (Exception e) {
            error(e);
        }
    }
}
