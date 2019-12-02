package com.example.mymusic.viewModel

import androidx.databinding.BindingAdapter
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import com.example.mymusic.database.Music
import com.example.mymusic.view.adapter.MusicAdapter
import javax.inject.Inject

class MusicListViewModel @Inject constructor() : ViewModel() {

    var musicList: List<Music>? = null

    companion object {

        lateinit var musicAdapter: MusicAdapter

        @JvmStatic
        @BindingAdapter("bind:recyclerMusic")
        fun recyclerBinding(recyclerView: RecyclerView, list: List<Music>) {
            musicAdapter = MusicAdapter(list)
            recyclerView.adapter = musicAdapter
        }
    }
}