package com.example.mymusic.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface PlaylistDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPlayList(playList: Playlist)

    @Delete
    fun deletePlayList(playList: Playlist): Int

    @Query("SELECT * FROM playLists WHERE id = :id")
    fun getPlayListById(id: Int): Playlist

    @Query("SELECT * FROM playLists")
    fun getAll(): LiveData<List<Playlist>>
}