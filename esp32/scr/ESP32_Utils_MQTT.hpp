
void InitMqtt() 
{
	mqttClient.setServer(MQTT_BROKER_ADRESS, MQTT_PORT);
	//prueba para docker macOS
	//mqttClient.setServer("host.docker.internal", 1883);
	SuscribeMqtt();
	mqttClient.setCallback(OnMqttReceived);
}


void ConnectMqtt()
{
	while (!mqttClient.connected())
	{
		Serial.print("MQTT broker configurado: ");
    Serial.println(MQTT_BROKER_ADRESS);

		Serial.print("Starting MQTT connection...");
		if (mqttClient.connect(MQTT_CLIENT_NAME))
		{
			Serial.print("conectado");
			SuscribeMqtt();
		}
		else
		{
			Serial.print("Failed MQTT connection, rc=");
			Serial.print(mqttClient.state());
			Serial.println(" try again in 5 seconds");

			delay(5000);
		}
	}
}


void HandleMqtt()
{
	if (!mqttClient.connected())
	{
		ConnectMqtt();
	}
	mqttClient.loop();
}