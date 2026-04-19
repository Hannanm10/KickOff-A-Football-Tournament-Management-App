package com.example.kickoff.activities

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.kickoff.R
import com.example.kickoff.utils.SessionManager
import com.example.kickoff.utils.UserStorage
import com.google.android.material.textfield.TextInputEditText

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Auto-login if already logged in
        val currentUser = SessionManager.getUser(this)
        if (currentUser != null) {
            startActivity(Intent(this, TournamentListActivity::class.java))
            finish()
        }

        setContentView(R.layout.activity_login)

        val etUsername = findViewById<TextInputEditText>(R.id.etUsername)
        val etPassword = findViewById<TextInputEditText>(R.id.etPassword)
        val btnLogin = findViewById<com.google.android.material.button.MaterialButton>(R.id.btnLogin)
        val btnSignup = findViewById<com.google.android.material.button.MaterialButton>(R.id.btnGoSignup)

        btnLogin.setOnClickListener {

            val username = etUsername.text.toString()
            val password = etPassword.text.toString()

            val users = UserStorage.getUsers(this)

            val user = users.find {
                it.username == username && it.password == password
            }

            if (user != null) {
                SessionManager.saveUser(this, username)

                startActivity(Intent(this, TournamentListActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show()
            }
        }

        btnSignup.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }
    }
}