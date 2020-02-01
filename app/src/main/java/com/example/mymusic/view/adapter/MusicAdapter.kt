package com.example.mymusic.view.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.mymusic.R
import com.example.mymusic.database.Music
import com.example.mymusic.databinding.ItemMusicVerticalBinding
import com.example.mymusic.viewModel.MusicItemViewModel
import java.util.ArrayList

class MusicAdapter(private var list: List<Music>) :
    RecyclerView.Adapter<MusicAdapter.MusicViewHolder>() {

    var musicListener: MusicListener? = null
    var musicSelected = ArrayList<Music>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MusicViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemMusicBinding: ItemMusicVerticalBinding =
            DataBindingUtil.inflate(inflater, R.layout.item_music_vertical, parent, false)
        return MusicViewHolder(itemMusicBinding)
    }

    override fun getItemCount(): Int {
        return list.size
    }


    override fun onBindViewHolder(holder: MusicViewHolder, position: Int) {
        val music = list[position]

        holder.itemView.setOnClickListener { musicListener?.onMusicClicked(position) }

        holder.itemView.setOnLongClickListener {

            if (music in musicSelected) {
                musicSelected.remove(music)
                holder.disableIconSelect()
            } else {
                musicSelected.add(music)
                holder.visibleIconSelect()
            }

            musicListener?.onMusicLongClicked(position)

            return@setOnLongClickListener true
        }
        holder.binding.imageSubject.setOnClickListener {
            musicListener?.onSubjectClicked(position, holder.binding.imageSubject)
        }
        holder.bind(music)
    }

    fun filterList(filteredMusic: ArrayList<Music>) {
        list = filteredMusic
        notifyDataSetChanged()
    }


    class MusicViewHolder(itemMusicBinding: ItemMusicVerticalBinding) :
        RecyclerView.ViewHolder(itemMusicBinding.root) {
        val binding = itemMusicBinding

        fun bind(music: Music) {
            val musicItemViewModel = MusicItemViewModel(music)
            binding.music = musicItemViewModel
            binding.executePendingBindings()
        }

        fun visibleIconSelect() {
            binding.iconSelect.visibility = View.VISIBLE
        }

        fun disableIconSelect() {
            binding.iconSelect.visibility = View.GONE
        }
    }
}