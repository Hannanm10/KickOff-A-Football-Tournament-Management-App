package com.example.kickoff.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.kickoff.R
import com.example.kickoff.models.Team
import com.example.kickoff.models.Tournament
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.card.MaterialCardView
import com.google.firebase.database.FirebaseDatabase

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
        val btnAnalytics = findViewById<MaterialCardView>(R.id.btnAnalyticsCard)

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

        btnAnalytics.setOnClickListener {
            val intent = Intent(this, AnalyticsActivity::class.java)
            intent.putExtra("tournamentId", tournamentId)
            intent.putExtra("tournamentName", tournamentName)
            startActivity(intent)
        }

        loadTournamentDetails()
    }

    private fun loadTournamentDetails() {
        FirebaseDatabase.getInstance().getReference("tournaments").child(tournamentId)
            .addValueEventListener(object : com.google.firebase.database.ValueEventListener {
                override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                    val tournament = snapshot.getValue(Tournament::class.java) ?: return
                    
                    findViewById<TextView>(R.id.tvFormat).text = "Format: ${if (tournament.format == "GROUP_KNOCKOUT") "Group + Knockout" else "League"}"
                    findViewById<TextView>(R.id.tvOrganizer).text = "Organizer: ${tournament.organizerName}"
                    
                    if (tournament.championTeamId.isNotEmpty()) {
                        fetchAndShowChampion(tournament.championTeamId)
                    } else {
                        findViewById<MaterialCardView>(R.id.cardChampion).visibility = View.GONE
                    }
                }

                override fun onCancelled(error: com.google.firebase.database.DatabaseError) {}
            })
    }

    private fun fetchAndShowChampion(teamId: String) {
        FirebaseDatabase.getInstance().getReference("teams").child(teamId).get()
            .addOnSuccessListener { snapshot ->
                val team = snapshot.getValue(Team::class.java)
                if (team != null) {
                    val card = findViewById<MaterialCardView>(R.id.cardChampion)
                    val tvChampion = findViewById<TextView>(R.id.tvChampionName)
                    tvChampion.text = team.name
                    card.visibility = View.VISIBLE
                }
            }
    }
}