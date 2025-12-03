package com.example.dbzapp

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.*
import com.example.dbzapp.ui.theme.DBZappTheme

class MainActivity : ComponentActivity() {
    // Variable de estado para comunicar la detección NFC a Compose
    private var nfcDetectedState = mutableStateOf(false)
    private var nfcAdapter: NfcAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Inicializar adaptador NFC
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        setContent {
            DBZappTheme {
                // Pasamos el estado del NFC y una función para resetearlo
                AppNavegacion(
                    nfcDetected = nfcDetectedState.value,
                    onNfcProcessed = { nfcDetectedState.value = false }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Configurar el sistema para que nuestra App tenga prioridad al leer NFC
        val intent = Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_MUTABLE
        )
        val filters = arrayOf(IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED))
        nfcAdapter?.enableForegroundDispatch(this, pendingIntent, filters, null)
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter?.disableForegroundDispatch(this)
    }

    // Manejar la detección de NFC
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.action == NfcAdapter.ACTION_TAG_DISCOVERED) {
            // aviso de deteccion de tarjeta
            nfcDetectedState.value = true
            handleNfcIntent(intent)
        }
    }
    private fun handleNfcIntent(intent: Intent) {
        if (NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action) {

            val rawMessages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)

            if (rawMessages != null) {
                val messages = rawMessages.map { it as NdefMessage }

                for (msg in messages) {
                    for (record in msg.records) {
                        val payload = String(record.payload)
                        Toast.makeText(this, "NFC leído: $payload", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }
}

@Composable
fun AppNavegacion(nfcDetected: Boolean, onNfcProcessed: () -> Unit) {
    val navController = rememberNavController()

    var saldo by remember { mutableDoubleStateOf(1250.75) }

    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            PantallaLogin(
                onLoginSuccess = { usuario ->
                    navController.navigate("banco/$usuario")
                }
            )
        }

        composable("banco/{usuario}") { backStackEntry ->
            val usuario = backStackEntry.arguments?.getString("usuario") ?: "Usuario"
            PantallaBanco(
                usuario = usuario,
                saldo = saldo, // Pasamos el saldo
                onRecargarClick = { navController.navigate("recarga") },
                onPagarNfcClick = { navController.navigate("pago_nfc") }
            )
        }

        composable("recarga") {
            PantallaRecarga(onBack = { navController.popBackStack() })
        }

        composable("pago_nfc") {
            PantallaPagoNFC(
                nfcDetected = nfcDetected,
                onPagoExitoso = {
                    saldo -= 8.00 // Descontamos los 8 pesos
                    onNfcProcessed() // Reseteamos el flag de NFC
                },
                onBack = { navController.popBackStack() }
            )
        }
    }
}

@Composable
fun PantallaLogin(onLoginSuccess: (String) -> Unit) {

    var usuario by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Iniciar Sesión", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        TextField(value = usuario, onValueChange = { usuario = it }, label = { Text("Usuario") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(12.dp))
        TextField(value = password, onValueChange = { password = it }, label = { Text("Contraseña") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = { if (usuario == "Irving" && password == "1234") onLoginSuccess(usuario) else mensaje = "Error" }, modifier = Modifier.fillMaxWidth()) { Text("Iniciar Sesión") }
        if (mensaje.isNotEmpty()) Text(mensaje, color = MaterialTheme.colorScheme.error)
    }
}

@Composable
fun PantallaBanco(
    usuario: String,
    saldo: Double, // Recibimos el saldo como parámetro
    onRecargarClick: () -> Unit,
    onPagarNfcClick: () -> Unit // Nuevo evento
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(colors = listOf(Color(0xFF1A237E), Color(0xFF283593))))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Bienvenido $usuario",
            style = MaterialTheme.typography.headlineMedium.copy(color = Color.White, fontWeight = FontWeight.Bold),
            modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(40.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.Start) {
                Text("Saldo disponible", color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                // Mostrar el saldo dinámico
                Text(
                    text = "$${"%.2f".format(saldo)} MXN",
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold, color = Color(0xFF1A237E))
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Botón Recargar
        Button(
            onClick = onRecargarClick,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3949AB)),
            modifier = Modifier.fillMaxWidth().height(55.dp)
        ) {
            Text("Recargar cuenta", style = MaterialTheme.typography.titleMedium.copy(color = Color.White))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // boton nfc
        Button(
            onClick = onPagarNfcClick,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00695C)), // Color
            modifier = Modifier.fillMaxWidth().height(55.dp)
        ) {
            Icon(Icons.Default.CreditCard, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Pagar con NFC ($8.00)", style = MaterialTheme.typography.titleMedium.copy(color = Color.White))
        }
    }
}

@Composable
fun PantallaPagoNFC(
    nfcDetected: Boolean,
    onPagoExitoso: () -> Unit,
    onBack: () -> Unit
) {

    var pagoRealizado by remember { mutableStateOf(false) }

    // Efecto que reacciona cuando nfcDetected cambia a true
    LaunchedEffect(nfcDetected) {
        if (nfcDetected && !pagoRealizado) {
            pagoRealizado = true
            onPagoExitoso() // Descuenta el dinero
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (!pagoRealizado) {
            // esperar pago
            Icon(
                imageVector = Icons.Default.CreditCard,
                contentDescription = "NFC",
                modifier = Modifier.size(100.dp),
                tint = Color.Gray
            )
            Spacer(modifier = Modifier.height(24.dp))
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "Esperando dispositivo de pago...",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
            Text(
                "Acerque su tarjeta al reverso del celular",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = Color.Gray
            )
        } else {
            // pago realizado
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Exito",
                modifier = Modifier.size(100.dp),
                tint = Color(0xFF4CAF50) // Verde éxito
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "Pago realizado",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Se han descontado $8.00 de tu saldo.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text(if (pagoRealizado) "Volver al inicio" else "Cancelar")
        }
    }
}

@Composable
fun PantallaRecarga(onBack: () -> Unit) {

    var nombre by remember { mutableStateOf("") }
    var numeroTarjeta by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Recarga con tarjeta", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(24.dp))
        TextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(12.dp))
        TextField(value = numeroTarjeta, onValueChange = { numeroTarjeta = it }, label = { Text("Tarjeta") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = { mensaje = "Recarga exitosa (Simulada)" }, modifier = Modifier.fillMaxWidth()) { Text("Confirmar") }
        if(mensaje.isNotEmpty()) Text(mensaje)
        Spacer(modifier = Modifier.height(24.dp))
        TextButton(onClick = onBack) { Text("Volver") }
    }
}