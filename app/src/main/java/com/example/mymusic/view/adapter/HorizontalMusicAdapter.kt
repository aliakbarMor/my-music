package com.example.mymusic.view.adapter


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.mymusic.R
import com.example.mymusic.storage.database.Music
import com.example.mymusic.databinding.ItemMusicHorizontaBinding
import com.example.mymusic.viewModel.MusicItemViewModel

class HorizontalMusicAdapter(private val list: List<Music>) :
    RecyclerView.Adapter<HorizontalMusicAdapter.MusicViewHolder>() {

    var musicListener: MusicListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MusicViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemMusicBinding: ItemMusicHorizontaBinding =
            DataBindingUtil.inflate(inflater, R.layout.item_music_horizonta, parent, false)
        return MusicViewHolder(itemMusicBinding)
    }

    override fun getItemCount(): Int {
        return list.size
    }


    override fun onBindViewHolder(holder: MusicViewHolder, position: Int) {
        val music = list[position]

        holder.itemView.setOnClickListener { musicListener?.onMusicClicked(position, true) }
        holder.itemView.setOnLongClickListener {
            musicListener?.onMusicLongClicked(position)
            return@setOnLongClickListener true
        }
        holder.binding.imageSubject.setOnClickListener {
            musicListener?.onSubjectClicked(position, true, holder.binding.imageSubject)
        }

        holder.bind(music)
    }


    class MusicViewHolder(itemMusicHorizontalBinding: ItemMusicHorizontaBinding) :
        RecyclerView.ViewHolder(itemMusicHorizontalBinding.root) {
        val binding = itemMusicHorizontalBinding

        fun bind(music: Music) {
            val musicItemViewModel = MusicItemViewModel(music)
            binding.music = musicItemViewModel
            binding.executePendingBindings()
        }
    }
}