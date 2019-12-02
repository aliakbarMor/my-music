package com.example.mymusic.view


import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.example.mymusic.R
import com.example.mymusic.ViewModelFactory
import com.example.mymusic.database.AppRepository
import com.example.mymusic.databinding.FragmentPlayMusicBinding
import com.example.mymusic.database.Music
import com.example.mymusic.di.component.DaggerViewModelComponent
import com.example.mymusic.service.MusicService
import com.example.mymusic.utility.MediaPlayerManager
import com.example.mymusic.utility.getMusics
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
    private var musics: List<Music>? = null
    lateinit var music: Music

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        DaggerViewModelComponent.create().inject(this)
        musics = getMusics(context!!)
        position = PlayMusicArgs.fromBundle(arguments!!).position
        music = musics!![position]
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_play_music, container, false)
        viewModel =
            ViewModelProviders.of(this, viewModelFactory).get(PlayMusicViewModel::class.java)
        viewModel.context = activity
        viewModel.music = music
        binding.music = viewModel

        val intent = Intent(activity, MusicService::class.java)
        intent.putExtra("position", position)
        activity?.startService(intent)

        setIsFavorite()
        setCurrentPosition()
        completeMusic()
        skipPrevious()
        skipNext()
//        sendBroadcast()
        onBackClick()


        return binding.root
    }


    private fun setCurrentPosition() {
        Thread(Runnable {
            while (binding.root.isVisible) {
                Thread.sleep(100)
                viewModel.currentPositionTime.postValue(mediaPlayer.currentPosition)
            }
        }).start()
    }

    private fun setIsFavorite() {
        Executors.newCachedThreadPool().execute {
            val music = AppRepository.getInstance(context!!)
                .getMusic(musics!![position].title!!, musics!![position].artist!!)
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
                    music.playMusic()
                } else if (viewModel.isShuffle) {
                    val rand = Random()
                    position = rand.nextInt(musics!!.size - 1)
                    music = musics!![position]
                    viewModel.music = musics!![position]
                    binding.music = viewModel
                    music.playMusic()
                } else {
                    if (position < musics!!.size - 1) {
                        position++
                        music = musics!![position]
                        viewModel.music = musics!![position]
                        binding.music = viewModel
//                        music.playMusic()
                        val intent = Intent(context, MusicService::class.java)
                        intent.putExtra("position", position)
                        context!!.startService(intent)

                    } else {
                        position = 0
                        music = musics!![position]
                        viewModel.music = musics!![position]
                        binding.music = viewModel
                        music.playMusic()
                    }
                }

            }
        }
    }


    private fun skipPrevious() {
        binding.btnPrevious.setOnClickListener {
            val viewModel =
                ViewModelProviders.of(this, viewModelFactory).get(PlayMusicViewModel::class.java)
            if (position > 0) {
                position--
                music = musics!![position]
            } else {
                position = musics!!.size - 1
                music = musics!![position]
            }
            viewModel.music = musics!![position]
            binding.music = viewModel
            music.playMusic()
        }
    }

    private fun skipNext() {
        binding.btnNext.setOnClickListener {
            val viewModel =
                ViewModelProviders.of(this, viewModelFactory).get(PlayMusicViewModel::class.java)
            if (position < musics!!.size - 1) {
                position++
                music = musics!![position]
            } else {
                position = 0
                music = musics!![position]
            }
            viewModel.music = musics!![position]
            binding.music = viewModel
            music.playMusic()
        }
    }
//    private fun sendBroadcast() {
//        val bundle = Bundle()
//        bundle.putInt("currentPosition", position)
//        val intent = Intent(MusicService.ACTION_MUSIC_COMPLETED)
//        intent.putExtras(bundle)
//        activity!!.sendBroadcast(intent)
//    }

    private fun onBackClick() {
        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }
    }
}
