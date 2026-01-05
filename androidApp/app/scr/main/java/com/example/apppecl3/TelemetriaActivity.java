package com.example.apppecl3;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TelemetriaActivity extends AppCompatActivity {

    private String deviceId;
    private String fecha;
    private String horaInicio;
    private String horaFin;

    private TextView tvFecha;
    private LinearLayout contenedorResultados;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_telemetria);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tvFecha = findViewById(R.id.tvFecha);
        contenedorResultados = findViewById(R.id.contenedorResultados);

        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            mostrarError("No llegaron datos");
            return;
        }

        deviceId = extras.getString("device_id");
        fecha = extras.getString("fecha");
        horaInicio = extras.getString("horaInicio");
        horaFin = extras.getString("horaFin");

        Log.i("ubicua", "TelemetriaActivity device=" + deviceId);
        Log.i("ubicua", "fecha=" + fecha);
        Log.i("ubicua", "horaInicio=" + horaInicio);
        Log.i("ubicua", "horaFin=" + horaFin);

        boolean usarHoras =
                horaInicio != null && !horaInicio.isEmpty()
                        && horaFin != null && !horaFin.isEmpty();

        if (usarHoras) {
            tvFecha.setText("Fecha: " + fecha + " (" + horaInicio + " - " + horaFin + ")");
            cargarTelemetriaPorHora();
        } else {
            tvFecha.setText("Fecha: " + fecha);
            cargarTelemetriaPorFecha();
        }
    }

    // ================= REST =================

    private void cargarTelemetriaPorFecha() {

        ApiService api = RetrofitClient.getRetrofitInstance().create(ApiService.class);

        api.getTelemetriaPorFecha(deviceId, fecha)
                .enqueue(new Callback<List<Telemetria>>() {
                    @Override
                    public void onResponse(Call<List<Telemetria>> call,
                                           Response<List<Telemetria>> response) {

                        if (response.isSuccessful() && response.body() != null) {
                            pintarTelemetrias(response.body());
                        } else {
                            mostrarError("Error del servidor");
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Telemetria>> call, Throwable t) {
                        Log.e("ubicua", "Error REST fecha", t);
                        mostrarError("No se pudo conectar");
                    }
                });
    }

    private void cargarTelemetriaPorHora() {

        ApiService api = RetrofitClient.getRetrofitInstance().create(ApiService.class);

        api.getTelemetriaPorHora(deviceId, fecha, horaInicio, horaFin)
                .enqueue(new Callback<List<Telemetria>>() {
                    @Override
                    public void onResponse(Call<List<Telemetria>> call,
                                           Response<List<Telemetria>> response) {

                        if (response.isSuccessful() && response.body() != null) {
                            pintarTelemetrias(response.body());
                        } else {
                            mostrarError("Error del servidor");
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Telemetria>> call, Throwable t) {
                        Log.e("ubicua", "Error REST hora", t);
                        mostrarError("No se pudo conectar");
                    }
                });
    }

    // ================= UI =================

    private void pintarTelemetrias(List<Telemetria> lista) {

        contenedorResultados.removeAllViews();

        if (lista.isEmpty()) {
            mostrarMensajeVacio();
            return;
        }

        for (Telemetria t : lista) {

            View fila = getLayoutInflater()
                    .inflate(R.layout.item_telemetria, contenedorResultados, false);

            TextView tvHora = fila.findViewById(R.id.tvHora);
            TextView tvMensaje = fila.findViewById(R.id.tvMensaje);
            TextView tvCo2 = fila.findViewById(R.id.tvCo2);
            TextView tvLuz = fila.findViewById(R.id.tvLuz);
            TextView tvCalidadAire = fila.findViewById(R.id.tvCalidadAire);

            // Hora
            if (t.getInstante() != null && t.getInstante().length() >= 16) {
                tvHora.setText(t.getInstante().substring(11, 16));
            } else {
                tvHora.setText("--:--");
            }

            tvMensaje.setText(t.getMensajeActual() != null ? t.getMensajeActual() : "-");
            tvCo2.setText(t.getCo2ppm() != null ? t.getCo2ppm() + " ppm" : "-");

            // LUZ (boolean real)
            Boolean luz = t.getLuzAmbienteAlta();
            tvLuz.setText(luz == null ? "-" : (luz ? "Alta" : "Baja"));

            tvCalidadAire.setText(
                    t.getMensajeCalidadAire() != null ? t.getMensajeCalidadAire() : "-"
            );

            contenedorResultados.addView(fila);
        }
    }

    private void mostrarMensajeVacio() {
        contenedorResultados.removeAllViews();
        TextView tv = new TextView(this);
        tv.setText("No hay datos para este filtro");
        tv.setPadding(16, 16, 16, 16);
        contenedorResultados.addView(tv);
    }

    private void mostrarError(String mensaje) {
        contenedorResultados.removeAllViews();
        TextView tv = new TextView(this);
        tv.setText("⚠️ " + mensaje);
        tv.setPadding(16, 16, 16, 16);
        contenedorResultados.addView(tv);
    }
}
