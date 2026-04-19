package com.example.kickoff.utils

import android.content.Context
import com.example.kickoff.models.Tournament
import org.json.JSONArray
import org.json.JSONObject

object TournamentStorage {

    private const val PREF_NAME = "kickoff_tournaments"
    private const val KEY = "tournaments"

    fun getTournaments(context: Context): MutableList<Tournament> {
        val pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val jsonString = pref.getString(KEY, null)

        val list = mutableListOf<Tournament>()

        if (jsonString != null) {
            val jsonArray = JSONArray(jsonString)

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)

                list.add(
                    Tournament(
                        obj.getString("name"),
                        obj.getString("organizer")
                    )
                )
            }
        }

        return list
    }

    fun saveTournaments(context: Context, list: List<Tournament>) {
        val pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val editor = pref.edit()

        val jsonArray = JSONArray()

        list.forEach {
            val obj = JSONObject()
            obj.put("name", it.name)
            obj.put("organizer", it.organizer)
            jsonArray.put(obj)
        }

        editor.putString(KEY, jsonArray.toString())
        editor.apply()
    }

    fun addTournament(context: Context, tournament: Tournament) {
        val list = getTournaments(context)
        list.add(tournament)
        saveTournaments(context, list)
    }

    fun updateTournament(context: Context, oldName: String, newName: String) {
        val all = getTournaments(context)
        val index = all.indexOfFirst { it.name == oldName }
        if (index != -1) {
            all[index] = all[index].copy(name = newName)
            saveTournaments(context, all)
            
            // Centralized Cascade
            TeamStorage.updateTournamentNameInTeams(context, oldName, newName)
            MatchStorage.updateTournamentNameInMatches(context, oldName, newName)
        }
    }

    fun deleteTournament(context: Context, tournament: Tournament) {
        val all = getTournaments(context)
        if (all.removeIf { it.name == tournament.name }) {
            saveTournaments(context, all)
            
            // Centralized Cascade
            TeamStorage.deleteTeamsByTournament(context, tournament.name)
            MatchStorage.deleteMatchesByTournament(context, tournament.name)
        }
    }
}