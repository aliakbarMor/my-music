package com.example.mymusic.di.component

import android.media.MediaPlayer
import com.example.mymusic.di.module.MediaPlayerManager
import com.example.mymusic.di.module.ViewModelModule
import com.example.mymusic.view.MusicList
import com.example.mymusic.view.PlayMusic
import com.example.mymusic.view.PlaylistFragment
import com.example.mymusic.view.adapter.MusicAdapter
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [MediaPlayerManager::class])
interface AppComponent {

    @Singleton
    fun mediaPlayer(): MediaPlayer

}
