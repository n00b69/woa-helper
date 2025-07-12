package com.woa.helper.main

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.topjohnwu.superuser.ShellUtils
import com.woa.helper.preference.pref

class AutoMount : BroadcastReceiver() {
    private var win: String? = null
    private var findWin: String? = null

    override fun onReceive(context: Context?, intent: Intent?) {
        val ok = pref.getSharedPreference(context)?.getBoolean(pref.autoMount, false)
        if (ok == true) {
            findWin = ShellUtils.fastCmd("find /dev/block | grep win")
            if (findWin!!.isEmpty()) findWin = ShellUtils.fastCmd("find /dev/block | grep mindows")
            if (findWin!!.isEmpty()) findWin = ShellUtils.fastCmd("find /dev/block | grep windows")
            if (findWin!!.isEmpty()) findWin = ShellUtils.fastCmd("find /dev/block | grep Win")
            if (findWin!!.isEmpty()) findWin = ShellUtils.fastCmd("find /dev/block | grep Mindows")
            if (findWin!!.isEmpty()) findWin = ShellUtils.fastCmd("find /dev/block | grep Windows")
            win = ShellUtils.fastCmd("realpath $findWin")
            if (pref.getSharedPreference(context)?.getBoolean(pref.mountLocation, false) == true) {
                ShellUtils.fastCmd("su -mm -c mkdir /mnt/Windows || true")
                ShellUtils.fastCmd("cd /data/data/com.woa.helper/files")
                ShellUtils.fastCmd("su -mm -c ./mount.ntfs $win /mnt/Windows")
            } else {
                ShellUtils.fastCmd("su -mm -c mkdir /mnt/sdcard/Windows || true")
                ShellUtils.fastCmd("cd /data/data/com.woa.helper/files")
                ShellUtils.fastCmd("su -mm -c mount.ntfs $win /mnt/sdcard/Windows")
            }
        }
    }
}