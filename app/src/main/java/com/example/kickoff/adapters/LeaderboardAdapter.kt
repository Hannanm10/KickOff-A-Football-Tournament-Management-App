package com.example.kickoff.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.kickoff.R
import com.example.kickoff.utils.ImageUtils
import com.example.kickoff.models.LeaderboardEntry

class LeaderboardAdapter(
    private val list: List<LeaderboardEntry>,
    private val onItemClick: (String) -> Unit
) :
    RecyclerView.Adapter<LeaderboardAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvPos: TextView = view.findViewById(R.id.tvPos)
        val ivLogo: ImageView = view.findViewById(R.id.ivTeamLogo)
        val tvName: TextView = view.findViewById(R.id.tvTeamName)
        val tvPlayed: TextView = view.findViewById(R.id.tvPlayed)
        val tvWins: TextView = view.findViewById(R.id.tvWins)
        val tvDraws: TextView = view.findViewById(R.id.tvDraws)
        val tvLosses: TextView = view.findViewById(R.id.tvLosses)
        val tvGF: TextView = view.findViewById(R.id.tvGF)
        val tvGA: TextView = view.findViewById(R.id.tvGA)
        val tvGD: TextView = view.findViewById(R.id.tvGD)
        val tvPoints: TextView = view.findViewById(R.id.tvPoints)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_leaderboard, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val entry = list[position]
        holder.tvPos.text = (position + 1).toString()
        holder.tvName.text = entry.teamName

        if (entry.logoUri != null) {
            val bitmap = ImageUtils.decodeSampledBitmapFromUri(holder.itemView.context, android.net.Uri.parse(entry.logoUri), 60, 60)
            holder.ivLogo.setImageBitmap(bitmap)
            holder.ivLogo.setColorFilter(null)
        } else {
            holder.ivLogo.setImageResource(android.R.drawable.ic_menu_myplaces)
            holder.ivLogo.setColorFilter(holder.itemView.context.getColor(R.color.primaryMaroon))
        }

        holder.tvPlayed.text = entry.matchesPlayed.toString()
        holder.tvWins.text = entry.wins.toString()
        holder.tvDraws.text = entry.draws.toString()
        holder.tvLosses.text = entry.losses.toString()
        holder.tvGF.text = entry.goalsFor.toString()
        holder.tvGA.text = entry.goalsAgainst.toString()
        holder.tvGD.text = entry.goalDifference.toString()
        holder.tvPoints.text = entry.points.toString()

        holder.itemView.setOnClickListener { onItemClick(entry.teamName) }
    }
}