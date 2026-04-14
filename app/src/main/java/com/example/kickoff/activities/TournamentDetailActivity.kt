package com.example.kickoff.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.kickoff.R
import com.example.kickoff.utils.SessionManager

class TournamentDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tournament_detail)

        val name = intent.getStringExtra("name") ?: ""
        val organizer = intent.getStringExtra("organizer") ?: ""
        val currentUser = SessionManager.getUser(this) ?: ""

        val tvName = findViewById<TextView>(R.id.tvTournamentName)
        val tvOrg = findViewById<TextView>(R.id.tvOrganizer)

        val btnTeams = findViewById<Button>(R.id.btnTeams)
        val btnMatches = findViewById<Button>(R.id.btnMatches)

        tvName.text = name
        tvOrg.text = "Organizer: $organizer"

        val isOrganizer = currentUser == organizer

        btnTeams.setOnClickListener {

            val intent = Intent(this, TeamListActivity::class.java)
            intent.putExtra("tournament", name)
            intent.putExtra("organizer", organizer)

            startActivity(intent)
        }

        btnMatches.setOnClickListener {
            Toast.makeText(this,
                if (isOrganizer) "Organizer access" else "View only access",
                Toast.LENGTH_SHORT
            ).show()

            // Next step: open MatchListActivity
        }
    }
}