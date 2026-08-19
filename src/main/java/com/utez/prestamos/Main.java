package com.utez.prestamos;

import java.awt.Font;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {
    public static void main(String[] args) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
            UIManager.put("defaultFont", new Font("Arial", Font.PLAIN, 13));
        } catch (Exception ignored) {
        }

        SwingUtilities.invokeLater(() -> new VentanaPrincipal().setVisible(true));
    }
}
