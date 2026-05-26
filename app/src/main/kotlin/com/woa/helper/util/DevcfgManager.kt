package com.woa.helper.util

import com.woa.helper.main.Download

object DevcfgManager {
    private const val BACKUP_DIR = "/sdcard/WOAHelper/Backups"
    private const val DEVCFG_BACKUP = "original-devcfg.img"

    fun getDevcfgDevice(device: String): String {
        return if (listOf("guacamole", "OnePlus7Pro", "OnePlus7Pro4G").contains(device)) {
            "guacamole"
        } else {
            "hotdog"
        }
    }

    fun backupDevcfg(filesDir: String): ShellResult {
        val slotSuffix = ShellManager.exec("getprop ro.boot.slot_suffix")
        val mkdirResult = ShellManager.execResult("mkdir -p $BACKUP_DIR")
        if (mkdirResult is ShellResult.Error) return mkdirResult
        if (ShellManager.exec("find $filesDir -maxdepth 1 -name $DEVCFG_BACKUP").isEmpty()) {
            val ddResult = ShellManager.execResult("dd bs=8M if=/dev/block/by-name/devcfg$slotSuffix of=$BACKUP_DIR/$DEVCFG_BACKUP")
            if (ddResult is ShellResult.Error) return ddResult
            return ShellManager.execResult("cp $BACKUP_DIR/$DEVCFG_BACKUP $filesDir/$DEVCFG_BACKUP")
        }
        return ShellResult.Success("")
    }

    fun downloadDevcfgImages(filesDir: String, devcfgDevice: String): ShellResult {
        if (ShellManager.exec("find $filesDir -maxdepth 1 -name OOS11_devcfg_*").isEmpty()) {
            val mkdirResult = ShellManager.execResult("mkdir -p $BACKUP_DIR")
            if (mkdirResult is ShellResult.Error) return mkdirResult
            val oos11Result = Download.file("https://github.com/n00b69/woa-op7/releases/download/Files/OOS11_devcfg_$devcfgDevice.img", "$BACKUP_DIR/OOS11_devcfg_$devcfgDevice.img")
            if (oos11Result is ShellResult.Error) return oos11Result
            val oos12Result = Download.file("https://github.com/n00b69/woa-op7/releases/download/Files/OOS12_devcfg_$devcfgDevice.img", "$BACKUP_DIR/OOS12_devcfg_$devcfgDevice.img")
            if (oos12Result is ShellResult.Error) return oos12Result
            val cp1 = ShellManager.execResult("cp $BACKUP_DIR/OOS11_devcfg_$devcfgDevice.img $filesDir/")
            if (cp1 is ShellResult.Error) return cp1
            return ShellManager.execResult("cp $BACKUP_DIR/OOS12_devcfg_$devcfgDevice.img $filesDir/")
        }
        return ShellResult.Success("")
    }

    fun flashDevcfg(filesDir: String, devcfgDevice: String): ShellResult {
        val slotSuffix = ShellManager.exec("getprop ro.boot.slot_suffix")
        return ShellManager.execResult("dd bs=8M if=$filesDir/OOS11_devcfg_$devcfgDevice.img of=/dev/block/by-name/devcfg$slotSuffix")
    }

    fun copyDevcfgToWindows(filesDir: String, useBootSddConf: Boolean, copyBackup: Boolean): ShellResult {
        val mountResult = MountManager.mount()
        if (mountResult is ShellResult.Error) return mountResult
        val winPath = MountManager.getWinPath()
        if (winPath.isEmpty()) return ShellResult.Error("Mount path is empty")
        val sddConf = if (useBootSddConf) "devcfg-boot-sdd.conf" else "devcfg-sdd.conf"
        val mkdir1 = ShellManager.execResult("mkdir -p /sdcard/WOAHelper/staDevcfg")
        if (mkdir1 is ShellResult.Error) return mkdir1
        val cp1 = ShellManager.execResult("cp $filesDir/sdd.exe /sdcard/WOAHelper/staDevcfg/sdd.exe")
        if (cp1 is ShellResult.Error) return cp1
        val cp2 = ShellManager.execResult("cp $filesDir/$sddConf /sdcard/WOAHelper/staDevcfg/sdd.conf")
        if (cp2 is ShellResult.Error) return cp2
        val mkdir2 = ShellManager.execResult("mkdir -p $winPath/sta")
        if (mkdir2 is ShellResult.Error) return mkdir2
        val cp3 = ShellManager.execResult("cp '$filesDir/Flash Devcfg.lnk' $winPath/Users/Public/Desktop")
        if (cp3 is ShellResult.Error) return cp3
        val cp4 = ShellManager.execResult("cp $filesDir/sdd.exe $winPath/sta/sdd.exe")
        if (cp4 is ShellResult.Error) return cp4
        val cp5 = ShellManager.execResult("cp $filesDir/$sddConf $winPath/sta/sdd.conf")
        if (cp5 is ShellResult.Error) return cp5
        if (copyBackup) {
            val cp6 = ShellManager.execResult("cp $filesDir/$DEVCFG_BACKUP $winPath/original-devcfg.img")
            if (cp6 is ShellResult.Error) return cp6
        }
        return ShellResult.Success("")
    }
}
