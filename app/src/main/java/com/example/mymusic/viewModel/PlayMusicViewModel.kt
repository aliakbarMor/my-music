package com.example.mymusic.viewModel

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.util.Log
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.BindingAdapter
import androidx.lifecycle.*
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.example.mymusic.R
import com.example.mymusic.notification.MusicNotification
import com.example.mymusic.service.MusicService
import com.example.mymusic.storage.database.AppRepository
import com.example.mymusic.storage.database.Music
import com.example.mymusic.storage.sharedPrefs.PrefsManager
import com.example.mymusic.utility.MediaPlayerManager
import com.example.mymusic.utility.getMusics
import com.example.mymusic.utility.milliToMinutes
import com.example.mymusic.utility.musics
import com.example.mymusic.view.LyricDialog
import com.example.mymusic.view.MusicListFragment
import kotlinx.coroutines.*
import java.lang.Runnable
import java.util.*
import java.util.concurrent.Executors
import javax.inject.Inject
import kotlin.collections.ArrayList

class PlayMusicViewModel @Inject constructor(application: Application) : AndroidViewModel(
    application
) {

    var music = MutableLiveData<Music>()
    var context: Context = application.applicationContext

    var isShuffle = false
    var isRepeat = MutableLiveData<Boolean>(false)
    var isPlay = MutableLiveData<Boolean>(true)
    var isFavorite = MutableLiveData<Boolean>()
    var currentPositionTime = MutableLiveData(0)

    private lateinit var intent: Intent
    private var musicsList: ArrayList<Music>? = null
    var mediaPlayer: MediaPlayer = MediaPlayerManager.getInstance()
    var position: Int = 0

    private val job = Job()
    private val coroutineScope = CoroutineScope(Dispatchers.Main.immediate + job)

    fun init() {

        if (MusicListFragment.onMostPlayedListClick) {
            Executors.newCachedThreadPool().execute {
                val list = AppRepository.getInstance(context).getMusicsByNumberOfPlayed()
                musicsList = if (list.isNotEmpty())
                    list as ArrayList
                else
                    getMusics(context) as ArrayList<Music>

            }
            while (musicsList == null) {
                Thread.sleep(30)
            }
        } else {
            musicsList =
                if (MusicListFragment.isCustomListMode || MusicListFragment.isFilteredListMode || MusicListFragment.isInPlaylist) {
                    musics
                } else
                    getMusics(context) as ArrayList<Music>
        }


        intent = Intent(context, MusicService::class.java)
        music.value = musicsList!![position]

        setIsFavorite()
        setCurrentPosition()
        completeMusic()
        val lastMusicPlayed = PrefsManager(context).loadLastMusicPlayed()

        if (!(mediaPlayer.isPlaying && music.value!!.artist == lastMusicPlayed.artist && music.value!!.title == lastMusicPlayed.title)) {
            intent.putExtra("position", position)
            context.startService(intent)
        }


    }

    override fun onCleared() {
        job.cancel()
        super.onCleared()
    }

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
                        seekBar.progress =
                            it.toInt() * 100 / viewModel.music.value!!.duration!!.toInt()
                    }
                })

            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        val currentDuration =
                            seekBar.progress * viewModel.music.value!!.duration!!.toInt() / 100
                        viewModel.music.value!!.seekTo(currentDuration)
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

//    private suspend fun getMusicListFromDb(): List<Music>? {
//        return withContext(Dispatchers.IO) {
//
//            val a = AppRepository.getInstance(context).getMusicsByNumberOfPlayed()
//            return@withContext a
//        }
//    }

    private fun setCurrentPosition() {
        Thread(Runnable {
            while (true) {
                Thread.sleep(10)
                currentPositionTime.postValue(mediaPlayer.currentPosition)
            }
        }).start()
    }

    fun skipPrevious() {
        if (position > 0) {
            position--
            music.value = musicsList!![position]
        } else {
            position = musicsList!!.size - 1
            music.value = musicsList!![position]
        }
        music.value = musicsList!![position]
        intent.putExtra("position", position)
        context.startService(intent)
        setIsFavorite()
    }

    fun skipNext() {
        if (position < musicsList!!.size - 1) {
            position++
            music.value = musicsList!![position]
        } else {
            position = 0
            music.value = musicsList!![position]
        }
        music.value = musicsList!![position]
        intent.putExtra("position", position)
        context.startService(intent)
        setIsFavorite()
    }

    private fun completeMusic() {
        mediaPlayer.setOnCompletionListener {
            if (currentPositionTime.value!! - music.value!!.duration!!.toInt() < 1000 &&
                currentPositionTime.value!! - music.value!!.duration!!.toInt() > -1000
            ) {
                if (isRepeat.value!!) {
                    intent.putExtra("position", position)
                    context.startService(intent)
                } else if (isShuffle) {
                    val rand = Random()
                    position = rand.nextInt(musicsList!!.size - 1)
                    music.value = musicsList!![position]
                    intent.putExtra("position", position)
                    context.startService(intent)
                } else {
                    if (position < musicsList!!.size - 1) {
                        position++
                        music.value = musicsList!![position]
                        intent.putExtra("position", position)
                        context.startService(intent)

                    } else {
                        position = 0
                        music.value = musicsList!![position]
                        intent.putExtra("position", position)
                        context.startService(intent)
                    }
                }
                setIsFavorite()
            }
        }
    }

    private fun setIsFavorite() {

        coroutineScope.launch {
            val music1 = setFavorite()
            isFavorite.value = music1 != null
        }
//        Executors.newCachedThreadPool().execute {
//            val music: Music? = AppRepository.getInstance(context)
//                .getMusic(musicsList.value!![position].title!!, musicsList.value!![position].artist!!)
//            isFavorite.postValue(music != null)
//        }
    }

    private suspend fun setFavorite(): Music? {
        return withContext(Dispatchers.IO) {
            AppRepository.getInstance(context)
                .isMusicInFavorite(
                    musicsList!![position].title!!, musicsList!![position].artist!!
                )
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
        val notification = MusicNotification.getInstance(context)

        if (music.value!!.mediaPlayer.isPlaying) {
            notification!!.remoteViews.setImageViewResource(
                R.id.ic_play_and_pause_song,
                R.drawable.ic_play
            )
            isPlay.postValue(false)
            music.value!!.mediaPlayer.stop()
        } else {
            notification!!.remoteViews.setImageViewResource(
                R.id.ic_play_and_pause_song,
                R.drawable.ic_pause
            )
            music.value!!.playMusic()
            isPlay.postValue(true)
            music.value!!.mediaPlayer.seekTo(currentPositionTime.value!!)
        }
        notification.notificationManager.notify(1929, notification.builder.build())
    }

    fun onFavoriteClicked() {
        music.value!!.playListName = "Favorite"
        if (!isFavorite.value!!) {
            isFavorite.postValue(true)
            AppRepository.getInstance(context).insertMusic(music.value!!)
            Toast.makeText(context, "added to favorite", Toast.LENGTH_SHORT).show()
        } else {
            isFavorite.postValue(false)
            AppRepository.getInstance(context).deleteMusic(
                music.value!!.title!!,
                music.value!!.artist!!,
                music.value!!.playListName!!
            )
            Toast.makeText(context, "remove from favorite", Toast.LENGTH_SHORT).show()
        }
    }

    fun onLyricClicked() {
        LyricDialog.getInstance().showDialog(context, music.value!!.artist, music.value!!.title)
    }

    val notificationReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            when (intent.action) {
                MusicNotification.ACTION_MUSIC_SKIP_NEXT -> {
                    skipNext()
                }
                MusicNotification.ACTION_MUSIC_SKIP_PREVIOUS -> {
                    skipPrevious()
                }
                MusicNotification.ACTION_MUSIC_STOP -> {
                    onPauseAndPlayClicked()
                }
            }
        }
    }


}