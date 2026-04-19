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
import com.example.kickoff.adapters.MatchAdapter
import com.example.kickoff.models.Match
import com.example.kickoff.utils.MatchStorage
import com.example.kickoff.utils.SessionManager
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MatchListActivity : AppCompatActivity() {

    private lateinit var list: MutableList<Match>
    private lateinit var adapter: MatchAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_match_list)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        val recycler = findViewById<RecyclerView>(R.id.recyclerMatches)
        val btnAdd = findViewById<FloatingActionButton>(R.id.btnAddMatch)
        val tvEmpty = findViewById<TextView>(R.id.tvEmpty)

        val tournament = intent.getStringExtra("tournament") ?: ""
        val teamFilter = intent.getStringExtra("team_filter")
        val organizer = intent.getStringExtra("organizer") ?: ""
        val currentUser = SessionManager.getUser(this) ?: ""

        val isOrganizer = currentUser == organizer

        if (!isOrganizer) btnAdd.visibility = View.GONE

        list = MatchStorage.getMatches(this, tournament)
        
        if (teamFilter != null) {
            toolbar.title = "Matches: $teamFilter"
            list.retainAll { it.teamA == teamFilter || it.teamB == teamFilter }
            btnAdd.visibility = View.GONE // Hide add button in filtered view
        }

        adapter = MatchAdapter(list, organizer)

        if (list.isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
        } else {
            tvEmpty.visibility = View.GONE
        }

        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        btnAdd.setOnClickListener {
            val intent = Intent(this, AddMatchActivity::class.java)
            intent.putExtra("tournament", tournament)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()

        val tournament = intent.getStringExtra("tournament") ?: ""
        val teamFilter = intent.getStringExtra("team_filter")

        list.clear()
        val allMatches = MatchStorage.getMatches(this, tournament)
        if (teamFilter != null) {
            allMatches.retainAll { it.teamA == teamFilter || it.teamB == teamFilter }
        }
        list.addAll(allMatches)
        
        val tvEmpty = findViewById<TextView>(R.id.tvEmpty)
        if (list.isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
        } else {
            tvEmpty.visibility = View.GONE
        }

        adapter.notifyDataSetChanged()
    }
}