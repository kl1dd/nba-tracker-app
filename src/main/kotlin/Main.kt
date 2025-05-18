import kotlinx.coroutines.*
import network.fetchPlayerDataTotals
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.foundation.layout.*
import model.PlayerDataTotals

enum class Screen {
    MAIN_MENU, PLAYER_STATS, PLAYER_COMPARE
}

@Composable
fun MainMenu(onNavigate: (Screen) -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "NBA Tracker",
                style = MaterialTheme.typography.h4,
            )
        }
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Button(
                modifier = Modifier.width(220.dp),
                onClick = { onNavigate(Screen.PLAYER_STATS) }
            ) {
                Text("Статистика игроков")
            }
            Spacer(Modifier.height(16.dp))
            Button(
                modifier = Modifier.width(220.dp),
                onClick = { onNavigate(Screen.PLAYER_COMPARE) }
            ) {
                Text("Сравнение игроков")
            }
        }
    }
}


@Composable
fun PlayerStatsScreen(onBack: () -> Unit) {
    val seasons = (2024 downTo 1990).map { it.toString() }
    var season by remember { mutableStateOf(seasons.first()) }
    var seasonExpanded by remember { mutableStateOf(false) }

    val teams = listOf(
        "ATL" to "Atlanta Hawks",
        "BOS" to "Boston Celtics",
        "BRK" to "Brooklyn Nets",
        "CHI" to "Chicago Bulls",
        "CHO" to "Charlotte Hornets",
        "CLE" to "Cleveland Cavaliers",
        "DAL" to "Dallas Mavericks",
        "DEN" to "Denver Nuggets",
        "DET" to "Detroit Pistons",
        "GSW" to "Golden State Warriors",
        "HOU" to "Houston Rockets",
        "IND" to "Indiana Pacers",
        "LAC" to "Los Angeles Clippers",
        "LAL" to "Los Angeles Lakers",
        "MEM" to "Memphis Grizzlies",
        "MIA" to "Miami Heat",
        "MIL" to "Milwaukee Bucks",
        "MIN" to "Minnesota Timberwolves",
        "NOP" to "New Orleans Pelicans",
        "NYK" to "New York Knicks",
        "OKC" to "Oklahoma City Thunder",
        "ORL" to "Orlando Magic",
        "PHI" to "Philadelphia 76ers",
        "PHO" to "Phoenix Suns",
        "POR" to "Portland Trail Blazers",
        "SAC" to "Sacramento Kings",
        "SAS" to "San Antonio Spurs",
        "TOR" to "Toronto Raptors",
        "UTA" to "Utah Jazz",
        "WAS" to "Washington Wizards"
    )
    var team by remember { mutableStateOf(teams.first().first) }
    var teamExpanded by remember { mutableStateOf(false) }

    var players by remember { mutableStateOf<List<PlayerDataTotals>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val playerNames = players.map { it.playerName }.distinct()
    var selectedPlayer by remember { mutableStateOf<String?>(null) }
    var playerExpanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(32.dp)) {
        Button(onClick = onBack) {
            Text("← В главное меню")
        }
        Spacer(Modifier.height(16.dp))

        // Сезон + команда + Загрузить
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Dropdown для сезона
            Box {
                Button(onClick = { seasonExpanded = true }) {
                    Text("Сезон: $season")
                }
                DropdownMenu(
                    expanded = seasonExpanded,
                    onDismissRequest = { seasonExpanded = false }
                ) {
                    seasons.forEach { s ->
                        DropdownMenuItem(onClick = {
                            season = s
                            seasonExpanded = false
                        }) {
                            Text(s)
                        }
                    }
                }
            }
            Spacer(Modifier.width(8.dp))

            // Dropdown для команды (с кратким и полным названием)
            Box {
                Button(onClick = { teamExpanded = true }) {
                    val selected = teams.find { it.first == team }
                    Text("Команда: ${selected?.first} (${selected?.second})")
                }
                DropdownMenu(
                    expanded = teamExpanded,
                    onDismissRequest = { teamExpanded = false }
                ) {
                    teams.forEach { (abbr, full) ->
                        DropdownMenuItem(onClick = {
                            team = abbr
                            teamExpanded = false
                        }) {
                            Text("$abbr ($full)")
                        }
                    }
                }
            }
            Spacer(Modifier.width(8.dp))

            Button(
                onClick = {
                    isLoading = true
                    error = null
                    players = emptyList()
                    selectedPlayer = null
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val result = fetchPlayerDataTotals(season.toInt(), team)
                            players = result
                            isLoading = false
                        } catch (e: Exception) {
                            error = e.message
                            isLoading = false
                        }
                    }
                }
            ) { Text("Загрузить") }
        }

        Spacer(Modifier.height(16.dp))

        // Dropdown для выбора игрока (после загрузки)
        if (players.isNotEmpty()) {
            Box {
                Button(onClick = { playerExpanded = true }) {
                    Text("Игрок: ${selectedPlayer ?: "Выбери игрока"}")
                }
                DropdownMenu(
                    expanded = playerExpanded,
                    onDismissRequest = { playerExpanded = false }
                ) {
                    playerNames.forEach { name ->
                        DropdownMenuItem(onClick = {
                            selectedPlayer = name
                            playerExpanded = false
                        }) {
                            Text(name)
                        }
                    }
                }
            }
            if (selectedPlayer != null) {
                Spacer(Modifier.height(8.dp))
                Button(onClick = { selectedPlayer = null }) {
                    Text("Показать всех игроков")
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        if (isLoading) {
            Text("Загрузка...")
        }
        error?.let {
            Text("Ошибка: $it", color = MaterialTheme.colors.error)
        }

        // Показываем либо выбранного игрока (подробно), либо всех игроков команды/сезона (списком)
        val shownPlayers = if (selectedPlayer != null) {
            players.filter { it.playerName == selectedPlayer }
        } else {
            players
        }
        Column {
            if (selectedPlayer != null && shownPlayers.isNotEmpty()) {
                val player = shownPlayers.first()
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    elevation = 6.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("${player.playerName} (${player.team})", style = MaterialTheme.typography.h6)
                        Spacer(Modifier.height(4.dp))
                        Text("Позиция: ${player.position}")
                        Text("Возраст: ${player.age}")
                        Spacer(Modifier.height(8.dp))
                        Text("Матчей: ${player.games}")
                        Text("Очков: ${player.points}")
                        Text("Ассистов: ${player.assists}")
                        Text("Подборов: ${player.totalRb}")
                        Text("Блоки: ${player.blocks}")
                        Text("Перехваты: ${player.steals}")
                        Text("Трёхочковые: ${player.threeFg} (${player.threePercent?.times(100)?.let { String.format("%.1f", it) } ?: "?"}%)")
                        Text("Двухочковые: ${player.twoFg} (${player.twoPercent?.times(100)?.let { String.format("%.1f", it) } ?: "?"}%)")
                        Text("Процент с игры: ${player.fieldPercent?.times(100)?.let { String.format("%.1f", it) } ?: "?"}%")
                        Text("Штрафные: ${player.ft} (${player.ftPercent?.times(100)?.let { String.format("%.1f", it) } ?: "?"}%)")
                        Text("Потери: ${player.turnovers}")
                        Text("Фолы: ${player.personalFouls}")
                    }
                }
            } else {
                shownPlayers.forEach {
                    Text("${it.playerName} (${it.team}) — Очки: ${it.points}, Ассисты: ${it.assists}, Подборы: ${it.totalRb}")
                }
            }
        }
    }
}


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
