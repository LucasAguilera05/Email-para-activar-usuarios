package com.test.email.services.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity @Table(name = "usuarios_verific", schema="public")
public class usuariosVerific {

    @Id
    @Column(name = "id_usuarios_verific")
    private Long idUsuariosVerific;

    @Column(name = "codpers")
    private Long codpers;


    @Column(name = "nomb_usr")          // <--- importante: buscamos por este campo
    private String nombUsr;

    public String getNombUsr() {
        return nombUsr;
    }

    public void setNombUsr(String nombUsr) {
        this.nombUsr = nombUsr;
    }

    @Column(name = "email_confirm")
    private Boolean emailConfirm; // opcional (ej: "YES"/"NO")

    @Column(name = "token")
    private String token;

    @Column(name = "fec_expira")
    private LocalDateTime fecExpira;

    @Column(name = "fec_verificado")
    private LocalDateTime fecVerificado;

    // getters/setters
    public Long getIdUsuariosVerific() {
        return idUsuariosVerific;
    }
    public void setIdUsuariosVerific(Long idUsuariosVerific) {
        this.idUsuariosVerific = idUsuariosVerific;
    }
    public Long getCodpers() {
        return codpers;
    }
    public void setCodpers(Long codpers) {
        this.codpers = codpers;
    }
    public Boolean getEmailConfirm() {
        return emailConfirm;
    }
    public void setEmailConfirm(Boolean emailConfirm) {
        this.emailConfirm = emailConfirm;
    }
    public String getToken() {
        return token;
    }
    public void setToken(String token) {
        this.token = token;
    }
    public LocalDateTime getFecExpira() {
        return fecExpira;
    }
    public void setFecExpira(LocalDateTime fecExpira) {
        this.fecExpira = fecExpira;
    }
    public LocalDateTime getFecVerificado() {
        return fecVerificado;
    }
    public void setFecVerificado(LocalDateTime fecVerificado) {
        this.fecVerificado = fecVerificado;
    }
}