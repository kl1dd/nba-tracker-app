import kotlinx.coroutines.*
import network.fetchPlayerDataTotals
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

enum class Screen {
    MAIN_MENU, PLAYER_STATS, PLAYER_COMPARE
}

@Composable
fun MainMenu(onNavigate: (Screen) -> Unit) {
    Column(
        modifier = Modifier.padding(32.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Выберите действие", style = MaterialTheme.typography.h5)
        Spacer(Modifier.height(16.dp))
        Button(onClick = { onNavigate(Screen.PLAYER_STATS) }) {
            Text("Статистика игроков")
        }
        Spacer(Modifier.height(8.dp))
        Button(onClick = { onNavigate(Screen.PLAYER_COMPARE) }) {
            Text("Сравнение игроков")
        }
    }
}

// Заглушка для PlayerStatsScreen с кнопкой назад
@Composable
fun PlayerStatsScreen(onBack: () -> Unit) {
    Column(modifier = Modifier.padding(32.dp)) {
        Button(onClick = onBack) {
            Text("← В главное меню")
        }
        Spacer(Modifier.height(16.dp))
        Text("Экран статистики игроков в разработке")
    }
}

// Заглушка для PlayerCompareScreen с кнопкой назад
@Composable
fun PlayerCompareScreen(onBack: () -> Unit) {
    Column(modifier = Modifier.padding(32.dp)) {
        Button(onClick = onBack) {
            Text("← В главное меню")
        }
        Spacer(Modifier.height(16.dp))
        Text("Функция сравнения игроков появится позже!")
    }
}

@Composable
fun AppRoot() {
    var screen by remember { mutableStateOf(Screen.MAIN_MENU) }

    when (screen) {
        Screen.MAIN_MENU -> MainMenu { screen = it }
        Screen.PLAYER_STATS -> PlayerStatsScreen(onBack = { screen = Screen.MAIN_MENU })
        Screen.PLAYER_COMPARE -> PlayerCompareScreen(onBack = { screen = Screen.MAIN_MENU })
    }
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "NBA Stats App") {
        MaterialTheme {
            AppRoot()
        }
    }
}
