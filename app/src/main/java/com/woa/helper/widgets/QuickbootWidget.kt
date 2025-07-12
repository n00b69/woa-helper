package com.woa.helper.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.widget.RemoteViews
import com.woa.helper.R

class QuickbootWidget : AppWidgetProvider() {
    @android.annotation.SuppressLint("StringFormatInvalid")
    override fun onUpdate(context: android.content.Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            val intent = android.content.Intent(context, QuickbootWidget::class.java)
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            intent.action = WidgetActivity.ACTION_CLICK
            val views = RemoteViews(context.packageName, R.layout.widget_layout)

            val pendingIntent: PendingIntent? = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

            views.setOnClickPendingIntent(R.id.root, pendingIntent)
            views.setImageViewResource(R.id.image, R.drawable.ic_launcher_foreground)

            views.setTextViewText(R.id.text, context.getString(R.string.quickboot_title))
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    override fun onReceive(context: android.content.Context, intent: android.content.Intent) {
        super.onReceive(context, intent)
        if (WidgetActivity.ACTION_CLICK == intent.action) {
            val appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
            val appWidgetManager: AppWidgetManager = AppWidgetManager.getInstance(context)
            val views = RemoteViews(context.packageName, R.layout.widget_layout)

            val i = android.content.Intent(context, WidgetActivity::class.java)
            i.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
            i.putExtra("WIDGET_TYPE", "quickboot")
            context.startActivity(i)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
        if ("UPDATE" == intent.action) {
            val text = intent.getStringExtra("text")

            val appWidgetManager: AppWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds: IntArray = appWidgetManager.getAppWidgetIds(ComponentName(context, QuickbootWidget::class.java))
            onUpdate(context, appWidgetManager, appWidgetIds)

            for (appWidgetId in appWidgetIds) {
                val views = RemoteViews(context.packageName, R.layout.widget_layout)

                views.setTextViewText(R.id.text, text)

                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }
    }

    companion object {
        fun updateText(context: android.content.Context, text: String?) {
            val intent = android.content.Intent(context, QuickbootWidget::class.java)
            intent.action = "UPDATE"
            intent.putExtra("text", text)
            context.sendBroadcast(intent)
        }
    }
}
