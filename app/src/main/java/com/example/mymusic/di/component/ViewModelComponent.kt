package com.example.mymusic.di.component

import android.app.Application
import com.example.mymusic.di.module.ViewModelModule
import com.example.mymusic.view.MusicListFragment
import com.example.mymusic.view.PlayMusic
import com.example.mymusic.view.PlaylistFragment
import dagger.BindsInstance
import dagger.Component

@Component(modules = [ViewModelModule::class])
interface ViewModelComponent {

    @Component.Builder
    interface Builder {
        fun build(): ViewModelComponent

        @BindsInstance
        fun application(application: Application): Builder
    }

    fun inject(frg: MusicListFragment)
    fun inject(frg: PlayMusic)
    fun inject(frg: PlaylistFragment)
}