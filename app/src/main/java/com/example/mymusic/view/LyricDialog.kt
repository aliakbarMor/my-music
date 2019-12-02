package com.example.mymusic.view

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.util.Log
import android.view.LayoutInflater
import androidx.databinding.DataBindingUtil
import com.example.mymusic.R
import com.example.mymusic.api.ApiManager
import com.example.mymusic.databinding.DialogLyricBinding
import com.example.mymusic.model.Lyric
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlin.system.exitProcess

class LyricDialog private constructor() {

    companion object {
        private var instance: LyricDialog? = null
        fun getInstance(): LyricDialog {
            if (instance == null) {
                instance = LyricDialog()
            }
            return instance as LyricDialog
        }
    }

    @SuppressLint("CheckResult", "SetTextI18n")
    fun showDialog(context: Context, artist: String?, title: String?) {

        val inflater = LayoutInflater.from(context)
        val dialogBinding: DialogLyricBinding =
            DataBindingUtil.inflate(inflater, (R.layout.dialog_lyric), null, false)
        dialogBinding.lyric = Lyric()

        val alertDialog = AlertDialog
            .Builder(context, R.style.DialogTheme)
            .setView(dialogBinding.root)
            .setNegativeButton("Cancel") { dialogInterface: DialogInterface, i: Int ->
                dialogInterface.cancel()
            }


        ApiManager
            .getLyric(artist!!, title!!)
            .subscribeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    dialogBinding.lyricBody.post {
                        dialogBinding.lyricBody.text = it.lyrics
                    }
                }, {
                    dialogBinding.lyricBody.post {
                        dialogBinding.lyricBody.text = "No embedded lyric found"
                    }
                }
            )
        alertDialog.show()
    }

}