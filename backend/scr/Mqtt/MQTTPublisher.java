package mqtt;

import logic.Log;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MQTTPublisher {

    /**
     *
     * @param broker
     * @param topic
     * @param content
     */
    public static void publish(MQTTBroker broker, String topic, String content) {
        MemoryPersistence persistence = new MemoryPersistence();
        try {
            MqttClient sampleClient = new MqttClient(MQTTBroker.getBroker(), MQTTBroker.getPublisherClientId(), persistence);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setUserName(MQTTBroker.getUsername());
            connOpts.setPassword(MQTTBroker.getPassword().toCharArray());
            connOpts.setCleanSession(true);
            Log.logmqtt.info("Connecting to broker: " + MQTTBroker.getBroker());
            sampleClient.connect(connOpts);
            Log.logmqtt.info("Connected");
            MqttMessage message = new MqttMessage(content.getBytes());
            message.setQos(MQTTBroker.getQos());
            sampleClient.publish(topic, message);
            Log.logmqtt.info("Message published");
            sampleClient.disconnect();
            Log.logmqtt.info("Disconnected");

        } catch (MqttException me) {
            Log.logmqtt.error("Error on publishing value: {}", me);
        } catch (Exception e) {
            Log.logmqtt.error("Error on publishing value: {}", e);
        }
    }
    
        /**
     * Envía un mensaje al panel information_display de la PL1.
     * Topic: madrid/sensors/ST_0947/information_display
     *
     * @param text        Texto a mostrar en el panel
     * @param forceUpdate true si queremos forzar la actualización del contenido
     */
    
    public static void publishToInformationDisplay(String text, boolean forceUpdate) {
        // Topic de actuador de la PL1
        String topic = "madrid/sensors/ST_0947/information_display";

        if (text == null) {
            text = "";
        }

        // Escapar caracteres conflictivos para no romper el JSON
        String safeText = text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");

        // JSON que recibirá la ESP32 (ajusta campos si en la PL1 usaste otros)
        String payload = "{"
                + "\"sensor_type\":\"information_display\","
                + "\"data\":{"
                + "\"message\":\"" + safeText + "\","
                + "\"force_update\":" + (forceUpdate ? "true" : "false")
                + "}"
                + "}";

        // Usamos el publish de la plantilla. El parámetro MQTTBroker no se usa, así que pasamos null.
        publish(null, topic, payload);
    }

    
}
