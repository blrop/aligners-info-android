package dev.isln.alignersinfo

import android.content.Context
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.core.content.edit

const val START_DATES = "start-dates"
const val CHANGE_INTERVALS = "change-intervals"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val params = calculate()

        enableEdgeToEdge()

        setContent {
            val navHostController = rememberNavController()
            NavHost(
                navController = navHostController,
                startDestination = "MainScreen",
                builder = {
                    composable("MainScreen") {
                        MainScreen(navHostController, params)
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
fun MainScreen(navController: NavController, params: MainScreenParams) {
    Scaffold(modifier = Modifier.fillMaxSize().padding(12.dp)) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Text("MainScreen")
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = { navController.navigate("SettingsScreen") },
                ) { Text("Settings") }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text("Percent: " + params.percent)
            Text("Days passed: " + params.daysPassed)
            Text("Days total: " + params.daysTotal)
            Text("Current: " + params.current)
            Text("Total: " + params.total)
            Text("Replace in days: " + params.replaceInDays)
            Text("Replace date: " + params.replaceDate)
            Spacer(modifier = Modifier.height(12.dp))
            if (params.errorMessage.isNotEmpty()) {
                Text("Error: " + params.errorMessage)
            }
            if (params.showReplaceWarning) {
                Text("replace today!")
            }
            if (params.showMainBlock) {
                Text("Main block is shown")
            }
            if (params.showCompletedBlock) {
                Text("Completed block is shown")
            }
        }
    }
}

@Composable
fun SettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val sharedPreferences = remember { context.getSharedPreferences("AlignersInfoPrefs", Context.MODE_PRIVATE) }
    var startDates by remember {
        mutableStateOf(sharedPreferences.getString(START_DATES, "") ?: "")
    }
    var changeIntervals by remember {
        mutableStateOf(sharedPreferences.getString(CHANGE_INTERVALS, "") ?: "")
    }

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
                value = startDates,
                onValueChange = {
                    startDates = it
                    sharedPreferences.edit { putString("saved_text", it) }
                },
                label = { Text("Start dates") }
            )
            Spacer(modifier = Modifier.height(12.dp))
            TextField(
                value = changeIntervals,
                onValueChange = {
                    changeIntervals = it
                    sharedPreferences.edit { putString("saved_text", it) }
                },
                label = { Text("Change intervals") }
            )
        }
    }
}
