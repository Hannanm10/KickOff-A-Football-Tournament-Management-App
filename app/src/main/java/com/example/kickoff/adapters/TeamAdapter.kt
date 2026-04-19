package com.example.kickoff.adapters

import android.app.AlertDialog
import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.kickoff.R
import android.content.Intent
import com.example.kickoff.activities.MatchListActivity
import com.example.kickoff.models.Team
import com.example.kickoff.utils.ImageUtils
import com.example.kickoff.utils.MatchStorage
import com.example.kickoff.utils.SessionManager
import com.example.kickoff.utils.TeamStorage

class TeamAdapter(
    private val list: List<Team>,
    private val organizer: String,
    private val onDataChanged: () -> Unit = {}
) : RecyclerView.Adapter<TeamAdapter.ViewHolder>() {

    private var currentEditingPosition: Int = -1
    private var currentEditingUri: String? = null
    private var currentEditingImageView: ImageView? = null

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name = view.findViewById<TextView>(R.id.tvTeamName)
        val logo = view.findViewById<ImageView>(R.id.ivTeamLogo)
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
        
        if (team.logoUri != null) {
            val bitmap = ImageUtils.decodeSampledBitmapFromUri(holder.itemView.context, Uri.parse(team.logoUri), 100, 100)
            holder.logo.setImageBitmap(bitmap)
            holder.logo.setColorFilter(null)
        } else {
            holder.logo.setImageResource(android.R.drawable.ic_menu_myplaces)
            holder.logo.setColorFilter(holder.itemView.context.getColor(R.color.primaryMaroon))
        }

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

            val options = arrayOf(context.getString(R.string.edit), context.getString(R.string.delete))
            AlertDialog.Builder(context)
                .setTitle(R.string.manage_team)
                .setItems(options) { _, which ->
                    when (which) {
                        0 -> { // Edit
                            showEditDialog(context, team, position)
                        }
                        1 -> { // Delete
                            AlertDialog.Builder(context)
                                .setTitle(R.string.delete)
                                .setMessage(R.string.confirm_delete)
                                .setPositiveButton(R.string.yes) { _, _ ->
                                    TeamStorage.deleteTeam(context, team)
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

    private fun showEditDialog(context: Context, team: Team, position: Int) {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_edit_team, null)
        val etName = view.findViewById<EditText>(R.id.etTeamName)
        val ivLogo = view.findViewById<ImageView>(R.id.ivTeamLogo)
        val btnChangeLogo = view.findViewById<Button>(R.id.btnChangeLogo)

        // Hide logo editing components as requested
        ivLogo.visibility = View.GONE
        btnChangeLogo.visibility = View.GONE

        etName.setText(team.name)

        AlertDialog.Builder(context)
            .setTitle(R.string.manage_team)
            .setView(view)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                val newName = etName.text.toString().trim()
                if (newName.isNotEmpty()) {
                    TeamStorage.updateTeamFull(context, team, newName, team.logoUri)
                    (list as MutableList)[position] = team.copy(name = newName)
                    notifyItemChanged(position)
                    onDataChanged()
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }
}