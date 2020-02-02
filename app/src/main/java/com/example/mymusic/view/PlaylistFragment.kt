package com.example.mymusic.view


import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.example.mymusic.R
import com.example.mymusic.ViewModelFactory
import com.example.mymusic.storage.database.AppRepository
import com.example.mymusic.storage.database.Music
import com.example.mymusic.databinding.FragmentPlaylistBinding
import com.example.mymusic.di.component.DaggerViewModelComponent
import com.example.mymusic.viewModel.PlaylistViewModel
import java.util.*
import java.util.concurrent.Executors
import javax.inject.Inject

class PlaylistFragment : Fragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private var musicList: List<Music>? = null
    private lateinit var playlistName: String
    private lateinit var binding: FragmentPlaylistBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        playlistName = PlaylistFragmentArgs.fromBundle(arguments!!).playlistName
        Executors.newCachedThreadPool().execute {
            musicList = AppRepository.getInstance(context!!).getMusicsFromPlaylist(playlistName)
        }

        DaggerViewModelComponent.create().inject(this)

        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_playlist, container, false)
        val viewModel =
            ViewModelProviders.of(this, viewModelFactory).get(PlaylistViewModel::class.java)
        while (musicList == null) {
            Thread.sleep(3)
        }
        viewModel.init(playlistName, musicList!!)
        binding.playlist = viewModel

        loadImg()

        return binding.root
    }

    private fun loadImg() {
        if (musicList!!.isNotEmpty()) {
            val metaRetriever = MediaMetadataRetriever()
            val random = Random()
            Timer().schedule(object : TimerTask() {
                override fun run() {
                    metaRetriever.setDataSource(musicList!![random.nextInt(musicList!!.size)].path)
                    val art = metaRetriever.embeddedPicture
                    if (art != null) {
                        val songImage = BitmapFactory.decodeByteArray(art, 0, art.size)
                        binding.imageSinger.post { binding.imageSinger.setImageBitmap(songImage) }
                    } else binding.imageSinger.post { binding.imageSinger.setImageResource(R.drawable.ic_music) }
                }
            }, 0, 10000)
        }
    }


}
