package com.example.mymusic.api

import com.example.mymusic.model.Lyric
import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers

class ApiManager {

    companion object {

        fun getLyric(artist: String, title: String): Flowable<Lyric> {
            val url = "v1/$artist/$title"

            return ApiServiceGenerator
                .createLyricApi()!!
                .getLyric(url)
                .subscribeOn(Schedulers.io())
        }
    }
}
