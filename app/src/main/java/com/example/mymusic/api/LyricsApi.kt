package com.example.mymusic.api

import com.example.mymusic.model.Lyric
import io.reactivex.Flowable
import retrofit2.http.GET
import retrofit2.http.Url

interface LyricsApi {

    @GET
    fun getLyric(@Url url: String): Flowable<Lyric>
}