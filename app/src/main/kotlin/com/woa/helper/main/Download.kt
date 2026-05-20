package com.woa.helper.main

import android.content.Context
import android.os.Build
import android.os.Environment
import com.topjohnwu.superuser.ShellUtils
import java.net.URL

object Download {

    fun permission(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                ShellUtils.fastCmd("appops set --uid ${context.packageName} MANAGE_EXTERNAL_STORAGE allow ")
            }
        }
    }

    fun text(link: String): String {
        return try {
            URL(link).readText()
        } catch (_: Exception) {
            ""
        }
    }
}
