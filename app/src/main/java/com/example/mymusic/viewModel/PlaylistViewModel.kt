package com.example.mymusic.viewModel

import androidx.lifecycle.ViewModel
import com.example.mymusic.database.Music
import javax.inject.Inject


class PlaylistViewModel @Inject constructor() : ViewModel() {

    var musicList: List<Music>? = null

    var playListName: String? = null

    fun init(playlistName: String, musicList: List<Music>) {
        this.playListName = playlistName
        this.musicList = musicList

    }

}