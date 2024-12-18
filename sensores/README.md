# Código ESP32 RAK11200 - Estación Meteorológica

Este código fue desarrollado específicamente para el ESP32-WROVER RAK11200 y sus sensores asociados para capturar y enviar datos meteorológicos al servidor AWS.

## Funcionalidades Implementadas

### Inicialización de Sensores
- Configuración e inicialización del sensor SHTC3 para temperatura y humedad
- Configuración e inicialización del sensor LPS22 para presión barométrica
- Sistema de verificación de inicialización correcta de sensores

### Conexión WiFi
- Conexión automática a la red WiFi configurada
- Sistema de reconexión automática en caso de pérdida de conexión
- Verificación de conectividad con el servidor AWS

### Captura de Datos
**NOTA IMPORTANTE**: 
Para la demostración en el video, configuré la captura de datos cada 10 segundos (en lugar de los 30 segundos requeridos) para mostrar más rápidamente:
- El funcionamiento de la app móvil
- La actualización de la página web
- El sistema de alertas por correo cuando la temperatura supera los 25°C

Para cambiar el intervalo a 30 segundos, modificar la línea: delay(10000);  // 10 segundos entre lecturas para pruebas
por: delay(30000);  // 30 segundos entre lecturas (requisito del proyecto)

El código captura:
- Temperatura en °C (SHTC3)
- Humedad en % (SHTC3)
- Presión atmosférica en hPa (LPS22)
- Cálculo de probabilidad de lluvia basado en los parámetros medidos

### Envío de Datos
- Formato JSON para envío de datos
- Comunicación con el servidor AWS mediante HTTP POST
- Sistema de reintentos en caso de fallos de comunicación
- Timeout de 10 segundos para evitar bloqueos

## Notas de Seguridad
Se han removido las siguientes credenciales:
- SSID de la red WiFi
- Contraseña de WiFi
- IP del servidor AWS

## Características Técnicas
- Comunicación I2C con los sensores
- Uso de librerías específicas para cada sensor
- Sistema de manejo de errores y reintentos
- Monitoreo de estado de conexión
