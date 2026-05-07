package com.woa.helper.main

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.woa.helper.main.MainActivity.Companion.getWin
import com.woa.helper.main.MainActivity.Companion.rootCommand
import com.woa.helper.main.MainActivity.Companion.shellInit
import com.woa.helper.main.MainActivity.Companion.updateWinPath
import com.woa.helper.preference.Pref
import java.io.File
import kotlin.concurrent.thread

class AutoMount : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val ctx = context ?: return
        if (intent?.action != Intent.ACTION_BOOT_COMPLETED) return

        val pendingResult = goAsync()
        
        thread {
            try {
                val filesDir = Pref.getSharedPreference(ctx).getString(Pref.FILESDIR, "") ?: ""
                shellInit(File(filesDir))
                if (Pref.getAutoMount(ctx)) {
                    val winPartition = getWin()
                    val winDir = updateWinPath(ctx)

                    rootCommand("mkdir -p $winDir")
                    rootCommand("./mount.ntfs $winPartition $winDir", master = true)
                }
            } catch (e: Exception) {
                Log.e("AutoMount", "Auto-mount failed", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
