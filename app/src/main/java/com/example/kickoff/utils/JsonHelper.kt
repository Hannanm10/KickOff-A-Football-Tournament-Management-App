package com.example.kickoff.utils

import android.content.Context
import com.example.kickoff.models.User
import org.json.JSONArray

object JsonHelper {

    fun loadUsers(context: Context): List<User> {
        val jsonString = context.assets.open("users.json")
            .bufferedReader()
            .use { it.readText() }

        val jsonArray = JSONArray(jsonString)
        val users = mutableListOf<User>()

        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)

            users.add(
                User(
                    obj.getString("username"),
                    obj.getString("password")
                )
            )
        }

        return users
    }
}