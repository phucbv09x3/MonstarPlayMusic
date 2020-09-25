package com.monstar_lab_lifetime.monstarplaymusic.`interface`

import com.monstar_lab_lifetime.monstarplaymusic.model.Music

interface  OnClickItem {
    fun clickItem(music: Music,position:Int)
}