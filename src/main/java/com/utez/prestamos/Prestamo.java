package com.utez.prestamos;

import java.time.LocalDate;

/**
 * Clase de modelo de datos para Prestamo.
 * Contiene constructor, getters y setters según lo solicitado.
 */
public class Prestamo {
    private int id;
    private String solicitante;
    private String recurso;
    private LocalDate fechaPrestamo;
    private LocalDate fechaLimite;
    private String estado;

    public Prestamo() {}

    public Prestamo(int id, String solicitante, String recurso, LocalDate fechaPrestamo, LocalDate fechaLimite, String estado) {
        this.id = id;
        this.solicitante = solicitante;
        this.recurso = recurso;
        this.fechaPrestamo = fechaPrestamo;
        this.fechaLimite = fechaLimite;
        this.estado = estado;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getSolicitante() { return solicitante; }
    public void setSolicitante(String solicitante) { this.solicitante = solicitante; }

    public String getRecurso() { return recurso; }
    public void setRecurso(String recurso) { this.recurso = recurso; }

    public LocalDate getFechaPrestamo() { return fechaPrestamo; }
    public void setFechaPrestamo(LocalDate fechaPrestamo) { this.fechaPrestamo = fechaPrestamo; }

    public LocalDate getFechaLimite() { return fechaLimite; }
    public void setFechaLimite(LocalDate fechaLimite) { this.fechaLimite = fechaLimite; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}
