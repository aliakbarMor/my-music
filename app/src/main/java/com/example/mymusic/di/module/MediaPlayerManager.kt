package com.example.mymusic.di.module

import android.media.MediaPlayer
import dagger.Module
import dagger.Provides
import javax.inject.Singleton


@Module
class MediaPlayerManager {

    @Singleton
    @Provides
    fun mediaPlayer() = MediaPlayer()

}