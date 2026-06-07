package com.example.kickoff.models

data class Match(
    var matchId: String = "",
    var tournamentId: String = "",
    var teamAId: String = "",
    var teamBId: String = "",
    var scoreA: Int = -1,
    var scoreB: Int = -1,
    var teamAName: String = "", 
    var teamBName: String = "", 
    var matchDate: String = "",
    var status: String = "UPCOMING", // UPCOMING, COMPLETED
    var stage: String = "", // LEAGUE, GROUP, SEMI_FINAL, FINAL
    var createdAt: Long = System.currentTimeMillis()
)