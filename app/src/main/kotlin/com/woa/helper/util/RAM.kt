package com.woa.helper.util

import android.app.ActivityManager
import android.content.Context

object RAM {
    fun getMemory(context: Context): String {
        val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        actManager.getMemoryInfo(memInfo)
        return MemoryUtils.bytesToHuman(memInfo.totalMem)
    }
}
