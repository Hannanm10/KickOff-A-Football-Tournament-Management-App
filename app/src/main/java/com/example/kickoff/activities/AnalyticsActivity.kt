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
import kotlin.math.abs

class AnalyticsActivity : AppCompatActivity() {

    private lateinit var tournamentId: String
    private lateinit var progressBar: ProgressBar
    
    // UI Elements
    private lateinit var progressTournament: CircularProgressIndicator
    private lateinit var tvProgressPercent: TextView
    private lateinit var tvTotalTeams: TextView
    private lateinit var tvTotalMatches: TextView
    private lateinit var tvAvgGoals: TextView
    private lateinit var tvTotalGoals: TextView
    private lateinit var tvLeaderTeam: TextView
    private lateinit var tvLeaderStats: TextView
    
    private lateinit var tvHighestScoringMatch: TextView
    private lateinit var tvBiggestVictory: TextView
    
    private lateinit var tvWinStreak: TextView
    private lateinit var tvUnbeatenRun: TextView
    private lateinit var tvCleanSheets: TextView
    private lateinit var tvMostActive: TextView
    private lateinit var tvBestOffense: TextView
    private lateinit var tvBestDefense: TextView
    
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
        tvAvgGoals = findViewById(R.id.tvAvgGoals)
        tvTotalGoals = findViewById(R.id.tvTotalGoals)
        tvLeaderTeam = findViewById(R.id.tvLeaderTeam)
        tvLeaderStats = findViewById(R.id.tvLeaderStats)
        
        tvHighestScoringMatch = findViewById(R.id.tvHighestScoringMatch)
        tvBiggestVictory = findViewById(R.id.tvBiggestVictory)
        
        tvWinStreak = findViewById(R.id.tvWinStreak)
        tvUnbeatenRun = findViewById(R.id.tvUnbeatenRun)
        tvCleanSheets = findViewById(R.id.tvCleanSheets)
        tvMostActive = findViewById(R.id.tvMostActive)
        tvBestOffense = findViewById(R.id.tvBestOffense)
        tvBestDefense = findViewById(R.id.tvBestDefense)
        
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

        val completedMatches = matches.filter { it.status == "COMPLETED" }
        val totalGoals = completedMatches.sumOf { it.scoreA + it.scoreB }
        val avgGoals = if (completedMatches.isNotEmpty()) totalGoals.toDouble() / completedMatches.size else 0.0
        val progress = if (matches.isNotEmpty()) (completedMatches.size * 100 / matches.size) else 0
        
        class TeamStats(val name: String, val id: String) {
            var wins = 0
            var draws = 0
            var losses = 0
            var gf = 0
            var ga = 0
            var pts = 0
            var cleanSheets = 0
            val gd get() = gf - ga
            
            // Streak tracking
            var currentWinStreak = 0
            var maxWinStreak = 0
            var currentUnbeatenRun = 0
            var maxUnbeatenRun = 0
        }

        val statsMap = teams.associate { it.teamId to TeamStats(it.name, it.teamId) }

        // Sort matches by date to calculate streaks correctly
        val sortedCompleted = completedMatches.sortedBy { it.matchDate }

        sortedCompleted.forEach { m ->
            val sA = statsMap[m.teamAId]
            val sB = statsMap[m.teamBId]
            
            if (sA != null && sB != null) {
                sA.gf += m.scoreA
                sA.ga += m.scoreB
                sB.gf += m.scoreB
                sB.ga += m.scoreA
                
                if (m.scoreB == 0) sA.cleanSheets++
                if (m.scoreA == 0) sB.cleanSheets++
                
                when {
                    m.scoreA > m.scoreB -> {
                        // Team A wins
                        sA.wins++; sA.pts += 3; sB.losses++
                        
                        sA.currentWinStreak++; sA.maxWinStreak = maxOf(sA.maxWinStreak, sA.currentWinStreak)
                        sA.currentUnbeatenRun++; sA.maxUnbeatenRun = maxOf(sA.maxUnbeatenRun, sA.currentUnbeatenRun)
                        
                        sB.currentWinStreak = 0
                        sB.currentUnbeatenRun = 0
                    }
                    m.scoreB > m.scoreA -> {
                        // Team B wins
                        sB.wins++; sB.pts += 3; sA.losses++
                        
                        sB.currentWinStreak++; sB.maxWinStreak = maxOf(sB.maxWinStreak, sB.currentWinStreak)
                        sB.currentUnbeatenRun++; sB.maxUnbeatenRun = maxOf(sB.maxUnbeatenRun, sB.currentUnbeatenRun)
                        
                        sA.currentWinStreak = 0
                        sA.currentUnbeatenRun = 0
                    }
                    else -> {
                        // Draw
                        sA.draws++; sA.pts += 1; sB.draws++; sB.pts += 1
                        
                        sA.currentWinStreak = 0
                        sA.currentUnbeatenRun++; sA.maxUnbeatenRun = maxOf(sA.maxUnbeatenRun, sA.currentUnbeatenRun)
                        
                        sB.currentWinStreak = 0
                        sB.currentUnbeatenRun++; sB.maxUnbeatenRun = maxOf(sB.maxUnbeatenRun, sB.currentUnbeatenRun)
                    }
                }
            }
        }

        // Match Records
        val highestScoringMatch = completedMatches.maxByOrNull { it.scoreA + it.scoreB }
        val biggestVictory = completedMatches.maxByOrNull { abs(it.scoreA - it.scoreB) }

        // Team-based aggregation
        val sortedStats = statsMap.values.sortedWith(compareByDescending<TeamStats> { it.pts }.thenByDescending { it.gd }.thenByDescending { it.gf })
        val leader = sortedStats.firstOrNull()
        
        val maxWinStreakTeam = statsMap.values.maxByOrNull { it.maxWinStreak }
        val maxUnbeatenRunTeam = statsMap.values.maxByOrNull { it.maxUnbeatenRun }
        val mostCleanSheetsTeam = statsMap.values.maxByOrNull { it.cleanSheets }
        val mostActiveTeam = statsMap.values.maxByOrNull { t -> completedMatches.count { it.teamAId == t.id || it.teamBId == t.id } }
        val bestOffense = statsMap.values.maxByOrNull { it.gf }
        val bestDefense = statsMap.values.filter { t -> completedMatches.any { it.teamAId == t.id || it.teamBId == t.id } }.minByOrNull { it.ga }

        // Update UI
        runOnUiThread {
            progressBar.visibility = View.GONE
            
            tvTotalTeams.text = teams.size.toString()
            tvTotalMatches.text = matches.size.toString()
            tvAvgGoals.text = String.format("%.1f", avgGoals)
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
            
            highestScoringMatch?.let { 
                tvHighestScoringMatch.text = "${it.teamAName} ${it.scoreA} - ${it.scoreB} ${it.teamBName} (${it.scoreA + it.scoreB} Goals)" 
            }
            biggestVictory?.let {
                tvBiggestVictory.text = "${it.teamAName} ${it.scoreA} - ${it.scoreB} ${it.teamBName} (+${abs(it.scoreA - it.scoreB)} Diff)"
            }
            
            maxWinStreakTeam?.let { tvWinStreak.text = "${it.name}: ${it.maxWinStreak}" }
            maxUnbeatenRunTeam?.let { tvUnbeatenRun.text = "${it.name}: ${it.maxUnbeatenRun}" }
            mostCleanSheetsTeam?.let { tvCleanSheets.text = "${it.name}: ${it.cleanSheets}" }
            mostActiveTeam?.let { tvMostActive.text = "${it.name}" }
            bestOffense?.let { tvBestOffense.text = "${it.name}: ${it.gf}" }
            bestDefense?.let { tvBestDefense.text = "${it.name}: ${it.ga}" }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        teamListener?.let { TeamRepository.removeListener(it) }
        matchListener?.let { MatchRepository.removeListener(it) }
    }
}