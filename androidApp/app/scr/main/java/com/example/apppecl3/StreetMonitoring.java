package com.example.apppecl3;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import androidx.appcompat.app.AppCompatDelegate;

import androidx.appcompat.app.AppCompatDelegate;
import android.os.Handler;


public class StreetMonitoring extends AppCompatActivity {

    private String street_id;
    private String device_id;


    // ===== UI =====
    private TextView streetName;
    private TextView tvInstante, tvMensaje, tvCo2, tvLuz, tvCalidad, tvConexion;

    private String ultimoModoTema = "";

    private Button btnSoloFecha;
    private Button btnFechaHora;

    // ===== Filtros =====
    private String fechaSeleccionada;
    private String horaInicio;
    private String horaFin;

    // ===== MQTT =====
    private MqttClient mqttClient;
    private static final String MQTT_BROKER = "tcp://10.0.2.2:1883";
    private long lastMqttMessageAt = 0L;
    private boolean conectado = false;

    private final android.os.Handler mqttHandler = new android.os.Handler(android.os.Looper.getMainLooper());
    private Runnable mqttWatchdog;


    // ----- Tiempo de actualizacion ----------
    private static final long MQTT_TIMEOUT_MS = 8000;   // si no llega nada en 10s => no conectado
    private static final long WATCHDOG_TICK_MS = 1000;   // comprobar cada 1s

    // ------- Cambio de temas
    private final Handler themeHandler = new Handler();
    private Runnable themeRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("DEBUG_MONITORING", "StreetMonitoring abierta");
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_street_monitoring);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // ===== UI =====
        streetName  = findViewById(R.id.streetname);
        tvInstante = findViewById(R.id.tvRtInstante);
        tvMensaje  = findViewById(R.id.tvRtMensaje);
        tvCo2      = findViewById(R.id.tvRtCo2);
        tvLuz      = findViewById(R.id.tvRtLuz);
        tvCalidad  = findViewById(R.id.tvRtCalidadAire);


        btnSoloFecha = findViewById(R.id.btnFecha);
        btnFechaHora = findViewById(R.id.btnHoraInicio);
        tvConexion = findViewById(R.id.tvConexion);

        iniciarWatchdogConexion();

        // ===== Extras =====
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            street_id = extras.getString("street_id");
            device_id = extras.getString("device_id");

            streetName.setText("Dispositivo: " + device_id);
            conectarMQTT();
        }

        btnSoloFecha.setOnClickListener(v -> seleccionarFechaSolo());
        btnFechaHora.setOnClickListener(v -> seleccionarFechaHora());
    }

    // =====================================================
    // ================= MQTT ==============================
    // =====================================================

    private void conectarMQTT() {
        try {
            mqttClient = new MqttClient(
                    MQTT_BROKER,
                    MqttClient.generateClientId(),
                    new MemoryPersistence()
            );

            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            mqttClient.connect(options);

            String topic = "madrid/sensors/" + street_id + "/telemetry";

            mqttClient.subscribe(topic, (t, msg) -> {
                String payload = new String(msg.getPayload());
                runOnUiThread(() -> procesarTelemetriaMQTT(payload));
            });

        } catch (MqttException e) {
            Log.e("ubicua", "Error MQTT", e);
        }
    }

    private void procesarTelemetriaMQTT(String json) {
        try {
            Log.i("MQTT_JSON", json); // JSON completo en logs

            lastMqttMessageAt = System.currentTimeMillis();
            // Si estaba como no conectado, al llegar un mensaje forzamos UI
            if (!conectado) {
                conectado = true;
                actualizarUIConexion(true);
            }
            JSONObject root = new JSONObject(json);
            JSONObject data = root.getJSONObject("data");

            // ===== INFO GENERAL =====
            String sensorId   = root.optString("sensor_id", "-");
            String sensorType = root.optString("sensor_type", "-");
            String streetId   = root.optString("street_id", "-");

            Log.d("DEBUG_MQTT",
                    "sensorId=" + sensorId +
                            " | sensorType=" + sensorType +
                            " | street=" + streetId
            );

            // ===== TIMESTAMP LOCAL =====
            String ahora = new SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss",
                    Locale.getDefault()
            ).format(new Date());

            tvInstante.setText("Instante: " + ahora);

            // ===== MENSAJES =====
            tvMensaje.setText(
                    "Mensaje: " + data.optString("current_message", "-")
            );

            tvCo2.setText(
                    "CO₂: " + data.optDouble("co2_ppm", -1) + " ppm"
            );

            // ===== LUZ + EMOJI =====
            String luz = data.optString("brightness_level", "-");
            tvLuz.setText("Luz ambiente: " + luz);

            aplicarTemaPorLuminosidad(luz);

            TextView tvLuzEmoji = findViewById(R.id.tvLuzEmoji);
            if ("Iluminado".equalsIgnoreCase(luz)) {
                tvLuzEmoji.setText("☀️");
            } else {
                tvLuzEmoji.setText("🌙");
            }


            // ===== CALIDAD AIRE =====
            tvCalidad.setText(
                    "Calidad del aire: " +
                            data.optString("air_quality_message", "-")
            );
        } catch (Exception e) {
            Log.e("ubicua", "Error parseando MQTT", e);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (mqttClient != null && mqttClient.isConnected()) {
                mqttClient.disconnect();
                mqttHandler.removeCallbacksAndMessages(null);

            }
        } catch (MqttException ignored) {}
    }

    // =====================================================
    // ================= FILTROS ===========================
    // =====================================================

    private void seleccionarFechaSolo() {
        Calendar c = Calendar.getInstance();

        new DatePickerDialog(this,
                (v, y, m, d) -> {
                    fechaSeleccionada = String.format("%04d-%02d-%02d", y, m + 1, d);
                    abrirTelemetriaSoloFecha();
                },
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private void abrirTelemetriaSoloFecha() {
        Intent i = new Intent(this, TelemetriaActivity.class);
        i.putExtra("device_id", device_id);
        i.putExtra("fecha", fechaSeleccionada);
        startActivity(i);
    }

    private void seleccionarFechaHora() {
        Calendar c = Calendar.getInstance();

        new DatePickerDialog(this,
                (v, y, m, d) -> {
                    fechaSeleccionada = String.format("%04d-%02d-%02d", y, m + 1, d);
                    seleccionarHoraInicio();
                },
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private void seleccionarHoraInicio() {
        Calendar c = Calendar.getInstance();

        new TimePickerDialog(this,
                (v, h, m) -> {
                    horaInicio = String.format("%02d:%02d", h, m);
                    seleccionarHoraFin();
                },
                c.get(Calendar.HOUR_OF_DAY),
                c.get(Calendar.MINUTE),
                true
        ).show();
    }

    private void seleccionarHoraFin() {
        Calendar c = Calendar.getInstance();

        new TimePickerDialog(this,
                (v, h, m) -> {
                    horaFin = String.format("%02d:%02d", h, m);
                    abrirTelemetriaFechaHora();
                },
                c.get(Calendar.HOUR_OF_DAY),
                c.get(Calendar.MINUTE),
                true
        ).show();
    }

    private void abrirTelemetriaFechaHora() {
        Intent i = new Intent(this, TelemetriaActivity.class);
        i.putExtra("device_id", device_id);
        i.putExtra("fecha", fechaSeleccionada);
        i.putExtra("horaInicio", horaInicio);
        i.putExtra("horaFin", horaFin);
        startActivity(i);
    }

    private void iniciarWatchdogConexion() {
        mqttWatchdog = () -> {
            long ahora = System.currentTimeMillis();
            boolean hayMensajesRecientes = lastMqttMessageAt != 0L && (ahora - lastMqttMessageAt) <= MQTT_TIMEOUT_MS;

            // Solo refrescar UI si cambia el estado
            if (hayMensajesRecientes != conectado) {
                conectado = hayMensajesRecientes;
                actualizarUIConexion(conectado);
            }

            mqttHandler.postDelayed(mqttWatchdog, WATCHDOG_TICK_MS);
        };

        mqttHandler.postDelayed(mqttWatchdog, WATCHDOG_TICK_MS);
    }

    private void actualizarUIConexion(boolean conectado) {
        if (conectado) {
            tvConexion.setText("✅ Dispositivo conectado");

        } else {
            tvConexion.setText("❌ Dispositivo no conectado");
        }
    }

    private void aplicarTemaPorLuminosidad(String luminosidad) {
        if (luminosidad == null) return;

        String nuevoModo = luminosidad.equalsIgnoreCase("Iluminado")
                ? "DAY"
                : "NIGHT";

        // ⛔️ No cambiar si es el mismo modo
        if (nuevoModo.equals(ultimoModoTema)) return;

        ultimoModoTema = nuevoModo;

        themeHandler.postDelayed(() -> {
            if ("NIGHT".equals(nuevoModo)) {
                AppCompatDelegate.setDefaultNightMode(
                        AppCompatDelegate.MODE_NIGHT_YES
                );
            } else {
                AppCompatDelegate.setDefaultNightMode(
                        AppCompatDelegate.MODE_NIGHT_NO
                );
            }
        }, 500);
    }







}
