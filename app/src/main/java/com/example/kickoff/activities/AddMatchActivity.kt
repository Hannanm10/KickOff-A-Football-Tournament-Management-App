package com.example.kickoff.activities

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.kickoff.R
import com.example.kickoff.models.Match
import com.example.kickoff.utils.MatchStorage
import com.example.kickoff.utils.TeamStorage
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textfield.TextInputEditText
import java.util.*

class AddMatchActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_match)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        val tournament = intent.getStringExtra("tournament") ?: ""

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

        // Load teams
        val teamList = TeamStorage.getTeams(this, tournament)
        val teamNames = teamList.map { it.name }

        if (teamNames.size < 2) {
            Toast.makeText(this, "Add at least 2 teams to the tournament first", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            teamNames
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        spTeamA.adapter = adapter
        spTeamB.adapter = adapter

        btnSave.setOnClickListener {
            val teamA = spTeamA.selectedItem?.toString() ?: ""
            val teamB = spTeamB.selectedItem?.toString() ?: ""

            if (teamA == teamB) {
                Toast.makeText(this, "A team cannot play against itself", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val sA = scoreA.text.toString()
            val sB = scoreB.text.toString()

            if (sA.isBlank()) {
                scoreA.error = "Enter score"
                return@setOnClickListener
            }
            if (sB.isBlank()) {
                scoreB.error = "Enter score"
                return@setOnClickListener
            }

            val valA = sA.toIntOrNull()
            val valB = sB.toIntOrNull()

            if (valA == null || valA < 0) {
                scoreA.error = "Invalid score"
                return@setOnClickListener
            }
            if (valB == null || valB < 0) {
                scoreB.error = "Invalid score"
                return@setOnClickListener
            }

            // Check if match already exists (optional business rule)
            val existingMatches = MatchStorage.getMatches(this, tournament)
            val isDuplicate = existingMatches.any { 
                (it.teamA == teamA && it.teamB == teamB) || (it.teamA == teamB && it.teamB == teamA) 
            }
            
            if (isDuplicate) {
                Toast.makeText(this, "This match pairing already exists", Toast.LENGTH_SHORT).show()
                // Not blocking, just a warning or could block based on requirement
            }

            val match = Match(
                teamA,
                teamB,
                valA,
                valB,
                tournament,
                etMatchDate.text.toString()
            )

            MatchStorage.addMatch(this, match)

            Toast.makeText(this, "Match Saved", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}