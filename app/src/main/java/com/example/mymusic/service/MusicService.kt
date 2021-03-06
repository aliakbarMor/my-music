package com.example.mymusic.service

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.IBinder
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.mymusic.notification.MusicNotification
import com.example.mymusic.storage.database.AppRepository
import com.example.mymusic.storage.database.Music
import com.example.mymusic.utility.MediaPlayerManager
import com.example.mymusic.utility.musics
import com.example.mymusic.view.MusicListFragment
import java.util.concurrent.Executors

class MusicService : Service() {

    companion object {
        const val ACTION_MUSIC_STARTED = "com.example.mymusic.action.MUSIC_STARTED"
        const val ACTION_MUSIC_COMPLETED = "com.example.mymusic.action.MUSIC_COMPLETED"
        const val ACTION_MUSIC_IN_PROGRESS = "com.example.mymusic.action.MUSIC_IN_PROGRESS"
    }

    private lateinit var mediaPlayer: MediaPlayer
    private var musicsList: List<Music>? = null

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }

    override fun onCreate() {
        super.onCreate()
        mediaPlayer = MediaPlayerManager.getInstance()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            if (MusicListFragment.onMostPlayedListClick) {
                Executors.newCachedThreadPool().execute {
                    musicsList =
                        AppRepository.getInstance(applicationContext).getMusicsByNumberOfPlayed()
                }
            } else
                musicsList = musics!!
            while (musicsList==null){
                Thread.sleep(3)
            }
            if (intent.hasExtra("position")) {
                val position = intent.getIntExtra("position", -1)
                val music = musicsList!![position]
                MusicNotification.getInstance(applicationContext)?.showNotification(music, position)
                playMusic(music)
                sendBroadcasts(position, music)
                updateNumberOfPlayed(music)
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun updateNumberOfPlayed(music: Music) {
        Executors.newCachedThreadPool().execute {
            val music1 =
                AppRepository.getInstance(applicationContext)
                    .getMusicInMostPlayed(music.title!!, music.artist!!)
            if (music1 == null) {
                music.playListName = "most played"
                music.numberOfPlayedSong = 1
                AppRepository.getInstance(applicationContext).insertMusic(music)
            } else {
                AppRepository.getInstance(applicationContext)
                    .updateNumberOfPlayed(
                        music.title!!,
                        music.artist!!,
                        music1.numberOfPlayedSong + 1
                    )
            }
        }
    }


    private fun playMusic(music: Music) {
        music.playMusic()
    }

    private fun sendBroadcasts(position: Int, music: Music) {
        val bundle = Bundle()
        bundle.putInt("currentPosition", position)
        sendBroadcast(ACTION_MUSIC_STARTED, bundle)

        val totalTimeSecond = music.duration!!.toInt() / 1000
        Thread(Runnable {
            for (int in 0..totalTimeSecond) {
                Thread.sleep(1000)
                val currentPosition = mediaPlayer.currentPosition
                val bundle1 = Bundle()
                bundle1.putInt("currentPositionTime", currentPosition)
                sendBroadcast(ACTION_MUSIC_IN_PROGRESS, bundle1)
            }
        }).start()
    }

    private fun sendBroadcast(action: String, bundle: Bundle) {
        val intent = Intent(action)
        intent.putExtras(bundle)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

}
