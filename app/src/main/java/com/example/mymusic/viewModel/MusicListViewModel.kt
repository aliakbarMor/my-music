package com.example.mymusic.viewModel

import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.widget.ImageView
import android.widget.SeekBar
import androidx.databinding.BindingAdapter
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mymusic.R
import com.example.mymusic.storage.database.Music
import com.example.mymusic.utility.musics
import com.example.mymusic.view.MusicListFragment.Companion.isFilteredListMode
import com.example.mymusic.view.adapter.HorizontalMusicAdapter
import com.example.mymusic.view.adapter.MusicAdapter
import javax.inject.Inject

class MusicListViewModel @Inject constructor() : ViewModel() {

    var musicList: List<Music>? = null
    var music: Music? = null

    var currentPositionTime = MutableLiveData(0)

    companion object {

        lateinit var musicAdapter: MusicAdapter
        lateinit var horizontalMusicAdapter: HorizontalMusicAdapter

        @JvmStatic
        @BindingAdapter("bind:recyclerMusic")
        fun recyclerBinding(recyclerView: RecyclerView, list: List<Music>) {
            musicAdapter = if (isFilteredListMode) {
                MusicAdapter(musics!!)
            } else
                MusicAdapter(list)
            recyclerView.adapter = musicAdapter
        }

        @JvmStatic
        @BindingAdapter("bind:recyclerHorizontalMusic")
        fun recyclerHorizontalBinding(recyclerView: RecyclerView, list: List<Music>) {
            horizontalMusicAdapter = HorizontalMusicAdapter(list)
            recyclerView.layoutManager =
                LinearLayoutManager(recyclerView.context, LinearLayoutManager.HORIZONTAL, false)
            recyclerView.adapter = horizontalMusicAdapter
        }


        @JvmStatic
        @BindingAdapter("bind:loadImgNav")
        fun loadImgNav(imageView: ImageView, path: String?) {
            val metaRetriever = MediaMetadataRetriever()
            if (path != null) {
                metaRetriever.setDataSource(path)
                if (metaRetriever.embeddedPicture != null) {
                    val art: ByteArray = metaRetriever.embeddedPicture
                    val songImage = BitmapFactory.decodeByteArray(art, 0, art.size)
                    imageView.setImageBitmap(songImage)
                    val bitmapRequestBuilder = Glide
                        .with(imageView.context)
                        .asBitmap()
                        .load(songImage)
                        .centerCrop()
                    bitmapRequestBuilder.into(imageView)
                } else
                    imageView.setImageResource(R.drawable.ic_music)
            }
        }

        @JvmStatic
        @BindingAdapter("app:showProgress")
        fun setProgress(seekBar: SeekBar, viewModel: MusicListViewModel) {
            viewModel.currentPositionTime.observe(seekBar.context as LifecycleOwner,
                Observer {
                    if (viewModel.currentPositionTime.value!! >= 0) {
                        seekBar.progress = it.toInt() * 100 / viewModel.music?.duration!!.toInt()
                    }
                })

        }

    }
}