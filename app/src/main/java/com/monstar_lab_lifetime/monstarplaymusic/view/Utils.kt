package com.monstar_lab_lifetime.monstarplaymusic.view

import android.widget.TextView
import androidx.databinding.BindingAdapter

class Utils {

    companion object{
        @JvmStatic
        @BindingAdapter("updateText")
        fun updateText(text:TextView,value:String){
            text.text=value
        }
    }
}