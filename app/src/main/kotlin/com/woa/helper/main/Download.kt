package com.woa.helper.main

import android.content.Context
import android.os.Build
import android.os.Environment
import com.topjohnwu.superuser.ShellUtils
import com.woa.helper.util.ShellResult
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
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

    fun file(url: String, output: String): ShellResult {
        val file = File(output)
        return try {
            val dir = file.parentFile
            if (dir != null && !dir.exists()) dir.mkdirs()
            val conn = URL(url).openConnection() as HttpURLConnection
            conn.connectTimeout = 15000
            conn.readTimeout = 30000
            conn.instanceFollowRedirects = true
            val responseCode = conn.responseCode
            if (responseCode != HttpURLConnection.HTTP_OK) {
                conn.disconnect()
                return ShellResult.Error("HTTP $responseCode downloading $url")
            }
            conn.inputStream.use { input ->
                FileOutputStream(file).use { output ->
                    val buf = ByteArray(8192)
                    var n: Int
                    while (input.read(buf).also { n = it } != -1) {
                        output.write(buf, 0, n)
                    }
                }
            }
            conn.disconnect()
            ShellResult.Success("")
        } catch (e: Exception) {
            if (file.exists()) file.delete()
            ShellResult.Error("Failed to download $url: ${e.message}")
        }
    }
}
