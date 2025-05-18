import kotlinx.coroutines.*
import network.fetchPlayerDataTotals

fun main() = runBlocking {
    val stats = fetchPlayerDataTotals(2025, "BOS")
    stats.forEach {
        println("${it.playerName} (${it.team}) — ${it.points} очков за сезон")
    }
}
