package com.example.apppecl3;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;


public interface ApiService {

    @GET("api/dispositivos")
    Call<List<Dispositivo>> getDispositivos();

    // ---------- Telemetría por hora ----------
    @GET("api/telemetriaHora")
    Call<List<Telemetria>> getTelemetriaPorHora(
            @Query("dispositivo") String dispositivoId,
            @Query("fecha") String fecha,
            @Query("horaInicio") String horaInicio,
            @Query("horaFin") String horaFin
    );

    // ---------- Telemetría por fecha ----------
    @GET("api/telemetria")
    Call<List<Telemetria>> getTelemetriaPorFecha(
            @Query("dispositivo") String dispositivoId,
            @Query("fecha") String fecha
    );


}
