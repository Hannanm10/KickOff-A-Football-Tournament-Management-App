package com.example.kickoff.utils

import android.content.Context

object SessionManager {

    private const val PREF_NAME = "kickoff_session"
    private const val KEY_USER = "current_user"

    fun saveUser(context: Context, username: String) {
        val pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        pref.edit().putString(KEY_USER, username).apply()
    }

    fun getUser(context: Context): String? {
        val pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return pref.getString(KEY_USER, null)
    }

    fun logout(context: Context) {
        val pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        pref.edit().clear().apply()
    }
}