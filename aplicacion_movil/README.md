# Código Android Studio - App Móvil Estación Meteorológica

Este código corresponde a la aplicación móvil desarrollada en Android Studio usando Kotlin y Jetpack Compose.

## Estructura del Proyecto

La aplicación está organizada en los siguientes directorios principales:

### app/src/main/java/com/example/climatemucoapp/
- **MainActivity.kt**: Actividad principal que contiene:
  - Conexión con el servidor AWS
  - Visualización de datos en tiempo real
  - Manejo de estado de la aplicación
  - Actualización automática cada 5 segundos
  - Sistema de manejo de errores y reconexión

### app/src/main/java/com/example/climatemucoapp/ui/theme/
- **Color.kt**: Definición de la paleta de colores
- **Theme.kt**: Implementación del tema de la aplicación
- **Type.kt**: Definición de la tipografía
- **Shape.kt**: Definición de formas para componentes

### app/src/main/res/values/
- **colors.xml**: Definición de colores en XML
- **strings.xml**: Textos localizados
- **themes.xml**: Estilos de la aplicación

### app/src/main/res/xml/
- **network_security_config.xml**: Configuración de seguridad de red
  - Permite tráfico en texto plano (HTTP)
  - Configuración específica para el dominio del servidor AWS
  - Certificados del sistema como fuente de confianza
  - IP del servidor removida por seguridad

## Funcionalidades Implementadas

### Visualización de Datos
- Temperatura en tiempo real
- Humedad
- Presión atmosférica
- Probabilidad de lluvia
- Historial de lecturas

### Características Técnicas
- Diseño Material 3
- Soporte para modo claro/oscuro
- Interfaz responsive
- Manejo de estados de carga y error
- Sistema de reconexión automática

## Notas de Seguridad
Se han eliminado las siguientes credenciales del código:
- URL del servidor AWS en MainActivity
- Dirección IP del servidor en network_security_config.xml
- Configuración de dominio específico en network_security_config.xml

## Principales Componentes UI
- **WeatherCard**: Muestra los datos actuales
- **HistorySection**: Tabla con historial de lecturas
- **ErrorDisplay**: Manejo de errores visual
- **LoadingIndicator**: Indicador de carga
_Esta aplicación forma parte integral del sistema de monitoreo meteorológico, consumiendo datos del servidor AWS para su visualización en dispositivos móviles._
