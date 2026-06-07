package com.example.kickoff.models

data class Match(
    var matchId: String = "",
    var tournamentId: String = "",
    var teamAId: String = "",
    var teamBId: String = "",
    var scoreA: Int = 0,
    var scoreB: Int = 0,
    var teamAName: String = "", // Helper field for UI
    var teamBName: String = "", // Helper field for UI
    var matchDate: String = "",
    var status: String = "UPCOMING", // UPCOMING, COMPLETED
    var createdAt: Long = System.currentTimeMillis(),
    
    // Compatibility fields
    var teamA: String = "",
    var teamB: String = "",
    var tournamentName: String = "",
    var date: String = ""
)