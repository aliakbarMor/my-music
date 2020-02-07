package com.example.mymusic.view


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaPlayer
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.example.mymusic.R
import com.example.mymusic.ViewModelFactory
import com.example.mymusic.storage.database.AppRepository
import com.example.mymusic.databinding.FragmentPlayMusicBinding
import com.example.mymusic.storage.database.Music
import com.example.mymusic.di.component.DaggerViewModelComponent
import com.example.mymusic.notification.MusicNotification
import com.example.mymusic.service.MusicService
import com.example.mymusic.storage.sharedPrefs.PrefsManager
import com.example.mymusic.utility.MediaPlayerManager
import com.example.mymusic.utility.getMusics
import com.example.mymusic.utility.musics
import com.example.mymusic.view.MusicListFragment.Companion.isCustomListMode
import com.example.mymusic.view.MusicListFragment.Companion.isFilteredListMode
import com.example.mymusic.view.PlaylistFragment.Companion.isInPlaylist
import com.example.mymusic.viewModel.PlayMusicViewModel
import java.util.*
import java.util.concurrent.Executors
import javax.inject.Inject


class PlayMusic : Fragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private var mediaPlayer: MediaPlayer = MediaPlayerManager.getInstance()

    private lateinit var binding: FragmentPlayMusicBinding
    private lateinit var viewModel: PlayMusicViewModel

    private var position: Int = 0
    private var musicsList: List<Music>? = null
    lateinit var music: Music

    private lateinit var contextCatch: Context
    private lateinit var intent: Intent

    companion object {
        var navigationResult: NavigationResult? = null
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        contextCatch = activity!!.applicationContext
        intent = Intent(contextCatch, MusicService::class.java)

        DaggerViewModelComponent.create().inject(this)
        musicsList = if (isCustomListMode || isFilteredListMode || isInPlaylist) {
            musics
        } else
            getMusics(context!!)

        position = PlayMusicArgs.fromBundle(arguments!!).position
        music = musicsList!![position]
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_play_music, container, false)
        viewModel =
            ViewModelProviders.of(this, viewModelFactory).get(PlayMusicViewModel::class.java)
        viewModel.context = activity
        viewModel.music = music
        binding.music = viewModel


        binding.btnPrevious.setOnClickListener {
            skipPrevious()
        }
        binding.btnNext.setOnClickListener {
            skipNext()
        }

        setIsFavorite()
        setCurrentPosition()
        completeMusic()
        backHandle()

        val lastMusicPlayed = PrefsManager(context!!).loadLastMusicPlayed()
        if (mediaPlayer.isPlaying && music.artist == lastMusicPlayed.artist && music.title == lastMusicPlayed.title) {
            return binding.root
        }

        intent.putExtra("position", position)
        activity?.startService(intent)

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        val musicIntentFilter = IntentFilter()
        musicIntentFilter.addAction(MusicNotification.ACTION_MUSIC_SKIP_NEXT)
        musicIntentFilter.addAction(MusicNotification.ACTION_MUSIC_STOP)
        musicIntentFilter.addAction(MusicNotification.ACTION_MUSIC_SKIP_PREVIOUS)
        activity!!.registerReceiver(notificationReceiver, musicIntentFilter)
    }

    private fun setCurrentPosition() {
        Thread(Runnable {
            while (binding.root.isVisible) {
                Thread.sleep(10)
                viewModel.currentPositionTime.postValue(mediaPlayer.currentPosition)
            }
        }).start()
    }

    private fun setIsFavorite() {
        Executors.newCachedThreadPool().execute {
            val music: Music? = AppRepository.getInstance(context!!)
                .getMusic(musicsList!![position].title!!, musicsList!![position].artist!!)
            if (music == null) {
                viewModel.isFavorite.postValue(false)
            } else {
                viewModel.isFavorite.postValue(true)
            }
        }
    }

    private fun completeMusic() {
        mediaPlayer.setOnCompletionListener {
            if (viewModel.currentPositionTime.value!! - music.duration!!.toInt() < 1000 &&
                viewModel.currentPositionTime.value!! - music.duration!!.toInt() > -1000
            ) {
                if (viewModel.isRepeat.value!!) {
                    intent.putExtra("position", position)
                    contextCatch.startService(intent)
                } else if (viewModel.isShuffle) {
                    val rand = Random()
                    position = rand.nextInt(musicsList!!.size - 1)
                    music = musicsList!![position]
                    viewModel.music = musicsList!![position]
                    binding.music = viewModel
                    intent.putExtra("position", position)
                    contextCatch.startService(intent)
                } else {
                    if (position < musicsList!!.size - 1) {
                        position++
                        music = musicsList!![position]
                        viewModel.music = musicsList!![position]
                        binding.music = viewModel
                        intent.putExtra("position", position)
                        contextCatch.startService(intent)

                    } else {
                        position = 0
                        music = musicsList!![position]
                        viewModel.music = musicsList!![position]
                        binding.music = viewModel
                        intent.putExtra("position", position)
                        contextCatch.startService(intent)
                    }
                }
                setIsFavorite()
            }
        }
    }

    private fun skipPrevious() {
        if (position > 0) {
            position--
            music = musicsList!![position]
        } else {
            position = musicsList!!.size - 1
            music = musicsList!![position]
        }
        viewModel.music = musicsList!![position]
        binding.music = viewModel
        intent.putExtra("position", position)
        contextCatch.startService(intent)
        setIsFavorite()
    }

    private fun skipNext() {
        if (position < musicsList!!.size - 1) {
            position++
            music = musicsList!![position]
        } else {
            position = 0
            music = musicsList!![position]
        }
        viewModel.music = musicsList!![position]
        binding.music = viewModel
        intent.putExtra("position", position)
        contextCatch.startService(intent)
        setIsFavorite()
    }

    private fun backHandle() {
        requireActivity().onBackPressedDispatcher.addCallback(this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    back()
                }
            })
        binding.backButton.setOnClickListener {
            back()
        }
    }

    private fun back() {
        findNavController().popBackStack()
        val bundle = Bundle()
        bundle.putInt("currentPosition", position)
        navigationResult?.onNavigationResult(bundle)
    }

    private val notificationReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            when (intent.action) {
                MusicNotification.ACTION_MUSIC_SKIP_NEXT -> {
                    skipNext()
                }
                MusicNotification.ACTION_MUSIC_SKIP_PREVIOUS -> {
                    skipPrevious()
                }
                MusicNotification.ACTION_MUSIC_STOP -> {
                    viewModel.onPauseAndPlayClicked()
                }
            }
        }
    }
}
