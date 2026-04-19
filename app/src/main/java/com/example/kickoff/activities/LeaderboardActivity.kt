package com.example.kickoff.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.kickoff.R
import com.example.kickoff.adapters.LeaderboardAdapter
import com.example.kickoff.models.LeaderboardEntry
import com.example.kickoff.utils.MatchStorage
import com.example.kickoff.utils.TeamStorage
import com.google.android.material.appbar.MaterialToolbar

class LeaderboardActivity : AppCompatActivity() {

    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var rv: RecyclerView
    private var tournamentName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leaderboard)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        tournamentName = intent.getStringExtra("tournament") ?: ""
        toolbar.title = "Leaderboard: $tournamentName"

        rv = findViewById(R.id.rvLeaderboard)
        rv.layoutManager = LinearLayoutManager(this)

        swipeRefresh = findViewById(R.id.swipeRefresh)
        swipeRefresh.setOnRefreshListener {
            loadLeaderboard()
        }

        loadLeaderboard()
    }

    override fun onResume() {
        super.onResume()
        loadLeaderboard()
    }

    private fun loadLeaderboard() {
        val teams = TeamStorage.getTeams(this, tournamentName)
        val matches = MatchStorage.getMatches(this, tournamentName)

        val leaderboard = teams.map { team ->
            val entry = LeaderboardEntry(team.name, team.logoUri)
            matches.forEach { match ->
                if (match.teamA == team.name || match.teamB == team.name) {
                    entry.matchesPlayed++
                    if (match.teamA == team.name) {
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
            val intent = android.content.Intent(this, MatchListActivity::class.java)
            intent.putExtra("tournament", tournamentName)
            intent.putExtra("team_filter", teamName)
            startActivity(intent)
        }
        swipeRefresh.isRefreshing = false
    }
}