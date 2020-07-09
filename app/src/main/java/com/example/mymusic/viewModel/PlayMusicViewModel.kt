package com.example.mymusic.viewModel

import android.content.Context
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.BindingAdapter
import androidx.lifecycle.*
import com.bumptech.glide.Glide
import com.example.mymusic.R
import com.example.mymusic.notification.MusicNotification
import com.example.mymusic.storage.database.AppRepository
import com.example.mymusic.storage.database.Music
import com.example.mymusic.utility.milliToMinutes
import com.example.mymusic.view.LyricDialog
import javax.inject.Inject

class PlayMusicViewModel @Inject constructor() : ViewModel() {

    lateinit var music: Music
    var context: Context? = null

    var isShuffle = false
    var isRepeat = MutableLiveData<Boolean>(false)
    var isPlay = MutableLiveData<Boolean>(true)
    var isFavorite = MutableLiveData<Boolean>()
    var currentPositionTime = MutableLiveData(0)

    companion object {
        private val metaRetriever = MediaMetadataRetriever()

        @JvmStatic
        @BindingAdapter("app:loadImage")
        fun loadImage(imageView: ImageView, path: String?) {
            if (path != null) {
                metaRetriever.setDataSource(path)
                val art = metaRetriever.embeddedPicture
                if (art != null) {
                    val songImage = BitmapFactory.decodeByteArray(art, 0, art.size)
                    val bitmapRequestBuilder = Glide
                        .with(imageView.context)
                        .asBitmap()
                        .override(1000, 1000)
                        .load(songImage)
                    bitmapRequestBuilder.into(imageView)
                } else {
                    imageView.setImageResource(R.drawable.ic_music)
                }
            }
        }

        @JvmStatic
        @BindingAdapter("app:progress")
        fun setProgress(seekBar: SeekBar, viewModel: PlayMusicViewModel) {
            viewModel.currentPositionTime.observe(seekBar.context as LifecycleOwner,
                Observer {
                    if (viewModel.currentPositionTime.value!! >= 0) {
                        seekBar.progress = it.toInt() * 100 / viewModel.music.duration!!.toInt()
                    }
                })

            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        val currentDuration =
                            seekBar.progress * viewModel.music.duration!!.toInt() / 100
                        viewModel.music.seekTo(currentDuration)
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar) {
                }
            })
        }

        @JvmStatic
        @BindingAdapter("app:setCurrentPositionTime")
        fun setCurrentTime(textView: TextView, currentPositionTime: MutableLiveData<Int>) {
            currentPositionTime.observe(textView.context as LifecycleOwner, Observer {
                textView.text = milliToMinutes(it.toString())
            })
        }

        @JvmStatic
        @BindingAdapter("app:loadIconRepeat")
        fun loadIconRepeat(imageView: ImageView, isRepeat: MutableLiveData<Boolean>) {
            isRepeat.observe(imageView.context as LifecycleOwner, Observer {
                if (!isRepeat.value!!)
                    imageView.setImageResource(R.drawable.ic_repeat_off)
                else
                    imageView.setImageResource(R.drawable.ic_repeat)
            })
        }

        @JvmStatic
        @BindingAdapter("app:loadIconPauseAndPlay")
        fun loadIconPauseAndPlay(imageView: ImageView, isPlay: MutableLiveData<Boolean>) {
            isPlay.observe(imageView.context as LifecycleOwner, Observer {
                if (isPlay.value!!) {
                    imageView.setImageResource(R.mipmap.ic_pause)
                } else {
                    imageView.setImageResource(R.mipmap.ic_play)
                }
            })
        }

        @JvmStatic
        @BindingAdapter("app:loadImgFavorite")
        fun loadImgFavorite(imageView: ImageView, isFavorite: MutableLiveData<Boolean>) {
            isFavorite.observe(imageView.context as LifecycleOwner, Observer {
                if (isFavorite.value!!) {
                    imageView.setImageResource(R.drawable.ic_favorite)
                } else {
                    imageView.setImageResource(R.drawable.ic_not_favorite)
                }
            })
        }
    }

    fun milliToTime(millisecondString: String): String {
        return milliToMinutes(millisecondString)
    }

    fun onShuffleClicked() {
        if (isShuffle) {
            isShuffle = false
            Toast.makeText(context, "Shuffle off", Toast.LENGTH_SHORT).show()
        } else {
            isShuffle = true
            if (isRepeat.value!!) {
                isRepeat.postValue(false)
                Toast.makeText(context, "Shuffle on \nRepeat off", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Shuffle on", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun onRepeatClicked() {
        if (isRepeat.value!!) {
            isRepeat.postValue(false)
            Toast.makeText(context, "Repeat off", Toast.LENGTH_SHORT).show()
        } else {
            isRepeat.postValue(true)
            if (isShuffle) {
                isShuffle = false
                Toast.makeText(context, "Repeat on \nShuffle off", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Repeat on", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun onPauseAndPlayClicked() {
        val notification = MusicNotification.getInstance(context!!)

        if (music.mediaPlayer.isPlaying) {
            notification!!.remoteViews.setImageViewResource(
                R.id.ic_play_and_pause_song,
                R.drawable.ic_play
            )
            isPlay.postValue(false)
            music.mediaPlayer.stop()
        } else {
            notification!!.remoteViews.setImageViewResource(
                R.id.ic_play_and_pause_song,
                R.drawable.ic_pause
            )
            music.playMusic()
            isPlay.postValue(true)
            music.mediaPlayer.seekTo(currentPositionTime.value!!)
        }
        notification.notificationManager.notify(1929, notification.builder.build())
    }

    fun onFavoriteClicked() {
        music.playListName = "Favorite"
        if (!isFavorite.value!!) {
            isFavorite.postValue(true)
            AppRepository.getInstance(context!!).insertMusic(music)
            Toast.makeText(context, "added to favorite", Toast.LENGTH_SHORT).show()
        } else {
            isFavorite.postValue(false)
            AppRepository.getInstance(context!!).deleteMusic(
                music.title!!,
                music.artist!!,
                music.playListName!!
            )
            Toast.makeText(context, "remove from favorite", Toast.LENGTH_SHORT).show()
        }
    }

    fun onLyricClicked() {
        LyricDialog.getInstance().showDialog(context!!, music.artist, music.title)
    }

}