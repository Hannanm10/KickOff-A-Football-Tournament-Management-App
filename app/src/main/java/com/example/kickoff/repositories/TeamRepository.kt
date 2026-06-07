package com.example.kickoff.repositories

import com.example.kickoff.models.Team
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

object TeamRepository {
    private val database = FirebaseDatabase.getInstance().getReference("teams")

    fun addTeam(tournamentId: String, name: String, onResult: (Boolean, String?) -> Unit) {
        val teamId = database.push().key ?: return onResult(false, "Could not generate ID")
        val team = Team(
            teamId = teamId,
            tournamentId = tournamentId,
            name = name
        )

        database.child(teamId).setValue(team)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, null)
                } else {
                    onResult(false, task.exception?.message)
                }
            }
    }

    fun getTeamsByTournament(tournamentId: String, onDataChange: (List<Team>) -> Unit) {
        database.orderByChild("tournamentId").equalTo(tournamentId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = mutableListOf<Team>()
                    for (child in snapshot.children) {
                        val team = child.getValue(Team::class.java)
                        if (team != null) {
                            list.add(team)
                        }
                    }
                    onDataChange(list)
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })
    }

    fun deleteTeam(teamId: String, onResult: (Boolean, String?) -> Unit) {
        val rootRef = FirebaseDatabase.getInstance().reference
        val updates = mutableMapOf<String, Any?>()
        updates["teams/$teamId"] = null
        
        // Find matches involving this team
        rootRef.child("matches").get().addOnSuccessListener { snapshot ->
            snapshot.children.forEach { matchSnap ->
                val teamAId = matchSnap.child("teamAId").getValue(String::class.java)
                val teamBId = matchSnap.child("teamBId").getValue(String::class.java)
                if (teamAId == teamId || teamBId == teamId) {
                    updates["matches/${matchSnap.key}"] = null
                }
            }
            
            rootRef.updateChildren(updates).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, null)
                } else {
                    onResult(false, task.exception?.message)
                }
            }
        }.addOnFailureListener { onResult(false, it.message) }
    }
}