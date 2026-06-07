package com.example.kickoff.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kickoff.R
import com.example.kickoff.adapters.TeamAdapter
import com.example.kickoff.models.Team
import com.example.kickoff.repositories.TeamRepository
import com.example.kickoff.repositories.TournamentRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton

class TeamListActivity : AppCompatActivity() {

    private var teamList = mutableListOf<Team>()
    private lateinit var adapter: TeamAdapter

    private lateinit var tournamentId: String
    private lateinit var tournamentName: String
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmpty: TextView
    private var teamListener: com.google.firebase.database.ValueEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_team_list)

        tournamentId = intent.getStringExtra("tournamentId") ?: ""
        tournamentName = intent.getStringExtra("tournamentName") ?: ""

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }
        toolbar.title = "Teams: $tournamentName"

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerTeams)
        val btnAdd = findViewById<FloatingActionButton>(R.id.btnAddTeam)
        progressBar = findViewById(R.id.progressBar)
        tvEmpty = findViewById(R.id.tvEmpty)

        adapter = TeamAdapter(teamList, tournamentId)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        btnAdd.setOnClickListener {
            val intent = Intent(this, AddTeamActivity::class.java)
            intent.putExtra("tournamentId", tournamentId)
            intent.putExtra("tournamentName", tournamentName) // Added this
            startActivity(intent)
        }

        checkPermissions(btnAdd)
        loadTeams()
    }

    private fun checkPermissions(btnAdd: FloatingActionButton) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseDatabase.getInstance().getReference("tournaments").child(tournamentId).get()
            .addOnSuccessListener { snapshot ->
                val organizerId = snapshot.child("organizerId").getValue(String::class.java)
                if (currentUserId != organizerId) {
                    btnAdd.visibility = View.GONE
                }
            }
    }

    private fun loadTeams() {
        progressBar.visibility = View.VISIBLE
        teamListener = TeamRepository.getTeamsByTournament(tournamentId) { list ->
            progressBar.visibility = View.GONE
            teamList.clear()
            teamList.addAll(list)
            adapter.notifyDataSetChanged()
            updateEmptyState()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        teamListener?.let { TeamRepository.removeListener(it) }
    }

    private fun updateEmptyState() {
        tvEmpty.visibility = if (teamList.isEmpty()) View.VISIBLE else View.GONE
    }
}