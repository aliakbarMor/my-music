package com.example.mymusic.storage.database

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "PlayLists")
class Playlist {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
    var playListName: String? = null

    @Ignore
    var list = ArrayList<Playlist>()
}