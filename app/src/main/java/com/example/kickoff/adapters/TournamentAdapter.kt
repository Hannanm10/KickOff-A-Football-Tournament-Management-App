package com.example.kickoff.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.kickoff.R
import com.example.kickoff.activities.TournamentDetailActivity
import com.example.kickoff.models.Tournament

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
    }
}