package com.woa.helper.main

import android.content.Context
import android.graphics.Typeface
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import java.io.File

object FontHelper {

    var font: Typeface = Typeface.DEFAULT
    fun recursiveFont(context: Context, v: Any){
        if ((v !is LinearLayout) && (v !is FrameLayout) ) return
        for (i in 0..<v.childCount) {
            val it = v.getChildAt(i)
            recursiveFont(context, it)
            if (it is TextView) {
                val original = it.typeface.style
                it.typeface = Typeface.create(font,original)
            }
        }
    }

    fun resetFont(){
        font = Typeface.DEFAULT
    }
    fun setFont(file: File){
        font = Typeface.createFromFile(file)
    }

    fun isSet(): Boolean {
        return font != Typeface.DEFAULT
    }

}