# Backend – Servidor Java

Servidor Java desplegado en Apache Tomcat encargado de:

- Suscribirse a tópicos MQTT
- Procesar telemetría de dispositivos IoT
- Persistir datos en PostgreSQL
- Exponer servicios REST

## Estructura de paquetes

- Database: conexión y acceso a base de datos
- Logic: lógica de negocio
- Mqtt: comunicación MQTT
- Servlets: APIs REST
