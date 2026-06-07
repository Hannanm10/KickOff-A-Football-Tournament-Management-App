package com.example.kickoff.activities

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.kickoff.R
import com.example.kickoff.models.Match
import com.example.kickoff.models.Team
import com.example.kickoff.models.Tournament
import com.example.kickoff.repositories.MatchRepository
import com.example.kickoff.repositories.TeamRepository
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

class TournamentCompletionActivity : AppCompatActivity() {

    private lateinit var tournamentId: String
    private var tournament: Tournament? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tournament_completion)

        tournamentId = intent.getStringExtra("tournamentId") ?: ""

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        loadData()
    }

    private fun loadData() {
        FirebaseDatabase.getInstance().getReference("tournaments").child(tournamentId).get()
            .addOnSuccessListener { snapshot ->
                tournament = snapshot.getValue(Tournament::class.java)
                tournament?.let { t ->
                    TeamRepository.getTeamsOnce(tournamentId) { teams ->
                        MatchRepository.getMatchesByTournament(tournamentId) { matches ->
                            calculateAndDisplay(t, teams, matches)
                        }
                    }
                }
            }
    }

    private fun calculateAndDisplay(t: Tournament, teams: List<Team>, matches: List<Match>) {
        val completed = matches.filter { it.status == "COMPLETED" }
        
        // Winner & Runner Up
        var championName = "Unknown"
        var runnerUpName = "N/A"

        if (t.format == "LEAGUE") {
            val standings = calculateStandings(teams, completed)
            championName = standings.firstOrNull()?.name ?: "Unknown"
            runnerUpName = if (standings.size > 1) standings[1].name else "N/A"
        } else {
            val finalMatch = matches.find { it.stage == "FINAL" }
            if (finalMatch != null) {
                if (finalMatch.scoreA > finalMatch.scoreB) {
                    championName = finalMatch.teamAName
                    runnerUpName = finalMatch.teamBName
                } else {
                    championName = finalMatch.teamBName
                    runnerUpName = finalMatch.teamAName
                }
            }
        }

        findViewById<TextView>(R.id.tvChampionName).text = championName
        findViewById<TextView>(R.id.tvRunnerUp).text = "Runner-up: $runnerUpName"

        // Stats rows
        setStatRow(R.id.rowFormat, "Format", if (t.format == "LEAGUE") "League" else "Group + Knockout")
        setStatRow(R.id.rowTeams, "Total Teams", teams.size.toString())
        setStatRow(R.id.rowMatches, "Total Matches", matches.size.toString())
        setStatRow(R.id.rowGoals, "Total Goals", completed.sumOf { it.scoreA + it.scoreB }.toString())
        
        val dateStr = if (t.completionDate > 0) {
            SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(t.completionDate))
        } else "N/A"
        setStatRow(R.id.rowDate, "Completed On", dateStr)

        // Awards
        val teamStats = teams.associate { it.teamId to TeamAwardStats(it.name, it.teamId) }
        completed.forEach { m ->
            teamStats[m.teamAId]?.add(m.scoreA, m.scoreB)
            teamStats[m.teamBId]?.add(m.scoreB, m.scoreA)
        }

        val bestAttack = teamStats.values.maxByOrNull { it.gf }
        // Best defense: least conceded, only for teams who played matches
        val bestDefense = teamStats.values.filter { stats -> 
            completed.any { it.teamAId == stats.id || it.teamBId == stats.id }
        }.minByOrNull { it.ga }
        val highestScoring = completed.maxByOrNull { it.scoreA + it.scoreB }

        setStatRow(R.id.rowBestAttack, "Best Attack", "${bestAttack?.name ?: "N/A"} (${bestAttack?.gf ?: 0} goals)")
        setStatRow(R.id.rowBestDefense, "Best Defense", "${bestDefense?.name ?: "N/A"} (${bestDefense?.ga ?: 0} conceded)")
        
        val hsText = if (highestScoring != null) {
            "${highestScoring.teamAName} ${highestScoring.scoreA}-${highestScoring.scoreB} ${highestScoring.teamBName}"
        } else "N/A"
        setStatRow(R.id.rowHighestScoring, "Highest Scoring", hsText)
    }

    private fun setStatRow(viewId: Int, label: String, value: String) {
        val row = findViewById<View>(viewId)
        row.findViewById<TextView>(R.id.tvLabel).text = label
        row.findViewById<TextView>(R.id.tvValue).text = value
    }

    private fun calculateStandings(teamList: List<Team>, matchList: List<Match>): List<TeamStat> {
        val statsMap = teamList.associate { it.teamId to TeamStat(it.name) }
        matchList.forEach { m ->
            val sA = statsMap[m.teamAId]
            val sB = statsMap[m.teamBId]
            if (sA != null && sB != null) {
                sA.gf += m.scoreA
                sA.ga += m.scoreB
                sB.gf += m.scoreB
                sB.ga += m.scoreA
                if (m.scoreA > m.scoreB) sA.pts += 3
                else if (m.scoreB > m.scoreA) sB.pts += 3
                else { sA.pts += 1; sB.pts += 1 }
            }
        }
        return statsMap.values.sortedWith(compareByDescending<TeamStat> { it.pts }.thenByDescending { it.gf - it.ga }.thenByDescending { it.gf })
    }

    class TeamStat(val name: String) {
        var pts = 0
        var gf = 0
        var ga = 0
    }

    class TeamAwardStats(val name: String, val id: String) {
        var gf = 0
        var ga = 0
        fun add(scored: Int, conceded: Int) {
            gf += scored
            ga += conceded
        }
    }
}