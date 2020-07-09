package com.example.mymusic.view.adapter

import android.view.View


interface MusicListener {

    fun onMusicClicked(position: Int, isMostPlayedList: Boolean)

    fun onMusicLongClicked(position: Int)

    fun onSubjectClicked(position: Int, isMostPlayedList: Boolean, view: View)
}