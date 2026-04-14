package com.example.kickoff.activities

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kickoff.R
import com.example.kickoff.adapters.TournamentAdapter
import com.example.kickoff.models.Tournament
import com.example.kickoff.utils.SessionManager

class TournamentListActivity : AppCompatActivity() {

    private val tournamentList = mutableListOf<Tournament>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tournament_list)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        val btnAdd = findViewById<Button>(R.id.btnAddTournament)

        val currentUser = SessionManager.getUser(this) ?: ""

        // Temporary dummy data (replace later)
        tournamentList.add(Tournament("Champions League", "user1"))
        tournamentList.add(Tournament("Local Cup", "user1"))

        val adapter = TournamentAdapter(tournamentList) { tournament ->
            Toast.makeText(this, "Clicked: ${tournament.name}", Toast.LENGTH_SHORT).show()
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        btnAdd.setOnClickListener {
            Toast.makeText(this, "Add Tournament Clicked", Toast.LENGTH_SHORT).show()
        }
    }
}