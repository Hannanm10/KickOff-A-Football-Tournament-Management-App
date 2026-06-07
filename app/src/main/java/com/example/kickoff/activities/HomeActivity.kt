package com.example.kickoff.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kickoff.R
import com.example.kickoff.adapters.TournamentAdapter
import com.example.kickoff.models.Tournament
import com.example.kickoff.repositories.TournamentRepository
import com.example.kickoff.repositories.UserRepository
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class HomeActivity : AppCompatActivity() {

    private lateinit var progressBar: ProgressBar
    private lateinit var tvWelcome: TextView
    private lateinit var tvCountMyTournaments: TextView
    private lateinit var tvCountActive: TextView
    private lateinit var tvCountTeams: TextView
    private lateinit var tvCountMatches: TextView
    private lateinit var tvCountChampionships: TextView
    private lateinit var rvRecent: RecyclerView
    private lateinit var btnCreate: MaterialButton
    private lateinit var tvViewAll: TextView

    private val recentTournaments = mutableListOf<Tournament>()
    private lateinit var adapter: TournamentAdapter
    private var tournamentListener: com.google.firebase.database.ValueEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        initUI()
        setupListeners()
        loadDashboardData()
    }

    private fun initUI() {
        progressBar = findViewById(R.id.progressBar)
        tvWelcome = findViewById(R.id.tvWelcome)
        tvCountMyTournaments = findViewById(R.id.tvCountMyTournaments)
        tvCountActive = findViewById(R.id.tvCountActive)
        tvCountTeams = findViewById(R.id.tvCountTeams)
        tvCountMatches = findViewById(R.id.tvCountMatches)
        tvCountChampionships = findViewById(R.id.tvCountChampionships)
        rvRecent = findViewById(R.id.rvRecentTournaments)
        btnCreate = findViewById(R.id.btnCreateTournament)
        tvViewAll = findViewById(R.id.tvViewAll)

        adapter = TournamentAdapter(recentTournaments) { tournament ->
            val intent = Intent(this, TournamentDetailActivity::class.java)
            intent.putExtra("tournamentId", tournament.tournamentId)
            intent.putExtra("tournamentName", tournament.name)
            startActivity(intent)
        }
        rvRecent.layoutManager = LinearLayoutManager(this)
        rvRecent.adapter = adapter
    }

    private fun setupListeners() {
        btnCreate.setOnClickListener {
            startActivity(Intent(this, AddTournamentActivity::class.java))
        }

        tvViewAll.setOnClickListener {
            startActivity(Intent(this, TournamentListActivity::class.java))
        }

        UserRepository.getCurrentUser { user ->
            tvWelcome.text = "Hello, ${user?.username ?: "User"}!"
        }
    }

    private fun loadDashboardData() {
        progressBar.visibility = View.VISIBLE
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        tournamentListener = TournamentRepository.getAllTournaments { allTournaments ->
            val myTournaments = allTournaments.filter { it.organizerId == currentUserId }
            val active = allTournaments.filter { it.championTeamId.isEmpty() }
            val myChampionships = myTournaments.filter { it.championTeamId.isNotEmpty() }

            tvCountMyTournaments.text = myTournaments.size.toString()
            tvCountActive.text = active.size.toString()
            tvCountChampionships.text = myChampionships.size.toString()

            // Update Recent Activity (Latest 5)
            recentTournaments.clear()
            recentTournaments.addAll(allTournaments.sortedByDescending { it.createdAt }.take(5))
            adapter.notifyDataSetChanged()

            // Fetch Global Totals for Teams and Matches
            fetchGlobalCounts()
        }
    }

    private fun fetchGlobalCounts() {
        val rootRef = FirebaseDatabase.getInstance().reference
        
        rootRef.child("teams").get().addOnSuccessListener { snapshot ->
            tvCountTeams.text = snapshot.childrenCount.toString()
        }

        rootRef.child("matches").get().addOnSuccessListener { snapshot ->
            tvCountMatches.text = snapshot.childrenCount.toString()
            progressBar.visibility = View.GONE
        }
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

    override fun onDestroy() {
        super.onDestroy()
        tournamentListener?.let { TournamentRepository.removeListener(it) }
    }
}