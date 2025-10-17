package com.test.email.services.models;


public class emailDTO {
    private String destinatario;
    private String asunto;
    private String mensaje;
    private Long codpers;
    private String token;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getNomb_usr() {
        return nomb_usr;
    }

    public void setNomb_usr(String nomb_usr) {
        this.nomb_usr = nomb_usr;
    }

    public Long getCodpers() {
        return codpers;
    }

    private String nomb_usr;  // para link alternativo (PK)

    public Long getCodpers(emailDTO email) {
        return codpers;
    }

    public void setCodpers(Long codpers) {
        this.codpers = codpers;
    }

    public String getDestinatario() {
        return destinatario;
    }

    public void setDestinatario(String destinatario) {
        this.destinatario = destinatario;
    }

    public String getAsunto() {
        return asunto;
    }

    public void setAsunto(String asunto) {
        this.asunto = asunto;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}
