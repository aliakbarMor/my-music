package com.example.mymusic.di.module

import androidx.lifecycle.ViewModel
import com.example.mymusic.viewModel.MusicListViewModel
import com.example.mymusic.viewModel.PlayMusicViewModel
import com.example.mymusic.viewModel.PlaylistViewModel
import dagger.multibindings.IntoMap
import dagger.Binds
import dagger.Module

@Module
interface ViewModelModule {
    @Binds
    @IntoMap
    @ViewModelKey(MusicListViewModel::class)
    fun bindsMusicListViewModel(musicListViewModel: MusicListViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(PlayMusicViewModel::class)
    fun bindsPlayMusicViewModel(playMusicViewModel: PlayMusicViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(PlaylistViewModel::class)
    fun bindsPlaylistViewModel(playlistViewModel: PlaylistViewModel): ViewModel


}