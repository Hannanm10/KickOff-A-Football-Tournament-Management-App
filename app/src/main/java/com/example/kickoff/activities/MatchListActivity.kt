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
import com.example.kickoff.repositories.TeamRepository
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class MatchListActivity : AppCompatActivity() {

    private var list = mutableListOf<Match>()
    private lateinit var adapter: MatchAdapter
    private lateinit var tournamentId: String
    private lateinit var tournamentName: String
    private var teamFilterId: String? = null
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmpty: TextView
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

        val recycler = findViewById<RecyclerView>(R.id.recyclerMatches)
        btnAdd = findViewById(R.id.btnAddMatch)
        btnGenerate = findViewById(R.id.btnGenerateFixtures)
        progressBar = findViewById(R.id.progressBar)
        tvEmpty = findViewById(R.id.tvEmpty)

        adapter = MatchAdapter(list, tournamentId)

        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

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
        if (list.isNotEmpty()) {
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
        // We use a one-time fetch for teams here
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
            list.clear()
            if (teamFilterId != null) {
                list.addAll(allMatches.filter { it.teamAId == teamFilterId || it.teamBId == teamFilterId })
            } else {
                list.addAll(allMatches)
            }
            adapter.notifyDataSetChanged()
            updateEmptyState()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        matchListener?.let { MatchRepository.removeListener(it) }
    }

    private fun updateEmptyState() {
        tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
    }
}