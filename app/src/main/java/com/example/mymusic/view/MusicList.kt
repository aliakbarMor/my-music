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
import com.example.mymusic.R
import com.example.mymusic.ViewModelFactory
import com.example.mymusic.database.AppRepository
import com.example.mymusic.database.Music
import com.example.mymusic.databinding.FragmentMusicListBinding
import com.example.mymusic.databinding.NavHeaderBinding
import com.example.mymusic.di.component.DaggerViewModelComponent
import com.example.mymusic.service.MusicService
import com.example.mymusic.utility.getMusics
import com.example.mymusic.view.adapter.MusicListener
import com.example.mymusic.viewModel.MusicListViewModel
import com.example.mymusic.viewModel.MusicListViewModel.Companion.musicAdapter
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.navigation.NavigationView
import javax.inject.Inject


class MusicList : Fragment(), MusicListener, NavigationView.OnNavigationItemSelectedListener,
    NavigationResult {

    companion object {
        private const val MY_PERMISSIONS_MUSIC: Int = 10001
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private lateinit var viewModel: MusicListViewModel
    private lateinit var controller: NavController
    private lateinit var binding: FragmentMusicListBinding
    lateinit var navBinding: NavHeaderBinding
    private lateinit var musicList: List<Music>

    override fun onMusicClicked(position: Int) {
        PlayMusic.navigationResult = this
        val action = MusicListDirections.actionMusicListToPlayMusic(position)
        controller = activity?.findNavController(R.id.nav_host_fragment)!!
        controller.navigate(action)
    }

    override fun onMusicLongClicked(position: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        checkPermission()

        musicList = getMusics(activity!!.applicationContext)
        DaggerViewModelComponent.create().inject(this)
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_music_list, container, false)
        viewModel =
            ViewModelProviders.of(this, viewModelFactory).get(MusicListViewModel::class.java)
        viewModel.musicList = getMusics(context!!)
        binding.music = viewModel

        navBinding = DataBindingUtil.inflate(inflater, R.layout.nav_header, container, false)
        navBinding.music = viewModel
        binding.navView.addHeaderView(navBinding.root)

        addNavViewMenu()
        setOnBackClick()
        setMusicListener()

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        val musicIntentFilter = IntentFilter()
        musicIntentFilter.addAction(MusicService.ACTION_MUSIC_COMPLETED)
        musicIntentFilter.addAction(MusicService.ACTION_MUSIC_IN_PROGRESS)
        activity!!.registerReceiver(musicReceiver, musicIntentFilter)
    }

    override fun onNavigationResult(result: Bundle) {
        val currentSongIndex = result.getInt("currentPosition")
//        setNavViewAndBottomShit(currentSongIndex)

    }

    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.sleepTimer -> {
                binding.drawerLayout.closeDrawer(GravityCompat.START)
                SleepTimerDialog.getInstance().showDialog(activity!!)
            }
            R.id.about -> {
//                TODO
            }
            R.id.addNewPlaylist -> {
                binding.drawerLayout.closeDrawer(GravityCompat.START)
                AddNewPlaylistDialog.getInstance().showDialog(activity!!)
            }
            else -> {
                binding.drawerLayout.closeDrawer(GravityCompat.START)
                val action =
                    MusicListDirections.actionMusicListToPlaylistFragment(menuItem.title.toString())
                controller = activity?.findNavController(R.id.nav_host_fragment)!!
                controller.navigate(action)
            }
        }
        return false
    }

    private fun addNavViewMenu() {
        binding.navView.setNavigationItemSelectedListener(this)
        AppRepository.getInstance(context!!).allPlaylists.observe(viewLifecycleOwner, Observer {
            if (it.isNotEmpty()) {
                for (int in it.indices)
                    binding.navView.menu.removeItem(52)

                for (int in it.indices) {
                    if (it[int].playListName != "Favorite") {
                        binding.navView.menu.add(0, 52, Menu.NONE, it[int].playListName)
                            .setIcon(R.drawable.ic_music)
                    }
                }
            }
        })
    }

    private fun setOnBackClick() {
        activity?.onBackPressedDispatcher?.addCallback {
            if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                binding.drawerLayout.closeDrawer(GravityCompat.START)
            } else if (!controller.popBackStack()) {
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

    private fun setNavViewAndBottomShit(position: Int) {


        Thread(Runnable {
            while (!this.isResumed) {
                Log.d("aaaaaaaaaaaaa", this.isResumed.toString())
                Thread.sleep(100)
                val bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet)
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }).start()


        navBinding.invalidateAll()
        viewModel.music = musicList[position]


        binding.notifyPropertyChanged(R.id.bottomSheet)
        binding.notifyPropertyChanged(R.id.textArtistBottomSheet)
        binding.notifyPropertyChanged(R.id.textTitleBottomSheet)

        val metaRetriever = MediaMetadataRetriever()
        metaRetriever.setDataSource(musicList[position].path)
        if (metaRetriever.embeddedPicture != null) {
            val art: ByteArray = metaRetriever.embeddedPicture
            val songImage = BitmapFactory.decodeByteArray(art, 0, art.size)
            binding.imageArtistBottomSheet.setImageBitmap(songImage)
        } else binding.imageArtistBottomSheet.setImageResource(R.drawable.ic_music)
    }

    private var musicReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (MusicService.ACTION_MUSIC_COMPLETED == action) {
                val bundle = intent.extras
                val currentSongIndex = bundle!!.getInt("currentPosition")
                setNavViewAndBottomShit(currentSongIndex)
            }
        }
    }


}
