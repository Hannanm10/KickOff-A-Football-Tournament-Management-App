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
import com.example.kickoff.adapters.MatchAdapter
import com.example.kickoff.models.Match
import com.example.kickoff.repositories.MatchRepository
import com.google.android.material.appbar.MaterialToolbar
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
        val btnAdd = findViewById<FloatingActionButton>(R.id.btnAddMatch)
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

        checkPermissions(btnAdd)
        loadMatches()
    }

    private fun checkPermissions(btnAdd: FloatingActionButton) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseDatabase.getInstance().getReference("tournaments").child(tournamentId).get()
            .addOnSuccessListener { snapshot ->
                val organizerId = snapshot.child("organizerId").getValue(String::class.java)
                if (currentUserId != organizerId || teamFilterId != null) {
                    btnAdd.visibility = View.GONE
                }
            }
    }

    private fun loadMatches() {
        progressBar.visibility = View.VISIBLE
        MatchRepository.getMatchesByTournament(tournamentId) { allMatches ->
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

    private fun updateEmptyState() {
        tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
    }
}