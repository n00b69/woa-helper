package com.woa.helper.main

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.widget.CompoundButton
import android.widget.FrameLayout
import com.woa.helper.R
import com.woa.helper.databinding.SettingsButtonBinding

class SettingsButton(private val context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {
    private val layout: SettingsButtonBinding = SettingsButtonBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.SettingsButton, 0, 0)
        if (null != attrs) {
            val text = a.getString(R.styleable.SettingsButton_text)
            layout.text.text = text
            setCornerRadius()
            setOnChangeListener { state: Boolean -> }
        }
    }

    fun setOnChangeListener(onChangeListener: OnChangeClickListener) {
        layout.switchButton.setOnCheckedChangeListener { v: CompoundButton?, state: Boolean ->
            setCornerRadius()
            onChangeListener.onClick(state)
        }
    }

    fun setChecked(state: Boolean) {
        layout.switchButton.isChecked = state
    }

    private val color: Int
        get() = if (layout.switchButton.isChecked) -0xe69b00 else -0x78fffe

    private fun setCornerRadius() {
        val background = GradientDrawable()
        background.setColor(this.color)
        background.cornerRadius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5.0f, context.resources.displayMetrics)
        layout.getRoot().background = background
    }

    fun interface OnChangeClickListener {
        fun onClick(state: Boolean)
    }
}
