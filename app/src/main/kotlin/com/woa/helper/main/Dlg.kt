package com.woa.helper.main

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.woa.helper.R
import com.woa.helper.main.MainActivity.Companion.hideBlur
import com.woa.helper.main.MainActivity.Companion.showBlur

@SuppressLint("StaticFieldLeak")
object Dlg {

    @JvmField
    var dialog: Dialog? = null
    var bar: ProgressBar? = null

    private var yes: Button? = null
    private var no: Button? = null
    private var dismiss: Button? = null
    private var icon: ImageView? = null
    private var text: TextView? = null

    fun dialogLoading() {
        setCancelable(false)
        clearButtons()
        setText(dialog?.context?.getString(R.string.please_wait) ?: "")
    }

    fun show(context: Context, text: String?) {
        if (dialog?.isShowing == true) {
            try { dialog?.dismiss() } catch (_: IllegalArgumentException) {}
        }
        dialog = Dialog(context).apply {
            setContentView(R.layout.dialog)
            window?.setBackgroundDrawableResource(android.R.color.transparent)
        }
        yes = dialog?.findViewById(R.id.yes)
        no = dialog?.findViewById(R.id.no)
        dismiss = dialog?.findViewById(R.id.dismiss)
        this.text = dialog?.findViewById(R.id.messages)
        icon = dialog?.findViewById(R.id.icon)
        bar = dialog?.findViewById(R.id.progress)

        setText(text)
        setCancelable(true)
        if (context is MainActivity) {
            showBlur(context)
        }
        dialog?.setOnDismissListener {
            if (context is MainActivity) {
                hideBlur(context, true)
            }
        }

        dialog?.show()
    }

    fun show(context: Context, @StringRes stringId: Int) {
        show(context, context.getString(stringId))
    }

    fun show(context: Context, @StringRes stringId: Int, @DrawableRes resId: Int) {
        show(context, context.getString(stringId), resId)
    }

    fun show(context: Context, text: String?, @DrawableRes resId: Int) {
        show(context, text)
        setIcon(resId)
    }

    fun close() {
        dialog?.dismiss()
    }

    fun clearButtons() {
        yes?.visibility = View.GONE
        no?.visibility = View.GONE
        dismiss?.visibility = View.GONE
    }

    fun setCancelable(state: Boolean) {
        dialog?.setCancelable(state)
    }

    fun dismissButton() {
        hideBar()
        setDismiss(R.string.dismiss) { dialog?.dismiss() }
        setCancelable(false)
    }

    fun setDismiss(@StringRes stringId: Int, onButtonClick: OnButtonClick) {
        setButton(dismiss, dialog?.context?.getString(stringId), onButtonClick)
    }

    fun setNo(@StringRes stringId: Int, onButtonClick: OnButtonClick) {
        setButton(no, dialog?.context?.getString(stringId), onButtonClick)
    }

    fun setYes(@StringRes stringId: Int, onButtonClick: OnButtonClick) {
        setButton(yes, dialog?.context?.getString(stringId), onButtonClick)
    }

    private fun setButton(button: Button?, text: String?, onButtonClick: OnButtonClick) {
        button?.apply {
            visibility = View.VISIBLE
            this.text = text
            setOnClickListener { onButtonClick.execute() }
        }
    }

    fun setText(@StringRes stringId: Int) {
        setText(dialog?.context?.getString(stringId))
    }

    fun setText(text: String?) {
        this.text?.text = text
    }

    fun setIcon(@DrawableRes resId: Int) {
        icon?.apply {
            visibility = View.VISIBLE
            setImageResource(resId)
        }
    }

    fun hideIcon() {
        icon?.visibility = View.GONE
    }

    private fun setBar(progress: Int, animate: Boolean) {
        bar?.apply {
            visibility = View.VISIBLE
            setProgress(progress, animate)
        }
    }

    fun setBar(progress: Int) {
        setBar(progress, true)
    }

    fun hideBar() {
        bar?.visibility = View.GONE
    }

    fun onCancel(event: OnButtonClick) {
        dialog?.setOnCancelListener {
            event.execute()
            close()
        }
    }

    fun showBackupWarning(context: Context, onAgree: () -> Unit) {
        show(context, context.getString(R.string.bwarn))
        onCancel { }
        setDismiss(R.string.cancel) { close() }
        setYes(R.string.agree) {
            onAgree()
            close()
        }
    }

    fun interface OnButtonClick {
        fun execute()
    }
}
