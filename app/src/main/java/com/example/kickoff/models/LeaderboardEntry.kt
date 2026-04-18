package com.example.kickoff.models

data class LeaderboardEntry(
    val teamName: String,
    var matchesPlayed: Int = 0,
    var wins: Int = 0,
    var draws: Int = 0,
    var losses: Int = 0,
    var goalsFor: Int = 0,
    var goalsAgainst: Int = 0,
    var points: Int = 0
) {
    val goalDifference: Int
        get() = goalsFor - goalsAgainst
}