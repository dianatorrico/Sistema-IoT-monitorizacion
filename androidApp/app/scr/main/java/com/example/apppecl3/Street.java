package com.example.apppecl3;


import java.util.List;

public class Street {

    private String streetId;     // ST_0947
    private String nombre;       // Calle de Maria Odiaga
    private String barrio;
    private String codigoPostal;

    private List<Dispositivo> dispositivos;

    public Street() {
    }

    public Street(String streetId, String nombre, String barrio, String codigoPostal) {
        this.streetId = streetId;
        this.nombre = nombre;
        this.barrio = barrio;
        this.codigoPostal = codigoPostal;
    }

    public String getStreetId() {
        return streetId;
    }

    public void setStreetId(String streetId) {
        this.streetId = streetId;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getBarrio() {
        return barrio;
    }

    public void setBarrio(String barrio) {
        this.barrio = barrio;
    }

    public String getCodigoPostal() {
        return codigoPostal;
    }

    public void setCodigoPostal(String codigoPostal) {
        this.codigoPostal = codigoPostal;
    }

    public List<Dispositivo> getDispositivos() {
        return dispositivos;
    }

    public void setDispositivos(List<Dispositivo> dispositivos) {
        this.dispositivos = dispositivos;
    }

    @Override
    public String toString() {
        return nombre != null ? nombre : streetId;
    }
}
