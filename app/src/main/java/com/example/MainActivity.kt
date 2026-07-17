package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import com.example.ui.QuestViewModel
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.Translations
import com.example.ui.screens.*
import com.example.ui.theme.*

enum class AppScreen {
    HOME,
    ADD_QUEST,
    PROFILE,
    SETTINGS
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Stably initialize the QuestViewModel
        val viewModel = ViewModelProvider(this)[QuestViewModel::class.java]

        setContent {
            val profile by viewModel.playerProfile.collectAsState()
            val language = AppLanguage.fromCode(profile.languageCode)
            val layoutDirection = language.layoutDirection
            
            // Re-compose entire layout direction (RTL if Persian, LTR if English/German)
            CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                MyApplicationTheme(darkTheme = profile.isDarkMode) {
                    var currentScreen by remember { mutableStateOf(AppScreen.HOME) }
                    
                    // Observe temporary XP alert events and level up event dialog triggers
                    val levelUpLevel by viewModel.levelUpEvent.collectAsState()
                    val xpGainedValue by viewModel.xpGainEvent.collectAsState()

                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        bottomBar = {
                            NavigationBar(
                                modifier = Modifier
                                    .border(width = 1.dp, color = BorderSilver, shape = androidx.compose.foundation.shape.AbsoluteRoundedCornerShape(0.dp))
                                    .windowInsetsPadding(WindowInsets.navigationBars),
                                containerColor = GlassSurface,
                                tonalElevation = 0.dp
                            ) {
                                val itemColors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = GlowingCyan,
                                    selectedTextColor = GlowingCyan,
                                    unselectedIconColor = TextGrey,
                                    unselectedTextColor = TextGrey,
                                    indicatorColor = GlowingCyan.copy(alpha = 0.15f)
                                )

                                NavigationBarItem(
                                    selected = currentScreen == AppScreen.HOME,
                                    onClick = { currentScreen = AppScreen.HOME },
                                    icon = { Icon(Icons.Default.Home, contentDescription = "Active Quests") },
                                    label = { Text(Translations.getString("home_tab", language)) },
                                    colors = itemColors
                                )
                                NavigationBarItem(
                                    selected = currentScreen == AppScreen.ADD_QUEST,
                                    onClick = { currentScreen = AppScreen.ADD_QUEST },
                                    icon = { Icon(Icons.Default.Add, contentDescription = "New Quest") },
                                    label = { Text(Translations.getString("add_tab", language)) },
                                    colors = itemColors
                                )
                                NavigationBarItem(
                                    selected = currentScreen == AppScreen.PROFILE,
                                    onClick = { currentScreen = AppScreen.PROFILE },
                                    icon = { Icon(Icons.Default.Person, contentDescription = "Hero Armory") },
                                    label = { Text(Translations.getString("profile_tab", language)) },
                                    colors = itemColors
                                )
                                NavigationBarItem(
                                    selected = currentScreen == AppScreen.SETTINGS,
                                    onClick = { currentScreen = AppScreen.SETTINGS },
                                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings Options") },
                                    label = { Text(Translations.getString("settings_tab", language)) },
                                    colors = itemColors
                                )
                            }
                        }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            // Render Screens dynamically maintaining Compose states
                            when (currentScreen) {
                                AppScreen.HOME -> HomeScreen(
                                    viewModel = viewModel,
                                    language = language,
                                    onNavigateToAdd = { currentScreen = AppScreen.ADD_QUEST }
                                )
                                AppScreen.ADD_QUEST -> AddQuestScreen(
                                    viewModel = viewModel,
                                    language = language,
                                    onSuccess = { currentScreen = AppScreen.HOME }
                                )
                                AppScreen.PROFILE -> ProfileScreen(
                                    viewModel = viewModel,
                                    language = language
                                )
                                AppScreen.SETTINGS -> SettingsScreen(
                                    viewModel = viewModel,
                                    language = language
                                )
                            }
                            
                            // Floating alert notifications layers
                            FloatingXpGainIndicator(
                                xpGainedValue = xpGainedValue,
                                language = language
                            )
                        }
                    }

                    // Level Up visual overlay modal
                    LevelUpOverlayDialog(
                        newLevel = levelUpLevel,
                        language = language,
                        onDismiss = { viewModel.dismissLevelUpDialog() }
                    )
                }
            }
        }
    }
}
