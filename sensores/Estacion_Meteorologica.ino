#include <Wire.h>
#include <WiFi.h>
#include <HTTPClient.h>
#include <SparkFun_SHTC3.h>
#include <Adafruit_LPS2X.h>
#include <ArduinoJson.h>

// Configuración WiFi
const char* ssid = "Eliminado ahora que se sube el codigo a GitHub";
const char* password = "Eliminado ahora que se sube el codigo a GitHub";
const char* serverName = "Eliminado ahora que se sube el codigo a GitHub";  // IP de AWS

// Sensores
SHTC3 shtc3;
Adafruit_LPS22 lps22;
#define LPS22_ADDRESS 0x5C

void setup() {
    Serial.begin(115200);
    Wire.begin();
    delay(100);  // Estabilización

    // Inicializar SHTC3
    if (shtc3.begin() != SHTC3_Status_Nominal) {
        Serial.println("Error: SHTC3 no inicializado");
        while (1) delay(100);
    }
    Serial.println("SHTC3 OK");

    // Inicializar LPS22
    if (!lps22.begin_I2C(LPS22_ADDRESS)) {
        Serial.println("Error: LPS22 no inicializado");
        while (1) delay(100);
    }
    Serial.println("LPS22 OK");

    // Conectar WiFi
    WiFi.mode(WIFI_STA);
    WiFi.begin(ssid, password);
    Serial.print("Conectando WiFi");
    
    int attempts = 0;
    while (WiFi.status() != WL_CONNECTED && attempts < 30) {
        delay(500);
        Serial.print(".");
        attempts++;
    }

    if (WiFi.status() == WL_CONNECTED) {
        Serial.println("\nWiFi conectado");
        Serial.println("IP del ESP32: " + WiFi.localIP().toString());
        
        // Verificar conectividad con AWS
        Serial.println("Verificando conexión con AWS...");
        IPAddress serverIP;
        if (WiFi.hostByName("3.215.21.245", serverIP)) {
            Serial.println("IP de AWS: " + serverIP.toString());
        } else {
            Serial.println("No se puede resolver la IP de AWS");
        }
    } else {
        Serial.println("\nFallo WiFi - Reiniciando");
        ESP.restart();
    }
}

void loop() {
    static unsigned long lastAttemptTime = 0;
    static int failedAttempts = 0;
    const int MAX_FAILED_ATTEMPTS = 5;
    
    // Verificar conexión WiFi
    if (WiFi.status() != WL_CONNECTED) {
        Serial.println("WiFi desconectado - Reconectando");
        WiFi.reconnect();
        delay(5000);
        return;
    }

    // Variables para las lecturas
    float temperature = 0.0;
    float humidity = 0.0;
    float pressure = 0.0;

    // Leer SHTC3
    if (shtc3.update() == SHTC3_Status_Nominal) {
        temperature = shtc3.toDegC();
        humidity = shtc3.toPercent();
        Serial.printf("Temp: %.2f°C, Hum: %.2f%%\n", temperature, humidity);
    } else {
        Serial.println("Error leyendo SHTC3");
        delay(1000);
        return;
    }

    // Leer LPS22
    sensors_event_t temp_event, pressure_event;
    if (lps22.getEvent(&pressure_event, &temp_event)) {
        pressure = pressure_event.pressure;
        Serial.printf("Presión: %.2f hPa\n", pressure);
    } else {
        Serial.println("Error leyendo LPS22");
        delay(1000);
        return;
    }

    // Calcular probabilidad de lluvia
    float rainProb = calculateRainProbability(temperature, humidity, pressure);

    // Preparar JSON
    StaticJsonDocument<200> doc;
    doc["temperature"] = temperature;
    doc["humidity"] = humidity;
    doc["pressure"] = pressure;
    doc["rain_probability"] = rainProb;

    String jsonString;
    serializeJson(doc, jsonString);

    // Enviar datos
    HTTPClient http;
    http.setTimeout(10000); // 10 segundos de timeout

    Serial.println("Conectando a AWS: " + String(serverName));
    http.begin(serverName);
    http.addHeader("Content-Type", "application/json");
    
    Serial.println("Enviando datos: " + jsonString);
    int httpCode = http.POST(jsonString);
    
    if (httpCode > 0) {
        String response = http.getString();
        Serial.printf("HTTP Code: %d\nRespuesta: %s\n", httpCode, response.c_str());
        failedAttempts = 0;  // Resetear contador de intentos fallidos
        
        if (httpCode == HTTP_CODE_OK || httpCode == HTTP_CODE_CREATED) {
            Serial.println("Datos enviados correctamente a AWS");
        } else {
            Serial.println("Respuesta inesperada de AWS");
        }
    } else {
        Serial.printf("Error HTTP: %d\n", httpCode);
        Serial.println("Error específico: " + http.errorToString(httpCode));
        failedAttempts++;

        if (failedAttempts >= MAX_FAILED_ATTEMPTS) {
            Serial.println("Demasiados intentos fallidos. Reiniciando...");
            ESP.restart();
        }
    }
    
    http.end();
    delay(10000);  // 10 segundos entre lecturas para pruebas
}

float calculateRainProbability(float temp, float humidity, float pressure) {
    if (temp >= 15 && temp <= 25 && humidity >= 80 && pressure < 1013) {
        return 80.0;
    } else if (humidity >= 70 && pressure < 1013) {
        return 60.0;
    } else if (humidity >= 60 && pressure < 1015) {
        return 40.0;
    }
    return 20.0;
}
