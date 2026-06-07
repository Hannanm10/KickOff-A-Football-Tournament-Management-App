package com.example.kickoff.models

data class Tournament(
    var tournamentId: String = "",
    var name: String = "",
    var organizerId: String = "",
    var organizerName: String = "",
    var description: String = "",
    var status: String = "LIVE",
    var format: String = "LEAGUE", // LEAGUE, GROUP_KNOCKOUT
    var championTeamId: String = "",
    var createdAt: Long = System.currentTimeMillis()
)