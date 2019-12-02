package com.example.mymusic.utility

import android.content.Context
import android.provider.MediaStore
import android.text.TextUtils
import com.example.mymusic.database.Music
import java.util.ArrayList


fun getMusics(context: Context): List<Music> {
    val list = ArrayList<Music>()
    val cursor = context.contentResolver.query(
        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null,
        MediaStore.Audio.AudioColumns.IS_MUSIC
    )
    if (cursor != null) {
        while (cursor.moveToNext()) {
            val music = Music(
                cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)),
                cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)),
                cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)),
                cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION))
            )
            if (music.title!!.contains("-")) {
                val str = music.title!!.split("-")
                if (TextUtils.isEmpty(music.artist))
                    music.artist = str[0]
                if (TextUtils.isEmpty(music.title))
                    music.title = str[1]
            }
            list.add(music)
        }
        cursor.close()
    }
    return list
}

fun milliToMinutes(millisecondString: String): String {
    if (millisecondString.toLong() > 0) {
        val millisecond = millisecondString.toLong()
        var hours = (millisecond / (60 * 60 * 1000)).toString()
        var minutes = (millisecond % (60 * 60 * 1000) / (60 * 1000)).toString()
        var seconds = (millisecond % (60 * 60 * 1000) % (60 * 1000) / 1000).toString()


        if (Integer.parseInt(hours) < 10)
            hours = "0$hours"
        if (Integer.parseInt(minutes) < 10)
            minutes = "0$minutes"
        if (Integer.parseInt(seconds) < 10)
            seconds = "0$seconds"

        return if (Integer.parseInt(hours) > 0)
            "$hours:$minutes:$seconds"
        else
            "$minutes:$seconds"
    }
    return "00:00"
}
