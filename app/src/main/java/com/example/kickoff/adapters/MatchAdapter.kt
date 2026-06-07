package com.example.kickoff.adapters

import android.app.AlertDialog
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.kickoff.R
import com.example.kickoff.models.Match
import com.example.kickoff.repositories.MatchRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

class MatchAdapter(
    private val list: List<Match>,
    private val tournamentId: String
) : RecyclerView.Adapter<MatchAdapter.ViewHolder>() {

    private val dateFormat = SimpleDateFormat("yyyy-M-d", Locale.getDefault())

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val teamA = view.findViewById<TextView>(R.id.tvTeamA)
        val teamB = view.findViewById<TextView>(R.id.tvTeamB)
        val score = view.findViewById<TextView>(R.id.tvScore)
        val winner = view.findViewById<TextView>(R.id.tvWinner)
        val date = view.findViewById<TextView>(R.id.tvDate)
        val statusBadge = view.findViewById<TextView>(R.id.tvMatchStatus)
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
        
        val dateStr = if (match.matchDate.isEmpty()) "N/A" else match.matchDate
        holder.date.text = "Date: $dateStr"
        holder.statusBadge.text = match.status

        if (match.status == "UPCOMING") {
            holder.score.text = "VS"
            holder.score.setBackgroundResource(R.drawable.bg_score_badge_upcoming)
            holder.winner.visibility = View.GONE
            
            // Check for passed date
            if (match.matchDate.isNotEmpty()) {
                try {
                    val matchDate = dateFormat.parse(match.matchDate)
                    val today = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.time
                    
                    if (matchDate != null && matchDate.before(today)) {
                        holder.date.text = "Scheduled date passed"
                        holder.date.setTextColor(Color.RED)
                    } else {
                        holder.date.setTextColor(context.getColor(R.color.darkGrey))
                    }
                } catch (e: Exception) {
                    holder.date.setTextColor(context.getColor(R.color.darkGrey))
                }
            } else {
                holder.date.setTextColor(context.getColor(R.color.darkGrey))
            }

        } else {
            holder.score.text = "${match.scoreA} - ${match.scoreB}"
            holder.score.setBackgroundResource(R.drawable.bg_score_badge)
            holder.winner.visibility = View.VISIBLE
            holder.date.setTextColor(context.getColor(R.color.darkGrey))
            
            val result = when {
                match.scoreA > match.scoreB -> "Winner: ${match.teamAName}"
                match.scoreB > match.scoreA -> "Winner: ${match.teamBName}"
                else -> "Result: Draw"
            }
            holder.winner.text = result
        }

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
                val status = snapshot.child("status").getValue(String::class.java)
                
                if (currentUserId == organizerId && status != "COMPLETED") {
                    showManageMenu(context, match)
                }
            }
    }

    private fun showManageMenu(context: android.content.Context, match: Match) {
        val options = arrayOf("Update Result", "Delete")
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
        if (match.scoreA >= 0) etScoreA.setText(match.scoreA.toString())
        etScoreA.inputType = android.text.InputType.TYPE_CLASS_NUMBER
        layout.addView(etScoreA)

        val etScoreB = EditText(context)
        etScoreB.hint = "Score ${match.teamBName}"
        if (match.scoreB >= 0) etScoreB.setText(match.scoreB.toString())
        etScoreB.inputType = android.text.InputType.TYPE_CLASS_NUMBER
        layout.addView(etScoreB)

        AlertDialog.Builder(context)
            .setTitle("Enter Scores")
            .setView(layout)
            .setPositiveButton("Save") { _, _ ->
                val sA = etScoreA.text.toString().toIntOrNull() ?: match.scoreA
                val sB = etScoreB.text.toString().toIntOrNull() ?: match.scoreB
                
                if (match.stage == "SEMI_FINAL" || match.stage == "FINAL") {
                    if (sA == sB) {
                        Toast.makeText(context, "Draws are not allowed in knockout stages", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }
                }

                match.scoreA = sA
                match.scoreB = sB
                
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