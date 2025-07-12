package com.woa.helper.main

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.view.View
import android.view.Window
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.google.android.material.button.MaterialButton
import com.woa.helper.R
import com.woa.helper.main.MainActivity.Companion.hideBlur
import com.woa.helper.main.MainActivity.Companion.showBlur
import com.woa.helper.widgets.WidgetActivity
import java.lang.Boolean
import java.util.Objects
import kotlin.Int
import kotlin.String
import kotlin.Unit


@SuppressLint("StaticFieldLeak")
object Dlg {

    @JvmField
    var dialog: Dialog? = null
    private var yes: MaterialButton? = null
    private var no: MaterialButton? = null
    private var dismiss: MaterialButton? = null
    private var icon: ImageView? = null
    var bar: ProgressBar? = null
    private var text: TextView? = null
    private var ctx: Context? = null

    fun dialogLoading() {
        setCancelable(Boolean.FALSE)
        clearButtons()
        setText(ctx!!.getString(R.string.please_wait))
    }

    fun show(context: Context, text: String?) {
        if (null != dialog && dialog!!.isShowing) {
            dialog!!.dismiss()
        }
        ctx = context
        dialog = Dialog(context)
        dialog!!.setContentView(R.layout.dialog)
        Objects.requireNonNull<Window?>(dialog!!.window).setBackgroundDrawableResource(android.R.color.transparent)
        yes = dialog!!.findViewById<MaterialButton?>(R.id.yes)
        no = dialog!!.findViewById<MaterialButton?>(R.id.no)
        dismiss = dialog!!.findViewById<MaterialButton?>(R.id.dismiss)
        Dlg.text = dialog!!.findViewById<TextView?>(R.id.messages)
        icon = dialog!!.findViewById<ImageView?>(R.id.icon)
        bar = dialog!!.findViewById<ProgressBar?>(R.id.progress)

        setText(text)
        setCancelable(Boolean.TRUE)
        showBlur()
        dialog!!.setOnDismissListener { v: DialogInterface? ->
            hideBlur(true)
            if (WidgetActivity.active) MainActivity.context!!.finish()
        }

        dialog!!.show()
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
        dialog!!.dismiss()
    }

    fun clearButtons() {
        yes!!.visibility = View.GONE
        no!!.visibility = View.GONE
        dismiss!!.visibility = View.GONE
    }

    fun setCancelable(state: kotlin.Boolean) {
        dialog!!.setCancelable(state)
    }

    fun dismissButton() {
        setDismiss(R.string.dismiss, { obj: Dlg? -> close() } as () -> Unit)
        setCancelable(Boolean.TRUE)
    }

    fun setDismiss(@StringRes stringId: Int, onButtonClick: OnButtonClick) {
        setButton(dismiss!!, ctx!!.getString(stringId), onButtonClick)
    }

    fun setNo(@StringRes stringId: Int, onButtonClick: OnButtonClick) {
        setButton(no!!, ctx!!.getString(stringId), onButtonClick)
    }

    fun setYes(@StringRes stringId: Int, onButtonClick: OnButtonClick) {
        setButton(yes!!, ctx!!.getString(stringId), onButtonClick)
    }

    private fun setButton(button: MaterialButton, text: String?, onButtonClick: OnButtonClick) {
        button.visibility = View.VISIBLE
        button.text = text
        button.setOnClickListener { v: View? -> onButtonClick.execute() }
    }

    fun setText(@StringRes stringId: Int) {
        setText(ctx!!.getString(stringId))
    }

    fun setText(text: String?) {
        Dlg.text!!.text = text
    }

    fun setIcon(@DrawableRes resId: Int) {
        icon!!.visibility = View.VISIBLE
        icon!!.setImageResource(resId)
    }

    fun hideIcon() {
        icon!!.visibility = View.GONE
    }

    private fun setBar(progress: Int, animate: kotlin.Boolean) {
        bar!!.visibility = View.VISIBLE
        bar!!.setProgress(progress, animate)
    }

    fun setBar(progress: Int) {
        setBar(progress, true)
    }

    fun hideBar() {
        bar!!.visibility = View.GONE
    }

    fun onCancel(event: OnButtonClick) {
        dialog!!.setOnCancelListener { v: DialogInterface? ->
            event.execute()
            close()
        }
    }

    fun interface OnButtonClick {
        fun execute()
    }
}
