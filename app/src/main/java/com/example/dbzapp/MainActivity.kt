package com.example.dbzapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import android.app.PendingIntent
import android.content.Intent
import android.nfc.NfcAdapter


class MainActivity : ComponentActivity() {

    private var nfcAdapter: NfcAdapter? = null
    private lateinit var pendingIntent: PendingIntent

    override fun onResume() {
        super.onResume()
        nfcAdapter?.enableForegroundDispatch(this, pendingIntent, null, null)
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter?.disableForegroundDispatch(this)
    }


    override fun onNewIntent(intent: Intent?) {
//        super.onNewIntent(Intent)

        if (intent?.action == NfcAdapter.ACTION_TAG_DISCOVERED) {
            // Cuando se detecta un tag NFC, enviamos un evento global
//            NFCEventManager.realizarCobro()
            var test = "test"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_MUTABLE
        )

        setContent {
            DBZappTheme {
                AppNavegacion()
            }
        }
    }
}

@Composable
fun AppNavegacion() {
    val navController = rememberNavController()

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
                onRecargarClick = { navController.navigate("recarga") }
            )
        }

        composable("recarga") {
            PantallaRecarga(onBack = { navController.popBackStack() })
        }
    }
}

@Composable
fun PantallaLogin(onLoginSuccess: (String) -> Unit) {
    var usuario by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Iniciar Sesión", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = usuario,
            onValueChange = { usuario = it },
            label = { Text("Nombre de usuario") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (usuario == "Irving" && password == "1234") {
                    onLoginSuccess(usuario)
                } else {
                    mensaje = "Usuario o contraseña incorrecta"
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Iniciar Sesión")
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (mensaje.isNotEmpty()) {
            Text(mensaje, color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
fun PantallaBanco(usuario: String, onRecargarClick: () -> Unit) {
    var saldo by remember { mutableDoubleStateOf(1250.75) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF1A237E), Color(0xFF283593))
                )
            )
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Bienvenido $usuario",
            style = MaterialTheme.typography.headlineMedium.copy(
                color = Color.White,
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(40.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text("Saldo disponible", color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "$${"%.2f".format(saldo)} MXN",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A237E)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = onRecargarClick,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3949AB)),
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp)
        ) {
            Text(
                text = "Recargar cuenta",
                style = MaterialTheme.typography.titleMedium.copy(color = Color.White)
            )
        }
    }
}

@Composable
fun PantallaRecarga(onBack: () -> Unit) {
    var nombre by remember { mutableStateOf("") }
    var numeroTarjeta by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Recarga con tarjeta de débito",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        TextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre del titular") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        TextField(
            value = numeroTarjeta,
            onValueChange = { numeroTarjeta = it },
            label = { Text("Número de tarjeta") },
            visualTransformation = VisualTransformation.None,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TextField(
                value = fecha,
                onValueChange = { fecha = it },
                label = { Text("MM/AA") },
                modifier = Modifier.weight(1f)
            )
            TextField(
                value = cvv,
                onValueChange = { cvv = it },
                label = { Text("CVV") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                mensaje = if (nombre.isNotBlank() && numeroTarjeta.length >= 16 && fecha.isNotBlank() && cvv.length == 3) {
                    "Recarga exitosa 💳"
                } else {
                    "Por favor, completa todos los campos correctamente."
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Confirmar recarga")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(mensaje, color = MaterialTheme.colorScheme.primary)

        Spacer(modifier = Modifier.height(24.dp))

        TextButton(onClick = onBack) {
            Text("Volver")
        }
    }
}

object NFCEvents {
    var onNfcDetected: () -> Unit = {}
}

@Composable
fun Prueba(){}