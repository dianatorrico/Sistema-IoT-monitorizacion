# Documentación del proyecto

Este directorio contiene la documentación asociada al proyecto **Sistema IoT de Monitorización Urbana**.

La documentación complementa el código fuente y permite comprender el diseño, la arquitectura, el funcionamiento y el uso del sistema sin necesidad de analizar directamente la implementación.

---

## Contenido

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

Documentación orientada al uso del sistema:

- Manual de usuario de la aplicación web.
- Manual de usuario de la aplicación móvil Android.
- Instrucciones básicas de navegación y consulta de datos.

### Manual de instalación

Guía para poner en marcha el sistema en un entorno local:

- Requisitos previos.
- Despliegue con Docker Compose.
- Comprobación de servicios.
- Verificación de la base de datos.
- Pruebas básicas de comunicación MQTT.

### Diagramas

Representaciones visuales del sistema:

- Diagrama general de arquitectura por capas.
- Diagrama de despliegue de componentes.
- Diagrama de clases de la aplicación Android.
- Diagrama de flujo de navegación.
- Comunicación entre ESP32, MQTT, backend, base de datos y aplicaciones.

---

## Objetivo de esta documentación

El objetivo de este directorio es:

- Facilitar la comprensión funcional y técnica del proyecto.
- Servir de apoyo a la evaluación académica del trabajo.
- Documentar las decisiones de diseño tomadas durante el desarrollo.
- Permitir que cualquier lector entienda el sistema sin necesidad de ejecutar el código.

---

## Relación con el código fuente

Esta documentación está vinculada con las principales partes del repositorio:

- `esp32/`: código del dispositivo físico basado en ESP32.
- `backend/`: servidor Java, comunicación MQTT, API REST y acceso a PostgreSQL.
- `docker/`: infraestructura de despliegue con Mosquitto, Tomcat y PostgreSQL.
- `androidApp/`: aplicación móvil Android.
- `docs/`: memoria, manuales y diagramas del proyecto.
