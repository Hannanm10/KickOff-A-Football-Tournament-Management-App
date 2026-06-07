package com.example.kickoff.activities

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.kickoff.R
import com.example.kickoff.models.Match
import com.example.kickoff.models.Team
import com.example.kickoff.models.Tournament
import com.example.kickoff.repositories.MatchRepository
import com.example.kickoff.repositories.TeamRepository
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.card.MaterialCardView
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AnalyticsActivity : AppCompatActivity() {

    private lateinit var tournamentId: String
    private lateinit var progressBar: ProgressBar
    
    // UI Elements
    private lateinit var progressTournament: CircularProgressIndicator
    private lateinit var tvProgressPercent: TextView
    private lateinit var tvTotalTeams: TextView
    private lateinit var tvTotalMatches: TextView
    private lateinit var tvCompletedMatches: TextView
    private lateinit var tvTotalGoals: TextView
    private lateinit var tvLeaderTeam: TextView
    private lateinit var tvLeaderStats: TextView
    private lateinit var tvBestOffense: TextView
    private lateinit var tvBestDefense: TextView
    private lateinit var tvMostWins: TextView
    private lateinit var tvMostDraws: TextView
    private lateinit var tvMostLosses: TextView
    private lateinit var cardLeader: MaterialCardView

    private var teams = listOf<Team>()
    private var matches = listOf<Match>()
    private var tournament: Tournament? = null
    
    private var teamListener: ValueEventListener? = null
    private var matchListener: ValueEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analytics)

        tournamentId = intent.getStringExtra("tournamentId") ?: ""
        val tournamentName = intent.getStringExtra("tournamentName") ?: ""

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }
        toolbar.title = "Analytics: $tournamentName"

        initUI()
        loadTournament()
        loadData()
    }

    private fun initUI() {
        progressBar = findViewById(R.id.progressBar)
        progressTournament = findViewById(R.id.progressTournament)
        tvProgressPercent = findViewById(R.id.tvProgressPercent)
        tvTotalTeams = findViewById(R.id.tvTotalTeams)
        tvTotalMatches = findViewById(R.id.tvTotalMatches)
        tvCompletedMatches = findViewById(R.id.tvCompletedMatches)
        tvTotalGoals = findViewById(R.id.tvTotalGoals)
        tvLeaderTeam = findViewById(R.id.tvLeaderTeam)
        tvLeaderStats = findViewById(R.id.tvLeaderStats)
        tvBestOffense = findViewById(R.id.tvBestOffense)
        tvBestDefense = findViewById(R.id.tvBestDefense)
        tvMostWins = findViewById(R.id.tvMostWins)
        tvMostDraws = findViewById(R.id.tvMostDraws)
        tvMostLosses = findViewById(R.id.tvMostLosses)
        cardLeader = findViewById(R.id.cardLeader)
    }

    private fun loadTournament() {
        FirebaseDatabase.getInstance().getReference("tournaments").child(tournamentId).get()
            .addOnSuccessListener { snapshot ->
                tournament = snapshot.getValue(Tournament::class.java)
                calculateStats()
            }
    }

    private fun loadData() {
        progressBar.visibility = View.VISIBLE
        
        teamListener = TeamRepository.getTeamsByTournament(tournamentId) { list ->
            teams = list
            calculateStats()
        }
        
        matchListener = MatchRepository.getMatchesByTournament(tournamentId) { list ->
            matches = list
            calculateStats()
        }
    }

    private fun calculateStats() {
        val t = tournament ?: return
        if (teams.isEmpty()) {
            progressBar.visibility = if (teams.isEmpty() && matches.isEmpty()) View.GONE else View.VISIBLE
            return
        }

        val totalTeams = teams.size
        val totalMatches = matches.size
        val completedMatches = matches.count { it.status == "COMPLETED" }
        val totalGoals = matches.filter { it.status == "COMPLETED" }.sumOf { it.scoreA + it.scoreB }
        
        val progress = if (totalMatches > 0) (completedMatches * 100 / totalMatches) else 0
        
        class TeamStats(val name: String, val id: String) {
            var wins = 0
            var draws = 0
            var losses = 0
            var gf = 0
            var ga = 0
            var pts = 0
            val gd get() = gf - ga
        }

        val statsMap = teams.associate { it.teamId to TeamStats(it.name, it.teamId) }

        matches.filter { it.status == "COMPLETED" }.forEach { m ->
            val sA = statsMap[m.teamAId]
            val sB = statsMap[m.teamBId]
            if (sA != null && sB != null) {
                sA.gf += m.scoreA
                sA.ga += m.scoreB
                sB.gf += m.scoreB
                sB.ga += m.scoreA
                when {
                    m.scoreA > m.scoreB -> { sA.wins++; sA.pts += 3; sB.losses++ }
                    m.scoreB > m.scoreA -> { sB.wins++; sB.pts += 3; sA.losses++ }
                    else -> { sA.draws++; sA.pts += 1; sB.draws++; sB.pts += 1 }
                }
            }
        }

        val sortedStats = statsMap.values.sortedWith(compareByDescending<TeamStats> { it.pts }.thenByDescending { it.gd }.thenByDescending { it.gf })
        val leader = sortedStats.firstOrNull()
        
        val bestOffense = statsMap.values.maxByOrNull { it.gf }
        val bestDefense = statsMap.values
            .filter { teamStat -> 
                matches.any { m -> (m.teamAId == teamStat.id || m.teamBId == teamStat.id) && m.status == "COMPLETED" } 
            }
            .minByOrNull { it.ga }
        
        val mostWins = statsMap.values.maxByOrNull { it.wins }
        val mostDraws = statsMap.values.maxByOrNull { it.draws }
        val mostLosses = statsMap.values.maxByOrNull { it.losses }

        runOnUiThread {
            progressBar.visibility = View.GONE
            tvTotalTeams.text = totalTeams.toString()
            tvTotalMatches.text = totalMatches.toString()
            tvCompletedMatches.text = completedMatches.toString()
            tvTotalGoals.text = totalGoals.toString()
            
            progressTournament.progress = progress
            tvProgressPercent.text = "$progress%"
            
            if (t.format == "GROUP_KNOCKOUT") {
                cardLeader.visibility = View.GONE
            } else {
                cardLeader.visibility = View.VISIBLE
                leader?.let {
                    tvLeaderTeam.text = it.name
                    tvLeaderStats.text = "${it.pts} Points | ${it.gd} GD"
                }
            }
            
            bestOffense?.let { tvBestOffense.text = "${it.name} (${it.gf})" }
            bestDefense?.let { tvBestDefense.text = "${it.name} (${it.ga})" }
            mostWins?.let { tvMostWins.text = "${it.name} (${it.wins})" }
            mostDraws?.let { tvMostDraws.text = "${it.name} (${it.draws})" }
            mostLosses?.let { tvMostLosses.text = "${it.name} (${it.losses})" }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        teamListener?.let { TeamRepository.removeListener(it) }
        matchListener?.let { MatchRepository.removeListener(it) }
    }
}