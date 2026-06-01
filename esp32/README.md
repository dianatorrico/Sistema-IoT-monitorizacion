# Dispositivo IoT – ESP32

Este directorio contiene el código y la configuración del dispositivo IoT basado en una placa **ESP32**.

El dispositivo forma parte de la **capa de percepción** del sistema IoT y se encarga de capturar datos ambientales, mostrarlos en una pantalla LCD y comunicarse con el backend mediante MQTT.

---

## Funcionalidad del dispositivo

El dispositivo ESP32 realiza las siguientes funciones:

- Lectura del sensor de luminosidad.
- Lectura del sensor MQ-135 para estimar el nivel de CO₂.
- Procesamiento básico de las medidas obtenidas.
- Publicación periódica de telemetría en formato JSON mediante MQTT.
- Recepción de mensajes enviados desde el backend.
- Actualización de la pantalla LCD con información ambiental o mensajes recibidos.
- Conexión WiFi con la red configurada en el código.

---

## Hardware utilizado

| Componente | Función |
|---|---|
| ESP32 | Microcontrolador principal del dispositivo |
| Sensor MQ-135 | Sensor utilizado para estimar la calidad del aire / CO₂ |
| Sensor digital de iluminación | Detección del nivel de luz ambiental |
| Pantalla LCD 20x4 con I2C | Actuador para mostrar datos y mensajes |
| Protoboard y cableado | Montaje del prototipo |

---

## Pines utilizados

| Componente | Pin ESP32 |
|---|---|
| LCD SDA | GPIO 21 |
| LCD SCL | GPIO 22 |
| Sensor de iluminación | GPIO 25 |
| Sensor MQ-135 | GPIO 34 |

---

## Comunicación MQTT

La comunicación se realiza mediante el protocolo **MQTT**, utilizando un broker Mosquitto desplegado mediante Docker.

El dispositivo:

- Se conecta a una red WiFi configurada en el código.
- Publica mensajes en formato JSON.
- Utiliza topics MQTT coherentes con el backend.
- Recibe mensajes de control para actualizar la pantalla LCD.

### Topic de telemetría

```text
madrid/sensors/ST_0947/telemetry
```

La ESP32 publica periódicamente en este topic los datos captados por los sensores.

### Topic de control

```text
madrid/sensors/ST_0947/information_display
```

La ESP32 se suscribe a este topic para recibir mensajes enviados desde el backend.

---

## Ejemplo de mensaje publicado

```json
{
  "sensor_id": "ST_0947",
  "street_id": "ST_0947",
  "timestamp": "2025-12-01T10:30:00",
  "brightness_level": "ALTA",
  "co2_ppm": 420,
  "air_quality_message": "Buena",
  "current_message": "Información urbana"
}
```

---

## Librerías necesarias

Para compilar y cargar el código en la ESP32 pueden ser necesarias las siguientes librerías:

- `WiFi`
- `PubSubClient`
- `ArduinoJson`
- `LiquidCrystal_I2C`

Estas librerías deben estar instaladas en el entorno de desarrollo utilizado, por ejemplo Arduino IDE o PlatformIO.

---

## Configuración

Antes de cargar el código en la ESP32, revisar los parámetros definidos en `config.h`:

```text
src/config.h
```

En este archivo se configuran valores como:

- Nombre de la red WiFi.
- Contraseña de la red WiFi.
- Dirección IP o nombre del broker MQTT.
- Puerto MQTT.
- Identificador del dispositivo.
- Topics MQTT utilizados.

> No se recomienda subir credenciales reales de WiFi a repositorios públicos.

---

## Estructura del directorio

```text
esp32/
├── src/
│   ├── main.ino              # Código principal del dispositivo
│   ├── config.h              # Parámetros de configuración WiFi y MQTT
│   ├── ESP32_Utils.hpp       # Funciones auxiliares de conexión y configuración
│   └── ESP32_Utils_MQTT.hpp  # Funciones relacionadas con MQTT
└── README.md
```

---

## Funcionamiento general

El funcionamiento del firmware sigue este flujo:

1. La ESP32 inicia la conexión WiFi.
2. Se establece conexión con el broker MQTT.
3. Se inicializan sensores y pantalla LCD.
4. El dispositivo lee periódicamente los sensores de luz y CO₂.
5. Se construye un mensaje JSON con la telemetría.
6. El mensaje se publica en el topic de telemetría.
7. La ESP32 permanece suscrita al topic de control.
8. Si recibe un mensaje desde el backend, actualiza la pantalla LCD.

---

## Relación con otros módulos

| Módulo | Relación |
|---|---|
| [`docker/`](../docker/) | Proporciona el broker MQTT Mosquitto al que se conecta la ESP32 |
| [`backend/`](../backend/) | Recibe la telemetría y envía mensajes de control al dispositivo |
| [`docs/`](../docs/) | Contiene diagramas, memoria y explicación del prototipo |
| [`androidApp/`](../androidApp/) | Visualiza los datos procesados por el backend |

---

## Notas

Este firmware forma parte de un prototipo académico.
