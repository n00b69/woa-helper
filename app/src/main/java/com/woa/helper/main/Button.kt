package com.woa.helper.main

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.annotation.StringRes
import com.google.android.material.card.MaterialCardView
import com.woa.helper.R
import com.woa.helper.databinding.ButtonBinding

class Button(private val context: Context, attrs: AttributeSet?) : MaterialCardView(context, attrs) {
    private val layout: ButtonBinding = ButtonBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.Button, 0, 0)
        if (attrs != null) {
            val title = a.getString(R.styleable.Button_title)
            val subtitle = a.getString(R.styleable.Button_subtitle)
            val imageId = a.getResourceId(R.styleable.Button_image, R.drawable.ic_disk)
            layout.title.text = title
            layout.subtitle.text = subtitle
            layout.image.setImageResource(imageId)
        }
    }

    fun setTitle(@StringRes stringId: Int) {
        layout.title.text = context.getString(stringId)
    }

    fun setTitle(title: String?) {
        layout.title.text = title
    }

    fun setSubtitle(@StringRes stringId: Int) {
        layout.subtitle.text = context.getString(stringId)
    }

    fun setSubtitle(subtitle: String?) {
        layout.subtitle.text = subtitle
    }
}
