package com.example.kickoff.activities

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.kickoff.R
import com.example.kickoff.adapters.LeaderboardAdapter
import com.example.kickoff.models.LeaderboardEntry
import com.example.kickoff.models.Match
import com.example.kickoff.models.Team
import com.example.kickoff.models.Tournament
import com.example.kickoff.repositories.MatchRepository
import com.example.kickoff.repositories.TeamRepository
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.database.FirebaseDatabase

class LeaderboardActivity : AppCompatActivity() {

    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var tournamentId: String
    private lateinit var tournamentName: String
    private var tournament: Tournament? = null

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
        toolbar.title = "Standings: $tournamentName"

        progressBar = findViewById(R.id.progressBar)
        swipeRefresh = findViewById(R.id.swipeRefresh)
        swipeRefresh.setOnRefreshListener {
            loadData()
        }

        loadTournament()
        loadData()
    }

    private fun loadTournament() {
        FirebaseDatabase.getInstance().getReference("tournaments").child(tournamentId).get()
            .addOnSuccessListener { snapshot ->
                tournament = snapshot.getValue(Tournament::class.java)
                calculateAndDisplay()
            }
    }

    private fun loadData() {
        progressBar.visibility = View.VISIBLE
        
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
        if (teams.isEmpty() || tournament == null) {
            progressBar.visibility = if (teams.isEmpty()) View.VISIBLE else View.GONE
            return
        }

        progressBar.visibility = View.GONE
        
        if (tournament?.format == "GROUP_KNOCKOUT") {
            showGroupStandings()
        } else {
            showLeagueStandings()
        }
        
        swipeRefresh.isRefreshing = false
    }

    private fun showLeagueStandings() {
        val rv = findViewById<RecyclerView>(R.id.rvLeaderboard)
        val header = findViewById<View>(R.id.headerLeague)
        
        rv.visibility = View.VISIBLE
        header.visibility = View.VISIBLE
        
        findViewById<View>(R.id.tvGroupALabel).visibility = View.GONE
        findViewById<View>(R.id.headerGroupA).visibility = View.GONE
        findViewById<View>(R.id.rvLeaderboardA).visibility = View.GONE
        
        findViewById<View>(R.id.tvGroupBLabel).visibility = View.GONE
        findViewById<View>(R.id.headerGroupB).visibility = View.GONE
        findViewById<View>(R.id.rvLeaderboardB).visibility = View.GONE

        val leaderboard = calculateEntries(teams, matches)
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = LeaderboardAdapter(leaderboard) { openFilter(it) }
    }

    private fun showGroupStandings() {
        findViewById<RecyclerView>(R.id.rvLeaderboard).visibility = View.GONE
        findViewById<View>(R.id.headerLeague).visibility = View.GONE
        
        val tvA = findViewById<TextView>(R.id.tvGroupALabel)
        val headerA = findViewById<View>(R.id.headerGroupA)
        val rvA = findViewById<RecyclerView>(R.id.rvLeaderboardA)
        
        val tvB = findViewById<TextView>(R.id.tvGroupBLabel)
        val headerB = findViewById<View>(R.id.headerGroupB)
        val rvB = findViewById<RecyclerView>(R.id.rvLeaderboardB)

        tvA.visibility = View.VISIBLE
        headerA.visibility = View.VISIBLE
        rvA.visibility = View.VISIBLE
        
        tvB.visibility = View.VISIBLE
        headerB.visibility = View.VISIBLE
        rvB.visibility = View.VISIBLE

        val teamsA = teams.filter { it.groupName == "A" }
        val teamsB = teams.filter { it.groupName == "B" }
        
        val groupAMatches = matches.filter { m -> m.stage == "GROUP" && teamsA.any { it.teamId == m.teamAId } }
        val groupBMatches = matches.filter { m -> m.stage == "GROUP" && teamsB.any { it.teamId == m.teamAId } }

        rvA.layoutManager = LinearLayoutManager(this)
        rvA.adapter = LeaderboardAdapter(calculateEntries(teamsA, groupAMatches)) { openFilter(it) }

        rvB.layoutManager = LinearLayoutManager(this)
        rvB.adapter = LeaderboardAdapter(calculateEntries(teamsB, groupBMatches)) { openFilter(it) }
    }

    private fun calculateEntries(teamList: List<Team>, matchList: List<Match>): List<LeaderboardEntry> {
        return teamList.map { team ->
            val entry = LeaderboardEntry(team.name)
            matchList.forEach { match ->
                if (match.status == "COMPLETED" && (match.teamAId == team.teamId || match.teamBId == team.teamId)) {
                    entry.matchesPlayed++
                    if (match.teamAId == team.teamId) {
                        entry.goalsFor += match.scoreA
                        entry.goalsAgainst += match.scoreB
                        when {
                            match.scoreA > match.scoreB -> { entry.wins++; entry.points += 3 }
                            match.scoreA == match.scoreB -> { entry.draws++; entry.points += 1 }
                            else -> entry.losses++
                        }
                    } else {
                        entry.goalsFor += match.scoreB
                        entry.goalsAgainst += match.scoreA
                        when {
                            match.scoreB > match.scoreA -> { entry.wins++; entry.points += 3 }
                            match.scoreB == match.scoreA -> { entry.draws++; entry.points += 1 }
                            else -> entry.losses++
                        }
                    }
                }
            }
            entry
        }.sortedWith(compareByDescending<LeaderboardEntry> { it.points }
            .thenByDescending { it.goalDifference }
            .thenByDescending { it.goalsFor })
    }

    private fun openFilter(teamName: String) {
        val teamId = teams.find { it.name == teamName }?.teamId
        val intent = android.content.Intent(this, TeamDetailsActivity::class.java)
        intent.putExtra("tournamentId", tournamentId)
        intent.putExtra("tournamentName", tournamentName)
        intent.putExtra("teamId", teamId)
        intent.putExtra("teamName", teamName)
        startActivity(intent)
    }
}