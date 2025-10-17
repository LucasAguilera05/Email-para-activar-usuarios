package com.test.email.services.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigInteger;
import java.time.Instant;

@Entity
@Table(name = "usuarios_verific", schema = "public")

public class UsuarioVerific {
    @Id
    @Column(name="id_usuarios_verific")
    private Integer id;

    @Column(name="codpers")
    private Long codpers;

    @Column(name="token")
    private String token;

    @Column(name="email_confirm")
    private Boolean emailConfirm;

    public Instant getFecExpira() {
        return fecExpira;
    }

    public void setFecExpira(Instant fecExpira) {
        this.fecExpira = fecExpira;
    }

    @Column(name="fec_expira")
    private java.time.Instant fecExpira;

    public Boolean getEmailConfirm() {
        return emailConfirm;
    }

    public void setEmailConfirm(Boolean emailConfirm) {
        this.emailConfirm = emailConfirm;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Long getCodpers() { return codpers; }   // ¡ojo: getCodpers(), no getCodPers()!
    public void setCodpers(Long codpers) { this.codpers = codpers; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
}
