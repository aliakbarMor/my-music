package com.example.mymusic.view

import android.Manifest
import android.annotation.SuppressLint
import android.content.*
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.MediaMetadataRetriever
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
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
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
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList


class MusicListFragment : Fragment(), MusicListener,
    NavigationView.OnNavigationItemSelectedListener, NavigationResult {

    companion object {
        private const val MY_PERMISSIONS_MUSIC: Int = 10001
        var isCustomListMode = false
        var isFilteredListMode = false
        var selectedMode = false
        var isInPlaylist = false
        var onMostPlayedListClick = false
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private lateinit var viewModel: MusicListViewModel
    private lateinit var binding: FragmentMusicListBinding
    private lateinit var navBinding: NavHeaderBinding
    private var musicList: List<Music>? = null
    private lateinit var controller: NavController
    private lateinit var prefsManager: PrefsManager
    private var currentSongIndex: Int = 0

    override fun onMusicClicked(position: Int, isMostPlayedList: Boolean) {
        if (selectedMode) {
            toggleToolbar()
        } else {
            goToFragmentPlayMusic(position)
            onMostPlayedListClick = isMostPlayedList
        }
    }

    override fun onMusicLongClicked(position: Int) {
        toggleToolbar()
    }

    override fun onSubjectClicked(position: Int, isMostPlayedList: Boolean, view: View) {
        val popup = PopupMenu(activity?.applicationContext, view)
        popup.menuInflater.inflate(R.menu.subject_menu, popup.menu)
        val itemAddTo = popup.menu.findItem(R.id.addTo)
        addPlayListsToMenuExceptFavorite(popup.menu, itemAddTo.subMenu)

        popup.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.play -> {
                    onMostPlayedListClick = isMostPlayedList
                    goToFragmentPlayMusic(position)
                }
                R.id.playNext -> {
                    playingNextSong(position)
                }
                R.id.share -> {
                    val intentShare = Intent(Intent.ACTION_SEND)
                    val uriParse = Uri.parse(musicList!![position].path)
                    intentShare.putExtra(Intent.EXTRA_STREAM, uriParse)
                    intentShare.type = "audio/*"
                    startActivity(Intent.createChooser(intentShare, "Share Sound File"))
                }
                R.id.delete -> {
                    val file = File(musicList!![position].path!!)
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
                        musicList!![position].title.toString() + " is deleted",
                        Toast.LENGTH_SHORT
                    ).show()
                    binding.music = viewModel
                    setMusicListener()
                }
                else -> {
                    if (item.itemId != R.id.addTo) {
                        val music = musicList!![position]
                        music.playListName = item.title.toString()
                        AppRepository.getInstance(requireContext())
                            .insertMusic(musicList!![position])
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
    ): View {
        checkPermission()
        onMostPlayedListClick = false
        DaggerViewModelComponent.builder().application(requireActivity().application).build().inject(this)
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_music_list, container, false)
        viewModel =
            ViewModelProvider(this, viewModelFactory).get(MusicListViewModel::class.java)

        val playlistName: String = MusicListFragmentArgs.fromBundle(requireArguments()).playlistName
        if (playlistName != "mainMusicList") {
            musicList = viewModel.getMusicList(playlistName, requireContext())
            binding.horizontalMusicList.visibility = View.GONE
            binding.appBarLayout.setBackgroundColor(Color.WHITE)
            binding.mainToolbar.visibility = View.GONE
            binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            loadImg()
            isInPlaylist = true
        } else {
            musicList = viewModel.getMusicList("mainMusicList", requireContext())
            viewModel.getMusicListHorizontal(requireContext())
            isInPlaylist = false
        }

        binding.music = viewModel

        navBinding = DataBindingUtil.inflate(inflater, R.layout.nav_header, container, false)
        navBinding.music = viewModel
        binding.navView.addHeaderView(navBinding.root)

        controller = activity?.findNavController(R.id.nav_host_fragment)!!
        prefsManager = PrefsManager(requireContext())

        addNavViewMenu()
        setOnBackClick()
        setMusicListener()
        onBottomSheetClick()
        filter()

        loadLastedMusic()

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        val musicIntentFilter = IntentFilter()
        musicIntentFilter.addAction(MusicService.ACTION_MUSIC_STARTED)
        musicIntentFilter.addAction(MusicService.ACTION_MUSIC_COMPLETED)
        musicIntentFilter.addAction(MusicService.ACTION_MUSIC_IN_PROGRESS)
        LocalBroadcastManager.getInstance(requireActivity())
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
                SleepTimerDialog.getInstance().showDialog(requireActivity())
            }
            R.id.about -> {
                binding.drawerLayout.closeDrawer(GravityCompat.START)
                val action = MusicListFragmentDirections.actionMusicListToAboutFragment()
                controller.navigate(action)
            }
            R.id.addNewPlaylist -> {
                binding.drawerLayout.closeDrawer(GravityCompat.START)
                AddNewPlaylistDialog.getInstance().showDialog(requireActivity())
            }
            else -> {
                binding.drawerLayout.closeDrawer(GravityCompat.START)
                val action =
                    MusicListFragmentDirections.actionMusicListSelf(menuItem.title.toString())
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
                requireActivity().finish()
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
        val havePermission = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
        if (!havePermission) {
            activity?.let {
                ActivityCompat.requestPermissions(
                    it,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    MY_PERMISSIONS_MUSIC
                )
            }
        }
        if (!havePermission) {
            Thread.sleep(100)
            checkPermission()
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
//        PlayMusic.navigationResult = this
        val action = MusicListFragmentDirections.actionMusicListToPlayMusic(position)
        controller.navigate(action)
    }

    private fun addPlayListsToMenuExceptFavorite(menu: Menu, subMenu: SubMenu?) {
        AppRepository.getInstance(requireContext()).allPlaylists.observe(
            viewLifecycleOwner,
            Observer {
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
        for (music in musicList!!) {
            if (music.title!!.toLowerCase()
                    .contains(binding.textSearch.text.toString().toLowerCase()) ||
                music.artist!!.toLowerCase()
                    .contains(binding.textSearch.text.toString().toLowerCase())
            ) {
                filteredMusic.add(music)
            }
        }
        isFilteredListMode = binding.textSearch.text.toString() != ""
        musicAdapter.filterList(filteredMusic)
        musics = filteredMusic
    }

    private fun playingNextSong(position: Int) {
        isCustomListMode = true
        musics?.add(currentSongIndex + 1, musicList!![position])
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
            binding.actionSelectToolbar.visibility = View.GONE
            if (!isInPlaylist)
                binding.mainToolbar.visibility = View.VISIBLE
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
                AppRepository.getInstance(requireContext()).insertSomeMusics(someMusicAddToPlaylist)
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

    private fun loadImg() {
        binding.imageSinger.visibility = View.VISIBLE
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
            }, 0, 6000)
        }
    }

    private var musicReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {

            val action = intent.action
            val bundle = intent.extras
            if (action == MusicService.ACTION_MUSIC_STARTED) {
                currentSongIndex = bundle!!.getInt("currentPosition")
                val music = if (isCustomListMode || isFilteredListMode) {
                    musics!![currentSongIndex]
                } else musicList!![currentSongIndex]
                setNavViewAndBottomShit(music)
                prefsManager.saveLastMusicPlayed(music)
                prefsManager.saveLastIndexMusic(currentSongIndex)
            } else if (action == MusicService.ACTION_MUSIC_IN_PROGRESS) {
                viewModel.currentPositionTime.postValue(bundle!!.getInt("currentPositionTime"))
            }
        }
    }


}
