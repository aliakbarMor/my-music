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
import androidx.navigation.findNavController
import com.example.mymusic.R
import com.example.mymusic.ViewModelFactory
import com.example.mymusic.storage.database.AppRepository
import com.example.mymusic.storage.database.Music
import com.example.mymusic.databinding.FragmentPlaylistBinding
import com.example.mymusic.di.component.DaggerViewModelComponent
import com.example.mymusic.utility.musics
import com.example.mymusic.view.adapter.MusicListener
import com.example.mymusic.viewModel.PlaylistViewModel
import com.example.mymusic.viewModel.PlaylistViewModel.Companion.musicAdapter
import java.util.*
import java.util.concurrent.Executors
import javax.inject.Inject
import kotlin.collections.ArrayList

class PlaylistFragment : Fragment(), MusicListener {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private var musicList: List<Music>? = null
    private lateinit var playlistName: String
    private lateinit var binding: FragmentPlaylistBinding

    companion object {
        var isInPlaylist = false
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        isInPlaylist = true
        playlistName = PlaylistFragmentArgs.fromBundle(requireArguments()).playlistName
        Executors.newCachedThreadPool().execute {
            musicList = AppRepository.getInstance(requireContext()).getMusicsFromPlaylist(playlistName)
        }
//        DaggerViewModelComponent.create().inject(this)
        DaggerViewModelComponent.builder().application(requireActivity().application).build().inject(this)


        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_playlist, container, false)
        val viewModel =
            ViewModelProviders.of(this, viewModelFactory).get(PlaylistViewModel::class.java)
        while (musicList == null) {
            Thread.sleep(3)
        }
        musics = (musicList as java.util.ArrayList<Music>?)!!
        viewModel.init(playlistName, musicList!!)
        binding.playlist = viewModel

        loadImg()
        setMusicListener()

        return binding.root
    }

    override fun onMusicClicked(position: Int, isMostPlayedList: Boolean) {
        val action =
            PlaylistFragmentDirections.actionPlaylistFragmentToPlayMusic(position, playlistName)
        requireActivity().findNavController(R.id.nav_host_fragment).navigate(action)
    }

    override fun onMusicLongClicked(position: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onSubjectClicked(position: Int, isMostPlayedList: Boolean, view: View) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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

    private fun setMusicListener() {
        Thread(Runnable {
            Thread.sleep(1000)
            musicAdapter.musicListener = this
        }).start()
    }

}
