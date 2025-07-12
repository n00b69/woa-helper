package com.woa.helper.preference

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.preference.PreferenceManager

enum class Pref {
    ;

    companion object {
        private const val MODEM = "woa unsupported modem"
        private const val BACKUP_QUICK = "woa backup when quickboot windows"
        private const val BACKUP_QUICK_A = "woa backup when quickboot android"
        private const val BACKUP_DATE = "woa last backup date"
        private const val AGREED = "woa unsupported"
        private const val AGREED_NTFS = "ntfs unsupported"
        private const val AUTOBACKUP = "woa force autobackup windows"
        private const val AUTOBACKUP_A = "woa force autobackup android"
        private const val CONFIRMATION = "woa quickboot confirmation"
        const val AUTOMOUNT: String = "woa auto mount preference"
        private const val SECURE = "woa secure tile"
        private const val LOCALE = "woa active language locale"
        const val MOUNT_LOCATION: String = "woa mount location"
        private const val APP_UPDATE = "woa app update"
        private const val WIDGET_OPACITY = "widget opacity"
        private const val DEVCFG1 = "devcfg flasher"
        private const val DEVCFG2 = "devcfg flasher & check for sdd.exe etc."

        fun getSharedPreference(context: Context?): SharedPreferences? {
            return PreferenceManager.getDefaultSharedPreferences(context!!)
        }

        fun setWidgetOpacity(context: Context, value: Int) {
            getSharedPreference(context)!!.edit {
                putInt(WIDGET_OPACITY, value)
            }
        }

        fun getWidgetOpacity(context: Context): Int {
            return getSharedPreference(context)!!.getInt(WIDGET_OPACITY, 255)
        }

        fun setlocale(context: Context, value: String?) {
            getSharedPreference(context)!!.edit {
                putString(LOCALE, value)
            }
        }

        fun getlocale(context: Context): String {
            return getSharedPreference(context)!!.getString(LOCALE, "")!!
        }

        fun setMODEM(context: Context, value: Boolean) {
            getSharedPreference(context)!!.edit {
                putBoolean(MODEM, value)
            }
        }

        fun getMODEM(context: Context): Boolean {
            return getSharedPreference(context)!!.getBoolean(MODEM, false)
        }

        fun setAUTO(context: Context, value: Boolean) {
            getSharedPreference(context)!!.edit {
                putBoolean(AUTOBACKUP, value)
            }
        }

        fun getAUTO(context: Context): Boolean {
            return getSharedPreference(context)!!.getBoolean(AUTOBACKUP, false)
        }

        fun setAutoA(context: Context, value: Boolean) {
            getSharedPreference(context)!!.edit {
                putBoolean(AUTOBACKUP_A, value)
            }
        }

        fun getAutoA(context: Context): Boolean {
            return getSharedPreference(context)!!.getBoolean(AUTOBACKUP_A, false)
        }


        fun setCONFIRM(context: Context, value: Boolean) {
            getSharedPreference(context)!!.edit {
                putBoolean(CONFIRMATION, value)
            }
        }

        fun getCONFIRM(context: Context): Boolean {
            return getSharedPreference(context)!!.getBoolean(CONFIRMATION, false)
        }

        fun setAGREE(context: Context, value: Boolean) {
            getSharedPreference(context)!!.edit {
                putBoolean(AGREED, value)
            }
        }

        fun getAGREE(context: Context): Boolean {
            return getSharedPreference(context)!!.getBoolean(AGREED, false)
        }

        fun setAGREENTFS(context: Context, value: Boolean) {
            getSharedPreference(context)!!.edit {
                putBoolean(AGREED_NTFS, value)
            }
        }

        fun getAGREENTFS(context: Context): Boolean {
            return getSharedPreference(context)!!.getBoolean(AGREED_NTFS, false)
        }

        fun setBACKUP(context: Context, value: Boolean) {
            getSharedPreference(context)!!.edit {
                putBoolean(BACKUP_QUICK, value)
            }
        }

        fun getBACKUP(context: Context): Boolean {
            return getSharedPreference(context)!!.getBoolean(BACKUP_QUICK, false)
        }

        fun setBackupA(context: Context, value: Boolean) {
            getSharedPreference(context)!!.edit {
                putBoolean(BACKUP_QUICK_A, value)
            }
        }

        fun getBackupA(context: Context): Boolean {
            return getSharedPreference(context)!!.getBoolean(BACKUP_QUICK_A, false)
        }

        fun setDATE(context: Context, value: String?) {
            getSharedPreference(context)!!.edit {
                putString(BACKUP_DATE, value)
            }
        }

        fun getDATE(context: Context): String {
            return getSharedPreference(context)!!.getString(BACKUP_DATE, "")!!
        }

        fun setAutoMount(context: Context, value: Boolean) {
            getSharedPreference(context)!!.edit {
                putBoolean(AUTOMOUNT, value)
            }
        }

        fun getAutoMount(context: Context): Boolean {
            return getSharedPreference(context)!!.getBoolean(AUTOMOUNT, false)
        }

        fun setSecure(context: Context, value: Boolean) {
            getSharedPreference(context)!!.edit {
                putBoolean(SECURE, value)
            }
        }

        fun getSecure(context: Context): Boolean {
            return getSharedPreference(context)!!.getBoolean(SECURE, false)
        }

        fun setMountLocation(context: Context, value: Boolean) {
            getSharedPreference(context)!!.edit {
                putBoolean(MOUNT_LOCATION, value)
            }
        }

        fun getMountLocation(context: Context): Boolean {
            return getSharedPreference(context)!!.getBoolean(MOUNT_LOCATION, false)
        }

        fun setAppUpdate(context: Context, value: Boolean) {
            getSharedPreference(context)!!.edit {
                putBoolean(APP_UPDATE, value)
            }
        }

        fun getAppUpdate(context: Context): Boolean {
            return getSharedPreference(context)!!.getBoolean(APP_UPDATE, false)
        }

        fun getDevcfg1(context: Context): Boolean {
            return getSharedPreference(context)!!.getBoolean(DEVCFG1, false)
        }

        fun getDevcfg2(context: Context): Boolean {
            return getSharedPreference(context)!!.getBoolean(DEVCFG2, false)
        }

        fun setDevcfg1(context: Context, value: Boolean) {
            getSharedPreference(context)!!.edit {
                putBoolean(DEVCFG1, value)
            }
        }

        fun setDevcfg2(context: Context, value: Boolean) {
            getSharedPreference(context)!!.edit {
                putBoolean(DEVCFG2, value)
            }
        }
    }
}

