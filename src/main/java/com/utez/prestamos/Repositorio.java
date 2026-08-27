package com.utez.prestamos;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Repositorio {

    public Iterable<Opcion> usuarios() {
        return new ArrayList<>();
    }

    public Iterable<Opcion> recursosDisponibles() {
        return new ArrayList<>();
    }

    public Iterable<Opcion> estadosFisicos() {
        return new ArrayList<>();
    }

    public Iterable<Opcion> tiposRecurso() {
        return new ArrayList<>();
    }

    public Iterable<Opcion> tiposUsuario() {
        return new ArrayList<>();
    }

    public record Opcion(int id, String nombre) {
        @Override
        public String toString() {
            return nombre;
        }
    }

    // --- MÉTODOS DE CONSULTA (SELECT) ---

    public List<Prestamo> consultarPrestamos() throws SQLException {
        String sql = """
                SELECT p.ID_PRESTAMO,
                       u.NOMBRE || ' ' || u.APELLIDO AS SOLICITANTE,
                       r.NOMBRE AS RECURSO,
                       p.FECHA_PRESTAMO,
                       p.FECHA_LIMITE,
                       ep.NOMBRE AS ESTADO
                FROM PRESTAMO p
                JOIN USUARIO u ON u.ID_USUARIO = p.ID_USUARIO
                JOIN RECURSO r ON r.ID_RECURSO = p.ID_RECURSO
                JOIN ESTADOPRESTAMO ep ON ep.ID_ESTADO_PRESTAMO = p.ID_ESTADO_PRESTAMO
                ORDER BY p.ID_PRESTAMO DESC
                """;
        List<Prestamo> lista = new ArrayList<>();
        try (Connection con = Conexion.getConexion();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new Prestamo(
                        rs.getInt("ID_PRESTAMO"),
                        rs.getString("SOLICITANTE"),
                        rs.getString("RECURSO"),
                        rs.getDate("FECHA_PRESTAMO").toLocalDate(),
                        rs.getDate("FECHA_LIMITE").toLocalDate(),
                        rs.getString("ESTADO")
                ));
            }
        }
        return lista;
    }

    public List<Recurso> consultarRecursos() throws SQLException {
        String sql = """
                SELECT r.ID_RECURSO,
                       r.NOMBRE,
                       r.NUMERO_SERIE,
                       tr.NOMBRE AS TIPO,
                       CASE
                           WHEN EXISTS (
                               SELECT 1
                               FROM PRESTAMO p
                               JOIN ESTADOPRESTAMO ep
                                 ON ep.ID_ESTADO_PRESTAMO = p.ID_ESTADO_PRESTAMO
                               WHERE p.ID_RECURSO = r.ID_RECURSO
                                 AND UPPER(ep.NOMBRE) = 'ACTIVO'
                           ) THEN 'Prestado'
                           ELSE ef.NOMBRE
                       END AS ESTADO
                FROM RECURSO r
                JOIN TIPORECURSO tr ON tr.ID_TIPO_RECURSO = r.ID_TIPO_RECURSO
                JOIN ESTADOFISICO ef ON ef.ID_ESTADO_FISICO = r.ID_ESTADO_FISICO_ACTUAL
                ORDER BY r.ID_RECURSO
                """;
        List<Recurso> lista = new ArrayList<>();
        try (Connection con = Conexion.getConexion();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new Recurso(
                        rs.getInt("ID_RECURSO"),
                        rs.getString("NOMBRE"),
                        rs.getString("NUMERO_SERIE"),
                        rs.getString("TIPO"),
                        rs.getString("ESTADO")
                ));
            }
        }
        return lista;
    }

    public List<Usuario> consultarUsuarios() throws SQLException {
        String sql = """
                SELECT u.ID_USUARIO,
                       u.NOMBRE,
                       u.APELLIDO,
                       u.MATRICULA,
                       tu.NOMBRE AS TIPO,
                       u.CORREO,
                       u.TELEFONO,
                       CASE
                           WHEN u.SANCIONADO = 1 THEN 'SANCIONADO'
                           ELSE 'HABILITADO'
                       END AS ESTADO
                FROM USUARIO u
                JOIN TIPOUSUARIO tu ON tu.ID_TIPO_USUARIO = u.ID_TIPO_USUARIO
                ORDER BY u.NOMBRE, u.APELLIDO
                """;
        List<Usuario> lista = new ArrayList<>();
        try (Connection con = Conexion.getConexion();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new Usuario(
                        rs.getInt("ID_USUARIO"),
                        rs.getString("NOMBRE"),
                        rs.getString("APELLIDO"),
                        rs.getString("MATRICULA"),
                        rs.getString("TIPO"),
                        rs.getString("CORREO"),
                        rs.getString("TELEFONO"),
                        rs.getString("ESTADO")
                ));
            }
        }
        return lista;
    }

    // --- MÉTODOS DE REGISTRO (INSERT) ---

    public void registrarPrestamo(int idUsuario, int idRecurso, int idEstadoFisico, LocalDate fechaPrestamo, LocalDate fechaLimite) throws SQLException {
        if (fechaLimite.isBefore(fechaPrestamo)) {
            throw new IllegalArgumentException("La fecha límite no puede ser anterior a la fecha del préstamo.");
        }

        String sql = """
                INSERT INTO PRESTAMO (
                    ID_PRESTAMO, ID_USUARIO, ID_RECURSO, ID_ESTADO_PRESTAMO,
                    ID_ESTADO_FISICO_ENTREGA, ID_ESTADO_FISICO_DEVOLUCION,
                    FECHA_PRESTAMO, FECHA_LIMITE, FECHA_DEVOLUCION
                )
                VALUES (?, ?, ?, ?, ?, NULL, ?, ?, NULL)
                """;

        try (Connection con = Conexion.getConexion()) {
            con.setAutoCommit(false);
            try {
                int idPrestamo = siguienteId(con, "PRESTAMO", "ID_PRESTAMO");
                int idActivo = idPorNombre(con, "ESTADOPRESTAMO", "ID_ESTADO_PRESTAMO", "ACTIVO");

                // 1. Revisar si ya estaba sancionado
                if (usuarioSancionado(con, idUsuario)) {
                    throw new IllegalArgumentException("El usuario está sancionado y no puede solicitar préstamos.");
                }

                // 2. Revisar si tiene un préstamo vencido
                if (tienePrestamoVencido(con, idUsuario)) {
                    sancionarUsuario(con, idUsuario);

                    throw new IllegalArgumentException("El usuario tiene un préstamo vencido y ha sido sancionado.");
                }

                // 3. Revisar máximo de préstamos activos
                int prestamosActivos =
                        contarPrestamosActivosUsuario(con, idUsuario, idActivo);

                if (prestamosActivos >= 3) {
                    throw new IllegalArgumentException("El usuario ya tiene el máximo de 3 préstamos activos.");
                }

                // 4. Revisar recurso
                if (!recursoDisponible(con, idRecurso, idActivo)) {
                    throw new IllegalArgumentException("El recurso ya tiene un préstamo activo.");
                }

                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setInt(1, idPrestamo);
                    ps.setInt(2, idUsuario);
                    ps.setInt(3, idRecurso);
                    ps.setInt(4, idActivo);
                    ps.setInt(5, idEstadoFisico);
                    ps.setDate(6, Date.valueOf(fechaPrestamo));
                    ps.setDate(7, Date.valueOf(fechaLimite));
                    ps.executeUpdate();
                }
                con.commit();
            } catch (SQLException | RuntimeException e) {
                con.rollback();
                throw e;
            }
        }
    }

    public void devolverPrestamo(int idPrestamo, int idEstadoFisicoDevolucion, String comentarios) throws SQLException {
        devolverPrestamo(idPrestamo, idEstadoFisicoDevolucion, LocalDate.now(), comentarios);
    }

    public void devolverPrestamo(int idPrestamo, int idEstadoFisicoDevolucion, LocalDate fechaDevolucion, String comentarios) throws SQLException {

        try (Connection con = Conexion.getConexion()) {

            con.setAutoCommit(false);

            try {

                int idDevuelto = idPorNombre(
                        con,
                        "ESTADOPRESTAMO",
                        "ID_ESTADO_PRESTAMO",
                        "DEVUELTO"
                );

                String consulta = """
                    SELECT ID_USUARIO,
                           ID_RECURSO,
                           FECHA_LIMITE,
                           ID_ESTADO_PRESTAMO
                    FROM PRESTAMO
                    WHERE ID_PRESTAMO = ?
                    """;

                int idUsuario;
                int idRecurso;
                LocalDate fechaLimite;
                int estadoActual;

                try (PreparedStatement ps = con.prepareStatement(consulta)) {

                    ps.setInt(1, idPrestamo);

                    try (ResultSet rs = ps.executeQuery()) {

                        if (!rs.next()) {
                            throw new IllegalArgumentException(
                                    "El préstamo seleccionado no existe."
                            );
                        }

                        idUsuario = rs.getInt("ID_USUARIO");
                        idRecurso = rs.getInt("ID_RECURSO");
                        fechaLimite =
                                rs.getDate("FECHA_LIMITE").toLocalDate();
                        estadoActual =
                                rs.getInt("ID_ESTADO_PRESTAMO");
                    }
                }

                int idActivo = idPorNombre(
                        con,
                        "ESTADOPRESTAMO",
                        "ID_ESTADO_PRESTAMO",
                        "ACTIVO"
                );

                if (estadoActual != idActivo) {
                    throw new IllegalArgumentException(
                            "Solamente se pueden devolver préstamos activos."
                    );
                }

                String actualizarPrestamo = """
                    UPDATE PRESTAMO
                    SET ID_ESTADO_PRESTAMO = ?,
                        ID_ESTADO_FISICO_DEVOLUCION = ?,
                        FECHA_DEVOLUCION = ?,
                        COMENTARIOS_DEVOLUCION = ?
                    WHERE ID_PRESTAMO = ?
                    """;

                try (PreparedStatement ps =
                             con.prepareStatement(actualizarPrestamo)) {

                    ps.setInt(1, idDevuelto);
                    ps.setInt(2, idEstadoFisicoDevolucion);
                    ps.setDate(3, Date.valueOf(fechaDevolucion));

                    if (comentarios == null ||
                            comentarios.isBlank()) {

                        ps.setNull(
                                4,
                                java.sql.Types.VARCHAR
                        );

                    } else {

                        ps.setString(
                                4,
                                comentarios.trim()
                        );
                    }

                    ps.setInt(5, idPrestamo);

                    ps.executeUpdate();
                }

                // Actualizar el estado físico actual del recurso
                String actualizarRecurso = """
                    UPDATE RECURSO
                    SET ID_ESTADO_FISICO_ACTUAL = ?
                    WHERE ID_RECURSO = ?
                    """;

                try (PreparedStatement ps =
                             con.prepareStatement(actualizarRecurso)) {

                    ps.setInt(
                            1,
                            idEstadoFisicoDevolucion
                    );

                    ps.setInt(
                            2,
                            idRecurso
                    );

                    ps.executeUpdate();
                }

                // Comprobar sanciones
                evaluarSancion(
                        con,
                        idUsuario,
                        fechaDevolucion,
                        fechaLimite
                );

                con.commit();

            } catch (SQLException | RuntimeException e) {

                con.rollback();
                throw e;
            }
        }
    }

    public void registrarRecurso(String nombre, String serie, int idTipo, int idEstado) throws SQLException {
        String sql = "INSERT INTO RECURSO (ID_RECURSO, ID_TIPO_RECURSO, ID_ESTADO_FISICO_ACTUAL, NOMBRE, NUMERO_SERIE) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, siguienteId(con, "RECURSO", "ID_RECURSO"));
            ps.setInt(2, idTipo);
            ps.setInt(3, idEstado);
            ps.setString(4, nombre.trim());
            ps.setString(5, serie.trim());
            ps.executeUpdate();
        }
    }

    public void registrarUsuario(String nombre, String apellido, String matricula,
                                 String correo, String telefono, int idTipo) throws SQLException {
        String sql = "INSERT INTO USUARIO (ID_USUARIO, ID_TIPO_USUARIO, NOMBRE, APELLIDO, MATRICULA, CORREO, TELEFONO) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, siguienteId(con, "USUARIO", "ID_USUARIO"));
            ps.setInt(2, idTipo);
            ps.setString(3, nombre.trim());
            ps.setString(4, apellido.trim());
            ps.setString(5, matricula.trim());
            ps.setString(6, correo.trim());
            ps.setString(7, telefono.trim());
            ps.executeUpdate();
        }
    }

    public Object[] obtenerDatosUsuario(int idUsuario) throws SQLException {
        String sql = "SELECT NOMBRE, APELLIDO, MATRICULA, CORREO, TELEFONO, ID_TIPO_USUARIO FROM USUARIO WHERE ID_USUARIO = ?";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Object[]{
                            rs.getString("NOMBRE"),
                            rs.getString("APELLIDO"),
                            rs.getString("MATRICULA"),
                            rs.getString("CORREO"),
                            rs.getString("TELEFONO"),
                            rs.getInt("ID_TIPO_USUARIO")
                    };
                }
            }
        }
        return null;
    }

    public void actualizarUsuario(int idUsuario, String nombre, String apellido, String matricula,
                                  String correo, String telefono, int idTipo) throws SQLException {
        String sql = "UPDATE USUARIO SET NOMBRE = ?, APELLIDO = ?, MATRICULA = ?, CORREO = ?, TELEFONO = ?, ID_TIPO_USUARIO = ? WHERE ID_USUARIO = ?";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nombre.trim());
            ps.setString(2, apellido.trim());
            ps.setString(3, matricula.trim());
            ps.setString(4, correo.trim());
            ps.setString(5, telefono.trim());
            ps.setInt(6, idTipo);
            ps.setInt(7, idUsuario);
            ps.executeUpdate();
        }
    }

    private int siguienteId(Connection con, String tabla, String columna) throws SQLException {
        String sql = "SELECT NVL(MAX(" + columna + "), 0) + 1 FROM " + tabla;
        try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            rs.next();
            return rs.getInt(1);
        }
    }

    private int idPorNombre(Connection con, String tabla, String columnaId, String nombre) throws SQLException {
        String sql = "SELECT " + columnaId + " FROM " + tabla + " WHERE UPPER(NOMBRE) = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nombre.toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("No existe el estado '" + nombre + "' en " + tabla + ".");
                }
                return rs.getInt(1);
            }
        }
    }

    private int contarDevolucionesDanadas(Connection con, int idUsuario) throws SQLException {

        String sql = """
            SELECT COUNT(*)
            FROM PRESTAMO p
            JOIN ESTADOFISICO ef
              ON ef.ID_ESTADO_FISICO =
                 p.ID_ESTADO_FISICO_DEVOLUCION
            WHERE p.ID_USUARIO = ?
              AND UPPER(TRIM(ef.NOMBRE)) = 'DAÑADO'
            """;

        try (PreparedStatement ps =
                     con.prepareStatement(sql)) {

            ps.setInt(1, idUsuario);

            try (ResultSet rs = ps.executeQuery()) {

                rs.next();

                return rs.getInt(1);
            }
        }
    }

    private boolean recursoDisponible(Connection con, int idRecurso, int idActivo) throws SQLException {
        String sql = "SELECT COUNT(*) FROM PRESTAMO WHERE ID_RECURSO = ? AND ID_ESTADO_PRESTAMO = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idRecurso);
            ps.setInt(2, idActivo);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) == 0;
            }
        }
    }

    private boolean usuarioSancionado(Connection con, int idUsuario) throws SQLException {
        String sql = "SELECT SANCIONADO FROM USUARIO WHERE ID_USUARIO = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("SANCIONADO") == 1;
                }
            }
        }

        return false;
    }

    private boolean tienePrestamoVencido(Connection con, int idUsuario) throws SQLException {

        String sql = """
            SELECT COUNT(*)
            FROM PRESTAMO p
            JOIN ESTADOPRESTAMO e
            ON p.ID_ESTADO_PRESTAMO = e.ID_ESTADO_PRESTAMO
            WHERE p.ID_USUARIO = ?
            AND UPPER(e.NOMBRE) = 'ACTIVO'
            AND p.FECHA_LIMITE < SYSDATE
            """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);

            try (ResultSet rs = ps.executeQuery()) {
                rs.next();

                return rs.getInt(1) > 0;
            }
        }
    }

    //Cuenta la cantidad de préstamos que tiene un usuario
    private int contarPrestamosActivosUsuario(Connection con, int idUsuario, int idActivo)
            throws SQLException {

        String sql = """
            SELECT COUNT(*)
            FROM PRESTAMO
            WHERE ID_USUARIO = ?
            AND ID_ESTADO_PRESTAMO = ?
            """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            ps.setInt(2, idActivo);

            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    private void evaluarSancion(Connection con, int idUsuario, LocalDate fechaDevolucion, LocalDate fechaLimite) throws SQLException {

        boolean entregaTardia =
                fechaDevolucion.isAfter(fechaLimite);

        int devolucionesDanadas =
                contarDevolucionesDanadas(
                        con,
                        idUsuario
                );

        String motivo = null;

        if (entregaTardia &&
                devolucionesDanadas >= 2) {

            motivo =
                    "Entrega fuera del plazo y acumuló 2 o más recursos dañados.";

        } else if (entregaTardia) {

            motivo =
                    "Entrega de préstamo fuera del plazo establecido.";

        } else if (devolucionesDanadas >= 2) {

            motivo =
                    "Acumuló 2 o más devoluciones de recursos dañados.";
        }

        if (motivo != null) {

            String sql = """
                UPDATE USUARIO
                SET SANCIONADO = 1,
                    MOTIVO_SANCION = ?
                WHERE ID_USUARIO = ?
                """;

            try (PreparedStatement ps =
                         con.prepareStatement(sql)) {

                ps.setString(1, motivo);
                ps.setInt(2, idUsuario);

                ps.executeUpdate();
            }
        }
    }

    private void sancionarUsuario(Connection con, int idUsuario) throws SQLException {
        String sql = "UPDATE USUARIO SET SANCIONADO = 1 WHERE ID_USUARIO = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            ps.executeUpdate();
        }
    }

    private void sancionarUsuario(int idUsuario) throws SQLException {
        try (Connection con = Conexion.getConexion()) {
            sancionarUsuario(con, idUsuario);
        }
    }

    //es para eliminar usuarios
    public void eliminarUsuario(Object idUsuario) throws SQLException {
        String sql = "DELETE FROM USUARIO WHERE ID_USUARIO = ?";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            // Convertimos el Object a int de forma segura
            ps.setInt(1, Integer.parseInt(idUsuario.toString()));
            ps.executeUpdate();
        }
    }

    //es para eliminar prestamos
    public void eliminarPrestamo(Object idPrestamo) throws SQLException {
        String sql = "DELETE FROM PRESTAMO WHERE ID_PRESTAMO = ?";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, Integer.parseInt(idPrestamo.toString()));
            ps.executeUpdate();
        }
    }

    //es para eliminar recursos
    public void eliminarRecurso(Object idRecurso) throws SQLException {
        String sql = "DELETE FROM RECURSO WHERE ID_RECURSO = ?";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, Integer.parseInt(idRecurso.toString()));
            ps.executeUpdate();
        }
    }
}
