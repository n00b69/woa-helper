package id.kuato.woahelper.preference;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.Window;

import androidx.annotation.RequiresApi;
import androidx.core.graphics.ColorUtils;

import com.google.android.material.color.MaterialColors;

public class WindowPreference {

    private static final String PREFERENCES_NAME = "id.kuato.woahelper_preferences";
    private static final String KEY_EDGE_TO_EDGE_ENABLED = "edge_to_edge_enabled";
    private static final int EDGE_TO_EDGE_BAR_ALPHA = 128;

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private static final int EDGE_TO_EDGE_FLAGS = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;

    private final Context context;

    public WindowPreference(Context context) {
        this.context = context;
    }

    @SuppressWarnings("ApplySharedPref")
    public void toggleEdgeToEdgeEnabled() {
        this.getSharedPreferences().edit().putBoolean(KEY_EDGE_TO_EDGE_ENABLED, !this.isEdgeToEdgeEnabled()).commit();
    }

    public boolean isEdgeToEdgeEnabled() {
        return this.getSharedPreferences().getBoolean(KEY_EDGE_TO_EDGE_ENABLED, Build.VERSION_CODES.Q <= Build.VERSION.SDK_INT);
    }

    @SuppressWarnings("RestrictTo")
    public void applyEdgeToEdgePreference(Window window, int color) {
        if (Build.VERSION_CODES.LOLLIPOP > Build.VERSION.SDK_INT) {
            return;
        }
        boolean edgeToEdgeEnabled = this.isEdgeToEdgeEnabled();

        int statusBarColor = this.getStatusBarColor(this.isEdgeToEdgeEnabled());
        int navbarColor = this.getNavBarColor(this.isEdgeToEdgeEnabled());

        boolean lightBackground = MaterialColors.isColorLight(
                MaterialColors.getColor(this.context, android.R.attr.colorBackground, Color.BLACK));
        boolean lightNavbar = MaterialColors.isColorLight(navbarColor);
        boolean showDarkNavbarIcons = lightNavbar || (Color.TRANSPARENT == navbarColor && lightBackground);

        View decorView = window.getDecorView();
        int currentStatusBar = MaterialColors.isColorLight(color) && Build.VERSION_CODES.M <= Build.VERSION.SDK_INT
                ? View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                : 0;
        int currentNavBar = showDarkNavbarIcons && Build.VERSION_CODES.O <= Build.VERSION.SDK_INT
                ? View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                : 0;

        window.setNavigationBarColor(navbarColor);
        window.setStatusBarColor(statusBarColor);
        int systemUiVisibility = (edgeToEdgeEnabled ? EDGE_TO_EDGE_FLAGS : View.SYSTEM_UI_FLAG_VISIBLE) | currentStatusBar
                | currentNavBar;

        decorView.setSystemUiVisibility(systemUiVisibility);
    }

    @SuppressWarnings("RestrictTo")
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private int getStatusBarColor(boolean isEdgeToEdgeEnabled) {
        if (isEdgeToEdgeEnabled && Build.VERSION_CODES.M > Build.VERSION.SDK_INT) {
            int opaqueStatusBarColor = MaterialColors.getColor(this.context, android.R.attr.statusBarColor, Color.BLACK);
            return ColorUtils.setAlphaComponent(opaqueStatusBarColor, EDGE_TO_EDGE_BAR_ALPHA);
        }
        if (isEdgeToEdgeEnabled) {
            return Color.TRANSPARENT;
        }
        return MaterialColors.getColor(this.context, android.R.attr.statusBarColor, Color.BLACK);
    }

    @SuppressWarnings("RestrictTo")
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private int getNavBarColor(boolean isEdgeToEdgeEnabled) {
        if (isEdgeToEdgeEnabled && Build.VERSION_CODES.O_MR1 > Build.VERSION.SDK_INT) {
            int opaqueNavBarColor = MaterialColors.getColor(this.context, android.R.attr.navigationBarColor, Color.BLACK);
            return ColorUtils.setAlphaComponent(opaqueNavBarColor, EDGE_TO_EDGE_BAR_ALPHA);
        }
        if (isEdgeToEdgeEnabled) {
            return Color.TRANSPARENT;
        }
        return MaterialColors.getColor(this.context, android.R.attr.navigationBarColor, Color.BLACK);
    }

    private SharedPreferences getSharedPreferences() {
        return this.context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
    }
}