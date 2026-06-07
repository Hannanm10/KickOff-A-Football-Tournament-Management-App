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
import com.example.kickoff.repositories.MatchRepository
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

        checkPermissions()
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

    private fun checkPermissions() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseDatabase.getInstance().getReference("tournaments").child(tournamentId).get()
            .addOnSuccessListener { snapshot ->
                val organizerId = snapshot.child("organizerId").getValue(String::class.java)
                if (currentUserId == organizerId && teamFilterId == null) {
                    btnAdd.visibility = View.VISIBLE
                    btnGenerate.visibility = View.VISIBLE
                } else {
                    btnAdd.visibility = View.GONE
                    btnGenerate.visibility = View.GONE
                }
            }
    }

    private fun checkAndGenerateFixtures() {
        val hasMatches = upcomingList.isNotEmpty() || completedList.isNotEmpty()
        if (hasMatches) {
            AlertDialog.Builder(this)
                .setTitle("Regenerate Fixtures?")
                .setMessage("Existing matches found. Do you want to clear them and generate a fresh schedule, or just add new ones?")
                .setPositiveButton("Clear & Generate") { _, _ -> 
                    MatchRepository.deleteAllMatchesInTournament(tournamentId) { success, _ ->
                        if (success) fetchTeamsAndGenerate()
                    }
                }
                .setNeutralButton("Add New Only") { _, _ -> fetchTeamsAndGenerate() }
                .setNegativeButton("Cancel", null)
                .show()
        } else {
            fetchTeamsAndGenerate()
        }
    }

    private fun fetchTeamsAndGenerate() {
        progressBar.visibility = View.VISIBLE
        FirebaseDatabase.getInstance().getReference("teams")
            .orderByChild("tournamentId").equalTo(tournamentId).get()
            .addOnSuccessListener { snapshot ->
                val teams = mutableListOf<Team>()
                snapshot.children.forEach { 
                    it.getValue(Team::class.java)?.let { team -> teams.add(team) }
                }
                
                if (teams.size < 2) {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this, "Need at least 2 teams to generate fixtures", Toast.LENGTH_SHORT).show()
                } else {
                    MatchRepository.generateRoundRobinFixtures(tournamentId, teams) { success, error ->
                        progressBar.visibility = View.GONE
                        if (success) {
                            Toast.makeText(this, "Fixtures generated successfully", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, "Error: $error", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            .addOnFailureListener {
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Error fetching teams: ${it.message}", Toast.LENGTH_SHORT).show()
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