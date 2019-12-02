
package com.example.mymusic.model;

import com.google.gson.annotations.SerializedName;

@SuppressWarnings("unused")
public class Lyric {

    @SerializedName("lyrics")
    private String mLyrics;

    public String getLyrics() {
        return mLyrics;
    }

    public void setLyrics(String lyrics) {
        mLyrics = lyrics;
    }

}
