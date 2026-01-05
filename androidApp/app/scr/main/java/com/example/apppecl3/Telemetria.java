package com.example.apppecl3;

import com.google.gson.annotations.SerializedName;

public class Telemetria {

    @SerializedName("instante")
    private String instante;

    @SerializedName("estado_dispositivo")
    private String estadoDispositivo;

    @SerializedName("mensaje_actual")
    private String mensajeActual;

    @SerializedName("tipo_contenido")
    private String tipoContenido;

    @SerializedName("luz_ambiente_alta")
    private Boolean luzAmbienteAlta;

    @SerializedName("co2_ppm")
    private Double co2ppm;

    @SerializedName("mensaje_calidad_aire")
    private String mensajeCalidadAire;

    public Telemetria() {
    }

    public String getInstante() {
        return instante;
    }

    public void setInstante(String instante) {
        this.instante = instante;
    }

    public String getEstadoDispositivo() {
        return estadoDispositivo;
    }

    public void setEstadoDispositivo(String estadoDispositivo) {
        this.estadoDispositivo = estadoDispositivo;
    }

    public String getMensajeActual() {
        return mensajeActual;
    }

    public void setMensajeActual(String mensajeActual) {
        this.mensajeActual = mensajeActual;
    }

    public String getTipoContenido() {
        return tipoContenido;
    }

    public void setTipoContenido(String tipoContenido) {
        this.tipoContenido = tipoContenido;
    }

    public Boolean getLuzAmbienteAlta() {
        return luzAmbienteAlta;
    }

    public void setLuzAmbienteAlta(Boolean luzAmbienteAlta) {
        this.luzAmbienteAlta = luzAmbienteAlta;
    }

    public Double getCo2ppm() {
        return co2ppm;
    }

    public void setCo2ppm(Double co2ppm) {
        this.co2ppm = co2ppm;
    }

    public String getMensajeCalidadAire() {
        return mensajeCalidadAire;
    }

    public void setMensajeCalidadAire(String mensajeCalidadAire) {
        this.mensajeCalidadAire = mensajeCalidadAire;
    }
}
