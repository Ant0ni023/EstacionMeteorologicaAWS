package com.example.climatemucoapp

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.climatemucoapp.ui.theme.ClimaTemucoAppTheme
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    private val baseUrl = "direccion eliminada ahora que subo a GitHub el codigo"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ClimaTemucoAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreenWithData(baseUrl)
                }
            }
        }
    }
}

fun isInternetAvailable(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork ?: return false
    val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenWithData(baseUrl: String) {
    var latestReading by remember { mutableStateOf<Reading?>(null) }
    var readingsHistory by remember { mutableStateOf<List<Reading>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    val context = LocalContext.current

    suspend fun loadReadings() {
        if (!isInternetAvailable(context)) {
            hasError = true
            errorMessage = "Sin conexión a Internet"
            isLoading = false
            return
        }

        try {
            val response = getJsonFromUrl("$baseUrl/readings")
            if (response != null) {
                val jsonArray = JSONArray(response)
                val readings = mutableListOf<Reading>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    readings.add(Reading(
                        timestamp = obj.getString("timestamp_str"),
                        temperature = obj.getString("temperature").toFloat(),
                        humidity = obj.getString("humidity").toFloat(),
                        pressure = obj.getString("pressure").toFloat(),
                        rainProbability = obj.getString("rain_probability").toFloat()
                    ))
                }
                if (readings.isNotEmpty()) {
                    latestReading = readings.first()
                    readingsHistory = readings
                    hasError = false
                }
            } else {
                hasError = true
                errorMessage = "Error al cargar datos del servidor AWS"
            }
        } catch (e: Exception) {
            hasError = true
            errorMessage = "Error: ${e.localizedMessage}"
        } finally {
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        loadReadings()
        while (true) {
            delay(5000)
            loadReadings()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Estación Meteorológica",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            "AWS EC2 - AntonioServer",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                isLoading && latestReading == null -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                hasError -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = {
                            isLoading = true
                            hasError = false
                            CoroutineScope(Dispatchers.IO).launch {
                                loadReadings()
                            }
                        }) {
                            Text("Reintentar")
                        }
                    }
                }
                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        latestReading?.let { reading ->
                            WeatherCard(reading = reading)
                            Spacer(modifier = Modifier.height(16.dp))
                            HistorySection(readings = readingsHistory.drop(1))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WeatherCard(reading: Reading) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Temuco, Chile",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                reading.timestamp,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "${String.format("%.2f", reading.temperature)} °C",
                style = MaterialTheme.typography.headlineLarge,
                color = when {
                    reading.temperature > 25 -> MaterialTheme.colorScheme.error
                    reading.temperature < 5 -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.secondary
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            WeatherDetail("Humedad", "${String.format("%.2f", reading.humidity)} %")
            WeatherDetail("Presión", "${String.format("%.2f", reading.pressure)} hPa")
            WeatherDetail("Prob. de Lluvia", "${String.format("%.2f", reading.rainProbability)} %")
        }
    }
}

@Composable
fun WeatherDetail(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun HistorySection(readings: List<Reading>) {
    Column {
        Text(
            "Historial",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Hora", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1.5f))
                    Text("°C", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                    Text("%H", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                    Text("hPa", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                    Text("%LL", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn(
                    modifier = Modifier.heightIn(max = 300.dp)
                ) {
                    items(readings) { reading ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(reading.timestamp,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1.5f))
                            Text(String.format("%.2f", reading.temperature),
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1f))
                            Text(String.format("%.2f", reading.humidity),
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1f))
                            Text(String.format("%.2f", reading.pressure),
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1f))
                            Text(String.format("%.2f", reading.rainProbability),
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

data class Reading(
    val timestamp: String,
    val temperature: Float,
    val humidity: Float,
    val pressure: Float,
    val rainProbability: Float
)

suspend fun getJsonFromUrl(url: String): String? {
    return withContext(Dispatchers.IO) {
        try {
            val client = OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .build()

            val request = Request.Builder()
                .url(url)
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                response.body?.string()
            } else null
        } catch (e: Exception) {
            null
        }
    }
}
