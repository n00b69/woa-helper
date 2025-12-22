package com.woa.helper.util

import android.app.ActivityManager
import android.content.Context

class RAM {
    private fun getTotalMemory(context: Context): Long {
        val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        checkNotNull(actManager)
        actManager.getMemoryInfo(memInfo)
        return memInfo.totalMem
    }

    fun getMemory(context: Context): String {
        val mem: String = MemoryUtils().bytesToHuman(getTotalMemory(context))
        return mem
    }
}
