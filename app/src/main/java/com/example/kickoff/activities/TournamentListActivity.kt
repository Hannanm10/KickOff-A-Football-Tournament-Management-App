package com.example.kickoff.activities

import  android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
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

    private fun updateEmptyState() {
        val tvEmpty = findViewById<TextView>(R.id.tvEmpty)
        if (tournamentList.isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
        } else {
            tvEmpty.visibility = View.GONE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tournament_list)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        val btnAdd = findViewById<FloatingActionButton>(R.id.btnAddTournament)
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        tournamentList = TournamentStorage.getTournaments(this)

        adapter = TournamentAdapter(tournamentList, onDataChanged = { updateEmptyState() }) {
            Toast.makeText(this, it.name, Toast.LENGTH_SHORT).show()
        }

        updateEmptyState()

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        btnAdd.setOnClickListener {
            startActivity(Intent(this, AddTournamentActivity::class.java))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                SessionManager.logout(this)
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()

        tournamentList.clear()
        tournamentList.addAll(TournamentStorage.getTournaments(this))
        updateEmptyState()
        adapter.notifyDataSetChanged()
    }
}