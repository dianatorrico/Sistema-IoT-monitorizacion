# Documentación del proyecto

Este directorio contiene la documentación asociada al proyecto **Sistema IoT de Monitorización Urbana**.

La documentación complementa el código fuente y permite comprender el diseño, la arquitectura, el funcionamiento y el uso del sistema sin necesidad de analizar directamente la implementación.

---

## Archivos incluidos

| Archivo | Descripción |
|---|---|
| `memoria.pdf` | Memoria justificativa completa del proyecto |
| `diagrama-arquitectura.png` | Diagrama general de arquitectura del sistema IoT |
| `diagrama-clases-appAndroid.png` | Diagrama de clases de la aplicación móvil Android |
| `Street Device Monitoring-2025-12-20-130313.pdf` | Documento adicional/exportación relacionada con el sistema de monitorización |

> Si alguno de estos archivos se renombra, se recomienda actualizar esta tabla para mantener la documentación sincronizada con el repositorio.

---

## Contenido de la documentación

### Memoria del proyecto

Documento principal del proyecto, donde se describe:

- Contexto y motivación del sistema.
- Objetivos y alcance.
- Arquitectura IoT por capas.
- Tecnologías utilizadas.
- Desarrollo del prototipo físico.
- Comunicación MQTT y API REST.
- Backend, base de datos y aplicaciones cliente.
- Pruebas realizadas.
- Conclusiones del trabajo.

### Manuales de usuario

La memoria incluye documentación orientada al uso del sistema:

- Manual de usuario de la aplicación web.
- Manual de usuario de la aplicación móvil Android.
- Instrucciones básicas de uso, navegación y consulta de datos.

### Manual de instalación

La documentación también incluye una guía para poner en marcha el sistema en un entorno local:

- Requisitos previos.
- Despliegue con Docker Compose.
- Comprobación de servicios.
- Verificación de la base de datos.
- Pruebas básicas de comunicación MQTT.

### Diagramas

Representaciones visuales del sistema:

- Diagrama general de arquitectura por capas.
- Diagrama de clases de la aplicación Android.
- Representación de la comunicación entre ESP32, MQTT, backend, base de datos y aplicaciones.

---

## Objetivo de esta documentación

El objetivo de este directorio es:

- Facilitar la comprensión funcional y técnica del proyecto.
- Servir de apoyo a la evaluación académica del trabajo.
- Documentar las decisiones de diseño tomadas durante el desarrollo.
- Permitir que cualquier lector entienda el sistema sin necesidad de ejecutar el código.
- Centralizar la memoria, diagramas y materiales explicativos del sistema.

---

## Relación con el código fuente

Esta documentación está vinculada con las principales partes del repositorio:

| Carpeta | Descripción |
|---|---|
| [`esp32/`](../esp32/) | Código del dispositivo físico basado en ESP32 |
| [`backend/`](../backend/) | Servidor Java, comunicación MQTT, API REST y acceso a PostgreSQL |
| [`docker/`](../docker/) | Infraestructura de despliegue con Mosquitto, Tomcat y PostgreSQL |
| [`androidApp/`](../androidApp/) | Aplicación móvil Android |
| [`docs/`](./) | Memoria, manuales y diagramas del proyecto |

---

## Recomendaciones de lectura

Para entender el proyecto de forma progresiva, se recomienda revisar la documentación en este orden:

1. `memoria.pdf`
2. `diagrama-arquitectura.png`
3. `diagrama-clases-appAndroid.png`
4. README principal del repositorio
5. README específicos de cada módulo: `docker/`, `backend/`, `esp32/` y `androidApp/`

---

## Notas

Este directorio forma parte de un proyecto académico.  
Los documentos incluidos sirven como apoyo para comprender el diseño, implementación y validación del prototipo.
