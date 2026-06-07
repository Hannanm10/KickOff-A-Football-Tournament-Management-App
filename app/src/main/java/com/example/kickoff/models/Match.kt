package com.example.kickoff.models

data class Match(
    var matchId: String = "",
    var tournamentId: String = "",
    var teamAId: String = "",
    var teamBId: String = "",
    var scoreA: Int = 0,
    var scoreB: Int = 0,
    var teamAName: String = "", 
    var teamBName: String = "", 
    var matchDate: String = "",
    var status: String = "UPCOMING",
    var createdAt: Long = System.currentTimeMillis()
)