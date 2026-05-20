package com.woa.helper.main

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.woa.helper.R
import com.woa.helper.databinding.SettingsButtonBinding

class SettingsButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding: SettingsButtonBinding =
        SettingsButtonBinding.inflate(LayoutInflater.from(context), this, true)

    private var onCheckedChangeListener: ((Boolean) -> Unit)? = null

    var isChecked: Boolean
        get() = binding.root.tag as? Boolean ?: false
        set(value) {
            binding.root.tag = value
            updateBackground()
        }

    init {
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.SettingsButton, 0, 0)
        try {
            binding.text.text = a.getString(R.styleable.SettingsButton_text)
        } finally {
            a.recycle()
        }

        binding.root.setOnClickListener {
            if (!isEnabled) return@setOnClickListener
            isChecked = !isChecked
            onCheckedChangeListener?.invoke(isChecked)
        }

        updateBackground()
    }

    fun setOnChangeListener(onChangeListener: OnChangeClickListener) {
        this.onCheckedChangeListener = { state -> onChangeListener.onClick(state) }
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        updateBackground()
    }

    private val backgroundColor: Int
        get() = if (isChecked) 0xFF196500.toInt() else 0xFF870002.toInt()

    private fun updateBackground() {
        val background = GradientDrawable().apply {
            setColor(backgroundColor)
            cornerRadius = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                5.0f,
                resources.displayMetrics
            )
        }
        binding.root.background = background
        binding.root.alpha = if (isEnabled) 1.0f else 0.5f
    }

    fun interface OnChangeClickListener {
        fun onClick(state: Boolean)
    }
}
