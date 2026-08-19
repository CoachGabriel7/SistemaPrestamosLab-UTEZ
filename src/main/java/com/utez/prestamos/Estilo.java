package com.utez.prestamos;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Image;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.ImageIcon;
import java.net.URL;

//El Estilo.java sirve para cambiar fuentes, colores o el diseño.
public final class Estilo {

    public static final Color VERDE = new Color(92, 111, 72);
    public static final Color VERDE_OSCURO = new Color(65, 82, 51);
    public static final Color VERDE_CLARO = new Color(232, 238, 225);
    public static final Color AZUL_SELECCION = new Color(210, 228, 249);
    public static final Color FONDO = new Color(247, 248, 245);
    public static final Color TEXTO = new Color(35, 38, 35);
    public static final Color GRIS = new Color(112, 118, 112);

    private Estilo() {
    }

    public static JButton botonPrincipal(String texto) {
        JButton boton = new JButton(texto);
        boton.setFont(new Font("Montserrat", Font.BOLD, 13));
        boton.setForeground(Color.WHITE);
        boton.setBackground(VERDE);
        boton.setFocusPainted(false);
        boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        boton.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        return boton;
    }

    public static JButton botonMenu(String texto) {
        JButton boton = new JButton(texto);
        boton.setHorizontalAlignment(JButton.LEFT);
        boton.setFont(new Font("Montserrat", Font.BOLD, 13));
        boton.setForeground(TEXTO);
        boton.setBackground(Color.WHITE);
        boton.setFocusPainted(false);
        boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        boton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(205, 209, 202)),
                BorderFactory.createEmptyBorder(11, 14, 11, 14)
        ));
        return boton;
    }

    public static void seleccionarMenu(JButton seleccionado, JButton... botones) {
        for (JButton boton : botones) {
            boton.setBackground(boton == seleccionado ? AZUL_SELECCION : Color.WHITE);
            boton.setForeground(boton == seleccionado ? VERDE_OSCURO : TEXTO);
        }
    }

    public static JTextField campoBusqueda(String textoAyuda) {
        JTextField campo = new JTextField(textoAyuda);
        campo.setForeground(GRIS);
        campo.setFont(new Font("Montserrat", Font.PLAIN, 13));
        campo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 185, 180)),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        campo.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                if (campo.getText().equals(textoAyuda)) {
                    campo.setText("");
                    campo.setForeground(TEXTO);
                }
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (campo.getText().isBlank()) {
                    campo.setText(textoAyuda);
                    campo.setForeground(GRIS);
                }
            }
        });
        return campo;
    }

    public static void prepararTabla(JTable tabla) {
        tabla.setFont(new Font("Montserrat", Font.PLAIN, 13));
        tabla.setRowHeight(30);
        tabla.setGridColor(new Color(225, 227, 223));
        tabla.setSelectionBackground(VERDE_CLARO);
        tabla.setSelectionForeground(TEXTO);
        tabla.setShowVerticalLines(false);
        tabla.setFillsViewportHeight(true);

        JTableHeader encabezado = tabla.getTableHeader();
        encabezado.setFont(new Font("Montserrat", Font.BOLD, 13));
        encabezado.setBackground(new Color(224, 226, 222));
        encabezado.setForeground(TEXTO);
        encabezado.setReorderingAllowed(false);

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                Component componente = super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    componente.setBackground(row % 2 == 0 ? Color.WHITE : new Color(245, 246, 244));
                }
                if (componente instanceof JComponent jc) {
                    jc.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                }
                return componente;
            }
        };

        for (int i = 0; i < tabla.getColumnCount(); i++) {
            tabla.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }
    }
    //iconos para botones de menu
    public static ImageIcon cargarIcono(String nombreArchivo, int ancho, int alto) {
        URL ruta = Estilo.class.getResource("/imagenes/" + nombreArchivo);

        if (ruta == null) {
            System.err.println("No se encontró el icono: " + nombreArchivo);
            return null;
        }

        ImageIcon original = new ImageIcon(ruta);

        Image imagen = original.getImage().getScaledInstance(
                ancho,
                alto,
                Image.SCALE_SMOOTH
        );

        return new ImageIcon(imagen);
    }
}
