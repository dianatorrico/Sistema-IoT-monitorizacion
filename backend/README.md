# Backend – Servidor Java

Este módulo implementa el backend del sistema IoT y está desarrollado como una aplicación Java basada en Maven y desplegable en Apache Tomcat.

## Funcionalidad principal

- Suscripción a tópicos MQTT
- Procesamiento de mensajes enviados por dispositivos ESP32
- Persistencia de datos en PostgreSQL mediante JPA
- Exposición de servicios REST mediante Servlets
- Provisión de una interfaz web básica

## Estructura del backend

```text
backend/
├── src/
│   ├── Database/     → Conexión y acceso a base de datos
│   ├── Logic/        → Lógica de negocio
│   ├── Mqtt/         → Comunicación MQTT
│   └── Servlets/     → APIs REST
├── resources/
│   ├── log4j2.xml
│   └── META-INF/persistence.xml
├── webapp/
│   ├── index.html
│   └── WEB-INF/
│       ├── web.xml
│       └── beans.xml
└── pom.xml
