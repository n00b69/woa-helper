package com.woa.helper.util

import android.content.Context
import android.os.Environment
import com.woa.helper.preference.Pref
import java.io.File

object MountManager {
    private var cachedWinPartition: String = ""
    private var cachedWinPath: String = ""
    private var cachedFilesDir: File? = null
    private var selinuxOnMount: Boolean = false

    fun init(filesDir: File, context: Context) {
        cachedFilesDir = filesDir
        cachedWinPath = if (Pref.getMountLocation(context)) {
            "/mnt/Windows"
        } else {
            "${Environment.getExternalStorageDirectory().path}/Windows"
        }
        selinuxOnMount = Pref.getSelinux(context)
    }

    fun getWinPartition(): String {
        if (cachedWinPartition.isNotEmpty()) return cachedWinPartition
        val partition = ShellManager.exec("find /dev/block | grep -i -E \"win|mindows|windows\" | head -1")
        if (partition.isEmpty()) return ""
        cachedWinPartition = ShellManager.exec("realpath $partition")
        return cachedWinPartition
    }

    fun getWinPath(): String = cachedWinPath

    fun isMounted(): Boolean {
        val win = getWinPartition()
        if (win.isEmpty()) return false
        return ShellManager.execResult("mount | grep -Fq '$win'") is ShellResult.Success
    }

    fun mount(): ShellResult {
        val win = getWinPartition()
        if (win.isEmpty()) return ShellResult.Error("Windows partition not found")
        if (isMounted()) return ShellResult.Success("")
        val winPath = cachedWinPath
        if (winPath.isEmpty()) return ShellResult.Error("Mount path not configured")
        if (selinuxOnMount) {
            val selinuxResult = ShellManager.execResult("echo 0 > /sys/fs/selinux/enforce")
            if (selinuxResult is ShellResult.Error) return selinuxResult
        }
        val mkdirResult = ShellManager.execResult("mkdir -p $winPath")
        if (mkdirResult is ShellResult.Error) return mkdirResult
        val mountResult = ShellManager.execResult("./mount.ntfs $win $winPath", master = true)
        if (mountResult is ShellResult.Error) return mountResult
        return if (isMounted()) ShellResult.Success("") else ShellResult.Error("Mount verification failed")
    }

    fun unmount(): ShellResult {
        val winPath = cachedWinPath
        if (winPath.isEmpty()) return ShellResult.Error("Mount path not configured")
        val umountResult = ShellManager.execResult("umount $winPath", master = true)
        if (umountResult is ShellResult.Error) {
            restoreSelinux()
            return umountResult
        }
        val rmdirResult = ShellManager.execResult("rmdir $winPath")
        if (rmdirResult is ShellResult.Error) {
            restoreSelinux()
            return rmdirResult
        }
        restoreSelinux()
        return ShellResult.Success("")
    }

    private fun restoreSelinux() {
        if (selinuxOnMount) {
            ShellManager.execResult("echo 1 > /sys/fs/selinux/enforce")
        }
    }

    fun resetCache() {
        cachedWinPartition = ""
        cachedWinPath = ""
        selinuxOnMount = false
    }

    fun isSelinuxPermissive(): Boolean {
        return ShellManager.exec("cat /sys/fs/selinux/enforce").trim() == "0"
    }
}
