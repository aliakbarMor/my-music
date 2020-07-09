package com.example.mymusic.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.mymusic.R
import com.example.mymusic.storage.database.Music
import com.example.mymusic.databinding.ItemMusicVerticalBinding
import com.example.mymusic.view.MusicListFragment.Companion.selectedMode
import com.example.mymusic.viewModel.MusicItemViewModel
import java.util.ArrayList

class MusicAdapter(private var list: List<Music>) :
    RecyclerView.Adapter<MusicAdapter.MusicViewHolder>() {

    companion object {
        var musicSelected = ArrayList<Music>()
    }

    var musicListener: MusicListener? = null
    var musicPositionSelected = ArrayList<Int>()

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
        holder.itemView.setOnClickListener {
            if (selectedMode)
                toggleSelection(music, position)
            musicListener?.onMusicClicked(position,false)
        }
        holder.itemView.setOnLongClickListener {
            toggleSelection(music, position)
            musicListener?.onMusicLongClicked(position)
            return@setOnLongClickListener true
        }
        holder.binding.imageSubject.setOnClickListener {
            musicListener?.onSubjectClicked(position, false, holder.binding.imageSubject)
        }
        holder.bind(music)
    }

    private fun toggleSelection(music: Music, pos: Int) {
        if (music in musicSelected) {
            musicSelected.remove(music)
            musicPositionSelected.remove(pos)
            notifyItemChanged(pos)
        } else {
            musicSelected.add(music)
            musicPositionSelected.add(pos)
            notifyItemChanged(pos)
        }
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

            if (music in musicSelected) {
                binding.iconSelect.visibility = View.VISIBLE
            } else
                binding.iconSelect.visibility = View.GONE

        }

//        fun visibleIconSelect() {
//            Log.d("aaaa kir tosh", binding.textNameMusic.text.toString())
//        }
//
//        fun disableIconSelect() {
//        }
    }
}