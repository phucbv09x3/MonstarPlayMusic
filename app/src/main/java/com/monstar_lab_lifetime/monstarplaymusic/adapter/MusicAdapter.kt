package com.monstar_lab_lifetime.monstarplaymusic.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.monstar_lab_lifetime.monstarplaymusic.`interface`.OnClickItem
import com.monstar_lab_lifetime.monstarplaymusic.databinding.ItemMusicBinding

import com.monstar_lab_lifetime.monstarplaymusic.model.Music
import com.monstar_lab_lifetime.monstarplaymusic.view.HomeActivity
import java.util.zip.Inflater

class MusicAdapter
 : RecyclerView.Adapter<MusicAdapter.MusicHolder>() {
    private  var onClick: OnClickItem?=null
    private var mutableList: MutableList<Music> = mutableListOf()
    class MusicHolder(val binding: ItemMusicBinding):RecyclerView.ViewHolder(binding.root) {
    }

    fun setListMusic( mutableList: MutableList<Music>){
        this.mutableList=mutableList
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MusicAdapter.MusicHolder {
        var root=ItemMusicBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return MusicAdapter.MusicHolder(root)
    }

    override fun getItemCount(): Int {
        return mutableList.size
    }

    override fun onBindViewHolder(holder: MusicAdapter.MusicHolder, position: Int) {
        holder.binding.itemData=mutableList[position]
        holder.binding.root.setOnClickListener {
            onClick?.clickItem(mutableList[position],position)

        }
    }
}