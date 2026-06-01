# Aplicación Android – Sistema IoT de Monitorización Urbana

Esta carpeta contiene la aplicación móvil Android del **Sistema IoT de Monitorización Urbana**.

La aplicación actúa como cliente del backend Java y permite a los usuarios consultar la información recogida por los dispositivos IoT desplegados en el entorno urbano.

---

## Funcionalidad de la aplicación

La aplicación Android permite:

- Seleccionar calles monitorizadas.
- Consultar los dispositivos asociados a cada calle.
- Visualizar el estado actual de un dispositivo.
- Recibir telemetría en tiempo real mediante MQTT.
- Consultar históricos de telemetría por fecha.
- Consultar históricos de telemetría por fecha y rango horario.
- Mostrar información ambiental como CO₂, luminosidad y calidad del aire.
- Adaptar la interfaz visual en función del nivel de luminosidad recibido.

---

## Arquitectura

La aplicación sigue una arquitectura **cliente-servidor**, complementada con comunicación MQTT para la recepción de datos en tiempo real.

- La aplicación Android actúa como cliente.
- El backend Java, desplegado en Tomcat, expone una API REST.
- La app consume la API REST mediante Retrofit.
- La app utiliza MQTT mediante Eclipse Paho para recibir telemetría en tiempo real.
- Los datos históricos provienen de la base de datos PostgreSQL.
- Los datos actuales pueden consultarse mediante REST o recibirse mediante MQTT.

---

## Tecnologías utilizadas

- Java
- Android Studio
- Retrofit
- Eclipse Paho MQTT
- Gradle
- XML Layouts
- API REST
- MQTT

---

## Estructura principal

```text
androidApp/
├── app/
│   └── src/
│       └── main/
│           ├── java/                # Lógica de la aplicación
│           ├── res/                 # Recursos gráficos y layouts XML
│           └── AndroidManifest.xml
├── gradle/                          # Configuración Gradle
├── build.gradle                     # Configuración global del proyecto
├── settings.gradle
├── gradle.properties
├── gradlew
├── gradlew.bat
└── README.md
```

---

## Pantallas principales

La aplicación se organiza en varias pantallas:

| Pantalla | Función |
|---|---|
| Pantalla de bienvenida | Inicio de la aplicación antes de acceder a la selección |
| Selección de calle | Permite elegir una calle monitorizada |
| Selección de dispositivo | Muestra los dispositivos disponibles para la calle seleccionada |
| Monitorización | Muestra los datos actuales del dispositivo |
| Histórico | Permite consultar registros de telemetría almacenados |

---

## Comunicación con el backend

La aplicación se comunica con el backend mediante dos mecanismos:

### API REST

Se utiliza para:

- Obtener la lista de dispositivos.
- Consultar el estado actual.
- Consultar históricos por fecha.
- Consultar históricos por fecha y rango horario.

### MQTT

Se utiliza para:

- Recibir actualizaciones en tiempo real del dispositivo seleccionado.
- Actualizar la interfaz automáticamente cuando llega nueva telemetría.

Topic principal utilizado:

```text
madrid/sensors/ST_0947/telemetry
```

---

## Datos mostrados

La aplicación muestra información como:

- Identificador del dispositivo.
- Calle asociada.
- Mensaje actual del panel.
- Nivel de CO₂.
- Calidad del aire.
- Nivel de luminosidad.
- Hora de la última actualización.
- Histórico de registros almacenados.

---

## Configuración

Antes de ejecutar la aplicación, revisar la configuración de conexión con el backend:

- URL base de la API REST.
- Dirección IP del servidor Tomcat.
- Dirección IP o nombre del broker MQTT.
- Topic MQTT del dispositivo.

En un entorno local con Docker, la IP puede variar según el equipo o la red utilizada.

---

## Ejecución

Para ejecutar la aplicación:

1. Abrir el proyecto `androidApp/` con Android Studio.
2. Esperar a que Gradle sincronice las dependencias.
3. Revisar la configuración de conexión con el backend.
4. Ejecutar la aplicación en un emulador o dispositivo físico Android.
5. Asegurarse de que el backend, PostgreSQL y Mosquitto están levantados desde el módulo `docker/`.

---

## Relación con otros módulos

| Módulo | Relación |
|---|---|
| [`backend/`](../backend/) | Expone la API REST consumida por la app |
| [`docker/`](../docker/) | Levanta Tomcat, PostgreSQL y Mosquitto |
| [`esp32/`](../esp32/) | Publica la telemetría que puede visualizarse en la app |
| [`docs/`](../docs/) | Contiene diagramas y documentación de la aplicación |

---

## Notas

Esta aplicación forma parte de un prototipo académico.

Para un entorno de producción sería necesario añadir mejoras como:

- Autenticación de usuarios.
- Gestión avanzada de errores de red.
- Configuración externa de IPs y endpoints.
- Cifrado de comunicaciones.
- Manejo avanzado de estados sin conexión.
- Mejoras de accesibilidad y diseño responsive.
