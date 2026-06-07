package com.example.kickoff.activities

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.kickoff.R
import com.example.kickoff.repositories.TeamRepository
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textfield.TextInputEditText

class AddTeamActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_team)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        val etName = findViewById<TextInputEditText>(R.id.etTeamName)
        val btnSave = findViewById<com.google.android.material.button.MaterialButton>(R.id.btnSaveTeam)

        val tournamentId = intent.getStringExtra("tournamentId") ?: ""

        btnSave.setOnClickListener {
            val name = etName.text.toString().trim()

            if (name.isEmpty()) {
                Toast.makeText(this, "Enter team name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnSave.isEnabled = false
            TeamRepository.addTeam(tournamentId, name) { success, error ->
                btnSave.isEnabled = true
                if (success) {
                    Toast.makeText(this, "Team added", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Error: $error", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}