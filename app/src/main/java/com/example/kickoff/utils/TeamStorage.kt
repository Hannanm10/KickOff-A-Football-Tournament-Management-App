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

    fun deleteTeam(context: Context, team: Team) {
        val all = getAllTeams(context)
        all.removeIf { it.name == team.name && it.tournamentName == team.tournamentName }
        saveTeams(context, all)
    }

    fun updateTeam(context: Context, oldName: String, newName: String, tournamentName: String) {
        val all = getAllTeams(context)
        val team = all.find { it.name == oldName && it.tournamentName == tournamentName }
        team?.let {
            all[all.indexOf(it)] = it.copy(name = newName)
            saveTeams(context, all)
        }
    }

    fun updateTournamentNameInTeams(context: Context, oldName: String, newName: String) {
        val all = getAllTeams(context)
        var changed = false
        all.forEachIndexed { index, team ->
            if (team.tournamentName == oldName) {
                all[index] = team.copy(tournamentName = newName)
                changed = true
            }
        }
        if (changed) saveTeams(context, all)
    }

    fun deleteTeamsByTournament(context: Context, tournamentName: String) {
        val all = getAllTeams(context)
        val initialSize = all.size
        all.removeIf { it.tournamentName == tournamentName }
        if (all.size != initialSize) saveTeams(context, all)
    }

    private fun saveTeams(context: Context, list: List<Team>) {
        val pref = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        val editor = pref.edit()
        val arr = JSONArray()
        list.forEach {
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