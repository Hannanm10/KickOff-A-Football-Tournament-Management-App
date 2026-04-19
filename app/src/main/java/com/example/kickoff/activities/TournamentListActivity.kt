package com.example.kickoff.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kickoff.R
import com.example.kickoff.adapters.TournamentAdapter
import com.example.kickoff.models.Tournament
import com.example.kickoff.utils.SessionManager
import com.example.kickoff.utils.TournamentStorage
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton

class TournamentListActivity : AppCompatActivity() {
    private lateinit var adapter: TournamentAdapter
    private lateinit var tournamentList: MutableList<Tournament>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tournament_list)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        val btnAdd = findViewById<FloatingActionButton>(R.id.btnAddTournament)
        val tvEmpty = findViewById<TextView>(R.id.tvEmpty)
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        val currentUser = SessionManager.getUser(this) ?: ""

        tournamentList = TournamentStorage.getTournaments(this)

        adapter = TournamentAdapter(tournamentList) {
            Toast.makeText(this, it.name, Toast.LENGTH_SHORT).show()
        }

        if (tournamentList.isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
        } else {
            tvEmpty.visibility = View.GONE
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        btnAdd.setOnClickListener {
            startActivity(Intent(this, AddTournamentActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()

        tournamentList.clear()
        tournamentList.addAll(TournamentStorage.getTournaments(this))
        adapter.notifyDataSetChanged()
    }
}