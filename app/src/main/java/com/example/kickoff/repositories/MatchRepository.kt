package com.example.kickoff.repositories

import com.example.kickoff.models.Match
import com.example.kickoff.models.Team
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

object MatchRepository {
    private val database = FirebaseDatabase.getInstance().getReference("matches")

    fun addMatch(match: Match, onResult: (Boolean, String?) -> Unit) {
        val matchId = database.push().key ?: return onResult(false, "Could not generate ID")
        match.matchId = matchId
        
        // Auto-status logic
        if (match.scoreA >= 0 && match.scoreB >= 0) {
            match.status = "COMPLETED"
        } else {
            match.status = "UPCOMING"
        }
        
        database.child(matchId).setValue(match)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, null)
                } else {
                    onResult(false, task.exception?.message)
                }
            }
    }

    fun getMatchesByTournament(tournamentId: String, onDataChange: (List<Match>) -> Unit): ValueEventListener {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<Match>()
                for (child in snapshot.children) {
                    val match = child.getValue(Match::class.java)
                    if (match != null) {
                        list.add(match)
                    }
                }
                onDataChange(list)
            }

            override fun onCancelled(error: DatabaseError) {}
        }
        database.orderByChild("tournamentId").equalTo(tournamentId)
            .addValueEventListener(listener)
        return listener
    }

    fun removeListener(listener: ValueEventListener) {
        database.removeEventListener(listener)
    }

    fun updateMatch(match: Match, onResult: (Boolean, String?) -> Unit) {
        // Auto-status logic
        if (match.scoreA >= 0 && match.scoreB >= 0) {
            match.status = "COMPLETED"
        } else {
            match.status = "UPCOMING"
        }

        database.child(match.matchId).setValue(match)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, null)
                } else {
                    onResult(false, task.exception?.message)
                }
            }
    }

    fun deleteMatch(matchId: String, onResult: (Boolean, String?) -> Unit) {
        database.child(matchId).removeValue()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, null)
                } else {
                    onResult(false, task.exception?.message)
                }
            }
    }

    fun generateLeagueFixtures(tournamentId: String, teams: List<Team>, onResult: (Boolean, String?) -> Unit) {
        val updates = mutableMapOf<String, Any?>()
        for (i in 0 until teams.size) {
            for (j in i + 1 until teams.size) {
                val teamA = teams[i]
                val teamB = teams[j]
                val matchId = database.push().key ?: continue
                val match = Match(
                    matchId = matchId,
                    tournamentId = tournamentId,
                    teamAId = teamA.teamId,
                    teamBId = teamB.teamId,
                    teamAName = teamA.name,
                    teamBName = teamB.name,
                    status = "UPCOMING",
                    stage = "LEAGUE"
                )
                updates["matches/$matchId"] = match
            }
        }
        FirebaseDatabase.getInstance().reference.updateChildren(updates).addOnCompleteListener { 
            onResult(it.isSuccessful, it.exception?.message)
        }
    }

    fun generateGroupStageFixtures(tournamentId: String, teams: List<Team>, onResult: (Boolean, String?) -> Unit) {
        val updates = mutableMapOf<String, Any?>()
        
        val groupA = teams.filter { it.groupName == "A" }
        val groupB = teams.filter { it.groupName == "B" }

        fun generateForGroup(groupTeams: List<Team>) {
            for (i in 0 until groupTeams.size) {
                for (j in i + 1 until groupTeams.size) {
                    val teamA = groupTeams[i]
                    val teamB = groupTeams[j]
                    val matchId = database.push().key ?: continue
                    val match = Match(
                        matchId = matchId,
                        tournamentId = tournamentId,
                        teamAId = teamA.teamId,
                        teamBId = teamB.teamId,
                        teamAName = teamA.name,
                        teamBName = teamB.name,
                        status = "UPCOMING",
                        stage = "GROUP"
                    )
                    updates["matches/$matchId"] = match
                }
            }
        }

        generateForGroup(groupA)
        generateForGroup(groupB)

        FirebaseDatabase.getInstance().reference.updateChildren(updates).addOnCompleteListener { 
            onResult(it.isSuccessful, it.exception?.message)
        }
    }

    fun deleteAllMatchesInTournament(tournamentId: String, onResult: (Boolean, String?) -> Unit) {
        database.orderByChild("tournamentId").equalTo(tournamentId).get().addOnSuccessListener { snapshot ->
            val updates = mutableMapOf<String, Any?>()
            snapshot.children.forEach { 
                updates[it.key!!] = null
            }
            database.updateChildren(updates).addOnCompleteListener { task ->
                if (task.isSuccessful) onResult(true, null)
                else onResult(false, task.exception?.message)
            }
        }.addOnFailureListener { onResult(false, it.message) }
    }
    
    fun generateKnockoutMatch(tournamentId: String, teamA: Team, teamB: Team, stage: String, onResult: (Boolean, String?) -> Unit) {
        val matchId = database.push().key ?: return onResult(false, "ID error")
        val match = Match(
            matchId = matchId,
            tournamentId = tournamentId,
            teamAId = teamA.teamId,
            teamBId = teamB.teamId,
            teamAName = teamA.name,
            teamBName = teamB.name,
            status = "UPCOMING",
            stage = stage
        )
        database.child(matchId).setValue(match).addOnCompleteListener {
            onResult(it.isSuccessful, it.exception?.message)
        }
    }
}