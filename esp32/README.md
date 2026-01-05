# Dispositivo IoT – ESP32

Este directorio contiene el código y la configuración del dispositivo IoT basado en una placa ESP32, encargado de la captura de datos ambientales y de la comunicación con el sistema backend.

El dispositivo forma parte de la capa de percepción del sistema IoT y actúa como productor de datos, enviando información al broker MQTT para su posterior procesamiento.

---

## 🔌 Funcionalidad del dispositivo

El dispositivo ESP32 realiza las siguientes funciones:

- Lectura de sensores ambientales (luz y CO₂)
- Procesamiento básico de las medidas obtenidas
- Publicación periódica de datos en tópicos MQTT
- Recepción de mensajes desde el backend (si aplica)
- Control de elementos actuadores (pantalla LCD)

---

## 📡 Comunicación

La comunicación se realiza mediante el protocolo **MQTT**, utilizando un broker Mosquitto desplegado mediante Docker.

El dispositivo:
- Se conecta a una red WiFi configurada en el código
- Publica mensajes en formato JSON
- Utiliza tópicos MQTT definidos de forma coherente con el backend

---

## 📂 Estructura del directorio

```text
esp32/
├── src/
│   ├── main.ino              → Código principal del dispositivo
│   ├── config.h              → Parámetros de configuración (WiFi, MQTT, etc.)
│   ├── ESP32_Utils.hpp       → Funciones auxiliares
│   └── ESP32_Utils_MQTT.hpp  → Funciones relacionadas con MQTT
└── README.md
