package com.example.kickoff.activities

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.kickoff.R
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.card.MaterialCardView

class TournamentDetailActivity : AppCompatActivity() {

    private lateinit var tournamentId: String
    private lateinit var tournamentName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tournament_detail)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        tournamentId = intent.getStringExtra("tournamentId") ?: ""
        tournamentName = intent.getStringExtra("tournamentName") ?: ""

        val tvName = findViewById<TextView>(R.id.tvTournamentName)
        tvName.text = tournamentName

        val btnTeams = findViewById<MaterialCardView>(R.id.btnTeamsCard)
        val btnMatches = findViewById<MaterialCardView>(R.id.btnMatchesCard)
        val btnLeaderboard = findViewById<MaterialCardView>(R.id.btnLeaderboardCard)

        btnTeams.setOnClickListener {
            val intent = Intent(this, TeamListActivity::class.java)
            intent.putExtra("tournamentId", tournamentId)
            intent.putExtra("tournamentName", tournamentName)
            startActivity(intent)
        }

        btnMatches.setOnClickListener {
            val intent = Intent(this, MatchListActivity::class.java)
            intent.putExtra("tournamentId", tournamentId)
            intent.putExtra("tournamentName", tournamentName)
            startActivity(intent)
        }

        btnLeaderboard.setOnClickListener {
            val intent = Intent(this, LeaderboardActivity::class.java)
            intent.putExtra("tournamentId", tournamentId)
            intent.putExtra("tournamentName", tournamentName)
            startActivity(intent)
        }
    }
}