package com.example.mymusic.storage.database

import android.content.Context
import androidx.lifecycle.LiveData
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class AppRepository private constructor(context: Context) {

    companion object {
        private var instance: AppRepository? = null
        fun getInstance(context: Context): AppRepository {
            if (instance == null) {
                instance = AppRepository(context)
            }
            return instance as AppRepository
        }
    }

    private val db = AppDatabase.getInstance(context)!!
    private val executor: Executor = Executors.newCachedThreadPool()

    var allPlaylists: LiveData<List<Playlist>> = getAllPlaylist()

    private fun getAllMusic(): LiveData<List<Music>> {
        return db.musicDAO.getAll()
    }

    fun insertMusic(music: Music) {
        executor.execute {
            db.musicDAO.insertMusic(music)
        }
    }

    fun insertSomeMusics(musics: ArrayList<Music>) {
        executor.execute {
            db.musicDAO.insertAll(musics)
        }
    }

    fun deleteMusic(title: String, artist: String, playListName: String) {
        executor.execute {
            db.musicDAO.deleteMusic(title, artist, playListName)
        }
    }

    fun getMusic(title: String, artist: String): Music {
        return db.musicDAO.getMusicById(title, artist)
    }

    fun getMusicsFromPlaylist(playListName: String): List<Music> {
        return db.musicDAO.getMusicsFromPlaylist(playListName)
    }

    fun insertPlaylist(playlist: Playlist) {
        executor.execute {
            db.playlistDAO.insertPlayList(playlist)
        }
    }

    fun getPlaylist(id: Int): Playlist {
        return db.playlistDAO.getPlayListById(id)
    }

    private fun getAllPlaylist(): LiveData<List<Playlist>> {
        return db.playlistDAO.getAll()
    }

}