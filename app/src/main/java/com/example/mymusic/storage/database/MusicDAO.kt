package com.example.mymusic.storage.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query


@Dao
interface MusicDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMusic(music: Music)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(musics: ArrayList<Music>)

    @Query("DELETE FROM musics WHERE title = :title and artist = :artist and playListName = :playListName")
    fun deleteMusic(title: String, artist: String, playListName: String)

    @Query("DELETE FROM musics")
    fun deleteAll(): Int

    @Query("SELECT * FROM musics WHERE title = :title and artist = :artist")
    fun getMusicById(title: String, artist: String): Music

    @Query("SELECT * FROM musics WHERE title = :title and artist = :artist and playListName = :playlistName")
    fun getMusicInMostPlayed(title: String, artist: String, playlistName: String = "most played"): Music

    @Query("SELECT * FROM musics WHERE playListName = :playlistName")
    fun getMusicsFromPlaylist(playlistName: String): List<Music>

    @Query("SELECT * FROM musics ORDER BY numberOfPlayedSong DESC")
    fun getMusicsByNumberOfPlayed(): List<Music>

    @Query("UPDATE musics SET numberOfPlayedSong = :numberOfPlayedSong WHERE title = :title and artist = :artist and playListName = :playlistName")
    fun updateNumberOfPlayed(title: String, artist: String, numberOfPlayedSong: Long, playlistName: String = "most played"): Int

    @Query("SELECT * FROM musics")
    fun getAll(): LiveData<List<Music>>

    @Query("SELECT COUNT(*) FROM musics")
    fun getCount(): Int

}