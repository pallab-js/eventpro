package com.eventpro.admin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.eventpro.admin.data.preferences.ThemeMode
import com.eventpro.admin.ui.components.BottomNavBar
import com.eventpro.admin.ui.navigation.AppNavGraph
import com.eventpro.admin.ui.theme.EventProTheme
import com.eventpro.admin.ui.theme.ThemeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeVm: ThemeViewModel = hiltViewModel()
            val themeMode by themeVm.prefs.themeMode.collectAsState(initial = ThemeMode.SYSTEM.value)
            val isDark = when (themeMode) {
                ThemeMode.LIGHT.value -> false
                ThemeMode.DARK.value -> true
                else -> isSystemInDarkTheme()
            }
            EventProTheme(darkTheme = isDark) {
                MainScreen()
            }
        }
    }
}

@Composable
private fun MainScreen() {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = { BottomNavBar(navController) }
    ) { innerPadding ->
        Box(Modifier.padding(innerPadding)) {
            AppNavGraph(navController)
        }
    }
}
