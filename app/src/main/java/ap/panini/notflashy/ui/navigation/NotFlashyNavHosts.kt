package ap.panini.notflashy.ui.navigation

import androidx.compose.runtime.Composable
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

@Composable
fun NotFlashyNavHost(
    screenComposing: (BottomAppBarViewState) -> Unit,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController,
        startDestination = LibraryDestination.route
    ) {
        // Library
        composable(LibraryDestination.routeWithArgs) {
            LibraryScreen(
                onComposing = { screenComposing(it) },
                navigateToSetStudy = {
                    navController.navigate(
                        "${StudyDestination.route}/$it"
                    )
                },
                navigateToSetEdit = {
                    navController.navigate(
                        "${EditSetDestination.route}?${EditSetDestination.setIdArg}=$it"
                    )
                },
                navigateToSetEntry = {
                    navController.navigate(
                        "${SetDetailsDestination.route}/$it"
                    )
                }
            )
        }

        // Edit Set
        composable(
            EditSetDestination.routeWithArgs,
            arguments = listOf(
                navArgument(EditSetDestination.setIdArg) {
                    type = NavType.LongType
                    defaultValue = -1
                },
                navArgument(EditSetDestination.editSpecificArg) {
                    type = NavType.IntType
                    defaultValue = -1
                }
            )
        ) {
            EditSetScreen(
                onComposing = { screenComposing(it) },
                navigateBack = { navController.popBackStack() }
            )
        }

        // Set Details
        composable(
            SetDetailsDestination.routeWithArgs,
            arguments = listOf(
                navArgument(SetDetailsDestination.setIdArg) {
                    type = NavType.LongType
                }
            )
        ) {
            SetDetailsScreen(
                onComposing = { screenComposing(it) },
                navigateToStudy = { setId, isShuffled, onlyStared ->
                    navController.navigate(
                        "${StudyDestination.route}/$setId" +
                            "?${StudyDestination.isShuffledArg}=$isShuffled" +
                            "&${StudyDestination.onlyStaredArg}=$onlyStared"
                    )
                },
                navigateToSetEdit = { setId, editSpecific ->
                    navController.navigate(
                        "${EditSetDestination.route}?${EditSetDestination.setIdArg}" +
                            "=$setId&${EditSetDestination.editSpecificArg}=$editSpecific"
                    )
                }
            )
        }

        // Study set
        composable(
            route = StudyDestination.routeWithArgs,
            arguments = listOf(
                navArgument(StudyDestination.setIdArg) { type = NavType.LongType },
                navArgument(StudyDestination.isShuffledArg) {
                    type = NavType.BoolType
                    defaultValue = false
                },
                navArgument(StudyDestination.onlyStaredArg) {
                    type = NavType.BoolType
                    defaultValue = false
                }
            )
        ) {
            StudyScreen(
                onComposing = { screenComposing(it) },
                navigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
