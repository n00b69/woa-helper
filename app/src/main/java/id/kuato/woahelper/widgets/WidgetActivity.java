package id.kuato.woahelper.widgets;

import android.annotation.SuppressLint;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.RemoteViews;

import androidx.appcompat.app.AppCompatActivity;

import com.topjohnwu.superuser.Shell;

import java.util.Objects;

import id.kuato.woahelper.BuildConfig;
import id.kuato.woahelper.R;
import id.kuato.woahelper.main.Dlg;
import id.kuato.woahelper.main.MainActivity;

public class 	WidgetActivity extends AppCompatActivity {
	public static final String ACTION_CLICK = BuildConfig.APPLICATION_ID + ".ACTION_CLICK";
	public static boolean active;

	@Override
	protected void onDestroy() {
		super.onDestroy();
		active = false;
		Dlg.dialog.dismiss();
		finish();
	}
	/*private static void sizeCheck(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
		Bundle options = appWidgetManager.getAppWidgetOptions(appWidgetId);
		RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
		int width = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
		if (width <= 100) {
			views.setViewVisibility(R.id.text, View.GONE);
		} else views.setViewVisibility(R.id.text, View.VISIBLE);
		appWidgetManager.updateAppWidget(appWidgetId, views);
	}
	public static void checks(Context context) {
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		int[] mountWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, MountWidget.class));
		int[] quickbootWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, QuickbootWidget.class));
		int[] allWidgetIds = new int[mountWidgetIds.length + quickbootWidgetIds.length];
		System.arraycopy(mountWidgetIds, 0, allWidgetIds, 0, mountWidgetIds.length);
		System.arraycopy(quickbootWidgetIds, 0, allWidgetIds, mountWidgetIds.length, quickbootWidgetIds.length);
		for (int appWidgetId : allWidgetIds) {
			sizeCheck(context, appWidgetManager, appWidgetId);
		}
	}
*/

	@SuppressLint({"StringFormatInvalid", "SdCardPath"})
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

		active = true;

		Intent intent = getIntent();
		String widget_type = intent.getStringExtra("WIDGET_TYPE");

		MainActivity.context = this;
		if (Boolean.FALSE.equals(Shell.isAppGrantedRoot())) return;
		if (Objects.equals(widget_type, "mount")) {
			Log.d("INFO", "mount");
			MainActivity.mountUI();
		} else if (Objects.equals(widget_type, "quickboot")) {
			Log.d("INFO", "quickboot");
			MainActivity.quickbootUI();
		}
		Dlg.dialog.setOnCancelListener((dialog) -> {
			dialog.dismiss();
			finish();
		});
		Dlg.dialog.show();
	}
}