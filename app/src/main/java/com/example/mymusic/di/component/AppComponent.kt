package com.example.mymusic.di.component

import android.media.MediaPlayer
import com.example.mymusic.di.module.MediaPlayerManager
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [MediaPlayerManager::class])
interface AppComponent {

    fun mediaPlayer(): MediaPlayer

}
