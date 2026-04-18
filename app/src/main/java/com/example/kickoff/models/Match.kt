package com.example.kickoff.models

data class Match(
    val teamA: String,
    val teamB: String,
    var scoreA: Int,
    var scoreB: Int,
    val tournamentName: String,
    val date: String = ""
)