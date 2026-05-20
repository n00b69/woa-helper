package com.woa.helper.main

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.woa.helper.preference.Pref
import com.woa.helper.util.MountManager
import com.woa.helper.util.ShellManager
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
                if (filesDir.isNotEmpty()) {
                    ShellManager.init(File(filesDir))
                    MountManager.init(File(filesDir), ctx)
                    if (Pref.getAutoMount(ctx)) {
                        MountManager.getWinPartition()
                        MountManager.mount()
                    }
                } else {
                    Log.w("AutoMount", "filesDir not set, skipping auto-mount")
                }
            } catch (e: Exception) {
                Log.e("AutoMount", "Auto-mount failed", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
