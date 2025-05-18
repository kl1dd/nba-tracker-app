package network

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
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
