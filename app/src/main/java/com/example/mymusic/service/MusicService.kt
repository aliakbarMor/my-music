package com.example.mymusic.service

import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import com.example.mymusic.database.Music
import com.example.mymusic.utility.getMusics

class MusicService : Service() {

    companion object {
        const val ACTION_MUSIC_COMPLETED = "com.example.mymusic.action.MUSIC_COMPLETED"
        const val ACTION_MUSIC_IN_PROGRESS = "com.example.mymusic.action.MUSIC_IN_PROGRESS"
    }

    private lateinit var musics: List<Music>

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }

    override fun onCreate() {
        super.onCreate()

        musics = getMusics(this)

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            if (intent.hasExtra("position")) {
                val position = intent.getIntExtra("position", -1)

                playMusic(position)


            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun playMusic(position: Int) {
        val music = musics[position]
        music.playMusic()

        val bundle = Bundle()
        bundle.putInt("currentPosition", position)
        sendBroadcast(ACTION_MUSIC_COMPLETED,bundle)


//        val totalTimeSecond = music.duration!!.toInt() / 1000
//        Thread(Runnable {
//            for (int in 0..totalTimeSecond) {
//                Thread.sleep(1000)
//                val currentPosition = mediaPlayer.currentPosition
//                val bundle = Bundle()
//                bundle.putInt("currentPosition", currentPosition)
//                sendBroadcast(ACTION_MUSIC_IN_PROGRESS, bundle)
//            }
//        }).start()

    }

    private fun sendBroadcast(action: String, bundle: Bundle) {
        val intent = Intent(action)
        intent.putExtras(bundle)
        sendBroadcast(intent)
    }

}
