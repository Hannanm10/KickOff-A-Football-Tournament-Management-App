package com.example.kickoff.activities

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.kickoff.R
import com.example.kickoff.models.Team
import com.example.kickoff.utils.SessionManager
import com.example.kickoff.utils.TeamStorage

class AddTeamActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_team)

        val etName = findViewById<EditText>(R.id.etTeamName)
        val btnSave = findViewById<Button>(R.id.btnSaveTeam)

        val tournament = intent.getStringExtra("tournament") ?: ""

        btnSave.setOnClickListener {

            val name = etName.text.toString()

            if (name.isEmpty()) {
                Toast.makeText(this, "Enter team name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            TeamStorage.addTeam(this, Team(name, tournament))

            Toast.makeText(this, "Team Added", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}