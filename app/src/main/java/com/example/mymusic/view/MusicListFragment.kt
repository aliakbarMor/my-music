package com.example.mymusic.view

import android.Manifest
import android.annotation.SuppressLint
import android.content.*
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.*
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.example.mymusic.R
import com.example.mymusic.ViewModelFactory
import com.example.mymusic.databinding.FragmentMusicListBinding
import com.example.mymusic.databinding.NavHeaderBinding
import com.example.mymusic.di.component.DaggerViewModelComponent
import com.example.mymusic.service.MusicService
import com.example.mymusic.storage.database.AppRepository
import com.example.mymusic.storage.database.Music
import com.example.mymusic.storage.sharedPrefs.PrefsManager
import com.example.mymusic.utility.getMusics
import com.example.mymusic.utility.musics
import com.example.mymusic.view.adapter.MusicAdapter.Companion.musicSelected
import com.example.mymusic.view.adapter.MusicListener
import com.example.mymusic.viewModel.MusicListViewModel
import com.example.mymusic.viewModel.MusicListViewModel.Companion.horizontalMusicAdapter
import com.example.mymusic.viewModel.MusicListViewModel.Companion.musicAdapter
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.navigation.NavigationView
import java.io.File
import java.io.IOException
import javax.inject.Inject


class MusicListFragment : Fragment(), MusicListener,
    NavigationView.OnNavigationItemSelectedListener, NavigationResult {

    companion object {
        private const val MY_PERMISSIONS_MUSIC: Int = 10001
        var isCustomListMode = false
        var isFilteredListMode = false
        var selectedMode = false
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private lateinit var viewModel: MusicListViewModel
    private lateinit var binding: FragmentMusicListBinding
    private lateinit var navBinding: NavHeaderBinding
    private lateinit var musicList: List<Music>
    private lateinit var controller: NavController
    private lateinit var prefsManager: PrefsManager
    private var currentSongIndex: Int = 0

    override fun onMusicClicked(position: Int) {
        if (selectedMode) {
            toggleToolbar()
        } else {
            goToFragmentPlayMusic(position)
        }
    }

    override fun onMusicLongClicked(position: Int) {
        toggleToolbar()
    }

    override fun onSubjectClicked(position: Int, view: View) {
        val popup = PopupMenu(activity?.applicationContext, view)
        popup.menuInflater.inflate(R.menu.subject_menu, popup.menu)
        val itemAddTo = popup.menu.findItem(R.id.addTo)
        addPlayListsToMenuExceptFavorite(popup.menu, itemAddTo.subMenu)

        popup.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.play -> {
                    goToFragmentPlayMusic(position)
                }
                R.id.playNext -> {
                    playingNextSong(position)
                }
                R.id.share -> {
                    val intentShare = Intent(Intent.ACTION_SEND)
                    val uriParse = Uri.parse(musicList[position].path)
                    intentShare.putExtra(Intent.EXTRA_STREAM, uriParse)
                    intentShare.type = "audio/*"
                    startActivity(Intent.createChooser(intentShare, "Share Sound File"))
                }
                R.id.delete -> {
                    val file = File(musicList[position].path!!)
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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_music_list, container, false)
        viewModel =
            ViewModelProviders.of(this, viewModelFactory).get(MusicListViewModel::class.java)
        viewModel.musicList = musicList
        binding.music = viewModel

        navBinding = DataBindingUtil.inflate(inflater, R.layout.nav_header, container, false)
        navBinding.music = viewModel
        binding.navView.addHeaderView(navBinding.root)

        controller = activity?.findNavController(R.id.nav_host_fragment)!!
        prefsManager = PrefsManager(context!!)

        addNavViewMenu()
        setOnBackClick()
        setMusicListener()
        onBottomSheetClick()
        filter()

        loadLastedMusic()
        PlaylistFragment.isInPlaylist = false

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        val musicIntentFilter = IntentFilter()
        musicIntentFilter.addAction(MusicService.ACTION_MUSIC_STARTED)
        musicIntentFilter.addAction(MusicService.ACTION_MUSIC_COMPLETED)
        musicIntentFilter.addAction(MusicService.ACTION_MUSIC_IN_PROGRESS)
        LocalBroadcastManager.getInstance(activity!!)
            .registerReceiver(musicReceiver, musicIntentFilter)
    }

    override fun onNavigationResult(result: Bundle) {
        selectedMode = false
//        val currentSongIndex = result.getInt("currentPosition")
//        setNavViewAndBottomShit(currentSongIndex)

    }

    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.sleepTimer -> {
                binding.drawerLayout.closeDrawer(GravityCompat.START)
                SleepTimerDialog.getInstance().showDialog(activity!!)
            }
            R.id.about -> {
                binding.drawerLayout.closeDrawer(GravityCompat.START)
                val action = MusicListFragmentDirections.actionMusicListToAboutFragment()
                controller.navigate(action)
            }
            R.id.addNewPlaylist -> {
                binding.drawerLayout.closeDrawer(GravityCompat.START)
                AddNewPlaylistDialog.getInstance().showDialog(activity!!)
            }
            else -> {
                binding.drawerLayout.closeDrawer(GravityCompat.START)
                val action =
                    MusicListFragmentDirections.actionMusicListToPlaylistFragment(menuItem.title.toString())
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
        addPlayListsToMenuExceptFavorite(binding.navView.menu, null)
    }

    private fun setOnBackClick() {
        activity?.onBackPressedDispatcher?.addCallback {
            if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                binding.drawerLayout.closeDrawer(GravityCompat.START)
            } else if (selectedMode) {
                musicSelected.clear()
                musicAdapter.notifyDataSetChanged()
                toggleToolbar()
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

    private fun loadLastedMusic() {
        val music = prefsManager.loadLastMusicPlayed()
        if (music.artist != null) {
            setNavViewAndBottomShit(music)
        }
    }

    private fun setNavViewAndBottomShit(music: Music) {
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
        viewModel.music = music

        setMusicListener()

    }

    private fun onBottomSheetClick() {
        binding.bottomSheet.setOnClickListener {
            currentSongIndex = prefsManager.loadLastIndexMusic()
            goToFragmentPlayMusic(currentSongIndex)
        }
    }

    private fun goToFragmentPlayMusic(position: Int) {
        PlayMusic.navigationResult = this
        val action = MusicListFragmentDirections.actionMusicListToPlayMusic(position)
        controller.navigate(action)
    }

    private fun addPlayListsToMenuExceptFavorite(menu: Menu, subMenu: SubMenu?) {
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

    private fun filter() {
        binding.textSearch.addTextChangedListener {
            search()
        }
        binding.imgSearch.setOnClickListener {
            search()
        }
    }

    @SuppressLint("DefaultLocale")
    private fun search() {
        val filteredMusic = ArrayList<Music>()
        for (music in musicList) {
            if (music.title!!.toLowerCase().contains(binding.textSearch.text.toString().toLowerCase()) ||
                music.artist!!.toLowerCase().contains(binding.textSearch.text.toString().toLowerCase())
            ) {
                filteredMusic.add(music)
            }
        }
        isFilteredListMode = binding.textSearch.toString() != ""
        musicAdapter.filterList(filteredMusic)
        musics = filteredMusic
    }

    private fun playingNextSong(position: Int) {
        isCustomListMode = true
        musics?.add(currentSongIndex + 1, musicList[position])
        if (currentSongIndex < position) {
            musics?.removeAt(position + 1)
        } else
            musics?.removeAt(position)
        musicList = musics!!
        musicAdapter.notifyDataSetChanged()
    }

    @SuppressLint("SetTextI18n")
    private fun toggleToolbar() {
        if (musicSelected.size != 0) {
            selectedMode = true
            binding.mainToolbar.visibility = View.GONE
            binding.actionSelectToolbar.visibility = View.VISIBLE
        } else {
            selectedMode = false
            binding.mainToolbar.visibility = View.VISIBLE
            binding.actionSelectToolbar.visibility = View.GONE
        }

        binding.imageDisable.setOnClickListener {
            disableSelectedMode()
        }
        binding.numberOfSelected.text = musicSelected.size.toString() + " selected"
        binding.actionAddTo.setOnClickListener {
            val popup = PopupMenu(activity?.applicationContext, binding.actionAddTo)
            popup.menu.add(0, 51, Menu.NONE, "Favorite")
            addPlayListsToMenuExceptFavorite(popup.menu, null)
            popup.setOnMenuItemClickListener {
                val someMusicAddToPlaylist = ArrayList<Music>()
                for (i in 0 until musicSelected.size) {
                    val music = musicSelected[i]
                    music.playListName = it.title.toString()
                    someMusicAddToPlaylist.add(music)
                }
                AppRepository.getInstance(context!!).insertSomeMusics(someMusicAddToPlaylist)
                disableSelectedMode()
                false
            }
            popup.show()
        }
        binding.actionPlayNext.setOnClickListener {
            for (pos in musicAdapter.musicPositionSelected) {
                playingNextSong(pos)
            }
            disableSelectedMode()
        }
    }

    private fun disableSelectedMode() {
        musicSelected.clear()
        musicAdapter.notifyDataSetChanged()
        toggleToolbar()
    }

    private var musicReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {

            val action = intent.action
            val bundle = intent.extras
            if (action == MusicService.ACTION_MUSIC_STARTED) {
                currentSongIndex = bundle!!.getInt("currentPosition")
                val music = if (isCustomListMode || isFilteredListMode) {
                    musics!![currentSongIndex]
                } else musicList[currentSongIndex]
                setNavViewAndBottomShit(music)
                prefsManager.saveLastMusicPlayed(music)
                prefsManager.saveLastIndexMusic(currentSongIndex)
            } else if (action == MusicService.ACTION_MUSIC_IN_PROGRESS) {
                viewModel.currentPositionTime.postValue(bundle!!.getInt("currentPositionTime"))
            }
        }
    }


}
