package com.test.email.services.models;

import jakarta.persistence.*;

@Entity
@Table(name = "persxcontacto", schema = "public")

public class PersContacto {

    @Id
    @Column(name = "codpers", nullable = false)
    private Long codpers;

    @Column(name = "correo1", length = 320)
    private String correo1;

    @Column(name = "correo2", length = 320)
    private String correo2;

    // --- getters/setters ---
    public Long getCodpers() {
        return codpers;
    }
    public void setCodpers(Long codpers) {
        this.codpers = codpers;
    }
    public String getCorreo1() {
        return correo1;
    }
    public void setCorreo1(String correo1) {
        this.correo1 = correo1;
    }
    public String getCorreo2() {
        return correo2;
    }
    public void setCorreo2(String correo2) {
        this.correo2 = correo2;
    }
}
