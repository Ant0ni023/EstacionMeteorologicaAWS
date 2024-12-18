# EstacionMeteorologicaAWS

Sistema de Monitoreo de Estación Meteorológica desarrollado con IoT. Incluye captura de datos climáticos con sensores, visualización gráfica en una página web, aplicación móvil en Android Studio y servidor en AWS con Flask y MongoDB. Proyecto número 3 de la asignatura Taller de Computación.

## Descripción

Este proyecto implementa una estación meteorológica completa que:
- Captura datos de temperatura, humedad, presión atmosférica y probabilidad de lluvia usando sensores IoT
- Procesa y almacena los datos en un servidor AWS EC2 usando MongoDB
- Visualiza la información en tiempo real a través de una página web
- Ofrece una aplicación móvil Android para consulta de datos
- Envía alertas por correo cuando se detectan temperaturas elevadas

## Componentes Electrónicos
- Placa base WisBlock 2nd Gen RAK19007
- WiFi y BLE Espressif ESP32-WROVER RAK11200
- Sensor de Temperatura y Humedad Sensirion SHTC3 RAK1901
- Sensor de presión barométrica RAK1902

## Estructura del Repositorio

### /aplicacion_movil
Contiene el proyecto de Android Studio desarrollado en Kotlin con Jetpack Compose para la visualización móvil de los datos meteorológicos.

### /sensores
Código para el ESP32 RAK11200 que maneja los sensores y envía datos al servidor AWS:
- Sensor de Temperatura y Humedad SHTC3
- Sensor de Presión Barométrica RAK1902

### /servidor
Implementación del servidor en AWS EC2 usando:
- Python Flask para la API REST
- MongoDB para almacenamiento de datos
- Interfaz web para visualización
- Sistema de alertas por correo

## Requisitos Cumplidos

1. ✅ Habilitación y configuración de servidor Linux en AWS EC2
2. ✅ Base de datos no relacional MongoDB
3. ✅ Servicios REST en Python-Flask 
4. ✅ Aplicación Android en Kotlin
5. ✅ Programación de controladores IoT con sensores
6. ✅ Visualización web de datos
