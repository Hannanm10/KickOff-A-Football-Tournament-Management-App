package com.example.kickoff.adapters

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.kickoff.R
import android.content.Intent
import com.example.kickoff.activities.MatchListActivity
import com.example.kickoff.models.Team
import com.example.kickoff.utils.MatchStorage
import com.example.kickoff.utils.SessionManager
import com.example.kickoff.utils.TeamStorage

class TeamAdapter(
    private val list: List<Team>,
    private val organizer: String
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
            intent.putExtra("tournament", team.tournamentName)
            intent.putExtra("team_filter", team.name)
            intent.putExtra("organizer", organizer)
            context.startActivity(intent)
        }

        holder.itemView.setOnLongClickListener {
            val context = holder.itemView.context
            val currentUser = SessionManager.getUser(context)

            if (currentUser != organizer) return@setOnLongClickListener true

            val options = arrayOf("Edit", "Delete")
            AlertDialog.Builder(context)
                .setTitle("Manage Team")
                .setItems(options) { _, which ->
                    when (which) {
                        0 -> { // Edit
                            val editText = EditText(context)
                            editText.setText(team.name)
                            AlertDialog.Builder(context)
                                .setTitle("Edit Team Name")
                                .setView(editText)
                                .setPositiveButton("Update") { _, _ ->
                                    val newName = editText.text.toString()
                                    if (newName.isNotEmpty()) {
                                        TeamStorage.updateTeam(context, team.name, newName, team.tournamentName)
                                        // Cascade update to matches
                                        MatchStorage.updateTeamNameInMatches(context, team.tournamentName, team.name, newName)

                                        (list as MutableList)[position] = team.copy(name = newName)
                                        notifyItemChanged(position)
                                    }
                                }
                                .setNegativeButton("Cancel", null)
                                .show()
                        }
                        1 -> { // Delete
                            AlertDialog.Builder(context)
                                .setTitle("Delete Team")
                                .setMessage("Are you sure? This will also delete all matches involving this team.")
                                .setPositiveButton("Yes") { _, _ ->
                                    TeamStorage.deleteTeam(context, team)
                                    // Cascade delete to matches
                                    MatchStorage.deleteMatchesByTeam(context, team.tournamentName, team.name)

                                    (list as MutableList).removeAt(position)
                                    notifyItemRemoved(position)
                                }
                                .setNegativeButton("No", null)
                                .show()
                        }
                    }
                }
                .show()
            true
        }
    }
}