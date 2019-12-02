package com.example.mymusic.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.mymusic.service.MusicService

class MusicReceiver : BroadcastReceiver() {

    private var currentPosition: Int = 0

    override fun onReceive(context: Context?, intent: Intent?) {

        val action = intent?.action

        if (action!! == MusicService.ACTION_MUSIC_COMPLETED) {
            TODO()
        } else if (action == MusicService.ACTION_MUSIC_IN_PROGRESS) {
            currentPosition = intent.extras?.getInt("currentPosition")!! /1000
        }

    }

}