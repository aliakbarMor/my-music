package com.example.mymusic.storage.database

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.example.mymusic.utility.MediaPlayerManager


@Entity(tableName = "Musics")
class Music(
    var musicId: String?,
    var artist: String?,
    var title: String?,
    var path: String?,
    var duration: String?
) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
    var playListName: String? = null
    var numberOfPlayedSong: Long = 0

    @Ignore
    val mediaPlayer = MediaPlayerManager.getInstance()

    @Ignore
    fun playMusic() {
        mediaPlayer.reset()
        mediaPlayer.setDataSource(path)
        mediaPlayer.prepare()
        mediaPlayer.start()
    }

    fun seekTo(position: Int) {
        mediaPlayer.seekTo(position)
    }


}