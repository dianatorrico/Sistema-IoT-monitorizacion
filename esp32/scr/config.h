// IDENTIFICACIÓN ESTACIÓN (nuestro grupo) 
//prueba 2 
//#define SENSOR_ID        "LAB12JAV-G10_INF_01"
#define SENSOR_ID        "DISP_ST_0947_01"
#define STREET_ID        "ST_0947"
#define STREET_NAME      "Calle de Maria Odiaga"
#define DISTRICT_NAME    "Latina"
#define NEIGHBORHOOD     "Puerta del Ángel"
#define POSTAL_CODE      "28011"
#define ALTITUDE_METERS  605.6

// Coordenadas principales
#define LATITUDE         40.379452
#define LONGITUDE        -3.7398847

// TÓPICOS MQTT
#define TOPIC_DISPLAY    "madrid/sensors/ST_0947/information_display"
#define TOPIC_TELEMETRY  "madrid/sensors/ST_0947/telemetry"

// Conexion a la wifi de uni
//const char* ssid     = "cubicua";
//const char* password = "";
//const char* hostname = "ESP32L80000";

//conexion wifi casa
const char* ssid     = "TP_LINK_0957";
const char* password = "T0rr1_L0$";
const char* hostname = "ESP32L80000";


// configuracion del ESP32 192.168.1.20 casa 
//IPAddress ip(172, 29, 42, 250);
IPAddress ip(192, 168, 1, 60);
IPAddress gateway(192, 168, 1, 1);
IPAddress subnet(255, 255, 255, 0);

