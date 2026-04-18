package com.example.kickoff.activities

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.kickoff.R
import com.example.kickoff.models.Team
import com.example.kickoff.utils.SessionManager
import com.example.kickoff.utils.TeamStorage
import com.google.android.material.appbar.MaterialToolbar

class AddTeamActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_team)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        val etName = findViewById<EditText>(R.id.etTeamName)
        val btnSave = findViewById<Button>(R.id.btnSaveTeam)

        val tournament = intent.getStringExtra("tournament") ?: ""

        btnSave.setOnClickListener {

            val name = etName.text.toString().trim()

            if (name.isEmpty()) {
                etName.error = "Enter team name"
                return@setOnClickListener
            }

            val existing = TeamStorage.getTeams(this, tournament)
            if (existing.any { it.name.equals(name, ignoreCase = true) }) {
                etName.error = "This team already exists in this tournament"
                return@setOnClickListener
            }

            TeamStorage.addTeam(this, Team(name, tournament))

            Toast.makeText(this, "Team Added", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}