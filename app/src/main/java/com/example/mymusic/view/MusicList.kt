package com.example.mymusic.view

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.activity.addCallback
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.example.mymusic.R
import com.example.mymusic.ViewModelFactory
import com.example.mymusic.database.AppRepository
import com.example.mymusic.databinding.FragmentMusicListBinding
import com.example.mymusic.di.component.DaggerViewModelComponent
import com.example.mymusic.service.MusicService
import com.example.mymusic.utility.getMusics
import com.example.mymusic.view.adapter.MusicListener
import com.example.mymusic.viewModel.MusicListViewModel
import com.example.mymusic.viewModel.MusicListViewModel.Companion.musicAdapter
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.fragment_music_list.*
import kotlinx.android.synthetic.main.nav_header.*
import javax.inject.Inject


class MusicList : Fragment(), MusicListener, NavigationView.OnNavigationItemSelectedListener {

    companion object {
        private const val MY_PERMISSIONS_MUSIC: Int = 10001
    }

    private var binding: FragmentMusicListBinding? = null
    private var controller: NavController? = null
    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    override fun onMusicClicked(position: Int) {
        val action = MusicListDirections.actionMusicListToPlayMusic(position)
        controller = activity?.findNavController(R.id.nav_host_fragment)
        controller?.navigate(action)

    }

    override fun onMusicLongClicked(position: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        checkPermission()

        DaggerViewModelComponent.create().inject(this)
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_music_list, container, false)
        val viewModel =
            ViewModelProviders.of(this, viewModelFactory).get(MusicListViewModel::class.java)
        viewModel.musicList = getMusics(context!!)
        binding!!.music = viewModel


        addNavViewMenu()
        setOnBackClick()
        setMusicListener()

        return binding!!.root
    }

    override fun onResume() {
        super.onResume()
        val musicIntentFilter = IntentFilter()
        musicIntentFilter.addAction(MusicService.ACTION_MUSIC_COMPLETED)
        musicIntentFilter.addAction(MusicService.ACTION_MUSIC_IN_PROGRESS)
        activity!!.registerReceiver(musicReceiver, musicIntentFilter)
    }

    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.sleepTimer -> {
                binding!!.drawerLayout.closeDrawer(GravityCompat.START)
                SleepTimerDialog.getInstance().showDialog(activity!!)
            }
            R.id.about -> {
//                TODO
            }
            R.id.addNewPlaylist -> {
                binding!!.drawerLayout.closeDrawer(GravityCompat.START)
                AddNewPlaylistDialog.getInstance().showDialog(activity!!)
            }
            else -> {
                binding!!.drawerLayout.closeDrawer(GravityCompat.START)
                val action =
                    MusicListDirections.actionMusicListToPlaylistFragment(menuItem.title.toString())
                controller = activity?.findNavController(R.id.nav_host_fragment)
                controller?.navigate(action)
            }
        }
        return false
    }

    private fun addNavViewMenu() {
        binding!!.navView.setNavigationItemSelectedListener(this)
        AppRepository.getInstance(context!!).allPlaylists.observe(viewLifecycleOwner, Observer {
            if (it.isNotEmpty()) {
                for (int in it.indices)
                    binding!!.navView.menu.removeItem(52)

                for (int in it.indices) {
                    if (it[int].playListName != "Favorite") {
                        binding!!.navView.menu.add(0, 52, Menu.NONE, it[int].playListName)
                            .setIcon(R.drawable.ic_music)
                    }
                }
            }
        })
    }

    private fun setOnBackClick() {
        activity?.onBackPressedDispatcher?.addCallback {
            if (binding!!.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                binding!!.drawerLayout.closeDrawer(GravityCompat.START)
            } else if (!controller!!.popBackStack()) {
                activity!!.finish()
            }
        }
    }

    private fun setMusicListener() {
        Thread(Runnable {
            Thread.sleep(1000)
            musicAdapter.musicListener = this
        }).start()
    }

    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(
                context!!,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            activity?.let {
                ActivityCompat.requestPermissions(
                    it,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    MY_PERMISSIONS_MUSIC
                )
            }
        }
    }

    private var musicReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {

            Log.d("aaaaaaaaaaaaaa","broadcast has send")


            val metaRetriever = MediaMetadataRetriever()
            val musicList = getMusics(context)

            val action = intent.action
            if (MusicService.ACTION_MUSIC_COMPLETED == action) {
                val bundle = intent.extras
                val currentSongIndex = bundle!!.getInt("currentPosition")

                binding!!.textArtistAndTitle.text =
                    musicList[currentSongIndex].title!! + musicList[currentSongIndex].artist!!
//                textTitleNavigation.text = musicList[currentSongIndex].title!!
//                textArtistNavigation.text = musicList[currentSongIndex].artist!!
                metaRetriever.setDataSource(musicList[currentSongIndex].path!!)
                val art: ByteArray = metaRetriever.embeddedPicture
                Log.d("aaaaaaaaaaaaaa",binding!!.textArtistAndTitle.text.toString())
                if (art != null) {
                    val songImage = BitmapFactory.decodeByteArray(art, 0, art.size)
                    binding!!.imageArtist.setImageBitmap(songImage)
                    val bitmapRequestBuilder = Glide
                        .with(context)
                        .asBitmap()
                        .load(songImage)
                        .centerCrop()
//                    imageViewNavigation.post {
//                        bitmapRequestBuilder.into(imageViewNavigation)
//                    }
                } else {
                    binding!!.imageArtist.setImageResource(R.drawable.ic_music)
//                    imageViewNavigation.setImageResource(R.drawable.ic_music)
                }
            }
        }
    }
}
