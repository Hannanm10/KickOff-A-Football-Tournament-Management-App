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

    fun generateRoundRobinFixtures(tournamentId: String, teams: List<Team>, onResult: (Boolean, String?) -> Unit) {
        if (teams.size < 2) {
            onResult(false, "At least 2 teams are required")
            return
        }

        val rootRef = FirebaseDatabase.getInstance().reference
        val updates = mutableMapOf<String, Any?>()

        // 1. Generate fixtures logic
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
                    scoreA = -1,
                    scoreB = -1
                )
                updates["matches/$matchId"] = match
            }
        }

        // 2. Clear existing matches first? No, we should ask user in UI.
        // This function just performs the insertion.
        rootRef.updateChildren(updates).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                onResult(true, null)
            } else {
                onResult(false, task.exception?.message)
            }
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
}