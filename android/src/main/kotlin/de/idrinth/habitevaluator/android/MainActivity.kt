package de.idrinth.habitevaluator.android

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import de.idrinth.habitevaluator.android.ui.navigation.AppNavigation
import de.idrinth.habitevaluator.android.ui.navigation.Screen
import de.idrinth.habitevaluator.android.ui.theme.HabitEvaluatorTheme

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: AppViewModel

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(newBase)
        FontSizeHelper.applyFontScale(newBase)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val prefs = getSharedPreferences(SettingsConstants.PREFS_NAME, MODE_PRIVATE)
        SettingsConstants.applyThemeMode(
            prefs.getString(SettingsConstants.KEY_THEME_MODE, SettingsConstants.THEME_SYSTEM) ?: SettingsConstants.THEME_SYSTEM
        )
        SettingsConstants.applyLanguage(
            prefs.getString(SettingsConstants.KEY_LANGUAGE, SettingsConstants.LANGUAGE_SYSTEM) ?: SettingsConstants.LANGUAGE_SYSTEM
        )

        viewModel = ViewModelProvider(this)[AppViewModel::class.java]

        NotificationHelper.ensureNotificationChannel(this)
        ReminderScheduler.rescheduleAll(this)

        setContent {
            HabitEvaluatorTheme {
                AppContent(viewModel = viewModel)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        viewModel.performOnStopSync()
        viewModel.performDailyBackupIfEnabled()
    }
}

private data class BottomNavItem(
    val screen: Screen,
    val labelRes: Int,
    val icon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppContent(viewModel: AppViewModel) {
    val navController = rememberNavController()
    val storageInitialized by viewModel.storageInitialized.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.initializeStorage()
    }

    val bottomNavItems = listOf(
        BottomNavItem(Screen.Home, R.string.home, Icons.Filled.Home),
        BottomNavItem(Screen.Diary, R.string.diary, Icons.AutoMirrored.Filled.List),
        BottomNavItem(Screen.Sleep, R.string.sleep_tracking, Icons.Filled.Favorite),
        BottomNavItem(Screen.EmergencyPlan, R.string.emergency_plan, Icons.Filled.Warning),
        BottomNavItem(Screen.EmotionalState, R.string.emotions, Icons.Filled.Favorite)
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    IconButton(onClick = {
                        navController.navigate(Screen.Stats.route) {
                            popUpTo(Screen.Home.route) { inclusive = false }
                            launchSingleTop = true
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.List, contentDescription = stringResource(R.string.statistics))
                    }
                    IconButton(onClick = {
                        navController.navigate(Screen.Settings.route) {
                            popUpTo(Screen.Home.route) { inclusive = false }
                            launchSingleTop = true
                        }
                    }) {
                        Icon(Icons.Filled.Settings, contentDescription = stringResource(R.string.settings))
                    }
                    IconButton(onClick = {
                        navController.navigate(Screen.Imprint.route) {
                            popUpTo(Screen.Home.route) { inclusive = false }
                            launchSingleTop = true
                        }
                    }) {
                        Icon(Icons.Filled.Info, contentDescription = stringResource(R.string.imprint))
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = stringResource(item.labelRes)) },
                        label = { Text(stringResource(item.labelRes), textAlign = TextAlign.Center) },
                        selected = currentRoute == item.screen.route,
                        onClick = {
                            navController.navigate(item.screen.route) {
                                popUpTo(Screen.Home.route) { inclusive = false }
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        AppNavigation(
            navController = navController,
            viewModel = viewModel,
            modifier = Modifier.padding(paddingValues)
        )
    }
}
