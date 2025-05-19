package network

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import model.PlayerDataTotals

suspend fun fetchPlayerDataTotals(
    season: Int,
    team: String
): List<PlayerDataTotals> {
    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }
    val url = "http://rest.nbaapi.com/api/PlayerDataTotals/query?season=$season&team=$team&sortBy=PlayerName&ascending=true&pageNumber=1&pageSize=35"
    val response: List<PlayerDataTotals> = client.get(url).body()
    client.close()
    return response
}

// Новый вариант: с поддержкой пагинации
suspend fun fetchPlayerDataByNamePaged(
    playerName: String,
    pageNumber: Int = 1,
    pageSize: Int = 6
): List<PlayerDataTotals> {
    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }
    return client.use { http ->
        http.get("http://rest.nbaapi.com/api/PlayerDataTotals/query") {
            parameter("playerName", playerName)
            parameter("sortBy", "PlayerName")
            parameter("ascending", true)
            parameter("pageNumber", pageNumber)
            parameter("pageSize", pageSize)
        }.body()
    }
}

suspend fun fetchPlayerDataByNameAndSeason(
    playerName: String,
    season: Int
): List<PlayerDataTotals> {
    val client = HttpClient(CIO) {
        install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
    }
    return client.use { http ->
        val response = http.get("http://rest.nbaapi.com/api/PlayerDataTotals/query") {
            parameter("playerName", playerName)
            parameter("season", season)
            parameter("pageSize", 30)
        }
        if (response.status.value == 404) return emptyList()
        val text = response.bodyAsText()
        if (text.trim().startsWith("{") && text.contains("status")) return emptyList()
        return response.body()
    }
}

suspend fun fetchSinglePlayerStatsByNameAndSeason(
    playerName: String,
    season: Int
): PlayerDataTotals? {
    return fetchPlayerDataByNameAndSeason(playerName, season).firstOrNull()
}

suspend fun fetchAllPlayersByName(playerName: String): List<PlayerDataTotals> {
    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }
    val pageSize = 30
    val result = mutableListOf<PlayerDataTotals>()
    var page = 1
    while (true) {
        val pageData: List<PlayerDataTotals> = client.get("http://rest.nbaapi.com/api/PlayerDataTotals/query") {
            parameter("playerName", playerName)
            parameter("sortBy", "PlayerName")
            parameter("ascending", true)
            parameter("pageNumber", page)
            parameter("pageSize", pageSize)
        }.body()
        if (pageData.isEmpty()) break
        result.addAll(pageData)
        if (pageData.size < pageSize) break
        page++
    }
    client.close()
    return result
}
