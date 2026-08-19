package com.utez.prestamos;

import java.awt.BorderLayout;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public abstract class PanelBase extends JPanel {

    protected final VentanaPrincipal ventana;
    protected final Repositorio repositorio;

    protected PanelBase(VentanaPrincipal ventana, Repositorio repositorio) {
        this.ventana = ventana;
        this.repositorio = repositorio;
        setLayout(new BorderLayout(0, 20));
        setBackground(Estilo.FONDO);
        setBorder(BorderFactory.createEmptyBorder(32, 34, 32, 34));
    }

    protected JLabel titulo(String texto) {
        JLabel titulo = new JLabel(texto);
        titulo.setFont(new Font("Montserrat", Font.BOLD, 25));
        titulo.setForeground(Estilo.TEXTO);
        return titulo;
    }

    protected void error(Exception e) {
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(
                this,
                "No fue posible consultar Oracle.\n\n" + e.getMessage()
                + "\n\nRevisa los tres datos de Conexion.java.",
                "Error de conexión",
                JOptionPane.ERROR_MESSAGE
        ));
    }

    public abstract void cargarDatos();
}
