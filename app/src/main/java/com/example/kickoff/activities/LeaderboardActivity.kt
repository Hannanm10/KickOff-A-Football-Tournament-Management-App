package com.example.kickoff.activities

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.kickoff.R
import com.example.kickoff.adapters.LeaderboardAdapter
import com.example.kickoff.models.LeaderboardEntry
import com.example.kickoff.models.Match
import com.example.kickoff.models.Team
import com.example.kickoff.repositories.MatchRepository
import com.example.kickoff.repositories.TeamRepository
import com.google.android.material.appbar.MaterialToolbar

class LeaderboardActivity : AppCompatActivity() {

    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var rv: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tournamentId: String
    private lateinit var tournamentName: String

    private var teams = listOf<Team>()
    private var matches = listOf<Match>()
    
    private var teamListener: com.google.firebase.database.ValueEventListener? = null
    private var matchListener: com.google.firebase.database.ValueEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leaderboard)

        tournamentId = intent.getStringExtra("tournamentId") ?: ""
        tournamentName = intent.getStringExtra("tournamentName") ?: ""

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }
        toolbar.title = "Leaderboard: $tournamentName"

        rv = findViewById(R.id.rvLeaderboard)
        rv.layoutManager = LinearLayoutManager(this)

        progressBar = findViewById(R.id.progressBar)
        swipeRefresh = findViewById(R.id.swipeRefresh)
        swipeRefresh.setOnRefreshListener {
            loadData()
        }

        loadData()
    }

    private fun loadData() {
        progressBar.visibility = View.VISIBLE
        
        // Remove existing if any (unlikely in onCreate but good for swipeRefresh)
        teamListener?.let { TeamRepository.removeListener(it) }
        matchListener?.let { MatchRepository.removeListener(it) }

        teamListener = TeamRepository.getTeamsByTournament(tournamentId) { teamList ->
            teams = teamList
            calculateAndDisplay()
        }
        
        matchListener = MatchRepository.getMatchesByTournament(tournamentId) { matchList ->
            matches = matchList
            calculateAndDisplay()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        teamListener?.let { TeamRepository.removeListener(it) }
        matchListener?.let { MatchRepository.removeListener(it) }
    }

    private fun calculateAndDisplay() {
        if (teams.isEmpty()) {
            progressBar.visibility = View.GONE
            rv.adapter = LeaderboardAdapter(emptyList()) { }
            swipeRefresh.isRefreshing = false
            return
        }

        progressBar.visibility = View.GONE
        
        val leaderboard = teams.map { team ->
            val entry = LeaderboardEntry(team.name)
            matches.forEach { match ->
                if (match.status == "COMPLETED" && (match.teamAId == team.teamId || match.teamBId == team.teamId)) {
                    entry.matchesPlayed++
                    if (match.teamAId == team.teamId) {
                        entry.goalsFor += match.scoreA
                        entry.goalsAgainst += match.scoreB
                        when {
                            match.scoreA > match.scoreB -> {
                                entry.wins++
                                entry.points += 3
                            }
                            match.scoreA == match.scoreB -> {
                                entry.draws++
                                entry.points += 1
                            }
                            else -> entry.losses++
                        }
                    } else {
                        entry.goalsFor += match.scoreB
                        entry.goalsAgainst += match.scoreA
                        when {
                            match.scoreB > match.scoreA -> {
                                entry.wins++
                                entry.points += 3
                            }
                            match.scoreB == match.scoreA -> {
                                entry.draws++
                                entry.points += 1
                            }
                            else -> entry.losses++
                        }
                    }
                }
            }
            entry
        }.sortedWith(compareByDescending<LeaderboardEntry> { it.points }
            .thenByDescending { it.goalDifference }
            .thenByDescending { it.goalsFor })

        rv.adapter = LeaderboardAdapter(leaderboard) { teamName ->
            val teamId = teams.find { it.name == teamName }?.teamId
            val intent = android.content.Intent(this, MatchListActivity::class.java)
            intent.putExtra("tournamentId", tournamentId)
            intent.putExtra("tournamentName", tournamentName)
            intent.putExtra("team_filter_id", teamId)
            startActivity(intent)
        }
        swipeRefresh.isRefreshing = false
    }
}