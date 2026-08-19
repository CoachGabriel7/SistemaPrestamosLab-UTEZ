package com.utez.prestamos;

/**
 * Clase de modelo de datos para Usuario.
 * Contiene constructor, getters y setters según lo solicitado.
 */
public class Usuario {
    private int id;
    private String nombre;
    private String apellido;
    private String matricula;
    private String tipo;
    private String correo;
    private String telefono;
    private String estado;

    // Constructor vacío
    public Usuario() {}

    // Constructor con parámetros
    public Usuario(int id, String nombre, String apellido, String matricula, String tipo, String correo, String telefono, String estado) {
        this.id = id;
        this.nombre = nombre;
        this.apellido = apellido;
        this.matricula = matricula;
        this.tipo = tipo;
        this.correo = correo;
        this.telefono = telefono;
        this.estado = estado;
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public String getMatricula() { return matricula; }
    public void setMatricula(String matricula) { this.matricula = matricula; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}
