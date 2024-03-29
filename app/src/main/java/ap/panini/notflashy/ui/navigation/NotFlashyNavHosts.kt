package ap.panini.notflashy.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import ap.panini.notflashy.BottomAppBarViewState
import ap.panini.notflashy.ui.library.LibraryDestination
import ap.panini.notflashy.ui.library.LibraryScreen
import ap.panini.notflashy.ui.library.details.SetDetailsDestination
import ap.panini.notflashy.ui.library.details.SetDetailsScreen
import ap.panini.notflashy.ui.library.edit.EditSetDestination
import ap.panini.notflashy.ui.library.edit.EditSetScreen
import ap.panini.notflashy.ui.library.study.StudyDestination
import ap.panini.notflashy.ui.library.study.StudyScreen
import com.mikepenz.aboutlibraries.ui.compose.LibrariesContainer

@Composable
fun NotFlashyNavHost(
    screenComposing: (BottomAppBarViewState) -> Unit,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController,
        startDestination = LibraryDestination.route,
    ) {
        // Library
        composable(LibraryDestination.routeWithArgs) {
            LibraryScreen(
                onComposing = { screenComposing(it) },
                navigateToOss = {
                    navController.navigate("oss")
                },
                navigateToSetStudy = {
                    navController.navigate(
                        "${StudyDestination.route}/$it",
                    )
                },
                navigateToSetEdit = {
                    navController.navigate(
                        "${EditSetDestination.route}?${EditSetDestination.SET_ID_ARG}=$it",
                    )
                },
                navigateToSetEntry = {
                    navController.navigate(
                        "${SetDetailsDestination.route}/$it",
                    )
                },
            )
        }

        // Edit Set
        composable(
            EditSetDestination.routeWithArgs,
            arguments =
                listOf(
                    navArgument(EditSetDestination.SET_ID_ARG) {
                        type = NavType.LongType
                        defaultValue = -1
                    },
                    navArgument(EditSetDestination.EDIT_SPECIFIC_ARG) {
                        type = NavType.IntType
                        defaultValue = -1
                    },
                ),
        ) {
            EditSetScreen(
                onComposing = { screenComposing(it) },
                navigateBack = { navController.popBackStack() },
            )
        }

        // Set Details
        composable(
            SetDetailsDestination.routeWithArgs,
            arguments =
                listOf(
                    navArgument(SetDetailsDestination.SET_ID_ARG) {
                        type = NavType.LongType
                    },
                ),
        ) {
            SetDetailsScreen(
                onComposing = { screenComposing(it) },
                navigateToStudy = { setId, isShuffled, onlyStarred ->
                    navController.navigate(
                        "${StudyDestination.route}/$setId" +
                            "?${StudyDestination.IS_SHUFFLED_ARG}=$isShuffled" +
                            "&${StudyDestination.ONLY_STARRED_ARG}=$onlyStarred",
                    )
                },
                navigateToSetEdit = { setId, editSpecific ->
                    navController.navigate(
                        "${EditSetDestination.route}?${EditSetDestination.SET_ID_ARG}" +
                            "=$setId&${EditSetDestination.EDIT_SPECIFIC_ARG}=$editSpecific",
                    )
                },
            )
        }

        // Study set
        composable(
            route = StudyDestination.routeWithArgs,
            arguments =
                listOf(
                    navArgument(StudyDestination.SET_ID_ARG) { type = NavType.LongType },
                    navArgument(StudyDestination.IS_SHUFFLED_ARG) {
                        type = NavType.BoolType
                        defaultValue = false
                    },
                    navArgument(StudyDestination.ONLY_STARRED_ARG) {
                        type = NavType.BoolType
                        defaultValue = false
                    },
                ),
        ) {
            StudyScreen(
                onComposing = { screenComposing(it) },
                navigateBack = {
                    navController.popBackStack()
                },
            )
        }

        composable(
            route = "oss",
        ) {
            LaunchedEffect(true) { screenComposing(BottomAppBarViewState()) }
            LibrariesContainer(
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
