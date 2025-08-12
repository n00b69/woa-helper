package com.woa.helper.main

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.topjohnwu.superuser.ShellUtils
import com.woa.helper.main.MainActivity.Companion.getWin
import com.woa.helper.preference.Pref

class AutoMount : BroadcastReceiver() {
    private var win: String? = null

    override fun onReceive(context: Context?, intent: Intent?) {
        if (Pref.getSharedPreference(context)?.getBoolean(Pref.AUTOMOUNT, false) == true) {
            win = getWin()
            if (Pref.getSharedPreference(context)?.getBoolean(Pref.MOUNT_LOCATION, false) == true) {
                ShellUtils.fastCmd("su -mm -c mkdir /mnt/Windows || true")
                ShellUtils.fastCmd("su -mm -c " + MainActivity.Companion.context!!.filesDir + "/mount.ntfs $win /mnt/Windows")
            } else {
                ShellUtils.fastCmd("su -mm -c mkdir /mnt/sdcard/Windows || true")
                ShellUtils.fastCmd("su -mm -c " + MainActivity.Companion.context!!.filesDir + "/mount.ntfs $win /mnt/sdcard/Windows")
            }
        }
    }
}