package com.example.kickoff.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kickoff.R
import com.example.kickoff.adapters.TournamentAdapter
import com.example.kickoff.models.Tournament
import com.example.kickoff.repositories.TournamentRepository
import com.example.kickoff.repositories.UserRepository
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton

class TournamentListActivity : AppCompatActivity() {
    private lateinit var adapter: TournamentAdapter
    private var tournamentList = mutableListOf<Tournament>()
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmpty: TextView
    private var tournamentListener: com.google.firebase.database.ValueEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tournament_list)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        val btnAdd = findViewById<FloatingActionButton>(R.id.btnAddTournament)
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        progressBar = findViewById(R.id.progressBar)
        tvEmpty = findViewById(R.id.tvEmpty)
        
        setSupportActionBar(toolbar)

        adapter = TournamentAdapter(tournamentList, onDataChanged = { updateEmptyState() }) { tournament ->
            val intent = Intent(this, TournamentDetailActivity::class.java)
            intent.putExtra("tournamentId", tournament.tournamentId)
            intent.putExtra("tournamentName", tournament.name)
            startActivity(intent)
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        btnAdd.setOnClickListener {
            startActivity(Intent(this, AddTournamentActivity::class.java))
        }

        loadTournaments()
    }

    private fun loadTournaments() {
        progressBar.visibility = View.VISIBLE
        tournamentListener = TournamentRepository.getAllTournaments { list ->
            progressBar.visibility = View.GONE
            tournamentList.clear()
            tournamentList.addAll(list)
            adapter.notifyDataSetChanged()
            updateEmptyState()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        tournamentListener?.let { TournamentRepository.removeListener(it) }
    }

    private fun updateEmptyState() {
        tvEmpty.visibility = if (tournamentList.isEmpty()) View.VISIBLE else View.GONE
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                UserRepository.logout()
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}