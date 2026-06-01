# Sistema IoT de Monitorización Urbana con ESP32

Proyecto académico desarrollado para la asignatura de Computación Ubicua.
Implementa un sistema IoT completo para monitorizar y controlar un panel informativo urbano mediante ESP32, MQTT, backend Java, PostgreSQL, aplicación web y aplicación Android.

## Objetivo del proyecto

El sistema permite:
- Captar datos ambientales: luminosidad y CO₂.
- Enviar telemetría desde una ESP32 mediante MQTT.
- Almacenar los datos en PostgreSQL.
- Consultar el estado actual y el histórico desde una aplicación web y una app Android.
- Enviar mensajes al panel LCD desde la aplicación web.

## Arquitectura general

El proyecto se divide en cuatro capas:

1. Capa de percepción: ESP32, sensor de luz, sensor MQ-135 y pantalla LCD.
2. Capa de transporte: comunicación MQTT mediante Mosquitto.
3. Capa de procesado: backend Java desplegado en Tomcat y base de datos PostgreSQL.
4. Capa de aplicación: interfaz web y aplicación Android.

## Tecnologías utilizadas

- ESP32 / Arduino
- MQTT
- Mosquitto
- Java
- Apache Tomcat
- PostgreSQL
- Docker / Docker Compose
- Android Studio
- Retrofit
- Eclipse Paho MQTT
- HTML / CSS / JavaScript

## Estructura del repositorio

```text
Sistema-IoT-monitorizacion/
├── androidApp/      # Aplicación móvil Android
├── backend/         # Backend Java, servlets, MQTT y acceso a base de datos
├── docker/          # Infraestructura Docker: Tomcat, PostgreSQL y Mosquitto
├── docs/            # Memoria, diagramas y documentación
├── esp32/           # Código del dispositivo físico
└── README.md
