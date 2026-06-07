package com.example.kickoff.repositories

import com.example.kickoff.models.Match
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
}