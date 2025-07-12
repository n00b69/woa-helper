package com.woa.helper.preference

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.preference.PreferenceManager

enum class pref {
    ;

    companion object {
        private const val modem = "woa unsupported modem"
        private const val backup_quick = "woa backup when quickboot windows"
        private const val backup_quick_A = "woa backup when quickboot android"
        private const val backup_date = "woa last backup date"
        private const val agreed = "woa unsupported"
        private const val agreedntfs = "ntfs unsupported"
        private const val autobackup = "woa force autobackup windows"
        private const val autobackup_A = "woa force autobackup android"
        private const val confirmation = "woa quickboot confirmation"
        const val autoMount: String = "woa auto mount preference"
        private const val secure = "woa secure tile"
        private const val locale = "woa active language locale"
        const val mountLocation: String = "woa mount location"
        private const val appUpdate = "woa app update"
        private const val widgetOpacity = "widget opacity"
        private const val devcfg1 = "devcfg flasher"
        private const val devcfg2 = "devcfg flasher & check for sdd.exe etc."

        fun getSharedPreference(context: Context?): SharedPreferences? {
            return PreferenceManager.getDefaultSharedPreferences(context!!)
        }

        fun setWidgetOpacity(context: Context, value: Int) {
            getSharedPreference(context)!!.edit {
                putInt(widgetOpacity, value)
            }
        }

        fun getWidgetOpacity(context: Context): Int {
            return getSharedPreference(context)!!.getInt(widgetOpacity, 255)
        }

        fun setlocale(context: Context, value: String?) {
            getSharedPreference(context)!!.edit {
                putString(locale, value)
            }
        }

        fun getlocale(context: Context): String {
            return getSharedPreference(context)!!.getString(locale, "")!!
        }

        fun setMODEM(context: Context, value: Boolean) {
            getSharedPreference(context)!!.edit {
                putBoolean(modem, value)
            }
        }

        fun getMODEM(context: Context): Boolean {
            return getSharedPreference(context)!!.getBoolean(modem, false)
        }

        fun setAUTO(context: Context, value: Boolean) {
            getSharedPreference(context)!!.edit {
                putBoolean(autobackup, value)
            }
        }

        fun getAUTO(context: Context): Boolean {
            return getSharedPreference(context)!!.getBoolean(autobackup, false)
        }

        fun setAUTO_A(context: Context, value: Boolean) {
            getSharedPreference(context)!!.edit {
                putBoolean(autobackup_A, value)
            }
        }

        fun getAUTO_A(context: Context): Boolean {
            return getSharedPreference(context)!!.getBoolean(autobackup_A, false)
        }


        fun setCONFIRM(context: Context, value: Boolean) {
            getSharedPreference(context)!!.edit {
                putBoolean(confirmation, value)
            }
        }

        fun getCONFIRM(context: Context): Boolean {
            return getSharedPreference(context)!!.getBoolean(confirmation, false)
        }

        fun setAGREE(context: Context, value: Boolean) {
            getSharedPreference(context)!!.edit {
                putBoolean(agreed, value)
            }
        }

        fun getAGREE(context: Context): Boolean {
            return getSharedPreference(context)!!.getBoolean(agreed, false)
        }

        fun setAGREENTFS(context: Context, value: Boolean) {
            getSharedPreference(context)!!.edit {
                putBoolean(agreedntfs, value)
            }
        }

        fun getAGREENTFS(context: Context): Boolean {
            return getSharedPreference(context)!!.getBoolean(agreedntfs, false)
        }

        fun setBACKUP(context: Context, value: Boolean) {
            getSharedPreference(context)!!.edit {
                putBoolean(backup_quick, value)
            }
        }

        fun getBACKUP(context: Context): Boolean {
            return getSharedPreference(context)!!.getBoolean(backup_quick, false)
        }

        fun setBACKUP_A(context: Context, value: Boolean) {
            getSharedPreference(context)!!.edit {
                putBoolean(backup_quick_A, value)
            }
        }

        fun getBACKUP_A(context: Context): Boolean {
            return getSharedPreference(context)!!.getBoolean(backup_quick_A, false)
        }

        fun setDATE(context: Context, value: String?) {
            getSharedPreference(context)!!.edit {
                putString(backup_date, value)
            }
        }

        fun getDATE(context: Context): String {
            return getSharedPreference(context)!!.getString(backup_date, "")!!
        }

        fun setAutoMount(context: Context, value: Boolean) {
            getSharedPreference(context)!!.edit {
                putBoolean(autoMount, value)
            }
        }

        fun getAutoMount(context: Context): Boolean {
            return getSharedPreference(context)!!.getBoolean(autoMount, false)
        }

        fun setSecure(context: Context, value: Boolean) {
            getSharedPreference(context)!!.edit {
                putBoolean(secure, value)
            }
        }

        fun getSecure(context: Context): Boolean {
            return getSharedPreference(context)!!.getBoolean(secure, false)
        }

        fun setMountLocation(context: Context, value: Boolean) {
            getSharedPreference(context)!!.edit {
                putBoolean(mountLocation, value)
            }
        }

        fun getMountLocation(context: Context): Boolean {
            return getSharedPreference(context)!!.getBoolean(mountLocation, false)
        }

        fun setAppUpdate(context: Context, value: Boolean) {
            getSharedPreference(context)!!.edit {
                putBoolean(appUpdate, value)
            }
        }

        fun getAppUpdate(context: Context): Boolean {
            return getSharedPreference(context)!!.getBoolean(appUpdate, false)
        }

        fun getDevcfg1(context: Context): Boolean {
            return getSharedPreference(context)!!.getBoolean(devcfg1, false)
        }

        fun getDevcfg2(context: Context): Boolean {
            return getSharedPreference(context)!!.getBoolean(devcfg2, false)
        }

        fun setDevcfg1(context: Context, value: Boolean) {
            getSharedPreference(context)!!.edit {
                putBoolean(devcfg1, value)
            }
        }

        fun setDevcfg2(context: Context, value: Boolean) {
            getSharedPreference(context)!!.edit {
                putBoolean(devcfg2, value)
            }
        }
    }
}

