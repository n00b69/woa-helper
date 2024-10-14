package id.kuato.woahelper.main;

import android.app.Application;

import androidx.appcompat.app.AppCompatDelegate;

public class Applications extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    }

}