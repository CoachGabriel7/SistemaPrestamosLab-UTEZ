package com.utez.prestamos;

/**
 * Clase de modelo de datos para Recurso.
 * Contiene constructor, getters y setters según lo solicitado.
 */
public class Recurso {
    private int id;
    private String nombre;
    private String numeroSerie;
    private String tipo;
    private String estado;

    public Recurso() {}

    public Recurso(int id, String nombre, String numeroSerie, String tipo, String estado) {
        this.id = id;
        this.nombre = nombre;
        this.numeroSerie = numeroSerie;
        this.tipo = tipo;
        this.estado = estado;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getNumeroSerie() { return numeroSerie; }
    public void setNumeroSerie(String numeroSerie) { this.numeroSerie = numeroSerie; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}
