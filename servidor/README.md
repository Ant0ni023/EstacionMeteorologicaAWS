# Códigos Servidor AWS - Estación Meteorológica

Este README detalla los códigos implementados en el servidor AWS EC2 para el proyecto de estación meteorológica, específicamente los archivos app.py y index.html que satisfacen varios de los requisitos mínimos del proyecto.

## Cumplimiento de Requisitos Mínimos

### 1. Server Linux en AWS EC2 ✓
Los códigos en esta carpeta se ejecutan en un servidor AWS EC2 con Linux, donde:
- app.py se ejecuta continuamente para recibir y procesar datos
- Se configura el puerto 8080 para el servicio web

### 2. Base de Datos MongoDB ✓
En app.py implementé la conexión con MongoDB para:
- Almacenar las lecturas de los sensores
- Consultar el historial de mediciones
- Mantener un registro de datos meteorológicos

### 3. Servicios REST en Python-Flask ✓
El archivo app.py contiene los siguientes endpoints REST:
- GET /: Sirve la página web
- GET /readings: Obtiene las últimas 10 lecturas
- POST /readings: Recibe nuevos datos de los sensores

### 4. Visualización Web de Datos ✓
El archivo index.html en la carpeta templates proporciona:
- Visualización en tiempo real de:
  - Temperatura en °C
  - Humedad en %
  - Presión atmosférica en hPa
  - Probabilidad de lluvia
- Actualización automática cada 5 segundos
- Tabla con historial de lecturas

### Notas de Seguridad
Por seguridad, se removieron las siguientes credenciales:
- EMAIL_SENDER: Correo para alertas
- EMAIL_PASSWORD: Contraseña del correo
- EMAIL_RECIPIENT: Destinatario de alertas
- AWS_PUBLIC_IP: IP pública del servidor

### Funcionalidades Adicionales
- Sistema de alertas por correo cuando la temperatura supera los 25°C
