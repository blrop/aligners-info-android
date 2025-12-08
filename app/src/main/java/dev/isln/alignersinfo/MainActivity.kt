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
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.core.content.edit

const val START_DATES_KEY = "start-dates"
const val DEFAULT_START_DATES = "2025-05-15,2025-12-01"
const val CHANGE_INTERVALS_KEY = "change-intervals"
const val DEFAULT_CHANGE_INTERVALS = "10,7"
const val TOTAL_ALIGNERS_KEY = "total-aligners"
const val DEFAULT_TOTAL_ALIGNERS = "52"

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
    val context = LocalContext.current
    val sharedPreferences = remember { context.getSharedPreferences("AlignersInfoPrefs", Context.MODE_PRIVATE) }

    val startDates = sharedPreferences.getString(START_DATES_KEY, DEFAULT_START_DATES) ?: ""
    val changeIntervals = sharedPreferences.getString(CHANGE_INTERVALS_KEY, DEFAULT_CHANGE_INTERVALS) ?: ""
    val totalAligners = sharedPreferences.getString(TOTAL_ALIGNERS_KEY, DEFAULT_TOTAL_ALIGNERS) ?: ""
    val params = calculate(startDates, changeIntervals, totalAligners)

    Scaffold(modifier = Modifier.fillMaxSize().padding(12.dp)) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Text("Aligners Info", fontSize = 24.sp)

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = { navController.navigate("SettingsScreen") },
                ) { Text("Settings") }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (params.errorMessage.isNotEmpty()) {
                Text("Error: " + params.errorMessage)
            }

            if (params.showReplaceWarning) {
                Spacer(modifier = Modifier.height(24.dp))
                Text("Replace today!", fontSize = 32.sp)
                Spacer(modifier = Modifier.height(24.dp))
            }

            if (params.showMainBlock) {
                Text("Aligner in use: " + params.current + " of " + params.total, fontSize = 24.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Text("Days left: " + params.replaceInDays, fontSize = 24.sp)
                Text("(replace date: " + params.replaceDate + ")")
                Spacer(modifier = Modifier.height(12.dp))
                Text("Progress: " + params.percent + "%", fontSize = 24.sp)
                Text("(days passed: " + params.daysPassed + " of " + params.daysTotal + ")")
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
        mutableStateOf(sharedPreferences.getString(START_DATES_KEY, DEFAULT_START_DATES) ?: "")
    }
    var changeIntervals by remember {
        mutableStateOf(sharedPreferences.getString(CHANGE_INTERVALS_KEY, DEFAULT_CHANGE_INTERVALS) ?: "")
    }
    var totalAligners by remember {
        mutableStateOf(sharedPreferences.getString(TOTAL_ALIGNERS_KEY, DEFAULT_TOTAL_ALIGNERS) ?: "")
    }

    Scaffold(modifier = Modifier.fillMaxSize().padding(12.dp)) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Text("Aligners Info: Settings", fontSize = 20.sp)
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
                    sharedPreferences.edit { putString(START_DATES_KEY, it) }
                },
                label = { Text("Start dates") }
            )
            Spacer(modifier = Modifier.height(12.dp))
            TextField(
                value = changeIntervals,
                onValueChange = {
                    changeIntervals = it
                    sharedPreferences.edit { putString(CHANGE_INTERVALS_KEY, it) }
                },
                label = { Text("Change intervals") }
            )
            Spacer(modifier = Modifier.height(12.dp))
            TextField(
                value = totalAligners,
                onValueChange = {
                    totalAligners = it
                    sharedPreferences.edit { putString(TOTAL_ALIGNERS_KEY, it) }
                },
                label = { Text("Total aligners number") }
            )
        }
    }
}
