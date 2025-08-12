package id.kuato.woahelper.main;

import android.annotation.SuppressLint;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import id.kuato.woahelper.BuildConfig;
import id.kuato.woahelper.R;
import id.kuato.woahelper.preference.pref;

public class WidgetConfigActivity extends AppCompatActivity {

    public static final String ACTION_CLICK = BuildConfig.APPLICATION_ID + ".ACTION_CLICK";

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setResult(RESULT_CANCELED);
        setContentView(R.layout.activity_widget_config);

        int widgetId = getIntent().getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }

        TextView opacity = findViewById(R.id.opacity);
        SeekBar seekBar = findViewById(R.id.opacity_seek_bar);
        Button confirmButton = findViewById(R.id.confirm_button);
        RadioButton quickboot = findViewById(R.id.quickboot);
        RadioButton mount = findViewById(R.id.mount);

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

        confirmButton.setOnClickListener(v -> {
            pref.setWidgetOpacity(WidgetConfigActivity.this, seekBar.getProgress());
            String widgetType = "";
            if (quickboot.isChecked())
                widgetType = "quickboot";
            if (mount.isChecked())
                widgetType = "mount";

            SharedPreferences preferences = pref.getSharedPreference(this);
            preferences.edit().putString("widget_type_" + widgetId, widgetType).apply();


            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
            setResult(RESULT_OK, resultValue);

            finish();
        });
    }
}
