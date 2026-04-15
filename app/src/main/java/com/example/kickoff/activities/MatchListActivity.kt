package com.example.kickoff.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kickoff.R
import com.example.kickoff.adapters.MatchAdapter
import com.example.kickoff.models.Match
import com.example.kickoff.utils.MatchStorage
import com.example.kickoff.utils.SessionManager

class MatchListActivity : AppCompatActivity() {

    private lateinit var list: MutableList<Match>
    private lateinit var adapter: MatchAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_match_list)

        val recycler = findViewById<RecyclerView>(R.id.recyclerMatches)
        val btnAdd = findViewById<Button>(R.id.btnAddMatch)

        val tournament = intent.getStringExtra("tournament") ?: ""
        val organizer = intent.getStringExtra("organizer") ?: ""
        val currentUser = SessionManager.getUser(this) ?: ""

        val isOrganizer = currentUser == organizer

        if (!isOrganizer) btnAdd.visibility = View.GONE

        list = MatchStorage.getMatches(this, tournament)

        adapter = MatchAdapter(list)

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

        list.clear()
        list.addAll(MatchStorage.getMatches(this, tournament))
        adapter.notifyDataSetChanged()
    }
}