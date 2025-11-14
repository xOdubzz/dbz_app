package com.example.textoapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.textoapp.ui.theme.TextoAppTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TextoAppTheme {
                PantallaFormulario()
            }
        }
    }
}
@Preview(showBackground = true)
@Composable
fun PantallaFormulario(){
    var nombre by remember{mutableStateOf(value ="")}
    var mensaje by remember {mutableStateOf(value ="")}

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center
    ){
        TextField(
            value = nombre,
            onValueChange = {nombre = it},
            label = {Text("Escribe tu nombre: ")},
            modifier = Modifier.fillMaxWidth( )
        )

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {mensaje = "Hola $nombre"},
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Red,
                contentColor = Color.White
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Picale aqui")
        }
        Spacer(Modifier.height(16.dp))

        Text(
            text = mensaje
        )

    }

}