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
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp


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
    var seasonDataByYearGlobal by remember { mutableStateOf<Map<Int, PlayerDataTotals?>>(emptyMap()) }

    fun loadPlayers() {
        isLoading = true
        error = null
        players = emptyList()
        selectedPlayer = null
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val fetched = network.fetchPlayerDataByNamePaged(searchQuery, currentPage, pageSize)
                if (fetched.isEmpty()) {
                    error = "Игрок не найден. Попробуйте другое имя или другую раскладку."
                } else {
                    players = fetched
                }
                isLoading = false
            } catch (e: Exception) {
                // Проверяем на 404 или Not Found
                val message = e.message ?: ""
                error = if ("404" in message || "Not Found" in message) {
                    "Игрок не найден. Попробуйте другое имя или другую раскладку."
                } else {
                    "Ошибка при запросе: ${message.lines().firstOrNull() ?: "Неизвестная ошибка"}"
                }
                isLoading = false
            }
        }
    }


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
        if (isLoading) {
            Text("Загрузка...")
        }
        if (error != null) {
            Text(error!!, color = MaterialTheme.colors.error)
            return@Column
        }

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

                        val horizontalState = rememberScrollState()
                        val verticalState = rememberScrollState()

                        Column(
                            Modifier
                                .fillMaxWidth()
                                .heightIn(min = 200.dp, max = 400.dp)
                        ) {
                            // Фиксированный заголовок (только по горизонтали)
                            Row(
                                Modifier.horizontalScroll(horizontalState)
                            ) {
                                headers.forEach { header ->
                                    Text(
                                        header,
                                        Modifier.width(90.dp).padding(vertical = 4.dp),
                                        style = MaterialTheme.typography.subtitle2
                                    )
                                }
                            }
                            Divider()
                            // Скроллимые строки (по горизонтали и вертикали)
                            Box(
                                Modifier
                                    .weight(1f)
                                    .horizontalScroll(horizontalState)
                                    .verticalScroll(verticalState)
                            ) {
                                Column {
                                    yearRange.forEachIndexed { idx, year ->
                                        val s = seasonDataByYear[idx]
                                        val row = s?.toTableRow() ?: List(headers.size) { "–" }
                                        Row {
                                            row.forEach { value ->
                                                Text(value, Modifier.width(90.dp).padding(vertical = 2.dp))
                                            }
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
    // --- State ---
    var player1Query by remember { mutableStateOf("") }
    var player2Query by remember { mutableStateOf("") }
    var player1Results by remember { mutableStateOf<List<PlayerDataTotals>>(emptyList()) }
    var player2Results by remember { mutableStateOf<List<PlayerDataTotals>>(emptyList()) }
    var selectedPlayer1 by remember { mutableStateOf<PlayerDataTotals?>(null) }
    var selectedPlayer2 by remember { mutableStateOf<PlayerDataTotals?>(null) }
    var season by remember { mutableStateOf(2024) }
    val seasons = (2024 downTo 1990).toList()
    var isLoading1 by remember { mutableStateOf(false) }
    var isLoading2 by remember { mutableStateOf(false) }
    var error1 by remember { mutableStateOf<String?>(null) }
    var error2 by remember { mutableStateOf<String?>(null) }

    val allStats = listOf(
        "points" to "Очки",
        "assists" to "Ассисты",
        "totalRb" to "Подборы",
        "games" to "Матчи",
        "minutesPg" to "Минуты",
        "fieldPercent" to "FG%",
        "threePercent" to "3PT%",
        "blocks" to "Блоки",
        "steals" to "Перехваты",
        "turnovers" to "Потери",
        "personalFouls" to "Фолы",
    )
    var selectedStats by remember { mutableStateOf(allStats.map { it.first }.toSet()) }

    var compareClicked by remember { mutableStateOf(false) }
    var compareStats1 by remember { mutableStateOf<PlayerDataTotals?>(null) }
    var compareStats2 by remember { mutableStateOf<PlayerDataTotals?>(null) }
    var compareError by remember { mutableStateOf<String?>(null) }
    var isCompareLoading by remember { mutableStateOf(false) } // <<==== добавили
    val scrollState = rememberScrollState()

    // --- UI ---
    Column(
        Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .verticalScroll(scrollState)
    ) {
        Button(onClick = onBack) { Text("← В главное меню") }
        Spacer(Modifier.height(16.dp))

        // Игрок 1
        Text("Игрок 1")
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = player1Query,
                onValueChange = { player1Query = it },
                label = { Text("Имя игрока") },
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            Button(
                onClick = {
                    isLoading1 = true
                    error1 = null
                    player1Results = emptyList()
                    selectedPlayer1 = null
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val res = network.fetchPlayerDataByNamePaged(player1Query, pageSize = 100)
                            player1Results = res
                        } catch (e: Exception) {
                            error1 = "Ошибка: ${e.message}"
                        }
                        isLoading1 = false
                    }
                }
            ) { Text("Найти") }
        }
        if (isLoading1) Text("Загрузка...", fontSize = 16.sp)
        error1?.let { Text(it, color = MaterialTheme.colors.error) }
        if (player1Results.isNotEmpty()) {
            DropdownMenuPlayerSelectorSimple(
                players = player1Results,
                selectedPlayer = selectedPlayer1,
                onPlayerSelected = { selectedPlayer1 = it }
            )
        }

        Spacer(Modifier.height(24.dp))

        // Игрок 2
        Text("Игрок 2")
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = player2Query,
                onValueChange = { player2Query = it },
                label = { Text("Имя игрока") },
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            Button(
                onClick = {
                    isLoading2 = true
                    error2 = null
                    player2Results = emptyList()
                    selectedPlayer2 = null
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val res = network.fetchPlayerDataByNamePaged(player2Query, pageSize = 100)
                            player2Results = res
                        } catch (e: Exception) {
                            error2 = "Ошибка: ${e.message}"
                        }
                        isLoading2 = false
                    }
                }
            ) { Text("Найти") }
        }
        if (isLoading2) Text("Загрузка...", fontSize = 16.sp)
        error2?.let { Text(it, color = MaterialTheme.colors.error) }
        if (player2Results.isNotEmpty()) {
            DropdownMenuPlayerSelectorSimple(
                players = player2Results,
                selectedPlayer = selectedPlayer2,
                onPlayerSelected = { selectedPlayer2 = it }
            )
        }

        Spacer(Modifier.height(24.dp))

        // Сезон
        Text("Сезон:", style = MaterialTheme.typography.body1)
        Row(verticalAlignment = Alignment.CenterVertically) {
            DropdownMenuSeasonPicker(
                seasons = seasons,
                selectedSeason = season,
                onSeasonSelected = { season = it }
            )
        }
        Spacer(Modifier.height(16.dp))

        // Выбор показателей
        Text("Выберите показатели для сравнения:", style = MaterialTheme.typography.body1)
        Column {
            allStats.forEach { (key, label) ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = selectedStats.contains(key),
                        onCheckedChange = {
                            selectedStats = if (it) selectedStats + key else selectedStats - key
                        }
                    )
                    Text(label)
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Button(
            enabled = selectedPlayer1 != null && selectedPlayer2 != null && selectedStats.isNotEmpty(),
            onClick = {
                compareClicked = true
                compareError = null
                compareStats1 = null
                compareStats2 = null
                isCompareLoading = true // <<=== теперь ставим загрузку
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val data1 = network.fetchPlayerDataByNameAndSeason(selectedPlayer1?.playerName ?: "", season)
                        val data2 = network.fetchPlayerDataByNameAndSeason(selectedPlayer2?.playerName ?: "", season)
                        compareStats1 = data1.firstOrNull()
                        compareStats2 = data2.firstOrNull()
                    } catch (e: Exception) {
                        compareError = "Ошибка загрузки: ${e.message}"
                    }
                    isCompareLoading = false // <<=== по завершении выключаем загрузку
                }
            }
        ) { Text("Сравнить") }

        Spacer(Modifier.height(16.dp))
        // Сравнительная таблица
        if (compareClicked) {
            when {
                isCompareLoading -> {
                    Text("Загрузка...", fontSize = 16.sp)
                }
                compareError != null -> {
                    Text(compareError!!, color = MaterialTheme.colors.error)
                }
                (compareStats1 != null || compareStats2 != null) -> {
                    Surface(
                        color = MaterialTheme.colors.primary.copy(alpha = 0.05f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(8.dp)) {
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Показатель", modifier = Modifier.weight(1f), fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                                Text(selectedPlayer1?.playerName ?: "Игрок 1", modifier = Modifier.weight(1f), fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                                Text(selectedPlayer2?.playerName ?: "Игрок 2", modifier = Modifier.weight(1f), fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                            }
                            selectedStats.forEach { statKey ->
                                val label = allStats.find { it.first == statKey }?.second ?: statKey
                                val v1 = compareStats1?.let { getStatValue(it, statKey) }
                                val v2 = compareStats2?.let { getStatValue(it, statKey) }
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(label, modifier = Modifier.weight(1f))
                                    Text(
                                        formatStatValue(v1),
                                        modifier = Modifier.weight(1f),
                                        color = colorCompare(v1, v2)
                                    )
                                    Text(
                                        formatStatValue(v2),
                                        modifier = Modifier.weight(1f),
                                        color = colorCompare(v2, v1)
                                    )
                                }
                            }
                        }
                    }
                }
                else -> {
                    Text("Нет данных для выбранных игроков/сезона.")
                }
            }
        }
    }
}


@Composable
fun DropdownMenuPlayerSelectorSimple(
    players: List<PlayerDataTotals>,
    selectedPlayer: PlayerDataTotals?,
    onPlayerSelected: (PlayerDataTotals) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val uniquePlayers = players.distinctBy { it.playerName }
    Box {
        Button(onClick = { expanded = true }) {
            Text(selectedPlayer?.let { "${it.playerName}" } ?: "Выбери игрока")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.width(350.dp).heightIn(max = 400.dp)
        ) {
            uniquePlayers.forEach { player ->
                DropdownMenuItem(onClick = {
                    onPlayerSelected(player)
                    expanded = false
                }) {
                    Text(player.playerName)
                }
            }
        }
    }
}


// Выпадающий список выбора сезона (можно и просто список, если нравится)
@Composable
fun DropdownMenuSeasonPicker(
    seasons: List<Int>,
    selectedSeason: Int,
    onSeasonSelected: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Button(onClick = { expanded = true }) {
            Text(selectedSeason.toString())
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.width(120.dp).heightIn(max = 350.dp)
        ) {
            seasons.forEach { s ->
                DropdownMenuItem(onClick = {
                    onSeasonSelected(s)
                    expanded = false
                }) {
                    Text(s.toString())
                }
            }
        }
    }
}

fun getStatValue(player: PlayerDataTotals, key: String): Double? = when (key) {
    "points" -> player.points?.toDouble()
    "assists" -> player.assists?.toDouble()
    "totalRb" -> player.totalRb?.toDouble()
    "games" -> player.games?.toDouble()
    "minutesPg" -> player.minutesPg?.toDouble()
    "fieldPercent" -> player.fieldPercent?.times(100)
    "threePercent" -> player.threePercent?.times(100)
    "blocks" -> player.blocks?.toDouble()
    "steals" -> player.steals?.toDouble()
    "turnovers" -> player.turnovers?.toDouble()
    "personalFouls" -> player.personalFouls?.toDouble()
    else -> null
}

fun formatStatValue(value: Double?, isPercent: Boolean = false): String {
    return when {
        value == null -> "–"
        isPercent -> String.format("%.1f%%", value)
        value % 1 == 0.0 -> value.toInt().toString()
        else -> String.format("%.1f", value)
    }
}


// Цвет для сравнения (зелёный — лучше, красный — хуже)
@Composable
fun colorCompare(value: Double?, other: Double?, reverse: Boolean = false): Color {
    return when {
        value == null || other == null -> Color.Unspecified
        value == other -> Color.Unspecified
        (reverse && value < other) || (!reverse && value > other) -> Color(0xFF13D869)
        else -> Color(0xFFFF2851)
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
