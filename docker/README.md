# Despliegue del sistema con Docker

Este directorio contiene toda la configuración necesaria para desplegar el sistema IoT completo mediante Docker y Docker Compose.

El uso de contenedores permite levantar de forma reproducible todos los servicios del proyecto, facilitando la portabilidad del sistema y asegurando una correcta integración entre sus distintos componentes.

---

## Servicios incluidos

El fichero `docker-compose.yml` define y orquesta los siguientes servicios:

### 🔹 Mosquitto (Broker MQTT)
- Gestiona la comunicación entre el dispositivo ESP32 y el backend.
- Permite la publicación y suscripción a los distintos tópicos MQTT.
- Se configura mediante el fichero `mosquitto.conf`.

### 🔹 PostgreSQL (Base de datos)
- Almacena la información relativa a calles, dispositivos y telemetrías.
- Se inicializa automáticamente mediante scripts SQL.
- La persistencia de datos se gestiona mediante volúmenes Docker.

### 🔹 Tomcat (Servidor del backend)
- Ejecuta la aplicación Java desarrollada en el módulo `backend`.
- La imagen se construye a partir de un `Dockerfile` específico.
- La aplicación se despliega dinámicamente, sin incluir artefactos compilados (WAR) en el repositorio.

---
## Puesta en marcha del sistema

Para desplegar el sistema completo es necesario tener previamente instalados:

- Docker
- Docker Compose

Una vez cumplidos estos requisitos, situarse en el directorio `docker/` y ejecutar el siguiente comando: docker-compose up -d

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
