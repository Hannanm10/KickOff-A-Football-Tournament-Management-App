package com.example.kickoff.models

data class Team(
    var teamId: String = "",
    var tournamentId: String = "",
    var name: String = "",
    var groupName: String = "", // "A", "B" or empty for League
    var createdAt: Long = System.currentTimeMillis()
)