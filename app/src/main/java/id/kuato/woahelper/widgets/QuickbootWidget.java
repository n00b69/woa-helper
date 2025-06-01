package id.kuato.woahelper.widgets;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import id.kuato.woahelper.R;
import id.kuato.woahelper.main.MainActivity;

public class QuickbootWidget extends AppWidgetProvider {
    @SuppressLint("StringFormatInvalid")
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            Intent intent = new Intent(context, QuickbootWidget.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            intent.setAction(WidgetActivity.ACTION_CLICK);
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            views.setOnClickPendingIntent(R.id.root, pendingIntent);

            views.setImageViewResource(R.id.image, R.drawable.ic_launcher_foreground);

            MainActivity.context = context;
            updateText(context, context.getString(R.string.quickboot_title));

            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (WidgetActivity.ACTION_CLICK.equals(intent.getAction())) {
            int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);

            Intent i = new Intent(context, WidgetActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.putExtra("WIDGET_TYPE", "quickboot");
            context.startActivity(i);

            appWidgetManager.updateAppWidget(appWidgetId, views);
        }

        if ("UPDATE".equals(intent.getAction())) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, QuickbootWidget.class));
            String text = intent.getStringExtra("text");

            for (int appWidgetId : appWidgetIds) {
                RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);

                views.setTextViewText(R.id.text, text);

                appWidgetManager.updateAppWidget(appWidgetId, views);
            }
        }
    }

    public static void updateText(Context context,  String text) {
        Intent intent = new Intent(context, QuickbootWidget.class);
        intent.setAction("UPDATE");
        intent.putExtra("text", text);
        context.sendBroadcast(intent);
    }
}
