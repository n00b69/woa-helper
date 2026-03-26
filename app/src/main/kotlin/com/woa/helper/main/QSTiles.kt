package com.woa.helper.main

import android.os.Build
import android.os.Environment
import android.service.quicksettings.TileService
import android.widget.Toast
import com.woa.helper.R
import com.woa.helper.main.MainActivity.Companion.isNetworkConnected
import com.woa.helper.main.MainActivity.Companion.rootCommand
import com.woa.helper.main.MainActivity.Companion.shellInit
import com.woa.helper.preference.Pref
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private lateinit var win: String
private lateinit var findWin: String
private lateinit var winPath: String
private var sdcard = Environment.getExternalStorageDirectory().path.toString()

abstract class CommonTileService : TileService() {
    protected fun mount() {
        val mntstat = rootCommand("mount | grep $win")
        if (mntstat.isEmpty()) {
            rootCommand("mkdir $winPath")
            rootCommand("./mount.ntfs $win $winPath", master = true)
        }
    }

    protected fun unmount() {
        rootCommand("umount $winPath", master = true)
        rootCommand("rmdir $winPath")
    }
}

class QuickBootTile : CommonTileService() {
    private lateinit var findUefi: String
    private var device: String? = null
    private var findBoot: String? = null
    private var boot: String? = null

    override fun onStartListening() {
        super.onStartListening()
        shellInit(this.filesDir)
        val tile = qsTile
        checkuefi()
        device = if (null == MainActivity.context)
            Build.DEVICE
        else
            Pref.codenameChanger(false, MainActivity.context!!, Build.DEVICE)
        findWin = rootCommand("find /dev/block | grep -i -E \"win|mindows|windows\" | head -1")
        if (findUefi.isEmpty() || (isSecure && !Pref.getSecure(this)) || findWin.isEmpty()) tile.state =
            0
        else tile.state = 1
        if (Pref.getDevcfg1(this)) {
            if (!isNetworkConnected(this)) {
                val finddevcfg = rootCommand("find $filesDir -maxdepth 1 -name OOS11_devcfg_*")
                if (finddevcfg.isEmpty()) {
                    tile.state = 0
                    // These titles don't actually even seem to work and are obsolete I guess, is there even a way to change the titles?
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        tile.subtitle = getString(R.string.qsinternet)
                    }
                }
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            tile.subtitle = ""
        }
        tile.updateTile()
    }

    override fun onClick() {
        super.onClick()
        val tile = qsTile
        if (1 == tile.state && Pref.getConfirm(this)) {
            tile.state = 2
            if (Build.VERSION_CODES.Q <= Build.VERSION.SDK_INT) {
                tile.subtitle = getString(R.string.qspressagain)
            }
            tile.updateTile()
            return
        }

        mount()
        val img =
            if (Pref.getMountLocation(this)) "/mnt/Windows/boot.img" else "$sdcard/Windows/boot.img"
        var notFound = rootCommand("ls $img").isEmpty()
        var backupDone = false
        if (Pref.getBackup(this) || (!Pref.getAuto(this) && notFound)) {
            rootCommand("dd bs=8388608 if=$boot of=$img")
            backupDone = true
        }
        notFound = rootCommand("find -maxdepth 1 /sdcard | grep boot.img").isEmpty()
        if (Pref.getBackupA(this) || (!Pref.getAutoA(this) && notFound)) {
            rootCommand("dd bs=8388608 if=$boot of=$sdcard/boot.img")
            backupDone = true
        }
        if (backupDone) {
            val sdf = SimpleDateFormat("dd-MM HH:mm", Locale.US)
            val currentDateAndTime = sdf.format(Date())
            Pref.setDate(this, currentDateAndTime)
        }
        // This whole feature feels very laggy to me in the QS tile, needs overhauling.
        if (Pref.getDevcfg1(this)) {
            val findDevCfg = rootCommand("find $filesDir -maxdepth 1 -name OOS11_devcfg_*")
            if (!isNetworkConnected(this)) {
                if (findDevCfg.isEmpty()) {
                    Toast.makeText(this, (getString(R.string.internet)), Toast.LENGTH_LONG).show()
                    tile.state = 0
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        tile.subtitle = getString(R.string.qsinternet)
                    }
                    return
                } else {
                    tile.state = 1
                }
            }
            var devcfgDevice = ""
            when (device) {
                "guacamole", "OnePlus7Pro", "OnePlus7Pro4G" -> devcfgDevice = "guacamole"
                "hotdog", "OnePlus7TPro", "OnePlus7TPro4G" -> devcfgDevice = "hotdog"
            }
            val findoriginaldevcfg =
                rootCommand("find $filesDir -maxdepth 1 -name original-devcfg.img")
            if (findoriginaldevcfg.isEmpty()) {
                rootCommand("dd bs=8M if=/dev/block/by-name/devcfg$(getprop ro.boot.slot_suffix) of=$sdcard/original-devcfg.img")
                rootCommand("cp /sdcard/original-devcfg.img $filesDir/original-devcfg.img")
            }
            if (findDevCfg.isEmpty()) {
                rootCommand("wget https://github.com/n00b69/woa-op7/releases/download/Files/OOS11_devcfg_$devcfgDevice.img -O $sdcard/OOS11_devcfg_$devcfgDevice.img")
                rootCommand("wget https://github.com/n00b69/woa-op7/releases/download/Files/OOS12_devcfg_$devcfgDevice.img -O $sdcard/OOS12_devcfg_$devcfgDevice.img")
                rootCommand("cp $sdcard/OOS11_devcfg_$devcfgDevice.img $filesDir")
                rootCommand("cp /sdcard/OOS12_devcfg_$devcfgDevice.img $filesDir")
            }
            rootCommand("dd bs=8M if=$filesDir/OOS11_devcfg_$devcfgDevice.img of=/dev/block/by-name/devcfg$(getprop ro.boot.slot_suffix)")
        }
        if (Pref.getDevcfg2(this) && Pref.getDevcfg1(this)) {
            rootCommand("mkdir $winPath/sta || true ")
            rootCommand("cp '$filesDir/Flash Devcfg.lnk' $winPath/Users/Public/Desktop")
            rootCommand("cp $filesDir/sdd.exe $winPath/sta/sdd.exe")
            rootCommand("cp $filesDir/devcfg-boot-sdd.conf $winPath/sta/sdd.conf")
            rootCommand("cp $filesDir/original-devcfg.img $winPath/original-devcfg.img")
        }
        //	Toast.makeText(this, "This should appear if it reaches the (disabled) flash uefi section", Toast.LENGTH_LONG).show();
        flash()
        rootCommand("/system/bin/svc power reboot")
    }

    private fun flash() {
        rootCommand("dd if=$findUefi of=/dev/block/bootdevice/by-name/boot$(getprop ro.boot.slot_suffix) bs=16777216")
    }

    private fun checkuefi() {
        findUefi = rootCommand(getString(R.string.uefiChk))
        findWin = rootCommand("find /dev/block | grep -i -E \"win|mindows|windows\" | head -1")
        win = rootCommand("realpath $findWin")
        winPath = (if (Pref.getMountLocation(this)) "/mnt/Windows" else "$sdcard/Windows")
        findBoot = rootCommand("find /dev/block | grep boot$(getprop ro.boot.slot_suffix)")
        if (findBoot!!.isEmpty()) findBoot =
            rootCommand("find /dev/block | grep BOOT$(getprop ro.boot.slot_suffix)")
        boot = rootCommand("realpath $findBoot")
    }
}

class MountTile : CommonTileService() {
    private var mntStat: String? = null

    override fun onStartListening() {
        super.onStartListening()
        shellInit(this.filesDir)
        update()
    }

    override fun onClick() {
        super.onClick()
        if (mntStat!!.isEmpty()) mount()
        else unmount()
        update()
    }

    private fun update() {
        val tile = qsTile
        if (isSecure && !Pref.getSecure(this)) {
            tile.state = 0
            return
        }
        findWin = rootCommand("find /dev/block | grep -i -E \"win|mindows|windows\" | head -1")
        if (findWin.isEmpty()) {
            tile.state = 0
            tile.updateTile()
            return
        }
        win = rootCommand("realpath $findWin")
        winPath = (if (Pref.getMountLocation(this)) "/mnt/Windows" else "$sdcard/Windows")
        mntStat = rootCommand("mount | grep $win")
        if (mntStat!!.isEmpty()) tile.state = 1
        else tile.state = 2
        tile.updateTile()
    }
}
