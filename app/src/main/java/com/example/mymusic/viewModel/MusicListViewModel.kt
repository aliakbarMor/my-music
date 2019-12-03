package com.example.mymusic.viewModel

import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mymusic.R
import com.example.mymusic.database.Music
import com.example.mymusic.view.adapter.MusicAdapter
import javax.inject.Inject

class MusicListViewModel @Inject constructor() : ViewModel() {

    var musicList: List<Music>? = null
    var music: Music? = null

    companion object {

        lateinit var musicAdapter: MusicAdapter

        @JvmStatic
        @BindingAdapter("bind:recyclerMusic")
        fun recyclerBinding(recyclerView: RecyclerView, list: List<Music>) {
            musicAdapter = MusicAdapter(list)
            recyclerView.adapter = musicAdapter
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

    }
}