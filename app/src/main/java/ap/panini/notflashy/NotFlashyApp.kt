package ap.panini.notflashy

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import ap.panini.notflashy.ui.navigation.NotFlashyNavHost
import ap.panini.notflashy.ui.theme.NotFlashyTheme

@Immutable
data class BottomAppBarViewState(
    val actions: @Composable (RowScope.() -> Unit)? = null,
    val floatingActionButton: @Composable (() -> Unit)? = null
) {
    fun exists(): Boolean = !(actions == null && floatingActionButton == null)
}

@Composable
fun NotFlashyApp() {
    NotFlashyTheme {
        var bottomAppBarState by remember { mutableStateOf(BottomAppBarViewState()) }

        Scaffold(bottomBar = {
            if (bottomAppBarState.exists()) {
                BottomAppBar(
                    actions = bottomAppBarState.actions!!,
                    floatingActionButton = bottomAppBarState.floatingActionButton
                )
            }
        }) { padding ->
            Box(Modifier.padding(padding)) {
                NotFlashyNavHost({ bottomAppBarState = it })
            }
        }
    }
}
