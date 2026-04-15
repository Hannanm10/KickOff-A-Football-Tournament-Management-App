package com.example.kickoff.activities

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.kickoff.R
import com.example.kickoff.models.Match
import com.example.kickoff.utils.MatchStorage
import com.example.kickoff.utils.TeamStorage

class AddMatchActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_match)

        val tournament = intent.getStringExtra("tournament") ?: ""

        val spTeamA = findViewById<Spinner>(R.id.spTeamA)
        val spTeamB = findViewById<Spinner>(R.id.spTeamB)

        val scoreA = findViewById<EditText>(R.id.etScoreA)
        val scoreB = findViewById<EditText>(R.id.etScoreB)
        val btnSave = findViewById<Button>(R.id.btnSaveMatch)

        // Load teams
        val teams = TeamStorage.getTeams(this, tournament).map { it.name }

        if (teams.size < 2) {
            Toast.makeText(this, "Add at least 2 teams first", Toast.LENGTH_LONG).show()
            finish()
        }

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            teams
        )

        spTeamA.adapter = adapter
        spTeamB.adapter = adapter

        btnSave.setOnClickListener {

            val teamA = spTeamA.selectedItem.toString()
            val teamB = spTeamB.selectedItem.toString()

            // Prevent same team match
            if (teamA == teamB) {
                Toast.makeText(this, "Select different teams", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (scoreA.text.isNullOrBlank() || scoreB.text.isNullOrBlank()
                || scoreA.text.toString().toInt() < 0 || scoreB.text.toString().toInt() < 0) {
                Toast.makeText(this, "Enter valid scores", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            val match = Match(
                teamA,
                teamB,
                scoreA.text.toString().toIntOrNull() ?: 0,
                scoreB.text.toString().toIntOrNull() ?: 0,
                tournament
            )

            MatchStorage.addMatch(this, match)

            Toast.makeText(this, "Match Saved", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}