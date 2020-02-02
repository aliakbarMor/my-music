package com.example.mymusic.storage.sharedPrefs

import android.content.Context
import com.example.mymusic.storage.database.Music

class PrefsManager(context: Context) {
    private val sharedPreferences = context.getSharedPreferences("app", Context.MODE_PRIVATE)

    fun saveLastMusicPlayed(music: Music) {
        sharedPreferences.edit().putString("artist", music.artist).apply()
        sharedPreferences.edit().putString("title", music.title).apply()
        sharedPreferences.edit().putString("duration", music.duration).apply()
        sharedPreferences.edit().putString("path", music.path).apply()
    }

    fun loadLastMusicPlayed(): Music {
        return Music(
            null
            , sharedPreferences.getString("artist", null)
            , sharedPreferences.getString("title", null)
            , sharedPreferences.getString("path", null)
            , sharedPreferences.getString("duration", null)

        )
    }

    fun saveLastIndexMusic(position: Int) {
        sharedPreferences.edit().putInt("position", position).apply()
    }

    fun loadLastIndexMusic(): Int {
        return sharedPreferences.getInt("position", 0)
    }
}