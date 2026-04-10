package com.example.kickoff.utils

import android.content.Context
import com.example.kickoff.models.User
import org.json.JSONArray
import org.json.JSONObject

object UserStorage {

    private const val PREF_NAME = "kickoff_users"
    private const val KEY_USERS = "users"

    fun getUsers(context: Context): MutableList<User> {

        val sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val savedUsersString = sharedPref.getString(KEY_USERS, null)

        val users = mutableListOf<User>()

        // Default users
        users.addAll(JsonHelper.loadUsers(context))

        // Saved users
        if (savedUsersString != null) {
            val jsonArray = JSONArray(savedUsersString)

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)

                users.add(
                    User(
                        obj.getString("username"),
                        obj.getString("password")
                    )
                )
            }
        }

        return users
    }

    fun addUser(context: Context, user: User) {

        val sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val editor = sharedPref.edit()

        val currentUsers = getUsers(context)

        // Prevent duplicate usernames
        if (currentUsers.any { it.username == user.username }) return

        currentUsers.add(user)

        val jsonArray = JSONArray()

        currentUsers.forEach {
            val obj = JSONObject()
            obj.put("username", it.username)
            obj.put("password", it.password)
            jsonArray.put(obj)
        }

        editor.putString(KEY_USERS, jsonArray.toString())
        editor.apply()
    }
}