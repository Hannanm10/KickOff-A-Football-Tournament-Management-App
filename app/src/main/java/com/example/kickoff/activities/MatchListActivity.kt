package com.example.kickoff.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kickoff.R
import com.example.kickoff.adapters.MatchAdapter
import com.example.kickoff.models.Match
import com.example.kickoff.models.Team
import com.example.kickoff.models.Tournament
import com.example.kickoff.repositories.MatchRepository
import com.example.kickoff.repositories.TeamRepository
import com.example.kickoff.repositories.TournamentRepository
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class MatchListActivity : AppCompatActivity() {

    private var upcomingList = mutableListOf<Match>()
    private var completedList = mutableListOf<Match>()
    
    private lateinit var upcomingAdapter: MatchAdapter
    private lateinit var completedAdapter: MatchAdapter
    
    private lateinit var tournamentId: String
    private lateinit var tournamentName: String
    private var teamFilterId: String? = null
    private var tournament: Tournament? = null
    
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmptyUpcoming: TextView
    private lateinit var tvEmptyCompleted: TextView
    private lateinit var tvUpcomingCount: TextView
    private lateinit var tvCompletedCount: TextView
    
    private var matchListener: com.google.firebase.database.ValueEventListener? = null
    
    private lateinit var btnAdd: FloatingActionButton
    private lateinit var btnGenerate: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_match_list)

        tournamentId = intent.getStringExtra("tournamentId") ?: ""
        tournamentName = intent.getStringExtra("tournamentName") ?: ""
        teamFilterId = intent.getStringExtra("team_filter_id")

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }
        toolbar.title = if (teamFilterId != null) "Matches" else "Matches: $tournamentName"

        initUI()
        setupRecyclers()

        btnAdd.setOnClickListener {
            val intent = Intent(this, AddMatchActivity::class.java)
            intent.putExtra("tournamentId", tournamentId)
            startActivity(intent)
        }

        btnGenerate.setOnClickListener {
            checkAndGenerateFixtures()
        }

        loadTournamentAndPermissions()
        loadMatches()
    }

    private fun initUI() {
        btnAdd = findViewById(R.id.btnAddMatch)
        btnGenerate = findViewById(R.id.btnGenerateFixtures)
        progressBar = findViewById(R.id.progressBar)
        tvEmptyUpcoming = findViewById(R.id.tvEmptyUpcoming)
        tvEmptyCompleted = findViewById(R.id.tvEmptyCompleted)
        tvUpcomingCount = findViewById(R.id.tvUpcomingCount)
        tvCompletedCount = findViewById(R.id.tvCompletedCount)
    }

    private fun setupRecyclers() {
        val recyclerUpcoming = findViewById<RecyclerView>(R.id.recyclerUpcoming)
        val recyclerCompleted = findViewById<RecyclerView>(R.id.recyclerCompleted)

        upcomingAdapter = MatchAdapter(upcomingList, tournamentId)
        completedAdapter = MatchAdapter(completedList, tournamentId)

        recyclerUpcoming.layoutManager = LinearLayoutManager(this)
        recyclerUpcoming.adapter = upcomingAdapter

        recyclerCompleted.layoutManager = LinearLayoutManager(this)
        recyclerCompleted.adapter = completedAdapter
    }

    private fun loadTournamentAndPermissions() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseDatabase.getInstance().getReference("tournaments").child(tournamentId).get()
            .addOnSuccessListener { snapshot ->
                tournament = snapshot.getValue(Tournament::class.java)
                val organizerId = tournament?.organizerId
                
                if (currentUserId == organizerId && teamFilterId == null) {
                    btnAdd.visibility = View.VISIBLE
                    btnGenerate.visibility = View.VISIBLE
                    updateGenerateButtonLabel()
                } else {
                    btnAdd.visibility = View.GONE
                    btnGenerate.visibility = View.GONE
                }
            }
    }

    private fun updateGenerateButtonLabel() {
        val t = tournament ?: return
        if (t.format == "LEAGUE") {
            btnGenerate.text = "Generate League Fixtures"
            return
        }

        // GROUP_KNOCKOUT logic
        val allMatches = upcomingList + completedList
        if (allMatches.isEmpty()) {
            btnGenerate.text = "Generate Group Fixtures"
            return
        }

        val groupMatches = allMatches.filter { it.stage == "GROUP" }
        val semiMatches = allMatches.filter { it.stage == "SEMI_FINAL" }
        val finalMatches = allMatches.filter { it.stage == "FINAL" }

        if (groupMatches.isNotEmpty() && groupMatches.all { it.status == "COMPLETED" } && semiMatches.isEmpty()) {
            btnGenerate.text = "Generate Semi-Finals"
            btnGenerate.visibility = View.VISIBLE
        } else if (semiMatches.isNotEmpty() && semiMatches.all { it.status == "COMPLETED" } && finalMatches.isEmpty()) {
            btnGenerate.text = "Generate Final"
            btnGenerate.visibility = View.VISIBLE
        } else if (finalMatches.isNotEmpty() && finalMatches.all { it.status == "COMPLETED" }) {
            btnGenerate.text = "Tournament Completed"
            btnGenerate.isEnabled = false
        } else {
            // Either stage in progress or already generated
            if (groupMatches.isNotEmpty() || semiMatches.isNotEmpty() || finalMatches.isNotEmpty()) {
                btnGenerate.visibility = View.GONE
            }
        }
    }

    private fun checkAndGenerateFixtures() {
        val label = btnGenerate.text.toString()
        if (label == "Generate Semi-Finals") {
            generateSemiFinals()
            return
        }
        if (label == "Generate Final") {
            generateFinal()
            return
        }

        val hasMatches = upcomingList.isNotEmpty() || completedList.isNotEmpty()
        if (hasMatches) {
            AlertDialog.Builder(this)
                .setTitle("Regenerate Fixtures?")
                .setMessage("Existing matches found. Do you want to clear them and generate a fresh schedule, or just add new ones?")
                .setPositiveButton("Clear & Generate") { _, _ -> 
                    MatchRepository.deleteAllMatchesInTournament(tournamentId) { success, _ ->
                        if (success) {
                            TournamentRepository.setChampion(tournamentId, "") { }
                            fetchTeamsAndGenerate()
                        }
                    }
                }
                .setNeutralButton("Add New Only") { _, _ -> fetchTeamsAndGenerate() }
                .setNegativeButton("Cancel", null)
                .show()
        } else {
            fetchTeamsAndGenerate()
        }
    }

    private fun generateSemiFinals() {
        progressBar.visibility = View.VISIBLE
        TeamRepository.getTeamsOnce(tournamentId) { teams ->
            val allMatches = upcomingList + completedList
            val groupAStats = calculateStandings(teams.filter { it.groupName == "A" }, allMatches.filter { it.stage == "GROUP" })
            val groupBStats = calculateStandings(teams.filter { it.groupName == "B" }, allMatches.filter { it.stage == "GROUP" })

            if (groupAStats.size < 2 || groupBStats.size < 2) {
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Error calculating standings", Toast.LENGTH_SHORT).show()
                return@getTeamsOnce
            }

            val winnerA = teams.find { it.name == groupAStats[0].name }!!
            val runnerA = teams.find { it.name == groupAStats[1].name }!!
            val winnerB = teams.find { it.name == groupBStats[0].name }!!
            val runnerB = teams.find { it.name == groupBStats[1].name }!!

            MatchRepository.generateKnockoutMatch(tournamentId, winnerA, runnerB, "SEMI_FINAL") { s1, _ ->
                if (s1) {
                    MatchRepository.generateKnockoutMatch(tournamentId, winnerB, runnerA, "SEMI_FINAL") { s2, _ ->
                        progressBar.visibility = View.GONE
                        if (s2) Toast.makeText(this, "Semi-Finals generated", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun generateFinal() {
        progressBar.visibility = View.VISIBLE
        TeamRepository.getTeamsOnce(tournamentId) { teams ->
            val semiMatches = completedList.filter { it.stage == "SEMI_FINAL" }
            if (semiMatches.size < 2) return@getTeamsOnce

            val winners = semiMatches.map { m ->
                if (m.scoreA > m.scoreB) teams.find { it.teamId == m.teamAId }!!
                else teams.find { it.teamId == m.teamBId }!!
            }

            MatchRepository.generateKnockoutMatch(tournamentId, winners[0], winners[1], "FINAL") { success, _ ->
                progressBar.visibility = View.GONE
                if (success) Toast.makeText(this, "Final generated", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun calculateStandings(groupTeams: List<Team>, groupMatches: List<Match>): List<TeamStat> {
        val statsMap = groupTeams.associate { it.teamId to TeamStat(it.name) }
        groupMatches.forEach { m ->
            val sA = statsMap[m.teamAId]
            val sB = statsMap[m.teamBId]
            if (sA != null && sB != null) {
                sA.gf += m.scoreA
                sA.ga += m.scoreB
                sB.gf += m.scoreB
                sB.ga += m.scoreA
                when {
                    m.scoreA > m.scoreB -> { sA.pts += 3 }
                    m.scoreB > m.scoreA -> { sB.pts += 3 }
                    else -> { sA.pts += 1; sB.pts += 1 }
                }
            }
        }
        return statsMap.values.sortedWith(compareByDescending<TeamStat> { it.pts }.thenByDescending { it.gf - it.ga }.thenByDescending { it.gf })
    }

    class TeamStat(val name: String) {
        var pts = 0
        var gf = 0
        var ga = 0
    }

    private fun fetchTeamsAndGenerate() {
        if (tournament == null) return
        progressBar.visibility = View.VISIBLE
        
        TeamRepository.getTeamsOnce(tournamentId) { teams ->
            if (tournament?.format == "GROUP_KNOCKOUT") {
                if (teams.size < 6 || teams.size % 2 != 0) {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this, "Group format requires even team count (min 6)", Toast.LENGTH_LONG).show()
                    return@getTeamsOnce
                }
                
                // Shuffle and Split
                val shuffled = teams.shuffled()
                val mid = shuffled.size / 2
                shuffled.forEachIndexed { index, team ->
                    team.groupName = if (index < mid) "A" else "B"
                }
                
                // Update groups then generate
                TeamRepository.updateTeamGroups(shuffled) { success ->
                    if (success) {
                        MatchRepository.generateGroupStageFixtures(tournamentId, shuffled) { mSuccess, error ->
                            progressBar.visibility = View.GONE
                            if (mSuccess) Toast.makeText(this, "Group fixtures generated", Toast.LENGTH_SHORT).show()
                            else Toast.makeText(this, "Error: $error", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        progressBar.visibility = View.GONE
                        Toast.makeText(this, "Error updating groups", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                // LEAGUE format
                if (teams.size < 2) {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this, "At least 2 teams required", Toast.LENGTH_SHORT).show()
                    return@getTeamsOnce
                }
                MatchRepository.generateLeagueFixtures(tournamentId, teams) { success, error ->
                    progressBar.visibility = View.GONE
                    if (success) Toast.makeText(this, "League fixtures generated", Toast.LENGTH_SHORT).show()
                    else Toast.makeText(this, "Error: $error", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun loadMatches() {
        progressBar.visibility = View.VISIBLE
        matchListener = MatchRepository.getMatchesByTournament(tournamentId) { allMatches ->
            progressBar.visibility = View.GONE
            
            val filteredMatches = if (teamFilterId != null) {
                allMatches.filter { it.teamAId == teamFilterId || it.teamBId == teamFilterId }
            } else {
                allMatches
            }

            upcomingList.clear()
            upcomingList.addAll(filteredMatches.filter { it.status == "UPCOMING" }.sortedBy { it.matchDate })

            completedList.clear()
            completedList.addAll(filteredMatches.filter { it.status == "COMPLETED" }.sortedByDescending { it.matchDate })

            upcomingAdapter.notifyDataSetChanged()
            completedAdapter.notifyDataSetChanged()

            updateUIStates()
            updateGenerateButtonLabel()
            checkAndSetChampion(allMatches)
        }
    }

    private fun checkAndSetChampion(matches: List<Match>) {
        val t = tournament ?: return
        if (t.status == "COMPLETED") return // Already completed
        if (matches.isEmpty()) return
        if (!matches.all { it.status == "COMPLETED" }) return

        if (t.format == "LEAGUE") {
            TeamRepository.getTeamsOnce(tournamentId) { teams ->
                val standings = calculateStandings(teams, matches)
                val leaderName = standings.firstOrNull()?.name
                val leaderId = teams.find { it.name == leaderName }?.teamId
                if (leaderId != null) {
                    TournamentRepository.completeTournament(tournamentId, leaderId) { }
                }
            }
        } else {
            // GROUP_KNOCKOUT
            val finalMatch = matches.find { it.stage == "FINAL" }
            if (finalMatch != null && finalMatch.status == "COMPLETED") {
                val winnerId = if (finalMatch.scoreA > finalMatch.scoreB) finalMatch.teamAId else finalMatch.teamBId
                TournamentRepository.completeTournament(tournamentId, winnerId) { }
            }
        }
    }

    private fun updateUIStates() {
        tvUpcomingCount.text = "(${upcomingList.size})"
        tvCompletedCount.text = "(${completedList.size})"
        
        tvEmptyUpcoming.visibility = if (upcomingList.isEmpty()) View.VISIBLE else View.GONE
        tvEmptyCompleted.visibility = if (completedList.isEmpty()) View.VISIBLE else View.GONE
    }

    override fun onDestroy() {
        super.onDestroy()
        matchListener?.let { MatchRepository.removeListener(it) }
    }
}