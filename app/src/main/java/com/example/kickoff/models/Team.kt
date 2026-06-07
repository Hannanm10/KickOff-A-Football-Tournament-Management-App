package com.example.kickoff.models

data class Team(
    var teamId: String = "",
    var tournamentId: String = "",
    var name: String = "",
    var createdAt: Long = System.currentTimeMillis()
)