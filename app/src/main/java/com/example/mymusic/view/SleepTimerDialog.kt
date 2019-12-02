package com.example.mymusic.view

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.widget.EditText
import com.example.mymusic.R
import com.example.mymusic.di.component.DaggerAppComponent
import java.util.*

class SleepTimerDialog private constructor() {

    companion object {
        private var instance: SleepTimerDialog? = null
        fun getInstance(): SleepTimerDialog {
            if (instance == null) {
                instance = SleepTimerDialog()
            }
            return instance as SleepTimerDialog
        }
    }


    fun showDialog(context: Context) {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_sleep_timer, null)

        AlertDialog
            .Builder(context, R.style.DialogTheme)
            .setView(view)
            .setNegativeButton("Cancel") { dialogInterface: DialogInterface, _ ->
                dialogInterface.cancel()
            }
            .setPositiveButton("Ok") { _, _ ->
                val textSleepTime: EditText = view.findViewById(R.id.textSleepTime)

                val sleepTime = textSleepTime.text
                val time = sleepTime.toString().toInt() * 1000 * 60
                Timer().schedule(object : TimerTask() {
                    override fun run() {
//                        TODO change mediaPlayer to singleton
                        val mediaPlayer = DaggerAppComponent.create().mediaPlayer()
                        mediaPlayer.stop()
                    }
                }, time.toLong())
            }
            .show()

    }

}