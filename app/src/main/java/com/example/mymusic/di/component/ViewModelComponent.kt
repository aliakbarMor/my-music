package com.example.mymusic.di.component

import com.example.mymusic.di.module.ViewModelModule
import com.example.mymusic.view.MusicList
import com.example.mymusic.view.PlayMusic
import com.example.mymusic.view.PlaylistFragment
import dagger.Component

@Component(modules = [ViewModelModule::class])
interface ViewModelComponent{


    fun inject(frg: MusicList)
    fun inject(frg: PlayMusic)
    fun inject(frg: PlaylistFragment)
}