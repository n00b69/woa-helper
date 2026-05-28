package com.woa.helper.main

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.woa.helper.R
import com.woa.helper.main.MainActivity.Companion.hideBlur
import com.woa.helper.main.MainActivity.Companion.openLink
import com.woa.helper.main.MainActivity.Companion.showBlur
import com.woa.helper.util.ShellResult

@SuppressLint("StaticFieldLeak")
object Dlg {

    @JvmField
    var dialog: Dialog? = null
    private var bar: ProgressBar? = null

    private var yes: Button? = null
    private var no: Button? = null
    private var dismiss: Button? = null
    private var icon: ImageView? = null
    private var text: TextView? = null
    private var filename: TextView? = null

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
        filename = dialog?.findViewById(R.id.filename)

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

    fun show(context: Context, stringId: Int) {
        show(context, context.getString(stringId))
    }

    fun show(context: Context, stringId: Int, resId: Int) {
        show(context, context.getString(stringId), resId)
    }

    fun show(context: Context, text: String?, resId: Int) {
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

    fun setDismiss(stringId: Int, onButtonClick: OnButtonClick) {
        setButton(dismiss, dialog?.context?.getString(stringId), onButtonClick)
    }

    fun setNo(stringId: Int, onButtonClick: OnButtonClick) {
        setButton(no, dialog?.context?.getString(stringId), onButtonClick)
    }

    fun setYes(stringId: Int, onButtonClick: OnButtonClick) {
        setButton(yes, dialog?.context?.getString(stringId), onButtonClick)
    }

    private fun setButton(button: Button?, text: String?, onButtonClick: OnButtonClick) {
        button?.apply {
            visibility = View.VISIBLE
            this.text = text
            setOnClickListener { onButtonClick.execute() }
        }
    }

    fun setText(stringId: Int) {
        setText(dialog?.context?.getString(stringId))
    }

    fun setText(text: String?) {
        this.text?.text = text
    }

    fun setIcon(resId: Int) {
        icon?.apply {
            visibility = View.VISIBLE
            setImageResource(resId)
        }
    }

    fun hideIcon() {
        icon?.visibility = View.GONE
    }

    fun setFileName(name: String?) {
        filename?.apply {
            text = name
            visibility = if (name.isNullOrEmpty()) View.GONE else View.VISIBLE
        }
    }

    fun downloadCallback(): Download.ProgressCallback {
        val handler = Handler(Looper.getMainLooper())
        return Download.ProgressCallback { percent, name ->
            handler.post {
                setBar(percent)
                setFileName(name)
            }
        }
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
        filename?.visibility = View.GONE
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

    fun showMountError(message: String) {
        hideIcon()
        setText("${dialog?.context?.getString(R.string.mountfail)}\n$message")
        setYes(R.string.chat) { MainActivity.openLink(dialog?.context!!, "https://t.me/woahelperchat") }
        setNo(R.string.dismiss) { close() }
    }

    fun showError(result: ShellResult.Error) {
        setText("${dialog?.context?.getString(R.string.wrong)}\n\n${result.message}")
        dismissButton()
    }

    fun interface OnButtonClick {
        fun execute()
    }
}
