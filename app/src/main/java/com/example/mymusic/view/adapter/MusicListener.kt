package com.example.mymusic.view.adapter

import android.view.View


interface MusicListener {

    fun onMusicClicked(position: Int)

    fun onMusicLongClicked(position: Int)

    fun onSubjectClicked(position: Int, view: View)
}