package com.example.kickoff.adapters

import android.app.AlertDialog
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.kickoff.R
import com.example.kickoff.activities.TournamentDetailActivity
import com.example.kickoff.models.Tournament
import com.example.kickoff.utils.MatchStorage
import com.example.kickoff.utils.SessionManager
import com.example.kickoff.utils.TeamStorage
import com.example.kickoff.utils.TournamentStorage

class TournamentAdapter(
    private val list: List<Tournament>,
    private val onClick: (Tournament) -> Unit
) : RecyclerView.Adapter<TournamentAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name = view.findViewById<TextView>(R.id.tvTournamentName)
        val organizer = view.findViewById<TextView>(R.id.tvOrganizer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tournament, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val tournament = list[position]

        holder.name.text = tournament.name
        holder.organizer.text = "Organizer: ${tournament.organizer}"

        holder.itemView.setOnClickListener {
            val context = holder.itemView.context

            val intent = Intent(context, TournamentDetailActivity::class.java)
            intent.putExtra("name", tournament.name)
            intent.putExtra("organizer", tournament.organizer)

            context.startActivity(intent)
        }

        holder.itemView.setOnLongClickListener {

            val context = holder.itemView.context

            val currentUser = SessionManager.getUser(context)
            val isOrganizer = currentUser == tournament.organizer

            if (!isOrganizer) return@setOnLongClickListener true

            val options = arrayOf("Edit", "Delete")
            AlertDialog.Builder(context)
                .setTitle("Manage Tournament")
                .setItems(options) { _, which ->
                    when (which) {
                        0 -> { // Edit
                            val editText = android.widget.EditText(context)
                            editText.setText(tournament.name)
                            AlertDialog.Builder(context)
                                .setTitle("Edit Tournament Name")
                                .setView(editText)
                                .setPositiveButton("Update") { _, _ ->
                                    val newName = editText.text.toString()
                                    if (newName.isNotEmpty()) {
                                        TournamentStorage.updateTournament(context, tournament.name, newName)
                                        
                                        // Cascade update to Teams and Matches
                                        TeamStorage.updateTournamentNameInTeams(context, tournament.name, newName)
                                        MatchStorage.updateTournamentNameInMatches(context, tournament.name, newName)

                                        (list as MutableList)[position] = tournament.copy(name = newName)
                                        notifyItemChanged(position)
                                    }
                                }
                                .setNegativeButton("Cancel", null)
                                .show()
                        }
                        1 -> { // Delete
                            AlertDialog.Builder(context)
                                .setTitle("Delete Tournament")
                                .setMessage("Are you sure? This will delete all teams and matches in this tournament.")
                                .setPositiveButton("Yes") { _, _ ->
                                    TournamentStorage.deleteTournament(context, tournament)
                                    // Cascade delete to Teams and Matches
                                    TeamStorage.deleteTeamsByTournament(context, tournament.name)
                                    MatchStorage.deleteMatchesByTournament(context, tournament.name)

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