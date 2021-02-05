package com.example.mymusic.utility

import android.media.MediaPlayer

class MediaPlayerManager private constructor() : MediaPlayer() {
    companion object {
        private var instance: MediaPlayerManager? = null
        fun getInstance(): MediaPlayerManager {
            if (instance == null) {
                instance = MediaPlayerManager()
            }
            return instance as MediaPlayerManager
        }
    }

}