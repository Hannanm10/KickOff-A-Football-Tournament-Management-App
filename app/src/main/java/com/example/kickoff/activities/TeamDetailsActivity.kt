package com.example.kickoff.activities

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kickoff.R
import com.example.kickoff.adapters.MatchAdapter
import com.example.kickoff.models.Match
import com.example.kickoff.models.Tournament
import com.example.kickoff.repositories.MatchRepository
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class TeamDetailsActivity : AppCompatActivity() {

    private lateinit var teamId: String
    private lateinit var teamName: String
    private lateinit var tournamentId: String
    private var tournamentName: String = ""

    private lateinit var progressBar: ProgressBar
    private lateinit var rvHistory: RecyclerView
    private lateinit var adapter: MatchAdapter
    private val historyList = mutableListOf<Match>()
    private var matchListener: ValueEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_team_details)

        teamId = intent.getStringExtra("teamId") ?: ""
        teamName = intent.getStringExtra("teamName") ?: ""
        tournamentId = intent.getStringExtra("tournamentId") ?: ""
        tournamentName = intent.getStringExtra("tournamentName") ?: ""

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }
        toolbar.title = "Team Performance"

        initUI()
        
        if (tournamentName.isEmpty()) {
            fetchTournamentName()
        }
        
        loadMatches()
    }

    private fun initUI() {
        progressBar = findViewById(R.id.progressBar)
        findViewById<TextView>(R.id.tvTeamNameHeader).text = teamName
        findViewById<TextView>(R.id.tvTournamentNameHeader).text = tournamentName

        rvHistory = findViewById(R.id.rvMatchHistory)
        adapter = MatchAdapter(historyList, tournamentId)
        rvHistory.layoutManager = LinearLayoutManager(this)
        rvHistory.adapter = adapter

        // Set Labels for include cards
        setupStatCard(R.id.cardPlayed, "Matches Played")
        setupStatCard(R.id.cardWins, "Wins")
        setupStatCard(R.id.cardDraws, "Draws")
        setupStatCard(R.id.cardLosses, "Losses")
        setupStatCard(R.id.cardGF, "Goals For")
        setupStatCard(R.id.cardGA, "Goals Against")
        setupStatCard(R.id.cardGD, "Goal Difference")
        setupStatCard(R.id.cardPoints, "Points")
        setupStatCard(R.id.cardWinRate, "Win %")
    }

    private fun fetchTournamentName() {
        FirebaseDatabase.getInstance().getReference("tournaments").child(tournamentId).get()
            .addOnSuccessListener { snapshot ->
                val tournament = snapshot.getValue(Tournament::class.java)
                tournamentName = tournament?.name ?: ""
                findViewById<TextView>(R.id.tvTournamentNameHeader).text = tournamentName
            }
    }

    private fun setupStatCard(viewId: Int, label: String) {
        val view = findViewById<View>(viewId)
        view.findViewById<TextView>(R.id.tvStatLabel).text = label
    }

    private fun updateStatValue(viewId: Int, value: String) {
        val view = findViewById<View>(viewId)
        view.findViewById<TextView>(R.id.tvStatValue).text = value
    }

    private fun loadMatches() {
        progressBar.visibility = View.VISIBLE
        matchListener = MatchRepository.getMatchesByTournament(tournamentId) { allMatches ->
            progressBar.visibility = View.GONE
            
            val teamMatches = allMatches.filter { 
                (it.teamAId == teamId || it.teamBId == teamId) && it.status == "COMPLETED" 
            }.sortedByDescending { it.matchDate }

            calculateStats(teamMatches)
            
            historyList.clear()
            historyList.addAll(teamMatches)
            adapter.notifyDataSetChanged()
            
            findViewById<View>(R.id.tvEmptyHistory).visibility = if (teamMatches.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun calculateStats(matches: List<Match>) {
        var played = matches.size
        var wins = 0
        var draws = 0
        var losses = 0
        var gf = 0
        var ga = 0
        val form = mutableListOf<String>()

        matches.forEach { m ->
            val isTeamA = m.teamAId == teamId
            val teamScore = if (isTeamA) m.scoreA else m.scoreB
            val oppScore = if (isTeamA) m.scoreB else m.scoreA

            gf += teamScore
            ga += oppScore

            when {
                teamScore > oppScore -> {
                    wins++
                    if (form.size < 5) form.add("W")
                }
                teamScore < oppScore -> {
                    losses++
                    if (form.size < 5) form.add("L")
                }
                else -> {
                    draws++
                    if (form.size < 5) form.add("D")
                }
            }
        }

        val pts = (wins * 3) + draws
        val gd = gf - ga
        val winRate = if (played > 0) (wins * 100 / played) else 0

        updateStatValue(R.id.cardPlayed, played.toString())
        updateStatValue(R.id.cardWins, wins.toString())
        updateStatValue(R.id.cardDraws, draws.toString())
        updateStatValue(R.id.cardLosses, losses.toString())
        updateStatValue(R.id.cardGF, gf.toString())
        updateStatValue(R.id.cardGA, ga.toString())
        updateStatValue(R.id.cardGD, (if (gd > 0) "+" else "") + gd.toString())
        updateStatValue(R.id.cardPoints, pts.toString())
        updateStatValue(R.id.cardWinRate, "$winRate%")

        displayForm(form)
    }

    private fun displayForm(form: List<String>) {
        val layout = findViewById<LinearLayout>(R.id.layoutForm)
        layout.removeAllViews()
        
        form.reversed().forEach { result ->
            val view = LayoutInflater.from(this).inflate(R.layout.item_form_dot, layout, false) as TextView
            view.text = result
            val color = when(result) {
                "W" -> "#4CAF50"
                "L" -> "#F44336"
                else -> "#9E9E9E"
            }
            view.background.setTint(android.graphics.Color.parseColor(color))
            layout.addView(view)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        matchListener?.let { MatchRepository.removeListener(it) }
    }
}