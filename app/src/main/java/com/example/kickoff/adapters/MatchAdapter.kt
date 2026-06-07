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
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.kickoff.R
import com.example.kickoff.models.Match
import com.example.kickoff.repositories.MatchRepository
import com.example.kickoff.repositories.TeamRepository
import com.example.kickoff.utils.ImageUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class MatchAdapter(
    private val list: List<Match>,
    private val tournamentId: String
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

        holder.teamA.text = match.teamAName
        holder.teamB.text = match.teamBName
        holder.score.text = "${match.scoreA} - ${match.scoreB}"

        // Fetch logos from TeamRepository
        TeamRepository.getTeamsByTournament(tournamentId) { teams ->
            val teamA = teams.find { it.teamId == match.teamAId }
            val teamB = teams.find { it.teamId == match.teamBId }

            if (teamA?.logoUrl?.isNotEmpty() == true) {
                try {
                    holder.logoA.setImageURI(Uri.parse(teamA.logoUrl))
                    holder.logoA.setColorFilter(null)
                } catch (e: Exception) {
                    holder.logoA.setImageResource(android.R.drawable.ic_menu_myplaces)
                }
            } else {
                holder.logoA.setImageResource(android.R.drawable.ic_menu_myplaces)
                holder.logoA.setColorFilter(context.getColor(R.color.primaryMaroon))
            }

            if (teamB?.logoUrl?.isNotEmpty() == true) {
                try {
                    holder.logoB.setImageURI(Uri.parse(teamB.logoUrl))
                    holder.logoB.setColorFilter(null)
                } catch (e: Exception) {
                    holder.logoB.setImageResource(android.R.drawable.ic_menu_myplaces)
                }
            } else {
                holder.logoB.setImageResource(android.R.drawable.ic_menu_myplaces)
                holder.logoB.setColorFilter(context.getColor(R.color.primaryMaroon))
            }
        }

        val result = when {
            match.scoreA > match.scoreB -> "Winner: ${match.teamAName}"
            match.scoreB > match.scoreA -> "Winner: ${match.teamBName}"
            else -> "Result: Draw"
        }

        holder.winner.text = result
        holder.date.text = "Date: ${match.matchDate.ifEmpty { "N/A" }}"

        holder.itemView.setOnLongClickListener {
            checkPermissionAndShowMenu(context, match)
            true
        }
    }

    private fun checkPermissionAndShowMenu(context: android.content.Context, match: Match) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseDatabase.getInstance().getReference("tournaments").child(tournamentId).get()
            .addOnSuccessListener { snapshot ->
                val organizerId = snapshot.child("organizerId").getValue(String::class.java)
                if (currentUserId == organizerId) {
                    showManageMenu(context, match)
                }
            }
    }

    private fun showManageMenu(context: android.content.Context, match: Match) {
        val options = arrayOf("Edit Scores", "Delete")
        AlertDialog.Builder(context)
            .setTitle("Manage Match")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showEditScoresDialog(context, match)
                    1 -> showDeleteConfirmDialog(context, match)
                }
            }
            .show()
    }

    private fun showEditScoresDialog(context: android.content.Context, match: Match) {
        val layout = LinearLayout(context)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(50, 20, 50, 20)

        val etScoreA = EditText(context)
        etScoreA.hint = "Score ${match.teamAName}"
        etScoreA.setText(match.scoreA.toString())
        etScoreA.inputType = android.text.InputType.TYPE_CLASS_NUMBER
        layout.addView(etScoreA)

        val etScoreB = EditText(context)
        etScoreB.hint = "Score ${match.teamBName}"
        etScoreB.setText(match.scoreB.toString())
        etScoreB.inputType = android.text.InputType.TYPE_CLASS_NUMBER
        layout.addView(etScoreB)

        AlertDialog.Builder(context)
            .setTitle("Edit Scores")
            .setView(layout)
            .setPositiveButton("Update") { _, _ ->
                val sA = etScoreA.text.toString().toIntOrNull() ?: match.scoreA
                val sB = etScoreB.text.toString().toIntOrNull() ?: match.scoreB
                
                match.scoreA = sA
                match.scoreB = sB
                match.status = "COMPLETED"
                
                MatchRepository.updateMatch(match) { success, error ->
                    if (!success) {
                        Toast.makeText(context, "Error: $error", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeleteConfirmDialog(context: android.content.Context, match: Match) {
        AlertDialog.Builder(context)
            .setTitle("Delete Match")
            .setMessage("Are you sure?")
            .setPositiveButton("Yes") { _, _ ->
                MatchRepository.deleteMatch(match.matchId) { success, error ->
                    if (!success) {
                        Toast.makeText(context, "Error: $error", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("No", null)
            .show()
    }
}