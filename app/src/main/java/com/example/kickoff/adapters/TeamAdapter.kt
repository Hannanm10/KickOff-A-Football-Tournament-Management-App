package com.example.kickoff.adapters

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import android.content.Intent
import com.example.kickoff.R
import com.example.kickoff.activities.MatchListActivity
import com.example.kickoff.models.Team
import com.example.kickoff.repositories.TeamRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class TeamAdapter(
    private val list: List<Team>,
    private val tournamentId: String
) : RecyclerView.Adapter<TeamAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name = view.findViewById<TextView>(R.id.tvTeamName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_team, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val team = list[position]
        holder.name.text = team.name
        
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, MatchListActivity::class.java)
            intent.putExtra("tournamentId", team.tournamentId)
            intent.putExtra("team_filter_id", team.teamId)
            context.startActivity(intent)
        }

        holder.itemView.setOnLongClickListener {
            val context = holder.itemView.context
            checkPermissionAndShowMenu(context, team)
            true
        }
    }

    private fun checkPermissionAndShowMenu(context: Context, team: Team) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseDatabase.getInstance().getReference("tournaments").child(tournamentId).get()
            .addOnSuccessListener { snapshot ->
                val organizerId = snapshot.child("organizerId").getValue(String::class.java)
                if (currentUserId == organizerId) {
                    showManageMenu(context, team)
                }
            }
    }

    private fun showManageMenu(context: Context, team: Team) {
        val options = arrayOf("Delete")
        AlertDialog.Builder(context)
            .setTitle("Manage Team")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> { // Delete
                        AlertDialog.Builder(context)
                            .setTitle("Delete")
                            .setMessage("Are you sure?")
                            .setPositiveButton("Yes") { _, _ ->
                                TeamRepository.deleteTeam(team.teamId) { success, error ->
                                    if (!success) {
                                        Toast.makeText(context, "Error: $error", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                            .setNegativeButton("No", null)
                            .show()
                    }
                }
            }
            .show()
    }
}