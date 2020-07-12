package com.telei.gravita

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.telei.gravita.levels.Level

class App : Application() {
    companion object {
        private const val PREF_LEVELS = "PREF_LEVELS"

        private val gson = Gson()

        private val preferences: SharedPreferences by lazy {
            context.getSharedPreferences("PREFS", Context.MODE_PRIVATE)
        }

        lateinit var context: Context

        var levels: MutableList<Level> = mutableListOf()

        var currentLevel: Level? = null
            get() = field?.clone()
            set(value) {
                field = value?.clone()
            }

        fun saveLevels() {
            preferences
                .edit()
                .putString(PREF_LEVELS, gson.toJson(levels))
                .apply()
        }
    }

    override fun onCreate() {
        super.onCreate()
        context = this
        preferences.getString(PREF_LEVELS, null)?.let {
            gson.fromJson<List<Level>>(
                it, object : TypeToken<List<Level>>() {}.type
            )?.toMutableList()?.let { fromJson -> levels = fromJson }
        }
    }
}