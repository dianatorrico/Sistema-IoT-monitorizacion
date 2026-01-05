# Despliegue del sistema con Docker

Este directorio contiene toda la configuración necesaria para desplegar el sistema IoT completo mediante Docker y Docker Compose, permitiendo levantar de forma reproducible todos los servicios del proyecto.

El uso de Docker facilita la portabilidad, el despliegue y la correcta integración entre los distintos componentes del sistema.

---

## Servicios incluidos

El fichero `docker-compose.yml` define los siguientes servicios:

### 🔹 Mosquitto (Broker MQTT)
- Gestiona la comunicación entre el dispositivo ESP32 y el backend.
- Permite la publicación y suscripción a tópicos MQTT.
- Configurado mediante un fichero `mosquitto.conf`.

### 🔹 PostgreSQL (Base de datos)
- Almacena la información de dispositivos, calles y telemetrías.
- Se inicializa automáticamente mediante scripts SQL.
- Los datos persistentes se gestionan mediante volúmenes Docker.

### 🔹 Tomcat (Servidor del backend)
- Ejecuta la aplicación Java desarrollada en el módulo `backend`.
- La imagen se construye mediante un `Dockerfile` específico.
- La aplicación se despliega dinámicamente, sin incluir artefactos compilados en el repositorio.

---

## Estructura del directorio

```text
docker/
├── docker-compose.yml
├── mqtt/
│   └── config/
│       └── mosquitto.conf
├── postgres/
│   └── initdb/
│       ├── 1.schema.sql
│       └── datos.sql
└── tomcat/
    ├── Dockerfile
    └── conf/
        └── context.xml
