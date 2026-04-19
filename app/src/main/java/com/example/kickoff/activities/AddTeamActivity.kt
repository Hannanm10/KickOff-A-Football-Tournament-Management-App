package com.example.kickoff.activities

import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.kickoff.R
import com.example.kickoff.models.Team
import com.example.kickoff.utils.SessionManager
import com.example.kickoff.utils.TeamStorage
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textfield.TextInputEditText

class AddTeamActivity : AppCompatActivity() {

    private var selectedLogoUri: String? = null

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            contentResolver.takePersistableUriPermission(it, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            selectedLogoUri = it.toString()
            
            // Downsample for preview to avoid lag
            val bitmap = com.example.kickoff.utils.ImageUtils.decodeSampledBitmapFromUri(this, it, 300, 300)
            findViewById<ImageView>(R.id.ivTeamLogo).setImageBitmap(bitmap)
            findViewById<ImageView>(R.id.ivTeamLogo).setColorFilter(null)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_team)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        val etName = findViewById<TextInputEditText>(R.id.etTeamName)
        val ivLogo = findViewById<ImageView>(R.id.ivTeamLogo)
        val btnSelectLogo = findViewById<Button>(R.id.btnSelectLogo)
        val btnSave = findViewById<com.google.android.material.button.MaterialButton>(R.id.btnSaveTeam)

        val tournament = intent.getStringExtra("tournament") ?: ""

        btnSelectLogo.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        btnSave.setOnClickListener {

            val name = etName.text.toString().trim()

            if (name.isEmpty()) {
                etName.error = getString(R.string.error_empty_name)
                return@setOnClickListener
            }

            val existing = TeamStorage.getTeams(this, tournament)
            if (existing.any { it.name.equals(name, ignoreCase = true) }) {
                etName.error = getString(R.string.error_duplicate_team)
                return@setOnClickListener
            }

            TeamStorage.addTeam(this, Team(name, tournament, selectedLogoUri))

            Toast.makeText(this, R.string.msg_team_added, Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}