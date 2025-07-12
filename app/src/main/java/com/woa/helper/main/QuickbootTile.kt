package com.woa.helper.main

import android.os.Build
import android.os.Environment
import android.service.quicksettings.TileService
import android.widget.Toast
import com.topjohnwu.superuser.ShellUtils
import com.woa.helper.R
import com.woa.helper.main.MainActivity.Companion.isNetworkConnected
import com.woa.helper.preference.pref
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class QuickbootTile : TileService() {
    private var findUefi: String? = null
    private var device: String? = null
    private var win: String? = null
    private var findWin: String? = null
    private var winPath: String? = null
    private var findBoot: String? = null
    private var boot: String? = null

    // Called when your app can update your tile.
    override fun onStartListening() {
        super.onStartListening()
        val tile = qsTile
        checkuefi()
        device = ShellUtils.fastCmd("getprop ro.product.device")
        findWin = ShellUtils.fastCmd("find /dev/block | grep -i -E \"win|mindows|windows\" | head -1")
        if (findUefi!!.isEmpty() || (isSecure && !pref.getSecure(this)) || findWin!!.isEmpty()) tile.state = 0
        else tile.state = 1
        if (pref.getDevcfg1(this)) {
            if (isNetworkConnected(this)) {
                tile.state = 1
            } else {
                val finddevcfg = ShellUtils.fastCmd("find $filesDir -maxdepth 1 -name OOS11_devcfg_*")
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

    // Called when the user taps on your tile in an active or inactive state.
    override fun onClick() {
        super.onClick()
        val tile = qsTile
        if (2 == tile.state || !pref.getCONFIRM(this)) {
            mount()
            var found = ShellUtils.fastCmd("ls " + (if (pref.getMountLocation(this)) "/mnt/Windows" else Environment.getExternalStorageDirectory().path + "/Windows") + " | grep boot.img")
            if (pref.getMountLocation(this)) {
                if (pref.getBACKUP(this) || (!pref.getAUTO(this) && found.isEmpty())) {
                    ShellUtils.fastCmd("dd bs=8m if=$boot of=/mnt/Windows/boot.img")
                    val sdf = SimpleDateFormat("dd-MM HH:mm", Locale.US)
                    val currentDateAndTime = sdf.format(Date())
                    pref.setDATE(this, currentDateAndTime)
                }
            } else {
                if (pref.getBACKUP(this) || (!pref.getAUTO(this) && found.isEmpty())) {
                    ShellUtils.fastCmd("dd bs=8m if=$boot of=/mnt/sdcard/Windows/boot.img")
                    val sdf = SimpleDateFormat("dd-MM HH:mm", Locale.US)
                    val currentDateAndTime = sdf.format(Date())
                    pref.setDATE(this, currentDateAndTime)
                }
            }
            found = ShellUtils.fastCmd("find /sdcard | grep boot.img")
            if (pref.getBACKUP_A(this) || (!pref.getAUTO_A(this) && found.isEmpty())) {
                ShellUtils.fastCmd("dd bs=8m if=$boot of=/sdcard/boot.img")
                val sdf = SimpleDateFormat("dd-MM HH:mm", Locale.US)
                val currentDateAndTime = sdf.format(Date())
                pref.setDATE(this, currentDateAndTime)
            }
            // This whole feature feels very laggy to me in the QS tile, needs overhauling.
            if (pref.getDevcfg1(this)) {
                if (!isNetworkConnected(this)) {
                    val finddevcfg = ShellUtils.fastCmd("find $filesDir -maxdepth 1 -name OOS11_devcfg_*")
                    if (finddevcfg.isEmpty()) {
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
                if ("guacamole" == device || "OnePlus7Pro" == device || "OnePlus7Pro4G" == device) devcfgDevice = "guacamole"
                else if ("hotdog" == device || "OnePlus7TPro" == device || "OnePlus7TPro4G" == device) devcfgDevice = "hotdog"
                val findoriginaldevcfg = ShellUtils.fastCmd("find $filesDir -maxdepth 1 -name original-devcfg.img")
                if (findoriginaldevcfg.isEmpty()) {
                    ShellUtils.fastCmd("dd bs=8M if=/dev/block/by-name/devcfg$(getprop ro.boot.slot_suffix) of=/sdcard/original-devcfg.img")
                    ShellUtils.fastCmd("cp /sdcard/original-devcfg.img $filesDir/original-devcfg.img")
                }
                val finddevcfg = ShellUtils.fastCmd("find $filesDir -maxdepth 1 -name OOS11_devcfg_*")
                if (finddevcfg.isEmpty()) {
                    ShellUtils.fastCmd(String.format("echo \"$(su -mm -c find /data/adb -name busybox) wget https://github.com/n00b69/woa-op7/releases/download/Files/OOS11_devcfg_%s.img -O /sdcard/OOS11_devcfg_%s.img\" | su -mm -c sh", devcfgDevice, devcfgDevice))
                    ShellUtils.fastCmd(String.format("echo \"$(su -mm -c find /data/adb -name busybox) wget https://github.com/n00b69/woa-op7/releases/download/Files/OOS12_devcfg_%s.img -O /sdcard/OOS12_devcfg_%s.img\" | su -mm -c sh", devcfgDevice, devcfgDevice))
                    ShellUtils.fastCmd(String.format("cp /sdcard/OOS11_devcfg_%s.img %s", devcfgDevice, filesDir))
                    ShellUtils.fastCmd(String.format("cp /sdcard/OOS12_devcfg_%s.img %s", devcfgDevice, filesDir))
                    ShellUtils.fastCmd(String.format("dd bs=8M if=%s/OOS11_devcfg_%s.img of=/dev/block/by-name/devcfg$(getprop ro.boot.slot_suffix)", filesDir, devcfgDevice))
                } else {
                    ShellUtils.fastCmd(String.format("dd bs=8M if=%s/OOS11_devcfg_%s.img of=/dev/block/by-name/devcfg$(getprop ro.boot.slot_suffix)", filesDir, devcfgDevice))
                }
            }
            if (pref.getDevcfg2(this) && pref.getDevcfg1(this)) {
                ShellUtils.fastCmd("mkdir $winPath/sta || true ")
                ShellUtils.fastCmd("cp '$filesDir/Flash Devcfg.lnk' $winPath/Users/Public/Desktop")
                ShellUtils.fastCmd("cp $filesDir/sdd.exe $winPath/sta/sdd.exe")
                ShellUtils.fastCmd("cp $filesDir/devcfg-boot-sdd.conf $winPath/sta/sdd.conf")
                ShellUtils.fastCmd("cp $filesDir/original-devcfg.img $winPath/original-devcfg.img")
            }
            //	Toast.makeText(this, "This should appear if it reaches the (disabled) flash uefi section", Toast.LENGTH_LONG).show();
            flash()
            ShellUtils.fastCmd("su -c svc power reboot")
        } else {
            tile.state = 2
            if (Build.VERSION_CODES.Q <= Build.VERSION.SDK_INT) {
                tile.subtitle = getString(R.string.qspressagain)
            }
            tile.updateTile()
        }
    }

    private fun mount() {
        val mntStat = ShellUtils.fastCmd("su -mm -c mount | grep $win")
        if (mntStat.isEmpty()) {
            ShellUtils.fastCmd("mkdir $winPath || true")
            ShellUtils.fastCmd("cd $filesDir")
            ShellUtils.fastCmd("su -mm -c ./mount.ntfs $win $winPath")
        }
    }

    private fun flash() {
        ShellUtils.fastCmd("dd if=$findUefi of=/dev/block/bootdevice/by-name/boot$(getprop ro.boot.slot_suffix) bs=16m")
    }

    private fun checkuefi() {
        findUefi = ShellUtils.fastCmd(getString(R.string.uefiChk))
        findWin = ShellUtils.fastCmd("find /dev/block | grep -i -E \"win|mindows|windows\" | head -1")
        win = ShellUtils.fastCmd("realpath $findWin")
        winPath = (if (pref.getMountLocation(this)) "/mnt/Windows" else Environment.getExternalStorageDirectory().path + "/Windows")
        findBoot = ShellUtils.fastCmd("find /dev/block | grep boot$(getprop ro.boot.slot_suffix)")
        if (findBoot!!.isEmpty()) findBoot = ShellUtils.fastCmd("find /dev/block | grep BOOT$(getprop ro.boot.slot_suffix)")
        boot = ShellUtils.fastCmd("realpath $findBoot")
    }
}
