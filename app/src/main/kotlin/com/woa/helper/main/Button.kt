package com.woa.helper.main

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.woa.helper.R
import com.woa.helper.databinding.ButtonBinding

class Button(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {
    private val layout: ButtonBinding = ButtonBinding.inflate(LayoutInflater.from(context), this, true)

    private val cornerRadiusPx = resources.getDimension(R.dimen.cardCornerRadius)
    private val strokeWidthPx = resources.getDimensionPixelSize(R.dimen.strokeWidth)
    private val fillColor = context.getColor(R.color.md_theme_inverseOnSurface)
    private val strokeColor = context.getColor(R.color.md_theme_outlineVariant)

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

        clipToOutline = true
        updateBackground()
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        updateBackground()
    }

    private fun updateBackground() {
        alpha = if (isEnabled) 1f else 0.5f
        background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = cornerRadiusPx
            setColor(fillColor)
            setStroke(if (isEnabled) strokeWidthPx else 0, strokeColor)
        }
    }

    fun setTitle(stringId: Int) {
        layout.title.text = context.getString(stringId)
    }

    fun setTitle(title: String?) {
        layout.title.text = title
    }

    fun setSubtitle(stringId: Int) {
        layout.subtitle.text = context.getString(stringId)
    }

    fun setSubtitle(subtitle: String?) {
        layout.subtitle.text = subtitle
    }
}
