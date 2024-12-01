package id.kuato.woahelper.preference;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

public enum pref {
    ;

    public static final String modem = "woa unsupported modem";
    public static final String backup_quick = "woa backup when quickboot";
    public static final String backup_quick_A = "woa backup when quickboot android";
    public static final String backup_date = "woa last backup date";
    public static final String agreed = "woa unsupported";
	public static final String agreedntfs = "ntfs unsupported";
    public static final String autobackup = "woa autobackup";
    public static final String autobackup_A = "woa autobackup android";
    public static final String confirmation = "woa quickboot confirmation";
    public static final String autoMount = "woa auto mount preference";
    public static final String secure = "woa secure tile";
    public static final String busybox = "woa busybox location";
    public static final String locale = "woa active language locale";
    public static final String version = "woa last app version";
    public static final String mountLocation = "woa mount location";
	public static final String appUpdate = "woa app update";

    public static SharedPreferences getSharedPreference(final Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static void setlocale(final Context context, final String value) {
        final SharedPreferences.Editor editor = pref.getSharedPreference(context).edit();
        editor.putString(pref.locale, value);
        editor.apply();
    }

    public static String getlocale(final Context context) {
        return pref.getSharedPreference(context).getString(pref.locale, "");
    }

    public static void setbusybox(final Context context, final String value) {
        final SharedPreferences.Editor editor = pref.getSharedPreference(context).edit();
        editor.putString(pref.busybox, value);
        editor.apply();
    }

    public static String getbusybox(final Context context) {
        return pref.getSharedPreference(context).getString(pref.busybox, "");
    }

    public static void setMODEM(final Context context, final boolean value) {
        final SharedPreferences.Editor editor = pref.getSharedPreference(context).edit();
        editor.putBoolean(pref.modem, value);
        editor.apply();
    }

    public static boolean getMODEM(final Context context) {
        return pref.getSharedPreference(context).getBoolean(pref.modem, false);
    }

    public static void setAUTO(final Context context, final boolean value) {
        final SharedPreferences.Editor editor = pref.getSharedPreference(context).edit();
        editor.putBoolean(pref.autobackup, value);
        editor.apply();
    }

    public static boolean getAUTO(final Context context) {
        return pref.getSharedPreference(context).getBoolean(pref.autobackup, false);
    }

    public static void setAUTO_A(final Context context, final boolean value) {
        final SharedPreferences.Editor editor = pref.getSharedPreference(context).edit();
        editor.putBoolean(pref.autobackup_A, value);
        editor.apply();
    }

    public static boolean getAUTO_A(final Context context) {
        return pref.getSharedPreference(context).getBoolean(pref.autobackup_A, false);
    }


    public static void setCONFIRM(final Context context, final boolean value) {
        final SharedPreferences.Editor editor = pref.getSharedPreference(context).edit();
        editor.putBoolean(pref.confirmation, value);
        editor.apply();
    }

    public static boolean getCONFIRM(final Context context) {
        return pref.getSharedPreference(context).getBoolean(pref.confirmation, false);
    }

    public static void setAGREE(final Context context, final boolean value) {
        final SharedPreferences.Editor editor = pref.getSharedPreference(context).edit();
        editor.putBoolean(pref.agreed, value);
        editor.apply();
    }

    public static boolean getAGREE(final Context context) {
        return pref.getSharedPreference(context).getBoolean(pref.agreed, false);
    }
	
	public static void setAGREENTFS(final Context context, final boolean value) {
        final SharedPreferences.Editor editor = pref.getSharedPreference(context).edit();
        editor.putBoolean(pref.agreedntfs, value);
        editor.apply();
    }

    public static boolean getAGREENTFS (final Context context) {
        return pref.getSharedPreference(context).getBoolean(pref.agreedntfs, false);
    }

    public static void setBACKUP(final Context context, final boolean value) {
        final SharedPreferences.Editor editor = pref.getSharedPreference(context).edit();
        editor.putBoolean(pref.backup_quick, value);
        editor.apply();
    }

    public static boolean getBACKUP(final Context context) {
        return pref.getSharedPreference(context).getBoolean(pref.backup_quick, false);
    }

    public static void setBACKUP_A(final Context context, final boolean value) {
        final SharedPreferences.Editor editor = pref.getSharedPreference(context).edit();
        editor.putBoolean(pref.backup_quick_A, value);
        editor.apply();
    }

    public static boolean getBACKUP_A(final Context context) {
        return pref.getSharedPreference(context).getBoolean(pref.backup_quick_A, false);
    }

    public static void setDATE(final Context context, final String value) {
        final SharedPreferences.Editor editor = pref.getSharedPreference(context).edit();
        editor.putString(pref.backup_date, value);
        editor.apply();
    }

    public static String getDATE(final Context context) {
        return pref.getSharedPreference(context).getString(pref.backup_date, "");
    }

    public static void setAutoMount(final Context context, final boolean value) {
        final SharedPreferences.Editor editor = pref.getSharedPreference(context).edit();
        editor.putBoolean(pref.autoMount, value);
        editor.apply();
    }

    public static boolean getAutoMount(final Context context) {
        return pref.getSharedPreference(context).getBoolean(pref.autoMount, false);
    }

    public static void setSecure(final Context context, final boolean value) {
        final SharedPreferences.Editor editor = pref.getSharedPreference(context).edit();
        editor.putBoolean(pref.secure, value);
        editor.apply();
    }

    public static boolean getSecure(final Context context) {
        return pref.getSharedPreference(context).getBoolean(pref.secure, false);
    }

    public static void setVersion(final Context context, final String value) {
        final SharedPreferences.Editor editor = pref.getSharedPreference(context).edit();
        editor.putString(pref.version, value);
        editor.apply();
    }

    public static String getVersion(final Context context) {
        return pref.getSharedPreference(context).getString(pref.version, "");
    }

    public static void setMountLocation(final Context context, final boolean value) {
        final SharedPreferences.Editor editor = pref.getSharedPreference(context).edit();
        editor.putBoolean(pref.mountLocation, value);
        editor.apply();
    }

    public static boolean getMountLocation(final Context context) {
        return pref.getSharedPreference(context).getBoolean(pref.mountLocation, false);
    }
	
	public static void setAppUpdate(final Context context, final boolean value) {
        final SharedPreferences.Editor editor = pref.getSharedPreference(context).edit();
        editor.putBoolean(pref.appUpdate, value);
        editor.apply();
    }

    public static boolean getAppUpdate(final Context context) {
        return pref.getSharedPreference(context).getBoolean(pref.appUpdate, false);
    }
}

