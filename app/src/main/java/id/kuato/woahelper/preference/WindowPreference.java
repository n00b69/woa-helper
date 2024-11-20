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
    private static final int EDGE_TO_EDGE_FLAGS = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;

    private final Context context;

    public WindowPreference(final Context context) {
        this.context = context;
    }

    @SuppressWarnings("ApplySharedPref")
    public void toggleEdgeToEdgeEnabled() {
        getSharedPreferences().edit().putBoolean(WindowPreference.KEY_EDGE_TO_EDGE_ENABLED, !isEdgeToEdgeEnabled()).commit();
    }

    public boolean isEdgeToEdgeEnabled() {
        return getSharedPreferences().getBoolean(WindowPreference.KEY_EDGE_TO_EDGE_ENABLED, Build.VERSION_CODES.Q <= Build.VERSION.SDK_INT);
    }

    @SuppressWarnings("RestrictTo")
    public void applyEdgeToEdgePreference(final Window window, final int color) {
        if (Build.VERSION_CODES.LOLLIPOP > Build.VERSION.SDK_INT) {
            return;
        }
        final boolean edgeToEdgeEnabled = isEdgeToEdgeEnabled();

        final int statusBarColor = getStatusBarColor(isEdgeToEdgeEnabled());
        final int navbarColor = getNavBarColor(isEdgeToEdgeEnabled());

        final boolean lightBackground = MaterialColors.isColorLight(MaterialColors.getColor(context, android.R.attr.colorBackground, Color.BLACK));
        final boolean lightNavbar = MaterialColors.isColorLight(navbarColor);
        final boolean showDarkNavbarIcons = lightNavbar || (Color.TRANSPARENT == navbarColor && lightBackground);

        final View decorView = window.getDecorView();
        final int currentStatusBar = MaterialColors.isColorLight(color) && Build.VERSION_CODES.M <= Build.VERSION.SDK_INT ? View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR : 0;
        final int currentNavBar = showDarkNavbarIcons && Build.VERSION_CODES.O <= Build.VERSION.SDK_INT ? View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR : 0;

        window.setNavigationBarColor(navbarColor);
        window.setStatusBarColor(statusBarColor);
        final int systemUiVisibility = (edgeToEdgeEnabled ? WindowPreference.EDGE_TO_EDGE_FLAGS : View.SYSTEM_UI_FLAG_VISIBLE) | currentStatusBar | currentNavBar;

        decorView.setSystemUiVisibility(systemUiVisibility);
    }

    @SuppressWarnings("RestrictTo")
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private int getStatusBarColor(final boolean isEdgeToEdgeEnabled) {
        if (isEdgeToEdgeEnabled && Build.VERSION_CODES.M > Build.VERSION.SDK_INT) {
            final int opaqueStatusBarColor = MaterialColors.getColor(context, android.R.attr.statusBarColor, Color.BLACK);
            return ColorUtils.setAlphaComponent(opaqueStatusBarColor, WindowPreference.EDGE_TO_EDGE_BAR_ALPHA);
        }
        if (isEdgeToEdgeEnabled) {
            return Color.TRANSPARENT;
        }
        return MaterialColors.getColor(context, android.R.attr.statusBarColor, Color.BLACK);
    }

    @SuppressWarnings("RestrictTo")
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private int getNavBarColor(final boolean isEdgeToEdgeEnabled) {
        if (isEdgeToEdgeEnabled && Build.VERSION_CODES.O_MR1 > Build.VERSION.SDK_INT) {
            final int opaqueNavBarColor = MaterialColors.getColor(context, android.R.attr.navigationBarColor, Color.BLACK);
            return ColorUtils.setAlphaComponent(opaqueNavBarColor, WindowPreference.EDGE_TO_EDGE_BAR_ALPHA);
        }
        if (isEdgeToEdgeEnabled) {
            return Color.TRANSPARENT;
        }
        return MaterialColors.getColor(context, android.R.attr.navigationBarColor, Color.BLACK);
    }

    private SharedPreferences getSharedPreferences() {
        return context.getSharedPreferences(WindowPreference.PREFERENCES_NAME, Context.MODE_PRIVATE);
    }
}