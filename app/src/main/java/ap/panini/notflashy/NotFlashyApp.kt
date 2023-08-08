package ap.panini.notflashy

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.Icons.Default
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import ap.panini.notflashy.ui.library.LibraryScreen
import ap.panini.notflashy.ui.library.details.SetDetailsScreen
import ap.panini.notflashy.ui.library.edit.EditSetScreen
import ap.panini.notflashy.ui.library.study.StudyScreen
import ap.panini.notflashy.ui.settings.SettingsScreen
import ap.panini.notflashy.ui.theme.NotFlashyTheme

sealed class Screen(
    val route: String,
    @StringRes val resourceId: Int? = null,
    val icon: ImageVector? = null
) {
    data object Library : Screen("library", R.string.library, Default.Notes)

    data object Settings : Screen("settings", R.string.settings, Default.Settings)

    data object EditSet : Screen("editSet")

    data object SetDetails : Screen("setDetails")

    data object StudySet : Screen("studySet")
}

@Immutable
data class ScaffoldViewState(
    @StringRes val fabText: Int? = null,
    val fabIcon: ImageVector? = null,
    val extendedFab: Boolean? = null,
    val action: () -> Unit = {}
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NotFlashyApp() {
    NotFlashyTheme {
        val navController = rememberNavController()
        var scaffoldViewState by remember {
            mutableStateOf(ScaffoldViewState())
        }

        Scaffold(bottomBar = { NotFlashyBottomNavigation(navController) }, floatingActionButton = {
            if (scaffoldViewState.fabIcon != null && scaffoldViewState.fabText != null) {
                if (scaffoldViewState.extendedFab == false) {
                    FloatingActionButton(
                        onClick = scaffoldViewState.action
                    ) {
                        Icon(
                            scaffoldViewState.fabIcon!!,
                            stringResource(scaffoldViewState.fabText!!)
                        )
                    }
                } else {
                    ExtendedFloatingActionButton(text = {
                        Text(
                            text = stringResource(
                                scaffoldViewState.fabText!!
                            )
                        )
                    }, onClick = scaffoldViewState.action, icon = {
                        Icon(
                            scaffoldViewState.fabIcon!!,
                            stringResource(scaffoldViewState.fabText!!)
                        )
                    })
                }
            }
        }) { padding ->

            val isImeVisible = WindowInsets.isImeVisible
            val innerPadding = remember(isImeVisible) {
                if (isImeVisible) PaddingValues(top = padding.calculateTopPadding()) else padding
            }

            Box(
                modifier = Modifier.padding(innerPadding)
            ) {
                NavHost(
                    navController,
                    startDestination = Screen.Library.route
                ) {
                    // Library
                    composable(Screen.Library.route) {
                        LaunchedEffect(Unit) {
                            scaffoldViewState = ScaffoldViewState(
                                fabIcon = Icons.Filled.Create,
                                fabText = R.string.new_set,
                                extendedFab = true,
                                action = {
                                    navController.navigate(Screen.EditSet.route)
                                }
                            )
                        }

                        LibraryScreen(navigateToSetEdit = {
                            navController.navigate("${Screen.EditSet.route}?setId=$it")
                        }, navigateToSetEntry = {
                            navController.navigate("${Screen.SetDetails.route}/$it")
                        })
                    }

                    // Settings
                    composable(Screen.Settings.route) {
                        LaunchedEffect(Unit) { scaffoldViewState = ScaffoldViewState() }
                        SettingsScreen()
                    }

                    // Edit Set
                    composable(
                        "${Screen.EditSet.route}?setId={setId}&editSpecific={editSpecific}",
                        arguments = listOf(
                            navArgument("setId") {
                                type = NavType.LongType
                                defaultValue = -1
                            },
                            navArgument("editSpecific") {
                                type = NavType.IntType
                                defaultValue = -1
                            }
                        )
                    ) {
                        LaunchedEffect(Unit) { scaffoldViewState = ScaffoldViewState() }
                        EditSetScreen(navigateBack = { navController.popBackStack() })
                    }

                    // Set Details
                    composable(
                        "${Screen.SetDetails.route}/{setId}",
                        arguments = listOf(
                            navArgument("setId") {
                                type = NavType.LongType
                            }
                        )
                    ) {
                        LaunchedEffect(Unit) { scaffoldViewState = ScaffoldViewState() }
                        SetDetailsScreen(navigateToStudy = { setId, isShuffled, onlyStared ->
                            navController.navigate(
                                "${Screen.StudySet.route}/$setId/$isShuffled/$onlyStared"
                            )
                        }, navigateToSetEdit = { setId, editSpecific ->
                            navController.navigate(
                                "${Screen.EditSet.route}?setId=$setId&editSpecific=$editSpecific"
                            )
                        })
                    }

                    // Study set
                    composable(
                        route = "${Screen.StudySet.route}/{setId}/{isShuffled}/{onlyStared}",
                        arguments = listOf(
                            navArgument("setId") { type = NavType.LongType },
                            navArgument("isShuffled") { type = NavType.BoolType },
                            navArgument("onlyStared") { type = NavType.BoolType }
                        )
                    ) {
                        LaunchedEffect(Unit) { scaffoldViewState = ScaffoldViewState() }

                        StudyScreen()
                    }
                }
            }
        }
    }
}

@Composable
fun NotFlashyBottomNavigation(navController: NavController) {
    val items = listOf(
        Screen.Library,
        Screen.Settings
    )

    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination
        items.forEach { screen ->
            NavigationBarItem(
                icon = { Icon(screen.icon!!, contentDescription = null) },
                label = { Text(stringResource(screen.resourceId!!)) },
                selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                onClick = {
                    navController.navigate(screen.route) {
                        // Pop up to the start destination of the graph to
                        // avoid building up a large stack of destinations
                        // on the back stack as users select items
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        // Avoid multiple copies of the same destination when
                        // reselecting the same item
                        launchSingleTop = true
                        // Restore state when reselecting a previously selected item
                        restoreState = true
                    }
                }
            )
        }
    }
}
