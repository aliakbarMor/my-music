package com.example.mymusic.storage.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase


@Database(entities = [Music::class, Playlist::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract val musicDAO: MusicDAO
    abstract val playlistDAO: PlaylistDAO

    companion object {
        private const val DATABASE_NAME = "app.db"
        @Volatile
        private var instance: AppDatabase? = null
        private var LOCK = Any()

        fun getInstance(context: Context): AppDatabase? {
            if (instance == null) {
                synchronized(LOCK) {
                    if (instance == null) {
                        instance =
                            Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME)
                                .addCallback(object : Callback() {
                                    override fun onCreate(db: SupportSQLiteDatabase) {
                                        super.onCreate(db)
                                        val values = ContentValues()
                                        values.put("playListName", "Favorite")
                                        db.insert("PlayLists", SQLiteDatabase.CONFLICT_IGNORE, values)
                                    }
                                })
                                .build()
                    }
                }
            }
            return instance
        }
    }


}