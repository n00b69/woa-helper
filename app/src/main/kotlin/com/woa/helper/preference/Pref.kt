package com.woa.helper.preference

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.preference.PreferenceManager

object Pref {
    private const val BACKUP_IF_NONE_WINDOWS = "woa backup when quickboot windows"
    private const val BACKUP_IF_NONE_ANDROID = "woa backup when quickboot android"
    private const val BACKUP_DATE = "woa last backup date"
    private const val AGREED = "woa unsupported"
    private const val BACKUP_FORCE_WINDOWS = "woa force autobackup windows"
    private const val BACKUP_FORCE_ANDROID = "woa force autobackup android"
    private const val CONFIRMATION = "woa quickboot confirmation"
    const val AUTOMOUNT = "woa auto mount preference"
    private const val SECURE = "woa secure tile"
    const val MOUNT_LOCATION = "woa mount location"
    private const val SELINUX = "woa selinux permissive on mount"
    private const val APP_UPDATE = "woa app update"
    private const val DEVCFG1 = "devcfg flasher"
    private const val DEVCFG2 = "devcfg flasher & check for sdd.exe etc."
    private const val CODENAME = "woa_codename_changer"
    const val FILESDIR = "woa_app_filesdir"

    fun getSharedPreference(context: Context): SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(context)

    fun codenameChanger(rw: Boolean, context: Context, value: String): String {
        val prefs = getSharedPreference(context)
        return if (rw) {
            prefs.edit { putString(CODENAME, value) }
            ""
        } else {
            prefs.getString(CODENAME, value) ?: value
        }
    }

    fun setForceBackupWindows(context: Context, value: Boolean) = getSharedPreference(context).edit { putBoolean(BACKUP_FORCE_WINDOWS, value) }
    fun getForceBackupWindows(context: Context): Boolean = getSharedPreference(context).getBoolean(BACKUP_FORCE_WINDOWS, false)

    fun setForceBackupAndroid(context: Context, value: Boolean) = getSharedPreference(context).edit { putBoolean(BACKUP_FORCE_ANDROID, value) }
    fun getForceBackupAndroid(context: Context): Boolean = getSharedPreference(context).getBoolean(BACKUP_FORCE_ANDROID, false)

    fun setConfirm(context: Context, value: Boolean) = getSharedPreference(context).edit { putBoolean(CONFIRMATION, value) }
    fun getConfirm(context: Context): Boolean = getSharedPreference(context).getBoolean(CONFIRMATION, false)

    fun setAGREE(context: Context, value: Boolean) = getSharedPreference(context).edit { putBoolean(AGREED, value) }
    fun getAGREE(context: Context): Boolean = getSharedPreference(context).getBoolean(AGREED, false)

    fun setBackupIfNoneWindows(context: Context, value: Boolean) = getSharedPreference(context).edit { putBoolean(BACKUP_IF_NONE_WINDOWS, value) }
    fun getBackupIfNoneWindows(context: Context): Boolean = getSharedPreference(context).getBoolean(BACKUP_IF_NONE_WINDOWS, true)

    fun setBackupIfNoneAndroid(context: Context, value: Boolean) = getSharedPreference(context).edit { putBoolean(BACKUP_IF_NONE_ANDROID, value) }
    fun getBackupIfNoneAndroid(context: Context): Boolean = getSharedPreference(context).getBoolean(BACKUP_IF_NONE_ANDROID, true)

    fun setDate(context: Context, value: String?) = getSharedPreference(context).edit { putString(BACKUP_DATE, value) }
    fun getDate(context: Context): String = getSharedPreference(context).getString(BACKUP_DATE, "") ?: ""

    fun setAutoMount(context: Context, value: Boolean) = getSharedPreference(context).edit { putBoolean(AUTOMOUNT, value) }
    fun getAutoMount(context: Context): Boolean = getSharedPreference(context).getBoolean(AUTOMOUNT, false)

    fun setSecure(context: Context, value: Boolean) = getSharedPreference(context).edit { putBoolean(SECURE, value) }
    fun getSecure(context: Context): Boolean = getSharedPreference(context).getBoolean(SECURE, false)

    fun setMountLocation(context: Context, value: Boolean) = getSharedPreference(context).edit { putBoolean(MOUNT_LOCATION, value) }
    fun getMountLocation(context: Context): Boolean = getSharedPreference(context).getBoolean(MOUNT_LOCATION, false)

    fun setSelinux(context: Context, value: Boolean) = getSharedPreference(context).edit { putBoolean(SELINUX, value) }
    fun getSelinux(context: Context): Boolean = getSharedPreference(context).getBoolean(SELINUX, false)

    fun setAppUpdate(context: Context, value: Boolean) = getSharedPreference(context).edit { putBoolean(APP_UPDATE, value) }
    fun getAppUpdate(context: Context): Boolean = getSharedPreference(context).getBoolean(APP_UPDATE, false)

    fun getDevcfg1(context: Context): Boolean = getSharedPreference(context).getBoolean(DEVCFG1, false)
    fun getDevcfg2(context: Context): Boolean = getSharedPreference(context).getBoolean(DEVCFG2, false)

    fun setDevcfg1(context: Context, value: Boolean) = getSharedPreference(context).edit { putBoolean(DEVCFG1, value) }
    fun setDevcfg2(context: Context, value: Boolean) = getSharedPreference(context).edit { putBoolean(DEVCFG2, value) }

    fun setFilesDir(context: Context, value: String?) = getSharedPreference(context).edit { putString(FILESDIR, value) }
}
