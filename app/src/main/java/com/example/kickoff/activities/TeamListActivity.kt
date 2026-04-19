package com.example.kickoff.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kickoff.R
import com.example.kickoff.adapters.TeamAdapter
import com.example.kickoff.models.Team
import com.example.kickoff.utils.SessionManager
import com.example.kickoff.utils.TeamStorage
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton

class TeamListActivity : AppCompatActivity() {

    private lateinit var teamList: MutableList<Team>
    private lateinit var adapter: TeamAdapter

    private lateinit var tournament: String
    private lateinit var organizer: String
    private var currentUser: String = ""

    private fun updateEmptyState() {
        val tvEmpty = findViewById<TextView>(R.id.tvEmpty)
        if (teamList.isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
        } else {
            tvEmpty.visibility = View.GONE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_team_list)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerTeams)
        val btnAdd = findViewById<FloatingActionButton>(R.id.btnAddTeam)

        // Get data from intent
        tournament = intent.getStringExtra("tournament") ?: ""
        organizer = intent.getStringExtra("organizer") ?: ""
        currentUser = SessionManager.getUser(this) ?: ""

        // Permission check
        val isOrganizer = currentUser == organizer

        // Hide button if viewer
        if (!isOrganizer) {
            btnAdd.visibility = View.GONE
        }

        // Load teams
        teamList = TeamStorage.getTeams(this, tournament)

        adapter = TeamAdapter(teamList, organizer, onDataChanged = { updateEmptyState() })

        updateEmptyState()

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // Add team button
        btnAdd.setOnClickListener {
            val intent = Intent(this, AddTeamActivity::class.java)
            intent.putExtra("tournament", tournament)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()

        // Refresh data
        teamList.clear()
        teamList.addAll(TeamStorage.getTeams(this, tournament))
        updateEmptyState()
        adapter.notifyDataSetChanged()
    }
}