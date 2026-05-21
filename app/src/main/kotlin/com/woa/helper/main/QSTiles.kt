package com.woa.helper.main

import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.service.quicksettings.TileService
import android.widget.Toast
import com.woa.helper.R
import com.woa.helper.preference.Pref
import com.woa.helper.util.MountManager
import com.woa.helper.util.ShellManager
import com.woa.helper.util.ShellResult
import com.woa.helper.util.DevcfgManager
import com.woa.helper.widget.MountWidget
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

abstract class CommonTileService : TileService() {
    protected fun mount(): String? {
        MountManager.init(filesDir, this)
        return when (val result = MountManager.mount()) {
            is ShellResult.Success -> null
            is ShellResult.Error -> result.message
        }
    }

    protected fun unmount(): String? {
        return when (val result = MountManager.unmount()) {
            is ShellResult.Success -> null
            is ShellResult.Error -> result.message
        }
    }

    protected fun updateTileState(state: Int) {
        Handler(Looper.getMainLooper()).post {
            qsTile.state = state
            qsTile.updateTile()
        }
    }
}

class QuickBootTile : CommonTileService() {
    private var findUefi: String = ""
    private var boot: String = ""

    override fun onStartListening() {
        super.onStartListening()
        Thread {
            ShellManager.init(filesDir)
            checkUefi()
            val newState = if (findUefi.isEmpty() || (isSecure && !Pref.getSecure(this)) || MountManager.getWinPartition().isEmpty()) {
                0
            } else {
                1
            }
            if (Pref.getDevcfg1(this) && !MainActivity.isNetworkConnected(this)) {
                val findDevcfg = ShellManager.exec("find ${filesDir.absolutePath} -maxdepth 1 -name OOS11_devcfg_*")
                if (findDevcfg.isEmpty()) {
                    updateTileState(0)
                    return@Thread
                }
            }
            updateTileState(newState)
        }.start()
    }

    override fun onClick() {
        super.onClick()
        val tile = qsTile
        if (tile.state == 1 && Pref.getConfirm(this)) {
            tile.state = 2
            tile.updateTile()
            return
        }

        Thread {
            ShellManager.init(filesDir)
            val mountError = mount()
            if (mountError != null) {
                Toast.makeText(this, "${getString(R.string.wrong)}\n$mountError", Toast.LENGTH_LONG).show()
                updateTileState(0)
                return@Thread
            }
            val img = if (Pref.getMountLocation(this)) "/mnt/Windows/boot.img" else "${Environment.getExternalStorageDirectory().path}/Windows/boot.img"
            var notFound = ShellManager.exec("ls $img").isEmpty()
            var backupDone = false
            if (Pref.getBackupIfNoneWindows(this) || (Pref.getForceBackupWindows(this) && notFound)) {
                ShellManager.exec("dd bs=8M if=$boot of=$img")
                backupDone = true
            }
            notFound = ShellManager.exec("find -maxdepth 1 /sdcard | grep boot.img").isEmpty()
            if (Pref.getBackupIfNoneAndroid(this) || (Pref.getForceBackupAndroid(this) && notFound)) {
                val sdcard = Environment.getExternalStorageDirectory().path
                ShellManager.exec("dd bs=8M if=$boot of=$sdcard/boot.img")
                backupDone = true
            }
            if (backupDone) {
                val sdf = SimpleDateFormat("dd-MM HH:mm", Locale.US)
                Pref.setDate(this, sdf.format(Date()))
            }
            if (Pref.getDevcfg1(this)) {
                val findDevcfg = ShellManager.exec("find ${filesDir.absolutePath} -maxdepth 1 -name OOS11_devcfg_*")
                if (!MainActivity.isNetworkConnected(this) && findDevcfg.isEmpty()) {
                    Toast.makeText(this, getString(R.string.internet), Toast.LENGTH_LONG).show()
                    updateTileState(0)
                    return@Thread
                }
                val devcfgDevice = DevcfgManager.getDevcfgDevice(Device.codename)
                val backupResult = DevcfgManager.backupDevcfg(filesDir.absolutePath)
                if (backupResult is ShellResult.Error) {
                    Toast.makeText(this, "${getString(R.string.wrong)}\n\n${backupResult.message}", Toast.LENGTH_LONG).show()
                    updateTileState(0)
                    return@Thread
                }
                val downloadResult = DevcfgManager.downloadDevcfgImages(filesDir.absolutePath, devcfgDevice)
                if (downloadResult is ShellResult.Error) {
                    Toast.makeText(this, "${getString(R.string.wrong)}\n\n${downloadResult.message}", Toast.LENGTH_LONG).show()
                    updateTileState(0)
                    return@Thread
                }
                val flashResult = DevcfgManager.flashDevcfg(filesDir.absolutePath, devcfgDevice)
                if (flashResult is ShellResult.Error) {
                    Toast.makeText(this, "${getString(R.string.wrong)}\n\n${flashResult.message}", Toast.LENGTH_LONG).show()
                    updateTileState(0)
                    return@Thread
                }
            }
            if (Pref.getDevcfg2(this) && Pref.getDevcfg1(this)) {
                val copyResult = DevcfgManager.copyDevcfgToWindows(filesDir.absolutePath, useBootSddConf = true, copyBackup = true)
                if (copyResult is ShellResult.Error) {
                    Toast.makeText(this, "${getString(R.string.wrong)}\n\n${copyResult.message}", Toast.LENGTH_LONG).show()
                    updateTileState(0)
                    return@Thread
                }
            }
            flash()
            ShellManager.exec("/system/bin/svc power reboot")
        }.start()
    }

    private fun flash() {
        if (findUefi.isEmpty()) return
        val slotSuffix = ShellManager.exec("getprop ro.boot.slot_suffix")
        ShellManager.exec("dd if=$findUefi of=/dev/block/bootdevice/by-name/boot$slotSuffix bs=16M")
    }

    private fun checkUefi() {
        MountManager.init(filesDir, this)
        Device.init(this)
        findUefi = ShellManager.exec(getString(R.string.uefiChk))
        MountManager.getWinPartition()
        val slotSuffix = ShellManager.exec("getprop ro.boot.slot_suffix")
        val findBoot = ShellManager.exec("find /dev/block | grep -i boot$slotSuffix | head -1")
        boot = if (findBoot.isNotEmpty()) ShellManager.exec("realpath $findBoot") else ""
    }
}

class MountTile : CommonTileService() {

    override fun onStartListening() {
        super.onStartListening()
        Thread {
            ShellManager.init(filesDir)
            update()
        }.start()
    }

    override fun onClick() {
        super.onClick()
        Thread {
            ShellManager.init(filesDir)
            val error = if (MountManager.isMounted()) unmount() else mount()
            if (error != null) {
                Toast.makeText(this, "${getString(R.string.wrong)}\n$error", Toast.LENGTH_LONG).show()
            } else {
                MountWidget.requestUpdate(this)
            }
            update()
        }.start()
    }

    private fun update() {
        if (isSecure && !Pref.getSecure(this)) {
            updateTileState(0)
            return
        }
        if (MountManager.getWinPartition().isEmpty()) {
            updateTileState(0)
            return
        }
        MountManager.init(filesDir, this)
        updateTileState(if (MountManager.isMounted()) 2 else 1)
    }
}
