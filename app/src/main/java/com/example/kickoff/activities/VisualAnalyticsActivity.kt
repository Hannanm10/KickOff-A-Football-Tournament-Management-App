package com.example.kickoff.activities

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import com.example.kickoff.R
import com.example.kickoff.models.Match
import com.example.kickoff.models.Team
import com.example.kickoff.repositories.MatchRepository
import com.example.kickoff.repositories.TeamRepository
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.database.ValueEventListener

class VisualAnalyticsActivity : AppCompatActivity() {

    private lateinit var tournamentId: String
    private lateinit var progressBar: ProgressBar
    
    private lateinit var chartPoints: BarChart
    private lateinit var chartGoals: BarChart
    private lateinit var chartProgress: PieChart
    private lateinit var chartResults: PieChart

    private var teams = listOf<Team>()
    private var matches = listOf<Match>()
    
    private var teamListener: ValueEventListener? = null
    private var matchListener: ValueEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_visual_analytics)

        tournamentId = intent.getStringExtra("tournamentId") ?: ""
        val tournamentName = intent.getStringExtra("tournamentName") ?: ""

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }
        toolbar.title = "Visuals: $tournamentName"

        initUI()
        loadData()
    }

    private fun initUI() {
        progressBar = findViewById(R.id.progressBar)
        chartPoints = findViewById(R.id.chartPoints)
        chartGoals = findViewById(R.id.chartGoals)
        chartProgress = findViewById(R.id.chartProgress)
        chartResults = findViewById(R.id.chartResults)
        
        setupChartStyle(chartPoints)
        setupChartStyle(chartGoals)
    }

    private fun setupChartStyle(chart: BarChart) {
        chart.setDrawGridBackground(false)
        chart.setDrawBarShadow(false)
        chart.setDrawValueAboveBar(true)
        chart.description.isEnabled = false
        chart.legend.isEnabled = false
        
        val xAxis = chart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.granularity = 1f
        xAxis.textColor = getColor(R.color.primaryMaroon)
        
        chart.axisLeft.textColor = getColor(R.color.primaryMaroon)
        chart.axisRight.isEnabled = false
    }

    private fun loadData() {
        progressBar.visibility = View.VISIBLE
        
        teamListener = TeamRepository.getTeamsByTournament(tournamentId) { list ->
            teams = list
            updateCharts()
        }
        
        matchListener = MatchRepository.getMatchesByTournament(tournamentId) { list ->
            matches = list
            updateCharts()
        }
    }

    private fun updateCharts() {
        if (teams.isEmpty()) return
        progressBar.visibility = View.GONE
        
        val completedMatches = matches.filter { it.status == "COMPLETED" }
        
        class TeamStats(val name: String) {
            var pts = 0
            var gf = 0
            var wins = 0
            var draws = 0
            var losses = 0
        }

        val statsMap = teams.associate { it.teamId to TeamStats(it.name) }

        completedMatches.forEach { m ->
            val sA = statsMap[m.teamAId]
            val sB = statsMap[m.teamBId]
            
            if (sA != null && sB != null) {
                sA.gf += m.scoreA
                sB.gf += m.scoreB
                
                when {
                    m.scoreA > m.scoreB -> { sA.wins++; sA.pts += 3; sB.losses++ }
                    m.scoreB > m.scoreA -> { sB.wins++; sB.pts += 3; sA.losses++ }
                    else -> { sA.draws++; sA.pts += 1; sB.draws++; sB.pts += 1 }
                }
            }
        }

        val sortedByPts = statsMap.values.sortedByDescending { it.pts }
        
        // 1. Points Distribution
        val ptsEntries = sortedByPts.mapIndexed { i, s -> BarEntry(i.toFloat(), s.pts.toFloat()) }
        val ptsDataSet = BarDataSet(ptsEntries, "Points")
        ptsDataSet.color = getColor(R.color.primaryMaroon)
        ptsDataSet.valueTextColor = getColor(R.color.primaryMaroon)
        chartPoints.data = BarData(ptsDataSet)
        chartPoints.xAxis.valueFormatter = IndexAxisValueFormatter(sortedByPts.map { it.name })
        chartPoints.animateY(1000)
        chartPoints.invalidate()

        // 2. Goals Scored
        val sortedByGf = statsMap.values.sortedByDescending { it.gf }
        val gfEntries = sortedByGf.mapIndexed { i, s -> BarEntry(i.toFloat(), s.gf.toFloat()) }
        val gfDataSet = BarDataSet(gfEntries, "Goals")
        gfDataSet.color = getColor(R.color.primaryMaroon)
        gfDataSet.valueTextColor = getColor(R.color.primaryMaroon)
        chartGoals.data = BarData(gfDataSet)
        chartGoals.xAxis.valueFormatter = IndexAxisValueFormatter(sortedByGf.map { it.name })
        chartGoals.animateY(1000)
        chartGoals.invalidate()

        // 3. Tournament Progress
        val progressEntries = listOf(
            PieEntry(completedMatches.size.toFloat(), "Done"),
            PieEntry((matches.size - completedMatches.size).toFloat(), "Left")
        )
        val progressDataSet = PieDataSet(progressEntries, "")
        progressDataSet.colors = listOf(getColor(R.color.primaryMaroon), Color.LTGRAY)
        chartProgress.data = PieData(progressDataSet)
        chartProgress.description.isEnabled = false
        chartProgress.legend.isEnabled = false
        chartProgress.holeRadius = 40f
        chartProgress.setHoleColor(Color.TRANSPARENT)
        chartProgress.animateXY(1000, 1000)
        chartProgress.invalidate()

        // 4. Overall Results (W/D/L)
        val totalW = statsMap.values.sumOf { it.wins }
        val totalD = statsMap.values.sumOf { it.draws } / 2 // Each draw counted twice
        val totalL = statsMap.values.sumOf { it.losses }
        
        val resultsEntries = listOf(
            PieEntry(totalW.toFloat(), "Wins"),
            PieEntry(totalD.toFloat(), "Draws"),
            PieEntry(totalL.toFloat(), "Losses")
        )
        val resultsDataSet = PieDataSet(resultsEntries, "")
        resultsDataSet.colors = listOf(Color.parseColor("#4CAF50"), Color.parseColor("#9E9E9E"), Color.parseColor("#F44336"))
        chartResults.data = PieData(resultsDataSet)
        chartResults.description.isEnabled = false
        chartResults.legend.isEnabled = false
        chartResults.holeRadius = 40f
        chartResults.setHoleColor(Color.TRANSPARENT)
        chartResults.animateXY(1000, 1000)
        chartResults.invalidate()
    }

    override fun onDestroy() {
        super.onDestroy()
        teamListener?.let { TeamRepository.removeListener(it) }
        matchListener?.let { MatchRepository.removeListener(it) }
    }
}