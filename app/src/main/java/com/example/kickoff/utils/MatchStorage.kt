package com.example.kickoff.utils

import android.content.Context
import com.example.kickoff.models.Match
import org.json.JSONArray
import org.json.JSONObject

object MatchStorage {

    private const val PREF = "kickoff_matches"
    private const val KEY = "matches"

    fun getMatches(context: Context, tournament: String): MutableList<Match> {

        val pref = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        val data = pref.getString(KEY, null)

        val list = mutableListOf<Match>()

        if (data != null) {
            val arr = JSONArray(data)

            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)

                val match = Match(
                    obj.getString("teamA"),
                    obj.getString("teamB"),
                    obj.getInt("scoreA"),
                    obj.getInt("scoreB"),
                    obj.getString("tournamentName"),
                    obj.optString("date", "")
                )

                if (match.tournamentName == tournament) {
                    list.add(match)
                }
            }
        }

        return list
    }

    fun addMatch(context: Context, match: Match) {

        val all = getAllMatches(context)
        all.add(match)
        save(context, all)
    }

    fun save(context: Context, list: List<Match>) {

        val pref = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        val editor = pref.edit()

        val arr = JSONArray()

        list.forEach {
            val obj = JSONObject()
            obj.put("teamA", it.teamA)
            obj.put("teamB", it.teamB)
            obj.put("scoreA", it.scoreA)
            obj.put("scoreB", it.scoreB)
            obj.put("tournamentName", it.tournamentName)
            obj.put("date", it.date)
            arr.put(obj)
        }

        editor.putString(KEY, arr.toString())
        editor.apply()
    }

    fun deleteMatch(context: Context, match: Match) {
        val all = getAllMatches(context)
        all.removeIf { it.teamA == match.teamA && it.teamB == match.teamB && it.tournamentName == match.tournamentName }
        save(context, all)
    }

    fun updateMatch(context: Context, oldMatch: Match, newMatch: Match) {
        val all = getAllMatches(context)
        val index = all.indexOfFirst { it.teamA == oldMatch.teamA && it.teamB == oldMatch.teamB && it.tournamentName == oldMatch.tournamentName }
        if (index != -1) {
            all[index] = newMatch
            save(context, all)
        }
    }

    fun updateTeamNameInMatches(context: Context, tournamentName: String, oldName: String, newName: String) {
        val all = getAllMatches(context)
        var changed = false
        all.forEachIndexed { index, match ->
            if (match.tournamentName == tournamentName) {
                var updatedMatch = match
                if (match.teamA == oldName) {
                    updatedMatch = updatedMatch.copy(teamA = newName)
                    changed = true
                }
                if (match.teamB == oldName) {
                    updatedMatch = updatedMatch.copy(teamB = newName)
                    changed = true
                }
                all[index] = updatedMatch
            }
        }
        if (changed) save(context, all)
    }

    fun updateTournamentNameInMatches(context: Context, oldName: String, newName: String) {
        val all = getAllMatches(context)
        var changed = false
        all.forEachIndexed { index, match ->
            if (match.tournamentName == oldName) {
                all[index] = match.copy(tournamentName = newName)
                changed = true
            }
        }
        if (changed) save(context, all)
    }

    fun deleteMatchesByTeam(context: Context, tournamentName: String, teamName: String) {
        val all = getAllMatches(context)
        val initialSize = all.size
        all.removeIf { it.tournamentName == tournamentName && (it.teamA == teamName || it.teamB == teamName) }
        if (all.size != initialSize) save(context, all)
    }

    fun deleteMatchesByTournament(context: Context, tournamentName: String) {
        val all = getAllMatches(context)
        val initialSize = all.size
        all.removeIf { it.tournamentName == tournamentName }
        if (all.size != initialSize) save(context, all)
    }

    private fun getAllMatches(context: Context): MutableList<Match> {

        val pref = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        val data = pref.getString(KEY, null)

        val list = mutableListOf<Match>()

        if (data != null) {
            val arr = JSONArray(data)

            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)

                list.add(
                    Match(
                        obj.getString("teamA"),
                        obj.getString("teamB"),
                        obj.getInt("scoreA"),
                        obj.getInt("scoreB"),
                        obj.getString("tournamentName"),
                        obj.optString("date", "")
                    )
                )
            }
        }

        return list
    }
}