package dev.isln.alignersinfo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            val navHostController = rememberNavController()
            NavHost(
                navController = navHostController,
                startDestination = "MainScreen",
                builder = {
                    composable("MainScreen") {
                        MainScreen(navHostController)
                    }
                    composable("SettingsScreen") {
                        SettingsScreen(navHostController)
                    }
                }
            )
        }
    }
}

@Composable
fun MainScreen(navController: NavController) {
    var visible by remember { mutableStateOf(true) }


    Scaffold(modifier = Modifier.fillMaxSize()) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Text("MainScreen")
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                if (visible) {
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = { navController.navigate("SettingsScreen") },
                    ) { Text("Goto Settings screen") }
                }

                Button(
                    modifier = Modifier.weight(1f),
                    onClick = { visible = !visible }
                ) { Text("Button 2") }
            }
        }
    }
}

@Composable
fun SettingsScreen(navController: NavController) {
    var text by remember { mutableStateOf("Hello") }

    Scaffold(modifier = Modifier.fillMaxSize()) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Text("Settings screen")
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = { navController.popBackStack() },
                ) { Text("Back") }
            }
            Spacer(modifier = Modifier.height(12.dp))
            TextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Label") }
            )
        }
    }
}
