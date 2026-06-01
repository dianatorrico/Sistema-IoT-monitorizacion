# Backend – Servidor Java

Este módulo implementa el backend del **Sistema IoT de Monitorización Urbana**.

Está desarrollado como una aplicación Java basada en **Maven**, desplegable en **Apache Tomcat** y conectada con **PostgreSQL** y **Mosquitto MQTT**.

---

## Funcionalidad principal

El backend actúa como capa de procesado del sistema. Sus funciones principales son:

- Suscribirse a topics MQTT para recibir telemetría enviada por dispositivos ESP32.
- Procesar y validar mensajes JSON procedentes del dispositivo físico.
- Almacenar la información recibida en PostgreSQL.
- Exponer servicios REST mediante servlets.
- Proporcionar una interfaz web básica para la consulta y control del sistema.
- Publicar mensajes MQTT de control hacia la ESP32 cuando el usuario envía un mensaje al panel.

---

## Estructura del backend

```text
backend/
├── src/
│   ├── Database/     # Conexión y acceso a base de datos
│   ├── Logic/        # Lógica de negocio
│   ├── Mqtt/         # Comunicación MQTT
│   └── Servlets/     # APIs REST mediante servlets
├── resources/
│   ├── log4j2.xml
│   └── META-INF/
│       └── persistence.xml
├── webapp/
│   ├── index.html
│   └── WEB-INF/
│       ├── web.xml
│       └── beans.xml
├── pom.xml
└── README.md
```

---

## Paquetes principales

### `Database/`

Contiene las clases encargadas de la conexión con PostgreSQL y del acceso a los datos del sistema.

Se encarga de operaciones como:

- Consultar dispositivos registrados.
- Obtener el estado actual de un dispositivo.
- Consultar históricos de telemetría.
- Insertar nuevas lecturas recibidas mediante MQTT.

### `Logic/`

Contiene la lógica de negocio del backend.

En esta capa se procesan los datos recibidos, se aplican validaciones y se coordinan las operaciones entre la base de datos, MQTT y los servlets.

### `Mqtt/`

Contiene las clases relacionadas con la comunicación MQTT.

Sus responsabilidades principales son:

- Suscribirse al topic de telemetría.
- Recibir mensajes enviados por la ESP32.
- Parsear mensajes JSON.
- Publicar mensajes de control hacia el dispositivo.

### `Servlets/`

Contiene los servlets que exponen la API REST utilizada por la aplicación web y la aplicación Android.

---

## Comunicación MQTT

El backend se comunica con el dispositivo físico mediante MQTT.

### Topic de telemetría

```text
madrid/sensors/ST_0947/telemetry
```

El backend se suscribe a este topic para recibir los datos enviados por la ESP32.

### Topic de control

```text
madrid/sensors/ST_0947/information_display
```

El backend publica en este topic los mensajes que deben mostrarse en la pantalla LCD del dispositivo.

---

## Ejemplo de telemetría recibida

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

## API REST

El backend expone una API REST consumida por la aplicación web y la aplicación Android.

| Funcionalidad | Descripción |
|---|---|
| Obtener dispositivos | Devuelve la lista de dispositivos registrados en el sistema |
| Obtener estado actual | Devuelve la última telemetría registrada de un dispositivo |
| Consultar histórico por fecha | Devuelve las lecturas almacenadas para una fecha concreta |
| Consultar histórico por rango horario | Devuelve las lecturas filtradas por fecha y franja horaria |
| Enviar mensaje al panel | Publica un mensaje MQTT para actualizar la pantalla LCD |

> Los endpoints concretos están implementados mediante servlets en el paquete `Servlets/`.

---

## Compilación del backend

Para compilar el proyecto es necesario tener instalado:

- Java
- Maven

Desde el directorio `backend/`, ejecutar:

```bash
mvn clean package
```

El proceso genera un artefacto `.war` desplegable en Apache Tomcat.

---

## Despliegue

El despliegue del backend se realiza mediante Docker desde el módulo [`docker/`](../docker/).

El contenedor de Tomcat se encarga de desplegar la aplicación Java y conectarla con los servicios definidos en `docker-compose.yml`:

- Mosquitto
- PostgreSQL
- Tomcat

Para levantar todo el sistema:

```bash
cd ../docker
docker compose up --build -d
```

---

## Relación con otros módulos

| Módulo | Relación |
|---|---|
| [`esp32/`](../esp32/) | Envía telemetría al backend mediante MQTT y recibe mensajes de control |
| [`docker/`](../docker/) | Proporciona Tomcat, PostgreSQL y Mosquitto para ejecutar el backend |
| [`androidApp/`](../androidApp/) | Consume la API REST expuesta por el backend |
| [`docs/`](../docs/) | Contiene la memoria y diagramas explicativos del sistema |

---

## Notas

Este backend forma parte de un prototipo académico funcional.
