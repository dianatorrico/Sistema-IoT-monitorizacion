package mqtt;

import Database.ConectionDDBB;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import Database.Topics;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import logic.Log;

public class MQTTSuscriber implements MqttCallback {

    private MqttClient client;
    private String brokerUrl;
    private String clientId;
    private String username;
    private String password;

    public MQTTSuscriber(MQTTBroker broker) {
        this.brokerUrl = broker.getBroker();
        this.clientId = broker.getClientId();
        this.username = broker.getUsername();
        this.password = broker.getPassword();
    }

    public void subscribeTopic(String topic) {
        try {
            MemoryPersistence persistence = new MemoryPersistence();
            client = new MqttClient(brokerUrl, MQTTBroker.getSubscriberClientId(), persistence);

            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setUserName(username);
            connOpts.setPassword(password.toCharArray());
            connOpts.setCleanSession(false); // Para mantener la suscripción
            connOpts.setAutomaticReconnect(true); // Reconexión automática
            connOpts.setConnectionTimeout(10);

            client.setCallback(this);
            client.connect(connOpts);

            client.subscribe(topic, 1); // QoS 1 para asegurarse de recibir
            Log.logmqtt.info("Subscribed to {}", topic);

        } catch (MqttException e) {
            Log.logmqtt.error("Error subscribing to topic: {}", e);
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        Log.logmqtt.warn("MQTT Connection lost, cause: {}", cause.getMessage());
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
    String payload = message.toString();
    Log.logmqtt.info("MQTT message arrived. Topic: {}, Payload: {}", topic, payload);

    // Mantener la lógica de la plantilla (guardar el último valor en memoria)
    Topics newTopic = new Topics();
    newTopic.setIdTopic(topic);
    newTopic.setValue(payload);

    // Solo procesamos los topics de telemetría
    if (!topic.endsWith("/telemetry")) {
        return;
    }

    // -------- 1) Parsear JSON de la PL1 --------
    String dispositivoId = null;
    String estadoDispositivo = null;     // display_status
    String mensajeActual = null;         // current_message
    String tipoContenido = null;         // content_type
    Boolean luzAmbienteAlta = null;      // derivada de brightness_level
    Double co2ppm = null;               // co2_ppm
    String mensajeCalidadAire = null;   // air_quality_message

    try {
        Gson gson = new Gson();
        JsonObject root = gson.fromJson(payload, JsonObject.class);

        if (root == null) {
            Log.logmqtt.warn("Telemetry JSON is null, skipping insert");
            return;
        }

        // sensor_id -> dispositivo_id (debe existir en la tabla dispositivo)
        if (root.has("sensor_id") && !root.get("sensor_id").isJsonNull()) {
            dispositivoId = root.get("sensor_id").getAsString();
        }

        if (dispositivoId == null || dispositivoId.isEmpty()) {
            Log.logmqtt.warn("Telemetry without sensor_id, skipping insert");
            return;
        }

        // data{...}
        JsonObject data = null;
        if (root.has("data") && root.get("data").isJsonObject()) {
            data = root.getAsJsonObject("data");
        }

        if (data != null) {
            // display_status -> estado_dispositivo
            if (data.has("display_status") && !data.get("display_status").isJsonNull()) {
                estadoDispositivo = data.get("display_status").getAsString();
            }

            // current_message -> mensaje_actual
            if (data.has("current_message") && !data.get("current_message").isJsonNull()) {
                mensajeActual = data.get("current_message").getAsString();
            }

            // content_type -> tipo_contenido
            if (data.has("content_type") && !data.get("content_type").isJsonNull()) {
                tipoContenido = data.get("content_type").getAsString();
            }

            // brightness_level -> luz_ambiente_alta (ejemplo: true si brillo >= 0.5)
            if (data.has("brightness_level") && !data.get("brightness_level").isJsonNull()) {
                try {
                    String brillo = data.get("brightness_level").getAsString()
                                       .trim()
                                       .toLowerCase();

                    // Ajusta aquí según exactamente cómo venga en el JSON
                    if (brillo.equals("iluminado")) {
                        luzAmbienteAlta = true;   // hay luz
                    } else if (brillo.equals("oscuro")) {
                        luzAmbienteAlta = false;  // poca luz
                    } else {
                        Log.logmqtt.warn("brightness_level desconocido: {}", brillo);
                    }

                } catch (Exception e) {
                    Log.logmqtt.warn("No se ha podido parsear brightness_level como String", e);
                }
            }

            // co2_ppm
            if (data.has("co2_ppm") && !data.get("co2_ppm").isJsonNull()) {
                try {
                    co2ppm = data.get("co2_ppm").getAsDouble();
                } catch (Exception e) {
                    Log.logmqtt.warn("No se ha podido parsear co2_ppm", e);
                }
            }

            // air_quality_message -> mensaje_calidad_aire
            if (data.has("air_quality_message") && !data.get("air_quality_message").isJsonNull()) {
                mensajeCalidadAire = data.get("air_quality_message").getAsString();
            }
        }

    } catch (Exception e) {
        Log.logmqtt.error("Error parseando JSON de telemetría", e);
        return;
    }

    // -------- 2) Insertar en telemetria_dispositivo --------
    Connection con = null;
    PreparedStatement ps = null;

    try {
        ConectionDDBB bbdd = new ConectionDDBB();
        con = bbdd.obtainConnection(true);  // usa tu método habitual

        String sql =
            "INSERT INTO telemetria_dispositivo (" +
            "  dispositivo_id, instante, estado_dispositivo, mensaje_actual, tipo_contenido, " +
            "  luz_ambiente_alta, co2_ppm, mensaje_calidad_aire, fecha_actualizacion_contenido, json_completo" +
            ") VALUES (" +
            "  ?, ?, ?, ?, ?, ?, ?, ?, ?, ?::jsonb" +
            ")";
        
        Timestamp nowTs = new Timestamp(System.currentTimeMillis());

        ps = con.prepareStatement(sql);

        ps.setString(1, dispositivoId);        // dispositivo_id
        ps.setTimestamp(2, nowTs);             // instante (hora real del servidor)
        ps.setString(3, estadoDispositivo);    // estado_dispositivo
        ps.setString(4, mensajeActual);        // mensaje_actual
        ps.setString(5, tipoContenido);        // tipo_contenido


        if (luzAmbienteAlta != null) {
            ps.setBoolean(6, luzAmbienteAlta);    // luz_ambiente_alta
        } else {
            ps.setNull(6, java.sql.Types.BOOLEAN);
        }

        if (co2ppm != null) {
            ps.setDouble(7, co2ppm);              // co2_ppm
        } else {
            ps.setNull(7, java.sql.Types.NUMERIC);
        }

        ps.setString(8, mensajeCalidadAire);      // mensaje_calidad_aire
        ps.setTimestamp(9, nowTs);              // fecha_actualizacion_contenido
        ps.setString(10, payload);                // json_completo

        int rows = ps.executeUpdate();
        Log.logmqtt.info("Insertadas {} filas de telemetría para dispositivo {}", rows, dispositivoId);

    } catch (Exception e) {
        Log.logmqtt.error("Error insertando telemetría en la BBDD", e);
    } finally {
        if (ps != null) {
            try { ps.close(); } catch (SQLException ignore) {}
        }
        if (con != null) {
            try { con.close(); } catch (SQLException ignore) {}
        }
    }
}




    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
    }
}
