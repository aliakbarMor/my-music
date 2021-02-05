package com.example.mymusic.view

import android.content.IntentFilter
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.mymusic.R
import com.example.mymusic.ViewModelFactory
import com.example.mymusic.databinding.FragmentPlayMusicBinding
import com.example.mymusic.di.component.DaggerViewModelComponent
import com.example.mymusic.notification.MusicNotification
import com.example.mymusic.viewModel.PlayMusicViewModel
import javax.inject.Inject

class PlayMusic : Fragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private lateinit var binding: FragmentPlayMusicBinding
    private lateinit var viewModel: PlayMusicViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        DaggerViewModelComponent.builder().application(requireActivity().application).build()
            .inject(this)
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_play_music, container, false)
        viewModel =
            ViewModelProvider(this, viewModelFactory).get(PlayMusicViewModel::class.java)
        viewModel.position = PlayMusicArgs.fromBundle(requireArguments()).position
        viewModel.init()

        binding.viewModelMusic = viewModel
        binding.lifecycleOwner = this

        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        val musicIntentFilter = IntentFilter()
        musicIntentFilter.addAction(MusicNotification.ACTION_MUSIC_SKIP_NEXT)
        musicIntentFilter.addAction(MusicNotification.ACTION_MUSIC_STOP)
        musicIntentFilter.addAction(MusicNotification.ACTION_MUSIC_SKIP_PREVIOUS)
        requireActivity().registerReceiver(viewModel.notificationReceiver, musicIntentFilter)
    }
}