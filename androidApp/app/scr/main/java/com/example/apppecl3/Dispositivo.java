package com.example.apppecl3;

import com.google.gson.annotations.SerializedName;

public class Dispositivo {

    // Viene como "id" en el JSON
    @SerializedName("id")
    private String dispositivoId;

    //nombre del dispositivo
    @SerializedName("nombre")
    private String nombre;

    // Viene como "calle" en el JSON
    @SerializedName("calle")
    private String calleId;

    // Opcional (si no viene, será null)
    @SerializedName("tipo_dispositivo")
    private String tipoDispositivo;

    // Opcional (si no viene, queda false por defecto)
    @SerializedName("activo")
    private Boolean activo;

    // No viene del endpoint /api/dispositivos
    // Se rellenará más adelante por REST o MQTT
    private Telemetria estadoActual;

    public Dispositivo() {
    }

    public Dispositivo(String dispositivoId,
                       String calleId,
                       String tipoDispositivo,
                       boolean activo,
                       String nombre) {
        this.dispositivoId = dispositivoId;
        this.calleId = calleId;
        this.tipoDispositivo = tipoDispositivo;
        this.activo = activo;
        this.nombre = nombre;
    }

    public String getDispositivoId() {
        return dispositivoId;
    }

    public void setDispositivoId(String dispositivoId) {
        this.dispositivoId = dispositivoId;
    }

    public String getNombre(){
        return nombre;
    }
    public String getCalleId() {
        return calleId;
    }

    public void setCalleId(String calleId) {
        this.calleId = calleId;
    }

    public String getTipoDispositivo() {
        return tipoDispositivo;
    }

    public void setTipoDispositivo(String tipoDispositivo) {
        this.tipoDispositivo = tipoDispositivo;
    }

    public boolean isActivo() {
        // Protección contra null
        return activo != null && activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public Telemetria getEstadoActual() {
        return estadoActual;
    }

    public void setEstadoActual(Telemetria estadoActual) {
        this.estadoActual = estadoActual;
    }

    @Override
    public String toString() {
        return dispositivoId != null ? dispositivoId : "(dispositivo sin ID)";
    }
}

