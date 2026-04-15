package com.example.kickoff.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.kickoff.R
import com.example.kickoff.models.Match

class MatchAdapter(
    private val list: List<Match>
) : RecyclerView.Adapter<MatchAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val teams = view.findViewById<TextView>(R.id.tvTeams)
        val score = view.findViewById<TextView>(R.id.tvScore)
        val winner = view.findViewById<TextView>(R.id.tvWinner)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_match, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val match = list[position]

        holder.teams.text = "${match.teamA} vs ${match.teamB}"
        holder.score.text = "${match.scoreA} - ${match.scoreB}"

        val result = when {
            match.scoreA > match.scoreB -> match.teamA
            match.scoreB > match.scoreA -> match.teamB
            else -> "Draw"
        }

        holder.winner.text = "Winner: $result"
    }
}