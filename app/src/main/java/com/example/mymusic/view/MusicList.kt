package com.example.mymusic.view

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.widget.PopupMenu
import android.widget.Toast
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
import com.example.mymusic.viewModel.MusicListViewModel.Companion.horizontalMusicAdapter
import com.example.mymusic.viewModel.MusicListViewModel.Companion.musicAdapter
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.navigation.NavigationView
import java.io.File
import java.io.IOException
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
    private lateinit var navBinding: NavHeaderBinding
    private lateinit var musicList: List<Music>
    private var currentSongIndex: Int = 0

    override fun onMusicClicked(position: Int) {
        PlayMusic.navigationResult = this
        goToFragmentPlayMusic(position)
    }

    override fun onMusicLongClicked(position: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onSubjectClicked(position: Int, view: View) {
        val popup = PopupMenu(activity?.applicationContext, view)
        popup.menuInflater.inflate(R.menu.subject_menu, popup.menu)
        val item = popup.menu.findItem(R.id.addTo)
        addPlayListsToMenu(popup.menu, item.subMenu)

        popup.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.play -> {
                    goToFragmentPlayMusic(position)
                }
                R.id.share -> {
                    val intentShare = Intent(Intent.ACTION_SEND)
                    val uriParse = Uri.parse(musicList[position].path)
                    intentShare.putExtra(Intent.EXTRA_STREAM, uriParse)
                    intentShare.type = "audio/*"
                    startActivity(Intent.createChooser(intentShare, "Share Sound File"))
                }
                R.id.delete -> {
                    val file = File(musicList[position].path)
                    file.delete()
                    if (file.exists()) {
                        try {
                            file.canonicalFile.delete()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                        if (file.exists()) {
                            activity?.applicationContext?.deleteFile(file.name)
                            if (file.exists()) {
                                Thread(Runnable {
                                    val cursor: Cursor? =
                                        activity?.contentResolver?.query(
                                            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                                            arrayOf(
                                                MediaStore.Audio.Media._ID
                                            ),
                                            MediaStore.Audio.Media.DATA + " =?",
                                            arrayOf<String>(file.absolutePath),
                                            null
                                        )
                                    while (cursor!!.moveToNext()) {
                                        val id = cursor.getString(
                                            cursor.getColumnIndexOrThrow(
                                                MediaStore.Audio.Media._ID
                                            )
                                        )
                                        val uri = ContentUris.withAppendedId(
                                            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id.toLong()
                                        )
                                        activity?.contentResolver?.delete(uri, null, null)
                                    }
                                    cursor.close()
                                })
                            }
                        }
                    }
                    Toast.makeText(
                        activity,
                        musicList[position].title.toString() + " is deleted",
                        Toast.LENGTH_SHORT
                    ).show()
                    binding.music = viewModel
                    setMusicListener()
                }
                else -> {
                    if (item.itemId != R.id.addTo) {
                        val music = musicList[position]
                        music.playListName = item.title.toString()
                        AppRepository.getInstance(context!!).insertMusic(musicList[position])
                    }
                }
            }
            false
        }
        popup.show()
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
        onBottomSheetClick()

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
        binding.imageMenu.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }
        binding.navView.setNavigationItemSelectedListener(this)
        addPlayListsToMenu(binding.navView.menu, null)
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
            horizontalMusicAdapter.musicListener = this
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
        // TODO set seek bar
        Thread(Runnable {
            while (!this.isResumed) {
                Thread.sleep(100)
            }
            val bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet)
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }).start()

        binding.invalidateAll()
        navBinding.invalidateAll()
        viewModel.music = musicList[position]
        setMusicListener()

    }

    private fun onBottomSheetClick() {
        binding.bottomSheet.setOnClickListener {
            goToFragmentPlayMusic(currentSongIndex)
        }
    }

    private fun goToFragmentPlayMusic(position: Int) {
        val action = MusicListDirections.actionMusicListToPlayMusic(position)
        controller = activity?.findNavController(R.id.nav_host_fragment)!!
        controller.navigate(action)
    }

    private fun addPlayListsToMenu(menu: Menu, subMenu: SubMenu?) {
        AppRepository.getInstance(context!!).allPlaylists.observe(viewLifecycleOwner, Observer {
            if (it.isNotEmpty()) {
                val menu1 = subMenu ?: menu

                for (int in it.indices)
                    menu1.removeItem(52)
                for (int in it.indices) {
                    if (it[int].playListName != "Favorite") {
                        menu1.add(0, 52, Menu.NONE, it[int].playListName)
                            .setIcon(R.drawable.ic_music)
                    }
                }
            }
        })
    }

    private var musicReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (MusicService.ACTION_MUSIC_COMPLETED == action) {
                val bundle = intent.extras
                currentSongIndex = bundle!!.getInt("currentPosition")
                setNavViewAndBottomShit(currentSongIndex)
            }
        }
    }


}
