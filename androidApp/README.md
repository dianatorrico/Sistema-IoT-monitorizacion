# Aplicación Android – Sistema IoT de Monitorización Urbana

Esta carpeta contiene la aplicación móvil Android del sistema IoT desarrollado en el proyecto.

La aplicación actúa como cliente del backend Java y permite a los usuarios consultar la información recogida por los dispositivos IoT desplegados en entorno urbano.

---

## 📱 Funcionalidad de la aplicación

La aplicación Android permite:

- Seleccionar calles monitorizadas
- Consultar los dispositivos asociados a cada calle
- Visualizar el estado actual de los dispositivos
- Consultar las telemetrías enviadas por los sensores
- Obtener la información mediante comunicación REST con el backend

---

## 🏗️ Arquitectura

La aplicación sigue una arquitectura **cliente-servidor**:

- La aplicación Android actúa como cliente
- El backend Java (Tomcat) expone una API REST
- La app consume dicha API mediante Retrofit
- Los datos mostrados provienen de la base de datos PostgreSQL

---

## 📂 Estructura principal

```text
androidApp/
├── app/
│   └── src/
│       └── main/
│           ├── java/                → Lógica de la aplicación
│           ├── res/                 → Recursos gráficos y layouts
│           └── AndroidManifest.xml
├── gradle/                          → Configuración Gradle
├── build.gradle                     → Configuración global
├── settings.gradle
├── gradle.properties
├── gradlew
└── gradlew.bat
