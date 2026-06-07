package com.example.kickoff.activities

import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.kickoff.R
import com.example.kickoff.repositories.TeamRepository
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textfield.TextInputEditText

class AddTeamActivity : AppCompatActivity() {

    private var selectedLogoUri: String? = null

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedLogoUri = it.toString()
            findViewById<ImageView>(R.id.ivTeamLogo).setImageURI(it)
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

        val tournamentId = intent.getStringExtra("tournamentId") ?: ""

        btnSelectLogo.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        btnSave.setOnClickListener {
            val name = etName.text.toString().trim()

            if (name.isEmpty()) {
                Toast.makeText(this, "Enter team name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnSave.isEnabled = false
            // Note: We are not uploading to Firebase Storage yet (Phase 5)
            // Storing local URI temporarily for testing
            TeamRepository.addTeam(tournamentId, name, selectedLogoUri ?: "") { success, error ->
                btnSave.isEnabled = true
                if (success) {
                    Toast.makeText(this, "Team added", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Error: $error", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}