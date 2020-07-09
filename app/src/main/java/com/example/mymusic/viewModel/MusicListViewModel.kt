package com.example.mymusic.viewModel

import android.content.Context
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
import com.example.mymusic.storage.database.AppRepository
import com.example.mymusic.storage.database.Music
import com.example.mymusic.utility.getMusics
import com.example.mymusic.utility.musics
import com.example.mymusic.view.MusicListFragment.Companion.isFilteredListMode
import com.example.mymusic.view.adapter.HorizontalMusicAdapter
import com.example.mymusic.view.adapter.MusicAdapter
import java.util.concurrent.Executors
import javax.inject.Inject

class MusicListViewModel @Inject constructor() : ViewModel() {

    var musicList: List<Music>? = null
    var musicListHorizontal: List<Music>? = null

    var music: Music? = null

    var currentPositionTime = MutableLiveData(0)

    fun getMusicList(playListName: String, context: Context): List<Music> {
        if (playListName != "mainMusicList") {
            Executors.newCachedThreadPool().execute {
                musicList = AppRepository.getInstance(context).getMusicsFromPlaylist(playListName)
                musics = (musicList as java.util.ArrayList<Music>?)!!
            }
        } else
            musicList = getMusics(context)

        while (musicList == null) {
            Thread.sleep(3)
        }
        return musicList!!
    }

    fun getMusicListHorizontal(context: Context): List<Music> {
        Executors.newCachedThreadPool().execute {
            musicListHorizontal = AppRepository.getInstance(context).getMusicsByNumberOfPlayed()
        }
        while (musicListHorizontal == null) {
            Thread.sleep(3)
        }
        if (musicListHorizontal?.size!! < 3) {
            musicListHorizontal = musicList
        }
        return musicListHorizontal!!
    }

    companion object {

        lateinit var musicAdapter: MusicAdapter
        lateinit var horizontalMusicAdapter: HorizontalMusicAdapter

        @JvmStatic
        @BindingAdapter("bind:recyclerMusic")
        fun recyclerBinding(recyclerView: RecyclerView, list: List<Music>) {
            musicAdapter = if (isFilteredListMode) {
//                TODO BUG
                MusicAdapter(musics!!)
            } else
                MusicAdapter(list)
            recyclerView.adapter = musicAdapter
        }

        @JvmStatic
        @BindingAdapter("bind:recyclerHorizontalMusic")
        fun recyclerHorizontalBinding(recyclerView: RecyclerView, list: List<Music>?) {
            if (list != null) {
                horizontalMusicAdapter = HorizontalMusicAdapter(list)
                recyclerView.layoutManager =
                    LinearLayoutManager(recyclerView.context, LinearLayoutManager.HORIZONTAL, false)
                recyclerView.adapter = horizontalMusicAdapter
            }
        }


        @JvmStatic
        @BindingAdapter("bind:loadImgNav")
        fun loadImgNav(imageView: ImageView, path: String?) {
            val metaRetriever = MediaMetadataRetriever()
            if (path != null) {
                metaRetriever.setDataSource(path)
                if (metaRetriever.embeddedPicture != null) {
                    val art: ByteArray = metaRetriever.embeddedPicture!!
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
                    if (viewModel.currentPositionTime.value!! >= 0 && viewModel.music?.duration != null) {
                        seekBar.progress = it.toInt() * 100 / viewModel.music?.duration!!.toInt()
                    }
                })

        }

    }
}