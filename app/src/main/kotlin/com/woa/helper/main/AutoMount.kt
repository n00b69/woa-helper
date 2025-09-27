package com.woa.helper.main

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.preference.PreferenceManager
import com.topjohnwu.superuser.ShellUtils
import com.woa.helper.main.MainActivity.Companion.getWin
import com.woa.helper.preference.Pref

class AutoMount : BroadcastReceiver() {
    private var win: String? = null

    override fun onReceive(context: Context?, intent: Intent?) {
        val filesdir = PreferenceManager.getDefaultSharedPreferences(context!!).getString(Pref.filesDir,"")
        if (Pref.getSharedPreference(context)?.getBoolean(Pref.AUTOMOUNT, false) == true) {
            win = getWin()
            ShellUtils.fastCmd("cd $filesdir")
            if (Pref.getSharedPreference(context)?.getBoolean(Pref.MOUNT_LOCATION, false) == true) {
                ShellUtils.fastCmd("mkdir /mnt/Windows || true")
                ShellUtils.fastCmd("su -mm -c ./mount.ntfs $win /mnt/Windows")
            } else {
                ShellUtils.fastCmd("mkdir /mnt/sdcard/Windows || true")
                ShellUtils.fastCmd("su -mm -c ./mount.ntfs $win /mnt/sdcard/Windows")
            }
        }
    }
}
