import kotlinx.coroutines.*
import network.fetchPlayerDataTotals
import network.fetchPlayerDataByNamePaged
import network.fetchPlayerDataByNameAndSeason
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.foundation.layout.*
import model.PlayerDataTotals
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState

enum class Screen {
    MAIN_MENU,
    PLAYER_STATS,      // По сезонам/командам
    PLAYER_SEARCH,     // Поиск по имени
    PLAYER_COMPARE
}

@Composable
fun MainMenu(onNavigate: (Screen) -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
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
                Text("Статистика по сезонам")
            }
            Spacer(Modifier.height(16.dp))
            Button(
                modifier = Modifier.width(220.dp),
                onClick = { onNavigate(Screen.PLAYER_SEARCH) }
            ) {
                Text("Статистика по имени")
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

        Row(verticalAlignment = Alignment.CenterVertically) {
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
fun PlayerSearchScreen(onBack: () -> Unit) {
    var searchQuery by remember { mutableStateOf("") }
    var players by remember { mutableStateOf<List<PlayerDataTotals>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var selectedPlayer by remember { mutableStateOf<PlayerDataTotals?>(null) }

    var currentPage by remember { mutableStateOf(1) }
    val pageSize = 6

    // Для диапазона сезонов
    val seasons = (2024 downTo 1990).toList()
    var fromSeason by remember { mutableStateOf(seasons.last()) }
    var toSeason by remember { mutableStateOf(seasons.first()) }
    var isRangeLoading by remember { mutableStateOf(false) }
    var wasShowClicked by remember { mutableStateOf(false) }

    // Для таблицы: хранит полный map "год -> данные", чтобы не терять даже пустые года
    var seasonDataByYearGlobal by remember { mutableStateOf<Map<Int, PlayerDataTotals?>>(emptyMap()) }

    fun loadPlayers() {
        isLoading = true
        error = null
        players = emptyList()
        selectedPlayer = null
        CoroutineScope(Dispatchers.IO).launch {
            try {
                players = network.fetchPlayerDataByNamePaged(searchQuery, currentPage, pageSize)
                isLoading = false
            } catch (e: Exception) {
                error = e.message
                isLoading = false
            }
        }
    }

    // ===== ТАБЛИЦА ПОЛНОЙ СТАТИСТИКИ =====
    val headers = listOf(
        "Сезон", "Команда", "Поз", "Возраст", "Матчи", "Старт", "Мин", "Очки",
        "ФГ", "ФГА", "% ФГ", "3П", "3ПА", "% 3П", "2П", "2ПА", "% 2П", "ЭФФ %",
        "Штр", "ШтрА", "% Штр", "ОРБ", "ДРБ", "Подб", "Асс", "ПХВ", "Блок", "Пот", "Фолы"
    )

    fun PlayerDataTotals.toTableRow(): List<String> = listOf(
        season?.toString() ?: "–",
        team ?: "–",
        position ?: "–",
        age?.toString() ?: "–",
        games?.toString() ?: "–",
        gamesStarted?.toString() ?: "–",
        minutesPg?.let { String.format("%.1f", it) } ?: "–",
        points?.toString() ?: "–",
        fieldGoals?.toString() ?: "–",
        fieldAttempts?.toString() ?: "–",
        fieldPercent?.let { String.format("%.1f", it * 100) } ?: "–",
        threeFg?.toString() ?: "–",
        threeAttempts?.toString() ?: "–",
        threePercent?.let { String.format("%.1f", it * 100) } ?: "–",
        twoFg?.toString() ?: "–",
        twoAttempts?.toString() ?: "–",
        twoPercent?.let { String.format("%.1f", it * 100) } ?: "–",
        effectFgPercent?.let { String.format("%.1f", it * 100) } ?: "–",
        ft?.toString() ?: "–",
        ftAttempts?.toString() ?: "–",
        ftPercent?.let { String.format("%.1f", it * 100) } ?: "–",
        offensiveRb?.toString() ?: "–",
        defensiveRb?.toString() ?: "–",
        totalRb?.toString() ?: "–",
        assists?.toString() ?: "–",
        steals?.toString() ?: "–",
        blocks?.toString() ?: "–",
        turnovers?.toString() ?: "–",
        personalFouls?.toString() ?: "–"
    )

    Column(modifier = Modifier.padding(32.dp)) {
        Button(onClick = onBack) { Text("← В главное меню") }
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Имя игрока (например, James)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))

        Button(
            onClick = {
                currentPage = 1
                loadPlayers()
            }
        ) { Text("Найти") }

        Spacer(Modifier.height(8.dp))
        if (isLoading) Text("Загрузка...")
        error?.let { Text("Ошибка: $it", color = MaterialTheme.colors.error) }

        // Пагинация + выбор игрока
        if (players.isNotEmpty() && selectedPlayer == null) {
            Text("Показано: ${players.size} игроков, страница $currentPage")
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    enabled = currentPage > 1,
                    onClick = {
                        currentPage--
                        loadPlayers()
                    }) {
                    Text("Назад")
                }
                Spacer(Modifier.width(16.dp))
                Text("Страница $currentPage")
                Spacer(Modifier.width(16.dp))
                Button(
                    enabled = players.size == pageSize,
                    onClick = {
                        currentPage++
                        loadPlayers()
                    }) {
                    Text("Вперёд")
                }
            }
            Spacer(Modifier.height(8.dp))
            Column {
                players.forEach { player ->
                    Button(
                        onClick = {
                            selectedPlayer = player
                            wasShowClicked = false
                            seasonDataByYearGlobal = emptyMap()
                            fromSeason = seasons.last()
                            toSeason = seasons.first()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.primary)
                    ) {
                        Text("${player.playerName} (${player.team}, ${player.season})", color = MaterialTheme.colors.onPrimary)
                    }
                }
            }
        }

        // --- После выбора игрока ---
        selectedPlayer?.let { player ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                elevation = 6.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("${player.playerName}", style = MaterialTheme.typography.h6)
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Сезон с:")
                        Spacer(Modifier.width(4.dp))
                        DropdownMenuBox(
                            options = seasons,
                            selected = fromSeason,
                            onSelect = { fromSeason = it }
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("по")
                        Spacer(Modifier.width(4.dp))
                        DropdownMenuBox(
                            options = seasons.filter { it >= fromSeason },
                            selected = toSeason,
                            onSelect = { toSeason = it }
                        )
                        Spacer(Modifier.width(8.dp))
                        Button(
                            onClick = {
                                isRangeLoading = true
                                wasShowClicked = true
                                seasonDataByYearGlobal = emptyMap()
                                CoroutineScope(Dispatchers.IO).launch {
                                    try {
                                        val years = (fromSeason..toSeason).toList()
                                        val deferreds = years.map { year ->
                                            async {
                                                try {
                                                    val data = network.fetchPlayerDataByNameAndSeason(player.playerName, year)
                                                    year to data.firstOrNull()
                                                } catch (_: Exception) {
                                                    year to null
                                                }
                                            }
                                        }
                                        val results = deferreds.awaitAll().toMap()
                                        seasonDataByYearGlobal = results
                                        isRangeLoading = false
                                    } catch (e: Exception) {
                                        isRangeLoading = false
                                    }
                                }
                            }
                        ) { Text("Показать") }
                    }
                    Spacer(Modifier.height(12.dp))

                    if (isRangeLoading) {
                        Text("Загрузка статистики по сезонам…")
                    }

                    if (wasShowClicked) {
                        val yearRange = (fromSeason..toSeason).toList()
                        val seasonDataByYear = yearRange.map { year ->
                            seasonDataByYearGlobal[year]
                        }
                        Box(
                            Modifier
                                .horizontalScroll(rememberScrollState())
                                .fillMaxWidth()
                                .heightIn(min = 200.dp, max = 400.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Column {
                                // Header row
                                Row {
                                    headers.forEach { header ->
                                        Text(header, Modifier.width(90.dp))
                                    }
                                }
                                Divider()
                                // Data rows
                                yearRange.forEachIndexed { idx, year ->
                                    val s = seasonDataByYear[idx]
                                    val row = s?.toTableRow() ?: List(headers.size) { "–" }
                                    Row {
                                        row.forEach { value ->
                                            Text(value, Modifier.width(90.dp))
                                        }
                                    }
                                }
                            }
                        }
                        if (!isRangeLoading && seasonDataByYear.all { it == null }) {
                            Spacer(Modifier.height(8.dp))
                            Text("Нет статистики за выбранные годы", color = MaterialTheme.colors.error)
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Button(onClick = {
                selectedPlayer = null
                seasonDataByYearGlobal = emptyMap()
                wasShowClicked = false
            }) { Text("Назад к списку") }
        }
    }
}

@Composable
fun DropdownMenuBox(options: List<Int>, selected: Int, onSelect: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Button(onClick = { expanded = true }) {
            Text(selected.toString())
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { value ->
                DropdownMenuItem(onClick = {
                    onSelect(value)
                    expanded = false
                }) {
                    Text(value.toString())
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
        Screen.PLAYER_SEARCH -> PlayerSearchScreen(onBack = { screen = Screen.MAIN_MENU })
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
