package com.example

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.domain.model.GenerationLanguage
import com.example.domain.model.StudioSection
import com.example.ui.navigation.RouteCreatorTool
import com.example.ui.navigation.RouteDashboard
import com.example.ui.navigation.RouteHistory
import com.example.ui.navigation.RouteSettings
import com.example.ui.navigation.RouteSplash
import com.example.ui.navigation.RouteStudioSection
import com.example.ui.screens.CreatorToolWorkspaceScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.StudioSectionScreen
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.SNCreativityTheme
import com.example.ui.viewmodel.SNCreativityViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SNCreativityAppRoot()
        }
    }
}

@Composable
fun SNCreativityAppRoot(
    skipSplashForTest: Boolean = false
) {
    val app = LocalContext.current.applicationContext as Application
    val viewModel: SNCreativityViewModel = viewModel(
        factory = SNCreativityViewModel.Factory(app)
    )

    val preferences by viewModel.preferencesState.collectAsStateWithLifecycle()
    val historyEntries by viewModel.allHistory.collectAsStateWithLifecycle()
    val toolFormState by viewModel.toolFormState.collectAsStateWithLifecycle()
    val connectionMode by viewModel.aiConnectionMode.collectAsStateWithLifecycle()

    SNCreativityTheme(themeMode = preferences.themeMode) {
        val navController = rememberNavController()
        val backStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = backStackEntry?.destination

        val isTopLevelDestination = currentDestination?.let { dest ->
            dest.hasRoute<RouteDashboard>() ||
                dest.hasRoute<RouteHistory>() ||
                dest.hasRoute<RouteSettings>()
        } == true

        fun navigateTopLevel(route: Any) {
            navController.navigate(route) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }

        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val useNavigationRail = maxWidth >= 600.dp

            Row(modifier = Modifier.fillMaxSize()) {
                if (isTopLevelDestination && useNavigationRail) {
                    NavigationRail(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                        modifier = Modifier
                            .fillMaxHeight()
                            .testTag("main_navigation_rail")
                    ) {
                        val onDash = currentDestination?.hasRoute<RouteDashboard>() == true
                        val onHist = currentDestination?.hasRoute<RouteHistory>() == true
                        val onSett = currentDestination?.hasRoute<RouteSettings>() == true

                        NavigationRailItem(
                            selected = onDash,
                            onClick = { navigateTopLevel(RouteDashboard) },
                            icon = {
                                Icon(
                                    imageVector = if (onDash) Icons.Filled.Dashboard else Icons.Outlined.Dashboard,
                                    contentDescription = stringResource(R.string.nav_dashboard)
                                )
                            },
                            label = { Text(stringResource(R.string.nav_dashboard)) },
                            modifier = Modifier.testTag("nav_tab_dashboard")
                        )
                        NavigationRailItem(
                            selected = onHist,
                            onClick = { navigateTopLevel(RouteHistory) },
                            icon = {
                                Icon(
                                    imageVector = if (onHist) Icons.Filled.History else Icons.Outlined.History,
                                    contentDescription = stringResource(R.string.nav_history)
                                )
                            },
                            label = { Text(stringResource(R.string.nav_history)) },
                            modifier = Modifier.testTag("nav_tab_history")
                        )
                        NavigationRailItem(
                            selected = onSett,
                            onClick = { navigateTopLevel(RouteSettings) },
                            icon = {
                                Icon(
                                    imageVector = if (onSett) Icons.Filled.Settings else Icons.Outlined.Settings,
                                    contentDescription = stringResource(R.string.nav_settings)
                                )
                            },
                            label = { Text(stringResource(R.string.nav_settings)) },
                            modifier = Modifier.testTag("nav_tab_settings")
                        )
                    }
                }

                Scaffold(
                    modifier = Modifier.weight(1f),
                    contentWindowInsets = WindowInsets.safeDrawing,
                    containerColor = MaterialTheme.colorScheme.background,
                    bottomBar = {
                        if (isTopLevelDestination && !useNavigationRail) {
                            val onDash = currentDestination?.hasRoute<RouteDashboard>() == true
                            val onHist = currentDestination?.hasRoute<RouteHistory>() == true
                            val onSett = currentDestination?.hasRoute<RouteSettings>() == true

                            NavigationBar(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                                modifier = Modifier.testTag("main_bottom_navigation")
                            ) {
                                NavigationBarItem(
                                    selected = onDash,
                                    onClick = { navigateTopLevel(RouteDashboard) },
                                    icon = {
                                        Icon(
                                            imageVector = if (onDash) Icons.Filled.Dashboard else Icons.Outlined.Dashboard,
                                            contentDescription = stringResource(R.string.nav_dashboard)
                                        )
                                    },
                                    label = { Text(stringResource(R.string.nav_dashboard)) },
                                    modifier = Modifier.testTag("nav_tab_dashboard")
                                )
                                NavigationBarItem(
                                    selected = onHist,
                                    onClick = { navigateTopLevel(RouteHistory) },
                                    icon = {
                                        Icon(
                                            imageVector = if (onHist) Icons.Filled.History else Icons.Outlined.History,
                                            contentDescription = stringResource(R.string.nav_history)
                                        )
                                    },
                                    label = { Text(stringResource(R.string.nav_history)) },
                                    modifier = Modifier.testTag("nav_tab_history")
                                )
                                NavigationBarItem(
                                    selected = onSett,
                                    onClick = { navigateTopLevel(RouteSettings) },
                                    icon = {
                                        Icon(
                                            imageVector = if (onSett) Icons.Filled.Settings else Icons.Outlined.Settings,
                                            contentDescription = stringResource(R.string.nav_settings)
                                        )
                                    },
                                    label = { Text(stringResource(R.string.nav_settings)) },
                                    modifier = Modifier.testTag("nav_tab_settings")
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = if (skipSplashForTest) RouteDashboard else RouteSplash,
                        enterTransition = { fadeIn(animationSpec = tween(240)) },
                        exitTransition = { fadeOut(animationSpec = tween(180)) },
                        modifier = Modifier.fillMaxSize()
                    ) {
                        composable<RouteSplash> {
                            SplashScreen(
                                enable3DMotion = preferences.enable3DMotionEffects,
                                onSplashFinished = {
                                    navController.navigate(RouteDashboard) {
                                        popUpTo<RouteSplash> { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable<RouteDashboard> {
                            DashboardScreen(
                                connectionMode = connectionMode,
                                themeMode = preferences.themeMode,
                                preferredLanguage = preferences.preferredLanguage,
                                enable3DMotion = preferences.enable3DMotionEffects,
                                recentHistory = historyEntries,
                                onToggleTheme = {
                                    val next = if (preferences.themeMode == AppThemeMode.NEON_DARK) {
                                        AppThemeMode.CREATIVE_LIGHT
                                    } else {
                                        AppThemeMode.NEON_DARK
                                    }
                                    viewModel.setThemeMode(next)
                                },
                                onToggleLanguage = {
                                    val next = if (preferences.preferredLanguage == GenerationLanguage.ENGLISH) {
                                        GenerationLanguage.ROMAN_URDU
                                    } else {
                                        GenerationLanguage.ENGLISH
                                    }
                                    viewModel.setPreferredLanguage(next)
                                },
                                onOpenSection = { section ->
                                    navController.navigate(RouteStudioSection(section.id))
                                },
                                onOpenToolDirect = { toolId, historyId ->
                                    navController.navigate(RouteCreatorTool(toolId, historyId))
                                },
                                onOpenHistory = {
                                    navigateTopLevel(RouteHistory)
                                },
                                onOpenSettings = {
                                    navigateTopLevel(RouteSettings)
                                },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }

                        composable<RouteStudioSection> { entry ->
                            val args = entry.toRoute<RouteStudioSection>()
                            val section = StudioSection.entries.firstOrNull { it.id == args.sectionId }
                                ?: StudioSection.GAMING_STUDIO
                            StudioSectionScreen(
                                section = section,
                                enable3DMotion = preferences.enable3DMotionEffects,
                                onBack = { navController.popBackStack() },
                                onSelectTool = { toolId ->
                                    navController.navigate(RouteCreatorTool(toolId, -1L))
                                }
                            )
                        }

                        composable<RouteCreatorTool> { entry ->
                            val args = entry.toRoute<RouteCreatorTool>()
                            LaunchedEffect(args.toolId, args.historyEntryId) {
                                viewModel.initializeToolSession(args.toolId, args.historyEntryId)
                            }
                            CreatorToolWorkspaceScreen(
                                formState = toolFormState,
                                enable3DMotion = preferences.enable3DMotionEffects,
                                onBack = { navController.popBackStack() },
                                onOpenSettings = {
                                    navController.navigate(RouteSettings)
                                },
                                onPrimaryInputChange = viewModel::updatePrimaryInput,
                                onSecondaryInputChange = viewModel::updateSecondaryInput,
                                onLanguageChange = viewModel::updateToolLanguage,
                                onDetailChange = viewModel::updateDetailLevel,
                                onToneChange = viewModel::updateTone,
                                onAspectChange = viewModel::updateAspectFormat,
                                onDurationChange = viewModel::updateDurationPreset,
                                onPlatformChange = viewModel::updatePlatform,
                                onWorkflowPresetChange = viewModel::updateWorkflowPreset,
                                onEnhancerOutputTypeChange = viewModel::updateEnhancerOutputType,
                                onVisualStyleChange = viewModel::updateVisualStyle,
                                onSpecialRequirementsChange = viewModel::updateSpecialRequirements,
                                onPreserveConstraintsChange = viewModel::updatePreserveReferenceConstraints,
                                onWebGroundingChange = viewModel::updateWebResearchGrounding,
                                onGenerate = viewModel::generateContent,
                                onGenerateLocalFallback = viewModel::generateUsingLocalFallback,
                                onCancelGeneration = viewModel::cancelActiveGeneration,
                                onToggleSaveResult = viewModel::toggleCurrentResultSaved
                            )
                        }

                        composable<RouteHistory> {
                            HistoryScreen(
                                historyEntries = historyEntries,
                                enable3DMotion = preferences.enable3DMotionEffects,
                                onReusePrompt = { toolId, historyId ->
                                    navController.navigate(RouteCreatorTool(toolId, historyId))
                                },
                                onToggleSaved = viewModel::toggleHistoryFavorite,
                                onDeleteEntry = viewModel::deleteHistoryEntry,
                                onClearAllHistory = viewModel::clearAllHistory,
                                modifier = Modifier.padding(innerPadding)
                            )
                        }

                        composable<RouteSettings> {
                            SettingsScreen(
                                preferences = preferences,
                                connectionMode = connectionMode,
                                historyCount = historyEntries.size,
                                onSelectTheme = viewModel::setThemeMode,
                                onSelectLanguage = viewModel::setPreferredLanguage,
                                onToggle3DMotion = viewModel::setEnable3DMotion,
                                onToggleAutoFallback = viewModel::setAutoLocalFallback,
                                onRefreshAiStatus = viewModel::refreshConnectionStatus,
                                onOpenHistory = { navigateTopLevel(RouteHistory) },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                    }
                }
            }
        }
    }
}
