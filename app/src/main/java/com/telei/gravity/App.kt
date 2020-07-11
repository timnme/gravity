package com.telei.gravity

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.telei.gravity.game.GameData

class App : Application() {
    companion object {
        private const val PREF_LEVELS = "PREF_LEVELS"

        private val gson = Gson()

        private val preferences: SharedPreferences by lazy {
            context.getSharedPreferences(PREF_LEVELS, Context.MODE_PRIVATE)
        }

        lateinit var context: Context

        lateinit var levels: MutableList<GameData>

        var currentLevel: GameData? = null
            get() = field?.clone()
            set(value) {
                field = value?.clone()
            }

        fun saveLevels() {
            preferences
                .edit()
                .remove(PREF_LEVELS)
                .putString(PREF_LEVELS, gson.toJson(levels))
                .apply()
        }
    }

    override fun onCreate() {
        super.onCreate()
        context = this
        preferences.getString(PREF_LEVELS, null)?.let {
            levels = gson.fromJson<List<GameData>>(
                it, object : TypeToken<List<GameData>>() {}.type
            ).toMutableList()
        }
    }
}