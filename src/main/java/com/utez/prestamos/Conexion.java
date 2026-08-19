package com.utez.prestamos;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexion {

    // las credenciales
    private static final String URL = "jdbc:oracle:thin:@rpc9l3oxk8a77efa_high"; 
    private static final String USUARIO = "ADMIN";
    private static final String PASSWORD = "S0ln@r@ng@1997";

    public static Connection getConexion() {
        Connection conexion = null;
        try {
            // Obtiene la ruta raíz del proyecto dinámicamente para la carpeta wallet
            String rutaProyecto = System.getProperty("user.dir");
            String rutaWallet = rutaProyecto + File.separator + "Wallet";

            // Asigna la ubicación del Wallet a Oracle
            System.setProperty("oracle.net.tns_admin", rutaWallet);

            // Obtiene la conexión a la base de datos
            conexion = DriverManager.getConnection(URL, USUARIO, PASSWORD);

        } catch (SQLException e) {
            System.err.println("Error al conectar a Oracle Cloud: " + e.getMessage());
            e.printStackTrace();
        }
        return conexion;
    }
}