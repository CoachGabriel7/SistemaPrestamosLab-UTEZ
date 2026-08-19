package com.utez.prestamos;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import com.toedter.calendar.JDateChooser;
import java.time.DayOfWeek;
import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;

abstract class DialogoBase extends JDialog {
    protected final VentanaPrincipal ventana;
    protected final Repositorio repositorio;
    protected final JPanel formulario = new JPanel(new GridBagLayout());
    private int fila = 0;

    protected DialogoBase(VentanaPrincipal ventana, Repositorio repositorio, String titulo) {
        super(ventana, titulo, true);
        this.ventana = ventana;
        this.repositorio = repositorio;
        formulario.setBackground(Color.WHITE);
        formulario.setBorder(BorderFactory.createEmptyBorder(18, 24, 10, 24));

        JLabel encabezado = new JLabel(titulo);
        encabezado.setFont(new Font("Montserrat", Font.BOLD, 22));
        encabezado.setForeground(Estilo.VERDE_OSCURO);
        encabezado.setBorder(BorderFactory.createEmptyBorder(22, 24, 5, 24));

        JButton cancelar = new JButton("Cancelar");
        JButton guardar = Estilo.botonPrincipal("Guardar");
        cancelar.addActionListener(e -> dispose());
        guardar.addActionListener(e -> guardar());

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        botones.setBackground(Color.WHITE);
        botones.setBorder(BorderFactory.createEmptyBorder(0, 15, 12, 15));
        botones.add(cancelar);
        botones.add(guardar);

        setLayout(new BorderLayout());
        add(encabezado, BorderLayout.NORTH);
        add(formulario, BorderLayout.CENTER);
        add(botones, BorderLayout.SOUTH);
        setMinimumSize(new Dimension(520, 420));
        pack();
        setLocationRelativeTo(ventana);
    }

    protected void agregar(String etiqueta, java.awt.Component campo) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridy = fila++;
        g.insets = new Insets(8, 5, 8, 5);
        g.anchor = GridBagConstraints.WEST;
        g.gridx = 0;
        formulario.add(new JLabel(etiqueta), g);
        g.gridx = 1;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        if (campo instanceof JTextField tf) tf.setPreferredSize(new Dimension(280, 34));
        formulario.add(campo, g);
    }

    protected void exito(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje);
        ventana.actualizarTodo();
        dispose();
    }

    protected void fallo(Exception e) {
        JOptionPane.showMessageDialog(this, e.getMessage(), "No se pudo guardar", JOptionPane.ERROR_MESSAGE);
    }

    protected abstract void guardar();
}

class DialogoPrestamo extends DialogoBase {

    private final JComboBox<Repositorio.Opcion> usuario =
            new JComboBox<>();

    private final JComboBox<Repositorio.Opcion> recurso =
            new JComboBox<>();

    private final JComboBox<Repositorio.Opcion> estadoFisico =
            new JComboBox<>();

    private final JDateChooser fechaInicio =
            new JDateChooser();

    private final JDateChooser fechaLimite =
            new JDateChooser();

    DialogoPrestamo(
            VentanaPrincipal ventana,
            Repositorio repositorio
    ) {
        super(
                ventana,
                repositorio,
                "Registrar nuevo préstamo"
        );

        configurarCalendarios();

        agregar("Solicitante:", usuario);
        agregar("Recurso disponible:", recurso);
        agregar("Estado físico de entrega:", estadoFisico);
        agregar("Fecha de préstamo:", fechaInicio);
        agregar("Fecha límite (5 días hábiles):", fechaLimite);

        cargarCatalogos();
    }

    private void configurarCalendarios() {

        // Formato que se mostrará en pantalla
        fechaInicio.setDateFormatString("yyyy-MM-dd");
        fechaLimite.setDateFormatString("yyyy-MM-dd");

        // Calendario en español de México
        fechaInicio.setLocale(
                Locale.forLanguageTag("es-MX")
        );

        fechaLimite.setLocale(
                Locale.forLanguageTag("es-MX")
        );

        // Tamaño de los controles
        fechaInicio.setPreferredSize(
                new Dimension(280, 34)
        );

        fechaLimite.setPreferredSize(
                new Dimension(280, 34)
        );

        // Colocar automáticamente la fecha actual
        LocalDate fechaActual = LocalDate.now();

        fechaInicio.setDate(
                convertirADate(fechaActual)
        );

        // Calcular inicialmente la fecha límite
        actualizarFechaLimite();

        /*
         * Cada vez que se seleccione otra fecha de préstamo,
         * se volverán a calcular los 5 días hábiles.
         */
        fechaInicio.addPropertyChangeListener(
                "date",
                evento -> actualizarFechaLimite()
        );

        /*
         * La fecha límite no se podrá modificar manualmente,
         * porque el sistema la calcula automáticamente.
         */
        fechaLimite.setEnabled(false);
    }

    private void actualizarFechaLimite() {

        if (fechaInicio.getDate() == null) {
            fechaLimite.setDate(null);
            return;
        }

        LocalDate inicio =
                convertirALocalDate(fechaInicio.getDate());

        LocalDate limite =
                sumarDiasHabiles(inicio, 5);

        fechaLimite.setDate(
                convertirADate(limite)
        );
    }

    private LocalDate sumarDiasHabiles(
            LocalDate fecha,
            int cantidadDias
    ) {
        LocalDate resultado = fecha;
        int diasAgregados = 0;

        while (diasAgregados < cantidadDias) {

            resultado = resultado.plusDays(1);

            DayOfWeek diaSemana =
                    resultado.getDayOfWeek();

            if (diaSemana != DayOfWeek.SATURDAY
                    && diaSemana != DayOfWeek.SUNDAY) {

                diasAgregados++;
            }
        }

        return resultado;
    }

    private LocalDate convertirALocalDate(Date fecha) {

        return fecha.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }

    private Date convertirADate(LocalDate fecha) {

        return Date.from(
                fecha.atStartOfDay(
                        ZoneId.systemDefault()
                ).toInstant()
        );
    }

    private void cargarCatalogos() {

        try {
            repositorio.usuarios()
                    .forEach(usuario::addItem);

            repositorio.recursosDisponibles()
                    .forEach(recurso::addItem);

            repositorio.estadosFisicos()
                    .forEach(estadoFisico::addItem);

        } catch (Exception e) {
            fallo(e);
        }
    }

    @Override
    protected void guardar() {

        try {
            Repositorio.Opcion u =
                    (Repositorio.Opcion)
                            usuario.getSelectedItem();

            Repositorio.Opcion r =
                    (Repositorio.Opcion)
                            recurso.getSelectedItem();

            Repositorio.Opcion ef =
                    (Repositorio.Opcion)
                            estadoFisico.getSelectedItem();

            if (u == null || r == null || ef == null) {
                throw new IllegalArgumentException(
                        "Selecciona todos los datos."
                );
            }

            if (fechaInicio.getDate() == null) {
                throw new IllegalArgumentException(
                        "Selecciona la fecha del préstamo."
                );
            }

            LocalDate inicio =
                    convertirALocalDate(
                            fechaInicio.getDate()
                    );

            LocalDate limite =
                    sumarDiasHabiles(inicio, 5);

            repositorio.registrarPrestamo(
                    u.id(),
                    r.id(),
                    ef.id(),
                    inicio,
                    limite
            );

            exito(
                    "¡Préstamo guardado en Oracle!"
            );

        } catch (Exception e) {
            fallo(e);
        }
    }
}

class DialogoDevolucion extends DialogoBase {

    private final int idPrestamo;

    private final JComboBox<Repositorio.Opcion> estadoFisico =
            new JComboBox<>();

    private final JTextField comentarios =
            new JTextField();

    DialogoDevolucion(VentanaPrincipal ventana, Repositorio repositorio, int idPrestamo
    ) {
        super(ventana, repositorio, "Registrar devolución");

        this.idPrestamo = idPrestamo;

        agregar("Estado físico:", estadoFisico);
        agregar("Comentarios:", comentarios);

        try {
            repositorio.estadosFisicos()
                    .forEach(estadoFisico::addItem);
        } catch (Exception e) {
            fallo(e);
        }
    }

    @Override
    protected void guardar() {

        try {
            Repositorio.Opcion estado =
                    (Repositorio.Opcion)
                            estadoFisico.getSelectedItem();

            if (estado == null) {
                throw new IllegalArgumentException("Selecciona el estado del recurso.");
            }

            String nombreEstado =
                    estado.toString();

            if (nombreEstado.equalsIgnoreCase("DAÑADO")
                    && comentarios.getText().isBlank()) {

                throw new IllegalArgumentException("Debes escribir el estado del recurso si está dañado.");
            }

            repositorio.devolverPrestamo(
                    idPrestamo,
                    estado.id(),
                    comentarios.getText()
            );

            exito(
                    "Préstamo devuelto correctamente.");

        } catch (Exception e) {
            fallo(e);
        }
    }
}

class DialogoRecurso extends DialogoBase {
    private final JTextField nombre = new JTextField();
    private final JTextField serie = new JTextField();
    private final JComboBox<Repositorio.Opcion> tipo = new JComboBox<>();
    private final JComboBox<Repositorio.Opcion> estado = new JComboBox<>();

    DialogoRecurso(VentanaPrincipal ventana, Repositorio repositorio) {
        super(ventana, repositorio, "Agregar recurso");
        agregar("Nombre o descripción:", nombre);
        agregar("Número de serie:", serie);
        agregar("Tipo de recurso:", tipo);
        agregar("Estado físico:", estado);
        try {
            repositorio.tiposRecurso().forEach(tipo::addItem);
            repositorio.estadosFisicos().forEach(estado::addItem);
        } catch (Exception e) { fallo(e); }
    }

    @Override protected void guardar() {
        try {
            if (nombre.getText().isBlank() || serie.getText().isBlank()) throw new IllegalArgumentException("Completa todos los campos.");
            Repositorio.Opcion tr = (Repositorio.Opcion) tipo.getSelectedItem();
            Repositorio.Opcion ef = (Repositorio.Opcion) estado.getSelectedItem();
            if (tr == null || ef == null) throw new IllegalArgumentException("Selecciona el tipo y el estado.");
            repositorio.registrarRecurso(nombre.getText(), serie.getText(), tr.id(), ef.id());
            exito("Recurso guardado correctamente.");
        } catch (Exception e) { fallo(e); }
    }
}

class DialogoUsuario extends DialogoBase {
    private final Integer idUsuario;
    private final JTextField nombre = new JTextField();
    private final JTextField apellido = new JTextField();
    private final JTextField matricula = new JTextField();
    private final JTextField correo = new JTextField();
    private final JTextField telefono = new JTextField();
    private final JComboBox<Repositorio.Opcion> tipo = new JComboBox<>();

    DialogoUsuario(VentanaPrincipal ventana, Repositorio repositorio) {
        this(ventana, repositorio, null);
    }

    DialogoUsuario(VentanaPrincipal ventana, Repositorio repositorio, Integer idUsuario) {
        super(ventana, repositorio, idUsuario == null ? "Registrar usuario" : "Editar usuario");
        this.idUsuario = idUsuario;
        agregar("Nombre:", nombre);
        agregar("Apellido:", apellido);
        agregar("Matrícula:", matricula);
        agregar("Correo:", correo);
        agregar("Teléfono:", telefono);
        agregar("Tipo de usuario:", tipo);
        try {
            repositorio.tiposUsuario().forEach(tipo::addItem);
            if (idUsuario != null) {
                cargarDatos();
            }
        } catch (Exception e) { fallo(e); }
    }

    private void cargarDatos() {
        try {
            Object[] datos = repositorio.obtenerDatosUsuario(idUsuario);
            if (datos != null) {
                nombre.setText((String) datos[0]);
                apellido.setText((String) datos[1]);
                matricula.setText((String) datos[2]);
                correo.setText((String) datos[3]);
                telefono.setText((String) datos[4]);
                // Evitamos error de casting si el ID viene de Oracle como BigDecimal
                int idTipo = Integer.parseInt(datos[5].toString());
                for (int i = 0; i < tipo.getItemCount(); i++) {
                    if (tipo.getItemAt(i).id() == idTipo) {
                        tipo.setSelectedIndex(i);
                        break;
                    }
                }
            }
        } catch (Exception e) { fallo(e); }
    }

    @Override protected void guardar() {
        try {
            if (nombre.getText().isBlank() || apellido.getText().isBlank()
                    || matricula.getText().isBlank() || correo.getText().isBlank()) {
                throw new IllegalArgumentException("Completa nombre, apellido, matrícula y correo.");
            }
            Repositorio.Opcion tu = (Repositorio.Opcion) tipo.getSelectedItem();
            if (tu == null) throw new IllegalArgumentException("Selecciona el tipo de usuario.");

            if (idUsuario == null) {
                repositorio.registrarUsuario(nombre.getText(), apellido.getText(), matricula.getText(),
                        correo.getText(), telefono.getText(), tu.id());
                exito("Usuario guardado correctamente.");
            } else {
                repositorio.actualizarUsuario(idUsuario, nombre.getText(), apellido.getText(), matricula.getText(),
                        correo.getText(), telefono.getText(), tu.id());
                exito("Usuario actualizado correctamente.");
            }
        } catch (Exception e) { fallo(e); }
    }
}
