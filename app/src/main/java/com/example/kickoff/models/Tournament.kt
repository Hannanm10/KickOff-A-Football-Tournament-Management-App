package com.example.kickoff.models

data class Tournament(
    var tournamentId: String = "",
    var name: String = "",
    var organizerId: String = "",
    var organizerName: String = "",
    var description: String = "",
    var status: String = "ONGOING", // ONGOING, COMPLETED
    var format: String = "LEAGUE", // LEAGUE, GROUP_KNOCKOUT
    var championTeamId: String = "",
    var completionDate: Long = 0,
    var createdAt: Long = System.currentTimeMillis()
)