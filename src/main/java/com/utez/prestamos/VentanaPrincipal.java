package com.utez.prestamos;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image; // Importado para redimensionar
import java.net.URL;   // Importado para cargar desde resources
import javax.swing.BorderFactory;
import javax.swing.ImageIcon; // Importado para la imagen
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class VentanaPrincipal extends JFrame {

    private final CardLayout tarjetas = new CardLayout();
    private final JPanel contenido = new JPanel(tarjetas);
    private final Repositorio repositorio = new Repositorio();

    private final PanelInicio inicio = new PanelInicio(this, repositorio);
    private final PanelPrestamos prestamos = new PanelPrestamos(this, repositorio);
    private final PanelRecursos recursos = new PanelRecursos(this, repositorio);
    private final PanelUsuarios usuarios = new PanelUsuarios(this, repositorio);

    private final JButton btnInicio = Estilo.botonMenu("MENÚ PRINCIPAL");
    private final JButton btnPrestamos = Estilo.botonMenu("PRÉSTAMOS");
    private final JButton btnRecursos = Estilo.botonMenu("RECURSOS");
    private final JButton btnUsuarios = Estilo.botonMenu("USUARIOS");

    public VentanaPrincipal() {
        super("LABORATORIO UTEZ - Sistema de préstamos");
        construirInterfaz();
        mostrar("INICIO");
    }

    private void construirInterfaz() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1050, 680));
        setSize(1180, 760);
        setLocationRelativeTo(null);
        getContentPane().setLayout(new BorderLayout());
        getContentPane().setBackground(Estilo.FONDO);

        JPanel lateral = new JPanel(new BorderLayout(0, 25));
        lateral.setPreferredSize(new Dimension(230, 0));
        lateral.setBackground(Color.WHITE);
        lateral.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(215, 218, 212)));

        // CÓDIGO DEL LOGO
        JLabel marca = new JLabel("", SwingConstants.CENTER);
        marca.setBorder(BorderFactory.createEmptyBorder(20, 10, 5, 10));

        marca.setIcon(Estilo.cargarIcono("logo_utez.png", 180, 115));

        if (marca.getIcon() == null) {
            marca.setText("LAB UTEZ");
            marca.setFont(new Font("MONSERRAT", Font.BOLD, 24));
            marca.setForeground(Estilo.VERDE_OSCURO);
        }
        //iconos para botones de menu
        btnInicio.setIcon(Estilo.cargarIcono("icon_menu.png", 24, 24));
        btnPrestamos.setIcon(Estilo.cargarIcono("icon_prestamos.png", 24, 24));
        btnRecursos.setIcon(Estilo.cargarIcono("icon_recursos.png", 24, 24));
        btnUsuarios.setIcon(Estilo.cargarIcono("icon_usuarios.png", 24, 24));

        btnInicio.setIconTextGap(12);
        btnPrestamos.setIconTextGap(12);
        btnRecursos.setIconTextGap(12);
        btnUsuarios.setIconTextGap(12);

        JPanel menu = new JPanel(new GridLayout(4, 1, 0, 12));
        menu.setOpaque(false);
        menu.setBorder(BorderFactory.createEmptyBorder(0, 18, 0, 18));
        menu.add(btnInicio);
        menu.add(btnPrestamos);
        menu.add(btnRecursos);
        menu.add(btnUsuarios);

        JPanel bloqueMenu = new JPanel(new BorderLayout());
        bloqueMenu.setOpaque(false);
        bloqueMenu.add(menu, BorderLayout.NORTH);

        JLabel pie = new JLabel("Universidad Tecnológica Emiliano Zapata", SwingConstants.CENTER);
        pie.setFont(new Font("Montserrat", Font.PLAIN, 10));
        pie.setForeground(Estilo.GRIS);
        pie.setBorder(BorderFactory.createEmptyBorder(10, 8, 25, 8));

        lateral.add(marca, BorderLayout.NORTH);
        lateral.add(bloqueMenu, BorderLayout.CENTER);
        lateral.add(pie, BorderLayout.SOUTH);

        contenido.add(inicio, "INICIO");
        contenido.add(prestamos, "PRESTAMOS");
        contenido.add(recursos, "RECURSOS");
        contenido.add(usuarios, "USUARIOS");

        btnInicio.addActionListener(e -> mostrar("INICIO"));
        btnPrestamos.addActionListener(e -> mostrar("PRESTAMOS"));
        btnRecursos.addActionListener(e -> mostrar("RECURSOS"));
        btnUsuarios.addActionListener(e -> mostrar("USUARIOS"));

        add(lateral, BorderLayout.WEST);
        add(contenido, BorderLayout.CENTER);
    }

    public void mostrar(String pantalla) {
        tarjetas.show(contenido, pantalla);
        JButton activo = switch (pantalla) {
            case "PRESTAMOS" -> btnPrestamos;
            case "RECURSOS" -> btnRecursos;
            case "USUARIOS" -> btnUsuarios;
            default -> btnInicio;
        };
        Estilo.seleccionarMenu(activo, btnInicio, btnPrestamos, btnRecursos, btnUsuarios);

        switch (pantalla) {
            case "PRESTAMOS" -> prestamos.cargarDatos();
            case "RECURSOS" -> recursos.cargarDatos();
            case "USUARIOS" -> usuarios.cargarDatos();
            default -> inicio.cargarDatos();
        }
    }

    public void actualizarTodo() {
        inicio.cargarDatos();
        prestamos.cargarDatos();
        recursos.cargarDatos();
        usuarios.cargarDatos();
    }
}