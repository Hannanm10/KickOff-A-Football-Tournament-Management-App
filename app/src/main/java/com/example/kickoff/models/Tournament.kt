package com.example.kickoff.models

data class Tournament(
    var tournamentId: String = "",
    var name: String = "",
    var organizerId: String = "",
    var organizerName: String = "",
    var description: String = "",
    var status: String = "LIVE",
    var createdAt: Long = System.currentTimeMillis()
)