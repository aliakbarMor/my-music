package com.example.mymusic.viewModel

import androidx.databinding.BindingAdapter
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import com.example.mymusic.storage.database.Music
import com.example.mymusic.view.adapter.MusicAdapter
import javax.inject.Inject


class PlaylistViewModel @Inject constructor() : ViewModel() {

    var musicList: List<Music>? = null

    var playListName: String? = null

    fun init(playlistName: String, musicList: List<Music>) {
        this.playListName = playlistName
        this.musicList = musicList

    }

    companion object {
        lateinit var musicAdapter: MusicAdapter
        @JvmStatic
        @BindingAdapter("bind:recyclerPlaylist")
        fun recyclerBinding(recyclerView: RecyclerView, list: List<Music>) {
            musicAdapter = MusicAdapter(list)
            recyclerView.adapter = musicAdapter
        }
    }

}