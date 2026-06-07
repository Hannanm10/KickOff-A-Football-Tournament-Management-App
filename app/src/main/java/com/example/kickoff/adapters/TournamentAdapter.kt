package com.example.kickoff.adapters

import android.app.AlertDialog
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.kickoff.R
import com.example.kickoff.activities.TournamentDetailActivity
import com.example.kickoff.models.Tournament
import com.example.kickoff.repositories.TournamentRepository
import com.google.firebase.auth.FirebaseAuth

class TournamentAdapter(
    private val list: List<Tournament>,
    private val onDataChanged: () -> Unit = {},
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
        holder.organizer.text = "Organizer: ${tournament.organizerName}"

        holder.itemView.setOnClickListener {
            onClick(tournament)
        }

        holder.itemView.setOnLongClickListener {
            val context = holder.itemView.context
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

            // Only organizer can edit/delete
            if (currentUserId != tournament.organizerId) {
                return@setOnLongClickListener true
            }

            val options = arrayOf("Edit", "Delete")
            AlertDialog.Builder(context)
                .setTitle("Manage Tournament")
                .setItems(options) { _, which ->
                    when (which) {
                        0 -> { // Edit
                            val editText = EditText(context)
                            editText.setText(tournament.name)
                            AlertDialog.Builder(context)
                                .setTitle("Edit Name")
                                .setView(editText)
                                .setPositiveButton("OK") { _, _ ->
                                    val newName = editText.text.toString().trim()
                                    if (newName.isNotEmpty()) {
                                        TournamentRepository.updateTournament(tournament.tournamentId, newName) { success, error ->
                                            if (!success) {
                                                Toast.makeText(context, "Error: $error", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                }
                                .setNegativeButton("Cancel", null)
                                .show()
                        }
                        1 -> { // Delete
                            AlertDialog.Builder(context)
                                .setTitle("Delete")
                                .setMessage("Are you sure?")
                                .setPositiveButton("Yes") { _, _ ->
                                    TournamentRepository.deleteTournament(tournament.tournamentId) { success, error ->
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
            true
        }
    }
}