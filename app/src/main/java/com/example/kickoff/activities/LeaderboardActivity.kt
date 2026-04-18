package com.example.kickoff.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kickoff.R
import com.example.kickoff.adapters.LeaderboardAdapter
import com.example.kickoff.models.LeaderboardEntry
import com.example.kickoff.utils.MatchStorage
import com.example.kickoff.utils.TeamStorage
import com.google.android.material.appbar.MaterialToolbar

class LeaderboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leaderboard)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        val tournamentName = intent.getStringExtra("tournament") ?: ""
        toolbar.title = "Leaderboard: $tournamentName"

        val teams = TeamStorage.getTeams(this, tournamentName)
        val matches = MatchStorage.getMatches(this, tournamentName)

        val leaderboard = teams.map { team ->
            val entry = LeaderboardEntry(team.name)
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

        val rv = findViewById<RecyclerView>(R.id.rvLeaderboard)
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = LeaderboardAdapter(leaderboard)
    }
}