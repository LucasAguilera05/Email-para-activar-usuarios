package com.test.email.services.models;


import jakarta.persistence.*;

// services/models/Usuario.java
@Entity @Table(name = "usuarios", schema = "public")
public class Usuario {
    @Id
    @Column(name="nomb_usr", nullable=false, length=100)
    private String nombUsr;

    @Column(name="activo", nullable=false)
    private boolean activo;

    // Si tu tabla tiene este campo, déjalo; si NO lo tiene, borralo y usamos solo nomb_usr
    @Column(name="codpers")
    private Long codpers;

    // getters/setters...

    public String getNombUsr() {
        return nombUsr;
    }

    public void setNombUsr(String nombUsr) {
        this.nombUsr = nombUsr;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public Long getCodpers() {
        return codpers;
    }

    public void setCodpers(Long codpers) {
        this.codpers = codpers;
    }
}

