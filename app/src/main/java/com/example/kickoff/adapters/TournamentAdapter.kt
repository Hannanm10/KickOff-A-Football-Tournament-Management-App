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
        holder.organizer.text = holder.itemView.context.getString(R.string.organizer_label, tournament.organizer)

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

            val options = arrayOf(context.getString(R.string.edit), context.getString(R.string.delete))
            AlertDialog.Builder(context)
                .setTitle(R.string.manage_tournament)
                .setItems(options) { _, which ->
                    when (which) {
                        0 -> { // Edit
                            val editText = android.widget.EditText(context)
                            editText.setText(tournament.name)
                            AlertDialog.Builder(context)
                                .setTitle(R.string.edit)
                                .setView(editText)
                                .setPositiveButton(android.R.string.ok) { _, _ ->
                                    val newName = editText.text.toString().trim()
                                    if (newName.isNotEmpty()) {
                                        TournamentStorage.updateTournament(context, tournament.name, newName)
                                        
                                        (list as MutableList)[position] = tournament.copy(name = newName)
                                        notifyItemChanged(position)
                                        onDataChanged()
                                    }
                                }
                                .setNegativeButton(android.R.string.cancel, null)
                                .show()
                        }
                        1 -> { // Delete
                            AlertDialog.Builder(context)
                                .setTitle(R.string.delete)
                                .setMessage(R.string.confirm_delete)
                                .setPositiveButton(R.string.yes) { _, _ ->
                                    TournamentStorage.deleteTournament(context, tournament)

                                    (list as MutableList).removeAt(position)
                                    notifyItemRemoved(position)
                                    onDataChanged()
                                }
                                .setNegativeButton(R.string.no, null)
                                .show()
                        }
                    }
                }
                .show()

            true
        }
    }
}