package com.example.kickoff.activities

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.kickoff.R
import com.example.kickoff.models.Match
import com.example.kickoff.models.Team
import com.example.kickoff.models.Tournament
import com.example.kickoff.repositories.MatchRepository
import com.example.kickoff.repositories.TeamRepository
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.FirebaseDatabase
import java.util.*

class AddMatchActivity : AppCompatActivity() {

    private var teamList = mutableListOf<Team>()
    private var tournament: Tournament? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_match)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        val tournamentId = intent.getStringExtra("tournamentId") ?: ""

        val spTeamA = findViewById<Spinner>(R.id.spTeamA)
        val spTeamB = findViewById<Spinner>(R.id.spTeamB)

        val scoreA = findViewById<TextInputEditText>(R.id.etScoreA)
        val scoreB = findViewById<TextInputEditText>(R.id.etScoreB)
        val etMatchDate = findViewById<TextInputEditText>(R.id.etMatchDate)
        val btnSave = findViewById<com.google.android.material.button.MaterialButton>(R.id.btnSaveMatch)

        etMatchDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val dpd = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                val dateStr = "$selectedYear-${selectedMonth + 1}-$selectedDay"
                etMatchDate.setText(dateStr)
            }, year, month, day)
            dpd.show()
        }

        FirebaseDatabase.getInstance().getReference("tournaments").child(tournamentId).get()
            .addOnSuccessListener { snapshot ->
                tournament = snapshot.getValue(Tournament::class.java)
            }

        TeamRepository.getTeamsByTournament(tournamentId) { list ->
            teamList.clear()
            teamList.addAll(list)
            
            if (teamList.size < 2) {
                Toast.makeText(this, "Add at least 2 teams first", Toast.LENGTH_LONG).show()
                finish()
                return@getTeamsByTournament
            }

            val teamNames = teamList.map { it.name }
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, teamNames).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
            spTeamA.adapter = adapter
            spTeamB.adapter = adapter
        }

        btnSave.setOnClickListener {
            val posA = spTeamA.selectedItemPosition
            val posB = spTeamB.selectedItemPosition

            if (posA == posB) {
                Toast.makeText(this, "Teams must be different", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val teamA = teamList[posA]
            val teamB = teamList[posB]

            val sA = scoreA.text.toString()
            val sB = scoreB.text.toString()

            if (sA.isBlank() || sB.isBlank()) {
                Toast.makeText(this, "Enter scores", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val valA = sA.toIntOrNull() ?: 0
            val valB = sB.toIntOrNull() ?: 0
            
            val stage = if (tournament?.format == "LEAGUE") "LEAGUE" else "GROUP"

            val match = Match(
                tournamentId = tournamentId,
                teamAId = teamA.teamId,
                teamBId = teamB.teamId,
                teamAName = teamA.name,
                teamBName = teamB.name,
                scoreA = valA,
                scoreB = valB,
                matchDate = etMatchDate.text.toString(),
                stage = stage
            )

            btnSave.isEnabled = false
            MatchRepository.addMatch(match) { success, error ->
                btnSave.isEnabled = true
                if (success) {
                    Toast.makeText(this, "Match added", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Error: $error", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}