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

    fun interface ProgressCallback {
        fun onProgress(percent: Int, fileName: String)
    }

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

    fun file(url: String, output: String, onProgress: ProgressCallback? = null): ShellResult {
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
            val contentLength = conn.contentLengthLong
            val fileName = file.name
            conn.inputStream.use { input ->
                FileOutputStream(file).use { out ->
                    val buf = ByteArray(8192)
                    var total: Long = 0
                    var n: Int
                    var lastPercent = -1
                    while (input.read(buf).also { n = it } != -1) {
                        out.write(buf, 0, n)
                        total += n
                        if (onProgress != null && contentLength > 0) {
                            val percent = (total * 100 / contentLength).toInt().coerceIn(0, 100)
                            if (percent != lastPercent) {
                                lastPercent = percent
                                onProgress.onProgress(percent, fileName)
                            }
                        }
                    }
                    if (lastPercent >= 0) onProgress?.onProgress(100, fileName)
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
