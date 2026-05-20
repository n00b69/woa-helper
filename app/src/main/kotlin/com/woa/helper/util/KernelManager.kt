package com.woa.helper.util

import com.woa.helper.dbkp.Dbkp
import java.io.File

object KernelManager {
    private var tempDir: File? = null

    fun getTempDir(filesDir: File): ShellResult {
        val dir = File(filesDir, "temp")
        if (!dir.mkdirs() && !dir.exists()) return ShellResult.Error("Failed to create temp directory")
        tempDir = dir
        return ShellResult.Success("")
    }

    fun cleanup() {
        tempDir?.let {
            ShellManager.exec("rm -rf ${it.absolutePath}")
            tempDir = null
        }
    }

    fun unpackKernel(bootIMG: String): ShellResult {
        val temp = tempDir ?: return ShellResult.Error("Temp directory not initialized")
        return ShellManager.execResult("( cd ${temp.absolutePath} ; \$(find /data/adb -name magiskboot) unpack $bootIMG)")
    }

    fun repackKernel(bootIMG: String): ShellResult {
        val temp = tempDir ?: return ShellResult.Error("Temp directory not initialized")
        return ShellManager.execResult("( cd ${temp.absolutePath} ; \$(find /data/adb -name magiskboot) repack $bootIMG)")
    }

    fun isPatched(): Boolean {
        val temp = tempDir ?: return false
        return Dbkp.isPatched(File(temp, "kernel"))
    }

    fun patch(kernel: File, fd: File, shellCode: File, patched: File, config: File): Int {
        return Dbkp.patch(kernel, fd, shellCode, patched, config)
    }

    fun removePatch(kernel: File, output: File): Int {
        return Dbkp.removePatch(kernel, output)
    }

    fun updateFD(kernel: File, fd: File, output: File): Int {
        return Dbkp.updateFD(kernel, fd, output)
    }

    fun getKernelFile(): File? = tempDir?.let { File(it, "kernel") }
}
