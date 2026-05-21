package com.woa.helper.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.widget.RemoteViews
import android.widget.Toast
import com.woa.helper.R
import com.woa.helper.util.MountManager
import com.woa.helper.util.ShellManager
import com.woa.helper.util.ShellResult

class MountWidget : AppWidgetProvider() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_MOUNT_TOGGLE -> runAsync(context) {
                val result = if (MountManager.isMounted()) MountManager.unmount() else MountManager.mount()
                val isMounted = MountManager.isMounted()
                updateAllWidgets(context, isMounted)
                val msg = when (result) {
                    is ShellResult.Success -> context.getString(if (isMounted) R.string.mounted else R.string.unmounted)
                    is ShellResult.Error -> "Operation failed: ${result.message}"
                }
                Handler(Looper.getMainLooper()).post { Toast.makeText(context, msg, Toast.LENGTH_SHORT).show() }
            }
            ACTION_UPDATE_WIDGET, AppWidgetManager.ACTION_APPWIDGET_UPDATE -> runAsync(context) {
                updateAllWidgets(context, runCatching { MountManager.isMounted() }.getOrDefault(false))
            }
            else -> super.onReceive(context, intent)
        }
    }

    private fun runAsync(context: Context, block: () -> Unit) {
        val pendingResult = goAsync()
        Thread {
            try {
                ShellManager.init(context.filesDir)
                MountManager.init(context.filesDir, context)
                block()
            } catch (e: Exception) {
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } finally {
                pendingResult.finish()
            }
        }.start()
    }

    private fun updateAllWidgets(context: Context, isMounted: Boolean) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val ids = appWidgetManager.getAppWidgetIds(ComponentName(context, MountWidget::class.java))
        val action = context.getString(if (isMounted) R.string.unmountt else R.string.mountt)
        val views = RemoteViews(context.packageName, R.layout.widget_mount).apply {
            setTextViewText(R.id.widget_text, context.getString(R.string.mnt_title, action))
            setImageViewResource(R.id.widget_icon, R.drawable.mnt)
            setOnClickPendingIntent(R.id.widget_container, clickIntent(context))
        }
        ids.forEach { appWidgetManager.updateAppWidget(it, views) }
    }

    private fun clickIntent(context: Context) = PendingIntent.getBroadcast(
        context, 0, Intent(context, MountWidget::class.java).apply { action = ACTION_MOUNT_TOGGLE },
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    companion object {
        const val ACTION_MOUNT_TOGGLE = "com.woa.helper.widget.ACTION_MOUNT_TOGGLE"
        const val ACTION_UPDATE_WIDGET = "com.woa.helper.widget.ACTION_UPDATE_WIDGET"

        fun requestUpdate(context: Context) {
            context.sendBroadcast(Intent(context, MountWidget::class.java).apply { action = ACTION_UPDATE_WIDGET })
        }
    }
}
