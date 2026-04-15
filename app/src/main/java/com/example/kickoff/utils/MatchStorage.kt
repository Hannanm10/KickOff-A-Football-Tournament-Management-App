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
                    obj.getString("tournamentName")
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
            arr.put(obj)
        }

        editor.putString(KEY, arr.toString())
        editor.apply()
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
                        obj.getString("tournamentName")
                    )
                )
            }
        }

        return list
    }
}