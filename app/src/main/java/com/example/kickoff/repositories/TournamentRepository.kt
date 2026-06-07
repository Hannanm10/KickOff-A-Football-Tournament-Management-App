package com.example.kickoff.repositories

import com.example.kickoff.models.Tournament
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

object TournamentRepository {
    private val database = FirebaseDatabase.getInstance().getReference("tournaments")
    private val auth = FirebaseAuth.getInstance()

    fun createTournament(name: String, description: String, format: String, onResult: (Boolean, String?) -> Unit) {
        val currentUserId = auth.currentUser?.uid ?: return onResult(false, "Not logged in")
        
        // Get username from users node first
        FirebaseDatabase.getInstance().getReference("users").child(currentUserId).get()
            .addOnSuccessListener { snapshot ->
                val username = snapshot.child("username").getValue(String::class.java) ?: "Unknown"
                
                val tournamentId = database.push().key ?: ""
                val tournament = Tournament(
                    tournamentId = tournamentId,
                    name = name,
                    description = description,
                    format = format,
                    organizerId = currentUserId,
                    organizerName = username
                )

                database.child(tournamentId).setValue(tournament)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            onResult(true, null)
                        } else {
                            onResult(false, task.exception?.message)
                        }
                    }
            }
            .addOnFailureListener {
                onResult(false, it.message)
            }
    }

    fun getAllTournaments(onDataChange: (List<Tournament>) -> Unit): ValueEventListener {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<Tournament>()
                for (child in snapshot.children) {
                    val tournament = child.getValue(Tournament::class.java)
                    if (tournament != null) {
                        list.add(tournament)
                    }
                }
                onDataChange(list)
            }

            override fun onCancelled(error: DatabaseError) {}
        }
        database.addValueEventListener(listener)
        return listener
    }

    fun removeListener(listener: ValueEventListener) {
        database.removeEventListener(listener)
    }

    fun updateTournament(tournamentId: String, newName: String, onResult: (Boolean, String?) -> Unit) {
        val updates = mapOf("name" to newName)
        database.child(tournamentId).updateChildren(updates)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, null)
                } else {
                    onResult(false, task.exception?.message)
                }
            }
    }

    fun deleteTournament(tournamentId: String, onResult: (Boolean, String?) -> Unit) {
        val rootRef = FirebaseDatabase.getInstance().reference
        
        // Define all paths to be deleted
        val updates = mutableMapOf<String, Any?>()
        updates["tournaments/$tournamentId"] = null
        
        // We need to find all teams and matches associated with this tournament
        rootRef.child("teams").orderByChild("tournamentId").equalTo(tournamentId).get()
            .addOnSuccessListener { teamsSnapshot ->
                teamsSnapshot.children.forEach { 
                    updates["teams/${it.key}"] = null 
                }
                
                rootRef.child("matches").orderByChild("tournamentId").equalTo(tournamentId).get()
                    .addOnSuccessListener { matchesSnapshot ->
                        matchesSnapshot.children.forEach { 
                            updates["matches/${it.key}"] = null 
                        }
                        
                        // Perform atomic delete
                        rootRef.updateChildren(updates)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    onResult(true, null)
                                } else {
                                    onResult(false, task.exception?.message)
                                }
                            }
                    }
                    .addOnFailureListener { onResult(false, it.message) }
            }
            .addOnFailureListener { onResult(false, it.message) }
    }

    fun setChampion(tournamentId: String, teamId: String, onResult: (Boolean) -> Unit) {
        database.child(tournamentId).child("championTeamId").setValue(teamId)
            .addOnCompleteListener { onResult(it.isSuccessful) }
    }

    fun completeTournament(tournamentId: String, championId: String, onResult: (Boolean) -> Unit) {
        val updates = mapOf(
            "status" to "COMPLETED",
            "championTeamId" to championId,
            "completionDate" to System.currentTimeMillis()
        )
        database.child(tournamentId).updateChildren(updates)
            .addOnCompleteListener { onResult(it.isSuccessful) }
    }
}