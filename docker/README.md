# Despliegue del sistema con Docker

Este directorio contiene la configuración necesaria para desplegar la infraestructura del **Sistema IoT de Monitorización Urbana** mediante Docker y Docker Compose.

El uso de contenedores permite levantar de forma reproducible los servicios principales del proyecto, facilitando la portabilidad del sistema y asegurando la integración entre sus distintos componentes.

---

## Servicios incluidos

El fichero `docker-compose.yml` define y orquesta los siguientes servicios:

| Servicio | Función | Puerto habitual |
|---|---|---|
| Mosquitto | Broker MQTT para la comunicación entre la ESP32 y el backend | `1883` |
| PostgreSQL | Base de datos relacional para calles, dispositivos y telemetrías | `5432` |
| Tomcat | Servidor de aplicaciones donde se despliega el backend Java y la aplicación web | `8080` |

---

## Descripción de los servicios

### Mosquitto

Mosquitto actúa como broker MQTT del sistema.

Sus funciones principales son:

- Gestionar la comunicación entre el dispositivo ESP32 y el backend.
- Permitir la publicación y suscripción a los topics MQTT.
- Recibir telemetría desde la ESP32.
- Distribuir mensajes de control hacia el dispositivo.

Su configuración se encuentra en:

```text
mqtt/config/mosquitto.conf
```

### PostgreSQL

PostgreSQL se utiliza como base de datos principal del sistema.

Almacena información sobre:

- Calles.
- Dispositivos.
- Registros de telemetría.
- Estado e histórico del panel informativo.

La base de datos se inicializa automáticamente mediante scripts SQL ubicados en:

```text
postgres/initdb/
```

### Tomcat

Tomcat ejecuta la aplicación Java desarrollada en el módulo `backend`.

Sus funciones principales son:

- Desplegar el backend Java.
- Recibir y procesar mensajes MQTT.
- Exponer la API REST utilizada por la aplicación web y la aplicación Android.
- Publicar mensajes de control hacia la ESP32.

La imagen se construye a partir del `Dockerfile` ubicado en:

```text
tomcat/Dockerfile
```

---

## Requisitos previos

Para desplegar el sistema es necesario tener instalado:

- Docker
- Docker Compose

---

## Puesta en marcha del sistema

Desde la raíz del repositorio, acceder al directorio `docker/`:

```bash
cd docker
```

Levantar todos los servicios:

```bash
docker compose up -d
```

También puede utilizarse, según la versión instalada:

```bash
docker-compose up -d
```

---

## Comandos útiles

Ver los contenedores en ejecución:

```bash
docker compose ps
```

Ver los logs de todos los servicios:

```bash
docker compose logs -f
```

Ver los logs de un servicio concreto:

```bash
docker compose logs -f mosquitto
docker compose logs -f postgres
docker compose logs -f tomcat
```

Detener los servicios:

```bash
docker compose down
```

Reconstruir las imágenes y levantar de nuevo el sistema:

```bash
docker compose up --build -d
```

Detener los servicios eliminando también los volúmenes:

```bash
docker compose down -v
```

> Usar `docker compose down -v` elimina los datos persistidos en la base de datos.

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
```

---

## Relación con el resto del proyecto

Este módulo conecta los principales componentes del sistema:

- `esp32/`: publica telemetría mediante MQTT hacia Mosquitto.
- `backend/`: se despliega sobre Tomcat y se comunica con Mosquitto y PostgreSQL.
- `androidApp/`: consume la API REST expuesta por el backend.
- `docs/`: contiene la memoria y diagramas explicativos del despliegue.

---

## Notas

Este despliegue está pensado para un entorno académico y de pruebas.

Para un entorno de producción sería necesario añadir medidas adicionales como:

- Gestión segura de credenciales.
- Cifrado TLS para MQTT.
- Autenticación en la API.
- Configuración avanzada de red.
- Copias de seguridad de la base de datos.
