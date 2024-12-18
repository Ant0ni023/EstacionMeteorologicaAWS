from flask import Flask, render_template, jsonify, request
from pymongo import MongoClient
from datetime import datetime
import pytz
import logging
from logging.handlers import RotatingFileHandler
import smtplib
from email.mime.text import MIMEText
from email.mime.multipart import MIMEMultipart
import ssl
import sys

app = Flask(__name__)

# Configuración de logging
logging.basicConfig(level=logging.INFO)
handler = RotatingFileHandler('weather_station.log', maxBytes=10000, backupCount=3)
handler.setFormatter(logging.Formatter(
    '%(asctime)s %(levelname)s: %(message)s [in %(pathname)s:%(lineno)d]'
))
app.logger.addHandler(handler)

# Configuración de correo electrónico
EMAIL_SENDER = "Eliminado ahora que se sube el codigo a GitHub"
EMAIL_PASSWORD = "Eliminado ahora que se sube el codigo a GitHub"
SMTP_SERVER = "smtp.gmail.com"
SMTP_PORT = 587
EMAIL_RECIPIENT = "Eliminado ahora que se sube el codigo a GitHub"

# Variables de AWS
AWS_PUBLIC_IP = "Eliminado ahora que se sube el codigo a GitHub"

# Conexión a MongoDB
try:
    client = MongoClient("mongodb://localhost:27017/", serverSelectionTimeoutMS=5000)
    client.admin.command('ping')
    db = client.weather_data
    app.logger.info("MongoDB conectado exitosamente")
except Exception as e:
    app.logger.error(f"Error MongoDB: {e}")
    sys.exit(1)

def send_email(subject, body):
    """Envía un correo electrónico."""
    try:
        msg = MIMEMultipart()
        msg['From'] = EMAIL_SENDER
        msg['To'] = EMAIL_RECIPIENT
        msg['Subject'] = f"[AWS {AWS_PUBLIC_IP}] {subject}"
        msg.attach(MIMEText(body, 'plain'))

        with smtplib.SMTP(SMTP_SERVER, SMTP_PORT) as server:
            server.starttls()
            server.login(EMAIL_SENDER, EMAIL_PASSWORD)
            server.send_message(msg)
        app.logger.info(f"Correo enviado: {subject}")
    except Exception as e:
        app.logger.error(f"Error al enviar correo: {e}")

def check_temperature_alerts(temperature):
    """Revisa la temperatura y envía un correo si supera los 25 grados."""
    if temperature > 25:
        send_email(
            "¡ALERTA DE TEMPERATURA ALTA!",
            f"Se detectó una temperatura alta:\n\n"
            f"Temperatura Actual: {temperature:.2f}°C\n"
            f"Hora: {datetime.now(pytz.timezone('America/Santiago')).strftime('%H:%M:%S %d/%m/%Y')}\n\n"
            "RECOMENDACIONES:\n- Mantente hidratado.\n- Evita exposición prolongada al sol.\n- Usa ropa ligera>
        )

@app.route('/')
def home():
    try:
        tz = pytz.timezone('America/Santiago')
        now = datetime.now(tz)
        latest_reading = db.readings.find_one(sort=[("timestamp", -1)])
        readings = list(db.readings.find().sort("timestamp", -1).limit(50))

        for r in readings:
            r["_id"] = str(r["_id"])
            r["timestamp_str"] = r["timestamp"].astimezone(tz).strftime("%H:%M:%S %d/%m/%Y")
            r["temperature"] = f"{float(r['temperature']):.2f}"
            r["humidity"] = f"{float(r['humidity']):.2f}"
            r["pressure"] = f"{float(r['pressure']):.2f}"
            r["rain_probability"] = f"{float(r['rain_probability']):.2f}"

        return render_template("index.html",
                               city="Temuco",
                               country="Chile",
                               current_time=now.strftime("%H:%M %d/%m/%Y"),
                               temperature=f"{float(latest_reading['temperature']):.2f}" if latest_reading els>
                               humidity=f"{float(latest_reading['humidity']):.2f}" if latest_reading else "0.0>
                               pressure=f"{float(latest_reading['pressure']):.2f}" if latest_reading else "0.0>
                               rain_probability=f"{float(latest_reading.get('rain_probability', 0)):.2f}" if l>
                               readings=readings)
    except Exception as e:
        app.logger.error(f"Error en /: {e}")
        return "Error del servidor", 500

@app.route('/readings', methods=['POST'])
def add_reading():
    """Recibe datos del ESP32, verifica alertas y guarda en MongoDB."""
    try:
        data = request.get_json()
        if not data:
            return jsonify({"error": "Sin datos"}), 400

        temperature = float(data["temperature"])
        check_temperature_alerts(temperature)

        tz = pytz.timezone('America/Santiago')
        reading = {
            "temperature": round(temperature, 2),
            "humidity": round(float(data["humidity"]), 2),
            "pressure": round(float(data["pressure"]), 2),
            "rain_probability": round(float(data["rain_probability"]), 2),
            "timestamp": datetime.now(tz)
        }
        result = db.readings.insert_one(reading)
        app.logger.info(f"Nueva lectura guardada: {reading}")
        return jsonify({"message": "Lectura guardada exitosamente", "id": str(result.inserted_id)}), 200
    except Exception as e:
        app.logger.error(f"Error en POST /readings: {e}")
        return jsonify({"error": str(e)}), 500

@app.route('/readings', methods=['GET'])
def get_readings():
    """Obtiene las últimas 10 lecturas desde MongoDB."""
    try:
        readings = list(db.readings.find().sort("timestamp", -1).limit(10))
        tz = pytz.timezone('America/Santiago')
        for r in readings:
            r["_id"] = str(r["_id"])
            r["timestamp_str"] = r["timestamp"].astimezone(tz).strftime("%H:%M:%S %d/%m/%Y")
            r["temperature"] = f"{float(r['temperature']):.2f}"
            r["humidity"] = f"{float(r['humidity']):.2f}"
            r["pressure"] = f"{float(r['pressure']):.2f}"
            r["rain_probability"] = f"{float(r['rain_probability']):.2f}"
        return jsonify(readings)
    except Exception as e:
        app.logger.error(f"Error en GET /readings: {e}")
        return jsonify({"error": str(e)}), 500

if __name__ == '__main__':
    app.logger.info(f"Servidor Flask iniciado en http://{AWS_PUBLIC_IP}:8080")
    app.run(host='0.0.0.0', port=8080, debug=True)
