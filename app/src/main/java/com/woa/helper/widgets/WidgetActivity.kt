package com.woa.helper.widgets

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.topjohnwu.superuser.Shell
import com.woa.helper.BuildConfig
import com.woa.helper.main.Dlg
import com.woa.helper.main.MainActivity
import com.woa.helper.main.MainActivity.Companion.mountUI
import com.woa.helper.main.MainActivity.Companion.quickbootUI
import com.woa.helper.main.MainActivity.Companion.runSilently
import java.lang.Boolean
import kotlin.String

class WidgetActivity : AppCompatActivity() {
    override fun onDestroy() {
        super.onDestroy()
        active = false
        runSilently { Dlg.dialog!!.dismiss() }
        finish()
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
    @SuppressLint("StringFormatInvalid", "SdCardPath")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        active = true

        val intent = getIntent()
        val widgetType = intent.getStringExtra("WIDGET_TYPE")

        MainActivity.context = this
        if (Boolean.TRUE != Shell.isAppGrantedRoot()) {
            Toast.makeText(this, "NO ROOT!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        if (widgetType == "mount") {
            Log.d("INFO", "mount")
            mountUI()
        } else if (widgetType == "quickboot") {
            Log.d("INFO", "quickboot")
            quickbootUI()
        }
        Dlg.dialog!!.setOnCancelListener { dialog: DialogInterface? ->
            dialog!!.dismiss()
            finish()
        }
        Dlg.dialog!!.show()
    }

    companion object {
        const val ACTION_CLICK: String = BuildConfig.APPLICATION_ID + ".ACTION_CLICK"
        var active: kotlin.Boolean = false
    }
}
