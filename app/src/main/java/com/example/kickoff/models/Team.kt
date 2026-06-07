package com.example.kickoff.models

data class Team(
    var teamId: String = "",
    var tournamentId: String = "",
    var name: String = "",
    var logoUrl: String = "",
    var createdAt: Long = System.currentTimeMillis(),
    
    // Compatibility fields
    var logoUri: String = "", 
    var tournamentName: String = ""
) {
    // Auxiliary constructor for old logic if needed, or handle defaults
}