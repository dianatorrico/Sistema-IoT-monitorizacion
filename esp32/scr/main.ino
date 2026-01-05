#include <WiFi.h>
#include <SPIFFS.h>
#include <PubSubClient.h>
#include <time.h> 
#include "config.h" 
#include "MQTT.hpp"
#include "ESP32_Utils.hpp"
#include "ESP32_Utils_MQTT.hpp"
#include <ArduinoJson.h>

//sensores
#include <Wire.h>
#include <LiquidCrystal_I2C.h>
LiquidCrystal_I2C lcd(0x27, 20, 4);

// Pines de sensores
const int SENSOR_DO = 25;      // pin digital sensor de luz 
const int SENSOR_CO2 = 34;     // pin analógico sensor de CO2

// mensaje de alerta 
volatile bool msgActivo = false;
unsigned long msgHasta = 0;
String msgTexto;

// tiempo envio 
constexpr unsigned long PUBLISH_INTERVAL_MS = 10UL * 1000UL; // 10 seg
unsigned long lastPublish = 0;

//UMBRALES ALERTA
const float CO2_ALERT_PPM = 2000;
const float ALERT_WIND_KMH        = 70.0;
const float ALERT_WIND_KMH_WARN   = 40.0;  
const int   ALERT_AQI             = 150;
const int   ALERT_UV              = 8;
const float ALERT_TEMP_HOT_C      = 40.0;
const float ALERT_TEMP_COLD_C     = 0.0;
const float ALERT_PRESSURE_LOW_HPA= 980.0;

//parpadeo para alertas -------------------------------------------------------------------------------------------------------------------------------------------------------------------
bool blinkActivo = false;
bool blinkOn = true;
unsigned long blinkHasta = 0;
unsigned long blinkCada = 300;
unsigned long blinkSiguiente = 0;

void iniciarParpadeoLCD(unsigned long duracionMs, unsigned long intervaloMs = 300) {
  blinkActivo = true;
  blinkOn = true;
  blinkHasta = millis() + duracionMs;
  blinkCada = intervaloMs;
  blinkSiguiente = 0;
  lcd.backlight();
}

void tickParpadeoLCD() {
  if (!blinkActivo) return;
  unsigned long now = millis();
  if (now >= blinkHasta) {
    blinkActivo = false;
    lcd.backlight();   // deja encendida al terminar
    return;
  }
  if (now >= blinkSiguiente) {
    blinkSiguiente = now + blinkCada;
    blinkOn = !blinkOn;
    if (blinkOn) lcd.backlight();
    else         lcd.noBacklight();
  }
}

//funciones de mensajes --------------------------------------------------------------------------------------------------------------------------------------------------------------------------
void mostrarAlertaMeteo(const String& detalle) {
  msgTexto = detalle;
  msgActivo = true;
  msgHasta = millis() + 6000; 

  lcd.clear();
  lcd.setCursor(0,0); 
  lcd.print("¡ ALERTA !");
  lcd.setCursor(0,1); 
  lcd.print(detalle.substring(0,20));

  if (detalle.length() > 20) {
    lcd.setCursor(0,2); lcd.print(detalle.substring(20, min(40,(int)detalle.length())));
  }
  iniciarParpadeoLCD(6000, 250); 
}

void mostrarContadorTrafico(int vehiculos, float velocidadMedia, int peatones, int bicicletas) {
  msgActivo = true;
  msgHasta = millis() + 6000; // visible 6 segundos
  msgTexto = "Contador trafico";

  lcd.clear();
  lcd.backlight();
  lcd.setCursor(0, 0);
  lcd.print("Contador Trafico");

  // vehículos + velocidad media
  lcd.setCursor(0, 1);
  lcd.print("Veh: ");
  lcd.print(vehiculos);
  lcd.print(" (");
  lcd.print(velocidadMedia, 1);
  lcd.print("km/h)");

  // peatones
  lcd.setCursor(0, 2);
  lcd.print("Ped: ");
  lcd.print(peatones);

  // bicicletas
  lcd.setCursor(0, 3);
  lcd.print("Bicis: ");
  lcd.print(bicicletas);
}


void mostrarMensaje(const String& text) {
  msgTexto = text;
  msgActivo = true;
  msgHasta = millis() + 5000;

  lcd.clear();
  lcd.setCursor(0, 0);
  lcd.print("Mensaje recibido:");

  if (text.length() > 0)  { lcd.setCursor(0, 1); lcd.print(text.substring(0, 20)); }
  if (text.length() > 20) { lcd.setCursor(0, 2); lcd.print(text.substring(20, min(40, (int)text.length()))); }
  if (text.length() > 40) { lcd.setCursor(0, 3); lcd.print(text.substring(40, min(60, (int)text.length()))); }
}

// Alerta C02 ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
void mostrarAlertaCalidadAire(float ppm) {
  msgTexto = "ALERTA CO2";
  msgActivo = true;
  msgHasta = millis() + 5000;

  lcd.clear();
  lcd.backlight();              
  lcd.setCursor(0, 0); 
  lcd.print("AIRE MUY MALO!");
  lcd.setCursor(0, 1); 
  lcd.print("CO2: "); 
  lcd.print((int)ppm); 
  lcd.print(" ppm");
  lcd.setCursor(0, 2); 
  lcd.print("Cuidado");
  iniciarParpadeoLCD(5000, 250);
}

//funciones auxiliares enviamos la calidad del aire 
static const char* airQualityMsg(float ppm) {
  if (ppm < 800.0f)   return "Bueno";
  if (ppm < 1200.0f)  return  "Moderado";
  if (ppm < 2000.0f)  return "Cargado";
  return "Malo";
}

String getTimestamp() {
  struct tm timeinfo;
  if (!getLocalTime(&timeinfo)) {
    return "1970-01-01T00:00:00";
  }
  char buffer[25];
  strftime(buffer, sizeof(buffer), "%Y-%m-%dT%H:%M:%S", &timeinfo);
  return String(buffer);
}

static String publishInformationDisplay(const String& message, String& brightness_level, float co2lvl) {
  String timestamp = getTimestamp();
  String p; p.reserve(700);

  p  = "{";
  p += "\"sensor_id\":\"" SENSOR_ID "\",";
  p += "\"sensor_type\":\"information_display\",";
  p += "\"street_id\":\"" STREET_ID "\",";
  p += "\"timestamp\":\"" + timestamp + "\",";
  p += "\"location\":{"
         "\"latitude\":"  + String(LATITUDE, 6) + ","
         "\"longitude\":" + String(LONGITUDE, 7) + ","
         "\"district\":\"" DISTRICT_NAME "\","
         "\"neighborhood\":\"" NEIGHBORHOOD "\""
       "},";

  //Mensaje de la iluminacion 
  p += "\"data\":{";
  p +=   "\"display_status\":\"active\",";
  p +=   "\"current_message\":\"" + message + "\",";
  p +=   "\"content_type\":\"traffic\",";
  p +=   "\"brightness_level\":" + brightness_level + ",";
  p +=   "\"display_type\":\"lcd_panel\",";
  p +=   "\"display_size_inches\":20.0,";
  p +=   "\"supports_color\":true,";
  //Mensaje del CO2
  p += "\"co2_ppm\":" + String(co2lvl, 1) + ",";  
  p += "\"air_quality_message\":\"" + String(airQualityMsg(co2lvl)) + "\"";
  p += "\"last_content_update\":\"" + timestamp + "\""; 
  p += "}}";
	return p;

}

void setup(void){

	Serial.begin(115200);
	SPIFFS.begin();

  #if defined(LiquidCrystal_I2C_h)
  lcd.init();
  #endif

  pinMode(SENSOR_DO, INPUT_PULLUP);
  Wire.begin(21, 22);
  
  lcd.begin(20, 4);
  lcd.backlight();
  lcd.clear();
  lcd.setCursor(0, 0);
  lcd.print("Inicializando...");
  delay(1500);
  lcd.clear();

  configTzTime("CET-1CEST,M3.5.0/2,M10.5.0/3", "es.pool.ntp.org", "pool.ntp.org");

	ConnectWiFi_STA(true);
	InitMqtt();

  //tiempo de envio
  lastPublish = millis();

  lcd.print("Sensores listos!");
  delay(1000);
  lcd.clear();
}

void loop()
{
	HandleMqtt();	
  tickParpadeoLCD();

  // mostramos mensajes ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  if (msgActivo){
    if (millis()<msgHasta){
      delay(50);
      return;
    } else {
      msgActivo = false;
      lcd.clear();
    }
  }

	// sensor de luz ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  int valorLuz = digitalRead(SENSOR_DO); // HIGH = sin luz
	String situacionLuz = "";
  lcd.setCursor(0, 0);
  lcd.print("Luz: ");
  if (valorLuz == HIGH) {           // oscuridad
		situacionLuz="Oscuro";
    lcd.backlight();
    lcd.print("Oscuridad  ");
    Serial.println("Oscuridad detectada");
  } else {         
    lcd.noBacklight();                 // hay luz
		situacionLuz="Iluminado";
    lcd.print("Detectada  ");
    Serial.println("Luz detectada");
  }

  // sensor de CO2 -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  int valorCO2 = analogRead(SENSOR_CO2);                // lectura analógica (0–4095)
  float ppm = map(valorCO2, 0, 4095, 400, 5000);        // conversión simple a ppm 
  
  // 
  if (!msgActivo && ppm >= CO2_ALERT_PPM) {
    mostrarAlertaCalidadAire(ppm);
  }

  // imprimimos por pantalla ------------------------------------------------------------------------------------------------------------------------------------------------------------------
  lcd.setCursor(0, 1);
  lcd.print("CO2: ");
  lcd.print(ppm);
  lcd.print(" ppm   ");

  // imprimirmos por la terminal ------------------------------------------------------------------------------------------------------------------------------------------------------------------
  Serial.print("CO2: ");
  Serial.print(ppm);
  Serial.println(" ppm");

	// para publicar cada 10 segundos
  if (millis() - lastPublish >= PUBLISH_INTERVAL_MS) {
    lastPublish += PUBLISH_INTERVAL_MS;
    PublisMqtt(publishInformationDisplay("Enviando", situacionLuz ,ppm));
  }
  
	delay(1000);
}
