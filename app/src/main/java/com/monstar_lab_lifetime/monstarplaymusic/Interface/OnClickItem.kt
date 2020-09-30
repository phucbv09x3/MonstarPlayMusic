package com.monstar_lab_lifetime.monstarplaymusic.Interface

import com.monstar_lab_lifetime.monstarplaymusic.model.Music

interface  OnClickItem {
    fun clickItem(music: Music,position:Int)
}