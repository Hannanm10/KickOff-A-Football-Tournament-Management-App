package com.example.kickoff.activities

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.kickoff.R
import com.example.kickoff.models.Tournament
import com.example.kickoff.utils.SessionManager
import com.example.kickoff.utils.TournamentStorage
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textfield.TextInputEditText

class AddTournamentActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_tournament)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        val etName = findViewById<TextInputEditText>(R.id.etTournamentName)
        val btnSave = findViewById<com.google.android.material.button.MaterialButton>(R.id.btnSaveTournament)

        val currentUser = SessionManager.getUser(this) ?: ""

        btnSave.setOnClickListener {

            val name = etName.text.toString().trim()

            if (name.isEmpty()) {
                etName.error = "Enter tournament name"
                return@setOnClickListener
            }

            val existing = TournamentStorage.getTournaments(this)
            if (existing.any { it.name.equals(name, ignoreCase = true) }) {
                etName.error = "A tournament with this name already exists"
                return@setOnClickListener
            }

            val tournament = Tournament(name, currentUser)
            TournamentStorage.addTournament(this, tournament)

            Toast.makeText(this, "Tournament Created", Toast.LENGTH_SHORT).show()

            finish()
        }
    }
}