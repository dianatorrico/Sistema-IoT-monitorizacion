#pragma once
#include <Arduino.h>
#include <WiFi.h>
#include <PubSubClient.h>
#include <ArduinoJson.h>

//funciones del .ino
void mostrarMensaje(const String& text);
void mostrarAlertaMeteo(const String& detalle);
void mostrarContadorTrafico(int vehiculos, float velocidadMedia, int peatones, int bicicletas);

//umbrales que hemos definidas en el .ino
extern const float ALERT_TEMP_HOT_C;
extern const float ALERT_TEMP_COLD_C;
extern const int   ALERT_AQI;
extern const float ALERT_WIND_KMH;
extern const float ALERT_WIND_KMH_WARN;
extern const int   ALERT_UV;
extern const float ALERT_PRESSURE_LOW_HPA;

//configuración del servidor (el del portatil)
//const char* MQTT_BROKER_ADRESS = "172.29.42.216"; 172, 29, 42, 216UNI
//casa
//const char* MQTT_BROKER_ADRESS = "192.168.1.20"; 
//prueba cambiamos a la direccion virtual del docker por algo de la privacidad en la wifi??

IPAddress MQTT_BROKER_ADRESS(192, 168, 1, 17); 
const uint16_t MQTT_PORT = 1883;
const char* MQTT_CLIENT_NAME = "ESP32L80000";

WiFiClient espClient;
PubSubClient mqttClient(espClient);

void SuscribeMqtt()
{
	mqttClient.subscribe(TOPIC_DISPLAY);
}

// String payload;

void PublisMqtt( String data){
	//Serial.print(data);
	if (!mqttClient.connected()) {
    Serial.println("[MQTT] No conectado; abort publish");
  }
	//cambiar el tamaño para que entre el JSON
  mqttClient.setBufferSize(1024);

  bool ok = mqttClient.publish(TOPIC_TELEMETRY, data.c_str(), true); // <- TELEMETRY
  Serial.printf("[MQTT] publish topic='%s' len=%u -> %s (state=%d)\n",TOPIC_TELEMETRY, data.length(), ok ? "OK" : "FALLO", mqttClient.state());
}

String content = "";


void OnMqttReceived(char* topic, byte* payload, unsigned int length) {
  // mostramos por la terminal que recibimos información
  Serial.print("Received on ");
  Serial.print(topic);
  Serial.print(": ");

  content = "";
  for (size_t i = 0; i < length; i++) {
    content.concat((char)payload[i]);
  }
  Serial.println(content);

  // Intentar parsear el JSON
  StaticJsonDocument<1024> doc;
  DeserializationError error = deserializeJson(doc, payload, length);

  if (error) {
    Serial.print("Error al parsear JSON: ");
    Serial.println(error.c_str());
    return;
  }

  // mostramos por pantalla que ha llegado un mensaje del servidor 
  mostrarMensaje("Ha llegado un mensaje");

  // Detectamos de donde viene el mensaje estación, semáforo, tráfico ...
  const char* type = doc["sensor_type"] | "";

  // CASO 1: Mensaje meteorológico ------------------------------------------------------------------------------------------------------------------------------------------------------------
  if (strcmp(type, "weather") == 0) {
    // almacenamos los datos en las variables creadas
    float temp = doc["data"]["temperature_celsius"];
    float aqi  = doc["data"]["air_quality_index"];
    float wind = doc["data"]["wind_speed_kmh"];
    float uv   = doc["data"]["uv_index"];
    float pres = doc["data"]["atmospheric_pressure_hpa"];

    Serial.printf("[WEATHER] T=%.1f°C | AQI=%.0f | Viento=%.1f km/h | UV=%.1f | Pres=%.1f hPa\n", temp, aqi, wind, uv, pres);

    // detectamos si hay condiciones peligrosas
    String alerta;
    if (temp > ALERT_TEMP_HOT_C)           alerta = "Calor extremo!";
    else if (temp < ALERT_TEMP_COLD_C)     alerta = "Frio extremo!";
    else if (aqi > ALERT_AQI)              alerta = "Aire contaminado!";
    else if (wind > ALERT_WIND_KMH)        alerta = "Viento peligroso!";
    else if (uv > ALERT_UV)                alerta = "UV muy alto!";
    else if (pres < ALERT_PRESSURE_LOW_HPA && wind > ALERT_WIND_KMH_WARN) alerta = "Posible tormenta!";

    // mostramos la alerta por la pantalla
    if (alerta.length() > 0) {
      Serial.printf("[ALERTA CLIMA] %s\n", alerta.c_str());
      mostrarAlertaMeteo(alerta);                                // funcion creada en el .ino
      return;                                                    // salimos porque ya hemos mostrado el mensaje 
    }

    // si no hay alerta grave, solo registrar en consola
    Serial.println("Condiciones normales.");
    // mostramos por pantalla
    mostrarMensaje("Condiciones normales");
    return;
  }

  // CASO 3: Mensaje contador de trafico ------------------------------------------------------------------------------------------------------------------------------------------------------------
  if (strcmp(type, "traffic_counter") == 0) {
    // solo cogemos los datos que nos interesan 
    int   veh   = doc["data"]["vehicle_count"]      ;
    int   ped   = doc["data"]["pedestrian_count"]   ;
    int   bike  = doc["data"]["bicycle_count"]      ;
    float avgSpd = doc["data"]["average_speed_kmh"] ;

    // imprimimos por pantalla
    Serial.printf("[TRAFFIC_COUNTER] Veh:%d Ped:%d Bike:%d VelMed:%.1f km/h\n", veh, ped, bike, avgSpd);
    // mostramos por pantalla
    mostrarContadorTrafico(veh, avgSpd, ped, bike);
    return;
  }

  // CASO 4: Mensaje de semáforos ------------------------------------------------------------------------------------------------------------------------------------------------------------
  if (strcmp(type, "traffic_light") == 0) {
    // igual que antes solo procesamos la información que nos interesa
    const char* state = doc["data"]["current_state"]   ;
    int remain = doc["data"]["time_remaining_seconds"] ;
    bool malf = doc["data"]["malfunction_detected"]    ;

    Serial.printf("[TRAFFIC_LIGHT] State=%s Rem=%ds Malf=%d\n",state, remain, malf);
    mostrarMensaje("Conexion Semáforos");
    return;
  }
  // CASO 5: Mensaje para el panel informativo ------------------------------------------------------------------------------------------------------------------------------------------------------------
  if (strcmp(type, "information_display") == 0) {
    const char* msg = doc["data"]["message"];
    mostrarMensaje(msg);
    return;
  }
}
