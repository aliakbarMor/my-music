package com.example.mymusic.view.adapter


interface MusicListener {

    fun onMusicClicked(position: Int)

    fun onMusicLongClicked(position: Int)
}