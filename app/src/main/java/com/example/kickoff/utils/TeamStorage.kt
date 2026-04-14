package com.example.kickoff.utils

import android.content.Context
import com.example.kickoff.models.Team
import org.json.JSONArray
import org.json.JSONObject

object TeamStorage {

    private const val PREF = "kickoff_teams"
    private const val KEY = "teams"

    fun getTeams(context: Context, tournament: String): MutableList<Team> {

        val pref = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        val data = pref.getString(KEY, null)

        val list = mutableListOf<Team>()

        if (data != null) {
            val arr = JSONArray(data)

            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)

                val team = Team(
                    obj.getString("name"),
                    obj.getString("tournamentName")
                )

                if (team.tournamentName == tournament) {
                    list.add(team)
                }
            }
        }

        return list
    }

    fun addTeam(context: Context, team: Team) {

        val pref = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        val editor = pref.edit()

        val existing = getAllTeams(context)

        existing.add(team)

        val arr = JSONArray()

        existing.forEach {
            val obj = JSONObject()
            obj.put("name", it.name)
            obj.put("tournamentName", it.tournamentName)
            arr.put(obj)
        }

        editor.putString(KEY, arr.toString())
        editor.apply()
    }

    private fun getAllTeams(context: Context): MutableList<Team> {

        val pref = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        val data = pref.getString(KEY, null)

        val list = mutableListOf<Team>()

        if (data != null) {
            val arr = JSONArray(data)

            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)

                list.add(
                    Team(
                        obj.getString("name"),
                        obj.getString("tournamentName")
                    )
                )
            }
        }

        return list
    }
}