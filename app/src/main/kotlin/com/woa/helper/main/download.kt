package com.woa.helper.main

import android.content.Context
import android.os.Build
import android.os.Environment
import com.topjohnwu.superuser.ShellUtils
import java.io.File
import java.io.FileOutputStream
import java.net.URL

object download{

    fun permission(context : Context){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                ShellUtils.fastCmd("appops set --uid ${context.packageName} MANAGE_EXTERNAL_STORAGE allow ")
            }
        }
    }

    fun file (link : String, location : String) : Boolean {
        val url = URL(link)
        val buffer = ByteArray(20000)
        val file = File(location)
        if (!file.exists()) {
            File(location.substringBeforeLast('/')).mkdirs()
        }
        val file1 = FileOutputStream(location)
        try {
            val thread = Thread {
                val i = url.openStream()
                while ((i.read(buffer)) > 0) {
                    file1.write(buffer, 0, buffer.size)
                }
            }
            thread.start()
            thread.join()
        }catch (_: Exception){return false}
        return true
    }


    fun text(link : String) : String {
        var text =""

            val url = URL(link)
            val thread = Thread{
                try {
                text= url.readText()
                }catch (_: Exception){}
            }
            thread.start()
            thread.join()

        return text
    }

}