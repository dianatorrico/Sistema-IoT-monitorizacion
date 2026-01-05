package com.example.apppecl3;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StreetSelection extends AppCompatActivity {

    private TextView btnSeleccionarCalle;
    private RecyclerView recyclerDevices;
    private DeviceAdapter adapter;

    // 🔹 Todos los dispositivos del servidor
    private final List<Dispositivo> todosLosDispositivos = new ArrayList<>();

    // 🔹 Dispositivos filtrados por calle
    private final List<Dispositivo> dispositivosCalle = new ArrayList<>();

    // 🔹 Calle seleccionada
    private String calleSeleccionada;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_street_selection);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnSeleccionarCalle = findViewById(R.id.searchStreet);
        recyclerDevices = findViewById(R.id.recyclerDevices);

        recyclerDevices.setLayoutManager(new LinearLayoutManager(this));

        adapter = new DeviceAdapter(dispositivosCalle, dispositivo -> {
            Log.i("ADAPTER", "CLICK en dispositivo: " + dispositivo.getDispositivoId());

            // ✅ SOLO panel_informativo abre monitoring
            if ("panel_informativo (ST_0947)".equalsIgnoreCase(dispositivo.getNombre())) {
                Intent intent = new Intent(
                        StreetSelection.this,
                        StreetMonitoring.class
                );
                intent.putExtra("street_id", dispositivo.getCalleId());
                intent.putExtra("device_id", dispositivo.getDispositivoId());
                startActivity(intent);
            } else {
                Log.i("ubicua", "Dispositivo no soportado: " + dispositivo.getTipoDispositivo());
            }
        });

        recyclerDevices.setAdapter(adapter);

        btnSeleccionarCalle.setOnClickListener(v -> mostrarSelectorCalles());

        obtenerDispositivos();
    }

    // =====================================================
    // ============== CARGA DE DISPOSITIVOS =================
    // =====================================================

    private void obtenerDispositivos() {

        ApiService apiService =
                RetrofitClient.getRetrofitInstance().create(ApiService.class);

        apiService.getDispositivos().enqueue(new Callback<List<Dispositivo>>() {
            @Override
            public void onResponse(Call<List<Dispositivo>> call,
                                   Response<List<Dispositivo>> response) {

                if (response.isSuccessful() && response.body() != null) {
                    todosLosDispositivos.clear();
                    todosLosDispositivos.addAll(response.body());

                    Log.i("ubicua", "Dispositivos cargados: " + todosLosDispositivos.size());

                } else {
                    Log.e("ubicua", "Error servidor: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Dispositivo>> call, Throwable t) {
                Log.e("ubicua", "Error red", t);
            }
        });
    }

    // =====================================================
    // ============== SELECTOR DE CALLES ====================
    // =====================================================

    private void mostrarSelectorCalles() {

        Set<String> callesSet = new HashSet<>();

        for (Dispositivo d : todosLosDispositivos) {
            if (d != null && d.getCalleId() != null) {
                callesSet.add(d.getCalleId());
            }
        }

        if (callesSet.isEmpty()) {
            Log.e("ubicua", "No hay calles disponibles");
            return;
        }

        String[] calles = callesSet.toArray(new String[0]);

        new AlertDialog.Builder(this)
                .setTitle("Selecciona una calle")
                .setItems(calles, (dialog, which) -> {
                    calleSeleccionada = calles[which];
                    btnSeleccionarCalle.setText(calleSeleccionada);
                    filtrarDispositivosPorCalle();
                })
                .show();
    }

    // =====================================================
    // ============== FILTRADO POR CALLE ====================
    // =====================================================

    private void filtrarDispositivosPorCalle() {

        dispositivosCalle.clear();

        for (Dispositivo d : todosLosDispositivos) {
            if (d != null && calleSeleccionada.equals(d.getCalleId())) {
                dispositivosCalle.add(d);
            }
        }

        adapter.notifyDataSetChanged();

        Log.i("ubicua",
                "Dispositivos en " + calleSeleccionada + ": " + dispositivosCalle.size());
    }
}
