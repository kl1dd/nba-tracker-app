package model

import kotlinx.serialization.Serializable

@Serializable
data class PlayerDataTotals(
    val id: Int,
    val playerName: String,
    val position: String?,
    val age: Int?,
    val games: Int?,
    val gamesStarted: Int?,
    val minutesPg: Double?,
    val fieldGoals: Int?,
    val fieldAttempts: Int?,
    val fieldPercent: Double?,
    val threeFg: Int?,
    val threeAttempts: Int?,
    val threePercent: Double?,
    val twoFg: Int?,
    val twoAttempts: Int?,
    val twoPercent: Double?,
    val effectFgPercent: Double?,
    val ft: Int?,
    val ftAttempts: Int?,
    val ftPercent: Double?,
    val offensiveRb: Int?,
    val defensiveRb: Int?,
    val totalRb: Int?,
    val assists: Int?,
    val steals: Int?,
    val blocks: Int?,
    val turnovers: Int?,
    val personalFouls: Int?,
    val points: Int?,
    val team: String?,
    val season: Int?,
    val playerId: String?
)


