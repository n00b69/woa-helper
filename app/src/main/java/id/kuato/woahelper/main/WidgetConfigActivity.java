package id.kuato.woahelper.main;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RemoteViews;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;

import id.kuato.woahelper.BuildConfig;
import id.kuato.woahelper.R;
import id.kuato.woahelper.preference.pref;
import id.kuato.woahelper.widgets.WidgetProvider;

public class WidgetConfigActivity extends AppCompatActivity {
    private int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    public static final String ACTION_CLICK = BuildConfig.APPLICATION_ID + ".ACTION_CLICK";
    public static final String ACTION_SET = BuildConfig.APPLICATION_ID + ".ACTION_SET";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setResult(RESULT_CANCELED);
        setContentView(R.layout.activity_widget_config);


        RadioButton quickboot = findViewById(R.id.quickboot);
        RadioButton mount = findViewById(R.id.mount);

        quickboot.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                mount.setChecked(!b);
            }
        });
        mount.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                quickboot.setChecked(!b);
            }
        });

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(AppWidgetManager.EXTRA_APPWIDGET_ID)) {
            appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }

        TextView opacity = findViewById(R.id.opacity);
        SeekBar seekBar = findViewById(R.id.opacity_seek_bar);
        Button confirmButton = findViewById(R.id.confirm_button);

        seekBar.setProgress(pref.getWidgetOpacity(this));
        opacity.setText("Opacity: " + pref.getWidgetOpacity(this));

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int value, boolean fromUser) {
                pref.setWidgetOpacity(WidgetConfigActivity.this, value);
                opacity.setText("Opacity: " + value);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        confirmButton.setOnClickListener(v -> {
            pref.setWidgetOpacity(WidgetConfigActivity.this, seekBar.getProgress());

            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
            RemoteViews views = new RemoteViews(getPackageName(), R.layout.widget_layout);

            int baseColor = ContextCompat.getColor(this, R.color.md_theme_inverseOnSurface);
            int colorWithAlpha = ColorUtils.setAlphaComponent(baseColor, seekBar.getProgress());

            views.setInt(R.id.root, "setBackgroundColor", colorWithAlpha);

            Intent clickIntent = new Intent(this, WidgetProvider.class)
                    .setAction(ACTION_CLICK)
                    .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            if (quickboot.isChecked()) {
                clickIntent.putExtra("WIDGET_TYPE", "quickboot");
                views.setImageViewResource(R.id.image, R.drawable.ic_launcher_foreground);
                /*String finduefi = "\""+ ShellUtils.fastCmd(WidgetConfigActivity.this.getString(R.string.uefiChk))+"\"";
                if (!finduefi.contains("img")) {
                    views.setTextViewText(R.id.text, this.getString(R.string.uefi_not_found));
                } else views.setTextViewText(R.id.text, this.getString(R.string.quickboot_title));*/
                views.setTextViewText(R.id.text, this.getString(R.string.quickboot_title));
            }
            if (mount.isChecked()) {
                clickIntent.putExtra("WIDGET_TYPE", "mount");
                views.setImageViewResource(R.id.image, R.drawable.ic_mnt);
                /*String mnt_stat = ShellUtils.fastCmd("su -mm -c mount | grep " + MainActivity.win);
                if (mnt_stat.isEmpty()) views.setTextViewText(R.id.text, String.format(this.getString(R.string.mount_question), MainActivity.winpath));
                else views.setTextViewText(R.id.text, this.getString(R.string.unmount_question));*/
                views.setTextViewText(R.id.text, this.getString(R.string.mountt));
            }

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    this,
                    appWidgetId,
                    clickIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );


            views.setOnClickPendingIntent(R.id.root, pendingIntent);

            appWidgetManager.updateAppWidget(appWidgetId, views);

            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            setResult(RESULT_OK, resultValue);
            finish();
        });
    }
}
