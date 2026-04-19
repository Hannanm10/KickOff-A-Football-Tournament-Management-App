package com.example.kickoff.adapters

import android.app.AlertDialog
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.kickoff.R
import com.example.kickoff.models.Match
import com.example.kickoff.utils.MatchStorage
import com.example.kickoff.utils.SessionManager
import com.example.kickoff.utils.ImageUtils
import com.example.kickoff.utils.TeamStorage

class MatchAdapter(
    private val list: List<Match>,
    private val organizer: String,
    private val onDataChanged: () -> Unit = {}
) : RecyclerView.Adapter<MatchAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val teamA = view.findViewById<TextView>(R.id.tvTeamA)
        val teamB = view.findViewById<TextView>(R.id.tvTeamB)
        val logoA = view.findViewById<ImageView>(R.id.ivLogoA)
        val logoB = view.findViewById<ImageView>(R.id.ivLogoB)
        val score = view.findViewById<TextView>(R.id.tvScore)
        val winner = view.findViewById<TextView>(R.id.tvWinner)
        val date = view.findViewById<TextView>(R.id.tvDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_match, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val match = list[position]
        val context = holder.itemView.context

        holder.teamA.text = match.teamA
        holder.teamB.text = match.teamB
        holder.score.text = "${match.scoreA} - ${match.scoreB}"

        // Load logos
        val teams = TeamStorage.getTeams(context, match.tournamentName)
        val teamA = teams.find { it.name == match.teamA }
        val teamB = teams.find { it.name == match.teamB }

        if (teamA?.logoUri != null) {
            val bitmap = ImageUtils.decodeSampledBitmapFromUri(context, Uri.parse(teamA.logoUri), 80, 80)
            holder.logoA.setImageBitmap(bitmap)
            holder.logoA.setColorFilter(null)
        } else {
            holder.logoA.setImageResource(android.R.drawable.ic_menu_myplaces)
            holder.logoA.setColorFilter(context.getColor(R.color.primaryMaroon))
        }

        if (teamB?.logoUri != null) {
            val bitmap = ImageUtils.decodeSampledBitmapFromUri(context, Uri.parse(teamB.logoUri), 80, 80)
            holder.logoB.setImageBitmap(bitmap)
            holder.logoB.setColorFilter(null)
        } else {
            holder.logoB.setImageResource(android.R.drawable.ic_menu_myplaces)
            holder.logoB.setColorFilter(context.getColor(R.color.primaryMaroon))
        }

        val result = when {
            match.scoreA > match.scoreB -> "Winner: ${match.teamA}"
            match.scoreB > match.scoreA -> "Winner: ${match.teamB}"
            else -> "Result: Draw"
        }

        holder.winner.text = result
        holder.date.text = "Date: ${match.date.ifEmpty { "N/A" }}"

        holder.itemView.setOnLongClickListener {
            val context = holder.itemView.context
            val currentUser = SessionManager.getUser(context)

            if (currentUser != organizer) return@setOnLongClickListener true

            val options = arrayOf("Edit Scores", "Delete")
            AlertDialog.Builder(context)
                .setTitle("Manage Match")
                .setItems(options) { _, which ->
                    when (which) {
                        0 -> { // Edit Scores
                            val layout = LinearLayout(context)
                            layout.orientation = LinearLayout.VERTICAL
                            layout.setPadding(50, 20, 50, 20)

                            val etScoreA = EditText(context)
                            etScoreA.hint = "Score ${match.teamA}"
                            etScoreA.setText(match.scoreA.toString())
                            etScoreA.inputType = android.text.InputType.TYPE_CLASS_NUMBER
                            layout.addView(etScoreA)

                            val etScoreB = EditText(context)
                            etScoreB.hint = "Score ${match.teamB}"
                            etScoreB.setText(match.scoreB.toString())
                            etScoreB.inputType = android.text.InputType.TYPE_CLASS_NUMBER
                            layout.addView(etScoreB)

                            AlertDialog.Builder(context)
                                .setTitle("Edit Scores")
                                .setView(layout)
                                .setPositiveButton("Update") { _, _ ->
                                    val sA = etScoreA.text.toString().toIntOrNull() ?: match.scoreA
                                    val sB = etScoreB.text.toString().toIntOrNull() ?: match.scoreB
                                    
                                    val newMatch = match.copy(scoreA = sA, scoreB = sB)
                                    MatchStorage.updateMatch(context, match, newMatch)
                                    (list as MutableList)[position] = newMatch
                                    notifyItemChanged(position)
                                    onDataChanged()
                                }
                                .setNegativeButton("Cancel", null)
                                .show()
                        }
                        1 -> { // Delete
                            AlertDialog.Builder(context)
                                .setTitle("Delete Match")
                                .setMessage("Are you sure?")
                                .setPositiveButton("Yes") { _, _ ->
                                    MatchStorage.deleteMatch(context, match)
                                    (list as MutableList).removeAt(position)
                                    notifyItemRemoved(position)
                                    onDataChanged()
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