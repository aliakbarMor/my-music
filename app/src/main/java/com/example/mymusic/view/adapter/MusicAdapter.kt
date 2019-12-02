package com.example.mymusic.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.mymusic.R
import com.example.mymusic.databinding.ItemMusicBinding
import com.example.mymusic.database.Music
import com.example.mymusic.viewModel.MusicItemViewModel

class MusicAdapter(private val list: List<Music>) :
    RecyclerView.Adapter<MusicAdapter.MusicViewHolder>() {

    var musicListener: MusicListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MusicViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemMusicBinding: ItemMusicBinding =
            DataBindingUtil.inflate(inflater, R.layout.item_music, parent, false)
        return MusicViewHolder(itemMusicBinding)
    }

    override fun getItemCount(): Int {
        return list.size
    }


    override fun onBindViewHolder(holder: MusicViewHolder, position: Int) {
        val music = list[position]

        holder.itemView.setOnClickListener { musicListener?.onMusicClicked(position) }
        holder.itemView.setOnLongClickListener {
            musicListener?.onMusicLongClicked(position)
            return@setOnLongClickListener true
        }
        holder.bind(music)
    }


    class MusicViewHolder(private val itemMusicBinding: ItemMusicBinding) :
        RecyclerView.ViewHolder(itemMusicBinding.root) {

        fun bind(music: Music) {
            val musicItemViewModel = MusicItemViewModel(music)
            itemMusicBinding.music = musicItemViewModel
            itemMusicBinding.executePendingBindings()
        }

    }
}