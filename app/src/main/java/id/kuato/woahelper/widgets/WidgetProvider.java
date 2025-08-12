package id.kuato.woahelper.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;

import id.kuato.woahelper.R;
import id.kuato.woahelper.main.WidgetConfigActivity;
import id.kuato.woahelper.preference.pref;

public class WidgetProvider extends AppWidgetProvider {


    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);

            SharedPreferences preferences = pref.getSharedPreference(context);
            String widgetType = preferences.getString("widget_type_" + appWidgetId, "asd");

            int baseColor = ContextCompat.getColor(context, R.color.md_theme_inverseOnSurface);
            int colorWithAlpha = ColorUtils.setAlphaComponent(baseColor, pref.getWidgetOpacity(context));

            views.setInt(R.id.root, "setBackgroundColor", colorWithAlpha);

            Intent clickIntent = new Intent(context, WidgetProvider.class)
                    .setAction(WidgetConfigActivity.ACTION_CLICK)
                    .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                    .putExtra("WIDGET_TYPE", widgetType);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    appWidgetId,
                    clickIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            views.setOnClickPendingIntent(R.id.root, pendingIntent);

            if (widgetType.equals("quickboot")) {
                views.setTextViewText(R.id.text, context.getString(R.string.quickboot_title));
                views.setImageViewResource(R.id.image, R.drawable.ic_launcher_foreground);
            }
            if (widgetType.equals("mount")) {
                views.setTextViewText(R.id.text, context.getString(R.string.mountt));
                views.setImageViewResource(R.id.image, R.drawable.ic_mnt);
            }

            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        String action = intent.getAction();
        Log.d("INFO", action);
        String widgetType = intent.getStringExtra("WIDGET_TYPE");
        if (WidgetConfigActivity.ACTION_CLICK.equals(action)) {
            Intent i = new Intent(context, WidgetActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.putExtra("WIDGET_TYPE", widgetType);
            context.startActivity(i);
        }
    }
}
