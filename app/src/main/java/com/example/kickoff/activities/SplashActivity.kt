package com.example.kickoff.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.kickoff.repositories.UserRepository

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.example.kickoff.R.layout.activity_splash)

        Handler(Looper.getMainLooper()).postDelayed({
            if (UserRepository.isUserLoggedIn()) {
                startActivity(Intent(this, TournamentListActivity::class.java))
            } else {
                startActivity(Intent(this, LoginActivity::class.java))
            }
            finish()
        }, 2000) // 2 seconds
    }
}