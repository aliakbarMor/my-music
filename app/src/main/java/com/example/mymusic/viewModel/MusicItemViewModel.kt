package com.example.mymusic.viewModel

import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.os.Build
import android.util.Log
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import androidx.lifecycle.ViewModel
import com.bumptech.glide.Glide
import com.example.mymusic.R
import com.example.mymusic.database.Music
import com.example.mymusic.utility.milliToMinutes

class MusicItemViewModel(music: Music) : ViewModel() {

    var id: Long = music.id
    var musicId: String? = music.musicId
    var artist: String? = music.artist
    var title: String? = music.title
    var path: String? = music.path
    var duration: String? = music.duration

    companion object {
        private val metaRetriever = MediaMetadataRetriever()
        private var LOCK = Any()

        var count = 0

        @RequiresApi(Build.VERSION_CODES.P)
        @JvmStatic
        @BindingAdapter("app:loadImg")
        fun loadImage(imageView: ImageView, path: String) {
            if (count % 2 == 0) {
                Thread(Runnable {
                    synchronized(LOCK) {
                        metaRetriever.setDataSource(path)
                        val art = metaRetriever.embeddedPicture
                        if (art != null) {
                            val songImage = BitmapFactory.decodeByteArray(art, 0, art.size)
                            val bitmapRequestBuilder = Glide
                                .with(imageView.context)
                                .asBitmap()
                                .override(100, 100)
                                .load(songImage)
                                .circleCrop()
                            imageView.post { bitmapRequestBuilder.into(imageView) }
                        } else {
                            imageView.post { imageView.setImageResource(R.drawable.ic_music) }
                        }
                    }
                }).start()
                count++
            } else {
                Thread(Runnable {
                    synchronized(LOCK) {
                        metaRetriever.setDataSource(path)
                        val art = metaRetriever.embeddedPicture
                        if (art != null) {
                            val songImage = BitmapFactory.decodeByteArray(art, 0, art.size)
                            val bitmapRequestBuilder = Glide
                                .with(imageView.context)
                                .asBitmap()
                                .override(100, 100)
                                .load(songImage)
                                .circleCrop()
                            imageView.post { bitmapRequestBuilder.into(imageView) }
                        } else {
                            imageView.post { imageView.setImageResource(R.drawable.ic_music) }
                        }
                    }
                }).start()
                count++
            }
        }
    }


    fun milliToTime(millisecondString: String): String {
        return milliToMinutes(millisecondString)
    }

}