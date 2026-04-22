package com.woa.helper.main

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Environment
import androidx.preference.PreferenceManager
import com.woa.helper.main.MainActivity.Companion.getWin
import com.woa.helper.main.MainActivity.Companion.rootCommand
import com.woa.helper.main.MainActivity.Companion.shellInit
import com.woa.helper.preference.Pref
import java.io.File

class AutoMount : BroadcastReceiver() {
    private var win: String? = null

    override fun onReceive(context: Context?, intent: Intent?) {
        val filesdir = PreferenceManager.getDefaultSharedPreferences(context!!).getString(Pref.FILESDIR, "")
        shellInit(File(filesdir))
        if (Pref.getSharedPreference(context)?.getBoolean(Pref.AUTOMOUNT, false) == true) {
            win = getWin()
            val winDir = if (Pref.getSharedPreference(context)?.getBoolean(Pref.MOUNT_LOCATION, false)==true) "/mnt/Windows" else Environment.getExternalStorageDirectory().path+"/Windows"
            rootCommand("mkdir $winDir")
            rootCommand("./mount.ntfs $win $winDir",master = true)
        }
    }
}
