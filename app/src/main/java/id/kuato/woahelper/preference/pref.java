package id.kuato.woahelper.preference;

import android.content.Context;

import androidx.preference.PreferenceManager;

import android.content.SharedPreferences;

public enum pref {
    ;

    public static final String modem = "woa unsupported modem";
    public static final String backup_quick = "woa backup when quickboot";
    public static final String backup_quick_A = "woa backup when quickboot android";
    public static final String backup_date = "woa last backup date";
    public static final String agreed = "woa unsupported";
    public static final String autobackup = "woa autobackup";
    public static final String autobackup_A = "woa autobackup android";
    public static final String confirmation = "woa quickboot confirmation";
    public static final String autoMount = "woa auto mount preference";
    public static final String secure = "woa secure tile";
    public static final String busybox = "woa busybox location";
    public static final String locale = "woa active language locale";
    public static final String version = "woa last app version";
    public static final String mountLocation = "woa mount location";

    public static SharedPreferences getSharedPreference(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static void setlocale(Context context, String value) {
        SharedPreferences.Editor editor = getSharedPreference(context).edit();
        editor.putString(locale, value);
        editor.apply();
    }

    public static String getlocale(Context context) {
        return getSharedPreference(context).getString(locale, "");
    }

    public static void setbusybox(Context context, String value) {
        SharedPreferences.Editor editor = getSharedPreference(context).edit();
        editor.putString(busybox, value);
        editor.apply();
    }

    public static String getbusybox(Context context) {
        return getSharedPreference(context).getString(busybox, "");
    }

    public static void setMODEM(Context context, boolean value) {
        SharedPreferences.Editor editor = getSharedPreference(context).edit();
        editor.putBoolean(modem, value);
        editor.apply();
    }

    public static boolean getMODEM(Context context) {
        return getSharedPreference(context).getBoolean(modem, false);
    }

    public static void setAUTO(Context context, boolean value) {
        SharedPreferences.Editor editor = getSharedPreference(context).edit();
        editor.putBoolean(autobackup, value);
        editor.apply();
    }

    public static boolean getAUTO(Context context) {
        return getSharedPreference(context).getBoolean(autobackup, false);
    }

    public static void setAUTO_A(Context context, boolean value) {
        SharedPreferences.Editor editor = getSharedPreference(context).edit();
        editor.putBoolean(autobackup_A, value);
        editor.apply();
    }

    public static boolean getAUTO_A(Context context) {
        return getSharedPreference(context).getBoolean(autobackup_A, false);
    }


    public static void setCONFIRM(Context context, boolean value) {
        SharedPreferences.Editor editor = getSharedPreference(context).edit();
        editor.putBoolean(confirmation, value);
        editor.apply();
    }

    public static boolean getCONFIRM(Context context) {
        return getSharedPreference(context).getBoolean(confirmation, false);
    }

    public static void setAGREE(Context context, boolean value) {
        SharedPreferences.Editor editor = getSharedPreference(context).edit();
        editor.putBoolean(agreed, value);
        editor.apply();
    }

    public static boolean getAGREE(Context context) {
        return getSharedPreference(context).getBoolean(agreed, false);
    }

    public static void setBACKUP(Context context, boolean value) {
        SharedPreferences.Editor editor = getSharedPreference(context).edit();
        editor.putBoolean(backup_quick, value);
        editor.apply();
    }

    public static boolean getBACKUP(Context context) {
        return getSharedPreference(context).getBoolean(backup_quick, false);
    }

    public static void setBACKUP_A(Context context, boolean value) {
        SharedPreferences.Editor editor = getSharedPreference(context).edit();
        editor.putBoolean(backup_quick_A, value);
        editor.apply();
    }

    public static boolean getBACKUP_A(Context context) {
        return getSharedPreference(context).getBoolean(backup_quick_A, false);
    }

    public static void setDATE(Context context, String value) {
        SharedPreferences.Editor editor = getSharedPreference(context).edit();
        editor.putString(backup_date, value);
        editor.apply();
    }

    public static String getDATE(Context context) {
        return getSharedPreference(context).getString(backup_date, "");
    }

    public static void setAutoMount(Context context, boolean value) {
        SharedPreferences.Editor editor = getSharedPreference(context).edit();
        editor.putBoolean(autoMount, value);
        editor.apply();
    }

    public static boolean getAutoMount(Context context) {
        return getSharedPreference(context).getBoolean(autoMount, false);
    }

    public static void setSecure(Context context, boolean value) {
        SharedPreferences.Editor editor = getSharedPreference(context).edit();
        editor.putBoolean(secure, value);
        editor.apply();
    }

    public static boolean getSecure(Context context) {
        return getSharedPreference(context).getBoolean(secure, false);
    }

    public static void setVersion(Context context, String value) {
        SharedPreferences.Editor editor = getSharedPreference(context).edit();
        editor.putString(version, value);
        editor.apply();
    }

    public static String getVersion(Context context) {
        return getSharedPreference(context).getString(version, "");
    }

    public static void setMountLocation(Context context, boolean value) {
        SharedPreferences.Editor editor = getSharedPreference(context).edit();
        editor.putBoolean(secure, value);
        editor.apply();
    }

    public static boolean getMountLocation(Context context) {
        return getSharedPreference(context).getBoolean(secure, false);
    }
}

