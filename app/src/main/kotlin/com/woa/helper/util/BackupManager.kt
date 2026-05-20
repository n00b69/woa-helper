package com.woa.helper.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object BackupManager {
    private const val BACKUP_DIR = "/sdcard/WOAHelper/Backups"

    fun winBackup(bootPartition: String): ShellResult {
        val mountResult = MountManager.mount()
        if (mountResult is ShellResult.Error) return mountResult
        val winPath = MountManager.getWinPath()
        if (winPath.isEmpty()) return ShellResult.Error("Mount path is empty")
        return ShellManager.execResult("dd bs=8M if=$bootPartition of=$winPath/boot.img")
    }

    fun androidBackup(bootPartition: String): ShellResult {
        val mkdirResult = ShellManager.execResult("mkdir -p $BACKUP_DIR")
        if (mkdirResult is ShellResult.Error) return mkdirResult
        return ShellManager.execResult("dd bs=8M if=$bootPartition of=$BACKUP_DIR/boot.img")
    }

    fun modemBackup(): ShellResult {
        val mkdirResult = ShellManager.execResult("mkdir -p $BACKUP_DIR")
        if (mkdirResult is ShellResult.Error) return mkdirResult
        val partitions = listOf("modemst1", "modemst2", "fsc", "fsg", "ftm", "persist", "efs")
        val failures = mutableListOf<String>()
        partitions.forEach { partition ->
            val result = ShellManager.execResult("dd bs=8M if=/dev/block/by-name/$partition of=$BACKUP_DIR/$partition.img")
            if (result is ShellResult.Error) failures.add("$partition: ${result.message}")
        }
        return if (failures.isEmpty()) {
            ShellResult.Success("")
        } else {
            ShellResult.Error("Failed to backup: ${failures.joinToString(", ")}")
        }
    }

    fun updateDate(onUpdate: (String) -> Unit) {
        val date = SimpleDateFormat("dd-MM HH:mm", Locale.US).format(Date())
        onUpdate(date)
    }
}
