package com.example.mymusic.utility

import android.media.MediaPlayer

class MediaPlayerManager private constructor(){
    companion object {
        private var instance: MediaPlayer? = null
        fun getInstance(): MediaPlayer {
            if (instance == null) {
                instance = MediaPlayer()
            }
            return instance as MediaPlayer
        }
    }

}