package id.kuato.woahelper.preference;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

public enum pref {
    ;

    private static final String modem = "woa unsupported modem";
    private static final String backup_quick = "woa backup when quickboot windows";
    private static final String backup_quick_A = "woa backup when quickboot android";
    private static final String backup_date = "woa last backup date";
    private static final String agreed = "woa unsupported";
    private static final String agreedntfs = "ntfs unsupported";
    private static final String autobackup = "woa force autobackup windows";
    private static final String autobackup_A = "woa force autobackup android";
    private static final String confirmation = "woa quickboot confirmation";
    public static final String autoMount = "woa auto mount preference";
    private static final String secure = "woa secure tile";
    private static final String locale = "woa active language locale";
    public static final String mountLocation = "woa mount location";
    private static final String appUpdate = "woa app update";
    private static final String widgetOpacity = "widget opacity";
    private static final String devcfg1 = "devcfg flasher";
    private static final String devcfg2 = "devcfg flasher & check for sdd.exe etc.";

    public static SharedPreferences getSharedPreference(final Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static void setWidgetOpacity(final Context context, final int value) {
        final SharedPreferences.Editor editor = getSharedPreference(context).edit();
        editor.putInt(widgetOpacity, value);
        editor.apply();
    }

    public static int getWidgetOpacity(final Context context) {
        return getSharedPreference(context).getInt(widgetOpacity, 255);
    }

    public static void setlocale(final Context context, final String value) {
        final SharedPreferences.Editor editor = getSharedPreference(context).edit();
        editor.putString(locale, value);
        editor.apply();
    }

    public static String getlocale(final Context context) {
        return getSharedPreference(context).getString(locale, "");
    }

    public static void setMODEM(final Context context, final boolean value) {
        final SharedPreferences.Editor editor = getSharedPreference(context).edit();
        editor.putBoolean(modem, value);
        editor.apply();
    }

    public static boolean getMODEM(final Context context) {
        return getSharedPreference(context).getBoolean(modem, false);
    }

    public static void setAUTO(final Context context, final boolean value) {
        final SharedPreferences.Editor editor = getSharedPreference(context).edit();
        editor.putBoolean(autobackup, value);
        editor.apply();
    }

    public static boolean getAUTO(final Context context) {
        return getSharedPreference(context).getBoolean(autobackup, false);
    }

    public static void setAUTO_A(final Context context, final boolean value) {
        final SharedPreferences.Editor editor = getSharedPreference(context).edit();
        editor.putBoolean(autobackup_A, value);
        editor.apply();
    }

    public static boolean getAUTO_A(final Context context) {
        return getSharedPreference(context).getBoolean(autobackup_A, false);
    }


    public static void setCONFIRM(final Context context, final boolean value) {
        final SharedPreferences.Editor editor = getSharedPreference(context).edit();
        editor.putBoolean(confirmation, value);
        editor.apply();
    }

    public static boolean getCONFIRM(final Context context) {
        return getSharedPreference(context).getBoolean(confirmation, false);
    }

    public static void setAGREE(final Context context, final boolean value) {
        final SharedPreferences.Editor editor = getSharedPreference(context).edit();
        editor.putBoolean(agreed, value);
        editor.apply();
    }

    public static boolean getAGREE(final Context context) {
        return getSharedPreference(context).getBoolean(agreed, false);
    }

    public static void setAGREENTFS(final Context context, final boolean value) {
        final SharedPreferences.Editor editor = getSharedPreference(context).edit();
        editor.putBoolean(agreedntfs, value);
        editor.apply();
    }

    public static boolean getAGREENTFS(final Context context) {
        return getSharedPreference(context).getBoolean(agreedntfs, false);
    }

    public static void setBACKUP(final Context context, final boolean value) {
        final SharedPreferences.Editor editor = getSharedPreference(context).edit();
        editor.putBoolean(backup_quick, value);
        editor.apply();
    }

    public static boolean getBACKUP(final Context context) {
        return getSharedPreference(context).getBoolean(backup_quick, false);
    }

    public static void setBACKUP_A(final Context context, final boolean value) {
        final SharedPreferences.Editor editor = getSharedPreference(context).edit();
        editor.putBoolean(backup_quick_A, value);
        editor.apply();
    }

    public static boolean getBACKUP_A(final Context context) {
        return getSharedPreference(context).getBoolean(backup_quick_A, false);
    }

    public static void setDATE(final Context context, final String value) {
        final SharedPreferences.Editor editor = getSharedPreference(context).edit();
        editor.putString(backup_date, value);
        editor.apply();
    }

    public static String getDATE(final Context context) {
        return getSharedPreference(context).getString(backup_date, "");
    }

    public static void setAutoMount(final Context context, final boolean value) {
        final SharedPreferences.Editor editor = getSharedPreference(context).edit();
        editor.putBoolean(autoMount, value);
        editor.apply();
    }

    public static boolean getAutoMount(final Context context) {
        return getSharedPreference(context).getBoolean(autoMount, false);
    }

    public static void setSecure(final Context context, final boolean value) {
        final SharedPreferences.Editor editor = getSharedPreference(context).edit();
        editor.putBoolean(secure, value);
        editor.apply();
    }

    public static boolean getSecure(final Context context) {
        return getSharedPreference(context).getBoolean(secure, false);
    }

    public static void setMountLocation(final Context context, final boolean value) {
        final SharedPreferences.Editor editor = getSharedPreference(context).edit();
        editor.putBoolean(mountLocation, value);
        editor.apply();
    }

    public static boolean getMountLocation(final Context context) {
        return getSharedPreference(context).getBoolean(mountLocation, false);
    }

    public static void setAppUpdate(final Context context, final boolean value) {
        final SharedPreferences.Editor editor = getSharedPreference(context).edit();
        editor.putBoolean(appUpdate, value);
        editor.apply();
    }

    public static boolean getAppUpdate(final Context context) {
        return getSharedPreference(context).getBoolean(appUpdate, false);
    }

    public static boolean getDevcfg1(final Context context) {
        return getSharedPreference(context).getBoolean(devcfg1, false);
    }

    public static boolean getDevcfg2(final Context context) {
        return getSharedPreference(context).getBoolean(devcfg2, false);
    }

    public static void setDevcfg1(final Context context, final boolean value) {
        final SharedPreferences.Editor editor = getSharedPreference(context).edit();
        editor.putBoolean(devcfg1, value);
        editor.apply();
    }

    public static void setDevcfg2(final Context context, final boolean value) {
        final SharedPreferences.Editor editor = getSharedPreference(context).edit();
        editor.putBoolean(devcfg2, value);
        editor.apply();
    }

}

