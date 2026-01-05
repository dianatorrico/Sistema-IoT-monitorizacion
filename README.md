# Sistema IoT de Monitorización Urbana con ESP32

## Descripción del proyecto

Este proyecto implementa un sistema completo de monitorización y actuación IoT aplicado a un panel informativo urbano situado en una calle del barrio de Puerta del Ángel (Madrid).

El sistema recoge datos ambientales mediante sensores, los transmite mediante MQTT y los procesa en un backend Java, almacenándolos en una base de datos PostgreSQL y mostrándolos a través de aplicaciones web y móvil.

---

## Arquitectura del sistema

El sistema sigue una arquitectura IoT clásica dividida en capas:

- **Capa de percepción**  
  Dispositivo ESP32 con:
  - Sensor digital de luz
  - Sensor de CO₂
  - Pantalla LCD como actuador

- **Capa de transporte**  
  Comunicación mediante protocolo MQTT usando el broker Mosquitto.

- **Capa de servicio / backend**  
  Servidor Java desplegado en Apache Tomcat que:
  - Se suscribe a los tópicos MQTT
  - Procesa la telemetría
  - Expone APIs REST

- **Capa de datos**  
  Base de datos PostgreSQL para persistencia de:
  - Dispositivos
  - Calles
  - Telemetrías

- **Capa de aplicación**  
  Aplicación web y aplicación móvil para visualización y control.

---

## Estructura del repositorio

```text
esp32/       → Código del dispositivo IoT  
backend/     → Servidor Java y lógica de negocio
appAndroid/  → Desarrollo aplicación Android
docker/      → Despliegue completo con Docker Compose  
docs/        → Memoria, diagramas y manuales de usuario
