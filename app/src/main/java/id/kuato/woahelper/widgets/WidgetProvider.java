package id.kuato.woahelper.widgets;

import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import id.kuato.woahelper.main.WidgetConfigActivity;

public class WidgetProvider extends AppWidgetProvider {

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
