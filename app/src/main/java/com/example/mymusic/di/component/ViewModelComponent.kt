package com.example.mymusic.di.component

import com.example.mymusic.di.module.ViewModelModule
import com.example.mymusic.view.MusicListFragment
import com.example.mymusic.view.PlayMusic
import com.example.mymusic.view.PlaylistFragment
import dagger.Component

@Component(modules = [ViewModelModule::class])
interface ViewModelComponent{


    fun inject(frg: MusicListFragment)
    fun inject(frg: PlayMusic)
    fun inject(frg: PlaylistFragment)
}