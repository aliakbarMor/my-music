package com.example.mymusic.view

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.util.Log
import android.view.LayoutInflater
import androidx.databinding.DataBindingUtil
import com.example.mymusic.R
import com.example.mymusic.database.AppRepository
import com.example.mymusic.database.Playlist
import com.example.mymusic.databinding.DialogAddNewPlaylistBinding

class AddNewPlaylistDialog private constructor() {
    companion object {
        private var instance: AddNewPlaylistDialog? = null
        fun getInstance(): AddNewPlaylistDialog {
            if (instance == null) {
                instance = AddNewPlaylistDialog()
            }
            return instance as AddNewPlaylistDialog
        }
    }

    fun showDialog(context: Context) {
        val inflater = LayoutInflater.from(context)
        val dialogBinding: DialogAddNewPlaylistBinding =
            DataBindingUtil.inflate(inflater, (R.layout.dialog_add_new_playlist), null, false)
        val playlist = Playlist()
        dialogBinding.playlist = playlist

        AlertDialog
            .Builder(context, R.style.DialogTheme)
            .setView(dialogBinding.root)
            .setNegativeButton("Cancel") { dialogInterface: DialogInterface, _ ->
                dialogInterface.cancel()
            }
            .setPositiveButton("Save") { _, _ ->
                AppRepository.getInstance(context).insertPlaylist(playlist)
            }
            .show()
    }
}